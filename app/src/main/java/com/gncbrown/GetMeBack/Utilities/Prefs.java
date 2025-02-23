package com.gncbrown.GetMeBack.Utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
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



    public static LatLng  retrieveLocationOfflineFromPreference(Context context) {
        String latitude = Utils.doubleToString(0.0);
        String longitude = Utils.doubleToString(0.0);
        try {
            latitude = context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_GPS_CACHE_LATITUDE, latitude);
            longitude = context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_GPS_CACHE_LONGITUDE, longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(Utils.stringToDouble(latitude), Utils.stringToDouble(longitude));
    }

    public static void  saveLocationOfflineToPreference(Context context, Location location) {
        //Log.d(TAG, "saveLocationOfflineToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putString(PREF_KEY_GPS_CACHE_LATITUDE, Utils.doubleToString(location.getLatitude())).apply();
            editor.putString(PREF_KEY_GPS_CACHE_LONGITUDE, Utils.doubleToString(location.getLongitude())).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static LatLng  retrieveDestinationLocationFromPreference(Context context) {
        double latitude = MainActivity.home.latitude;
        double longitude = MainActivity.home.longitude;
        try {
            latitude = Utils.stringToDouble(context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_DESTINATION_LATITUDE, Utils.doubleToString(MainActivity.home.latitude)));
            longitude = Utils.stringToDouble(context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_DESTINATION_LONGITUDE, Utils.doubleToString(MainActivity.home.longitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void  saveDestinationLocationToPreference(Context context, LatLng value) {
        //Log.d(TAG, "saveDestinationLocationToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            if (value != null) {
                editor.putString(PREF_KEY_DESTINATION_LATITUDE, Utils.doubleToString(value.latitude)).apply();
                editor.putString(PREF_KEY_DESTINATION_LONGITUDE, Utils.doubleToString(value.longitude)).apply();
            } else {
                editor.putString(PREF_KEY_DESTINATION_LATITUDE, Utils.doubleToString(0.0)).apply();
                editor.putString(PREF_KEY_DESTINATION_LONGITUDE, Utils.doubleToString(0.0)).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static double  retrieveDestinationAltitudeFromPreference(Context context) {
        double altitude = 0.0;
        try {
            altitude = Utils.stringToDouble(context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_DESTINATION_ALTITUDE, Utils.doubleToString(altitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return altitude;
    }

    public static void  saveDestinationAltitudeToPreference(Context context, double value) {
        //Log.d(TAG, "saveDestinationAltitudeToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putString(PREF_KEY_DESTINATION_ALTITUDE, Utils.doubleToString(value)).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String  retrieveDestinationAddressFromPreference(Context context) {
        String destination = "???";
        try {
            destination = context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_DESTINATION_ADDRESS, "???");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destination;
    }

    public static void  saveDestinationAddressToPreference(Context context, String value) {
        //Log.d(TAG, "saveDestinationAddressToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putString(PREF_KEY_DESTINATION_ADDRESS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static LatLng  retrieveHomeLocationFromPreference(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            latitude = Utils.stringToDouble(context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_HOME_LATITUDE, Utils.doubleToString(MainActivity.home.latitude)));
            longitude = Utils.stringToDouble(context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_HOME_LONGITUDE, Utils.doubleToString(MainActivity.home.longitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }

    public static void  saveHomeLocationToPreference(Context context, LatLng value) {
        //Log.d(TAG, "saveDestinationToPreference, value=" + value);
        SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
        if (value != null) {
            editor.putString(PREF_KEY_HOME_LATITUDE, Utils.doubleToString(value.latitude)).apply();
            editor.putString(PREF_KEY_HOME_LONGITUDE, Utils.doubleToString(value.longitude)).apply();
        } else {
            editor.putString(PREF_KEY_HOME_LATITUDE, Utils.doubleToString(0.0)).apply();
            editor.putString(PREF_KEY_HOME_LONGITUDE, Utils.doubleToString(0.0)).apply();
        }
    }

    public static String[] retrieveNamedLocations(Context context) {
        try {
            Set<String> locations = context.getSharedPreferences("USER", MODE_PRIVATE).getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
            String[] arrayOfLocations = locations.toArray(new String[0]);
            return arrayOfLocations;
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveToNamedLocations(Context context, String name) {
        try {
            Set<String> locations = context.getSharedPreferences("USER", MODE_PRIVATE).getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
            locations.remove(name);
            locations.add(name);
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putStringSet(PREF_KEY_NAMED_LOCATIONS, locations).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static LatLng retrieveNamedLocation(Context context, String name) {
        try {
            String locationFor = context.getSharedPreferences("USER", MODE_PRIVATE).getString(name, "43.05687,-75.25245");
            String[] latLngString = locationFor.split(",");
            double latitude = 0.0;
            double longitude = 0.0;
            try {
                latitude = Utils.stringToDouble(latLngString[0]);
                longitude = Utils.stringToDouble(latLngString[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new LatLng(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void removeNamedLocationFromPreference(Context context, String name) {
        try {
            Set<String> locations = context.getSharedPreferences("USER", MODE_PRIVATE).getStringSet(PREF_KEY_NAMED_LOCATIONS, new HashSet<String>());
            locations.remove(name);
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.remove(name);
            editor.putStringSet(PREF_KEY_NAMED_LOCATIONS, locations).apply();
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void  saveNamedLocationToPreference(Context context, String name, LatLng value) {
        //Log.d(TAG, "saveNamedLocationToPreference, name=" + name + ", value=" + value);
        try {
            saveToNamedLocations(context, name);

            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putString(name, String.format("%s,%s", value.latitude, value.longitude)).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void  saveHomeAddressToPreference(Context context, String value) {
        //Log.d(TAG, "saveHomeAddressToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putString(PREF_KEY_HOME_ADDRESS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String  retrieveHomeAddressFromPreference(Context context) {
        String home = "???";
        try {
            home = context.getSharedPreferences("USER", MODE_PRIVATE).getString(PREF_KEY_HOME_ADDRESS, "???");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return home;
    }

    public static boolean  retrieveFirstTimeFromPreference(Context context) {
        boolean firstTime = true;
        try {
            try {
                firstTime = context.getSharedPreferences("USER", MODE_PRIVATE).getBoolean(PREF_KEY_FIRST_TIME, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return firstTime;
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void  saveFirstTimeToPreference(Context context, boolean value) {
        //Log.d(TAG, "saveFirstTimeToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putBoolean(PREF_KEY_FIRST_TIME, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static long  retrieveGPSRefreshRateMillisFromPreference(Context context) {
        long value = 0; // 1 second = 10000 ms
        try {
            value = 1000;
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getLong(PREF_KEY_GPS_REFRESH_RATE_MILLIS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveGPSRefreshRateMillisToPreference(Context context, long value) {
        //Log.d(TAG, "saveGPSRefreshRateToPreference, value=" + value);
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putLong(PREF_KEY_GPS_REFRESH_RATE_MILLIS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static float  retrieveMinUpdateDistanceMetersFromPreference(Context context) {
        float value = 1; // 1 meter min distance moved
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getFloat(PREF_KEY_MIN_UPDATE_DISTANCE_METERS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveMinUpdateDistanceMetersToPreference(Context context, float value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putFloat(PREF_KEY_MIN_UPDATE_DISTANCE_METERS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static long  retrieveMinUpdateIntervalMillisFromPreference(Context context) {
        long value = 500;
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getLong(PREF_KEY_MIN_UPDATE_INTERVAL_MILLIS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveMinUpdateIntervalMillisToPreference(Context context, long value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putLong(PREF_KEY_MIN_UPDATE_INTERVAL_MILLIS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static long  retrieveMaxUpdateDelayMillisFromPreference(Context context) {
        long value = 1; // 1 meter min distance moved
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getLong(PREF_KEY_MAX_UPDATE_DELAY_MILLIS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveMaxUpdateDelayMillisToPreference(Context context, long value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putLong(PREF_KEY_MAX_UPDATE_DELAY_MILLIS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean  retrieveShowBuildingsFromPreference(Context context) {
        boolean value = false;
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getBoolean(PREF_KEY_SHOW_BUILDINGS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveShowBuildingsToPreference(Context context, boolean value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putBoolean(PREF_KEY_SHOW_BUILDINGS, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean  retrieveShowTrafficFromPreference(Context context) {
        boolean value = false;
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getBoolean(PREF_KEY_SHOW_TRAFFIC, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveShowTrafficToPreference(Context context, boolean value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putBoolean(PREF_KEY_SHOW_TRAFFIC, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean  retrieveIndoorModeFromPreference(Context context) {
        boolean value = false;
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getBoolean(PREF_KEY_INDOOR_MODE, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveIndoorModeToPreference(Context context, boolean value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putBoolean(PREF_KEY_INDOOR_MODE, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean  retrieveDebugModeFromPreference(Context context) {
        boolean value = false;
        try {
            value = context.getSharedPreferences("USER", MODE_PRIVATE).getBoolean(PREF_KEY_DEBUG_MODE, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void  saveDebugModeToPreference(Context context, boolean value) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences("USER", MODE_PRIVATE).edit();
            editor.putBoolean(PREF_KEY_DEBUG_MODE, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
