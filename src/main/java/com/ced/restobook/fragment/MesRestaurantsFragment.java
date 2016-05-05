package com.ced.restobook.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ced.restobook.Main;
import com.ced.restobook.R;
import com.ced.restobook.util.InfoRestaurant;
import com.ced.restobook.provider.RestaurantProvider;
import com.ced.restobook.util.LocalisationManager;
import com.ced.restobook.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MesRestaurantsFragment extends Fragment implements LocationListener {

    public static final String TAG = "MyRestaurants";

    private static final String SAVE_LOCATION_LATITUTE = "save location latitute";
    private static final String SAVE_LOCATION_LONGITUTDE = "save location longitude";
    private static final String SAVE_LOCATION_TIME = "save location time";
    private static final String SAVE_ORDERBY_ASC = "save orderby asc";

    /**
     * Liste des informations des restaurants
     */
    private ArrayList<InfoRestaurant> restaurantsList;

    private RestaurantArrayAdapter adapter;
    private ListView listView;

    private Spinner spinner;
    private String choixSpinner;

    private ImageButton buttonSubmit;
    private ImageButton buttonUp;
    private LinearLayout progressBarLayout;

    private boolean orderByASC = true;
    private boolean orderByDistance = false;

    private LocalisationManager localisationManager;
    /**
     * Dernière Location connue
     */
    private Location currentLocation;

    /**
     * Temps en minute avant de regéolocaliser
     */
    private long DELTA_TIME = 2;


    public static MesRestaurantsFragment newInstance() {
        MesRestaurantsFragment fragment = new MesRestaurantsFragment();
        return fragment;
    }

    public MesRestaurantsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restaurantsList = new ArrayList<InfoRestaurant>();
        adapter = new RestaurantArrayAdapter(getContext(), restaurantsList);
        choixSpinner = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mes_restaurants, container, false);

        restaurantsList.clear();

        // ListView
        listView = (ListView) v.findViewById(R.id.my_restos_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InfoRestaurant target = (InfoRestaurant) parent.getItemAtPosition(position);
                ((Main) getActivity()).setCurrentSelectedRestaurant(target);
                ((Main) getActivity()).changeFragment(getString(R.string.section_detail_resto));
            }
        });

        // Spinner
        spinner = (Spinner) v.findViewById(R.id.my_restos_orderby_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.my_restos_orderby_spinner, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choixSpinner = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Button Spinner
        buttonSubmit = (ImageButton) v.findViewById(R.id.my_restos_orderby_button);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateListStart();
            }
        });

        buttonUp = (ImageButton) v.findViewById(R.id.my_restos_orderby_up_down_button);
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderByASC = !orderByASC;
                updateButtonOrderByAsc();
            }
        });

        // Progress Bar
        progressBarLayout  = (LinearLayout) v.findViewById(R.id.my_restos_progress_bar_layout);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SAVE_ORDERBY_ASC)){
                orderByASC = savedInstanceState.getBoolean(SAVE_ORDERBY_ASC);
            }
        }

        updateButtonOrderByAsc();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SAVE_LOCATION_LATITUTE) &&
                    savedInstanceState.containsKey(SAVE_LOCATION_LONGITUTDE) &&
                    savedInstanceState.containsKey(SAVE_LOCATION_TIME)){
                currentLocation = new Location("");
                currentLocation.setLatitude(savedInstanceState.getDouble(SAVE_LOCATION_LATITUTE));
                currentLocation.setLongitude(savedInstanceState.getDouble(SAVE_LOCATION_LONGITUTDE));
                currentLocation.setTime(savedInstanceState.getLong(SAVE_LOCATION_TIME));
            }
        }
        listView.setAdapter(adapter);
        updateListStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(currentLocation != null){
            outState.putDouble(SAVE_LOCATION_LATITUTE, currentLocation.getLatitude());
            outState.putDouble(SAVE_LOCATION_LONGITUTDE, currentLocation.getLongitude());
            outState.putLong(SAVE_LOCATION_TIME, currentLocation.getTime());
        }
        outState.putBoolean(SAVE_ORDERBY_ASC, orderByASC);
        super.onSaveInstanceState(outState);
    }


    /**
     * Lance la récupération des informations des restaurants dans la base de donnée
     */
    public void updateListStart(){
        orderByDistance = choixSpinner != null && choixSpinner.equals(getString(R.string.my_restos_spinner_distance));
        if(orderByDistance){
            if(currentLocation != null &&
                    SystemClock.elapsedRealtimeNanos() - currentLocation.getElapsedRealtimeNanos() < TimeUnit.MINUTES.toNanos(DELTA_TIME)){
                progressBarLayout.setVisibility(View.VISIBLE);
                onLocationChanged(currentLocation);
            }else {
                localisationManager = new LocalisationManager(getActivity());
                if(localisationManager.singleUpdate(this)) {
                    Toast.makeText(getActivity(), "Géolocalisation en cours ...", Toast.LENGTH_LONG).show();
                    progressBarLayout.setVisibility(View.VISIBLE);
                }else
                    Toast.makeText(getActivity(), "Impossible d'accéder au service de géolocalisation", Toast.LENGTH_SHORT).show();
            }
        }else {
            progressBarLayout.setVisibility(View.VISIBLE);
            onLocationChanged(currentLocation);
        }
    }

    /**
     * Une fois que la récupération des informations est terminé
     * @param list
     */
    public void updateListEnd(List<InfoRestaurant> list){
        restaurantsList.clear();
        restaurantsList.addAll(list);

        if(orderByDistance){
            Location tmp;
            for(InfoRestaurant resto : restaurantsList){
                tmp = new Location("");
                tmp.setLongitude(resto.longitude);
                tmp.setLatitude(resto.latitude);

                if(resto.latitude != 0.0 && resto.longitude != 0.0)
                    resto.distance = (int) currentLocation.distanceTo(tmp);
                else
                    resto.distance = -1;
            }
        }

        adapter.notifyDataSetChanged();
        progressBarLayout.setVisibility(View.GONE);
    }

    /**
     * Change l'image du bouton d'ordre ASC DESC
     */
    private void updateButtonOrderByAsc(){
        if(orderByASC) {
            buttonUp.setImageDrawable(getActivity().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
        }else {
            buttonUp.setImageDrawable(getActivity().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
        }
    }


    /*
            LOCATION LISTENER IMPLEMENTATION
     */

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        if(orderByDistance){
            localisationManager.removeUpdates(this);
        }

        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;

        String orderBy = null;
        orderByDistance = false;
        if(choixSpinner == null)
            orderBy = RestaurantProvider.Restaurant.NOM;
        else if(choixSpinner.equals(getString(R.string.my_restos_spinner_nom)))
            orderBy = RestaurantProvider.Restaurant.NOM;
        else if(choixSpinner.equals(getString(R.string.my_restos_spinner_note)))
            orderBy = RestaurantProvider.Restaurant.NOTE;
        else if(choixSpinner.equals(getString(R.string.my_restos_spinner_date)))
            orderBy = RestaurantProvider.Restaurant.DATE_AJOUT;
        else if(choixSpinner.equals(getString(R.string.my_restos_spinner_distance))){
            orderByDistance = true;
            orderBy = RestaurantProvider.Restaurant.NOM;
        }

        if(orderByASC)
            orderBy += " ASC";
        else
            orderBy += " DESC";

        new LoaderQuery().execute(projection,
                new String[]{selection},
                selectionArgs,
                new String[]{orderBy});
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}



    /*
            ADAPTER RESTAURANTS LIST
     */

    private static class ViewHolder{
        TextView textTitre;
        TextView textAdresse;
        TextView textDistance;
        RatingBar ratingbar;
    }

    class RestaurantArrayAdapter extends ArrayAdapter<InfoRestaurant> {

        public RestaurantArrayAdapter(Context context, List<InfoRestaurant> items) {
            super(context, R.layout.fragment_mes_restaurants_row, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InfoRestaurant target = (InfoRestaurant) getItem(position);
            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater(null).inflate(R.layout.fragment_mes_restaurants_row, parent, false);
                viewHolder.textTitre = (TextView) convertView.findViewById(R.id.my_restos_title);
                viewHolder.textAdresse = (TextView) convertView.findViewById(R.id.my_restos_addresse);
                viewHolder.ratingbar = (RatingBar) convertView.findViewById(R.id.my_restos_rating_bar);
                viewHolder.textDistance = (TextView) convertView.findViewById(R.id.my_restos_distance);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textTitre.setText(target.nom);
            viewHolder.textAdresse.setText(target.adresse);
            viewHolder.ratingbar.setRating(target.note);

            if(orderByDistance && target.distance != -1){
                viewHolder.textDistance.setVisibility(View.VISIBLE);
                viewHolder.textDistance.setText(Util.distanceToString(target.distance));
            }else{
                viewHolder.textDistance.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            setNotifyOnChange(false);
            sort(new Comparator<InfoRestaurant>() {
                @Override
                public int compare(InfoRestaurant lhs, InfoRestaurant rhs) {
                    if(lhs.distance < rhs.distance)
                        return (orderByASC)? -1 : 1;
                    else if(lhs.distance > rhs.distance)
                        return (orderByASC)? 1 : -1;
                    return 0;
                }
            });
            setNotifyOnChange(true);
            super.notifyDataSetChanged();
        }
    }


    /*
            ASYNCTASK : recupere la liste des restaurants
     */

    class LoaderQuery extends AsyncTask<String[], Void, List>{

        private static final String TAG = "LoaderQuery";

        @Override
        protected List doInBackground(String[]... params) {
            String[] projection = params[0];
            String selection = (params[1] != null)? params[1][0] : null;
            String[] selectionArgs = params[2];
            String orderBy = (params[3] != null)? params[3][0]: null;

            Cursor cursor = getActivity().getContentResolver().query(RestaurantProvider.Restaurant.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    orderBy);

            if(cursor == null) {
                Log.d(TAG, "cursor is null");
                cancel(true);
            }

            List<InfoRestaurant> list = new ArrayList<InfoRestaurant>();
            cursor.moveToFirst();
            int order = 0;
            while(!cursor.isAfterLast()){
                InfoRestaurant info = new InfoRestaurant();
                info.fromCursorRow(cursor);
                info.order = order;
                order++;
                list.add(info);
                cursor.moveToNext();
            }

            cursor.close();
            Log.d(TAG, "cursor count: "+cursor.getCount());
            return list;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            updateListEnd(list);
        }
    }

}
