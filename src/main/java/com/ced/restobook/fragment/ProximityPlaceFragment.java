package com.ced.restobook.fragment;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ced.restobook.R;
import com.ced.restobook.util.InfoRestaurant;

import java.util.List;


public class ProximityPlaceFragment extends ProximityFragment {

    public final static String TAG = "ProximityPlaceFrag";


    public static ProximityPlaceFragment newInstance() {
        ProximityPlaceFragment fragment = new ProximityPlaceFragment();
        return fragment;
    }

    public ProximityPlaceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proximity_place, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.proximity_place_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectRestaurant((InfoRestaurant) parent.getItemAtPosition(position));
            }
        });

        //Seek bar
        seekBar = (SeekBar) view.findViewById(R.id.proximity_place_rayon_bar);
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
        rayonSeekBarTitle = (TextView) view.findViewById(R.id.proximity_place_rayon_titre);

        // Seek bar button
        buttonRayon = (ImageButton) view.findViewById(R.id.proximity_place_rayon_bouton);
        buttonRayon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResultsStart();
            }
        });

        //Progress bar
        progressBar = (ProgressBar) view.findViewById(R.id.proximity_place_progress_bar);
        progressBarLayout = (LinearLayout) view.findViewById(R.id.proximity_place_progress_bar_layout);

        return view;
    }

    @Override
    protected void updateResultsStart(){
        Log.d(TAG, "Update results");

        String lieu = ((EditText) getView().findViewById(R.id.proximity_place_rayon_edit_adresse)).getText().toString();
        if(lieu == null || lieu.length() == 0){
            Toast.makeText(getContext(), "Aucun lieu de recherche spécifié", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Address> addresses = localisationManager.forwardGeocode(lieu, 1);
        if(addresses == null || addresses.size() == 0){
            Toast.makeText(getContext(), "Lieu introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarLayout.setVisibility(View.VISIBLE);
        buttonRayon.setEnabled(false);

        location = new Location("");
        location.setLatitude(addresses.get(0).getLatitude());
        location.setLongitude(addresses.get(0).getLongitude());

        String link = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        link += "location="+location.getLatitude()+","+location.getLongitude();
        link += "&radius="+rayonSelected;
        link += "&language=fr";
        link += "&type=restaurant|cafe";
        link += "&orderby=distance";
        link += "&key="+getString(R.string.google_place_key_server);

        Log.d(TAG, "Google Places API request: \n" + link);

        new LoaderURL().execute(link);
    }
}
