package com.hdlovefork.mobilesafe.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.activities.HomeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2015/10/10.
 */
public class UpdateService extends Service {
    //下载状态
    private final static int DOWNLOAD_COMPLETE = 0;
    private final static int DOWNLOAD_FAIL = 1;

    //文件存储
    private File updateFile = null;

    //通知栏
    private NotificationManager updateNotificationManager = null;
    private Notification updateNotification = null;
    //通知栏跳转Intent
//    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;
    private RemoteViews mRemoteViews;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取传值
        //创建文件
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
            updateFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "xxx.apk");
        }

        Intent updateIntent = new Intent(this, HomeActivity.class);
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.updateNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notify_content);
        mRemoteViews.setTextViewText(R.id.tv_notify_content, "开始下载");
        mRemoteViews.setProgressBar(R.id.pb_nofity_progress, 100, 0, false);
        this.updateNotification = builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContent(mRemoteViews)
                .setAutoCancel(false)
                .setTicker("开始下载")
                .setContentIntent(updatePendingIntent)
                .getNotification();


        //设置下载过程中，点击通知栏，回到主界面
        //设置通知栏显示内容
        //updateNotification.setLatestEventInfo(this, "手机卫士", "0%", updatePendingIntent);
        //发出通知
        updateNotificationManager.notify(0, updateNotification);

        //开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
        new Thread(new UpdateRunnable(intent.getStringExtra("downloadurl"))).start();//这个是下载的重点，是下载的过程

        return super.onStartCommand(intent, flags, startId);
    }

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Notification.Builder builder = new Notification.Builder(UpdateService.this);
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    //点击安装PendingIntent
                    Uri uri = Uri.fromFile(updateFile);
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.addCategory("android.intent.category.DEFAULT");
                    installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                    updatePendingIntent = PendingIntent.getActivity(UpdateService.this, 0, installIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mRemoteViews.setTextViewText(R.id.tv_notify_content, "下载完成，点击安装");
                    mRemoteViews.setProgressBar(R.id.pb_nofity_progress, 100,100, false);
                    updateNotification = builder
                            .setAutoCancel(true)
                            .setContent(mRemoteViews)
                            .setTicker("下载完成")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(updatePendingIntent)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .getNotification();
                    updateNotificationManager.notify(0, updateNotification);

                    //停止服务
                    stopSelf();
                    break;
                case DOWNLOAD_FAIL:
                    //下载失败
                    mRemoteViews.setTextViewText(R.id.tv_notify_content, "下载失败");
                    updateNotification = builder
                            .setAutoCancel(true)
                            .setContent(mRemoteViews)
                            .setTicker("下载失败")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(updatePendingIntent)
                            .getNotification();
                    updateNotificationManager.notify(0, updateNotification);
                    break;
                default:
                    stopSelf();
            }
        }
    };

    class UpdateRunnable implements Runnable {
        private String mDownloadUrl = "";

        public UpdateRunnable(String downloadUrl) {
            mDownloadUrl = downloadUrl;
        }

        Message message = updateHandler.obtainMessage();

        public void run() {
            message.what = DOWNLOAD_COMPLETE;
            try {
                if (!updateFile.exists()) {
                    updateFile.createNewFile();
                }
                //下载函数，以QQ为例子
                //增加权限;
                boolean downloadSuccess = downloadUpdateFile(mDownloadUrl, updateFile);
                if (downloadSuccess) {
                    //下载成功
                    updateHandler.sendMessage(message);
                }else{
                    message.what=DOWNLOAD_FAIL;
                    updateHandler.sendMessage(message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                message.what = DOWNLOAD_FAIL;
                updateHandler.sendMessage(message);
            }
        }

        public boolean downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {
            //这样的下载代码很多，我就不做过多的说明
            int downloadCount = 0;
            int currentSize = 0;
            long totalSize = 0;
            int updateTotalSize = 0;

            HttpURLConnection httpConnection = null;
            InputStream is = null;
            FileOutputStream fos = null;

            try {
                URL url = new URL(downloadUrl);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
                if (currentSize > 0) {
                    httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
                }
                httpConnection.setConnectTimeout(10000);
                httpConnection.setReadTimeout(20000);
                updateTotalSize = httpConnection.getContentLength();
                if (httpConnection.getResponseCode() == 404) {
                    throw new Exception("fail!");
                }
                is = httpConnection.getInputStream();
                fos = new FileOutputStream(saveFile, false);
                byte buffer[] = new byte[4096];
                int readsize = 0;
                while ((readsize = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, readsize);
                    totalSize += readsize;
                    //为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
                    if ((downloadCount == 0) || (int) (totalSize * 100 / updateTotalSize) - 10 > downloadCount) {
                        downloadCount += 10;
                        Notification.Builder builder = new Notification.Builder(UpdateService.this);
                        mRemoteViews.setTextViewText(R.id.tv_notify_content, "已下载："+(int) totalSize * 100 / updateTotalSize + "%");
                        mRemoteViews.setProgressBar(R.id.pb_nofity_progress,100,(int) totalSize * 100 / updateTotalSize,false);
                        updateNotification = builder
                                .setContent(mRemoteViews)
                                .setTicker("正在下载")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentIntent(updatePendingIntent)
                                .getNotification();
//                        updateNotification.setLatestEventInfo(UpdateService.this, "正在下载", (int) totalSize * 100 / updateTotalSize + "%", updatePendingIntent);
                        updateNotificationManager.notify(0, updateNotification);
                        Thread.sleep(1000);
                    }
                }
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            return totalSize==updateTotalSize && totalSize != 0;
        }
    }


}
