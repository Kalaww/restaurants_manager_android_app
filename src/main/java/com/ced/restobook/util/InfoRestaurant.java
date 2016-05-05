package com.ced.restobook.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;

import com.ced.restobook.provider.RestaurantProvider;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Regroupe les informations sur un restaurant
 */
public class InfoRestaurant implements Serializable {

    /**
     * Ordre du restaurant dans une liste
     */
    public int order;

    public int id;

    public String nom = "";

    public String place_id = "";

    public String adresse = "";

    public String telephone = "";

    public String siteWeb = "";

    public float note = 0;

    public double latitude = 0.0;

    public double longitude = 0.0;

    public Date dateAjout = new Date();

    public String commentaire = "";

    public String photo = "";

    /**
     * Si le restaurant n'est pas ajouté à la base de donnée
     */
    public boolean notAdded = false;

    /**
     * Si le restaurant est ajouté au contact de l'appareil
     */
    public boolean addedContact = false;

    /**
     * Distance du restaurant par rapport aux coordonnées utilisées par un fragment
     */
    public int distance;


    @Override
    public String toString(){
        String s = "ID: "+id;
        s += ", order: "+order;
        s += ", nom: "+nom;
        s += ", place_id: "+place_id;
        s += ", adresse: "+adresse;
        s += ", telephone: "+telephone;
        s += ", site web: "+siteWeb;
        s += ", note: "+note;
        s += ", lat: "+latitude;
        s += ", long: "+longitude;
        s += ", date ajout: "+dateAjout.toString();
        s += ", commentaire: "+commentaire;
        s += ", photo: "+photo;
        return s+"\n";
    }

    /**
     * Converti la date en version lisible
     * @return
     */
    public String toStringDate(){
        String s = "";
        s += DateFormat.format("dd", dateAjout)+"/";
        s += DateFormat.format("MM", dateAjout)+"/";
        s += DateFormat.format("yyyy", dateAjout);
        return s;
    }

    /**
     * Rempli un ContentValues des informations du restaurant
     * @return
     */
    private ContentValues getContentValues(){
        ContentValues values = new ContentValues();

        values.put(RestaurantProvider.Restaurant.NOM, nom);
        values.put(RestaurantProvider.Restaurant.ADRESSE, adresse);
        values.put(RestaurantProvider.Restaurant.LATITUDE, latitude);
        values.put(RestaurantProvider.Restaurant.LONGITUDE, longitude);
        values.put(RestaurantProvider.Restaurant.COMMENTAIRE, commentaire);
        values.put(RestaurantProvider.Restaurant.TELEPHONE, telephone);
        values.put(RestaurantProvider.Restaurant.SITE_WEB, siteWeb);
        values.put(RestaurantProvider.Restaurant.NOTE, note);
        values.put(RestaurantProvider.Restaurant.PLACE_ID, place_id);
        values.put(RestaurantProvider.Restaurant.DATE_AJOUT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateAjout));
        values.put(RestaurantProvider.Restaurant.PHOTO, photo);

        return values;
    }

    /**
     * Ajoute le restaurant à la base de donnée
     * @param resolver
     * @return
     */
    public Uri addRestaurantToProvider(ContentResolver resolver){
        return resolver.insert(
                RestaurantProvider.Restaurant.CONTENT_URI,
                getContentValues());
    }

    /**
     * Met à jour le restaurant de la base de donnée
     * @param resolver
     * @return
     */
    public int updateRestaurantToProvider(ContentResolver resolver){
        return resolver.update(
                RestaurantProvider.Restaurant.CONTENT_URI,
                getContentValues(),
                RestaurantProvider.Restaurant.ID + " = ?",
                new String[]{id + ""});
    }

    /**
     * Supprime le restaurant de la base de donnée
     * @param resolver
     * @return
     */
    public int deleteRestaurantToProvider(ContentResolver resolver){
        return resolver.delete(
                RestaurantProvider.Restaurant.CONTENT_URI,
                RestaurantProvider.Restaurant.ID + " = ?",
                new String[]{id + ""});
    }

    /**
     * Récupère toutes les informations disponibles sur le restaurant contenu dans un Cursor
     * @param cursor
     */
    public void fromCursorRow(Cursor cursor){
        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.ID) != -1)
            id = cursor.getInt(cursor.getColumnIndex(RestaurantProvider.Restaurant.ID));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.NOM) != -1)
            nom = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.NOM));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.PLACE_ID) != -1)
            place_id = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.PLACE_ID));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.ADRESSE) != -1)
            adresse = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.ADRESSE));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.NOTE) != -1)
            note = cursor.getFloat(cursor.getColumnIndex(RestaurantProvider.Restaurant.NOTE));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.SITE_WEB) != -1)
            siteWeb = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.SITE_WEB));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.TELEPHONE) != -1)
            telephone = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.TELEPHONE));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.COMMENTAIRE) != -1)
            commentaire = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.COMMENTAIRE));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.LONGITUDE) != -1)
            longitude = cursor.getDouble(cursor.getColumnIndex(RestaurantProvider.Restaurant.LONGITUDE));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.LATITUDE) != -1)
            latitude = cursor.getDouble(cursor.getColumnIndex(RestaurantProvider.Restaurant.LATITUDE));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.PHOTO) != -1)
            photo = cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.PHOTO));

        if(cursor.getColumnIndex(RestaurantProvider.Restaurant.DATE_AJOUT) != -1) {
            try {
                dateAjout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(cursor.getColumnIndex(RestaurantProvider.Restaurant.DATE_AJOUT)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        notAdded = false;
    }
}
