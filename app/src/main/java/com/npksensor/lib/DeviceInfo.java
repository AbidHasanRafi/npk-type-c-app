package com.npksensor.lib;

/**
 * Device information retrieved from the NPK soil sensor.
 */
public class DeviceInfo {
    
    /** Device type identifier */
    private int deviceType;
    
    /** Firmware version string (e.g., "V1.6") */
    private String version;
    
    /** Device date/time in milliseconds */
    private long dateTime;
    
    /** Element bit flags indicating which sensors are available */
    private byte elementBit;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public byte getElementBit() {
        return elementBit;
    }

    public void setElementBit(byte elementBit) {
        this.elementBit = elementBit;
    }

    /**
     * Check if temperature sensor is available.
     */
    public boolean hasTemperature() {
        return (elementBit & 0x01) != 0;
    }

    /**
     * Check if humidity sensor is available.
     */
    public boolean hasHumidity() {
        return (elementBit & 0x02) != 0;
    }

    /**
     * Check if conductivity sensor is available.
     */
    public boolean hasConductivity() {
        return (elementBit & 0x04) != 0;
    }

    /**
     * Check if pH sensor is available.
     */
    public boolean hasPH() {
        return (elementBit & 0x08) != 0;
    }

    /**
     * Check if nitrogen sensor is available.
     */
    public boolean hasNitrogen() {
        return (elementBit & 0x10) != 0;
    }

    /**
     * Check if phosphorus sensor is available.
     */
    public boolean hasPhosphorus() {
        return (elementBit & 0x20) != 0;
    }

    /**
     * Check if potassium sensor is available.
     */
    public boolean hasPotassium() {
        return (elementBit & 0x40) != 0;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceType=" + deviceType +
                ", version='" + version + '\'' +
                ", dateTime=" + dateTime +
                ", elementBit=" + elementBit +
                '}';
    }
}
