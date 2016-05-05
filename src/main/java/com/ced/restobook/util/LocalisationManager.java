package com.ced.restobook.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocalisationManager {

    private final String TAG = "LocalisationManager";

    private Activity activity;

    private LocationManager lm;

    public LocalisationManager(Activity activity) {
        this.activity = activity;

        lm = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
    }

    /**
     * Récupère la dernière position connue
     * @return
     */
    public Location getLastKnownLocation() {
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;

        if (activity.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission fine location non granted");
            return null;
        }

        for (String provider : providers) {
            Log.d("lastKnownLocation", "Test provider: " + provider);
            Location l = lm.getLastKnownLocation(provider);
            if (l == null)
                continue;

            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                Log.d("lastKnownLocation", "Found better location");
                bestLocation = l;
            }
        }

        return bestLocation;
    }

    /**
     * Demande une mesure des coordonnées géographiques
     * @param listener
     */
    public boolean singleUpdate(LocationListener listener) {
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);

        String provider = lm.getBestProvider(crit, true);
        if (provider == null || !provider.equals("gps")) {
            Log.d(TAG, "Aucun provider trouvé");
            return false;
        }

        Log.d(TAG, "Provider trouvé: " + provider);

        if(!checkPermission())
            return false;

        lm.requestSingleUpdate(provider, listener, null);
        return true;
    }

    /**
     * Récupère des données géographiques à partir de coordonnées géographiques
     * @param lat
     * @param lon
     * @param nb
     * @return
     */
    public List<Address> reverseGeocode(double lat, double lon, int nb) {
        List<Address> addresses = null;
        Geocoder gc = new Geocoder(activity, Locale.FRANCE);

        try {
            addresses = gc.getFromLocation(lat, lon, nb);
        } catch (IOException e) {
            Log.e(TAG, "Reverse Geocode", e);
        }

        return addresses;
    }

    /**
     * Récupère des données géographiques à partir d'un nom de lieu
     * @param lieu
     * @param nb
     * @return
     */
    public List<Address> forwardGeocode(String lieu, int nb) {
        List<Address> addresses = null;
        Geocoder gc = new Geocoder(activity, Locale.FRANCE);

        try {
            addresses = gc.getFromLocationName(lieu, nb);
        } catch (IOException e) {
            Log.e(TAG, "Forward Geocode", e);
        }

        return addresses;
    }

    /**
     * Stop la mesure de la position géographoque
     * @param listener
     */
    public void removeUpdates(LocationListener listener){
        if(!checkPermission())
            return;
        lm.removeUpdates(listener);
    }

    /**
     * Vérifie si l'application possède le droit ACCES FINE LOCATION
     * @return
     */
    private boolean checkPermission(){
        if (activity.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission FINE LOCATION non granted");
            return false;
        }
        return true;
    }

}
