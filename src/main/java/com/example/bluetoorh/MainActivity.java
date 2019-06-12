package com.example.bluetoorh;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{
    private final static String TAG = MainActivity.class.getSimpleName();
    String[] permissions = new String[]{BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION,CAMERA
    };
    List<String> mPermissionList = new ArrayList<>();
    private static final int BLE_REQID = 1;
    private int REQUEST_ENABLE_BT = 2;
    private boolean mScanning;
    private Handler handler ;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListadpter leDeviceListAdapter;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        leDeviceListAdapter= new LeDeviceListadpter();
        handler =new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPermissions();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        listView=findViewById(R.id.ble_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dev_name= leDeviceListAdapter.getDevice(i).getName();
                String dev_addr=leDeviceListAdapter.getDevice(i).getAddress();
                Intent intent = new Intent(MainActivity.this,DeviceControlActivity.class);
                intent.putExtra("dev_name",dev_name);
                intent.putExtra("dev_addr",dev_addr);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        leDeviceListAdapter = new LeDeviceListadpter();
        listView.setAdapter(leDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
            super.onPause();
            scanLeDevice(false);
            leDeviceListAdapter.clear();
    }

    public String[] getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            mPermissionList.clear();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(MainActivity.this, permissions, BLE_REQID);
            }
        }
        return permissions;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                leDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BLE_REQID:
                mPermissionList.clear();
                int i = 0;
                Log.e(TAG, "legthr:" + grantResults.length + " i=" + i);
                for (i = 0; i < grantResults.length; i++)
                {
                Log.e(TAG, "legthn:" + grantResults.length + " i=" + i);

                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }

                }

            if (mPermissionList.size() > 0) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage("应用需要权限才能正常运行，是否重新申请?");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, BLE_REQID);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                dialog.show();
            }

                break;
            default:
                break;

        }
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(MainActivity.this);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(MainActivity.this);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(MainActivity.this);
        }
        invalidateOptionsMenu();
    }


    @Override
    public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                leDeviceListAdapter.addDevice(bluetoothDevice);
                leDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }
    private  class LeDeviceListadpter extends BaseAdapter{
        private  ArrayList<BluetoothDevice> mLeDeviceList;
        private LayoutInflater mInflator;
        public LeDeviceListadpter(){
            super();
            mLeDeviceList =new ArrayList<BluetoothDevice>();
            mInflator  = MainActivity.this.getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device){
            if(!mLeDeviceList.contains(device))
            {
                mLeDeviceList.add(device);
            }
        }
        public void clear() {
            mLeDeviceList.clear();
        }

        public BluetoothDevice getDevice(int position)
        {
            return mLeDeviceList.get(position);
        }
        @Override
        public int getCount() {
            return mLeDeviceList.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDeviceList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view==null)
            {
                view =mInflator.inflate(R.layout.device,null);
                viewHolder=new ViewHolder();
                viewHolder.deviceName=(TextView) view.findViewById(R.id.dev_name);
                viewHolder.deviceAddr=(TextView) view.findViewById(R.id.dev_addr);
                view.setTag(viewHolder);
            }else{
                viewHolder= (ViewHolder) view.getTag();
            }
            BluetoothDevice bluetoothDevice =mLeDeviceList.get(i);
            String dev_name = bluetoothDevice.getName();
            String dev_addr = bluetoothDevice.getAddress();
            if(dev_name!=null&&dev_name.length()>0) {
                viewHolder.deviceName.setText(bluetoothDevice.getName());
            }
            viewHolder.deviceAddr.setText(bluetoothDevice.getAddress());
            return view;
        }
        public class ViewHolder{
            TextView deviceName;
            TextView deviceAddr;
        }
    }
}
