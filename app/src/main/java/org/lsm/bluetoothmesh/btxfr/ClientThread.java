package org.lsm.bluetoothmesh.btxfr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.lsm.bluetoothmesh.service.MessageBox;
import org.lsm.bluetoothmesh.database.DBHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ClientThread extends Thread {
    private final String TAG = "=========ClientThread";
    private  BluetoothSocket socket;
    private  Handler handler;
    public Handler incomingHandler;
    private DBHelper dbHelper;
    private BluetoothDevice device;
    public ClientThread(BluetoothDevice device, Handler handler, DBHelper dBhelper) {
        BluetoothSocket tempSocket = null;
        this.handler = handler;
        this.dbHelper=dBhelper;
        this.device=device;

        try {
            tempSocket = createSocket(device, tempSocket);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        this.socket = tempSocket;
    }
    public ClientThread(BluetoothDevice device, DBHelper dBhelper) {
        BluetoothSocket tempSocket = null;
        this.device=device;
        this.dbHelper=dBhelper;

        tempSocket = getBluetoothSocket(device, tempSocket);
        this.socket = tempSocket;
    }

    private BluetoothSocket getBluetoothSocket(BluetoothDevice device, BluetoothSocket tempSocket) {
        try {
            tempSocket = createSocket(device, tempSocket);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return tempSocket;
    }

    private BluetoothSocket createSocket(BluetoothDevice device, BluetoothSocket tempSocket) throws IOException {
        tempSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID_STRING));
        return tempSocket;
    }

    public void setHandler(Handler handler){
        this.handler=handler;
    }

    public void run() {
        try {
            Log.v(TAG, "Opening client socket");
            socket.connect();
            Log.v(TAG, "Connection established");

        } catch (IOException ioe) {

            try{
                this.socket=createSocket(device,null);
                socket.connect();
            }catch (Exception e){
                handler.sendEmptyMessage(MessageType.COULD_NOT_CONNECT);
                Log.e(TAG, ioe.toString());
            }

            try {
                socket.close();
            } catch (IOException ce) {
                Log.e(TAG, "Socket close exception: " + ce.toString());
            }
        }

        Looper.prepare();

        incomingHandler = new Handler(){
            @Override
            public void handleMessage(Message message)
            {
                if (message.obj != null)
                {
                    MessageBox mbox = (MessageBox) message.obj;
                    Log.v(TAG, "Handle received data to send"+" "+mbox.getDp().getDeviceName());
                    byte[] payload = (byte[])mbox.getData();

                    try {
                        handler.sendEmptyMessage(MessageType.SENDING_DATA);
                        OutputStream outputStream = socket.getOutputStream();

                        // Send the header control first
                        Log.v(TAG, "Data sent.  Writing header");
                        outputStream.write(Constants.HEADER_MSB);
                        outputStream.write(Constants.HEADER_LSB);

                        // write size
                        Log.v(TAG, "Data sent.  Writing size");
                        outputStream.write(Utils.intToByteArray(payload.length));

                        // write digest
                        Log.v(TAG, "Data sent.  Writing digest");
                        byte[] digest = Utils.getDigest(payload);
                        outputStream.write(digest);

                        // now write the data
                        Log.v(TAG, "Data sent.  Writing payload");
                        outputStream.write(payload);
                        outputStream.flush();

                        Log.v(TAG, "Data sent.  Waiting for return digest as confirmation");
                        InputStream inputStream = socket.getInputStream();
                        byte[] incomingDigest = new byte[16];
                        int incomingIndex = 0;

                        try {
                            while (true) {
                                byte[] header = new byte[1];
                                inputStream.read(header, 0, 1);
                                incomingDigest[incomingIndex++] = header[0];
                                if (incomingIndex == 16) {
                                    if (Utils.digestMatch(payload, incomingDigest)) {
                                        Log.v(TAG, "Digest matched OK.  Data was received OK.");
                                        ClientThread.this.handler.sendEmptyMessage(MessageType.DATA_SENT_OK);
                                        dbHelper.deleteDeviceFileInfo(mbox.getDp());
                                    } else {
                                        Log.e(TAG, "Digest did not match.  Might want to resend.");
                                        ClientThread.this.handler.sendEmptyMessage(MessageType.DIGEST_DID_NOT_MATCH);
                                    }

                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }

                        Log.v(TAG, "Closing the client socket.");
                        socket.close();

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            }
        };

        handler.sendEmptyMessage(MessageType.READY_FOR_DATA);
        Looper.loop();
    }

    public void cancel() {
        try {
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}