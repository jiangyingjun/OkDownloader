package com.jyj.okdownloader.utils;

import androidx.annotation.NonNull;

public class Util {
    private static final char[] HEX_CHAR_ARRAY = "0123456789abcdef".toCharArray();

    private static final char[] SHA_256_CHARS = new char[64];

    /**
     * Returns the hex string of the given byte array representing a SHA256 hash.
     */
    @NonNull
    public static String sha256BytesToHex(@NonNull byte[] bytes) {
        synchronized (SHA_256_CHARS) {
            return bytesToHex(bytes, SHA_256_CHARS);
        }
    }


    // Taken from:
    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    // /9655275#9655275
    @NonNull
    private static String bytesToHex(@NonNull byte[] bytes, @NonNull char[] hexChars) {
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHAR_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
