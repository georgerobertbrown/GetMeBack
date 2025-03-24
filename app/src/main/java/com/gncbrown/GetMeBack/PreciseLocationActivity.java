package com.gncbrown.GetMeBack;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.gncbrown.GetMeBack.Utilities.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.PrintWriter;
import java.io.StringWriter;

public class PreciseLocationActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final String TAG = "PreciseLocationActivity";

    private static Context context;
    private static Preferences prefs;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng destinationLatLng;
    private double destinationAltitude;

    private TextView locationTextView;
    private float zoomLevel;
    private static final int BOUNDS_PADDING = 100;

    private static BitmapDescriptor destinationMarker =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
    //BitmapDescriptorFactory.fromResource(R.drawable.pushpin_red_nobackground);
    private static BitmapDescriptor currentMarker =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);


    public void handleUncaughtException(Thread thread, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        String pStackTrace = sStackTrace.replaceAll("\n\t", "\n...");
        String msg = "Unhandled exception: " + e.getMessage() + ", stack trace:\n"
                + pStackTrace;
        Log.e(TAG, msg);
        prefs.saveToPreferences("StackTrace", msg);

        System.exit(1); // kill off the crashed app
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable closeActivityRunnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showKillAlertDialog(context, "Battery Saver", "Closing navigation to save battery.");
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        if (destinationMarker == null)
            destinationMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        if (currentMarker == null)
            currentMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

        setContentView(R.layout.activity_precise_location);
        prefs = new Preferences(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            EdgeToEdge.enable(this);
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(0, topInset, 0, 0);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        destinationAltitude = (double) prefs.retrieveFromPreferences("DestinationAltitude");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationTextView = findViewById(R.id.locationTextView);
        locationTextView.setText("No location yet");

        // Initialize the destination LatLng
        destinationLatLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation"); //new LatLng(34.0522, -118.2437); // Example: Los Angeles

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = Utils.createLocationRequest(context);
        createLocationCallback();

        // Schedule the activity to close after 5 minutes (300,000 milliseconds)
        int killAfterMinutes = (int) prefs.retrieveFromPreferences("KillAfterMinutes");
        if (killAfterMinutes > 0)
            handler.postDelayed(closeActivityRunnable, killAfterMinutes*60*1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_precise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        String menuTitle = item.getTitle().toString();
        if (menuTitle.equals("Quit")) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled((boolean)prefs.retrieveFromPreferences("ShowBuildings"));
        mMap.setTrafficEnabled((boolean)prefs.retrieveFromPreferences("ShowTraffic"));
        mMap.setIndoorEnabled((boolean)prefs.retrieveFromPreferences("IndoorMode"));
        mMap.setOnCameraIdleListener(this);

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        mMap.setMyLocationEnabled(true);
        startLocationUpdates();
    }

    @Override
    public void onCameraIdle() {
        if (mMap != null) {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            zoomLevel = cameraPosition.zoom;
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    updateMapWithLocation(location);
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateMapWithLocation(Location location) {
        mMap.clear();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING);
        // Move the camera to the current location
        mMap.animateCamera(cameraUpdate);

        // Add a marker for the current location
        mMap.addMarker(new MarkerOptions().position(currentLatLng)
                .title("Current Location")
                //.icon(currentMarker)
        );
        mMap.addMarker(new MarkerOptions().position(destinationLatLng)
                .title("Destination")
                .icon(destinationMarker)
        );

        double distance = calculateDistanceHaversine(location.getLatitude(), location.getLongitude(),
                destinationLatLng.latitude, destinationLatLng.longitude);
        double altitudeDiff = destinationAltitude - location.getAltitude();
        // Draw a route to the destination
        drawRoute(currentLatLng, destinationLatLng);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationTextView.setText(String.format("Location: %s, %s\nDistance: %s\nAltitude: %s\n",
                        location.getLatitude(), location.getLongitude(), formatDistance(distance),
                        formatAltitude(altitudeDiff)));
            }
        });
    }

    private static String formatDistance(double distance) {
        if (distance < 1000) {
            return String.format("%.3fm", distance);
        } else {
            double kilometers = distance / 1000;
            return String.format("%.3fkm", kilometers);
        }
    }

    private static String formatAltitude(double altitude) {
        return String.format("%s%.3fm", (altitude < 0 ? "↓" : "↑"), Math.abs(altitude));
    }

    private static double calculateDistanceHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371000; // Earth's radius in meters

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences between the latitudes and longitudes
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Apply the Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance
        double distance = EARTH_RADIUS * c;
        return distance;
    }

    private void drawRoute(LatLng start, LatLng end) {
        // Clear any existing polylines
        mMap.clear();

        // Add a marker for the destination
        mMap.addMarker(new MarkerOptions().position(destinationLatLng)
                .title("Destination")
                .icon(destinationMarker)
        );

        // Create a polyline options object
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(start)
                .add(end)
                .color(Color.BLUE)
                .width(10);

        // Add the polyline to the map
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                onMapReady(mMap);
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(closeActivityRunnable);
    }

    private void showKillAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        dialogBuilder.setMessage(message);
        dialogBuilder.setTitle(title);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        Dialog dialog = dialogBuilder.create();
        dialog.show();
    }
}