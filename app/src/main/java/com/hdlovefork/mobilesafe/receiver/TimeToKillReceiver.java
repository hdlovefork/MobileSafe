package com.hdlovefork.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hdlovefork.mobilesafe.services.AutoKillService;

/**
 * 接收定时清理的广播
 */
public class TimeToKillReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i=new Intent(context, AutoKillService.class);
        context.startService(i);
    }
}
