package com.npksensor.lib;

/**
 * Represents a single real-time sensor value from the NPK sensor.
 * Used internally for parsing response data.
 */
public class RealTimeValue {
    
    /** Parameter ID from the sensor */
    private int id;
    
    /** Data type identifier */
    private int type;
    
    /** String representation of the value */
    private String value;

    public RealTimeValue(int id, int type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RealTimeValue{" +
                "id=" + id +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
