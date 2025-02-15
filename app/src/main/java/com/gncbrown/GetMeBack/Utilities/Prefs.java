package com.gncbrown.GetMeBack.Utilities;
import android.content.SharedPreferences;
import android.location.Location;

import com.gncbrown.GetMeBack.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Set;

public class Prefs {

    private static final String TAG = "Prefs";

    private static final String PREF_KEY_GPS_CACHE_LATITUDE = "gpsCacheLatitude";
    private static final String PREF_KEY_GPS_CACHE_LONGITUDE = "gpsCacheLongitude";
    private static final String PREF_KEY_FILTERED_DESTINATION_LATITUDE = "filteredDestinationLatitude";
    private static final String PREF_KEY_FILTERED_DESTINATION_LONGITUDE = "filteredDestinationLongitude";
    private static final String PREF_KEY_DESTINATION_LATITUDE = "destinationLatitude";
    private static final String PREF_KEY_DESTINATION_LONGITUDE = "destinationLongitude";
    private static final String PREF_KEY_DESTINATION_ALTITUDE = "destinationAltitude";
    private static final String PREF_KEY_DESTINATION_ADDRESS = "destinationAddress";
    private static final String PREF_KEY_HOME_ADDRESS = "homeAddress";
    private static final String PREF_KEY_HOME_LATITUDE = "homeLatitude";
    private static final String PREF_KEY_HOME_LONGITUDE = "homeLongitude";
    private static final String PREF_KEY_FIRST_TIME = "firstTime";
    private static final String PREF_KEY_NAMED_LOCATIONS = "namedLocations";


    public static String doubleToString(double value) {
        return String.format("%f", value);
    }

