package com.ced.restobook.fragment;

import android.app.Activity;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.ced.restobook.Main;
import com.ced.restobook.R;
import com.ced.restobook.util.InfoRestaurant;
import com.ced.restobook.util.LocalisationManager;

import java.util.List;


public class EditRestaurantFragment extends Fragment {

    public static final String TAG = "EditRestoFrag";

    private static final String SAVE_IN_EDIT_MODE = "save in edit mode";

    /**
     * Infos du restaurant
     */
    private InfoRestaurant infos;

    private EditText editNom;
    private EditText editAdresse;
    private EditText editTelephone;
    private EditText editSiteweb;
    private EditText editCommentaire;

    private RatingBar ratingBar;

    /**
     * True: edite le restaurant
     * False: ajoute un nouveau restaurant
     */
    private boolean inEditMode;


    public static EditRestaurantFragment newInstance(boolean inEditMode) {
        EditRestaurantFragment fragment = new EditRestaurantFragment();
        fragment.inEditMode = inEditMode;
        return fragment;
    }

    public EditRestaurantFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_restaurant, container, false);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SAVE_IN_EDIT_MODE)){
                inEditMode = savedInstanceState.getBoolean(SAVE_IN_EDIT_MODE);
            }
        }

        Button submit = (Button) v.findViewById(R.id.edit_resto_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitChange();
            }
        });

        editNom = (EditText) v.findViewById(R.id.edit_resto_nom);
        editAdresse = (EditText) v.findViewById(R.id.edit_resto_adresse);
        editSiteweb = (EditText) v.findViewById(R.id.edit_resto_siteweb);
        editTelephone = (EditText) v.findViewById(R.id.edit_resto_telephone);
        editCommentaire = (EditText) v.findViewById(R.id.edit_resto_commentaire);

        ratingBar = (RatingBar) v.findViewById(R.id.edit_resto_note_bar);

        infos = (inEditMode)? ((Main)getActivity()).getCurrentSelectedRestaurant() : new InfoRestaurant();

        updateView();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_IN_EDIT_MODE, inEditMode);
        super.onSaveInstanceState(outState);
    }



    /**
     * Met à jour les Views avec les informations sur le restaurant
     */
    private void updateView(){
        View v = getView();

        editNom.setText(infos.nom);
        editAdresse.setText(infos.adresse);
        editTelephone.setText(infos.telephone);
        editSiteweb.setText(infos.siteWeb);
        editCommentaire.setText(infos.commentaire);

        ratingBar.setRating(infos.note);
    }

    /**
     * Lance l'update des informations du restaurant vers la base de donnée
     */
    private void submitChange(){
        infos.nom = editNom.getText().toString();
        infos.siteWeb = editSiteweb.getText().toString();
        infos.telephone = editTelephone.getText().toString();
        infos.commentaire = editCommentaire.getText().toString();
        infos.note = ratingBar.getRating();

        if(!inEditMode || !infos.adresse.equals(editAdresse.getText().toString())){
            LocalisationManager l = new LocalisationManager(getActivity());
            List<Address> adresses = l.forwardGeocode(editAdresse.getText().toString(), 1);
            if(adresses.size() > 0) {
                infos.longitude = adresses.get(0).getLongitude();
                infos.latitude = adresses.get(0).getLatitude();
            }else{
                infos.longitude = 0.0;
                infos.latitude = 0.0;
            }
            Log.d(TAG, "New adresse : long="+infos.longitude+", lat="+infos.latitude);
            infos.adresse = editAdresse.getText().toString();
        }

        if(inEditMode)
            new LoaderUpdate().execute();
        else
            new LoaderAdd().execute();
    }

    /**
     * Une fois que l'update des informations du restaurant vers la base de donnée sont terminées
     */
    private void submitChangeFinish(){
        if(inEditMode)
            Toast.makeText(getActivity(), "Restaurant mit à jour", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "Restaurant ajouté", Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }

    /**
     * Retour si ce fragment est en mode edition ou ajout
     * @return
     */
    public boolean isInEditMode(){
        return inEditMode;
    }


    /*
            ASYNCTASK : met a jour les infos du restaurant
     */
    class LoaderUpdate extends AsyncTask<String[], Integer, Void> {

        @Override
        protected Void doInBackground(String[]... params) {
            int res = infos.updateRestaurantToProvider(getActivity().getContentResolver());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            submitChangeFinish();
        }
    }

    /*
           ASYNCTASK : ajoute le restaurant
    */
    class LoaderAdd extends AsyncTask<String[], Integer, Void> {

        @Override
        protected Void doInBackground(String[]... params) {
            infos.addRestaurantToProvider(getActivity().getContentResolver());
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            submitChangeFinish();
        }
    }


}
