package com.gncbrown.GetMeBack.Utilities;
import android.content.SharedPreferences;

import com.gncbrown.GetMeBack.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Set;

public class Prefs {

    private static final String TAG = "Prefs";

    private static final String PREF_KEY_DESTINATION_LATITUDE = "destinationLatitude";
    private static final String PREF_KEY_DESTINATION_LONGITUDE = "destinationLongitude";
    private static final String PREF_KEY_DESTINATION_ALTITUDE = "destinationAltitude";
    private static final String PREF_KEY_DESTINATION_ADDRESS = "destinationAddress";
    private static final String PREF_KEY_HOME_ADDRESS = "homeAddress";
    private static final String PREF_KEY_HOME_LATITUDE = "homeLatitude";
    private static final String PREF_KEY_HOME_LONGITUDE = "homeLongitude";
    private static final String PREF_KEY_FIRST_TIME = "firstTime";
    private static final String PREF_KEY_NAMED_LOCATIONS = "namedLocations";


    public static LatLng retrieveDestinationLocationFromPreference() {
        float latitude = 0.0f;
        float longitude = 0.0f;
        try {
            latitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_DESTINATION_LATITUDE, (float) MainActivity.home.latitude);
            longitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_DESTINATION_LONGITUDE, (float) MainActivity.home.longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void saveDestinationLocationToPreference(LatLng value) {
        //Log.d(TAG, "saveDestinationLocationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        if (value != null) {
            editor.putFloat(PREF_KEY_DESTINATION_LATITUDE, (float) value.latitude).apply();
            editor.putFloat(PREF_KEY_DESTINATION_LONGITUDE, (float) value.longitude).apply();
        } else {
            editor.putFloat(PREF_KEY_DESTINATION_LATITUDE, 0.0f).apply();
            editor.putFloat(PREF_KEY_DESTINATION_LONGITUDE, 0.0f).apply();
        }
    }

    public static double retrieveDestinationAltitudeFromPreference() {
        double altitude = 0.0;
        try {
            altitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_DESTINATION_ALTITUDE, 0.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return altitude;
    }

    public static void saveDestinationAltitudeToPreference(double value) {
        //Log.d(TAG, "saveDestinationAltitudeToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putFloat(PREF_KEY_DESTINATION_ALTITUDE, (float)value).apply();
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
        float latitude = 0.0f;
        float longitude = 0.0f;
        try {
            latitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_HOME_LATITUDE, (float) MainActivity.home.latitude);
            longitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_HOME_LONGITUDE, (float) MainActivity.home.longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void saveHomeLocationToPreference(LatLng value) {
        //Log.d(TAG, "saveDestinationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        if (value != null) {
            editor.putFloat(PREF_KEY_HOME_LATITUDE, (float) value.latitude).apply();
            editor.putFloat(PREF_KEY_HOME_LONGITUDE, (float) value.longitude).apply();
        } else {
            editor.putFloat(PREF_KEY_HOME_LATITUDE, 0.0f).apply();
            editor.putFloat(PREF_KEY_HOME_LONGITUDE, 0.0f).apply();
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
        float latitude = 0.0f;
        float longitude = 0.0f;
        try {
            latitude = Float.parseFloat(latLngString[0]);
            longitude = Float.parseFloat(latLngString[1]);
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
