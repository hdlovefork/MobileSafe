package com.hdlovefork.mobilesafe.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hdlovefork.mobilesafe.activities.EnterPwdActivity;
import com.hdlovefork.mobilesafe.db.dao.AppLockDao;

import java.util.List;

/**
 * Created by Administrator on 2015/10/29.
 */
public class AppLockService extends Service {
    /**
     * 放行一个应用包的广播
     */
    public static final String ACTION_GRANT_APP = "com.hdlovefork.mobilesafe.GRANT_APP";

    /**
     * 需要监视的应用程序包名
     */
    private List<String> mLockPackages;
    private boolean mRunning = false;
    private ActivityManager mActivityManager;
    private Intent mIntent;
    private InnerAppLockReceiver mReceiver;
    /**
     * 临时允许的不需要输入密码的应用的包名,该应用已经输入过正确的密码
     */
    private String mGrantPackageName="";
    private AppLockObserver mAppLockObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppLockDao appLockDao = AppLockDao.getInstance(this);
        //访问数据库获取所有待监视的应用包名
        mLockPackages = appLockDao.findAll();
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //拦截到一个被锁定的应用时,需要弹出输入解锁密码的对话框
        mIntent = new Intent(this, EnterPwdActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //某个应用被临时允许放行,因为用户已经输入了正确的密码
        IntentFilter filter=new IntentFilter(ACTION_GRANT_APP);
        //锁屏后不监视应用的启动
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //解屏后继续监视应用的启动
        filter.addAction(Intent.ACTION_SCREEN_ON);
        //接收屏幕锁定,解锁和应用被临时放行的广播
        mReceiver = new InnerAppLockReceiver();
        registerReceiver(mReceiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        mAppLockObserver = new AppLockObserver(new Handler());
        //数据库内容更新后,需要更新mLockPackages的内容
        getContentResolver().registerContentObserver(AppLockDao.CONTENT_URI, false, mAppLockObserver);
        //开始监视应用程序的启动
        startWatch();
        PackageManager packageManager=getPackageManager();
        Log.d("AppLockService", packageManager.getClass().getSimpleName());
    }

    //开始监视应用启动
    private void startWatch() {
        new Thread(){
            @Override
            public void run() {
                threadWatchNewTask();
            }
        }.start();
    }

    private void threadWatchNewTask() {
        if (mRunning)
            return;
        mRunning = true;
        while (mRunning) {
            //获取当前运行在最上面的任务栈,即最近运行的任务
            List<ActivityManager.RunningTaskInfo> runningTasks = mActivityManager.getRunningTasks(1);
            String curPackageName = runningTasks.get(0).topActivity.getPackageName();
            if(!mGrantPackageName.equals(curPackageName) && mLockPackages.contains(curPackageName)) {
                //当前启动的应用不被临时放行同时是一个应该加锁的应用
                //弹出输入密码提示框
                mIntent.putExtra("package_name", curPackageName);
                startActivity(mIntent);
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver);
        getContentResolver().unregisterContentObserver(mAppLockObserver);
    }

    class InnerAppLockReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case Intent.ACTION_SCREEN_OFF:
                    //退出监视应用启动的线程,防止耗电,因为锁屏时用户不能看到屏幕中应用程序显示的内容
                    //没有必要再监视应用的启动
                    mRunning=false;
                    //锁屏后这个已经被放行的应用将取消授权,下次启动这个应用时还是需要输入密码
                    //清除临时放行的应用包名
                    mGrantPackageName="";
                    break;
                case Intent.ACTION_SCREEN_ON:
                    //开启监视应用启动的线程
                    startWatch();
                    break;
                case ACTION_GRANT_APP:
                    //用户输入了正确的程序锁密码,临时放行某个应用
                    mGrantPackageName = intent.getStringExtra("package_name");
                    break;
            }
        }
    }

    class AppLockObserver extends ContentObserver{

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public AppLockObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //数据库内容改变后,更新要监视的应用程序列表数据
            mLockPackages = AppLockDao.getInstance(getApplicationContext()).findAll();
        }


    }
}
