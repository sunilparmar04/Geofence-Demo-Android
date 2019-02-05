package com.nobroker.nbgeo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class Util {


    public static  void requestPermission(Activity context, int permissionCode) {
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                permissionCode);
    }


    public static boolean isPermissionRequired(Activity activity) {
        if (checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            return false;
        }
        return true;
    }

    public static boolean checkPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
