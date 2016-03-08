package com.hdlovefork.mobilesafe.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Administrator on 2015/10/17.
 */
public class ServiceUtils {
    private static final String TAG = "ServiceUtils";

    /**
     * 判断提供的服务类是否正在运行
     * @param context
     * @param clsName 待检测服务类名
     * @return
     */
    public static boolean existsService(Context context,Class<?> clsName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : runningServices) {
            if (info.service.getClassName().equals(clsName.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }
}
