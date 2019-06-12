package com.example.bluetoorh;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;

/**
 * Created by zhangaisong on 2018/11/28.
 */
public class PermissonManager {
    private  String TAG= "PermissonManager";
    private String[] permissionStr=new String[]{CAMERA,BLUETOOTH,CALL_PHONE};
    private String[] permissStrReq;
    private Activity activity;
    private List <Integer> PermissionFailList;
    private int permissionRflag;
    public PermissonManager(Activity activity) {
        Log.e(TAG, "创建");
        this.activity = activity;
        PermissionFailList = new ArrayList<>();
    }
    public  boolean WhereNeedToRequset()
    {
        Log.e(TAG, "WhereNeedToRequset");
        int i;
        for (i = 0 ; i<  permissionStr.length;i++ )
        {
            if( ContextCompat.checkSelfPermission(activity,permissionStr[i])!=PackageManager.PERMISSION_GRANTED)
                PermissionFailList.add(i);
        }
        if(PermissionFailList.size()!=0)
        {
            Log.e(TAG, "WhereNeedToRequset false");
            return true;
        }
        Log.e(TAG, "WhereNeedToRequset true");
        return false;
    }

    public void  RequestPermission()
    {
        /*
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_CONTACTS)) {
                Log.i(TAG,"检查权限");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
        */
        int FailSize=PermissionFailList.size();
        String[] FailStrs=new String[FailSize];
        for(int i=0;i<FailSize;i++)
        {
            FailStrs[i]=permissionStr[PermissionFailList.get(i).intValue()];
            Log.e(TAG, FailStrs[i]);
        }
        Log.i(TAG,"requestPermission");
        ActivityCompat.requestPermissions(activity, FailStrs, permissionRflag);

    }


}
