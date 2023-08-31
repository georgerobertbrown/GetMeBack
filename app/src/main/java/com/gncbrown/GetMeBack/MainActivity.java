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

import com.gncbrown.GetMeBack.Utilities.BackgroundTask;
import com.gncbrown.GetMeBack.Utilities.ButtonWidgetReceiver;
import com.gncbrown.GetMeBack.Utilities.Prefs;
import com.gncbrown.GetMeBack.Utilities.Utils;
import com.gncbrown.GetMeBack.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = "MainActivity";

    private static final int requestCode = 40;

    public static Context mContext;
    public static SharedPreferences sharedPreferences;

    SupportMapFragment mapFragment;

    private static CoordinatorLayout mainLayout;
    private static ProgressBar progressBar;

    private static String[] navigationMethods;

    int mapTypeIndex = 0;
    List<Integer> mapTypes = Arrays.asList(
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_HYBRID);
    public static String[] requiredPermissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS};
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
    private static String homeAddress = "1650 Amphitheatre Pkwy, Mountain View, CA, 94043"; //"4 Frederick Drive, New Hartford, NY";
    public static LatLng home = new LatLng(37.4219983, -122.084); //new LatLng(43.05687, -75.25245);

    private Double latitude = 0.00;
    private Double longitude = 0.00;
    private static Double destinationLatitude = 0.00;
    private static Double destinationLongitude = 0.00;
    private Double currentLatitude = 0.00;
    private Double currentLongitude = 0.00;
    private long lastMarkerTime = System.currentTimeMillis();;

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
                            Prefs.saveDestinationLocationToPreference(newPoint);

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
            requestLocationUpdate(true);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_layout);

        mContext = this;
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
                        requestLocationUpdate(true);
                    }
                });
            }
        });

        FloatingActionButton fabGo = (FloatingActionButton) findViewById(R.id.fabGo);
        fabGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationSource == LocationSource.Uninitialized) {
                    Utils.showAlertDialog(mContext, "Error", "Destination location not set");
                } else {
                    Toast.makeText(getApplicationContext(), "Return to mark", Toast.LENGTH_SHORT).show();
                    goToDestination();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        if (Prefs.retrieveFirstTimeFromPreference()) {
            Prefs.saveDestinationLocationToPreference(home);
            Prefs.saveHomeLocationToPreference(home);
            Prefs.saveHomeAddressToPreference(homeAddress);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            Intent showHelp = new Intent(mContext, HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "welcome");
            startActivityForResult(showHelp, requestCode);
        } else {
            if (!Utils.hasPermissions(requiredPermissions, mContext))
                requestMultiplePermissions();
            if (!Utils.hasPermissions(requiredPermissions, mContext))
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

            LatLng initialLatLng = Prefs.retrieveDestinationLocationFromPreference();
            destinationLatitude = initialLatLng.latitude;
            destinationLongitude = initialLatLng.longitude;
            destinationAddress = Prefs.retrieveDestinationAddressFromPreference();

            Utils.getAddressFromLocation(destinationLatitude, destinationLongitude, mContext,
                    addressResultHandler);
        }

        registerReceivers(true);

        Prefs.saveFirstTimeToPreference(false);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 40) {

            if (!Utils.hasPermissions(requiredPermissions, mContext))
                requestMultiplePermissions();
            if (!Utils.hasPermissions(requiredPermissions, mContext))
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
        Prefs.saveDestinationLocationToPreference(new LatLng(destinationLatitude, destinationLongitude));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers(true);

        destinationAddress = Prefs.retrieveDestinationAddressFromPreference(); // null;
        homeAddress = Prefs.retrieveHomeAddressFromPreference();
        home = Prefs.retrieveDestinationLocationFromPreference();
        LatLng latLng = Prefs.retrieveDestinationLocationFromPreference();
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
        Prefs.saveDestinationLocationToPreference(latLng);
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

        if (id == R.id.actionAbout) {
            Intent showHelp = new Intent(mContext, HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "help");
            mContext.startActivity(showHelp);
            return true;
        } else if (id == R.id.actionWelcome) {
            Intent showHelp = new Intent(mContext, HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "welcome");
            mContext.startActivity(showHelp);
            return true;
        } else if (id == R.id.actionReleases) {
            Intent showHelp = new Intent(mContext, HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "changeLog");
            mContext.startActivity(showHelp);
            return true;
        } else if (id == R.id.actionSetHome) {
            setHomeDialog();
            return true;
        } else if (id == R.id.actionRestoreHome) {
            restoreHome();
            return true;
        } else if (id == R.id.actionValues) {
            LatLng destinationLatLng = Prefs.retrieveDestinationLocationFromPreference();
            String destinationAddress = Prefs.retrieveDestinationAddressFromPreference();
            LatLng homeLatLng = Prefs.retrieveHomeLocationFromPreference();
            String homeAddress = Prefs.retrieveHomeAddressFromPreference();
            String values = String.format("Lat/Lng: %s, %s\nAddress: %s\nHome LatLng: %s, %s\nHome: %s",
                    destinationLatLng.latitude, destinationLatLng.longitude, destinationAddress,
                    homeLatLng.latitude, homeLatLng.longitude, homeAddress);
            Utils.showAlertDialog(mContext, "Values", values);
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
                long markerTime = System.currentTimeMillis();
                if (markerTime - lastMarkerTime < 500.0) {
                    Log.d(TAG, "double click detected!");
                }
                marker.setTitle(getMarkerLabel());
                lastMarkerTime = System.currentTimeMillis();
                return false;
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        LatLng latLng = Prefs.retrieveDestinationLocationFromPreference();
        if (latLng.latitude == 0.0f && latLng.longitude == 0.0f) {
            latLng = home;
        }

        if (mGoogleMap != null) {
            String markerLabel = getMarkerLabel();
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(markerLabel)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }

        locationSource = LocationSource.Uninitialized;
        startLocationService();
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
            Prefs.saveDestinationLocationToPreference(latLng);

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
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    private void startLocationService() {
        BackgroundTask bg = new BackgroundTask() {
            @Override
            public void onPreExecute() {
                progress(true);
            }

            @Override
            public void doInBackground() {
                if (ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestMultiplePermissions();
                }
                if (ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    toastMessage("Permissions not granted by user!");
                }

                // TODO takes a long time
                if (mGoogleApiClient.isConnected())
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                else
                    Log.e(TAG, "startLocationService.doInBackground not connected");
            }

            @Override
            public void onPostExecute() {
                progress(false);
            }
        };
        bg.execute();
    }

    private void requestLocationUpdate(boolean start) {
        Log.d(TAG, "requestLocationUpdate");
        // Request location updates
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions();
        }
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastMessage("Permissions not granted by user!");
            return;
        }

        if (start) {
            progress(true);
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setNumUpdates(1);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationCallback mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null)
                        return;
                    Location location = locationResult.getLocations().get(0);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        LatLng updatedLocation = new LatLng(latitude, longitude);
                        String locationString = String.format("%s, %s", latitude, longitude);
                        String msg = "Updated location: " + locationString;
                        Log.d(TAG, "onLocationResult: " + msg);

                        Prefs.saveDestinationLocationToPreference(updatedLocation);

                        mGoogleMap.clear();
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(updatedLocation)
                                .title(locationString)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updatedLocation, zoom));
                        toastMessage(msg);

                        Utils.getAddressFromLocation(latitude, longitude, mContext, addressResultHandler);
                    }
                }
            };
            LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest,
                    mLocationCallback, null);
            Log.d("enque", "--->>>>");
        } else {
            progress(false);
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("deque", "<<<<---");
        }
    }

    private void  requestMultiplePermissions() {
        Log.d(TAG, "requestMultiplePermissions");
        Dexter.withActivity(this)
                .withPermissions(requiredPermissions)
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
                            Utils.openSettingsDialog(MainActivity.this, mContext);
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

    private void setNewDestinationAlert(LatLng point) {
        progress(true);
        Utils.getAddressFromLocation(point.latitude, point.longitude, mContext,
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
                home = Utils.getLocationFromAddress(destinationAddress, mContext);
                Prefs.saveHomeAddressToPreference(destinationAddress);
                Prefs.saveHomeLocationToPreference(home);
                Prefs.saveDestinationLocationToPreference(home);

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

    private void restoreHome() {
        progress(true);
        LatLng homeLocation = Prefs.retrieveHomeLocationFromPreference();
        String homeAddress = Prefs.retrieveHomeAddressFromPreference();

        Prefs.saveDestinationLocationToPreference(homeLocation);
        Prefs.saveHomeAddressToPreference(homeAddress);

        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions()
                .position(homeLocation)
                .title(homeAddress)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(homeLocation, zoom));

        progress(false);
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

    public static String getMarkerLabel() {
        if (destinationAddress != null && !destinationAddress.equals("???"))
            return destinationAddress;

        // Start a handler to translate location to address
        Utils.getAddressFromLocation(destinationLatitude, destinationLongitude, mContext,
                addressResultHandler);

        // In the meantime, return something
        return String.format("%s, %s", destinationLatitude, destinationLongitude);
    }

    private void goToDestination() {
        String destination = Prefs.retrieveDestinationAddressFromPreference();
        LatLng destinationLatLng = Prefs.retrieveDestinationLocationFromPreference();
        Double destinationLatitude = destinationLatLng.latitude;
        Double destinationLongitude = destinationLatLng.longitude;

        if (destinationLatitude == 0.0 && destinationLongitude == 0.0) {
            Utils.showAlertDialog(this, "Error", "Destination not set!");
        } else {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setTitle(String.format("Choose a navigation method to %s", destination));
            //destinationLatLng.latitude, destinationLatLng.longitude));
            mBuilder.setSingleChoiceItems(navigationMethods, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        String selectedNavigationMethod = navigationMethods[i].toLowerCase().substring(0, 1);
                        // Launch maps intent
                        Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s&mode=%s", destinationLatitude, destinationLongitude,
                                selectedNavigationMethod));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        Log.d(TAG, "onLocationChanged: gmmIntentUri=" + gmmIntentUri);
                        startActivity(mapIntent);
                    } catch (Exception e) {
                        Utils.showAlertDialog(mContext, "Navigation Error", e.getMessage());
                    }

                    dialogInterface.dismiss();
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        }
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

    public void toastMessage(String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}