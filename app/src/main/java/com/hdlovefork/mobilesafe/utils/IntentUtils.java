package com.hdlovefork.mobilesafe.utils;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by Administrator on 2015/10/10.
 */
public class IntentUtils {

    /**
     * 启动一个Activity
     * @param activity
     * @param cls
     */
    public static  void startActivity(Activity activity,Class<?> cls){
        Intent intent=new Intent(activity,cls);
        activity.startActivity(intent);
    }

    /**
     * 启动一个Activity
     * @param activity
     * @param cls
     */
    public static  void startActivityAndFinish(Activity activity,Class<?> cls){
        Intent intent=new Intent(activity,cls);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * 延时启动一个Activity后关闭自身
     * @param activity
     * @param cls
     * @param delayMill
     */
    public static void startActivityForDelayAndFinish(final Activity activity, final Class<?> cls, final int delayMill){
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(delayMill);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent=new Intent(activity,cls);
                activity.startActivity(intent);
                activity.finish();
            }
        }.start();
    }

    /**
     * 延时启动一个Activity
     * @param activity
     * @param cls
     * @param delayMill
     */
    public static  void startActivityForDelay(final Activity activity, final Class<?> cls, final int delayMill){
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(delayMill);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent=new Intent(activity,cls);
                activity.startActivity(intent);
            }
        }.start();
    }
}
