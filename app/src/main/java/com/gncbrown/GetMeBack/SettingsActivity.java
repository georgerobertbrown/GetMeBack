package com.gncbrown.GetMeBack;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.Preferences;

public class SettingsActivity extends AppCompatActivity {

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

    private int numberOfIncrements = (10000 - 100) / 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        prefs = new Preferences(context);

        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            EdgeToEdge.enable(this);
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topView), (v, insets) -> {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(0, topInset+200, 0, 0);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        gpsRefreshRateMillisTextView = findViewById(R.id.gpsRefreshRateMillisTextView);
        int currentGpsRefreshRateMillis = (int)(prefs.retrieveFromPreferences("GPSRefreshRateMillis"));
        gpsRefreshRateMillisSeekBar = findViewById(R.id.gpsRefreshRateMillisSeekBar);
        gpsRefreshRateMillisSeekBar.setProgress((int)(currentGpsRefreshRateMillis/100));
        updateGpsRefreshRateMillisTextView(currentGpsRefreshRateMillis);
        gpsRefreshRateMillisSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateGpsRefreshRateMillisTextView(milliseconds); // Convert back to milliseconds
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
        updateMinUpdateIntervalTextView(minUpdateInterval);
        minUpdateIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateMinUpdateIntervalTextView(milliseconds); // Convert back to milliseconds
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
        updateMinUpdateDistanceTextView(minUpdateDistance);
        minUpdateDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMinUpdateDistanceTextView(progress);
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
        updateMaxUpdateDelayTextView(maxUpdateDelay);
        maxUpdateDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateMaxUpdateDelayTextView(milliseconds); // Convert back to milliseconds
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

        CheckBox buildingsCheckBox = findViewById(R.id.buildingsCheckBox);
        buildingsCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ShowBuildings"));
        buildingsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("ShowBuildings", isChecked);
        });
        ImageButton buildingsImageButton = (ImageButton)findViewById(R.id.buildingsImageButton);
        buildingsImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.showBuildingsHint));
        });

        CheckBox trafficCheckBox = findViewById(R.id.trafficCheckBox);
        trafficCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("ShowTraffic"));
        trafficCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("ShowTraffic", isChecked);
        });
        ImageButton trafficImageButton = (ImageButton)findViewById(R.id.trafficImageButton);
        trafficImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.showTrafficHint));
        });

        CheckBox indoorModeCheckBox = findViewById(R.id.indoorModeCheckBox);
        indoorModeCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("IndoorMode"));
        indoorModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("IndoorMode", isChecked);
        });
        ImageButton indoorModeImageButton = (ImageButton)findViewById(R.id.indoorModeImageButton);
        indoorModeImageButton.setOnClickListener(v -> {
            showPopup(v, context.getResources().getString(R.string.indoorModeHint));
        });


        CheckBox debugModeCheckBox = findViewById(R.id.debugModeCheckBox);
        debugModeCheckBox.setChecked((boolean)prefs.retrieveFromPreferences("DebugMode"));
        debugModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.saveToPreferences("DebugMode", isChecked);
        });
    }

    private void updateGpsRefreshRateMillisTextView(int value) {
        gpsRefreshRateMillisTextView.setText("GPS update interval: " + value + "ms");
    }
    private void updateMinUpdateIntervalTextView(int value) {
        minUpdateIntervalTextView.setText("Min update interval: " + value + "ms");
    }
    private void updateMaxUpdateDelayTextView(int value) {
        maxUpdateDelayTextView.setText("Max update delay: " + value + "ms");
    }
    private void updateMinUpdateDistanceTextView(int value) {
        minUpdateDistanceTextView.setText("Min update distance: " + value + "m");
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