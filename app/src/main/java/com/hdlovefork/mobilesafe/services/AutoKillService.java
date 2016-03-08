package com.hdlovefork.mobilesafe.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hdlovefork.mobilesafe.receiver.TimeToKillReceiver;

/**
 * 定时清理进程
 */
public class AutoKillService extends Service {

    private PendingIntent mPendingIntent;
    private TimeToKillReceiver mTimeToKillReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //注册屏幕锁定的广播
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mTimeToKillReceiver = new TimeToKillReceiver();
        registerReceiver(mTimeToKillReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销屏幕锁定的广播
        unregisterReceiver(mTimeToKillReceiver);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (mPendingIntent != null) {
            //注销定时器
            alarmManager.cancel(mPendingIntent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AutoKillService", "清理进程");
        //清理进程的逻辑
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : activityManager.getRunningAppProcesses()) {
            //不杀死自己
            if (info.processName.equals(getPackageName())) continue;
            activityManager.killBackgroundProcesses(info.processName);
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, TimeToKillReceiver.class), 0);
        //下次10分钟后执行
        long trigger = SystemClock.elapsedRealtime() + 5 * 1000;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, mPendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }
}

