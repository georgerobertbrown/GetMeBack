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
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.gncbrown.GetMeBack.Utilities.BackgroundTask;
import com.gncbrown.GetMeBack.Utilities.ButtonWidgetReceiver;
import com.gncbrown.GetMeBack.Utilities.NamedLocation;
import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.gncbrown.GetMeBack.Utilities.Utils;
import com.gncbrown.GetMeBack.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, TaskLoadedCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = "MainActivity";

    private static final int requestCode = 40;
    public static Context context;
    public static Preferences prefs;
    public static SharedPreferences sharedPreferences;

    private static ProgressBar progressBar;

    private static boolean alreadyRegistered = false;
    public static String[] requiredPermissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS};
    private static String[] navigationMethods;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    private Polyline currentPolyline;
    SupportMapFragment mapFragment;
    int mapTypeIndex = 0;
    List<Integer> mapTypes = Arrays.asList(
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_HYBRID);
    private float ZOOM = 15.0f; //8.0f; // 10
    private GoogleMap mGoogleMap;

    private enum LocationSource {Uninitialized, DestinationLocation, CurrentLocation}
    private LocationSource locationSource = LocationSource.Uninitialized;

    private String navigationMethod = "d";

    private static String destinationAddress = null;
    private static String homeAddress = "1650 Amphitheatre Pkwy, Mountain View, CA, 94043"; //"4 Frederick Drive, New Hartford, NY";
    public static LatLng home = new LatLng(37.4219983, -122.084); //new LatLng(43.05687, -75.25245);

    private static Double destinationLatitude = 0.00;
    private static Double destinationLongitude = 0.00;
    private Double currentLatitude = 0.00;
    private Double currentLongitude = 0.00;

    private long lastMarkerTime = System.currentTimeMillis();;

    public static String moreSubmenuContext = "";
    public static Menu optionsMenu;
    private static final String OPTION_INFO = "Info";
    private static final String OPTION_HELP = "Help";
    private static final String OPTION_VERSION = "Version";
    private static final String OPTION_WELCOME = "Welcome";
    private static final String OPTION_VALUES = "Values";
    private static final String OPTION_LOCATION = "Location";
    private static final String OPTION_SAVE_LOCATION_TO = "Save location to";
    private static final String OPTION_RESTORE_FROM = "Restore from";
    private static final String OPTION_FORGET_LOCATION = "Forget location";
    private static final String OPTION_RELEASE_HISTORY = "Release history";
    private static final String OPTION_NEW = "New...";
    private static final String OPTION_HOME = "Home";
    private static final String OPTION_SETTINGS = "Settings";

    private static Handler addressResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            destinationAddress = msg.getData().getString("address");
            prefs.saveToPreferences("DestinationAddress", destinationAddress);
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
                            prefs.saveToPreferences("DestinationLocation", newPoint);

                            locationSource = LocationSource.DestinationLocation;
                            destinationAddress = newDestinationAddress;
                            prefs.saveToPreferences("DestinationAddress", destinationAddress);
                            String markerLabel = getMarkerLabel();
                            animateMap(newPoint, markerLabel);
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
            goToDestination();
        }
    };

    private BroadcastReceiver preciseGoToDestinationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "preciseGoToDestinationReceiver.onReceive");
            preciseGoToDestination();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            EdgeToEdge.enable(this);
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topView), (v, insets) -> {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(0, topInset, 0, 0);
                return WindowInsetsCompat.CONSUMED;
            });
        }


        CoordinatorLayout mainLayout = findViewById(R.id.topView);

        context = this;
        prefs = new Preferences(context);

        navigationMethods = getResources().getStringArray(R.array.navigationMethods);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

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
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, ZOOM));
                        }
                        progress(false);
                    }
                });
            }
        });
        fabLayer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "Action: Map layers", Toast.LENGTH_SHORT).show();
                return true;
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
        fabMark.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "Action: Mark current location", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        FloatingActionButton fabGo = (FloatingActionButton) findViewById(R.id.fabGo);
        fabGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (destinationLatitude == 0.0 && destinationLongitude == 0.0) {
                    Utils.showAlertDialog(context,
                            "Error", "Destination location not set");
                } else {
                    Toast.makeText(getApplicationContext(), "Return to mark", Toast.LENGTH_SHORT).show();
                    goToDestination();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        fabGo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "Action: use turn-by-turn directions to mark", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        FloatingActionButton fabPreciseLocation = (FloatingActionButton) findViewById(R.id.fabPreciseLocation);
        fabPreciseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (destinationLatitude == 0.0 && destinationLongitude == 0.0) {
                    Utils.showAlertDialog(context,
                            "Error", "Destination location not set");
                } else {
                    final AlertDialog.Builder batteryDialog = new AlertDialog.Builder(MainActivity.this);
                    batteryDialog.setTitle("Return to marked location")
                            .setMessage("This uses precise location, which may drain the battery. Continue?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    preciseGoToDestination();
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                }
                            });
                    batteryDialog.show();
                }
            }
        });
        fabPreciseLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "Action: use precise location to mark", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        if ((boolean)prefs.retrieveFromPreferences("FirstTime")) {
            prefs.saveToPreferences("DestinationLocation", home);
            prefs.saveToPreferences("HomeLocation", home);
            prefs.saveToPreferences("HomeAddress", homeAddress);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            //Intent showHelp = new Intent(mContext, HelpActivity.class);
            Intent showHelp = new Intent(getApplicationContext(), HelpActivity.class);
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

            LatLng initialLatLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation");
            Log.d(TAG, "onCreate: initialLatLng=" + initialLatLng);

            destinationLatitude = initialLatLng.latitude;
            destinationLongitude = initialLatLng.longitude;
            destinationAddress = (String) prefs.retrieveFromPreferences("DestinationAddress");

            Utils.getAddressFromLocation(destinationLatitude, destinationLongitude, context,
                    addressResultHandler);
        }

        registerReceivers(true);

        String launchedFrom = getIntent().getStringExtra("ACTION");
        if (launchedFrom != null && launchedFrom.equals(ButtonWidgetReceiver.ACTION_ACTIVITY_UPDATE_FROM_WIDGET))
            requestLocationUpdate(true);

        prefs.saveToPreferences("FirstTime", false);
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
        registerReceivers(false);
        // TODO Should this be here?
        //prefs.saveToPreferences("DestinationLocation", new LatLng(destinationLatitude, destinationLongitude));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers(true);

        destinationAddress = (String) prefs.retrieveFromPreferences("DestinationAddress"); // null;
        homeAddress = (String) prefs.retrieveFromPreferences("HomeAddress");
        home = (LatLng) prefs.retrieveFromPreferences("HomeLocation");
        LatLng latLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation");
        Log.d(TAG, "onResume: initialLatLng=" + latLng);

        if (latLng.latitude == 0.0f && latLng.longitude == 0.0f) {
            latLng = home;
            destinationAddress = homeAddress;
        }
        destinationLatitude = latLng.latitude;
        destinationLongitude = latLng.longitude;

        if (mGoogleMap != null) {
            String markerLabel = getMarkerLabel();
            animateMap(latLng, markerLabel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerReceivers(false);

        // TODO should this be here?
        //LatLng latLng = new LatLng(destinationLatitude, destinationLongitude);
        //prefs.saveToPreferences("DestinationLocation", latLng);
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
        optionsMenu = menu;
        moreSubmenuContext = "";
        createOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        SubMenu sMenu = item.getSubMenu();
        String menuTitle = item.getTitle().toString();
        createOptionsMenu();

        if (menuTitle.equals(OPTION_HELP)) {
            moreSubmenuContext = "";
            Intent showHelp = new Intent(getApplicationContext(), HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "help");
            context.startActivity(showHelp);

        } else if (menuTitle.equals(OPTION_VERSION)) {
            moreSubmenuContext = "";
            Utils.showAlertDialog(context, "Version", Utils.getVersion());

        } else if (menuTitle.equals(OPTION_WELCOME)) {
            moreSubmenuContext = "";
            Intent showHelp = new Intent(getApplicationContext(), HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "welcome");
            context.startActivity(showHelp);

        } else if (menuTitle.equals(OPTION_RELEASE_HISTORY)) {
            moreSubmenuContext = "";
            Intent showHelp = new Intent(getApplicationContext(), HelpActivity.class);
            showHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            showHelp.putExtra("type", "changeLog");
            context.startActivity(showHelp);

        } else if (menuTitle.equals(OPTION_VALUES)) {
            moreSubmenuContext = "";
            LatLng destinationLatLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation");
            String destinationAddress = (String) prefs.retrieveFromPreferences("DestinationAddress");
            Double destinationAltitude = (Double) prefs.retrieveFromPreferences("DestinationAltitude");
            LatLng homeLatLng = (LatLng) prefs.retrieveFromPreferences("HomeLocation");
            String homeAddress = (String) prefs.retrieveFromPreferences("HomeAddress");
            String values = String.format("Lat/Lng(Alt): %s, %s (%sm)\nUpdate interval: %sms\nAddress: %s\nHome Lat/Lng: %s, %s\nHome: %s",
                    destinationLatLng.latitude, destinationLatLng.longitude, destinationAltitude,
                    prefs.retrieveFromPreferences("GPSRefreshRateMillis"),
                    destinationAddress,
                    homeLatLng.latitude, homeLatLng.longitude, homeAddress);
            Utils.showAlertDialog(context, "Values", values);

        } else if (menuTitle.equals(OPTION_LOCATION)) {
            moreSubmenuContext = OPTION_LOCATION;

        } else if (menuTitle.equals(OPTION_RESTORE_FROM)) {
            moreSubmenuContext = OPTION_RESTORE_FROM;

        } else if (menuTitle.equals(OPTION_SAVE_LOCATION_TO)) {
            moreSubmenuContext = OPTION_SAVE_LOCATION_TO;

        } else if (menuTitle.equals(OPTION_FORGET_LOCATION)) {
            moreSubmenuContext = OPTION_FORGET_LOCATION;

        } else if (menuTitle.equals(OPTION_NEW)) {
            moreSubmenuContext = "";
            setNamedLocation();
            createOptionsMenu();

        } else if (menuTitle.equals(OPTION_HOME) && moreSubmenuContext.contains("Save")) {
            moreSubmenuContext = "";
            setHomeDialog();

        } else if (menuTitle.equals(OPTION_HOME) && moreSubmenuContext.contains("Restore")) {
            moreSubmenuContext = "";
            restoreHome();

        } else if (moreSubmenuContext.equals(OPTION_RESTORE_FROM)) {
            moreSubmenuContext = "";
            restoreFrom(menuTitle);

        } else if (moreSubmenuContext.equals(OPTION_FORGET_LOCATION)) {
            if (!menuTitle.equals(OPTION_HOME))
                prefs.removeNamedLocationFromPreference(context, menuTitle);
            //Prefs.removeNamedLocationFromPreference(context, menuTitle); // TODO remove this
            createOptionsMenu();

        } else if (menuTitle.equals(OPTION_SETTINGS)) {
            moreSubmenuContext = "";
            Intent showSettings = new Intent(getApplicationContext(), SettingsActivity.class);
            showSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showSettings);
        }

        return super.onOptionsItemSelected(item);
    }

    private void createOptionsMenu() {
        Log.d(TAG, "createOptionsMenu");
        List<NamedLocation> namedLocations = prefs.retrieveNamedLocationsFromPreference(context);
        //String[] locations = Prefs.retrieveNamedLocations(context);
        if (optionsMenu != null) {
            optionsMenu.clear();

            Drawable helpIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_help);
            Drawable versionIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_info_details);
            Drawable welcomeIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_manage);
            Drawable valuesIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view);
            Drawable historyIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_recent_history);
            Drawable settingsIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_preferences);

            SubMenu infoMenu = optionsMenu.addSubMenu(1, Menu.FIRST, Menu.NONE, OPTION_INFO);
            infoMenu.clear();
            MenuItem settingsItem = infoMenu.add(OPTION_SETTINGS);
            settingsItem.setIcon(settingsIcon);
            settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM  | MenuItem.SHOW_AS_ACTION_WITH_TEXT); // always|withText
            settingsItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM  | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            MenuItem versionItem = infoMenu.add(OPTION_VERSION);
            versionItem.setIcon(versionIcon);
            MenuItem welcomeItem = infoMenu.add(OPTION_WELCOME);
            welcomeItem.setIcon(welcomeIcon);
            MenuItem helpItem = infoMenu.add(OPTION_HELP);
            helpItem.setIcon(helpIcon);
            MenuItem valuesItem = infoMenu.add(OPTION_VALUES);
            valuesItem.setIcon(valuesIcon);
            MenuItem historyItem = infoMenu.add(OPTION_RELEASE_HISTORY);
            historyItem.setIcon(historyIcon);

            // Location submenu
            SubMenu locationSubMenu = optionsMenu.addSubMenu(2, Menu.FIRST, Menu.NONE, OPTION_LOCATION);
            locationSubMenu.clear();

            // Restore submenu
            SubMenu restoreSubMenu = locationSubMenu.addSubMenu(3, Menu.FIRST, Menu.NONE, OPTION_RESTORE_FROM);
            restoreSubMenu.clear();
