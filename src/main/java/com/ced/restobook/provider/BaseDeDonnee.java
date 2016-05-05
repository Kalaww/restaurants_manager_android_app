package com.ced.restobook.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper de la base de donnee des restaurants
 */
public class BaseDeDonnee extends SQLiteOpenHelper{

    private static final String TAG = "BaseDeDonnee";

    private static final int VERSION = 2;

    private static final String DB_NAME = "restaurant_db";


    public BaseDeDonnee(Context context){
        super(context, DB_NAME, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RestaurantProvider.Restaurant.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion){
            Log.d(TAG, "Upgrade v" + oldVersion + " to v" + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + RestaurantProvider.Restaurant.TABLE_NAME);
            onCreate(db);
        }
    }
}
