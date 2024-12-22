package com.alp2app.smsspamdetection;

import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private Switch autoBlockSwitch;
    private Switch notificationSwitch;
    private Switch modelUpdateSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("SpamDetectionPrefs", MODE_PRIVATE);
        initializeViews();
        loadSettings();
        setupListeners();
    }

    private void initializeViews() {
        autoBlockSwitch = findViewById(R.id.autoBlockSwitch);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        modelUpdateSwitch = findViewById(R.id.modelUpdateSwitch);
    }

    private void loadSettings() {
        autoBlockSwitch.setChecked(prefs.getBoolean("auto_block", false));
        notificationSwitch.setChecked(prefs.getBoolean("show_notifications", true));
        modelUpdateSwitch.setChecked(prefs.getBoolean("auto_update_model", true));
    }

    private void setupListeners() {
        autoBlockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_block", isChecked).apply();
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("show_notifications", isChecked).apply();
        });

        modelUpdateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_update_model", isChecked).apply();
        });
    }
} 