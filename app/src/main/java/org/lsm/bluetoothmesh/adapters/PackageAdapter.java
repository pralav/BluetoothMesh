/*
 * Copyright (C) 2013 47 Degrees, LLC
 *  http://47deg.com
 *  hello@47deg.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.lsm.bluetoothmesh.adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fortysevendeg.swipelistview.SwipeListView;
import org.lsm.bluetoothmesh.R;
import org.lsm.bluetoothmesh.database.DBHelper;
import org.lsm.bluetoothmesh.database.DataPacket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.lsm.bluetoothmesh.R;

public class PackageAdapter extends BaseAdapter {

    private List<FilesItem> data;
    private Context context;

    public PackageAdapter(Context context, List<FilesItem> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public FilesItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    @Override
//    public boolean isEnabled(int position) {
//        if (position == 2) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FilesItem item = getItem(position);
        final DBHelper helper=new DBHelper(this.context);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.package_row, parent, false);
            holder = new ViewHolder();
            holder.ivImage = (ImageView) convertView.findViewById(R.id.example_row_iv_image);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.example_row_tv_title);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.example_row_tv_description);
            holder.bAction1 = (Button) convertView.findViewById(R.id.example_row_b_action_1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((SwipeListView)parent).recycle(convertView, position);

        holder.ivImage.setImageDrawable(item.getIcon());
        holder.tvTitle.setText(item.getName());
        holder.tvDescription.setText(item.getFullPath());
        final View view=convertView;
        final BluetoothDevice[] devicesList=getDevices();

        holder.bAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] deviceNames=new CharSequence[devicesList.length];
                int j=0;
                for(BluetoothDevice d:devicesList){
                    deviceNames[j++]=d.getName();
                }

                new MaterialDialog.Builder(context)
                        .title(R.string.title)
                        .items(deviceNames)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMulti() {
                            @Override
                            public void onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                Log.d("====>", Arrays.toString(which)+" "+item.getFullPath());
                                for(int devIdx:which) {
                                    helper.createFile(new DataPacket(devicesList[devIdx].getName(),devicesList[devIdx].getAddress(),item.getFullPath()));
                                }
                                Log.d("===>",helper.getAllDeviceFileInfo().size()+"");
                            }

                        })
                        .positiveText(R.string.choose)
                        .negativeText(R.string.cancel)
                        .show();
                    Toast.makeText(context, R.string.cantOpen, Toast.LENGTH_SHORT).show();

            }
        });

//        holder.bAction2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isPlayStoreInstalled()) {
//                    context.startActivity(new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("market://details?id=" + item.getFullPath())));
//                } else {
//                    context.startActivity(new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("http://play.google.com/store/apps/details?id=" + item.getFullPath())));
//                }
//            }
//        });
//
//        holder.bAction3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri packageUri = Uri.parse("package:" + item.getFullPath());
//                Intent uninstallIntent;
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                    uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
//                } else {
//                    uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
//                }
//                context.startActivity(uninstallIntent);
//            }
//        });


        return convertView;
    }

    private BluetoothDevice[] getDevices() {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices=new HashSet<BluetoothDevice>();
        if(adapter==null) {
            Log.e("Error","No bluetooth");

        }else{
            if(!adapter.isEnabled())
                adapter.enable();
            devices=adapter.getBondedDevices();
        }
        BluetoothDevice[] deviceArray=new BluetoothDevice[devices.size()];
        int i=0;
        for(BluetoothDevice device:devices){
            deviceArray[i++]=device;
        }


        return deviceArray;
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
        TextView tvDescription;
        Button bAction1;
        Button bAction2;
        Button bAction3;
    }

    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }

}
