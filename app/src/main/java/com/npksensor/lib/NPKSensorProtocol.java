package com.npksensor.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol handler for NPK soil sensor communication.
 * Handles building commands and parsing responses.
 * 
 * Protocol Structure:
 * - All packets are 64 bytes
 * - Byte 0: Header (0x55 for commands, 0xAA for responses)
 * - Byte 1: Data length
 * - Byte 2: Command code
 * - Bytes 3-61: Data payload
 * - Bytes 62-63: CRC16 checksum
 */
public class NPKSensorProtocol {

    /** Packet size for USB communication */
    public static final int PACKET_SIZE = 64;
    
    /** Command header byte */
    public static final byte CMD_HEADER = 0x55;
    
    /** Response header byte */
    public static final byte RESP_HEADER = (byte) 0xAA;
    
    /** Command: Get device info */
    public static final byte CMD_DEVICE_INFO = 0x00;
    
    /** Command: Get real-time data */
    public static final byte CMD_REALTIME_DATA = 0x22;
    
    /** Command: Read parameters */
    public static final byte CMD_READ_PARAMS = 0x20;
    
    /** Command: Write parameters */
    public static final byte CMD_WRITE_PARAMS = 0x21;

    /**
     * Build command to request device information.
     * @return 64-byte command array
     */
    public static byte[] buildDeviceInfoCommand() {
        byte[] cmd = new byte[PACKET_SIZE];
        cmd[0] = CMD_HEADER;  // Header
        cmd[1] = 1;           // Length
        cmd[2] = CMD_DEVICE_INFO;  // Command
        return addChecksum(cmd);
    }

    /**
     * Build command to request real-time sensor data.
     * @return 64-byte command array
     */
    public static byte[] buildReadDataCommand() {
        byte[] cmd = new byte[PACKET_SIZE];
        cmd[0] = CMD_HEADER;  // Header
        cmd[1] = 1;           // Length
        cmd[2] = CMD_REALTIME_DATA;  // Command: 0x22 (34 decimal)
        return addChecksum(cmd);
    }

    /**
     * Add CRC16 checksum to the last 2 bytes of a command.
     */
    private static byte[] addChecksum(byte[] cmd) {
        short crc = CRC16.getCRC(cmd, PACKET_SIZE - 2);
        cmd[PACKET_SIZE - 2] = CRC16.getLow(crc);
        cmd[PACKET_SIZE - 1] = CRC16.getHigh(crc);
        return cmd;
    }

    /**
     * Parse device info from response bytes.
     * @param response 64-byte response array
     * @return DeviceInfo object or null if invalid
     */
    public static DeviceInfo parseDeviceInfo(byte[] response) {
        if (response == null || response.length < PACKET_SIZE) {
            return null;
        }
        if (response[0] != RESP_HEADER) {
            return null;
        }
        
        DeviceInfo info = new DeviceInfo();
        info.setDeviceType(CodeUtils.getShortBigU(response, 2));
        info.setVersion("V" + Integer.toHexString(response[5]) + "." + Integer.toHexString(response[6]));
        info.setDateTime((CodeUtils.getLongBig(response, 7) - 28800) * 1000);
        info.setElementBit(response[11]);
        return info;
    }

    /**
     * Parse real-time sensor data from response bytes.
     * @param response 64-byte response array
     * @return SoilData object with all sensor values
     */
    public static SoilData parseRealTimeData(byte[] response) {
        if (response == null || response.length < PACKET_SIZE) {
            return null;
        }
        
        // Validate response header and command
        if (response[0] != RESP_HEADER || response[2] != CMD_REALTIME_DATA) {
            return null;
        }
        
        List<RealTimeValue> values = parseRealTimeValues(response);
        return convertToSoilData(values);
    }

