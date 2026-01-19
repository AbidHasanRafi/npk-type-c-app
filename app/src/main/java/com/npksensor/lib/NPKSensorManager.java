package com.npksensor.lib;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Main manager class for NPK soil sensor USB communication.
 * Handles USB connection, permission requests, and data reading.
 * 
 * Usage:
 * 1. Create instance with context
 * 2. Set listener for connection and data events
 * 3. Call connect() to start
 * 4. Call startReading() for continuous readings or readOnce() for single reading
 * 5. Call disconnect() when done
 */
public class NPKSensorManager {

    private static final String TAG = "NPKSensorManager";
    
    /** USB Vendor ID for the NPK sensor */
    public static final int VENDOR_ID = 1159;
    
    /** USB Product ID for the NPK sensor */
    public static final int PRODUCT_ID = 7;
    
    /** USB permission action */
    private static final String ACTION_USB_PERMISSION = "com.npksensor.USB_PERMISSION";
    
    /** Default read interval in milliseconds */
    private static final int DEFAULT_READ_INTERVAL = 2000;
    
    /** USB transfer timeout in milliseconds */
    private static final int USB_TIMEOUT = 1000;

    private final Context context;
    private final UsbManager usbManager;
    private final Handler handler;
    
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbInterface usbInterface;
    private UsbEndpoint endpointIn;
    private UsbEndpoint endpointOut;
    private PendingIntent permissionIntent;
    
    private NPKSensorListener listener;
    private boolean isConnected = false;
    private boolean isReading = false;
    private int readInterval = DEFAULT_READ_INTERVAL;
    
    private BroadcastReceiver usbReceiver;

    /**
     * Listener interface for sensor events.
     */
    public interface NPKSensorListener {
        /** Called when sensor is connected and ready */
        void onConnected(DeviceInfo deviceInfo);
        
        /** Called when sensor is disconnected */
        void onDisconnected();
        
        /** Called when new sensor data is available */
        void onDataReceived(SoilData data);
        
        /** Called when an error occurs */
        void onError(String message);
    }

    /**
     * Create a new NPKSensorManager.
     * @param context Application or activity context
     */
    public NPKSensorManager(Context context) {
        this.context = context.getApplicationContext();
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
        
        setupBroadcastReceiver();
    }

    /**
     * Set the listener for sensor events.
     */
    public void setListener(NPKSensorListener listener) {
        this.listener = listener;
    }

    /**
     * Set the interval between readings in milliseconds.
     * @param intervalMs Interval in milliseconds (minimum 500ms)
     */
    public void setReadInterval(int intervalMs) {
        this.readInterval = Math.max(500, intervalMs);
    }

    /**
     * Register broadcast receivers and attempt to connect.
     */
    public void connect() {
        registerReceivers();
        findAndConnectDevice();
    }

    /**
     * Disconnect from sensor and cleanup resources.
     */
    public void disconnect() {
        stopReading();
        
        if (connection != null) {
            if (usbInterface != null) {
                connection.releaseInterface(usbInterface);
            }
            connection.close();
            connection = null;
        }
        
        isConnected = false;
        device = null;
        usbInterface = null;
        endpointIn = null;
        endpointOut = null;
        
        try {
            context.unregisterReceiver(usbReceiver);
        } catch (Exception e) {
            // Receiver may not be registered
        }
        
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    /**
     * Check if sensor is currently connected.
     */
    public boolean isConnected() {
        return isConnected && connection != null;
    }

    /**
     * Start continuous reading of sensor data.
     */
    public void startReading() {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Sensor not connected");
            }
            return;
        }
        
