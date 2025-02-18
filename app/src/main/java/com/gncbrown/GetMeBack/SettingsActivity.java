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
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.Prefs;

public class SettingsActivity extends AppCompatActivity {

    private static Context mContext;

    private SeekBar intervalSeekBar;
    private TextView intervalTextView;
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
        mContext = this;

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

        intervalTextView = findViewById(R.id.intervalTextView);
        intervalTextView.setOnClickListener(v -> {
            showPopup((TextView) v, mContext.getResources().getString(R.string.intervalHint));
        });
        int currentInterval = (int)(Prefs.retrieveGPSRefreshRateMillisFromPreference());
        intervalSeekBar = findViewById(R.id.intervalSeekBar);
        intervalSeekBar.setProgress((int)(currentInterval/100));
        updateIntervalTextView(currentInterval);
        intervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int milliseconds = 100 + (progress * 100);
                updateIntervalTextView(milliseconds); // Convert back to milliseconds
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int milliseconds = 100 + (seekBar.getProgress() * 100);
                Prefs.saveGPSRefreshRateMillisToPreference(milliseconds);
            }
        });
        intervalSeekBar.setMax(numberOfIncrements);

        minUpdateIntervalTextView = findViewById(R.id.minUpdateIntervalTextView);
        minUpdateIntervalTextView.setOnClickListener(v -> {
            showPopup((TextView) v, mContext.getResources().getString(R.string.minUpdateIntervalHint));
        });
        minUpdateIntervalSeekBar = findViewById(R.id.minUpdateIntervalSeekBar);
        long minUpdateInterval = Prefs.retrieveMinUpdateIntervalMillisFromPreference();
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
                Prefs.saveMinUpdateIntervalMillisToPreference(milliseconds);
            }
        });
        minUpdateIntervalSeekBar.setMax(numberOfIncrements);

        minUpdateDistanceTextView = findViewById(R.id.minUpdateDistanceTextView);
        minUpdateDistanceTextView.setOnClickListener(v -> {
            showPopup((TextView) v, mContext.getResources().getString(R.string.minUpdateDistanceHint));
        });
        minUpdateDistanceSeekBar = findViewById(R.id.minUpdateDistanceSeekBar);
        float minUpdateDistance = Prefs.retrieveMinUpdateDistanceMetersFromPreference();
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
                Prefs.saveMinUpdateDistanceMetersToPreference(value);
            }
        });

        maxUpdateDelayTextView = findViewById(R.id.maxUpdateDelayTextView);
        maxUpdateDelayTextView.setOnClickListener(v -> {
            showPopup((TextView) v, mContext.getResources().getString(R.string.maxUpdateDelayHint));
        });
        maxUpdateDelaySeekBar = findViewById(R.id.maxUpdateDelaySeekBar);
        long maxUpdateDelay = Prefs.retrieveMaxUpdateDelayMillisFromPreference();
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
                Prefs.saveMaxUpdateDelayMillisToPreference(milliseconds);
            }
        });
        maxUpdateDelaySeekBar.setMax(numberOfIncrements);

        CheckBox buildingsCheckBox = findViewById(R.id.buildingsCheckBox);
        buildingsCheckBox.setChecked(Prefs.retrieveShowBuildingsFromPreference());
        buildingsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.saveShowBuildingsToPreference(isChecked);
        });
        CheckBox trafficCheckBox = findViewById(R.id.trafficCheckBox);
        trafficCheckBox.setChecked(Prefs.retrieveShowTrafficFromPreference());
        trafficCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.saveShowTrafficToPreference(isChecked);
        });
        CheckBox indoorModeCheckBox = findViewById(R.id.indoorModeCheckBox);
        indoorModeCheckBox.setChecked(Prefs.retrieveIndoorModeFromPreference());
        indoorModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.saveIndoorModeToPreference(isChecked);
        });

    }

    private void updateIntervalTextView(int value) {
        intervalTextView.setText("GPS update interval: " + (value) + "ms");
    }
    private void updateMinUpdateIntervalTextView(float value) {
        minUpdateIntervalTextView.setText("Min update interval: " + (int)value + "ms");
    }
    private void updateMaxUpdateDelayTextView(long value) {
        maxUpdateDelayTextView.setText("Max update delay: " + (value) + "ms");
    }
    private void updateMinUpdateDistanceTextView(float value) {
        minUpdateDistanceTextView.setText("Min update distance: " + (int)(value) + "m");
    }

    private void showPopup(TextView clickedView, String hint) {
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