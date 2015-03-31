package org.lsm.bluetoothmesh.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.lsm.bluetoothmesh.btxfr.BluetoothMesh;
import org.lsm.bluetoothmesh.database.DBHelper;
import org.lsm.bluetoothmesh.database.DataPacket;

import java.util.ArrayList;

/**
 * Created by pralav on 3/31/15.
 */
public class MeshService extends Service {

    private boolean isRunning;
    private Context context;
    private DBHelper dbHandler;
    Thread backgroundThread;
    private static final String TAG = "=======>MeshService";
    private static final boolean D = true;
    //    private BluetoothServerService blueMesh;
    private BluetoothMesh blueMesh;
    ArrayList<BluetoothDevice> bluetoothdevice=new ArrayList<BluetoothDevice>();

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public class LocalBinder extends Binder {

        public MeshService getService() {

            return MeshService.this;

        }

    }
    private final LocalBinder mBinder = new LocalBinder();

    // Member fields
    private BluetoothAdapter mBtAdapter;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        dbHandler= new DBHelper(context);
        blueMesh =new BluetoothMesh(dbHandler);
//        blueMesh.setView();

//        if (blueMesh.getApplicationThreads().serverThread == null) {
//            Log.v(TAG, "Starting server thread.  Able to accept photos.");
//            blueMesh.getApplicationThreads().serverThread = new ServerThread(blueMesh.getApplicationThreads().adapter, blueMesh.getApplicationThreads().serverHandler);
//            blueMesh.getApplicationThreads().serverThread.start();
//        }
        bluetoothdevice=new ArrayList<BluetoothDevice>();
        this.backgroundThread = new Thread(myTask);


    }

    private Runnable myTask = new Runnable() {
        public void run() {
            BluetoothDevice dev=null;
            Log.d(TAG,"Running thread");

            if (blueMesh.getPairedDevices() != null) {
                ArrayList<DataPacket> dps=dbHandler.getAllDeviceFileInfo();
                Log.d(TAG,"Scheduling "+ dps.size()+" datapackets");
                if(dps!=null) {

                    Log.d(TAG, "Begin Processing");
                    blueMesh.scheduleDataPackets(dps);
//                    blueMesh.beginProcessing();
                }

            }


            stopSelf();
        }
    };


    @Override
    public void onDestroy() {
        this.isRunning = false;
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        Log.d("=====","Destroying====");

//        this.unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            Log.d("====Starting","startt");

            this.backgroundThread.start();
        }
        return START_STICKY;
    }


}