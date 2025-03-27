package com.gncbrown.GetMeBack.Utilities;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.gncbrown.GetMeBack.MainActivity;
import com.gncbrown.GetMeBack.R;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Utils {
    private static final String TAG = "Utils";

    public static String packageURI = "android.resource://com.gncbrown.GetMeBackWatch";

    public static String getVersion() {
        return "Version " + com.gncbrown.GetMeBack.BuildConfig.VERSION_NAME
                + "\nDeveloper: George Brown"
                + "\nTester: Cindy Brown"
                + "\nemail: georgerobertbrown@gmail.com";
    }

    public static void old_showAlertDialog(Context context, String title, String message) {
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

    public static void showDialog(Context context, String title, String message, int iconId) {
        try {
            // Inflate the custom layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.dialog_scrollable_message, null);

            // Get a reference to the TextView
            TextView messageTextView = dialogView.findViewById(R.id.scrollable_message);

            // Set the message text
            messageTextView.setText(message);

            // Build the AlertDialog
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialogBuilder.setTitle(title);
            dialogBuilder.setIcon(iconId);

            // Set the custom view
            dialogBuilder.setView(dialogView);

            // Create and show the dialog
            Dialog dialog = dialogBuilder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "showAlertDialog: ", e);
            throw new RuntimeException(e);
        }
    }

    public static void showAlertDialog(Context context, String title, String message) {
        showDialog(context, title, message, android.R.drawable.ic_dialog_alert);
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
                        } else {
                            result = String.format("Empty address returned for: %s, %s", latitude, longitude);
                        }
                    } catch (IOException e) {
                        result = String.format("No address found for: %s, %s; e=%s", latitude, longitude, e.getMessage());
                        Log.e(TAG, "Cannot connect to Geocoder, e=", e);
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
                            Log.d(TAG, String.format("getAddressFromLocation.finally: address=%s %s,%s",
                                    result, latitude, longitude));
                        } else
                            msg.what = 0;
                        msg.sendToTarget();
                    }
                }
            };
            thread.start();
        } else {
            Log.e(TAG, "Location 0.0???");
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
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
            CharSequence name = context.getResources().getString(R.string.appName) + " Notification Channel";// The user-visible name of the channel.
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
        locationRequestBuilder.setMaxUpdates(1);
        return locationRequestBuilder.build();
    }

    public static void playSound(Context context, int resources, int delay) {
        MediaPlayer mMediaPlayer = null;
        boolean mStartPlaying = true;
        try {
            if (mStartPlaying) {
                mMediaPlayer = new MediaPlayer();

                Uri uri = Uri.parse(packageURI + "/" + resources);
                mMediaPlayer.setDataSource(context, uri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                int count = 0;
                do {
                    slumber(delay);
                } while (mMediaPlayer.isPlaying() && count++ < 5);
                mMediaPlayer.reset();
            }
            mStartPlaying = !mStartPlaying;
        } catch (IOException e) {
            Toast.makeText(
                            context,
                            getAppName(context)
                                    + " Could not play sound, reason "
                                    + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            Log.e(TAG, "playSound.prepare() failed");
        } finally {
            if (mMediaPlayer != null) {
                try {
                    mMediaPlayer.release();
                } catch (Exception e) {
                }
            }
            mMediaPlayer = null;
            mStartPlaying = false;
        }
    }

    public static void playSound(Context context, int resources) {
        playSound(context, resources, 500);
    }

    public static String getAppName(Context context) {
        return context.getResources().getString(R.string.appName);
    }

    public static void slumber(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isServiceRunning(Context context,
                                           String serviceClassName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            String serviceName = service.service.getClassName();

            // Log.d(TAG, "->service=" + serviceName);
            if (serviceName.contains(serviceClassName)) {
//				Log.d(TAG, "Service " + serviceClassName
//						+ " is ALREADY running");
                return true;
            }
        }
//		Log.d(TAG, "Service " + serviceClassName + " is NOT running");
        return false;
    }

}