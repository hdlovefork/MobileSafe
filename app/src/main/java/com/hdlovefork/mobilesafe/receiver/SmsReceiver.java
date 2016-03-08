package com.hdlovefork.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.util.Log;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.services.GPSService;

/**
 * Created by Administrator on 2015/10/14.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //检查用户配置中是否开启保护功能
        DevicePolicyManager devicePolicyManager= (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName=new ComponentName(context,MyAdminReceiver.class);
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getString(R.string.config_file_name),Context.MODE_PRIVATE);
        boolean protecting=sharedPreferences.getBoolean("protecting",false);
        if(protecting==false) return;
        Object[] objs= (Object[]) intent.getExtras().get("pdus");
        for (Object o:objs){
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) o);
            String body=sms.getMessageBody();
            switch (body){
                case "#*location*#":
                    Log.d("SmsReceiver", "返回手机位置信息");
                    //开启服务进行定位
                    Intent gpsService = new Intent(context, GPSService.class);
                    context.startService(gpsService);
                    abortBroadcast();
                    break;
                case "#*alarm*#":
                    Log.d("SmsReceiver", "播放报警音乐");
                    MediaPlayer player=MediaPlayer.create(context, R.raw.ylzs);
                    player.setVolume(1,1);
                    player.setLooping(true);
                    player.start();
                    abortBroadcast();
                    break;
                case "#*wipedate*#":
                    Log.d("SmsReceiver", "清除手机数据");
                    //没有获取管理员权限直接退出
                    if(!devicePolicyManager.isAdminActive(componentName)) return;
                    devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE|DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
                    abortBroadcast();
                    break;
                case "#*lockscreen*#":
                    Log.d("SmsReceiver", "锁定手机屏幕");
                    //没有获取管理员权限直接退出
                    if(!devicePolicyManager.isAdminActive(componentName)) return;
                    devicePolicyManager.resetPassword("123",0);
                    devicePolicyManager.lockNow();
                    abortBroadcast();
                    break;
            }
        }
    }
}
