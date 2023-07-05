package com.gncbrown.GetMeBack;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.gncbrown.GetMeBack.Utilities.Prefs;
import com.google.android.gms.maps.model.LatLng;

public class GoToActivity extends AppCompatActivity {
    private static final String TAG = "GoToActivity";

    private String[] navigationMethods;

    private static Double destinationLatitude = 0.00;
    private static Double destinationLongitude = 0.00;
    private static String destinationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_go_to);

        navigationMethods = getResources().getStringArray(R.array.navigationMethods);

        LatLng initialLatLng = Prefs.retrieveDestinationFromPreference();
        destinationLatitude = initialLatLng.latitude;
        destinationLongitude = initialLatLng.longitude;
        destinationAddress = Prefs.retrieveDestinationAddressFromPreference();

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(GoToActivity.this);
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
                finish();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
}