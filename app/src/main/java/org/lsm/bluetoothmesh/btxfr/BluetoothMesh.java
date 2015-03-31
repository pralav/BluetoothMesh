package org.lsm.bluetoothmesh.btxfr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.lsm.bluetoothmesh.service.MessageBox;
import org.lsm.bluetoothmesh.database.DBHelper;
import org.lsm.bluetoothmesh.database.DataPacket;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by pralav on 3/30/15.
 */
public class BluetoothMesh {

    private ArrayList<ClientThread> clientThreads;
    private ArrayList<Handler> handlers;
    private static final String TAG = "====>BluetoothMesh";
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter adapter;
    private ServerThread serverThread;
    private HashMap<String, BluetoothDevice> deviceMap;
    private DBHelper dbHelper;
    String dirPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"lsm_dcim/received_files";

    public BluetoothMesh( DBHelper dbHelper) {
        deviceMap=new HashMap<String, BluetoothDevice>();
        handlers=new ArrayList<Handler>();
        clientThreads=new ArrayList<ClientThread>();
        adapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = adapter.getBondedDevices();
        Log.d(TAG,pairedDevices+"");
        this.dbHelper=dbHelper;
        if(pairedDevices!=null) {
            for (BluetoothDevice device : pairedDevices) {
                deviceMap.put(device.getAddress(), device);
            }
        }
        createServerHandlers();



    }

    public Set<BluetoothDevice> getPairedDevices() {
        return pairedDevices;
    }

    public void setPairedDevices(Set<BluetoothDevice> pairedDevices) {
        this.pairedDevices = pairedDevices;
    }

    public void scheduleDataPackets(ArrayList<DataPacket> dataPackets){

        HashMap<String, ArrayList<DataPacket>> dataMap = new HashMap<String, ArrayList<DataPacket>>();
        if(pairedDevices!=null) {


            for (DataPacket dp : dataPackets) {
                ArrayList<DataPacket> files = new ArrayList<DataPacket>();
                if (dataMap.containsKey(dp.getDeviceAddress()))
                    files = dataMap.get(dp.getDeviceAddress());
                files.add(dp);
                dataMap.put(dp.getDeviceAddress(), files);
            }
            ValueComparator bvc = new ValueComparator(dataMap);
            Log.d(TAG, "datamap Size: "+dataMap.size());
            TreeMap<String, ArrayList<DataPacket>> sorted_map = new TreeMap<String, ArrayList<DataPacket>>(bvc);
            sorted_map.putAll(dataMap);
//        for(Map.Entry<String,ArrayList<String>> entry : sorted_map.entrySet()) {
//            ArrayList<String> files=entry.getValue();
//        }
//        createHandlers();
            scheduleTasks(sorted_map, dataMap , true);
        }

    }

    class ValueComparator implements Comparator<String> {

        Map<String, ArrayList<DataPacket>> base;

