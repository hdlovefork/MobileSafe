package com.hdlovefork.mobilesafe.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用程序包管理，包括分享、卸载、启动、查看应用详情、获取用户或系统应用等功能
 */
public class AppUtils {
    /**
     * 卸载指定包名的应用
     * @param context
     * @param packageName
     */
    public static void uninstall(Context context,String packageName){
        Intent intent=new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:"+packageName));
        context.startActivity(intent);
    }

    /**
     * 分享指定包名的应用
     * @param context
     * @param text
     */
    public static void share(Context context,String text){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(intent);
    }

    /**
     * 打开指定包名的详细信息
     * @param context
     * @param packageName
     */
    public static void detail(Context context,String packageName){
        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:"+packageName));
        context.startActivity(intent);
    }

    /**
     * 运行指定包名的应用
     * @param context
     * @param packageName
     * @return
     */
    public static boolean launch(Context context,String packageName){
        PackageManager packageManager = context.getPackageManager();
        Intent intentForPackage = packageManager.getLaunchIntentForPackage(packageName);
        if(intentForPackage!=null) {
            context.startActivity(intentForPackage);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取所有安装的包信息
     * @param context
     * @return
     */
    public static List<AppInfo> getAllAppInfos(Context context) {
        List<AppInfo> appInfos = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo info : installedPackages) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppPackageName(info.packageName);
            appInfo.setAppIcon(info.applicationInfo.loadIcon(packageManager));
            File file = new File(info.applicationInfo.sourceDir);
            appInfo.setApkSize(file.length());
            appInfo.setAppName(info.applicationInfo.loadLabel(packageManager).toString());
            appInfo.setInRom((info.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0);
            appInfo.setIsUserApp((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
            appInfos.add(appInfo);
        }
        return appInfos;
    }

    /**
     * 获取用户安装应用的信息
     * @param context
     * @return
     */
    public static List<AppInfo> getUserAppInfos(Context context) {
        List<AppInfo> appInfos = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo info : installedPackages) {
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                continue;
            AppInfo appInfo = new AppInfo();
            appInfo.setIsUserApp((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
            appInfo.setAppPackageName(info.packageName);
            appInfo.setAppIcon(info.applicationInfo.loadIcon(packageManager));
            File file = new File(info.applicationInfo.sourceDir);
            appInfo.setApkSize(file.length());
            appInfo.setAppName(info.applicationInfo.loadLabel(packageManager).toString());
            appInfo.setInRom((info.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0);
            appInfos.add(appInfo);
        }
        return appInfos;
    }

    /**
     * 获取系统内置应用安装信息
     * @param context
     * @return
     */
    public static List<AppInfo> getSystemAppInfos(Context context) {
        List<AppInfo> appInfos = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo info : installedPackages) {
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                continue;
            AppInfo appInfo = new AppInfo();
            appInfo.setIsUserApp((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
            appInfo.setAppPackageName(info.packageName);
            appInfo.setAppIcon(info.applicationInfo.loadIcon(packageManager));
            File file = new File(info.applicationInfo.sourceDir);
            appInfo.setApkSize(file.length());
            appInfo.setAppName(info.applicationInfo.loadLabel(packageManager).toString());
            appInfo.setInRom((info.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0);
            appInfos.add(appInfo);
        }
        return appInfos;
    }

    /**
     * 描述一个应用程序的信息类
     */
    public static class AppInfo {

        private boolean tag;
        private String appName;
        private Drawable appIcon;
        private String appPackageName;
        private long apkSize;
        private boolean inRom;
        private boolean isUserApp;


        public boolean isTag() {
            return tag;
        }

        public void setTag(boolean tag) {
            this.tag = tag;
        }

        /**
         * 应用程序的名称
         *
         * @return
         */
        public String getAppName() {
            return appName;
        }

        /**
         * 应用程序的名称
         *
         * @param appName
         */
        public void setAppName(String appName) {
            this.appName = appName;
        }

        /**
         * 应用程序的图标
         *
         * @return
         */
        public Drawable getAppIcon() {
            return appIcon;
        }

        @Override
        public String toString() {
            return "AppInfo{" +
                    "appName='" + appName + '\'' +
                    ", appIcon=" + appIcon +
                    ", appPackageName='" + appPackageName + '\'' +
                    ", apkSize=" + apkSize +
                    ", inRom=" + inRom +
                    ", isUserApp=" + isUserApp +
                    '}';
        }

        /**
         * 应用程序的图标
         *
         * @param appIcon
         */
        public void setAppIcon(Drawable appIcon) {
            this.appIcon = appIcon;
        }

        /**
         * 应用程序的包名
         *
         * @return
         */
        public String getAppPackageName() {
            return appPackageName;
        }

        /**
         * 应用程序的包名
         *
         * @param appPackageName
         */
        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        /**
         * 应用程序的大小
         *
         * @return
         */
        public long getApkSize() {
            return apkSize;
        }

        /**
         * 应用程序的大小
         *
         * @param apkSize
         */
        public void setApkSize(long apkSize) {
            this.apkSize = apkSize;
        }

        /**
         * 是否安装在手机内存中
         *
         * @return
         */
        public boolean isInRom() {
            return inRom;
        }

        /**
         * 是否安装在手机内存中
         *
         * @param inUserRom
         */
        public void setInRom(boolean inUserRom) {
            this.inRom = inUserRom;
        }

        /**
         * 是否是一个用户安装的应用
         *
         * @return
         */
        public boolean isUserApp() {
            return isUserApp;
        }

        /**
         * 是否是一个用户安装的应用
         *
         * @param isUserApp
         */
        public void setIsUserApp(boolean isUserApp) {
            this.isUserApp = isUserApp;
        }
    }
}