    /**
     * Parse individual values from response data.
     */
    private static List<RealTimeValue> parseRealTimeValues(byte[] response) {
        List<RealTimeValue> values = new ArrayList<>();
        
        int paramCount = response[5];  // Number of parameters in response
        int offset = 6;
        
        for (int i = 0; i < paramCount && offset < PACKET_SIZE - 5; i++) {
            try {
                // Read parameter ID (2 bytes, big-endian)
                int paramId = ((response[offset] & 0xFF) << 8) | (response[offset + 1] & 0xFF);
                
                // Read data type (2 bytes, big-endian)
                int dataType = ((response[offset + 2] & 0xFF) << 8) | (response[offset + 3] & 0xFF);
                
                // Read data length
                int dataLen = response[offset + 4] & 0xFF;
                
                // Extract value based on data type
                String value = extractValueString(response, offset + 5, dataLen, dataType);
                
                values.add(new RealTimeValue(paramId, dataType, value));
                
                offset += 5 + dataLen;
            } catch (Exception e) {
                break;
            }
        }
        
        return values;
    }

    /**
     * Extract value as string based on data type.
     */
    private static String extractValueString(byte[] data, int offset, int length, int dataType) {
        if (offset + length > data.length) {
            return "0";
        }
        
        switch (dataType) {
            case 0:  // INT8S - signed byte
                return String.valueOf(data[offset]);
                
            case 1:  // INT8U - unsigned byte
                int byteVal = data[offset] & 0xFF;
                if (data[offset] < 0) {
                    byteVal = (data[offset] & 0xFF);
                }
                return String.valueOf(byteVal);
                
            case 2:  // INT16S - signed short
                return String.valueOf(CodeUtils.getShortBig(data, offset));
                
            case 3:  // INT16U - unsigned short
                return String.valueOf(CodeUtils.getShortBigU(data, offset));
                
            case 4:  // INT32S - signed int
                return String.valueOf(CodeUtils.getIntBig(data, offset));
                
            case 5:  // INT32U - unsigned int
                return String.valueOf(CodeUtils.getIntBigU(data, offset));
                
            case 6:  // FLOAT
                return String.valueOf(CodeUtils.getFloatBig(data, offset));
                
            case 7:  // DOUBLE
                return String.valueOf(CodeUtils.getDoubleBig(data, offset));
                
            default:
                // Default: read as unsigned short
                if (length >= 2) {
                    return String.valueOf(CodeUtils.getShortBigU(data, offset));
                }
                return String.valueOf(data[offset] & 0xFF);
        }
    }

    /**
     * Convert parsed values to SoilData object.
     */
    private static SoilData convertToSoilData(List<RealTimeValue> values) {
        SoilData data = new SoilData();
        
        for (RealTimeValue value : values) {
            try {
                double numValue = Double.parseDouble(value.getValue());
                
                // Map data type to soil parameter
                // Type values: 0=Temp, 1=Humidity, 2=Conductivity, 3=pH, 4=N, 5=P, 6=K
                switch (value.getType()) {
                    case 0:
                        data.temperature = numValue / 10.0;  // Divide by 10 for actual value
                        break;
                    case 1:
                        data.humidity = numValue / 10.0;  // Divide by 10 for actual value
                        break;
                    case 2:
                        data.conductivity = numValue;  // Direct value in µS/cm
                        break;
                    case 3:
                        data.ph = numValue / 10.0;  // Divide by 10 for actual value
                        break;
                    case 4:
                        data.nitrogen = numValue;  // Direct value in mg/kg
                        break;
                    case 5:
                        data.phosphorus = numValue;  // Direct value in mg/kg
                        break;
                    case 6:
                        data.potassium = numValue;  // Direct value in mg/kg
                        break;
                }
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
        
        return data;
    }

    /**
     * Verify CRC checksum of received data.
     * @param data Response data
     * @return true if checksum is valid
     */
    public static boolean verifyCRC(byte[] data) {
        if (data == null || data.length < PACKET_SIZE) {
            return false;
        }
        short calculatedCRC = CRC16.getCRC(data, PACKET_SIZE - 2);
        short receivedCRC = (short) ((data[PACKET_SIZE - 1] << 8) | (data[PACKET_SIZE - 2] & 0xFF));
        return calculatedCRC == receivedCRC;
    }
}
