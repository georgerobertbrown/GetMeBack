package com.gncbrown.GetMeBack.Services;

import android.Manifest;
import android.app.AlertDialog;
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
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.gncbrown.GetMeBack.R;
import com.gncbrown.GetMeBack.Utilities.ButtonWidgetReceiver;
import com.gncbrown.GetMeBack.Utilities.Prefs;
import com.gncbrown.GetMeBack.Utilities.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = LocationService.class.getSimpleName();

    private static String[] navigationMethods;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

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

    private void goToDestination() {
        LatLng destinationLatLng = Prefs.retrieveDestinationLocationFromPreference();
        Double destinationLatitude = destinationLatLng.latitude;
        Double destinationLongitude = destinationLatLng.longitude;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getApplicationContext());
        mBuilder.setTitle(String.format("Choose a navigation method to %s, %s",
                destinationLatLng.latitude, destinationLatLng.longitude));
        mBuilder.setSingleChoiceItems(navigationMethods, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selectedNavigationMethod = navigationMethods[i].toLowerCase().substring(0,1);
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
        intent.setClassName(packageName,packageName + ".MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected void startLocationUpdates(boolean start) {
        Log.d(TAG, "startLocationUpdates: start=" + start);
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
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL)
                    .setNumUpdates(1)
                    .setFastestInterval(FASTEST_INTERVAL);

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } else {
                Log.d(TAG, "GoogleAPIClient not connected yet!");
                Toast.makeText(getApplicationContext(), "GoogleAPIClient not connected yet!", Toast.LENGTH_SHORT).show();
            }

            Log.d("reque", "--->>>>");
        } else {
            //progress(false);
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        String msg = "Acquired location: " + latitude + ", " + longitude;
        Log.d(TAG, "onLocationChanged: " + msg);

        Prefs.saveDestinationLocationToPreference(new LatLng(latitude, longitude));
        Utils.getAddressFromLocation(latitude, longitude, getApplicationContext(), locationAddressResultHandler);

        Utils.makeNotification(getApplicationContext(), "Acquire Location", msg, ButtonWidgetReceiver.REQ_CODE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getBaseContext();
        int myStartID = startId;
        Log.d(TAG, "onStartCommand; flags=" + flags + ", startId=" + startId);


        navigationMethods = getResources().getStringArray(R.array.navigationMethods);

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
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        if (intent.hasExtra("action")) {
            String action = intent.getStringExtra("action");
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Handler locationAddressResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String destinationAddress = msg.getData().getString("address");
            Prefs.saveDestinationAddressToPreference(destinationAddress);
            Log.d(TAG, "addressResultHandler, result=" + destinationAddress);
        }
    };

}