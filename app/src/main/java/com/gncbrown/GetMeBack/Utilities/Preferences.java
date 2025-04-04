package com.gncbrown.GetMeBack.Utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gncbrown.GetMeBack.MainActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Preferences {
    private static final String TAG = "Preferences";

    private static final String PREFS_NAME = "MyPrefs";
    private static  Context context;

    private static final List<Entry>preferencesMap = new LinkedList<>();

    private static final String PREF_KEY_NAMED_LOCATIONS = "namedLocations";


    public Preferences(Context context) {
        this.context = context;
        preferencesMap.clear();
        preferencesMap.add(new Entry("LocationOffline", DataType.LATLNG, new LatLng(37.4219983, -122.084)));
        preferencesMap.add(new Entry("DestinationAltitude", DataType.DOUBLE, 0.0));
        preferencesMap.add(new Entry("DestinationLocation", DataType.LATLNG, new LatLng(37.4219983, -122.084)));
        preferencesMap.add(new Entry("DestinationAddress", DataType.STRING, "1650 Amphitheatre Pkwy, Mountain View, CA, 94043"));

        preferencesMap.add(new Entry("HomeLocation", DataType.LATLNG, new LatLng(43.056854, -75.252122)));
        preferencesMap.add(new Entry("HomeAddress", DataType.STRING, "4 Frederick Drive, New Hartford, NY 13413"));

        preferencesMap.add(new Entry("NamedLocation", DataType.LATLNG, new LatLng(0.0, 0.0)));
        preferencesMap.add(new Entry("NavigationMode", DataType.STRING, "Use precise navigation"));

        preferencesMap.add(new Entry("GPSRefreshRateMillis", DataType.INTEGER, 1000));
        preferencesMap.add(new Entry("MinUpdateIntervalMillis", DataType.INTEGER, 1000));
        preferencesMap.add(new Entry("MinUpdateDistanceMeters", DataType.INTEGER, 10));
        preferencesMap.add(new Entry("MaxUpdateDelayMillis", DataType.INTEGER, 1000));

        preferencesMap.add(new Entry("KillAfterMinutes", DataType.INTEGER, 5));

        preferencesMap.add(new Entry("ShowBuildings", DataType.BOOLEAN, true));
        preferencesMap.add(new Entry("ShowTraffic", DataType.BOOLEAN, true));
        preferencesMap.add(new Entry("IndoorMode", DataType.BOOLEAN, false));
        preferencesMap.add(new Entry("FirstTime", DataType.BOOLEAN, true));
        preferencesMap.add(new Entry("DebugMode", DataType.BOOLEAN, false));
        preferencesMap.add(new Entry("ToneOnLocationUpdate", DataType.BOOLEAN, false));

        preferencesMap.add(new Entry("StackTrace", DataType.STRING, ""));
        preferencesMap.add(new Entry("LogFileLimit", DataType.INTEGER, 100));
        preferencesMap.add(new Entry("LogLevel", DataType.STRING, "Normal"));
        preferencesMap.add(new Entry("ReverseLog", DataType.BOOLEAN, false));
        preferencesMap.add(new Entry("FilterLog", DataType.STRING, ""));
        preferencesMap.add(new Entry("SearchLog", DataType.STRING, ""));
        preferencesMap.add(new Entry("LogFileLimit", DataType.INTEGER, 100));
    }

    public enum DataType {
        LONG(Long.class),
        INTEGER(Integer.class),
        STRING(String.class),
        FLOAT(Float.class),
        BOOLEAN(Boolean.class),
        DOUBLE(Double.class),
        SET(HashSet.class),
        LATLNG(LatLng .class);

        private final Class<?> type;

        DataType(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }

        public static DataType fromString(String typeName) {
            for (DataType dataType : DataType.values()) {
                if (dataType.getType().getSimpleName().equalsIgnoreCase(typeName)) {
                    return dataType;
                }
            }
            throw new IllegalArgumentException("Invalid data type: " + typeName);
        }
    }

    public class Entry {
        String keyName;
        DataType keyType;
        Object defaultValue;

        public Entry(String keyName, DataType keyType, Object defaultValue) {
            this.keyName = keyName;
            this.keyType = keyType;
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "keyName='" + keyName + '\'' +
                    ", keyType=" + keyType +
                    ", defaultValue=" + defaultValue + '}';
        }
    }

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

    public Object retrieveFromPreferences(String key) {
        Object e = getEntryFor(key);
        if (e == null) {
            return null;
        }
        Entry entry = (Entry) e;
        switch (entry.keyType) {
            case LATLNG:
                return retrieveLatLngFromPreferences(key);
            case DOUBLE:
                return retrieveDoubleFromPreferences(key);
            case FLOAT:
                return retrieveFloatFromPreferences(key);
            case LONG:
                return retrieveLongFromPreferences(key);
            case INTEGER:
                return retrieveIntegerFromPreferences(key);
            case BOOLEAN:
                return retrieveBooleanFromPreferences(key);
            case STRING:
                return retrieveStringToPreferences(key);
            default:
                return null;
        }
    }

    public void saveToPreferences(String key, Object value) {
        Object e = getEntryFor(key);
        if (e == null) {
            Log.e(TAG, "Key not found: " + key);
            return;
        }
        Entry entry = (Entry) e;
        switch (entry.keyType) {
            case LATLNG:
                saveLatLngToPreferences(key, (LatLng) value);
                break;
            case DOUBLE:
                saveDoubleToPreferences(key, (Double) value);
                break;
            case FLOAT:
                saveFloatToPreferences(key, (Float) value);
                break;
            case LONG:
                saveLongToPreferences(key, (Long) value);
                break;
            case INTEGER:
                saveIntegerToPreferences(key, (int) value);
                break;
            case BOOLEAN:
                saveBooleanToPreferences(key, (Boolean) value);
                break;
            case STRING:
                saveStringToPreferences(key, (String) value);
                break;
        }
    }

    public String retrieveStringToPreferences(String key) {
        try {
            String defaultValue = (String)getEntryFor(key).defaultValue;
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void saveStringToPreferences(String key, String value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public boolean retrieveBooleanFromPreferences(String key) {
        try {
            Boolean defaultValue = (Boolean)getEntryFor(key).defaultValue;
            return context.getSharedPreferences(PREFS_NAME, Context
                    .MODE_PRIVATE).getBoolean(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveBooleanToPreferences(String key, boolean value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    public float retrieveFloatFromPreferences(String key) {
        try {
            Float defaultValue = (Float)getEntryFor(key).defaultValue;
            return context.getSharedPreferences(PREFS_NAME, Context
                    .MODE_PRIVATE).getFloat(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    public void saveFloatToPreferences(String key, float value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putFloat(key, value).apply();
    }

    public long retrieveLongFromPreferences(String key) {
        try {
            long defaultValue = (long)getEntryFor(key).defaultValue;
            return context.getSharedPreferences(PREFS_NAME, Context
                    .MODE_PRIVATE).getLong(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public void saveLongToPreferences(String key, long value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putLong(key, value).apply();
    }

    public int retrieveIntegerFromPreferences(String key) {
        try {
            int defaultValue = (int)getEntryFor(key).defaultValue;
            return context.getSharedPreferences(PREFS_NAME, Context
                    .MODE_PRIVATE).getInt(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void saveIntegerToPreferences(String key, int value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    public LatLng retrieveLatLngFromPreferences(String key) {
        try {
            LatLng defaultValue = (LatLng)getEntryFor(key).defaultValue;
            String defaultValueString = doubleToString(defaultValue.latitude) + "," + doubleToString(defaultValue.longitude);
            String value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(key, defaultValueString);
            String[] latLngString = value.split(",");
            return new LatLng(stringToDouble(latLngString[0]), stringToDouble(latLngString[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return new LatLng(0.0, 0.0);
        }
    }

    public void saveLatLngToPreferences(String key, LatLng value) {
        if (value == null)
            return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(key,
                doubleToString(value.latitude) + "," + doubleToString(value.longitude)).apply();
    }

    public double retrieveDoubleFromPreferences(String key) {
        double defaultValue = (Double)getEntryFor(key).defaultValue;
        try {
            return stringToDouble(context.getSharedPreferences(PREFS_NAME, Context
                    .MODE_PRIVATE).getString(key, doubleToString(defaultValue)));
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public void saveDoubleToPreferences(String key, double value) {
        String valueString = doubleToString(value);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(key, valueString).apply();
    }

    public List<NamedLocation> retrieveNamedLocationsFromPreferences() {
        List<NamedLocation> defaultNamedLocations = new ArrayList<NamedLocation>();
        NamedLocation defaultNamedLocation = new NamedLocation("Home", new LatLng(43.056854, -75.252122));
        defaultNamedLocations.add(defaultNamedLocation);
        Gson gson = new Gson();
        String jsonString = gson.toJson(defaultNamedLocations);

        String locationsString = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_KEY_NAMED_LOCATIONS, jsonString);
        List<NamedLocation> namedLocations = getList(locationsString, NamedLocation.class);
        if (namedLocations == null) {
            namedLocations = new ArrayList<NamedLocation>();
        }
        boolean foundHome = false;
        for (NamedLocation namedLocation : namedLocations) {
            if (namedLocation.getName().equals("Home")) {
                foundHome = true;
                break;
            }
        }
        if (!foundHome) {
            namedLocations.add(defaultNamedLocation);
            saveNamedLocationToPreferences(defaultNamedLocation.getName(), defaultNamedLocation.getLatLng());
        }
        return namedLocations;
    }

    public void saveNamedLocationToPreferences(NamedLocation value) {
        saveNamedLocationToPreferences(value.getName(), value.getLatLng());
    }

    public LatLng retrieveNamedLocationFromPreferences(String name) {
        try {
            List<NamedLocation> namedLocations = retrieveNamedLocationsFromPreferences();
            if (namedLocations == null) {
                namedLocations = new ArrayList<NamedLocation>();
            }
            for (NamedLocation namedLocation : namedLocations) {
                if (namedLocation.getName().equals(name)) {
                    return namedLocation.getLatLng();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
        return new LatLng(MainActivity.home.latitude, MainActivity.home.longitude);
    }

    public void removeNamedLocationFromPreferences(String name) {
        try {
            List<NamedLocation> namedLocations = retrieveNamedLocationsFromPreferences();
            if (namedLocations == null) {
                namedLocations = new ArrayList<NamedLocation>();
            }
            for (NamedLocation namedLocation : namedLocations) {
                if (namedLocation.getName().equals(name)) {
                    namedLocations.remove(namedLocation);
                    break;
                }
            }

            Gson gson = new Gson();
            String jsonString = gson.toJson(namedLocations);

            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(PREF_KEY_NAMED_LOCATIONS, jsonString).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveNamedLocationToPreferences(String name, LatLng value) {
        try {
            String locationsString = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_KEY_NAMED_LOCATIONS, "");
            List<NamedLocation> namedLocations = getList(locationsString, NamedLocation.class);
            if (namedLocations == null) {
                namedLocations = new ArrayList<NamedLocation>();
            }
            for (NamedLocation namedLocation : namedLocations) {
                if (namedLocation.getName().equals(name)) {
                    namedLocations.remove(namedLocation);
                    break;
                }
            }
            NamedLocation location = new NamedLocation(name, value);
            namedLocations.add(location);

            Gson gson = new Gson();
            String jsonString = gson.toJson(namedLocations);

            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(PREF_KEY_NAMED_LOCATIONS, jsonString).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences error=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> getList(String jsonArray, Class<T> clazz) {
        Type typeOfT = TypeToken.getParameterized(List.class, clazz).getType();
        return new Gson().fromJson(jsonArray, typeOfT);
    }

    public static <T> T getFromJson(String json, Class<T> clazz) {
        Type typeOfT = TypeToken.get(clazz).getType();
        return new Gson().fromJson(json, typeOfT);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : preferencesMap) {
            sb.append(entry.toString()).append(": ")
                    .append(" = ")
                    .append(retrieveFromPreferences(entry.keyName))
                    .append("\n");
        }
        return sb.toString();
    }

    public Entry getEntryFor(String key) {
        for (Entry entry : preferencesMap) {
            if (entry.keyName.equals(key)) {
                return entry;
            }
        }
        Log.e(TAG, "Key not found: " + key);
        return null;
    }

    public void resetPreferences() {
        for (Entry entry : preferencesMap) {
            if (entry.keyType == DataType.LATLNG)
                saveLatLngToPreferences(entry.keyName, (LatLng) entry.defaultValue);
            else if (entry.keyType == DataType.DOUBLE)
                saveDoubleToPreferences(entry.keyName, (Double) entry.defaultValue);
            else if (entry.keyType == DataType.FLOAT)
                saveFloatToPreferences(entry.keyName, (Float) entry.defaultValue);
            else if (entry.keyType == DataType.LONG)
                saveLongToPreferences(entry.keyName, (Long) entry.defaultValue);
            else if (entry.keyType == DataType.INTEGER)
                saveIntegerToPreferences(entry.keyName, (Integer) entry.defaultValue);
            else if (entry.keyType == DataType.BOOLEAN)
                saveBooleanToPreferences(entry.keyName, (Boolean) entry.defaultValue);
            else if (entry.keyType == DataType.STRING)
                saveStringToPreferences(entry.keyName, (String) entry.defaultValue);
            else
                Log.e(TAG, "Unknown data type: " + entry.keyType);
        }
    }
}
