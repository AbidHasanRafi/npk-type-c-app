package com.npksensor.lib;

/**
 * Utility class for byte array conversions and data parsing.
 * Handles big-endian and little-endian conversions for various data types.
 */
public class CodeUtils {

    /**
     * Convert an integer to a 4-byte big-endian array.
     */
    public static byte[] getBytesBig(int value) {
        byte[] result = new byte[4];
        result[3] = (byte) (value & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[0] = (byte) ((value >> 24) & 0xFF);
        return result;
    }

    /**
     * Convert a long to an 8-byte big-endian array.
     */
    public static byte[] getBytesBig(long value) {
        byte[] result = new byte[8];
        result[7] = (byte) (value & 0xFF);
        result[6] = (byte) ((value >> 8) & 0xFF);
        result[5] = (byte) ((value >> 16) & 0xFF);
        result[4] = (byte) ((value >> 24) & 0xFF);
        result[3] = (byte) ((value >> 32) & 0xFF);
        result[2] = (byte) ((value >> 40) & 0xFF);
        result[1] = (byte) ((value >> 48) & 0xFF);
        result[0] = (byte) ((value >> 56) & 0xFF);
        return result;
    }

    /**
     * Convert a short to a 2-byte big-endian array.
     */
    public static byte[] getBytesBig(short value) {
        byte[] result = new byte[2];
        result[1] = (byte) (value & 0xFF);
        result[0] = (byte) ((value >> 8) & 0xFF);
        return result;
    }

    /**
     * Convert an unsigned short (int) to a 2-byte big-endian array.
     */
    public static byte[] getBytesBigShortU(int value) {
        byte[] result = new byte[2];
        result[1] = (byte) (value & 0xFF);
        result[0] = (byte) ((value >> 8) & 0xFF);
        return result;
    }

    /**
     * Convert an unsigned int (long) to a 4-byte big-endian array.
     */
    public static byte[] getBytesIntBigU(long value) {
        byte[] result = new byte[4];
        result[3] = (byte) (value & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[0] = (byte) ((value >> 24) & 0xFF);
        return result;
    }

    /**
     * Convert a float to a 4-byte big-endian array.
     */
    public static byte[] getBytesBig(float value) {
        return getBytesBig(Float.floatToIntBits(value));
    }

    /**
     * Convert a double to an 8-byte big-endian array.
     */
    public static byte[] getBytesBig(double value) {
        return getBytesBig(Double.doubleToLongBits(value));
    }

    /**
     * Read a signed 32-bit integer from a big-endian byte array.
     */
    public static int getIntBig(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
               ((data[offset + 1] & 0xFF) << 16) |
               ((data[offset + 2] & 0xFF) << 8) |
               (data[offset + 3] & 0xFF);
    }

    /**
     * Read an unsigned 32-bit integer from a big-endian byte array.
     */
    public static long getIntBigU(byte[] data, int offset) {
        return (((long) (data[offset] & 0xFF)) << 24) |
               ((long) ((data[offset + 1] & 0xFF) << 16)) |
               ((long) ((data[offset + 2] & 0xFF) << 8)) |
               ((long) (data[offset + 3] & 0xFF));
    }

    /**
     * Read a float from a big-endian byte array.
     */
    public static float getFloatBig(byte[] data, int offset) {
        return Float.intBitsToFloat(getIntBig(data, offset));
    }

    /**
     * Read a signed 16-bit integer from a big-endian byte array.
     */
    public static short getShortBig(byte[] data, int offset) {
        return (short) ((data[offset + 1] & 0xFF) | (data[offset] << 8));
    }

    /**
     * Read an unsigned 16-bit integer from a big-endian byte array.
     */
    public static int getShortBigU(byte[] data, int offset) {
        return (data[offset + 1] & 0xFF) | ((data[offset] & 0xFF) << 8);
    }

    /**
     * Read a 64-bit long from a big-endian byte array.
     */
    public static long getLongBig(byte[] data, int offset) {
        return ((((long) data[offset]) & 0xFF) << 56) |
               ((((long) data[offset + 1]) & 0xFF) << 48) |
               ((((long) data[offset + 2]) & 0xFF) << 40) |
               ((((long) data[offset + 3]) & 0xFF) << 32) |
               ((((long) data[offset + 4]) & 0xFF) << 24) |
               ((((long) data[offset + 5]) & 0xFF) << 16) |
               ((((long) data[offset + 6]) & 0xFF) << 8) |
               (((long) data[offset + 7]) & 0xFF);
    }

    /**
     * Read a double from a big-endian byte array.
     */
    public static double getDoubleBig(byte[] data, int offset) {
        return Double.longBitsToDouble(getLongBig(data, offset));
    }

    /**
     * Convert a byte array to a hex string.
     */
    public static String getHexString(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Convert a byte array to a hex string with spaces.
     */
    public static String getHexStringWithSpaces(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                sb.append('0');
            }
            sb.append(hex).append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * Convert a hex string to a byte array.
     */
    public static byte[] getBytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        String upper = hexString.toUpperCase();
        int length = upper.length() / 2;
        char[] chars = upper.toCharArray();
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            result[i] = (byte) ((getByte(chars[pos]) << 4) | getByte(chars[pos + 1]));
        }
        return result;
    }

    /**
     * Convert a hex character to its byte value.
     */
    public static byte getByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
