package com.github.mdsina.corona.util;

public class HexUtil {

    private static final char[] CHARS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String byteArrayToHexString(byte[] in) {
        byte ch;

        if (in == null || in.length <= 0) {
            return null;
        }

        StringBuilder out = new StringBuilder(in.length * 2);

        for (byte b : in) {
            ch = (byte) (b & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4); // shift the bits down
            ch = (byte) (ch & 0x0F); // must do this is high order bit is on!

            out.append(CHARS[ch]); // convert the nibble to a String Character
            ch = (byte) (b & 0x0F); // Strip off low nibble
            out.append(CHARS[ch]); // convert the nibble to a String Character
        }

        return out.toString();
    }
}
