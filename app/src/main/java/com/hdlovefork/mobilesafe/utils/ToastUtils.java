package com.hdlovefork.mobilesafe.utils;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/10/10.
 */
public class ToastUtils {
    public static void show(final Activity activity, final CharSequence text, final int duration){
        if("main".equalsIgnoreCase(Thread.currentThread().getName())){
            Toast.makeText(activity, text,duration).show();
        }else{
           activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(activity,text,duration).show();
               }
           });
        }
    }

    public static void show(final Activity activity, final CharSequence text) {
        show(activity,text,Toast.LENGTH_SHORT);
    }
}
