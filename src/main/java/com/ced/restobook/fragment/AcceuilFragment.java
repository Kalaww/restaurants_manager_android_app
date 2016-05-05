package com.ced.restobook.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ced.restobook.R;
import com.ced.restobook.provider.RestaurantProvider;


public class AcceuilFragment extends Fragment {

    public final static String TAG = "AcceuilFrag";

    private TextView textNbRestaurants;

    public static AcceuilFragment newInstance() {
        AcceuilFragment fragment = new AcceuilFragment();
        return fragment;
    }

    public AcceuilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acceuil, container, false);

        textNbRestaurants = (TextView) view.findViewById(R.id.accueil_nb_restaurants);

        new LoaderQuery().execute();
        return view;
    }

    private void updateCountRestaurant(int count){
        textNbRestaurants.setText(getString(R.string.acceuil_text_restaurant)+" "+count);
    }

    /*
            ASYNCTASK : recupère le nombre de restaurant dans la base de donnée
     */
    class LoaderQuery extends AsyncTask<String[], Integer, Integer> {

        private static final String TAG = "LoaderQuery";

        @Override
        protected Integer doInBackground(String[]... params) {
            Cursor cursor = getActivity().getContentResolver().query(
                    RestaurantProvider.Restaurant.CONTENT_URI,
                    new String[]{RestaurantProvider.Restaurant.ID},
                    null,
                    null,
                    null);

            Log.d(TAG, "Count : " + cursor.getCount());
            int res = cursor.getCount();
            cursor.close();
            return res;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            updateCountRestaurant(i);
        }
    }

}
