package com.ced.restobook.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Content Provider pour la base de donnee des restaurants
 */
public class RestaurantProvider extends ContentProvider{

    private BaseDeDonnee helper;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int RESTO_ALL = 10;
    private static final int RESTO_BY_ID = 11;


    private static final String authority = "com.ced.restobook.provider";
    static{
        matcher.addURI(authority, Restaurant.TABLE_NAME, RESTO_ALL);
        matcher.addURI(authority, Restaurant.TABLE_NAME+"/#", RESTO_BY_ID);
    }

    public static class Restaurant{

        public static final String TABLE_NAME = "restaurants";

        public static final Uri CONTENT_URI = Uri.parse("content://"+authority+"/"+TABLE_NAME);

        private static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd."+authority+"."+TABLE_NAME;
        private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."+authority+"."+TABLE_NAME;

        public static final String NOM = "nom_resto",
                ADRESSE = "adresse",
                TELEPHONE = "telephone",
                SITE_WEB = "site_web",
                NOTE = "note",
                LATITUDE = "latitude",
                LONGITUDE = "longitude",
                ID = "_ID",
                PLACE_ID = "place_id",
                COMMENTAIRE = "commentaire",
                PHOTO = "photo",
                DATE_AJOUT = "date_ajout";


        public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+"("+
                ID+" integer primary key, "+
                NOM+" text, "+
                PLACE_ID+" text, "+
                ADRESSE+" text, "+
                TELEPHONE+" text, "+
                SITE_WEB+" text, "+
                NOTE+" integer, "+
                LATITUDE+" numeric, "+
                LONGITUDE+" numeric, "+
                DATE_AJOUT+" date, "+
                PHOTO+" text, "+
                COMMENTAIRE+" text)";
    }

    @Override
    public boolean onCreate() {
        helper = new BaseDeDonnee(getContext());
        Log.d("RestaurantProvider", "onCreate done");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;

        switch (matcher.match(uri)){
            case RESTO_ALL:
                cursor = db.query(Restaurant.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;

            case RESTO_BY_ID:
                cursor = db.query(Restaurant.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;

            default:
                return null;
        }

        Log.d("RestaurantProvider", "Query: cursor_count = "+cursor.getCount());
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int id = (int) db.insert(Restaurant.TABLE_NAME, null, values);
        return ContentUris.withAppendedId(Restaurant.CONTENT_URI, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(Restaurant.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(Restaurant.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        switch (matcher.match(uri)) {
            case RESTO_ALL:
                return Restaurant.CONTENT_DIR_TYPE;

            case RESTO_BY_ID:
                return Restaurant.CONTENT_ITEM_TYPE;

            default:
                return null;
        }
    }
}
