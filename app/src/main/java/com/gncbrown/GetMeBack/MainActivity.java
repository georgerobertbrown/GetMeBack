package com.gncbrown.GetMeBack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.gncbrown.GetMeBack.directionhelpers.FetchURL;
import com.gncbrown.GetMeBack.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = "MainActivity";

    private static Context context;

    SupportMapFragment mapFragment;

    int mapTypeIndex = 0;
    List<Integer> mapTypes = Arrays.asList(
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_HYBRID);
    String[] requiredPermissions = { Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION } ;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    private Polyline currentPolyline;


    private GoogleMap mGoogleMap;

    private enum LocationSource { Uninitialized, DestinationLocation, CurrentLocation }
    private LocationSource locationSource = LocationSource.Uninitialized;
    private Double latitude = 0.00;
    private Double longitude = 0.00;
    private Double destinationLatitude = 0.00;
    private Double destinationLongitude = 0.00;
    private Double currentLatitude = 0.00;
    private Double currentLongitude = 0.00;

    private static final int permissionsCode = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager myFragmentManager = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mGoogleMap = googleMap;
                        mapTypeIndex = ++mapTypeIndex % mapTypes.size();
                        googleMap.setMapType(mapTypes.get(mapTypeIndex));

                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(37.4233438, -122.0728817))
                                .title("LinkedIn")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(37.4629101,-122.2449094))
                                .title("Facebook")
                                .snippet("Facebook HQ: Menlo Park"));

                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(37.3092293, -122.1136845))
                                .title("Apple"));

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.4233438, -122.0728817), 10));
                    }
                });
            }
        });

        if (!hasPermissions(requiredPermissions, context))
            requestMultiplePermissions();
        if (!hasPermissions(requiredPermissions, context))
            Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();

        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, new OnMapsSdkInitializedCallback() {
            @Override
            public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
                Log.d(TAG, "onMapsSdkInitialized");
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    private String getUrl(LatLng origin, LatLng destination, String directionMode) {
        String sOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String sDestination = "destination=" + destination.latitude + "," + destination.longitude;
        String parameters = sOrigin + "&" + sDestination + "&" + directionMode;
        String outputFormat = "json";
        String apiKey = getApiKey();
        String url = "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + parameters + "&key=" + apiKey;

        return url;
    }

    private String getApiKey() {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String apiKey = null;
        if (applicationInfo != null) {
            apiKey = applicationInfo.metaData.getString("com.google.android.maps.v2.API_KEY");
        }
        return apiKey;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            mGoogleMap.clear();
            if (latitude != 0.0 && longitude != 0.0) {
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("Original"));

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
            }
            return true;
        } else if (id == R.id.action_mark) {
            locationSource = LocationSource.DestinationLocation;
            startLocationUpdates(true);
            return true;
        } else if (id == R.id.action_return) {
            locationSource = LocationSource.CurrentLocation;
            startLocationUpdates(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.addMarker(new MarkerOptions()
              .position(new LatLng(37.4233438, -122.0728817))
              .title("LinkedIn")
              .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(37.4629101,-122.2449094))
                .title("Facebook")
                .snippet("Facebook HQ: Menlo Park"));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(37.3092293, -122.1136845))
                .title("Apple"));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.4233438, -122.0728817), 10));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions();
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
            return;
        }

        locationSource = LocationSource.Uninitialized;
        startLocationUpdates(true);
        // TODO takes a long time
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates(true);
        }
        if (mLocation != null) {
            Log.d(TAG, String.format("lat=%s, lon=%s", String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude())));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
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
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        String msg = "Updated Location: " +
                Double.toString(latitude) + "," +
                Double.toString(longitude);
        Log.d(TAG, "onLocationChanged: " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        String markerLabel = "";
        if (locationSource == LocationSource.CurrentLocation) {
            currentLatitude = latitude;
            currentLongitude = longitude;
            markerLabel = "Current";

            if (destinationLatitude != 0.0 && destinationLongitude != 0.0) {
                LatLng destinationLatLng = new LatLng(destinationLatitude, destinationLongitude);
                LatLng startingLatLng = new LatLng(currentLatitude, currentLongitude);
                String url = getUrl(startingLatLng, destinationLatLng, "driving");
                Log.d(TAG, "onLocationChanged: url=" + url);
                new FetchURL(MainActivity.this).execute(url, "driving");
            }
        } else {
            destinationLatitude = latitude;
            destinationLongitude = longitude;
            markerLabel = "Marker";
        }
        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(markerLabel));

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));

        startLocationUpdates(false);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    protected void startLocationUpdates(boolean start) {
        Log.d(TAG, "startLocationUpdates: start=" + start);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions();
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (start) {
            // Create the location request
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL)
                    .setNumUpdates(1)
                    .setFastestInterval(FASTEST_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
            Log.d("reque", "--->>>>");
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
            Log.d("deque", "--->>>>");
        }
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void  requestMultiplePermissions(){
        Log.d(TAG, "requestMultiplePermissions");
        Dexter.withActivity(this)
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
                            openSettingsDialog();
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
    }

    private void openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
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

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Precise Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public static boolean hasPermissions(String[] permissions, Context context) {
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mGoogleMap.addPolyline((PolylineOptions) values[0]);
    }

}
