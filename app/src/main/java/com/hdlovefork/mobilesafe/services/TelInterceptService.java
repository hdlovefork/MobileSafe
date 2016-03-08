package com.hdlovefork.mobilesafe.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.hdlovefork.mobilesafe.db.dao.BlackListDao;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2015/10/17.
 */
public class TelInterceptService extends Service {

    private SmsReceiver mSmsReceiver;
    private BlackListDao mBlackListDao;
    private TelephonyManager mTelephonyManager;
    private TelListener mTelListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //电话管理服务
        mTelephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        //注册来电监听器
        mTelListener = new TelListener();
        mTelephonyManager.listen(mTelListener, PhoneStateListener.LISTEN_CALL_STATE);
        //注册短信广播接收
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);
        mSmsReceiver = new SmsReceiver();
        registerReceiver(mSmsReceiver, filter);
        Log.d("TelInterceptService", "短信监听已开启");
        Log.d("TelInterceptService", "电话监听已开启");
        mBlackListDao = new BlackListDao(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //反注册短信广播接收
        unregisterReceiver(mSmsReceiver);
        mTelephonyManager.listen(mTelListener, 0);
        Log.d("TelInterceptService", "短信监听已关闭");
        Log.d("TelInterceptService", "电话监听已关闭");
    }

    //短信拦截
    class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //解析短信
            Object[] objs = (Object[]) intent.getExtras().get("pdus");
            for (Object obj : objs) {
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) obj);
                String addr = msg.getOriginatingAddress();
                int mode = mBlackListDao.findMode(addr);
                if (mode == BlackListDao.MODE_SMS || mode == BlackListDao.MODE_ALL) {
                    //拦截短信
                    abortBroadcast();
                    Log.d("SmsReceiver", addr + "短信被拦截");
                }
            }
        }
    }

    class TelListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    int mode = mBlackListDao.findMode(incomingNumber);
                    if (mode == BlackListDao.MODE_PHO || mode == BlackListDao.MODE_ALL) {
                        //拦截电话
                        try {
                            Log.d("TelListener", "准备拦截电话");
                            Class cls = getClassLoader().loadClass("android.os.ServiceManager");
                            Method method = cls.getDeclaredMethod("getService", String.class);
                            IBinder binder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
                            ITelephony telephony = ITelephony.Stub.asInterface(binder);
                            telephony.endCall();
                            Log.d("TelListener", "电话被拦截");
                            //清除通话记录，通过内容观察者当通话记录表发生改变时收到通知
                            getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, new ContentObserver(new Handler()) {
                                @Override
                                public void onChange(boolean selfChange) {
                                    try {
                                        getContentResolver().unregisterContentObserver(this);
                                        Log.d("TelListener", "清除通话记录");
                                        getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + "=?", new String[]{incomingNumber});
                                    }catch (SecurityException e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }
}
