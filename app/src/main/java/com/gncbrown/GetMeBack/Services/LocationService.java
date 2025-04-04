package com.gncbrown.GetMeBack.Services;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.gncbrown.GetMeBack.R;
import com.gncbrown.GetMeBack.Utilities.ButtonWidgetReceiver;
import com.gncbrown.GetMeBack.Utilities.Logger;
import com.gncbrown.GetMeBack.Utilities.MySQLiteHelper;
import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.gncbrown.GetMeBack.Utilities.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = "LocationService";

    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "precise_location_channel";

    private static Context context;
    private static Preferences prefs;
    private static MySQLiteHelper dbHelper;

    private static String[] navigationMethods;

    private static GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private boolean useLocationBuilder = true;

    private FusedLocationProviderClient fuzedLocationClient;
    private LocationCallback locationCallback;

    public LocationService() {
        Log.d(TAG, "onStart:LocationService");
    }

    private Double latitude = 0.00;
    private Double longitude = 0.00;


    private class LoadActivity extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //progress(false);
        }

        @Override
        protected void onPreExecute() {
            //progress(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions();
            }
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
            }

            // TODO takes a long time
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String msg = String.format("LocationService.onStartCommand; flags=%s, startId=%s", flags, startId);
        Log.d(TAG, msg);
        context = getBaseContext();
        prefs = new Preferences(context);
        dbHelper = MySQLiteHelper.getInstance(this);
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Debug, msg);

        navigationMethods = getResources().getStringArray(R.array.navigationMethods);
        if (useLocationBuilder) {
            fuzedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Create a notification for the foreground service
            Notification notification = createNotification();

            try {
                // Promote the service to the foreground immediately
                startForeground(NOTIFICATION_ID, notification);

                if (intent.hasExtra("LocationAction")) {
                    String action = intent.getStringExtra("LocationAction");
                    if (action.equals(context.getResources().getString(R.string.ACTION_GET_LOCATION)))
                        startLocationUpdates(true);
                    else if (action.equals(context.getResources().getString(R.string.ACTION_GO_TO_DESTINATION)))
                        goToDestination();
                    else if (action.equals(context.getResources().getString(R.string.ACTION_LAUNCH)))
                        launchApp();
                    else
                        Log.e(TAG, String.format("onStartCommand; action %s unknown", action));
                }
            } catch (Exception e) {
                msg = "onStartCommand: Exception " + e.getMessage();
                Log.e(TAG, msg);
                dbHelper.appendLogTranscript(context, Logger.LogLevel.Error, msg);
                throw new RuntimeException(e);
            }
        } else {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        mGoogleApiClient.blockingConnect();
                    }
                });

                //mGoogleApiClient.connect();
            }
        }

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (intent.hasExtra("NavigationMode")) {
            String action = intent.getStringExtra("NavigationMode");
            if (action.equals(context.getResources().getString(R.string.ACTION_GET_LOCATION)))
                startLocationUpdates(true);
            else if (action.equals(context.getResources().getString(R.string.ACTION_GO_TO_DESTINATION)))
                goToDestination();
            else if (action.equals(context.getResources().getString(R.string.ACTION_LAUNCH)))
                launchApp();
            else
                Log.e(TAG, String.format("onStartCommand; action %s unknown", action));

        }
        return Service.START_NOT_STICKY;
    }

    private Notification createNotification() {
        String channelName = "Location Service Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription("Channel for Location Service");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.icon);

        return builder.build();
    }


    private void goToDestination() {
        LatLng destinationLatLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation");
        Double destinationLatitude = destinationLatLng.latitude;
        Double destinationLongitude = destinationLatLng.longitude;
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Debug, "LocationService.goToDestination: destinationLatitude="
                + destinationLatitude + ", destinationLongitude=" + destinationLongitude);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getApplicationContext());
        mBuilder.setTitle(String.format("Choose a navigation method to %s, %s",
                destinationLatLng.latitude, destinationLatLng.longitude));
        mBuilder.setSingleChoiceItems(navigationMethods, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selectedNavigationMethod = navigationMethods[i].toLowerCase().substring(0, 1);
                // Launch maps intent
                Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s&mode=%s", destinationLatitude, destinationLongitude,
                        selectedNavigationMethod));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                Log.d(TAG, "onLocationChanged: gmmIntentUri=" + gmmIntentUri);
                startActivity(mapIntent);

                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void launchApp() {
        String packageName = getApplicationContext().getResources().getString(R.string.myPackage);
        Intent intent = new Intent("android.intent.category.LAUNCHER");
        intent.setClassName(packageName, packageName + ".MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected void startLocationUpdates(boolean start) {
        int gpsRefreshRateMills = (int) prefs.retrieveFromPreferences("GPSRefreshRateMillis");
        int minUpdateIntervalMillis = (int) prefs.retrieveFromPreferences("MinUpdateIntervalMillis");
        int minUpdateDistanceMeters = (int) prefs.retrieveFromPreferences("MinUpdateDistanceMeters");
        int maxUpdateDelayMills = (int) prefs.retrieveFromPreferences("MaxUpdateDelayMillis");
        String msg = String.format("LocationService.startLocationUpdates: start=%s, gpsRefreshRateMills=%s, minUpdateIntervalMillis=%s, minUpdateDistanceMeters=%s, maxUpdateDelayMills=%s",
                start, gpsRefreshRateMills, minUpdateIntervalMillis, minUpdateDistanceMeters, maxUpdateDelayMills);
        Log.d(TAG, msg);
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Info, msg);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions();
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (start) {
            //progress(true);
            // Create the location request

            fuzedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationRequest = Utils.createLocationRequest(context);
            msg = String.format("LocationService.startLocationUpdates: mLocationRequest=%s", mLocationRequest);
            dbHelper.appendLogTranscript(context, Logger.LogLevel.Info, msg);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult == null) return;
                    for (Location location : locationResult.getLocations()) {
                        onLocationChanged(location);
                    }
                }

                @Override
                public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            };
            fuzedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } else {
                Log.d(TAG, "GoogleAPIClient not connected yet!");
                //Toast.makeText(getApplicationContext(), "GoogleAPIClient not connected yet!", Toast.LENGTH_SHORT).show();
            }

            Log.d("reque", "--->>>>");
        } else {
            //progress(false);
            if (fuzedLocationClient != null)
                fuzedLocationClient.removeLocationUpdates(locationCallback);
            fuzedLocationClient = null;
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient = null;
            Log.d("deque", "<<<<---");
        }
    }

    private void requestMultiplePermissions() {
        Log.d(TAG, "LocationService.requestMultiplePermissions");
        /*
        Dexter.withActivity(getApplicationContext())
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            Utils.openSettingsDialog(getApplicationContext());
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
         */
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Info, "LocationService.onConnected");
        startLocationUpdates(true);

        LoadActivity activityLoader = new LoadActivity();
        activityLoader.execute();

        if (mLocation == null) {
            startLocationUpdates(true);
        }
        if (mLocation != null) {
            Log.d(TAG, String.format("lat=%s, lon=%s", mLocation.getLatitude(), mLocation.getLongitude()));
        } else {
            //Toast.makeText(this, "Location not detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Info, "LocationService.onConnectionSuspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Error, "LocationService.onConnectionFailed="
                + connectionResult.getErrorCode());
        Log.e(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        String msg = "LocationService.onLocationChanged: " + latitude + ", " + longitude + " (" + location.getAltitude() + ")";
        Log.d(TAG, "onLocationChanged: " + msg);
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Debug, msg);

        prefs.saveToPreferences("DestinationLocation", new LatLng(latitude, longitude));
        prefs.saveToPreferences("DestinationAltitude", location.getAltitude());
        Utils.getAddressFromLocation(latitude, longitude, getApplicationContext(), locationAddressResultHandler);

        Utils.makeNotification(getApplicationContext(), "Acquire Location", msg, ButtonWidgetReceiver.REQ_CODE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if ((boolean)prefs.retrieveFromPreferences("ToneOnLocationUpdate"))
            Utils.playSound(context, R.raw.ding, 100);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Handler locationAddressResultHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            String destinationAddress = message.getData().getString("address");
            prefs.saveToPreferences("DestinationAddress", destinationAddress);
            String msg = "LocationService.locationAddressResultHandler, result=" + destinationAddress;
            Log.d(TAG, msg);
            dbHelper.appendLogTranscript(context, Logger.LogLevel.Debug, msg);
        }
    };

}