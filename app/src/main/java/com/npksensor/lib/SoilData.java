package com.npksensor.lib;

/**
 * Data class representing soil sensor readings.
 * Contains all 7 parameters measured by the NPK soil sensor.
 */
public class SoilData {
    
    /** Soil temperature in degrees Celsius (°C) */
    public double temperature;
    
    /** Soil moisture/humidity in percentage (%) */
    public double humidity;
    
    /** Electrical conductivity in microsiemens per centimeter (µS/cm) */
    public double conductivity;
    
    /** pH value (0-14 scale) */
    public double ph;
    
    /** Nitrogen content in milligrams per kilogram (mg/kg) */
    public double nitrogen;
    
    /** Phosphorus content in milligrams per kilogram (mg/kg) */
    public double phosphorus;
    
    /** Potassium content in milligrams per kilogram (mg/kg) */
    public double potassium;
    
    /** Timestamp when data was read */
    public long timestamp;

    public SoilData() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SoilData{" +
                "temperature=" + temperature + "°C" +
                ", humidity=" + humidity + "%" +
                ", conductivity=" + conductivity + "µS/cm" +
                ", ph=" + ph +
                ", nitrogen=" + nitrogen + "mg/kg" +
                ", phosphorus=" + phosphorus + "mg/kg" +
                ", potassium=" + potassium + "mg/kg" +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Get a formatted string for display purposes.
     */
    public String toDisplayString() {
        return String.format(
            "Temperature: %.1f°C\n" +
            "Humidity: %.1f%%\n" +
            "Conductivity: %.0f µS/cm\n" +
            "pH: %.1f\n" +
            "Nitrogen (N): %.0f mg/kg\n" +
            "Phosphorus (P): %.0f mg/kg\n" +
            "Potassium (K): %.0f mg/kg",
            temperature, humidity, conductivity, ph, nitrogen, phosphorus, potassium
        );
    }

    /**
     * Check if all values are within normal ranges.
     */
    public boolean isValid() {
        return temperature >= -40 && temperature <= 80 &&
               humidity >= 0 && humidity <= 100 &&
               conductivity >= 0 && conductivity <= 20000 &&
               ph >= 0 && ph <= 14 &&
               nitrogen >= 0 && nitrogen <= 2000 &&
               phosphorus >= 0 && phosphorus <= 2000 &&
               potassium >= 0 && potassium <= 2000;
    }
}
