package org.lsm.bluetoothmesh.btxfr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

class Utils {
    private final static String TAG = "===>Utils";

    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static int byteArrayToInt(byte[] b) {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }

    public static boolean digestMatch(byte[] imageData, byte[] digestData) {
        return Arrays.equals(getDigest(imageData), digestData);
    }

    public static byte[] getDigest(byte[] imageData) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return messageDigest.digest(imageData);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            throw new UnsupportedOperationException("MD5 algorithm not available on this device.");
        }
    }
    public static boolean isConnected(BluetoothDevice device){
        boolean canConnect=false;
        BluetoothSocket tempSocket;
        try {
            Log.d(TAG, "Trying to connect ");
            tempSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID_STRING));
            tempSocket.connect();
            Log.d(TAG, "Successfully connected");
            tempSocket.close();
            return true;
        } catch (IOException e) {
            return false;

        }
    }
}