        isReading = true;
        scheduleNextRead();
    }

    /**
     * Stop continuous reading.
     */
    public void stopReading() {
        isReading = false;
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Read sensor data once.
     * @return SoilData or null if error
     */
    public SoilData readOnce() {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Sensor not connected");
            }
            return null;
        }
        
        return performRead();
    }

    /**
     * Get device info from connected sensor.
     * @return DeviceInfo or null if error
     */
    public DeviceInfo getDeviceInfo() {
        if (!isConnected) {
            return null;
        }
        
        try {
            // Send device info command
            byte[] cmd = NPKSensorProtocol.buildDeviceInfoCommand();
            int sent = connection.bulkTransfer(endpointOut, cmd, NPKSensorProtocol.PACKET_SIZE, USB_TIMEOUT);
            
            if (sent < 0) {
                return null;
            }
            
            // Receive response
            byte[] response = new byte[NPKSensorProtocol.PACKET_SIZE];
            int received = connection.bulkTransfer(endpointIn, response, NPKSensorProtocol.PACKET_SIZE, USB_TIMEOUT);
            
            if (received < 0) {
                return null;
            }
            
            return NPKSensorProtocol.parseDeviceInfo(response);
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading device info", e);
            return null;
        }
    }

    // ==================== Private Methods ====================

    private void setupBroadcastReceiver() {
        usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    UsbDevice attachedDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (isTargetDevice(attachedDevice)) {
                        device = attachedDevice;
                        requestPermissionAndConnect();
                    }
                    
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice detachedDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null && device.equals(detachedDevice)) {
                        disconnect();
                    }
                    
                } else if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice permDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (permDevice != null) {
                                connectToDevice(permDevice);
                            }
                        } else {
                            if (listener != null) {
                                listener.onError("USB permission denied");
                            }
                        }
                    }
                }
            }
        };
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(usbReceiver, filter);
        }
        
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? 
                    PendingIntent.FLAG_MUTABLE : 0;
        permissionIntent = PendingIntent.getBroadcast(context, 0, 
                new Intent(ACTION_USB_PERMISSION), flags);
    }

    private void findAndConnectDevice() {
        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if (isTargetDevice(usbDevice)) {
                device = usbDevice;
                requestPermissionAndConnect();
                return;
            }
        }
        
        Log.d(TAG, "NPK sensor not found. Waiting for connection...");
    }

    private boolean isTargetDevice(UsbDevice device) {
        return device != null && 
               device.getVendorId() == VENDOR_ID && 
               device.getProductId() == PRODUCT_ID;
    }

    private void requestPermissionAndConnect() {
        if (device == null) return;
        
        if (usbManager.hasPermission(device)) {
            connectToDevice(device);
        } else {
            usbManager.requestPermission(device, permissionIntent);
        }
    }

    private void connectToDevice(UsbDevice usbDevice) {
        try {
            connection = usbManager.openDevice(usbDevice);
            if (connection == null) {
                if (listener != null) {
                    listener.onError("Failed to open USB device");
                }
                return;
            }
            
            // Find and claim interface
            usbInterface = usbDevice.getInterface(0);
            if (!connection.claimInterface(usbInterface, true)) {
                if (listener != null) {
                    listener.onError("Failed to claim USB interface");
                }
                connection.close();
                connection = null;
                return;
            }
            
            // Find endpoints (HID Interrupt type)
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                    if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                        endpointOut = endpoint;
                    } else if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                        endpointIn = endpoint;
                    }
                }
            }
            
            if (endpointIn == null || endpointOut == null) {
                if (listener != null) {
                    listener.onError("Could not find USB endpoints");
                }
                connection.close();
                connection = null;
                return;
            }
            
            isConnected = true;
            
            // Get and report device info
            DeviceInfo deviceInfo = getDeviceInfo();
            if (listener != null) {
                listener.onConnected(deviceInfo);
            }
            
            Log.d(TAG, "NPK sensor connected successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to device", e);
            if (listener != null) {
                listener.onError("Connection error: " + e.getMessage());
            }
        }
    }

    private void scheduleNextRead() {
        if (!isReading) return;
        
        handler.postDelayed(() -> {
            if (isReading && isConnected) {
                SoilData data = performRead();
                if (data != null && listener != null) {
                    listener.onDataReceived(data);
                }
                scheduleNextRead();
            }
        }, readInterval);
    }

    private SoilData performRead() {
        if (connection == null || endpointIn == null || endpointOut == null) {
            return null;
        }
        
        try {
            // Send read command
            byte[] cmd = NPKSensorProtocol.buildReadDataCommand();
            int sent = connection.bulkTransfer(endpointOut, cmd, NPKSensorProtocol.PACKET_SIZE, USB_TIMEOUT);
            
            if (sent < 0) {
                Log.e(TAG, "Failed to send read command");
                return null;
            }
            
            // Receive response
            byte[] response = new byte[NPKSensorProtocol.PACKET_SIZE];
            int received = connection.bulkTransfer(endpointIn, response, NPKSensorProtocol.PACKET_SIZE, USB_TIMEOUT);
            
            if (received < 0) {
                Log.e(TAG, "Failed to receive response");
                return null;
            }
            
            // Parse and return data
            return NPKSensorProtocol.parseRealTimeData(response);
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading sensor data", e);
            if (listener != null) {
                listener.onError("Read error: " + e.getMessage());
            }
            return null;
        }
    }
}
