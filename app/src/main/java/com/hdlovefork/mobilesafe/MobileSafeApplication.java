package com.hdlovefork.mobilesafe;

import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * Created by Administrator on 2015/11/4.
 */
public class MobileSafeApplication extends Application {
    private static final String TAG = "MobileSafeApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        //设置未捕获的异常处理方法
        //Thread.currentThread().setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    //当有未捕获的异常发生时该方法被执行
    //该方法将会在cache目录下创建一个名为error.txt的文件
    class MyExceptionHandler implements Thread.UncaughtExceptionHandler{

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.d(TAG, "uncaughtException() called with: " + "thread = [" + thread + "], ex = [" + ex + "]");
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("**********设备信息**********\r\n");
            //在Build类中存有设备的信息，所以利用反射的方式获取设备的信息
            //获取Build类的所有静态字段定义
            Field[] declaredFields = Build.class.getDeclaredFields();

            try {
                //获取设备的详细信息
                //在cache目录中生成名为error.txt的文件
                FileOutputStream outputStream=new FileOutputStream(new File(getCacheDir(),"error.txt"));
                for(Field field:declaredFields){
                    //按“属性名：值”的格式生成字符串追加到StringBuilder中
                    stringBuilder.append(field.getName()+":"+field.get(null).toString()+"\r\n");
                }
                stringBuilder.append("\r\n**********错误报告**********\r\n");
                //把配置信息写入文件中
                outputStream.write(stringBuilder.toString().getBytes());
                //把错误堆栈信息写入文件中
                ex.printStackTrace(new PrintStream(outputStream));
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //自身进程已经无法救活，干脆自杀得了
            android.os.Process.killProcess(Process.myPid());
        }
    }
}
