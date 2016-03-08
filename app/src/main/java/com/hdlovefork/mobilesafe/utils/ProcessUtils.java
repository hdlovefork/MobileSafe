package com.hdlovefork.mobilesafe.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/25.
 */
public class ProcessUtils {
    /**
     * 获取系统总内存大小
     *
     * @param context
     * @return
     */
    public static long getTotalMemorySize(Context context) {
        File inFile = new File("/proc/meminfo");
        try {
            FileInputStream inputStream = new FileInputStream(inFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String text = reader.readLine();
            for (char c : text.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    stringBuilder.append(c);
                }
            }
            reader.close();
            inputStream.close();
            return Long.parseLong(stringBuilder.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取系统已经使用的内存大小
     *
     * @param context
     * @return
     */
    public static long getUsedMemorySize(Context context) {
        File inFile = new File("/proc/meminfo");
        try {
            FileInputStream inputStream = new FileInputStream(inFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            reader.readLine();
            String text = reader.readLine();
            for (char c : text.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    stringBuilder.append(c);
                }
            }
            reader.close();
            inputStream.close();
            return getTotalMemorySize(context) - Long.parseLong(stringBuilder.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取系统可用的内存大小
     *
     * @param context
     * @return
     */
    public static long getAvailableMemorySize(Context context) {
        File inFile = new File("/proc/meminfo");
        try {
            FileInputStream inputStream = new FileInputStream(inFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            reader.readLine();
            String text = reader.readLine();
            for (char c : text.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    stringBuilder.append(c);
                }
            }
            reader.close();
            inputStream.close();
            return Long.parseLong(stringBuilder.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取给定集合中的所有用户进程
     *
     * @param allInfos
     * @return
     */
    public static List<ProcessInfo> getUserProcess(List<ProcessInfo> allInfos) {
        List<ProcessInfo> infos = new ArrayList<>();
        for (ProcessInfo info : allInfos) {
            if (info.isUserProcess()) {
                infos.add(info);
            }
        }
        return infos;
    }

    /**
     * 获取给定集合中的所有系统进程
     *
     * @param allInfos
     * @return
     */
    public static List<ProcessInfo> getSystemProcess(List<ProcessInfo> allInfos) {
        List<ProcessInfo> infos = new ArrayList<>();
        for (ProcessInfo info : allInfos) {
            if (!info.isUserProcess()) {
                infos.add(info);
            }
        }
        return infos;
    }

    /**
     * 获取所有的活动进程（除去system,android.process.media,android.process.acore这3个特殊进程）
     *
     * @param context
     * @return
     */
    public static List<ProcessInfo> getAllProcess(Context context) {
        List<ProcessInfo> infos = new ArrayList<>();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取正在运行的所有进程
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        PackageManager packageManager = context.getPackageManager();
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            ProcessInfo processInfo = new ProcessInfo();
            //获取占用内存大小
            Debug.MemoryInfo processMemoryInfo = activityManager.getProcessMemoryInfo(new int[]{info.pid})[0];
            long size = processMemoryInfo.getTotalPrivateDirty() * 1024;
            processInfo.setMemorySize(size);
            try {
                //获取应用图标和应用名
                PackageInfo packageInfo = packageManager.getPackageInfo(info.processName, 0);
                //获取包名，进程名=包名
                processInfo.setPackageName(info.processName);

                //获取图标
                Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                processInfo.setIcon(icon);
                //获取应用名
                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                processInfo.setProcessName(appName);
                //获取进程类型（用户、系统进程）
                processInfo.setIsUserProcess((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                processInfo.setIsUserProcess(false);
                processInfo.setPackageName(info.processName);
                processInfo.setProcessName(info.processName);
                processInfo.setIcon(context.getResources().getDrawable(android.R.mipmap.sym_def_app_icon));
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            infos.add(processInfo);
        }
        return infos;
    }


    public static class ProcessInfo {

        private boolean check;
        private String mProcessName;
        private String mPackageName;
        private Drawable mIcon;
        private long mMemorySize;

        /**
         * 是否标记该进程
         * @return
         */
        public boolean isCheck() {
            return check;
        }
        /**
         * 是否标记该进程
         * @return
         */
        public void setCheck(boolean check) {
            this.check = check;
        }

        /**
         * 指示是否为用户进程
         *
         * @return
         */
        public boolean isUserProcess() {
            return mIsUserProcess;
        }

        /**
         * 指示是否为用户进程
         *
         * @return
         */
        public void setIsUserProcess(boolean isUserProcess) {
            mIsUserProcess = isUserProcess;
        }

        private boolean mIsUserProcess;

        /**
         * 应用的名称（非包名）
         *
         * @return
         */
        public String getProcessName() {
            return mProcessName;
        }

        /**
         * 应用的名称（非包名）
         *
         * @return
         */
        public void setProcessName(String processName) {
            mProcessName = processName;
        }

        /**
         * 进程的包名
         *
         * @return
         */
        public String getPackageName() {
            return mPackageName;
        }

        /**
         * 进程的包名
         *
         * @return
         */
        public void setPackageName(String packageName) {
            mPackageName = packageName;
        }

        /**
         * 进程的图标
         *
         * @return
         */
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * 进程的图标
         *
         * @return
         */
        public void setIcon(Drawable icon) {
            mIcon = icon;
        }

        /**
         * 所占内存大小
         *
         * @return
         */
        public long getMemorySize() {
            return mMemorySize;
        }

        /**
         * 所占内存大小
         *
         * @return
         */
        public void setMemorySize(long memorySize) {
            mMemorySize = memorySize;
        }
    }
}
