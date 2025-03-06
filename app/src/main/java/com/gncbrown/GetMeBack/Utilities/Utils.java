package com.gncbrown.GetMeBack.Utilities;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.gncbrown.GetMeBack.MainActivity;
import com.gncbrown.GetMeBack.R;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Utils {
    private static final String TAG = "Utils";

    public static String getVersion() {
        return "Version " + com.gncbrown.GetMeBack.BuildConfig.VERSION_NAME
                + "\nDeveloper: George Brown"
                + "\nTester: Cindy Brown"
                + "\nemail: georgerobertbrown@gmail.com";
    }

    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setMessage(message);
        dialogBuilder.setTitle(title);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        Dialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

    public static void getAddressFromLocation(final Double latitude, final Double longitude,
                                              final Context context, final Handler handler) {
        Log.d(TAG, String.format("getAddressFromLocation: %s, %s", latitude, longitude));
        if (latitude != 0.0f && longitude != 0.0f) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    String result = String.format("No address found for: %s, %s", latitude, longitude);
                    try {
                        List<Address> list = geocoder.getFromLocation(
                                latitude, longitude, 1);
                        if (list != null && list.size() > 0) {
                            Address address = list.get(0);
                            // sending back first address line and locality
                            result = address.getAddressLine(0) + ", " + address.getLocality();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Impossible to connect to Geocoder", e);
                    } finally {
                        Message msg = Message.obtain();
                        msg.setTarget(handler);
                        if (result != null) {
                            msg.what = 1;
                            Bundle bundle = new Bundle();
                            bundle.putString("address", result);
                            bundle.putDouble("latitude", latitude);
                            bundle.putDouble("longitude", longitude);
                            msg.setData(bundle);
                        } else
                            msg.what = 0;
                        msg.sendToTarget();
                    }
                }
            };
            thread.start();
        }
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress) {
        LatLng p1 = new LatLng(MainActivity.home.latitude, MainActivity.home.longitude);
        try {
            Geocoder coder = new Geocoder(context);
            List<Address> address;
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return p1;
            }

            if (address == null || address.size() == 0) {
                return p1;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return p1;
    }

    public static void openSettingsDialog(Context context, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app requires location permissions. Grant them in app settings.");
        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(activity, intent, 101, null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public static void makeNotification(Context context, String title,
                                        String message, int reqCode) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "EEEE, dd MMMM yyyy hh:mm.SSS");
        String dateString = formatter.format(date);
        String notificationMessage = "Date: " + dateString +  "\n" + message;
        Intent intent = new Intent(context, MainActivity.class);
        showNotification(context, title, notificationMessage, intent, reqCode);
    }

    public static void showNotification(Context context, String title, String message, Intent intent, int reqCode) {
        intent.putExtra("From", title);
        intent.putExtra("Message", message);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        String channelId = context.getResources().getString(R.string.appName); //"channel_name";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id

        Log.d("showNotification", "showNotification: " + reqCode);
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

    public static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static LocationRequest createLocationRequest(Context context) {
        Preferences prefs = new Preferences(context);
        int gpsRefreshRateMillis = (int) prefs.retrieveFromPreferences("GPSRefreshRateMillis");
        int minUpdateDistanceMetersAsInteger = (int)prefs.retrieveFromPreferences("MinUpdateDistanceMeters");
        float minUpdateDistanceMeters = Float.valueOf(minUpdateDistanceMetersAsInteger); // 1 meters
        int minUpdateIntervalMillisAsInteger = (int) prefs.retrieveFromPreferences("MinUpdateIntervalMillis"); // 500 millis
        long minUpdateIntervalMillis = Long.valueOf(minUpdateIntervalMillisAsInteger); // 500 millis
        int maxUpdateDelayMillisAsInteger = (int) prefs.retrieveFromPreferences("MaxUpdateDelayMillis");
        long maxUpdateDelayMillis = Long.valueOf(maxUpdateDelayMillisAsInteger); // 1000 millis
        Log.d(TAG, String.format("startLocationUpdates; gpsRefreshRateMillis=%s, minUpdateIntervalMillis=%s, minUpdateDistanceMeters=%s, maxUpdateDelayMillisAsInteger=%s",
                gpsRefreshRateMillis, minUpdateIntervalMillis, minUpdateDistanceMeters, maxUpdateDelayMillisAsInteger));

        LocationRequest.Builder locationRequestBuilder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, (long)gpsRefreshRateMillis);
        locationRequestBuilder.setMinUpdateDistanceMeters(minUpdateDistanceMeters); // Minimum distance change for updates (e.g., 10 meters)
        locationRequestBuilder.setMinUpdateIntervalMillis(minUpdateIntervalMillis); // minimum time between consecutive updates
        locationRequestBuilder.setMaxUpdateDelayMillis(maxUpdateDelayMillis); // The longest an update may be delayed before it is sent to the client
        locationRequestBuilder.setGranularity(Granularity.GRANULARITY_FINE); // Fine-grained location updates
        locationRequestBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequestBuilder.setWaitForAccurateLocation(false);
        return locationRequestBuilder.build();
    }

}