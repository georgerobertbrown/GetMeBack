package com.gncbrown.GetMeBack;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.gncbrown.GetMeBack.Utilities.ButtonWidgetReceiver;
import com.gncbrown.GetMeBack.Utilities.Prefs;
import com.gncbrown.GetMeBack.Utilities.Utils;
import com.gncbrown.GetMeBack.directionhelpers.FetchURL;
import com.gncbrown.GetMeBack.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = "MainActivity";

    private static final int requestCode = 40;

    public static Context context;
    public static SharedPreferences sharedPreferences;

    SupportMapFragment mapFragment;

    private static CoordinatorLayout mainLayout;
    private static ProgressBar progressBar;


    int mapTypeIndex = 0;
    List<Integer> mapTypes = Arrays.asList(
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_HYBRID);
    public static String[] requiredPermissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private static boolean alreadyRegistered = false;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    private Polyline currentPolyline;


    private float zoom = 15.0f; //8.0f; // 10
    private GoogleMap mGoogleMap;

    private enum LocationSource {Uninitialized, DestinationLocation, CurrentLocation}

    private LocationSource locationSource = LocationSource.Uninitialized;

    private String navigationMethod = "d";

    private static String destinationAddress = null;
    private static String homeAddress = "4 Frederick Drive, New Hartford, NY";
    public static LatLng home = new LatLng(43.05687, -75.25245);

    private Double latitude = 0.00;
    private Double longitude = 0.00;
    private static Double destinationLatitude = 0.00;
    private static Double destinationLongitude = 0.00;
    private Double currentLatitude = 0.00;
    private Double currentLongitude = 0.00;

    private static Handler addressResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            destinationAddress = msg.getData().getString("address");
            Prefs.saveDestinationAddressToPreference(destinationAddress);
            progress(false);
            Log.d(TAG, "onLocationChanged: getAddressFromLocation, result=" + destinationAddress);
        }
    };


    private Handler newDestinationResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String newDestinationAddress = msg.getData().getString("address");
            Double newLatitide = msg.getData().getDouble("latitude");
            Double newLongitude = msg.getData().getDouble("longitude");
            progress(false);


            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Set destination?")
                    .setMessage(String.format("Set new destination location to %s (%s, %s).",
                            newDestinationAddress, newLatitide, newLongitude))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            LatLng newPoint = new LatLng(newLatitide, newLongitude);
                            destinationLatitude = newLatitide;
                            destinationLongitude = newLongitude;
                            Prefs.saveDestinationToPreference(newPoint);

                            locationSource = LocationSource.DestinationLocation;
                            destinationAddress = newDestinationAddress;
                            Prefs.saveDestinationAddressToPreference(destinationAddress);
                            String markerLabel = getMarkerLabel();
                            mGoogleMap.clear();
                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(newPoint)
                                    .title(markerLabel)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, zoom));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        }
                    });
            dialog.show();

            Log.d(TAG, "onLocationChanged: getAddressFromLocation, result=" + destinationAddress);
        }
    };


    private BroadcastReceiver updateDestinationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "updateDestinationReceiver.onReceive");
            locationSource = LocationSource.DestinationLocation;
            startLocationUpdates(true);
        }
    };

    private BroadcastReceiver gotoDestinationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "goToDestinationReceiver.onReceive");
            locationSource = LocationSource.CurrentLocation;
            goToDestination();
        }
    };


    private String[] navigationMethods;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_layout);

        context = this;
        sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        navigationMethods = getResources().getStringArray(R.array.navigationMethods);

        FragmentManager myFragmentManager = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fabLayer = (FloatingActionButton) findViewById(R.id.fabLayer);
        fabLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Switch map layer", Toast.LENGTH_SHORT).show();

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mGoogleMap = googleMap;
                        mapTypeIndex = ++mapTypeIndex % mapTypes.size();
                        googleMap.setMapType(mapTypes.get(mapTypeIndex));

                        if (locationSource == LocationSource.Uninitialized) {
                            googleMap.addMarker(new MarkerOptions()
                                    .position(home)
                                    .title(homeAddress)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
                        }
                    }
                });
            }
        });

        FloatingActionButton fabMark = (FloatingActionButton) findViewById(R.id.fabMark);
        fabMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Mark location", Toast.LENGTH_SHORT).show();
                locationSource = LocationSource.DestinationLocation;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress(true);
                        startLocationUpdates(true);
                        //TODO progress(false);
                    }
                });
            }
        });

        FloatingActionButton fabGo = (FloatingActionButton) findViewById(R.id.fabGo);
        fabGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationSource == LocationSource.Uninitialized) {
                    showAlert("Error", "Destination location not set");
                } else {
                    Toast.makeText(getApplicationContext(), "Return to mark", Toast.LENGTH_SHORT).show();
                    locationSource = LocationSource.CurrentLocation;
                    startLocationUpdates(true);
                }
            }
        });

        if (Prefs.retrieveFirstTimeFromPreference()) {
            Prefs.saveDestinationToPreference(home);
            Prefs.saveHomeAddressToPreference(homeAddress);

            Intent showHelp = new Intent(context, HelpDisplay.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "welcome");
            startActivityForResult(showHelp, requestCode);
        } else {
            if (!Utils.hasPermissions(requiredPermissions, context))
                requestMultiplePermissions();
            if (!Utils.hasPermissions(requiredPermissions, context))
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

            LatLng initialLatLng = Prefs.retrieveDestinationFromPreference();
            destinationLatitude = initialLatLng.latitude;
            destinationLongitude = initialLatLng.longitude;
            destinationAddress = Prefs.retrieveDestinationAddressFromPreference();
            getAddressFromLocation(destinationLatitude, destinationLongitude, context,
                    addressResultHandler);
        }

        registerReceivers(true);

        Prefs.saveFirstTimeToPreference(false);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 40) {

            if (!Utils.hasPermissions(requiredPermissions, context))
                requestMultiplePermissions();
            if (!Utils.hasPermissions(requiredPermissions, context))
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
    }
    @Override
    public void onPause() {
        super.onPause();
        //TODO registerReceivers(false);
        Prefs.saveDestinationToPreference(new LatLng(destinationLatitude, destinationLongitude));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers(true);

        destinationAddress = Prefs.retrieveDestinationAddressFromPreference(); // null;
        homeAddress = Prefs.retrieveHomeAddressFromPreference();
        home = Prefs.retrieveDestinationFromPreference();
        LatLng latLng = Prefs.retrieveDestinationFromPreference();
        if (latLng.latitude == 0.0f && latLng.longitude == 0.0f) {
            latLng = home;
        }
        destinationLatitude = latLng.latitude;
        destinationLongitude = latLng.longitude;

        if (mGoogleMap != null) {
            String markerLabel = getMarkerLabel();
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(markerLabel)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerReceivers(false);

        LatLng latLng = new LatLng(destinationLatitude, destinationLongitude);
        Prefs.saveDestinationToPreference(latLng);
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

        if (id == R.id.action_about) {
            Intent showHelp = new Intent(context, HelpDisplay.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "help");
            context.startActivity(showHelp);
            return true;
        } else if (id == R.id.action_welcome) {
            Intent showHelp = new Intent(context, HelpDisplay.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "welcome");
            context.startActivity(showHelp);
            return true;
        } else if (id == R.id.action_releases) {
            Intent showHelp = new Intent(context, HelpDisplay.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "changeLog");
            context.startActivity(showHelp);
            return true;
        } else if (id == R.id.action_set_home) {
            setHomeDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                setNewDestinationAlert(point);
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                marker.setTitle(getMarkerLabel());
                return false;
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationSource = LocationSource.Uninitialized;
        startLocationUpdates(true);

        if (true) {
            LoadActivity activityLoader = new LoadActivity();
            activityLoader.execute();
        } else {
           runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestMultiplePermissions();
                    }
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progress(true);
                    // TODO takes a long time
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    progress(false);
                }
            });
        }


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
        String msg = "Updated location: " + latitude + "," + longitude;
        Log.d(TAG, "onLocationChanged: " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        progress(false);

        String markerLabel = "";
        if (locationSource == LocationSource.CurrentLocation) {
            currentLatitude = latitude;
            currentLongitude = longitude;
            markerLabel = locationSource.toString(); //"Marker";
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(markerLabel)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));

            if (destinationLatitude != 0.0 && destinationLongitude != 0.0) {
                goToDestination();
            } else {
                Toast.makeText(this, "Current location not set.", Toast.LENGTH_SHORT).show();
            }
        } else if (locationSource == LocationSource.DestinationLocation) {
            destinationLatitude = latitude;
            destinationLongitude = longitude;
            destinationAddress = null;
            markerLabel = getMarkerLabel();

            LatLng latLng = new LatLng(latitude, longitude);
            Prefs.saveDestinationToPreference(latLng);

            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(markerLabel)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        } else {
            mGoogleMap.clear();

            locationSource = LocationSource.DestinationLocation;
            destinationAddress = homeAddress;
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(home)
                    .title("Home")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
        }

        startLocationUpdates(false);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    protected void startLocationUpdates(boolean start) {
        Log.d(TAG, "startLocationUpdates: start=" + start);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions();
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (start) {
            progress(true);
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
            progress(false);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
            Log.d("deque", "<<<<---");
        }
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
                            Utils.openSettingsDialog(MainActivity.this, context);
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

    private void showAlert(String title, String message) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private void setNewDestinationAlert(LatLng point) {
        progress(true);
        getAddressFromLocation(point.latitude, point.longitude, context,
                newDestinationResultHandler);
    }

    private void setHomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Home address");

        final EditText inputText = new EditText(this);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                destinationAddress = inputText.getText().toString();
                homeAddress = inputText.getText().toString();

                progress(true);
                home = getLocationFromAddress(destinationAddress);
                Prefs.saveHomeAddressToPreference(destinationAddress);
                Prefs.saveDestinationToPreference(home);

                destinationLatitude = home.latitude;
                destinationLongitude = home.longitude;
                locationSource = LocationSource.DestinationLocation;
                progress(false);

                mGoogleMap.clear();
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(home)
                        .title(destinationAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
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

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 2);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return p1;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mGoogleMap.addPolyline((PolylineOptions) values[0]);
    }

    private static void progress(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private class LoadActivity extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress(false);
        }

        @Override
        protected void onPreExecute() {
            progress(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions();
            }
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permissions not granted by user!", Toast.LENGTH_SHORT).show();
            }

            // TODO takes a long time
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return null;
        }
    }

    public static void getAddressFromLocation(final Double latitude, final Double longitude,
            final Context context, final Handler handler) {
        if (latitude != 0.0f && longitude != 0.0f) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    String result = null;
                    try {
                        List<Address> list = geocoder.getFromLocation(
                                latitude, longitude, 1);
                        if (list != null && list.size() > 0) {
                            Address address = list.get(0);
                            // sending back first address line and locality
                            result = address.getAddressLine(0) + ", " + address.getLocality();
                        }
                    } catch (IOException e) {
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

    public static String getMarkerLabel() {
        if (destinationAddress != null && !destinationAddress.equals("???"))
            return destinationAddress;

        // Start a handler to translate location to address
        getAddressFromLocation(destinationLatitude, destinationLongitude, context,
                addressResultHandler);

        // In the meantime, return something
        return String.format("%s, %s", destinationLatitude, destinationLongitude);
    }

    private void goToDestination() {
        LatLng destinationLatLng = new LatLng(destinationLatitude, destinationLongitude);
        LatLng startingLatLng = new LatLng(currentLatitude, currentLongitude);

        String url = getUrl(startingLatLng, destinationLatLng, "driving");
        Log.d(TAG, "onLocationChanged: url=" + url);
        new FetchURL(MainActivity.this).execute(url, "driving");

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Choose a navigation method to " + destinationAddress);
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

    private void registerReceivers(boolean flag) {
        Log.d("TAG", String.format("registerReceiver[flag=%s, alreadyRegistered=%s] for %s+%s", flag, alreadyRegistered,
                ButtonWidgetReceiver.ACTION_ACTIVITY_UPDATE_FROM_WIDGET,
                ButtonWidgetReceiver.ACTION_ACTIVITY_GO_TO_FROM_WIDGET));
        if (flag) {
                if (!alreadyRegistered) {
                    try {
                        registerReceiver(updateDestinationReceiver, new IntentFilter(ButtonWidgetReceiver.ACTION_ACTIVITY_UPDATE_FROM_WIDGET),
                                Context.RECEIVER_EXPORTED);
                        registerReceiver(gotoDestinationReceiver, new IntentFilter(ButtonWidgetReceiver.ACTION_ACTIVITY_GO_TO_FROM_WIDGET),
                                Context.RECEIVER_EXPORTED);
                    } catch (Exception e) {
                        Log.e(TAG, "Could not register receivers");
                    }
                } else {
                    Log.i(TAG, "Receivers already registered");
                }
            alreadyRegistered = true;
        } else {
            if (alreadyRegistered) {
                try {
                    unregisterReceiver(updateDestinationReceiver);
                    unregisterReceiver(gotoDestinationReceiver);
                } catch (Exception e) {
                    Log.e(TAG, "Could not unregister receivers");
                }
            } else {
                Log.i(TAG, "Receivers already unregistered");
            }
            alreadyRegistered = false;
        }

    }

}
