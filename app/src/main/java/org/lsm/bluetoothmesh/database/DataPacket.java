package org.lsm.bluetoothmesh.database;

/**
 * Created by pralav on 3/17/15.
 */
public class DataPacket {
    private String deviceName;
    private String deviceAddress;
    private String filePath;
    private int id;


    public DataPacket(String deviceName, String deviceAddress, String filePath) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.filePath = filePath;
    }

    public DataPacket(int id,String deviceName, String deviceAddress, String filePath) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.filePath = filePath;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "deviceName='" + deviceName + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", filePath='" + filePath + '\'' +
                ", id=" + id +
                '}';
    }
}
