package org.lsm.bluetoothmesh.database;

/**
 * Created by pralav on 2/26/15.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String
            DATABASE_NAME = "LSM_DB.db",
            TABLE_FILES = "BT_files",
            KEY_ID = "id",
            FILE_NAME = "filename",
            DEVICE_NAME = "device_name",
            DEVICE_ADDRESS= "device_address";
    String columns[]=new String[]{KEY_ID,FILE_NAME, DEVICE_NAME,DEVICE_ADDRESS};



    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
//        getWritableDatabase
        Log.d("=====>","ennnnaaaa");
        db.execSQL("CREATE TABLE " + TABLE_FILES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + FILE_NAME + " TEXT," + DEVICE_NAME + " TEXT,"+DEVICE_ADDRESS+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);

        onCreate(db);
    }
    public void createFile(DataPacket dataPacket) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(FILE_NAME, dataPacket.getFilePath());
        values.put(DEVICE_NAME, dataPacket.getDeviceName());
        values.put(DEVICE_ADDRESS, dataPacket.getDeviceAddress());
        if(dataPacket.getFilePath()!=null && dataPacket.getDeviceAddress()!=null && dataPacket.getDeviceName()!=null )
            db.insert(TABLE_FILES, null, values);
        else{
            Log.d("====>","Error"+dataPacket);
        }
        db.close();
    }

    public static String getFileName() {
        return FILE_NAME;
    }

    public static String getDeviceName() {
        return DEVICE_NAME;
    }

    public static String getDeviceAddress() {
        return DEVICE_ADDRESS;
    }

    public ArrayList<DataPacket> getDeviceFileInfo(String name) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_FILES, columns, DEVICE_NAME + "=?", new String[] { String.valueOf(name) }, null, null, null, null);
        ArrayList<DataPacket> dataPackets = new ArrayList<DataPacket>();
        if (cursor != null){
            if (cursor.moveToFirst()) {
                do {
                    dataPackets.add(new DataPacket(Integer.parseInt(cursor.getString(0)), cursor.getString(2),cursor.getString(3),cursor.getString(1)));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }



        db.close();
        cursor.close();
        return dataPackets;
    }
    public void deleteDeviceFileInfo(DataPacket dataPacket) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FILES, KEY_ID + "=?", new String[] { String.valueOf(dataPacket.getId()) });
        db.close();
    }

    public int getFileCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FILES, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();

        return count;
    }

    public int updateDeviceFileInfo(DataPacket dataPacket) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(FILE_NAME, dataPacket.getFilePath());
        values.put(DEVICE_NAME, dataPacket.getDeviceName());
        values.put(DEVICE_ADDRESS, dataPacket.getDeviceAddress());


        int rowsAffected = db.update(TABLE_FILES, values, KEY_ID + "=?", new String[] { String.valueOf(dataPacket.getId()) });
        db.close();

        return rowsAffected;
    }
    public ArrayList<DataPacket> getAllDeviceFileInfo() {
        ArrayList<DataPacket> dataPackets = new ArrayList<DataPacket>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FILES +"", null);

        if (cursor.moveToFirst()) {
            do {
                DataPacket dp=new DataPacket(Integer.parseInt(cursor.getString(0)), cursor.getString(2),cursor.getString(3),cursor.getString(1));
                Log.d("==>", dp.toString());
                dataPackets.add(dp);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataPackets;
    }


}