        public ValueComparator(Map<String, ArrayList<DataPacket>> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String a, String b) {
            if (base.get(a).size() >= base.get(b).size()) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    public void scheduleTasks(TreeMap<String, ArrayList<DataPacket>> dataTasks,Map<String, ArrayList<DataPacket>> origDataTasks, boolean SJF) {
        ArrayList<String> devices = new ArrayList<String>();
        Log.d(TAG, "Size: "+dataTasks.size());

        if (SJF) {
            for (Map.Entry<String, ArrayList<DataPacket>> entry : dataTasks.entrySet()) {
                Log.d(TAG, entry.getKey());
                devices.add(entry.getKey());
            }

        } else {
            for (String key : dataTasks.descendingKeySet()) {
                Log.d(TAG, key);
                devices.add(key);
            }
        }
        try {

            for (String address : devices) {
                BluetoothDevice device=deviceMap.get(address);
                Log.d(TAG, "Trying to connect to device....:"+device.getName());
                if (Utils.isConnected(device)) {
                    TaskQueue.getInstance().enqueueForTaskQueue(address);
                    ClientThread clientThread=new ClientThread(device,dbHelper);
                    clientThreads.add(clientThread);
                }
            }

            for(int i=0;i<Constants.MAX_THREADS;i++) {
                String address=TaskQueue.getInstance().dequeueFromTaskQueue();
                if(address!=null){
                    Log.d(TAG, (origDataTasks.containsKey(address))+"");
                    if(origDataTasks.containsKey(address))
                    for(DataPacket dp:origDataTasks.get(address)) {
                        TaskQueue.getInstance().enqueueForSenderQueue(dp, i);
                    }
                }else{
                    break;
                }
            }
        } catch (InterruptedException ie) {
            Log.d(TAG, "Task Queue Exception");
        }
        if(clientThreads.size()>0) {
            Log.d(TAG, "Creating Handlers:"+clientThreads.size());
            createHandlers();
        }

    }

    public void createServerHandlers(){

        Handler serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.DATA_RECEIVED: {

                        Log.d(TAG, "Data received" + ((byte[]) message.obj).length);
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inSampleSize = 2;
//
//                        Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
//                        new SavePhotoTask().execute((byte[])message.obj);
                        File dir=new File(dirPath);
                        if(!dir.exists()){

                            dir.mkdirs();
                        }
                        File photo=new File(dir, System.currentTimeMillis()+".mp4");
                        Log.d(TAG,"Saving into Path:"+photo.getAbsolutePath());

                        if (photo.exists()) {
                            photo.delete();
                        }

                        FileOutputStream fos= null;
                        try {
                            fos = new FileOutputStream(photo.getPath());
                            fos.write((byte[])message.obj);

                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG,"Exception",e);
                        }




                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Log.d(TAG,"DIGEST NOT MATCHED!!");
//                        Toast.makeText(getActivity().getApplicationContext(), "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DATA_PROGRESS_UPDATE: {

                        ProgressData progressData = (ProgressData) message.obj;
                        double pctRemaining = 100 - (((double) progressData.remainingSize / progressData.totalSize) * 100);
                        Log.d(TAG, (int) Math.floor(pctRemaining) + "");
                        break;
                    }

                    case MessageType.INVALID_HEADER: {
                        Log.d(TAG,"Invalid Header!!");
//                        Toast.makeText(getActivity().getApplicationContext(), "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

        if (pairedDevices != null) {
            if (serverThread == null) {
                Log.v(TAG, "Starting server thread.  Able to accept photos.");
                serverThread = new ServerThread(adapter, serverHandler);
                serverThread.start();
            }
        }

    }


    public void createHandlers() {
        Looper.prepare();

        for (final ClientThread clientThread : clientThreads) {
            Handler clientHandler = new Handler() {
                @Override
                public synchronized void handleMessage(Message message) {
                    switch (message.what) {
                        case MessageType.READY_FOR_DATA: {
                            try {
                                Log.d(TAG, message.what + "");
                                DataPacket dp = TaskQueue.getInstance().dequeueFromSenderQueue(clientThreads.indexOf(clientThread));
                                if (dp != null) {
                                    String fileName = dp.getFilePath();
                                    File file = new File(fileName);
                                    if (file.exists()) {
                                        byte[] videoData = FileUtils.readFileToByteArray(file);
                                        Message msg = new Message();
                                        msg.obj = new MessageBox(dp, videoData, file.getName().substring(file.getName().indexOf(".") + 1));
                                        clientThread.incomingHandler.sendMessage(msg);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                            break;
                        }
                        case MessageType.COULD_NOT_CONNECT: {
                            Log.d(TAG,"Could not Connect!!");
                            break;
                        }

                        case MessageType.SENDING_DATA: {
                            Log.d(TAG, "Sending photo...");
                            break;
                        }

                        case MessageType.DATA_SENT_OK: {
                            Log.d(TAG,"Data sent !!");

                            new SendImageTask().execute(clientThread);
                            break;
                        }

                        case MessageType.DIGEST_DID_NOT_MATCH: {
                            Log.d(TAG,"DIGEST NOT MATCHED!!");
                            break;
                        }
                    }
                }
            };
            handlers.add(clientHandler);
            clientThread.setHandler(clientHandler);
            clientThread.start();
        }
        Looper.loop();




    }

    public void beginProcessing(){
        for(ClientThread clientThread:clientThreads) {
            Log.d(TAG,"Size:"+clientThreads.size());
            clientThread.start();
        }
    }
    private class SendImageTask extends AsyncTask<ClientThread, Void, DataPacket> {

        protected DataPacket doInBackground(ClientThread... clientsThread) {
            DataPacket dp=null;
            ClientThread clientThread=clientsThread[0];
            try {
                Log.d(TAG,"Trying to Send Next..");
                dp = TaskQueue.getInstance().dequeueFromSenderQueue(clientThreads.indexOf(clientThread));

                if(dp!=null) {
                    String fileName=dp.getFilePath();
                    clientThread.start();
                    File file = new File(fileName);
                    if (file.exists()) {
                        byte[] videoData = FileUtils.readFileToByteArray(file);
                        Message msg = new Message();
                        msg.obj = new MessageBox(dp,videoData,file.getName().substring(file.getName().indexOf(".")+1));
                        clientThread.incomingHandler.sendMessage(msg);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();

            }
            return dp;
        }


        protected void onPostExecute(DataPacket result) {
            if(result!=null)
                dbHelper.deleteDeviceFileInfo(result);

        }
    }
    class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... jpeg) {
            Log.d(TAG,"Saving file....");
            File dir=new File(dirPath);
            if(!dir.exists()){

                dir.mkdirs();
            }
            try {
                String ext;
                try {
                    ext="mp4";//Magic.getMagicMatch(jpeg[0]).getExtension();
                }catch (Exception e){
                    ext="jpg";
                }
                File photo=new File(dir, System.currentTimeMillis()+"."+ext);
                Log.d(TAG,"Saving into Path:"+photo.getAbsolutePath());

                if (photo.exists()) {
                    photo.delete();
                }

                FileOutputStream fos=new FileOutputStream(photo.getPath());

                fos.write(jpeg[0]);

                fos.close();
//                if(ext.equals("jpg")) {
//                    ContentValues values = new ContentValues();
//
//                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                    values.put(MediaStore.MediaColumns.DATA, photo.getAbsolutePath());
//
//                    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                }
            }
            catch (IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }

            return(null);
        }
    }


}
