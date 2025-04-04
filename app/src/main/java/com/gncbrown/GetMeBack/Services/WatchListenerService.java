package com.gncbrown.GetMeBack.Services;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gncbrown.GetMeBack.MainActivity;
import com.gncbrown.GetMeBack.Utilities.Logger;
import com.gncbrown.GetMeBack.Utilities.MySQLiteHelper;
import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListenerService extends WearableListenerService {
    private static final String TAG = "PhoneListenerService";
    public static Preferences prefs;
    private static MySQLiteHelper dbHelper;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SEND_LOCATION_PATH = "/send-location";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String msg = String.format("WatchListenerService.onMessageReceived: %s", messageEvent);
        Log.d(TAG, msg);

        prefs = new Preferences(this);
        dbHelper = MySQLiteHelper.getInstance(this);
        dbHelper.appendLogTranscript(this, Logger.LogLevel.Debug, msg);
        if (messageEvent.getPath().contains(SEND_LOCATION_PATH)) {
            String packageName = new String(messageEvent.getData());
            Log.d(TAG, "Package name: " + packageName);
            String location = messageEvent.getPath().substring(SEND_LOCATION_PATH.length() + 1);
            dbHelper.appendLogTranscript(this, Logger.LogLevel.Debug, "location=" + location);
            try {
                String[] l = location.split(",");
                Log.d(TAG, "Location: " + location);
                prefs.saveToPreferences("DestinationLocation", new LatLng(Double.parseDouble(l[0]), Double.parseDouble(l[1])));

                LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
                Intent broadcastIntent = new Intent(MainActivity.ACTION_UPDATE_DESTINATION_FROM_WATCH);
                broadcastIntent.putExtra("latitude", Double.parseDouble(l[0]));
                broadcastIntent.putExtra("longitude", Double.parseDouble(l[1]));
                prefs.saveLatLngToPreferences("DestinationLocation",
                        new LatLng(Double.parseDouble(l[0]), Double.parseDouble(l[1])));
                boolean result = bManager.sendBroadcast(broadcastIntent);
                Log.d(TAG, "WatchListenerService.onMessageReceived: sendBroadcast=" + result);
            } catch (NumberFormatException e) {
                Log.e(TAG, "WatchListenerService.onMessageReceived, NumberFormatException", e);
                throw new RuntimeException(e);
            }
        } else if (messageEvent.getPath().startsWith(START_ACTIVITY_PATH)) {
            Log.d(TAG, "WatchListenerService.onMessageReceived: Starting activity");
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}