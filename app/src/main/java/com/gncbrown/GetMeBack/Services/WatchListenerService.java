package com.gncbrown.GetMeBack.Services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.gncbrown.GetMeBack.Utilities.Preferences;
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
            Log.d(TAG, "Location: " + location);
            prefs.saveToPreferences("Location", location);
        } else if (messageEvent.getPath().startsWith(START_ACTIVITY_PATH)) {
            Log.d(TAG, "WatchListenerService.onMessageReceived: Starting activity");
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}