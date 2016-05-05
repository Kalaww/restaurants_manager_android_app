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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ced.restobook.Main;
import com.ced.restobook.R;
import com.ced.restobook.util.LocalisationManager;
import com.ced.restobook.util.InfoRestaurant;
import com.ced.restobook.provider.RestaurantProvider;
import com.ced.restobook.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ProximityFragment extends Fragment implements LocationListener {

    public final static String TAG = "ProximityFrag";

    protected final static String SAVE_LOCATION_LONGITUDE = "save location longitude",
            SAVE_LOCATION_LATITUDE = "save location latitude",
            SAVE_LIST_RESTAURANTS = "save list restaurants",
            SAVE_RAYON = "save seek bar";

    protected ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with Views.
     */
    protected RestaurantArrayAdapter mAdapter;

    protected LocalisationManager localisationManager;

    /**
     * Dernière Location connue
     */
    protected Location location;

    /**
     * Liste des informations des restaurants
     */
    protected ArrayList<InfoRestaurant> restaurantsList;

    protected SeekBar seekBar;
    protected ProgressBar progressBar;
    protected LinearLayout progressBarLayout;
    protected TextView rayonSeekBarTitle;
    protected ImageButton buttonRayon;

    protected int rayonSelected;

    /**
     * Temps en minute avant de regéolocaliser
     */
    private long DELTA_TIME = 2;


    public static ProximityFragment newInstance() {
        ProximityFragment fragment = new ProximityFragment();
        return fragment;
    }

    public ProximityFragment() {}

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localisationManager = new LocalisationManager(getActivity());
        restaurantsList = new ArrayList<InfoRestaurant>();
        rayonSelected = 1000;

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SAVE_LOCATION_LONGITUDE) &&
                    savedInstanceState.containsKey(SAVE_LOCATION_LATITUDE)){
                location = new Location("");
                location.setLongitude(savedInstanceState.getDouble(SAVE_LOCATION_LONGITUDE));
                location.setLatitude(savedInstanceState.getDouble(SAVE_LOCATION_LATITUDE));
            }

            if(savedInstanceState.containsKey(SAVE_LIST_RESTAURANTS))
                restaurantsList = (ArrayList<InfoRestaurant>) savedInstanceState.getSerializable(SAVE_LIST_RESTAURANTS);

            if(savedInstanceState.containsKey(SAVE_RAYON))
                rayonSelected = savedInstanceState.getInt(SAVE_RAYON);
        }else{
            restaurantsList = new ArrayList<InfoRestaurant>();
            rayonSelected = 1000;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proximityresult, container, false);

        // List view
        mListView = (ListView) view.findViewById(R.id.proximity_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectRestaurant((InfoRestaurant) parent.getItemAtPosition(position));
            }
        });

        //Seek bar
        seekBar = (SeekBar) view.findViewById(R.id.proximity_rayon_bar);
        seekBar.setMax(4900);
        seekBar.setProgress(rayonSelected);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rayonSelected = progress + 100;
                updateDistanceLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        rayonSeekBarTitle = (TextView) view.findViewById(R.id.proximity_rayon_titre);

        // Seek bar button
        buttonRayon = (ImageButton) view.findViewById(R.id.proximity_rayon_bouton);
        buttonRayon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResultsStart();
            }
        });

        //Progress bar
        progressBar = (ProgressBar) view.findViewById(R.id.proximity_progress_bar);
        progressBarLayout = (LinearLayout) view.findViewById(R.id.proximity_progress_bar_layout);

        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        updateDistanceLabel();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Adapter
        mAdapter = new RestaurantArrayAdapter(getActivity(), restaurantsList);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Save instance state");
        if(location != null)
            outState.putDouble(SAVE_LOCATION_LATITUDE, location.getLatitude());

        if(location != null)
            outState.putDouble(SAVE_LOCATION_LONGITUDE, location.getLongitude());

        if(restaurantsList != null)
            outState.putSerializable(SAVE_LIST_RESTAURANTS, restaurantsList);

        outState.putInt(SAVE_RAYON, rayonSelected);
    }


    /**
     * Lance la mise à jour des resultats des restaurants proches
     */
    protected void updateResultsStart(){
        if(location != null &&
                SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() < TimeUnit.MINUTES.toNanos(DELTA_TIME)){
            onLocationChanged(location);
        }else {
            localisationManager = new LocalisationManager(getActivity());
            if(localisationManager.singleUpdate(this))
                Toast.makeText(getActivity(), "Géolocalisation en cours ...", Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(getActivity(), "Impossible d'accéder au service de géolocalisation", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressBarLayout.setVisibility(View.VISIBLE);
        buttonRayon.setEnabled(false);
    }

    /**
     * Une fois que la récupération des informations des restaurants proches est fini
     * @param list
     */
    protected void updateResultsEnd(List<InfoRestaurant> list){
        restaurantsList.clear();
        restaurantsList.addAll(list);
        progressBarLayout.setVisibility(View.GONE);
        buttonRayon.setEnabled(true);
        Log.d(TAG, "List: " + restaurantsList.toString());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Click sur un restaurant de la liste
     * @param target
     */
    public void selectRestaurant(InfoRestaurant target){
        Log.d(TAG, "resto targeted: " + target.nom + " | " + target.adresse);

        ((Main) getActivity()).setCurrentSelectedRestaurant(target);
        ((Main) getActivity()).changeFragment(getString(R.string.section_detail_resto));
    }

    /**
     * Met à jour le titre du Spinner avec la bonne valeur de distance
     */
    protected void updateDistanceLabel(){
        rayonSeekBarTitle.setText(getString(R.string.proximity_rayon_titre) + " " + Util.distanceToString(rayonSelected));
    }


    /*
            LOCATION LISTENER IMPLEMENTATION
     */

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location changed");
        localisationManager.removeUpdates(this);
        this.location = location;

        String link = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        link += "location="+location.getLatitude()+","+location.getLongitude();
        link += "&radius="+rayonSelected;
        link += "&language=fr";
        link += "&type=cafe|restaurant";
        link += "&orderby=distance";
        link += "&key="+getString(R.string.google_place_key_server);

        Log.d(TAG, "Google Places API request: \n"+link);

        new LoaderURL().execute(link);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}



     /*
            ADAPTER POUR LA LISTVIEW
     */

    private static class ViewHolder{
        TextView textTitle;
        TextView textAdresse;
        TextView textDistance;
        TextView textNouveau;
    }

    class RestaurantArrayAdapter extends ArrayAdapter<InfoRestaurant>{

        public RestaurantArrayAdapter(Context context, List<InfoRestaurant> items) {
            super(context, R.layout.fragment_proximity_row, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InfoRestaurant target = getItem(position);
            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater(null).inflate(R.layout.fragment_proximity_row, parent, false);
                viewHolder.textTitle = (TextView) convertView.findViewById(R.id.proximity_row_title);
                viewHolder.textAdresse = (TextView) convertView.findViewById(R.id.proximity_row_addresse);
                viewHolder.textDistance = (TextView) convertView.findViewById(R.id.proximity_row_distance);
                viewHolder.textNouveau = (TextView) convertView.findViewById(R.id.proximity_row_nouveau);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textTitle.setText(target.nom);
            viewHolder.textAdresse.setText(target.adresse);
            viewHolder.textDistance.setText(Util.distanceToString(target.distance));

            if(!target.notAdded)
                viewHolder.textNouveau.setVisibility(View.INVISIBLE);
            else
                viewHolder.textNouveau.setVisibility(View.VISIBLE);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            setNotifyOnChange(false);
            sort(new Comparator<InfoRestaurant>() {
                @Override
                public int compare(InfoRestaurant lhs, InfoRestaurant rhs) {
                    if(lhs.distance < rhs.distance)
                        return -1;
                    else if(lhs.distance > rhs.distance)
                        return 1;
                    return 0;
                }
            });
            setNotifyOnChange(true);
            super.notifyDataSetChanged();
        }
    }


    /*
            ASYNC TASK LOAD URL
     */
    class LoaderURL extends AsyncTask<String, Void, List> {

        private static final String TAG = "LoaderURL";

        private static final String TAG_RESULTS = "results";
        private static final String TAG_LAT = "lat";
        private static final String TAG_LNG = "lng";
        private static final String TAG_NAME = "name";
        private static final String TAG_ADDR = "vicinity";
        private static final String TAG_GEOMETRY = "geometry";
        private static final String TAG_LOCATION = "location";
        private static final String TAG_PLACE_ID = "place_id";


        @Override
        protected List doInBackground(String... params) {
            String s = params[0];
            URL url = null;
            StringBuilder response = new StringBuilder();

            try{
                url = new URL(s);
            }catch (MalformedURLException e){
                Log.e(TAG, "do in background", e);
                cancel(true);
            }

            // Recupere réponse API place nearby
            InputStream is = null;
            HttpURLConnection http = null;
            try{
                http = (HttpURLConnection) url.openConnection();
                int code = http.getResponseCode();
                if(code == HttpURLConnection.HTTP_OK)
                    is = http.getInputStream();
                else{
                    Log.d(TAG, "do in background: code HTTP no ok");
                    cancel(true);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String ligne;
                while((ligne = br.readLine()) != null)
                    response.append(ligne+"\n");
                br.close();

            }catch (IOException e){
                Log.e(TAG, "do in background", e);
                cancel(true);
            }

            String jsonString = response.toString();
            if(jsonString == null){
                Log.d(TAG, "do in background: json empty");
                return null;
            }

            // Recupere les infos du JSON sur le restaurant de place nearby
            List<InfoRestaurant> newRestos = new ArrayList<InfoRestaurant>();
            Location targetLocation = new Location("");
            int order = 0;
            try {
                JSONObject jsonObj = new JSONObject(jsonString);
                JSONArray results = jsonObj.getJSONArray(TAG_RESULTS);

                for(int i = 0; i < results.length(); i++){
                    JSONObject result = results.getJSONObject(i);
                    InfoRestaurant resto = new InfoRestaurant();

                    resto.nom = result.getString(TAG_NAME);
                    resto.adresse = result.getString(TAG_ADDR);
                    resto.place_id = result.getString(TAG_PLACE_ID);

                    JSONObject locationJSON = result.getJSONObject(TAG_GEOMETRY).getJSONObject(TAG_LOCATION);
                    resto.latitude = locationJSON.getDouble(TAG_LAT);
                    resto.longitude = locationJSON.getDouble(TAG_LNG);

                    resto.order = order;
                    order++;

                    targetLocation.setLatitude(resto.latitude);
                    targetLocation.setLongitude(resto.longitude);
                    resto.distance = (int) targetLocation.distanceTo(location);

                    newRestos.add(resto);
                }
            }catch (JSONException e) {
                Log.e(TAG, "Error Json", e);
            }


            for(InfoRestaurant resto : newRestos){
                Cursor cursor = getActivity().getContentResolver().query(
                        RestaurantProvider.Restaurant.CONTENT_URI,
                        new String[]{RestaurantProvider.Restaurant.PLACE_ID},
                        RestaurantProvider.Restaurant.PLACE_ID+" = ?",
                        new String[]{resto.place_id},
                        null);

                if(cursor.getCount() == 1)
                    resto.notAdded = false;
                else
                    resto.notAdded = true;
                cursor.close();
            }

            Log.d(TAG, "Task done: " + newRestos.size() + " resto found");

            return newRestos;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            updateResultsEnd(list);
        }
    }
}
