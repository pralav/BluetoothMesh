package org.lsm.bluetoothmesh.service;

import org.lsm.bluetoothmesh.database.DataPacket;

/**
 * Created by pralav on 3/18/15.
 */
public class MessageBox {
    DataPacket dp;
    byte[] data;
    String extension;

    public MessageBox(DataPacket dp, byte[] data,String extension) {
        this.dp = dp;
        this.data = data;
        this.extension=extension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public DataPacket getDp() {
        return dp;
    }

    public void setDp(DataPacket dp) {
        this.dp = dp;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
