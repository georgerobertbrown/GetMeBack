package com.gncbrown.GetMeBack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.Logger;
import com.gncbrown.GetMeBack.Utilities.MySQLiteHelper;
import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.google.android.gms.maps.model.LatLng;

public class GoToActivity extends AppCompatActivity {
    private static final String TAG = "GoToActivity";

    private static Context context;
    private static Preferences prefs;
    private static MySQLiteHelper dbHelper;

    private String selectedNavigationMethod = "d";
    private String[] navigationMethods;

    private static Double destinationLatitude = 0.00;
    private static Double destinationLongitude = 0.00;
    private static String destinationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        prefs = new Preferences(context);
        dbHelper = MySQLiteHelper.getInstance(this);
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Info, "GoToActivity.onCreate");

        setContentView(R.layout.activity_go_to);
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

        selectedNavigationMethod = "";
        navigationMethods = getResources().getStringArray(R.array.navigationMethods);

        LatLng initialLatLng = (LatLng)prefs.retrieveFromPreferences("DestinationLocation");
        Log.d(TAG, "onCreate: initialLatLng=" + initialLatLng);

        destinationLatitude = initialLatLng.latitude;
        destinationLongitude = initialLatLng.longitude;
        destinationAddress = (String)prefs.retrieveFromPreferences("DestinationAddress");
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Debug, "GoToActivity.onCreate; destinationAddress=" + destinationAddress);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context); //GoToActivity.this);
        mBuilder.setTitle("Choose a navigation method to " + destinationAddress);
        mBuilder.setCancelable(true);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "navigationMethod.onClick: OK, which="+which);
                if (selectedNavigationMethod != null & !selectedNavigationMethod.isEmpty()) {
                    launchMaps(selectedNavigationMethod);
                    finish();
                } else if (which >= 0 && which < navigationMethods.length) {
                    selectedNavigationMethod = navigationMethods[which].toLowerCase().substring(0, 1);
                    launchMaps(selectedNavigationMethod);
                    finish();
                }
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "navigationMethod.onClick: Cancel");
                finish();
            }
        });
        mBuilder.setSingleChoiceItems(navigationMethods, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "navigationMethod.onClick: i=" + i);
                selectedNavigationMethod = navigationMethods[i].toLowerCase().substring(0,1);
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void launchMaps(String navigationMethod) {
        Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s&mode=%s&t=p", destinationLatitude, destinationLongitude,
                navigationMethod));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        String msg = String.format("GoToActivity.launchMaps: gmmIntentUri=%s", gmmIntentUri);
        Log.d(TAG, msg);
        dbHelper.appendLogTranscript(context, Logger.LogLevel.Debug, msg);
        startActivity(mapIntent);
    }

}