package com.gncbrown.GetMeBack.Utilities;
import android.content.SharedPreferences;

import com.gncbrown.GetMeBack.MainActivity;
import com.google.android.gms.maps.model.LatLng;

public class Prefs {

    private static final String PREF_KEY_DESTINATION_LATITUDE = "destinationLatitude";
    private static final String PREF_KEY_DESTINATION_LONGITUDE = "destinationLongitude";
    private static final String PREF_KEY_DESTINATION_HOME_ADDRESS = "homeAddress";
    private static final String PREF_KEY_DESTINATION_ADDRESS = "destinationAddress";
    private static final String PREF_KEY_FIRST_TIME = "firstTime";


    public static LatLng retrieveDestinationFromPreference() {
        float latitude = 0.0f;
        float longitude = 0.0f;
        try {
            latitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_DESTINATION_LATITUDE, 0.0f);
            longitude = MainActivity.sharedPreferences.getFloat(PREF_KEY_DESTINATION_LONGITUDE, 0.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void saveDestinationToPreference(LatLng value) {
        //Log.d(TAG, "saveDestinationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putFloat(PREF_KEY_DESTINATION_LATITUDE, (float) value.latitude).apply();
        editor.putFloat(PREF_KEY_DESTINATION_LONGITUDE, (float) value.longitude).apply();
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

    public static String retrieveHomeAddressFromPreference() {
        String homeAddress = "4 Frederick Drive, New Hartford, NY";
        try {
            homeAddress = MainActivity.sharedPreferences.getString(PREF_KEY_DESTINATION_HOME_ADDRESS, homeAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return homeAddress;
    }

    public static void saveHomeAddressToPreference(String value) {
        //Log.d(TAG, "saveDestinationToPreference, value=" + value);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putString(PREF_KEY_DESTINATION_HOME_ADDRESS, value).apply();
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
