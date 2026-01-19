package com.npksensor.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.npksensor.lib.DeviceInfo;
import com.npksensor.lib.NPKSensorManager;
import com.npksensor.lib.SoilData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Main Activity demonstrating how to use the NPKSensorManager library.
 * Shows real-time soil sensor data from the connected NPK sensor.
 */
public class MainActivity extends AppCompatActivity implements NPKSensorManager.NPKSensorListener {

    private NPKSensorManager sensorManager;
    private boolean isReading = false;

    // UI Elements
    private View statusIndicator;
    private TextView tvConnectionStatus;
    private TextView tvDeviceInfo;
    private TextView tvLastUpdate;
    private Button btnStartStop;
    private Button btnReadOnce;

    // Value displays
    private TextView tvTempValue, tvTempUnit;
    private TextView tvHumValue, tvHumUnit;
    private TextView tvCondValue, tvCondUnit;
    private TextView tvPhValue, tvPhUnit;
    private TextView tvNValue, tvNUnit;
    private TextView tvPValue, tvPUnit;
    private TextView tvKValue, tvKUnit;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();

        // Initialize sensor manager
        sensorManager = new NPKSensorManager(this);
        sensorManager.setListener(this);
        sensorManager.setReadInterval(2000); // Read every 2 seconds
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to sensor when activity starts
        sensorManager.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect when activity stops
        sensorManager.disconnect();
    }

    private void initViews() {
        statusIndicator = findViewById(R.id.statusIndicator);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnReadOnce = findViewById(R.id.btnReadOnce);

        // Initialize sensor value cards
        // Note: You'll need to find these views within the included layouts
        // For simplicity, using a helper method
        initSensorCards();
    }

    private void initSensorCards() {
        // Temperature
        View cardTemp = findViewById(R.id.cardTemperature);
        if (cardTemp != null) {
            ((TextView) cardTemp.findViewById(R.id.tvLabel)).setText(R.string.label_temperature);
            tvTempValue = cardTemp.findViewById(R.id.tvValue);
            tvTempUnit = cardTemp.findViewById(R.id.tvUnit);
            tvTempUnit.setText(R.string.unit_temperature);
        }

        // Humidity
        View cardHum = findViewById(R.id.cardHumidity);
        if (cardHum != null) {
            ((TextView) cardHum.findViewById(R.id.tvLabel)).setText(R.string.label_humidity);
            tvHumValue = cardHum.findViewById(R.id.tvValue);
            tvHumUnit = cardHum.findViewById(R.id.tvUnit);
            tvHumUnit.setText(R.string.unit_humidity);
        }

        // Conductivity
        View cardCond = findViewById(R.id.cardConductivity);
        if (cardCond != null) {
            ((TextView) cardCond.findViewById(R.id.tvLabel)).setText(R.string.label_conductivity);
            tvCondValue = cardCond.findViewById(R.id.tvValue);
            tvCondUnit = cardCond.findViewById(R.id.tvUnit);
            tvCondUnit.setText(R.string.unit_conductivity);
        }

        // pH
        View cardPh = findViewById(R.id.cardPH);
        if (cardPh != null) {
            ((TextView) cardPh.findViewById(R.id.tvLabel)).setText(R.string.label_ph);
            tvPhValue = cardPh.findViewById(R.id.tvValue);
            tvPhUnit = cardPh.findViewById(R.id.tvUnit);
            tvPhUnit.setText(R.string.unit_ph);
        }

        // Nitrogen
        View cardN = findViewById(R.id.cardNitrogen);
        if (cardN != null) {
            ((TextView) cardN.findViewById(R.id.tvLabel)).setText(R.string.label_nitrogen);
            tvNValue = cardN.findViewById(R.id.tvValue);
            tvNUnit = cardN.findViewById(R.id.tvUnit);
            tvNUnit.setText(R.string.unit_npk);
        }

        // Phosphorus
        View cardP = findViewById(R.id.cardPhosphorus);
        if (cardP != null) {
            ((TextView) cardP.findViewById(R.id.tvLabel)).setText(R.string.label_phosphorus);
            tvPValue = cardP.findViewById(R.id.tvValue);
            tvPUnit = cardP.findViewById(R.id.tvUnit);
            tvPUnit.setText(R.string.unit_npk);
        }

        // Potassium
        View cardK = findViewById(R.id.cardPotassium);
        if (cardK != null) {
            ((TextView) cardK.findViewById(R.id.tvLabel)).setText(R.string.label_potassium);
            tvKValue = cardK.findViewById(R.id.tvValue);
            tvKUnit = cardK.findViewById(R.id.tvUnit);
            tvKUnit.setText(R.string.unit_npk);
        }
    }

    private void setupClickListeners() {
        btnStartStop.setOnClickListener(v -> {
            if (isReading) {
                stopReading();
            } else {
                startReading();
            }
        });

        btnReadOnce.setOnClickListener(v -> {
            if (sensorManager.isConnected()) {
                SoilData data = sensorManager.readOnce();
                if (data != null) {
                    updateUI(data);
                }
            } else {
                Toast.makeText(this, R.string.msg_connect_sensor, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startReading() {
        if (sensorManager.isConnected()) {
            isReading = true;
            sensorManager.startReading();
            btnStartStop.setText(R.string.btn_stop);
            btnReadOnce.setEnabled(false);
        } else {
            Toast.makeText(this, R.string.msg_connect_sensor, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopReading() {
        isReading = false;
        sensorManager.stopReading();
        btnStartStop.setText(R.string.btn_start);
        btnReadOnce.setEnabled(true);
    }

    private void updateUI(SoilData data) {
        if (data == null) return;

        runOnUiThread(() -> {
            // Update all sensor values
            if (tvTempValue != null) tvTempValue.setText(String.format(Locale.US, "%.1f", data.temperature));
            if (tvHumValue != null) tvHumValue.setText(String.format(Locale.US, "%.1f", data.humidity));
            if (tvCondValue != null) tvCondValue.setText(String.format(Locale.US, "%.0f", data.conductivity));
            if (tvPhValue != null) tvPhValue.setText(String.format(Locale.US, "%.1f", data.ph));
            if (tvNValue != null) tvNValue.setText(String.format(Locale.US, "%.0f", data.nitrogen));
            if (tvPValue != null) tvPValue.setText(String.format(Locale.US, "%.0f", data.phosphorus));
            if (tvKValue != null) tvKValue.setText(String.format(Locale.US, "%.0f", data.potassium));

            // Update timestamp
            tvLastUpdate.setText("Last update: " + timeFormat.format(new Date()));
        });
    }

    private void setConnected(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                statusIndicator.setBackgroundResource(R.drawable.status_indicator_connected);
                tvConnectionStatus.setText(R.string.status_connected);
                btnStartStop.setEnabled(true);
                btnReadOnce.setEnabled(true);
            } else {
                statusIndicator.setBackgroundResource(R.drawable.status_indicator_disconnected);
                tvConnectionStatus.setText(R.string.status_disconnected);
                btnStartStop.setEnabled(false);
                btnReadOnce.setEnabled(false);
                stopReading();
            }
        });
    }

    // ==================== NPKSensorListener Implementation ====================

    @Override
    public void onConnected(DeviceInfo deviceInfo) {
        setConnected(true);
        runOnUiThread(() -> {
            if (deviceInfo != null) {
                tvDeviceInfo.setText("Device: " + deviceInfo.getVersion());
            } else {
                tvDeviceInfo.setText("Device: Connected");
            }
            Toast.makeText(this, "NPK Sensor Connected", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDisconnected() {
        setConnected(false);
        runOnUiThread(() -> {
            tvDeviceInfo.setText("Device: --");
            Toast.makeText(this, "NPK Sensor Disconnected", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDataReceived(SoilData data) {
        updateUI(data);
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
        });
    }
}
