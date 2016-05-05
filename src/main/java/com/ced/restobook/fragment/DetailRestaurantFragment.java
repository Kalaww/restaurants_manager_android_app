package com.ced.restobook.fragment;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ced.restobook.Main;
import com.ced.restobook.R;
import com.ced.restobook.util.InfoRestaurant;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DetailRestaurantFragment extends Fragment {

    public static final String TAG = "DetailRestaurant";

    private static final int REQUEST_TAKE_PHOTO = 55;

    private static final String SAVE_OLD_PHOTO = "save old photo";

    /**
     * Informations du restaurants
     */
    private InfoRestaurant infos;


    private TextView textNom;
    private TextView textAdresse;
    private TextView textSiteweb;
    private TextView textTelephone;
    private TextView textCommentaire;
    private TextView textDateAjout;

    private ImageView imagePhoto;

    private RatingBar ratingBar;

    private ImageButton buttonEdit;
    private ImageButton buttonDelete;
    private ImageButton buttonAdd;
    private ImageButton buttonAddContact;
    private ImageButton buttonCall;
    private Button buttonMaps;
    private Button buttonStreetView;
    private Button buttonNewPhoto;
    private Button buttonReplacePhoto;

    private boolean cameraEnable = false;
    private String oldPhoto = "";

    private int imageViewWidth;
    private int imageViewHeight;

    public static DetailRestaurantFragment newInstance() {
        DetailRestaurantFragment fragment = new DetailRestaurantFragment();
        return fragment;
    }

    public DetailRestaurantFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail_restaurant, container, false);

        buttonEdit = (ImageButton) v.findViewById(R.id.detail_resto_edit_button);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Main)getActivity()).changeFragment(getString(R.string.section_edit_resto));
            }
        });

        buttonDelete = (ImageButton)v.findViewById(R.id.detail_resto_delete_button);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteStart();
            }
        });

        buttonAdd = (ImageButton) v.findViewById(R.id.detail_resto_add_button);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStart();
            }
        });

        buttonCall = (ImageButton) v.findViewById(R.id.detail_resto_call_button);
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPhoneCall();
            }
        });

        buttonAddContact = (ImageButton) v.findViewById(R.id.detail_resto_add_contact_button);
        buttonAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        buttonMaps = (Button) v.findViewById(R.id.detail_resto_maps_button);
        buttonMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMaps();
            }
        });

        buttonStreetView = (Button) v.findViewById(R.id.detail_resto_street_view_button);
        buttonStreetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchStreetView();
            }
        });

        buttonNewPhoto = (Button) v.findViewById(R.id.detail_resto_new_photo_button);
        buttonNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        buttonReplacePhoto = (Button) v.findViewById(R.id.detail_resto_replace_photo_button);
        buttonReplacePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        ratingBar = (RatingBar) v.findViewById(R.id.detail_resto_note_bar);
        ratingBar.setIsIndicator(true);

        textNom = (TextView) v.findViewById(R.id.detail_resto_nom);
        textAdresse = (TextView) v.findViewById(R.id.detail_resto_adresse);
        textCommentaire = (TextView) v.findViewById(R.id.detail_resto_commentaire);
        textDateAjout = (TextView) v.findViewById(R.id.detail_resto_date_ajout);
        textTelephone = (TextView) v.findViewById(R.id.detail_resto_telephone);
        textSiteweb = (TextView) v.findViewById(R.id.detail_resto_site_web);
        textSiteweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchWebBrowser();
            }
        });

        imagePhoto = (ImageView) v.findViewById(R.id.detail_resto_photo);
        imagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGalleryPhoto();
            }
        });
        cameraEnable = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        infos = ((Main) getActivity()).getCurrentSelectedRestaurant();
        Log.d(TAG, "Current Restaurant :"+infos);
        getView().post(new Runnable() {
            @Override
            public void run() {
                imageViewWidth = imagePhoto.getWidth();
                imageViewHeight = imagePhoto.getHeight();
                updatePhoto();
            }
        });

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SAVE_OLD_PHOTO))
                oldPhoto = savedInstanceState.getString(SAVE_OLD_PHOTO);
        }

        updateView();
        updateViewsVisibility();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_OLD_PHOTO, oldPhoto);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TAKE_PHOTO){
            if(resultCode == Activity.RESULT_OK)
                saveNewPhoto();
            else{
                infos.photo = oldPhoto;
                Toast.makeText(getActivity(), "Echec de la récupération de la photo", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Met à jour les View des informations sur le restaurants
     */
    private void updateView(){
        textNom.setText(infos.nom);
        textAdresse.setText(infos.adresse);
        textCommentaire.setText(infos.commentaire);
        textTelephone.setText(infos.telephone);

        ratingBar.setRating(infos.note);
        if (!infos.notAdded)
            textDateAjout.setText(getString(R.string.detail_resto_date_ajout)+" "+infos.toStringDate());

        if(infos.telephone != null && infos.telephone.length() > 0)
            infos.addedContact = checkContactAlreadyAdded();
        else
            infos.addedContact = false;

        String s = infos.siteWeb;
        SpannableString underlined = new SpannableString(s);
        underlined.setSpan(new UnderlineSpan(), 0, s.length(), 0);
        textSiteweb.setText(underlined);

        updatePhoto();
    }

    /**
     * Affiche la photo dans l'ImageView prévu.
     * Besoin de redimenssionner la photo sans perte du ratio
     */
    private void updatePhoto(){
        if(infos.photo == null || infos.photo.length() == 0){
            imagePhoto.setVisibility(View.GONE);
            return;
        }

        imagePhoto.setVisibility(View.VISIBLE);

        // Redimensionne l'image
        int targetW = imageViewWidth;
        int targetH = imageViewHeight;
        if(targetW <= 0 || targetH <= 0)
            return;

        Bitmap bitmap;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(infos.photo));
        }catch (IOException e){
            Toast.makeText(getActivity(), "Impossible d'acceder à la photo", Toast.LENGTH_SHORT).show();
            return;
        }

        int imgW = bitmap.getWidth();
        int imgH = bitmap.getHeight();
        int scale = Math.max(imgW / targetW, imgH / targetH);

        Bitmap rescaleBitmap = Bitmap.createScaledBitmap(bitmap, imgW/scale, imgH/scale, false);
        imagePhoto.setImageBitmap(rescaleBitmap);
    }

    /**
     * Met à jour les éléments qui doivent être affiché
     * Essentiellement les boutons
     */
    private void updateViewsVisibility(){
        if(!infos.notAdded){
            buttonAdd.setVisibility(View.GONE);

            buttonDelete.setVisibility(View.VISIBLE);
            buttonEdit.setVisibility(View.VISIBLE);
            textDateAjout.setVisibility(View.VISIBLE);
        }else{
            buttonDelete.setVisibility(View.GONE);
            buttonEdit.setVisibility(View.GONE);
            textDateAjout.setVisibility(View.GONE);

            buttonAdd.setVisibility(View.VISIBLE);
        }

        if(!infos.addedContact && infos.telephone != null && infos.telephone.length() > 0){
            buttonAddContact.setVisibility(View.VISIBLE);
        }else{
            buttonAddContact.setVisibility(View.GONE);
        }

        if(infos.telephone != null && infos.telephone.length() > 0){
            buttonCall.setVisibility(View.VISIBLE);
        }else{
            buttonCall.setVisibility(View.GONE);
        }

        if(infos.longitude != 0.0 && infos.latitude != 0.0) {
            buttonMaps.setVisibility(View.VISIBLE);
            buttonStreetView.setVisibility(View.VISIBLE);
        }else {
            buttonMaps.setVisibility(View.GONE);
            buttonStreetView.setVisibility(View.GONE);
        }

        if(cameraEnable && !infos.notAdded) {
            if(infos.photo == null || infos.photo.length() == 0){
                buttonNewPhoto.setVisibility(View.VISIBLE);
                buttonReplacePhoto.setVisibility(View.GONE);
                imagePhoto.setVisibility(View.GONE);
            }else{
                imagePhoto.setVisibility(View.VISIBLE);
                buttonNewPhoto.setVisibility(View.GONE);
                buttonReplacePhoto.setVisibility(View.VISIBLE);
            }
        }else{
            buttonNewPhoto.setVisibility(View.GONE);
            buttonReplacePhoto.setVisibility(View.GONE);
        }
    }




    /**
     * Lance l'ajout du restaurant à la base de donnée
     */
    private void addStart(){
        new LoaderAdd().execute();
    }

    /**
     * Une fois que le restaurant a été ajouté à la base de donnée
     */
    private void addFinish(){
        infos.notAdded = false;
        updateView();
        updateViewsVisibility();
    }

    /**
     * Lance la suppression du restaurant de la base de donnée
     */
    private void deleteStart(){
        new LoaderDelete().execute();
    }

    /**
     * Une fois que le restaurant a été supprimé de la base de donnée
     * @param success
     */
    private void deleteFinish(boolean success){
        if(success)
            Toast.makeText(getContext(), "Restaurant supprimé", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Impossible de supprimer", Toast.LENGTH_SHORT).show();

        if(success)
            getActivity().onBackPressed();
    }

    /**
     * Lance un appel téléphonique
     */
    private void launchPhoneCall(){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+infos.telephone));
        startActivity(intent);
    }

    /**
     * Lance Google Maps
     */
    private void launchMaps(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+infos.latitude+","+infos.longitude+"?q="+infos.latitude+","+infos.longitude+"("+infos.nom+")"));
        startActivity(intent);
    }

    /**
     * Lance Google Street View
     */
    private void launchStreetView(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.streetview:cbll="+infos.latitude+","+infos.longitude));
        startActivity(intent);
    }

    /**
     * Lance un Web Browser
     */
    private void launchWebBrowser(){
        String link = infos.siteWeb;
        if(!link.startsWith("https://") && !link.startsWith("http://"))
            link = "http://"+link;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }

    /**
     * Lance la Galerie Photo
     */
    private void launchGalleryPhoto(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(infos.photo), "image/*");
        startActivity(intent);
    }

    /**
     * Lance l'Appareil Photo
     */
    private void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getActivity().getPackageManager()) != null){
            // Creer un fichier pour la photo
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File dossier = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = null;

            try{
                imageFile = File.createTempFile("RestoBook_"+timestamp, ".jpg", dossier);
            }catch (IOException e){
                Log.d(TAG, "Erreur save new Photo create file", e);
                Toast.makeText(getActivity(), "Impossible d'ajouter la photo au dossier: "+dossier, Toast.LENGTH_SHORT).show();
            }

            // lance l'app camera
            if(imageFile != null){
                oldPhoto = infos.photo;
                infos.photo = "file:"+imageFile.getAbsolutePath();
                Log.d(TAG, "image url: "+infos.photo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        }else{
            Toast.makeText(getActivity(), "Impossible d'accéder à la camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ajoute la photo prise à la Galerie Photo et à la base de donnée
     */
    private void saveNewPhoto(){
        // Ajoute la photo au Photo Gallery
        Intent intentGallery = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentGallery.setData(Uri.parse(infos.photo));
        getActivity().sendBroadcast(intentGallery);

        new LoaderUpdate().execute();
    }

    /**
     * Ajoute le restaurant au contact de l'appareil
     */
    private void addContact(){
        if(addToContactProvider()){
            Toast.makeText(getActivity(), "Contact ajouté avec succès", Toast.LENGTH_SHORT).show();
            infos.addedContact = true;
        }else{
            Toast.makeText(getActivity(), "Impossible d'ajouter le contact", Toast.LENGTH_SHORT).show();
            infos.addedContact = false;
        }

        updateViewsVisibility();
    }

    /**
     * Vérifie si le numéro de téléphone du restaurant est déjà dans les contacts de l'appareil
     * @return
     */
    private boolean checkContactAlreadyAdded(){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(infos.telephone));
        Log.d(TAG, "Check contact already added URI: "+uri.toString());
        Cursor cursor = getActivity().getContentResolver().query(
                uri,
                new String[]{ContactsContract.PhoneLookup._ID},
                ContactsContract.PhoneLookup.NUMBER+" = ?",
                new String[]{infos.telephone},
                null);

        int count = cursor.getCount();
        cursor.close();

        return count >= 1;
    }

    /**
     * Fait appel au provider des contacts de l'appareil
     * @return
     */
    private boolean addToContactProvider(){
        ArrayList<ContentProviderOperation> op = new ArrayList<ContentProviderOperation>();

        op.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // NOM
        if(infos.nom != null && infos.nom.length() > 0)
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, infos.nom)
                .build());

        // SiteWeb
        if(infos.siteWeb != null && infos.siteWeb.length() > 0)
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Website.DATA, infos.siteWeb)
                .build());

        // TELEPHONE
        op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, infos.telephone)
                .build());

        // ADRESSE
        if(infos.adresse != null && infos.adresse.length() > 0)
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, infos.adresse)
                .build());

        // COMMENTAIRE
        if(infos.commentaire != null && infos.commentaire.length() > 0)
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, infos.commentaire)
                .build());

        // Add new contact
        try{
            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, op);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /*
           ASYNCTASK : ajoute le restaurant
    */
    class LoaderAdd extends AsyncTask<String[], Integer, Void> {

        private static final String TAG = "LoaderDelete";

        @Override
        protected Void doInBackground(String[]... params) {
            infos.addRestaurantToProvider(getActivity().getContentResolver());
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            addFinish();
        }
    }


    /*
           ASYNCTASK : suprime le restaurant
    */
    class LoaderDelete extends AsyncTask<String[], Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String[]... params) {
            return infos.deleteRestaurantToProvider(getActivity().getContentResolver()) == 1;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            deleteFinish(bool);
        }
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
    }

}
