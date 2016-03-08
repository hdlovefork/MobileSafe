package com.hdlovefork.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Administrator on 2015/10/14.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
        SharedPreferences sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        //用户没有开启保护直接退出
        if(!sharedPreferences.getBoolean("protecting",false)) return;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String newSim = telephonyManager.getSimSerialNumber();
        if (!TextUtils.isEmpty(newSim)) {
            String oldSim = sharedPreferences.getString("sim", "") + "1";
            //用户换电话卡了
            if (!TextUtils.isEmpty(oldSim) && !newSim.equals(oldSim)) {
                String phone = sharedPreferences.getString("phone", "");
                if (!TextUtils.isEmpty(phone)) {
                    //发短信通知安全号码
                    Log.d("BootCompleteReceiver", "手机被盗发送短信到安全号码:" + phone);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone, null, "sim changed", null, null);
                }
            }
        }

    }
}
