package com.hdlovefork.mobilesafe.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2015/10/10.
 */
public class PackageUtils {

    public static int getVersionCode(Context context){
        PackageManager pk=context.getPackageManager();
        try {
            PackageInfo info = pk.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static String getVersionName(Context context){
        PackageManager pk=context.getPackageManager();
        try {
            PackageInfo info = pk.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
