package com.gncbrown.GetMeBack.Services;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gncbrown.GetMeBack.MainActivity;
import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListenerService extends WearableListenerService {
    private static final String TAG = "PhoneListenerService";
    public static Preferences prefs;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SEND_LOCATION_PATH = "/send-location";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "WatchListenerService.onMessageReceived: " + messageEvent);

        prefs = new Preferences(this);
        if (messageEvent.getPath().startsWith(SEND_LOCATION_PATH)) {
            String packageName = new String(messageEvent.getData());
            Log.d(TAG, "Package name: " + packageName);
            String location = messageEvent.getPath().substring(SEND_LOCATION_PATH.length() + 1);
            try {
                String[] l = location.split(",");
                Log.d(TAG, "Location: " + location);
                prefs.saveToPreferences("DestinationLocation", new LatLng(Double.parseDouble(l[0]), Double.parseDouble(l[1])));

                LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
                Intent broadcastIntent = new Intent(MainActivity.ACTION_UPDATE_DESTINATION_FROM_WATCH);
                broadcastIntent.putExtra("latitude", Double.parseDouble(l[0]));
                broadcastIntent.putExtra("longitude", Double.parseDouble(l[1]));
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