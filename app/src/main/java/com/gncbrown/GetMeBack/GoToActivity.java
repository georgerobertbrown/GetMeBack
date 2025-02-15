package com.gncbrown.GetMeBack;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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

        navigationMethods = getResources().getStringArray(R.array.navigationMethods);

        LatLng initialLatLng = Prefs.retrieveDestinationLocationFromPreference();
        LatLng filteredLatLng = Prefs.retrieveFilteredDestinationLocationFromPreference();
        Log.d(TAG, "onCreate: initialLatLng=" + initialLatLng + ", filteredLatLng=" + filteredLatLng);

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