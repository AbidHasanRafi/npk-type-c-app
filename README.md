# NPK Soil Sensor Library for Android

A clean, reusable Android library for communicating with NPK soil sensors via USB OTG.

## Features

- Reads 7 soil parameters from the NPK sensor:
  - **Temperature** (°C)
  - **Humidity** (%)
  - **Electrical Conductivity** (µS/cm)
  - **pH** value
  - **Nitrogen (N)** content (mg/kg)
  - **Phosphorus (P)** content (mg/kg)
  - **Potassium (K)** content (mg/kg)

- Automatic USB device detection and permission handling
- Continuous reading mode with configurable interval
- Single read mode
- Clean callback-based API

## Supported Device

- **Vendor ID**: 1159 (0x0487)
- **Product ID**: 7 (0x0007)

## Project Structure

```
NPKSensorLib/
??? app/
?   ??? src/main/
?   ?   ??? java/com/npksensor/
?   ?   ?   ??? lib/                    # Core library classes
?   ?   ?   ?   ??? CRC16.java          # CRC16 checksum calculator
?   ?   ?   ?   ??? CodeUtils.java      # Byte conversion utilities
?   ?   ?   ?   ??? DeviceInfo.java     # Device information model
?   ?   ?   ?   ??? NPKSensorManager.java   # Main manager class
?   ?   ?   ?   ??? NPKSensorProtocol.java  # Protocol handler
?   ?   ?   ?   ??? RealTimeValue.java  # Internal value model
?   ?   ?   ?   ??? SoilData.java       # Soil data model
?   ?   ?   ??? app/
?   ?   ?       ??? MainActivity.java   # Sample activity
?   ?   ??? res/
?   ?   ?   ??? layout/                 # UI layouts
?   ?   ?   ??? xml/device_filter.xml   # USB device filter
?   ?   ?   ??? values/                 # Resources
?   ?   ??? AndroidManifest.xml
?   ??? build.gradle
??? build.gradle
??? settings.gradle
??? README.md
```

## Quick Start

### 1. Add USB Host Feature to AndroidManifest.xml

```xml
<uses-feature
    android:name="android.hardware.usb.host"
    android:required="true" />
```

### 2. Create USB Device Filter (res/xml/device_filter.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <usb-device vendor-id="1159" product-id="7" />
</resources>
```

### 3. Use NPKSensorManager in Your Activity

```java
public class MainActivity extends AppCompatActivity implements NPKSensorManager.NPKSensorListener {

    private NPKSensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize sensor manager
        sensorManager = new NPKSensorManager(this);
        sensorManager.setListener(this);
        sensorManager.setReadInterval(2000); // 2 seconds
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.disconnect();
    }

    // Start continuous reading
    public void startReading() {
        sensorManager.startReading();
    }

    // Stop reading
    public void stopReading() {
        sensorManager.stopReading();
    }

    // Read once
    public SoilData readOnce() {
        return sensorManager.readOnce();
    }

    // ======== Listener callbacks ========

    @Override
    public void onConnected(DeviceInfo deviceInfo) {
        Log.d("NPK", "Sensor connected: " + deviceInfo.getVersion());
    }

    @Override
    public void onDisconnected() {
        Log.d("NPK", "Sensor disconnected");
    }

    @Override
    public void onDataReceived(SoilData data) {
        Log.d("NPK", "Temperature: " + data.temperature + "°C");
        Log.d("NPK", "Humidity: " + data.humidity + "%");
        Log.d("NPK", "Conductivity: " + data.conductivity + " µS/cm");
        Log.d("NPK", "pH: " + data.ph);
        Log.d("NPK", "Nitrogen: " + data.nitrogen + " mg/kg");
        Log.d("NPK", "Phosphorus: " + data.phosphorus + " mg/kg");
        Log.d("NPK", "Potassium: " + data.potassium + " mg/kg");
    }

    @Override
    public void onError(String message) {
        Log.e("NPK", "Error: " + message);
    }
}
```

## Communication Protocol

The sensor uses a custom binary protocol over USB HID:

### Packet Structure (64 bytes)

| Byte | Description |
|------|-------------|
| 0 | Header: 0x55 (command) or 0xAA (response) |
| 1 | Data length |
| 2 | Command code |
| 3-61 | Data payload |
| 62-63 | CRC16 checksum |

### Commands

| Command | Code | Description |
|---------|------|-------------|
| Get Device Info | 0x00 | Read device type, version, and available sensors |
| Get Real-Time Data | 0x22 | Read all sensor values |

### Data Types in Response

| Type | ID | Value Scale |
|------|-----|-------------|
| Temperature | 0 | Divide by 10 |
| Humidity | 1 | Divide by 10 |
| Conductivity | 2 | Direct value |
| pH | 3 | Divide by 10 |
| Nitrogen | 4 | Direct value |
| Phosphorus | 5 | Direct value |
| Potassium | 6 | Direct value |

## Building

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on a device with USB OTG support

## Requirements

- Android 5.0 (API 21) or higher
- Device with USB OTG support
- NPK soil sensor (Vendor ID: 1159, Product ID: 7)

## License

MIT License - Feel free to use and modify for your projects.
