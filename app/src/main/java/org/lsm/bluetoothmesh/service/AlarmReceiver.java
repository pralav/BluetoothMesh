package org.lsm.bluetoothmesh.service;

/**
 * Created by pralav on 3/14/15.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent background = new Intent(context, MeshService.class);
        context.startService(background);
    }


}