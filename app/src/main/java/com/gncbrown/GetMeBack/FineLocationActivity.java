package com.gncbrown.GetMeBack;

import android.Manifest;
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

import com.gncbrown.GetMeBack.Utilities.Prefs;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class FineLocationActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final String TAG = "FineLocationActivity";
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fine_location);

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
        destinationLatLng = Prefs.retrieveDestinationLocationFromPreference(); //new LatLng(34.0522, -118.2437); // Example: Los Angeles

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
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Update interval in milliseconds (e.g., 10 seconds)
        locationRequest.setFastestInterval(5000); // Fastest update interval (e.g., 5 seconds)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // High accuracy
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
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));

        // Draw a route to the destination
        drawRoute(currentLatLng, destinationLatLng);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationTextView.setText(String.format("Coordinates: %s, %s", location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private void drawRoute(LatLng start, LatLng end) {
        // Clear any existing polylines
        mMap.clear();

        // Add a marker for the destination
        mMap.addMarker(new MarkerOptions().position(end).title("Destination"));

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
        Location destinationLocation = new Location("");
        destinationLocation.setLatitude(destinationLatLng.latitude);
        destinationLocation.setLongitude(destinationLatLng.longitude);

        //float bearing = currentLocation.bearingTo(destinationLocation);
        float bearing = destinationLocation.bearingTo(currentLocation);
        arrowView.setBearing(bearing);
    }
}