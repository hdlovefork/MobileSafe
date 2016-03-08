package com.hdlovefork.mobilesafe.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.db.dao.AddressDao;

/**
 * Created by Administrator on 2015/10/21.
 */
public class AddressTipService extends Service {

    private IncomingListener mIncomingListener;
    private InnerOutCallReceiver mInnerOutCallReceiver;
    private AddressDao mDao;
    private TelephonyManager mTelephonyManager;
    private WindowManager mWindowManager;
    private View mToastView;
    private WindowManager.LayoutParams mParams;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //来电监听
        mDao = new AddressDao(AddressTipService.this);
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mIncomingListener = new IncomingListener();
        mTelephonyManager.listen(mIncomingListener, PhoneStateListener.LISTEN_CALL_STATE);
        //去电监听
        mInnerOutCallReceiver = new InnerOutCallReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(mInnerOutCallReceiver, intentFilter);
        Log.d("AddressTipService", "归属地显示服务已创建");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //来电监听销毁
        mTelephonyManager.listen(mIncomingListener, PhoneStateListener.LISTEN_NONE);
        mIncomingListener = null;
        mTelephonyManager = null;
        //去电监听销毁
        unregisterReceiver(mInnerOutCallReceiver);
        Log.d("AddressTipService", "归属地显示服务已销毁");
    }

    //来电监听
    class IncomingListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //查询来电归属地
                    String addr = mDao.findAddress(incomingNumber);
                    //显示自定义土司
                    showMyToast(addr);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mToastView != null) {
                        mWindowManager.removeView(mToastView);
                        mToastView = null;
                    }
                    break;
            }
        }
    }

    private void showMyToast(String addr) {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //生成土司视图
        mToastView = View.inflate(this, R.layout.toast_address, null);
        mToastView.setOnTouchListener(new View.OnTouchListener() {
            //为了使归属地能移动所以需要监听手指触摸事件
            //记录手指按下时的坐标
            private int mDownY;
            private int mDownX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = (int) event.getRawX();
                        mDownY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        int dx = moveX - mDownX;
                        int dy = moveY - mDownY;
                        mParams.x += dx;
                        mParams.y += dy;
                        //校正归属地的在屏幕中的位置
                        if (mParams.x < 0) mParams.x = 0;
                        if (mParams.y < 0) mParams.y = 0;
                        if (mParams.x > mWindowManager.getDefaultDisplay().getWidth() - mToastView.getWidth()) {
                            mParams.x = mWindowManager.getDefaultDisplay().getWidth() - mToastView.getWidth();
                        }
                        if (mParams.y > mWindowManager.getDefaultDisplay().getHeight() - mToastView.getHeight()) {
                            mParams.y = mWindowManager.getDefaultDisplay().getHeight() - mToastView.getHeight();
                        }
                        //移动归属地框
                        mWindowManager.updateViewLayout(mToastView, mParams);
                        mDownX = moveX;
                        mDownY = moveY;
                        break;
                    case MotionEvent.ACTION_UP:
                        //保存归属地框的位置，便于下次显示
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putInt("addr_box_x", mParams.x);
                        edit.putInt("addr_box_y", mParams.y);
                        edit.commit();
                        break;
                }
                return true;
            }
        });
        //获取资源文件中的样式定义数组
        String[] addrBoxStyleDescArray = getResources().getStringArray(R.array.addrBoxStyleDescArray);
        int[] addrBoxStyleResArray = {R.drawable.call_locate_white, R.drawable.call_locate_orange, R.drawable.call_locate_blue, R.drawable.call_locate_gray, R.drawable.call_locate_green};
        //读取用户配置文件中归属地提示框背景样式
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        int styleIndex = sharedPreferences.getInt(getString(R.string.addr_box_style), 0);
        styleIndex = styleIndex < 0 ? 0 : styleIndex;
        styleIndex = styleIndex > addrBoxStyleResArray.length ? 0 : styleIndex;
        //设置归属地提示框背景样式
        mToastView.setBackgroundResource(addrBoxStyleResArray[styleIndex]);
        TextView tvAddress = (TextView) mToastView.findViewById(R.id.tv_toast_address);
        //设置土司文本
        tvAddress.setText(addr);
        mParams = new WindowManager.LayoutParams();
        //归属地默认对齐于屏幕左上角，如果不指定下面代码的话，它将默认显示在屏幕的正中间位置，并且坐标系统的0，0点也是在屏幕正中间
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        //读取上次归属地的位置
        int lastX = sharedPreferences.getInt("addr_box_x", -1);
        int lastY = sharedPreferences.getInt("addr_box_y", -1);
        if (lastX != -1 && lastY != -1) {
            //设置归属地对齐于窗口的左上角，否则显示在屏幕的正中间
            mParams.x = lastX;
            mParams.y = lastY;
        } else {
            //将归属地摆在正中间位置
            mParams.x = (mWindowManager.getDefaultDisplay().getWidth() - mToastView.getWidth()) / 2;
            mParams.y = (mWindowManager.getDefaultDisplay().getHeight() - mToastView.getHeight()) / 2;
        }
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.format = PixelFormat.TRANSLUCENT;
        //让归属地具有电话界面一样的优先级，需要android.permission.SYSTEM_ALERT_WINDOW权限
        mParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //显示土司
        mWindowManager.addView(mToastView, mParams);
    }

    //去电监听
    class InnerOutCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取去电号码
            String number = getResultData();
            //查询去电归属地
            String addr = mDao.findAddress(number);
            //显示自定义土司
            showMyToast(addr);
        }
    }
}
