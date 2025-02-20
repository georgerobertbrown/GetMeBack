package com.gncbrown.GetMeBack.Utilities;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.gncbrown.GetMeBack.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Set;

public class Prefs {

    private static final String TAG = "Prefs";

    private static final String PREF_KEY_GPS_CACHE_LATITUDE = "gpsCacheLatitude";
    private static final String PREF_KEY_GPS_CACHE_LONGITUDE = "gpsCacheLongitude";
    private static final String PREF_KEY_DESTINATION_LATITUDE = "destinationLatitude";
    private static final String PREF_KEY_DESTINATION_LONGITUDE = "destinationLongitude";
    private static final String PREF_KEY_DESTINATION_ALTITUDE = "destinationAltitude";
    private static final String PREF_KEY_DESTINATION_ADDRESS = "destinationAddress";
    private static final String PREF_KEY_HOME_ADDRESS = "homeAddress";
    private static final String PREF_KEY_HOME_LATITUDE = "homeLatitude";
    private static final String PREF_KEY_HOME_LONGITUDE = "homeLongitude";
    private static final String PREF_KEY_FIRST_TIME = "firstTime";
    private static final String PREF_KEY_NAMED_LOCATIONS = "namedLocations";
    private static final String PREF_KEY_GPS_REFRESH_RATE_MILLIS = "gpsRefreshRate";
    private static final String PREF_KEY_MIN_UPDATE_DISTANCE_METERS = "minUpdateDistance";
    private static final String PREF_KEY_MIN_UPDATE_INTERVAL_MILLIS = "minUpdateInterval";
    private static final String PREF_KEY_MAX_UPDATE_DELAY_MILLIS = "maxUpdateDelay";
    private static final String PREF_KEY_SHOW_BUILDINGS = "showBuildings";
    private static final String PREF_KEY_SHOW_TRAFFIC = "showTraffic";
    private static final String PREF_KEY_INDOOR_MODE = "indoorMode";
    private static final String PREF_KEY_DEBUG_MODE = "debugMode";


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
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putString(PREF_KEY_GPS_CACHE_LATITUDE, doubleToString(location.getLatitude())).apply();
            editor.putString(PREF_KEY_GPS_CACHE_LONGITUDE, doubleToString(location.getLongitude())).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
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
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            if (value != null) {
                editor.putString(PREF_KEY_DESTINATION_LATITUDE, doubleToString(value.latitude)).apply();
                editor.putString(PREF_KEY_DESTINATION_LONGITUDE, doubleToString(value.longitude)).apply();
            } else {
                editor.putString(PREF_KEY_DESTINATION_LATITUDE, doubleToString(0.0)).apply();
                editor.putString(PREF_KEY_DESTINATION_LONGITUDE, doubleToString(0.0)).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
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
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putString(PREF_KEY_DESTINATION_ALTITUDE, doubleToString(value)).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
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
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putString(PREF_KEY_DESTINATION_ADDRESS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
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
        try {
            Set<String> locations = MainActivity.sharedPreferences.getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
            String[] arrayOfLocations = locations.toArray(new String[0]);
            return arrayOfLocations;
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveToNamedLocations(String name) {
        try {
            Set<String> locations = MainActivity.sharedPreferences.getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
            locations.remove(name);
            locations.add(name);
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putStringSet(PREF_KEY_NAMED_LOCATIONS, locations).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static LatLng retrieveNamedLocation(String name) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void removeNamedLocationFromPreference(String name) {
        try {
            Set<String> locations = MainActivity.sharedPreferences.getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
            locations.remove(name);
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.remove(name);
            editor.putStringSet(PREF_KEY_NAMED_LOCATIONS, locations).apply();
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveNamedLocationToPreference(String name, LatLng value) {
        //Log.d(TAG, "saveNamedLocationToPreference, name=" + name + ", value=" + value);
        try {
            saveToNamedLocations(name);

            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putString(name, String.format("%s,%s", value.latitude, value.longitude)).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveHomeAddressToPreference(String value) {
        //Log.d(TAG, "saveHomeAddressToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putString(PREF_KEY_HOME_ADDRESS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
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
            try {
                firstTime = MainActivity.sharedPreferences.getBoolean(PREF_KEY_FIRST_TIME, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return firstTime;
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveFirstTimeToPreference(boolean value) {
        //Log.d(TAG, "saveFirstTimeToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putBoolean(PREF_KEY_FIRST_TIME, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static long retrieveGPSRefreshRateMillisFromPreference() {
        long value = 0; // 1 second = 10000 ms
        try {
            value = 1000;
            value = MainActivity.sharedPreferences.getLong(PREF_KEY_GPS_REFRESH_RATE_MILLIS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveGPSRefreshRateMillisToPreference(long value) {
        //Log.d(TAG, "saveGPSRefreshRateToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putLong(PREF_KEY_GPS_REFRESH_RATE_MILLIS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static float retrieveMinUpdateDistanceMetersFromPreference() {
        float value = 1; // 1 meter min distance moved
        try {
            value = MainActivity.sharedPreferences.getFloat(PREF_KEY_MIN_UPDATE_DISTANCE_METERS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveMinUpdateDistanceMetersToPreference(float value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putFloat(PREF_KEY_MIN_UPDATE_DISTANCE_METERS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static long retrieveMinUpdateIntervalMillisFromPreference() {
        long value = 500;
        try {
            value = MainActivity.sharedPreferences.getLong(PREF_KEY_MIN_UPDATE_INTERVAL_MILLIS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveMinUpdateIntervalMillisToPreference(long value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putLong(PREF_KEY_MIN_UPDATE_INTERVAL_MILLIS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static long retrieveMaxUpdateDelayMillisFromPreference() {
        long value = 1; // 1 meter min distance moved
        try {
            value = MainActivity.sharedPreferences.getLong(PREF_KEY_MAX_UPDATE_DELAY_MILLIS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveMaxUpdateDelayMillisToPreference(long value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putLong(PREF_KEY_MAX_UPDATE_DELAY_MILLIS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean retrieveShowBuildingsFromPreference() {
        boolean value = false;
        try {
            value = MainActivity.sharedPreferences.getBoolean(PREF_KEY_SHOW_BUILDINGS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveShowBuildingsToPreference(boolean value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putBoolean(PREF_KEY_SHOW_BUILDINGS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean retrieveShowTrafficFromPreference() {
        boolean value = false;
        try {
            value = MainActivity.sharedPreferences.getBoolean(PREF_KEY_SHOW_TRAFFIC, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveShowTrafficToPreference(boolean value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putBoolean(PREF_KEY_SHOW_TRAFFIC, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean retrieveIndoorModeFromPreference() {
        boolean value = false;
        try {
            value = MainActivity.sharedPreferences.getBoolean(PREF_KEY_INDOOR_MODE, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveIndoorModeToPreference(boolean value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putBoolean(PREF_KEY_INDOOR_MODE, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean retrieveDebugModeFromPreference() {
        boolean value = false;
        try {
            value = MainActivity.sharedPreferences.getBoolean(PREF_KEY_DEBUG_MODE, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void saveDebugModeToPreference(boolean value) {
        try {
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putBoolean(PREF_KEY_DEBUG_MODE, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
