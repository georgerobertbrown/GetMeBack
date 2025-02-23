package com.gncbrown.GetMeBack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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

    private ArrowView arrowView;
    private TextView locationTextView;
    private float zoomLevel;
    private static final int BOUNDS_PADDING = 100;
    private static BitmapDescriptor destinationMarker =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            //BitmapDescriptorFactory.fromResource(R.drawable.pushpin_red_nobackground);
    private static BitmapDescriptor currentMarker =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
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

        arrowView = findViewById(R.id.arrowView);
        locationTextView = findViewById(R.id.locationTextView);
        locationTextView.setText("Coordinates: ");

        // Initialize the destination LatLng
        destinationLatLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation"); //new LatLng(34.0522, -118.2437); // Example: Los Angeles

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();
        createLocationCallback();
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

    private void createLocationRequest() {
        int intervalMillis = (int) prefs.retrieveFromPreferences("GPSRefreshRateMillis");
        float minUpdateDistanceMeters = (float) prefs.retrieveFromPreferences("MinUpdateDistanceMeters"); // 1 meters
        long minUpdateIntervalMillis = (long) prefs.retrieveFromPreferences("MinUpdateIntervalMillis"); // 500 millis
        long maxUpdateDelayMillis = (long) prefs.retrieveFromPreferences("MaxUpdateDelayMillis"); // 1000 millis
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, (long)intervalMillis);
        builder.setMinUpdateDistanceMeters(minUpdateDistanceMeters); // Minimum distance change for updates (e.g., 10 meters)
        builder.setMinUpdateIntervalMillis(minUpdateIntervalMillis); // minimum time between consecutive updates
        builder.setMaxUpdateDelayMillis(maxUpdateDelayMillis); // The longest an update may be delayed before it is sent to the client
        builder.setGranularity(Granularity.GRANULARITY_FINE); // Fine-grained location updates
        builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest = builder.build();
// TODO remove when debugged
//        locationRequest = LocationRequest.create();
//        locationRequest.setInterval(intervalMillis); //10000 Update interval in milliseconds (e.g., 10 seconds)
//        locationRequest.setFastestInterval(5000); // Fastest update interval (e.g., 5 seconds)
//        locationRequest.setSmallestDisplacement(1); // Minimum distance change for updates (e.g., 10 meters)
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // High accuracy
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
                    updateArrowDirection(location);
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
        // Draw a route to the destination
        drawRoute(currentLatLng, destinationLatLng);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationTextView.setText(String.format("Coordinates: %s, %s\nDistance: %s)",
                        location.getLatitude(), location.getLongitude(), formatDistance(distance)));
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

    private void updateArrowDirection(Location currentLocation) {
        Location destinationLocation = new Location("Destination");
        destinationLocation.setLatitude(destinationLatLng.latitude);
        destinationLocation.setLongitude(destinationLatLng.longitude);

        float bearing = currentLocation.bearingTo(destinationLocation);
        //float bearing = destinationLocation.bearingTo(currentLocation);
        arrowView.setBearing(bearing);
    }
}