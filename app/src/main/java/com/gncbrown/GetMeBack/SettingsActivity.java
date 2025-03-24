package com.gncbrown.GetMeBack;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.Preferences;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private static Context context;
    private static Preferences prefs;

    private SeekBar gpsRefreshRateMillisSeekBar;
    private TextView gpsRefreshRateMillisTextView;
    private SeekBar minUpdateDistanceSeekBar;
    private TextView minUpdateDistanceTextView;
    private SeekBar minUpdateIntervalSeekBar;
    private TextView minUpdateIntervalTextView;
    private SeekBar maxUpdateDelaySeekBar;
    private TextView maxUpdateDelayTextView;
    private TextView killAfterTextView;
    private SeekBar killAfterSeekBar;
    private CheckBox buildingsCheckBox;
    private CheckBox trafficCheckBox;
    private CheckBox indoorModeCheckBox;
    private CheckBox debugModeCheckBox;
    private CheckBox toneOnLocationUpdateCheckBox;

    private int numberOfIncrements = (10000 - 100) / 100;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

        prefs = new Preferences(context);

        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            EdgeToEdge.enable(this);
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topView), (v, insets) -> {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(0, topInset+50, 0, 0);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        gpsRefreshRateMillisTextView = findViewById(R.id.gpsRefreshRateMillisTextView);
        int currentGpsRefreshRateMillis = (int)(prefs.retrieveFromPreferences("GPSRefreshRateMillis"));
        updateSeekbarTextView(gpsRefreshRateMillisTextView, "GPS update interval: ", currentGpsRefreshRateMillis, "ms");
        gpsRefreshRateMillisSeekBar = findViewById(R.id.gpsRefreshRateMillisSeekBar);
        gpsRefreshRateMillisSeekBar.setProgress((int)(currentGpsRefreshRateMillis/100));
        gpsRefreshRateMillisSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateSeekbarTextView(gpsRefreshRateMillisTextView, "GPS update interval: ", milliseconds, "ms");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int milliseconds = 100 + (seekBar.getProgress() * 100);
                prefs.saveToPreferences("GPSRefreshRateMillis", milliseconds);
            }
        });
        gpsRefreshRateMillisSeekBar.setMax(numberOfIncrements);
        ImageButton gpsRefreshRateMillisImageButton = (ImageButton)findViewById(R.id.gpsRefreshRateMillisImageButton);
        gpsRefreshRateMillisImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.gpsRefreshRateMillisHint));
        });

        minUpdateIntervalTextView = findViewById(R.id.minUpdateIntervalTextView);
        minUpdateIntervalSeekBar = findViewById(R.id.minUpdateIntervalSeekBar);
        int minUpdateInterval = (int) prefs.retrieveFromPreferences("MinUpdateIntervalMillis");
        minUpdateIntervalSeekBar.setProgress((int)(minUpdateInterval/100));
        updateSeekbarTextView(minUpdateIntervalTextView, "Min update interval: ", minUpdateInterval, "ms");
        minUpdateIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateSeekbarTextView(minUpdateIntervalTextView, "Min update interval: ", milliseconds, "ms");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int milliseconds = 100 + (seekBar.getProgress() * 100);
                prefs.saveToPreferences("MinUpdateIntervalMillis", milliseconds);
            }
        });
        minUpdateIntervalSeekBar.setMax(numberOfIncrements);
        ImageButton minUpdateIntervalImageButton = (ImageButton)findViewById(R.id.minUpdateIntervalImageButton);
        minUpdateIntervalImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.minUpdateIntervalHint));
        });

        minUpdateDistanceTextView = findViewById(R.id.minUpdateDistanceTextView);
        minUpdateDistanceSeekBar = findViewById(R.id.minUpdateDistanceSeekBar);
        int minUpdateDistance = (int) prefs.retrieveFromPreferences("MinUpdateDistanceMeters");
        minUpdateDistanceSeekBar.setProgress((int)(minUpdateDistance));
        updateSeekbarTextView(minUpdateDistanceTextView, "Min update distance: ", minUpdateDistance, "m");
        minUpdateDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeekbarTextView(minUpdateDistanceTextView, "Min update distance: ", progress, "m");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress(); // Convert back to milliseconds
                prefs.saveToPreferences("MinUpdateDistanceMeters", value);
            }
        });
        minUpdateDistanceSeekBar.setMax(50);
        ImageButton minUpdateDistanceImageButton = (ImageButton)findViewById(R.id.minUpdateDistanceImageButton);
        minUpdateDistanceImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.minUpdateDistanceHint));
        });

        maxUpdateDelayTextView = findViewById(R.id.maxUpdateDelayTextView);
        maxUpdateDelaySeekBar = findViewById(R.id.maxUpdateDelaySeekBar);
        int maxUpdateDelay = (int) prefs.retrieveFromPreferences("MaxUpdateDelayMillis");
        maxUpdateDelaySeekBar.setProgress((int)(maxUpdateDelay/100));
        updateSeekbarTextView(maxUpdateDelayTextView, "Min update delay: ", maxUpdateDelay, "ms");
        maxUpdateDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateSeekbarTextView(maxUpdateDelayTextView, "Min update delay: ", milliseconds, "ms");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int milliseconds = 100 + (seekBar.getProgress() * 100);
                prefs.saveToPreferences("MaxUpdateDelayMillis", milliseconds);
            }
        });
        maxUpdateDelaySeekBar.setMax(numberOfIncrements);
        ImageButton maxUpdateDelaylImageButton = (ImageButton)findViewById(R.id.maxUpdateDelayImageButton);
        maxUpdateDelaylImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.maxUpdateDelayHint));
        });

        killAfterTextView = findViewById(R.id.killAfterTextView);
        killAfterSeekBar = findViewById(R.id.killAfterSeekBar);
        int killAfterMinutes = (int) prefs.retrieveFromPreferences("KillAfterMinutes");
        killAfterSeekBar.setProgress((int)(killAfterMinutes));
        updateSeekbarTextView(killAfterTextView, "Kill precise navigation after: ", killAfterMinutes, "minutes");
        killAfterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeekbarTextView(killAfterTextView, "Kill precise navigation after: ", progress, "minutes");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress(); // Convert back to milliseconds
                prefs.saveToPreferences("KillAfterMinutes", value);
            }
        });
        killAfterSeekBar.setMax(60);
        ImageButton killAfterImageButton = (ImageButton)findViewById(R.id.killAfterImageButton);
        killAfterImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.killAfterHint));
        });


        buildingsCheckBox = findViewById(R.id.buildingsCheckBox);
        buildingsCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ShowBuildings"));
        buildingsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("ShowBuildings", isChecked);
        });
        ImageButton buildingsImageButton = (ImageButton)findViewById(R.id.buildingsImageButton);
        buildingsImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.showBuildingsHint));
        });

        trafficCheckBox = findViewById(R.id.trafficCheckBox);
        trafficCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ShowTraffic"));
        trafficCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("ShowTraffic", isChecked);
        });
        ImageButton trafficImageButton = (ImageButton)findViewById(R.id.trafficImageButton);
        trafficImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.showTrafficHint));
        });

        indoorModeCheckBox = findViewById(R.id.indoorModeCheckBox);
        indoorModeCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("IndoorMode"));
        indoorModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("IndoorMode", isChecked);
        });
        ImageButton indoorModeImageButton = (ImageButton)findViewById(R.id.indoorModeImageButton);
        indoorModeImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.indoorModeHint));
        });


        debugModeCheckBox = findViewById(R.id.debugModeCheckBox);
        debugModeCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("DebugMode"));
        debugModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("DebugMode", isChecked);
        });

        toneOnLocationUpdateCheckBox = findViewById(R.id.toneOnLocationUpdateCheckBox);
        toneOnLocationUpdateCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ToneOnLocationUpdate"));
        toneOnLocationUpdateCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("ToneOnLocationUpdate", isChecked);
        });
        ImageButton toneOnLocationUpdateImageButton = (ImageButton)findViewById(R.id.toneOnLocationUpdateImageButton);
        toneOnLocationUpdateImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.toneOnLocationUpdateHint));
        });

        Button buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(v -> {
            prefs.resetPreferences();

            buildingsCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ShowBuildings"));
            trafficCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ShowTraffic"));
            indoorModeCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("IndoorMode"));
            debugModeCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("DebugMode"));
            toneOnLocationUpdateCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ToneOnLocationUpdate"));

            int seekbarMillis = (int)(prefs.retrieveFromPreferences("GPSRefreshRateMillis"));
            gpsRefreshRateMillisSeekBar.setProgress((int)(seekbarMillis/100));
            seekbarMillis = (int)(prefs.retrieveFromPreferences("MinUpdateIntervalMillis"));
            minUpdateIntervalSeekBar.setProgress((int)(seekbarMillis/100));
            seekbarMillis = (int)(prefs.retrieveFromPreferences("MaxUpdateDelayMillis"));
            maxUpdateDelaySeekBar.setProgress((int)(seekbarMillis/100));

            int seekbarMeters = (int)(prefs.retrieveFromPreferences("MinUpdateDistanceMeters"));
            minUpdateDistanceSeekBar.setProgress((int)(seekbarMeters));

            int seekbarMinutes = (int)(prefs.retrieveFromPreferences("KillAfterMinutes"));
            killAfterSeekBar.setProgress((int)(seekbarMinutes));

            Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSeekbarTextView(TextView textView, String textPrefix, int value, String units) {
        textView.setText(textPrefix + value + units);
    }

    private void showPopup(View clickedView, String hint) {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_hint, null);
        TextView popupTextView = popupView.findViewById(R.id.popupTextView);
        popupTextView.setText(hint);

        // Create the PopupWindow
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // Let taps outside the popup dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Set a background drawable for the popup window
        GradientDrawable border = new GradientDrawable();
        border.setStroke(10, Color.BLACK); // Set border width and color
        border.setCornerRadius(8); // Set corner radius

        // Show the popup window
        popupWindow.showAsDropDown(clickedView, 3, 3);
    }
}