    public static double stringToDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }


    public static LatLng retrieveLocationOfflineFromPreference() {
        String latitude = doubleToString(0.0);
        String longitude = doubleToString(0.0);
        try {
            latitude = MainActivity.sharedPreferences.getString(PREF_KEY_GPS_CACHE_LATITUDE, latitude);
            longitude = MainActivity.sharedPreferences.getString(PREF_KEY_GPS_CACHE_LONGITUDE, longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(stringToDouble(latitude), stringToDouble(longitude));
    }

    public static void saveLocationOfflineToPreference(Location location) {
        //Log.d(TAG, "saveLocationOfflineToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putString(PREF_KEY_GPS_CACHE_LATITUDE, doubleToString(location.getLatitude())).apply();
        editor.putString(PREF_KEY_GPS_CACHE_LONGITUDE, doubleToString(location.getLongitude())).apply();
    }

    public static LatLng retrieveDestinationLocationFromPreference() {
        double latitude = MainActivity.home.latitude;
        double longitude = MainActivity.home.longitude;
        try {
            latitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_DESTINATION_LATITUDE, doubleToString(MainActivity.home.latitude)));
            longitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_DESTINATION_LONGITUDE, doubleToString(MainActivity.home.longitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void saveDestinationLocationToPreference(LatLng value) {
        //Log.d(TAG, "saveDestinationLocationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        if (value != null) {
            editor.putString(PREF_KEY_DESTINATION_LATITUDE, doubleToString(value.latitude)).apply();
            editor.putString(PREF_KEY_DESTINATION_LONGITUDE, doubleToString(value.longitude)).apply();
        } else {
            editor.putString(PREF_KEY_DESTINATION_LATITUDE, doubleToString(0.0)).apply();
            editor.putString(PREF_KEY_DESTINATION_LONGITUDE, doubleToString(0.0)).apply();
        }
    }

    public static LatLng retrieveFilteredDestinationLocationFromPreference() {
        double latitude = MainActivity.home.latitude;
        double longitude = MainActivity.home.longitude;
        try {
            latitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_FILTERED_DESTINATION_LATITUDE, doubleToString(latitude)));
            longitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_FILTERED_DESTINATION_LONGITUDE, doubleToString(longitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void saveFilteredDestinationLocationToPreference(LatLng value) {
        //Log.d(TAG, "saveFilteredDestinationLocationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        if (value != null) {
            editor.putString(PREF_KEY_FILTERED_DESTINATION_LATITUDE, doubleToString(value.latitude)).apply();
            editor.putString(PREF_KEY_FILTERED_DESTINATION_LONGITUDE, doubleToString(value.longitude)).apply();
        } else {
            editor.putString(PREF_KEY_FILTERED_DESTINATION_LATITUDE, doubleToString(0.0)).apply();
            editor.putString(PREF_KEY_FILTERED_DESTINATION_LONGITUDE, doubleToString(0.0)).apply();
        }
    }

    public static double retrieveDestinationAltitudeFromPreference() {
        double altitude = 0.0;
        try {
            altitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_DESTINATION_ALTITUDE, doubleToString(altitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return altitude;
    }

    public static void saveDestinationAltitudeToPreference(double value) {
        //Log.d(TAG, "saveDestinationAltitudeToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putString(PREF_KEY_DESTINATION_ALTITUDE, doubleToString(value)).apply();
    }

    public static String retrieveDestinationAddressFromPreference() {
        String destination = "???";
        try {
            destination = MainActivity.sharedPreferences.getString(PREF_KEY_DESTINATION_ADDRESS, "???");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destination;
    }

    public static void saveDestinationAddressToPreference(String value) {
        //Log.d(TAG, "saveDestinationAddressToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putString(PREF_KEY_DESTINATION_ADDRESS, value).apply();
    }

    public static LatLng retrieveHomeLocationFromPreference() {
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            latitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_HOME_LATITUDE, doubleToString(MainActivity.home.latitude)));
            longitude = stringToDouble(MainActivity.sharedPreferences.getString(PREF_KEY_HOME_LONGITUDE, doubleToString(MainActivity.home.longitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void saveHomeLocationToPreference(LatLng value) {
        //Log.d(TAG, "saveDestinationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        if (value != null) {
            editor.putString(PREF_KEY_HOME_LATITUDE, doubleToString(value.latitude)).apply();
            editor.putString(PREF_KEY_HOME_LONGITUDE, doubleToString(value.longitude)).apply();
        } else {
            editor.putString(PREF_KEY_HOME_LATITUDE, doubleToString(0.0)).apply();
            editor.putString(PREF_KEY_HOME_LONGITUDE, doubleToString(0.0)).apply();
        }
    }

    public static String[] retrieveNamedLocations() {
        Set<String> locations = MainActivity.sharedPreferences.getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
        String[] arrayOfLocations = locations.toArray(new String[0]);
        return arrayOfLocations;
    }

    public static void saveToNamedLocations(String name) {
        Set<String> locations = MainActivity.sharedPreferences.getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
        locations.remove(name);
        locations.add(name);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putStringSet(PREF_KEY_NAMED_LOCATIONS, locations).apply();
    }

    public static LatLng retrieveNamedLocation(String name) {
        String locationFor = MainActivity.sharedPreferences.getString(name, "43.05687,-75.25245");
        String[] latLngString = locationFor.split(",");
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            latitude = stringToDouble(latLngString[0]);
            longitude = stringToDouble(latLngString[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void removeNamedLocationFromPreference(String name) {
        Set<String> locations = MainActivity.sharedPreferences.getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
        locations.remove(name);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.remove(name);
        editor.putStringSet(PREF_KEY_NAMED_LOCATIONS, locations).apply();
        editor.commit();
    }

    public static void saveNamedLocationToPreference(String name, LatLng value) {
        //Log.d(TAG, "saveNamedLocationToPreference, name=" + name + ", value=" + value);
        saveToNamedLocations(name);

        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putString(name, String.format("%s,%s", value.latitude, value.longitude)).apply();
    }

    public static void saveHomeAddressToPreference(String value) {
        //Log.d(TAG, "saveHomeAddressToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putString(PREF_KEY_HOME_ADDRESS, value).apply();
    }

    public static String retrieveHomeAddressFromPreference() {
        String home = "???";
        try {
            home = MainActivity.sharedPreferences.getString(PREF_KEY_HOME_ADDRESS, "???");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return home;
    }

    public static boolean retrieveFirstTimeFromPreference() {
        boolean firstTime = true;
        try {
            firstTime = MainActivity.sharedPreferences.getBoolean(PREF_KEY_FIRST_TIME, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return firstTime;
    }

    public static void saveFirstTimeToPreference(boolean value) {
        //Log.d(TAG, "saveFirstTimeToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putBoolean(PREF_KEY_FIRST_TIME, value).apply();
    }
}