//            restoreSubMenu.add(OPTION_HOME);
            for (NamedLocation namedLocation : namedLocations) {
                restoreSubMenu.add(namedLocation.getName());
            }
//            for (String l : locations) {
//                restoreSubMenu.add(l);
//            }

            // Save submenu
            SubMenu saveSubMenu = locationSubMenu.addSubMenu(4, Menu.FIRST, Menu.NONE, OPTION_SAVE_LOCATION_TO);
            saveSubMenu.clear();
            saveSubMenu.add(OPTION_NEW);
//            saveSubMenu.add(OPTION_HOME);
            for (NamedLocation namedLocation : namedLocations) {
                saveSubMenu.add(namedLocation.getName());
            }
//            for (String l : locations) {
//                saveSubMenu.add(l);
//            }

            // Forget submenu
            if (!namedLocations.isEmpty()) {//locations.length > 0) {
                SubMenu forgetSubMenu = locationSubMenu.addSubMenu(2, Menu.FIRST, Menu.NONE, OPTION_FORGET_LOCATION);
                forgetSubMenu.clear();
                for (NamedLocation namedLocation : namedLocations) {
                    forgetSubMenu.add(namedLocation.getName());
                }
//                for (String l : locations) {
//                    forgetSubMenu.add(l);
//                }
            }
        }
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
        LatLng latLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation");
        Log.d(TAG, "onConnected: initialLatLng=" + latLng);

        if (latLng.latitude == 0.0f && latLng.longitude == 0.0f) {
            latLng = home;
        }

        if (mGoogleMap != null) {
            String markerLabel = getMarkerLabel();
            animateMap(latLng, markerLabel);
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
        destinationLatitude = location.getLatitude();
        destinationLongitude = location.getLongitude();
        String msg = "Updated location: " + destinationLatitude + "," + destinationLongitude;
        Log.d(TAG, "onLocationChanged: " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        progress(false);

        String markerLabel = "";
        if (locationSource == LocationSource.CurrentLocation) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            markerLabel = locationSource.toString(); //"Marker";
            animateMap(new LatLng(location.getLatitude(), location.getLongitude()), markerLabel);

            if (destinationLatitude != 0.0 && destinationLongitude != 0.0) {
                goToDestination();
            } else {
                Toast.makeText(this, "Current location not set.", Toast.LENGTH_SHORT).show();
            }
        } else if (locationSource == LocationSource.DestinationLocation) {
            destinationLatitude = location.getLatitude();
            destinationLongitude = location.getLongitude();
            destinationAddress = null;
            markerLabel = getMarkerLabel();

            LatLng latLng = new LatLng(destinationLatitude, destinationLongitude);
            prefs.saveToPreferences("DestinationLocation", latLng);

            animateMap(latLng, markerLabel);
        } else {
            animateMap(home, "Home");
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
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestMultiplePermissions();
                }
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context,
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
                locationSource = LocationSource.DestinationLocation;
                progress(false);
            }
        };
        bg.execute();
    }

    private void requestLocationUpdate(boolean start) {
        Log.d(TAG, "requestLocationUpdate");
        // Request location updates
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions();
        }
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
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
            mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(1); // Minimum movement required (in meters)
            LocationCallback mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null)
                        return;
                    Location location = locationResult.getLocations().get(0);
                    if (location != null) {
                        locationSource = LocationSource.CurrentLocation;

                        destinationLatitude = location.getLatitude();
                        destinationLongitude = location.getLongitude();

                        LatLng updatedLocation = new LatLng(destinationLatitude, destinationLongitude);
                        String locationString = String.format("%s, %s", destinationLatitude, destinationLongitude);
                        String msg = "Updated location: " + locationString;

                        prefs.saveToPreferences("DestinationLocation", new LatLng(destinationLatitude, destinationLongitude));
                        prefs.saveToPreferences("DestinationAltitude", location.getAltitude());
                        animateMap(updatedLocation, locationString);
                        toastMessage(msg);

                        Utils.getAddressFromLocation(destinationLatitude, destinationLongitude, context, addressResultHandler);
                    }
                }
            };
            LocationServices.getFusedLocationProviderClient(getApplicationContext()) //mContext)
                    .requestLocationUpdates(mLocationRequest,
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
                        Toast.makeText(getApplicationContext(), "DexterError: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void setNewDestinationAlert(LatLng point) {
        progress(true);
        Utils.getAddressFromLocation(point.latitude, point.longitude, context,
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
                home = Utils.getLocationFromAddress(getApplicationContext(), destinationAddress); //mContext);
                prefs.saveToPreferences("HomeAddress", destinationAddress);
                prefs.saveToPreferences("HomeLocation", home);
                prefs.saveToPreferences("DestinationLocation", home);

                destinationLatitude = home.latitude;
                destinationLongitude = home.longitude;
                locationSource = LocationSource.DestinationLocation;
                progress(false);
                animateMap(home, destinationAddress);
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
        Toast.makeText(this, "Restoring from Home", Toast.LENGTH_SHORT).show();
        progress(true);
        LatLng homeLocation = (LatLng) prefs.retrieveFromPreferences("HomeLocation");
        String homeAddress = (String) prefs.retrieveFromPreferences("HomeAddress");

        prefs.saveToPreferences("DestinationLocation", homeLocation);
        prefs.saveToPreferences("HomeAddress", homeAddress);
        animateMap(homeLocation, homeAddress);
        progress(false);
    }

    private void restoreFrom(String name) {
        Toast.makeText(this, "Restoring from " + name, Toast.LENGTH_SHORT).show();
        progress(true);
        prefs.retrieveNamedLocationsFromPreference(context);
        LatLng restoredLocation = prefs.retrieveNamedLocationFromPreference(context, name);
        //LatLng restoredLocation = Prefs.retrieveNamedLocation(context, name); // TODO remove this
        destinationLatitude = restoredLocation.latitude;
        destinationLongitude = restoredLocation.longitude;
        prefs.saveToPreferences("DestinationLocation", restoredLocation);
        Utils.getAddressFromLocation(destinationLatitude, destinationLongitude, context,
                addressResultHandler);
        animateMap(restoredLocation, "Address pending");
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

    public static String getMarkerLabel() {
        if (destinationAddress != null && !destinationAddress.equals("???"))
            return destinationAddress;

        // Start a handler to translate location to address
        Utils.getAddressFromLocation(destinationLatitude, destinationLongitude, context,
                addressResultHandler);

        // In the meantime, return something
        return String.format("%s, %s", destinationLatitude, destinationLongitude);
    }

    private void preciseGoToDestination() {
        Intent intent = new Intent(this, PreciseLocationActivity.class);
        startActivity(intent);
    }

    private void goToDestination() {
        String destination = (String) prefs.retrieveFromPreferences("DestinationAddress");
        LatLng destinationLatLng = (LatLng) prefs.retrieveFromPreferences("DestinationLocation");
        Log.d(TAG, "goToDestination: destinationLatLng=" + destinationLatLng);

        destinationLatitude = destinationLatLng.latitude;
        destinationLongitude = destinationLatLng.longitude;

        if (destinationLatitude == 0.0 && destinationLongitude == 0.0) {
            Utils.showAlertDialog(this, "Error", "Destination not set!");
        } else {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
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
                        Utils.showAlertDialog(context,
                                "Navigation Error", e.getMessage());
                    }

                    dialogInterface.dismiss();
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        }
    }

    public void toastMessage(String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerReceivers(boolean flag) {
        Log.d("TAG", String.format("registerReceiver[flag=%s, alreadyRegistered=%s] for %s+%s+%s", flag, alreadyRegistered,
                ButtonWidgetReceiver.ACTION_ACTIVITY_UPDATE_FROM_WIDGET,
                ButtonWidgetReceiver.ACTION_ACTIVITY_GO_TO_FROM_WIDGET,
                ButtonWidgetReceiver.ACTION_ACTIVITY_PRECISE_GO_TO_FROM_WIDGET));
        if (flag) {
            if (!alreadyRegistered) {
                try {
                    registerReceiver(updateDestinationReceiver, new IntentFilter(ButtonWidgetReceiver.ACTION_ACTIVITY_UPDATE_FROM_WIDGET),
                            Context.RECEIVER_EXPORTED);
                    registerReceiver(gotoDestinationReceiver, new IntentFilter(ButtonWidgetReceiver.ACTION_ACTIVITY_GO_TO_FROM_WIDGET),
                            Context.RECEIVER_EXPORTED);
                    registerReceiver(preciseGoToDestinationReceiver, new IntentFilter(ButtonWidgetReceiver.ACTION_ACTIVITY_PRECISE_GO_TO_FROM_WIDGET),
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
                    unregisterReceiver(preciseGoToDestinationReceiver);
                } catch (Exception e) {
                    Log.e(TAG, "Could not unregister receivers");
                }
            } else {
                Log.i(TAG, "Receivers already unregistered");
            }
            alreadyRegistered = false;
        }
    }

    private void animateMap(LatLng latLng, String markerLabel) {
        if (mGoogleMap != null) {
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(markerLabel)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        } else {
            toastMessage(String.format("Could not animate map to %s, %s (%s)", latLng.latitude, latLng.longitude, markerLabel));
        }
    }

    private void setNamedLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mText = input.getText().toString();
                //prefs.saveToPreferences("NamedLocation", new LatLng(destinationLatitude, destinationLongitude));// TODO remove this
                prefs.saveNamedLocationToPreferences(context, new NamedLocation(mText, new LatLng(destinationLatitude, destinationLongitude)));
                createOptionsMenu();
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

}