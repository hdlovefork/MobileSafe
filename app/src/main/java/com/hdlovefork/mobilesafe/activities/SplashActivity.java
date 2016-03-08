package com.hdlovefork.mobilesafe.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.services.UpdateService;
import com.hdlovefork.mobilesafe.utils.IntentUtils;
import com.hdlovefork.mobilesafe.utils.PackageUtils;
import com.hdlovefork.mobilesafe.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * 这是一个启动窗体，用于展示LOGO和检测程序是否有新版本
 */
public class SplashActivity extends AppCompatActivity {
    public static final String TAG = "SplashActivity";
    TextView mTvVersion;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mTvVersion = (TextView) findViewById(R.id.tv_splash_version);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_splash_progress);
        mTvVersion.setText("当前版本：" + PackageUtils.getVersionName(this));
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        //判断是否生成过快捷方式
        boolean shortCut=sharedPreferences.getBoolean("shortcut",false);
        if(!shortCut) {
            //createShortCut();
            //下次不再创建快捷方式
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("shortcut",true);
            edit.commit();
        }
        copyDB();
        boolean update = sharedPreferences.getBoolean(getString(R.string.config_update), false);
        if (update) {
            Log.d("SplashActivity", "检查版本更新");
            checkUpdate();
        } else {
            Log.d("SplashActivity", "不需要更新");
            IntentUtils.startActivityForDelayAndFinish(this, HomeActivity.class, 1000);
        }

    }

    //创建快捷方式
    private void createShortCut() {
        //系统生成快捷键方式的广播打开
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //设置在桌面显示的名称
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "黑马卫士");
        //设置在桌面生成的图标
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));
        //用户点击快捷方式后打开的Activity
        Intent i = new Intent(HomeActivity.ACTION_LAUNCH);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,i);
        //向系统发送广播生成快捷方式
        sendBroadcast(intent);
    }

    //拷贝address.db文件到Files目录下
    private void copyDB() {
        final File outFile = new File(getFilesDir(), "address.db");
        if (outFile.exists() && outFile.length() > 0) {
            //文件已经存在直接退出
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream in = getAssets().open("address.db");
                    OutputStream out = new FileOutputStream(outFile);
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void checkUpdate() {
        new AsyncTask<Void, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                try {
                    //获取服务器更新配置文件信息返回获取到的JSON对象
                    //失败返回NULL
                    publishProgress(10);
                    URL url = new URL(getString(R.string.serverAddr));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(2000);
                    conn.setConnectTimeout(2000);
                    conn.setRequestMethod("GET");
                    int status = conn.getResponseCode();
                    publishProgress(30);
                    if (status == 200) {
                        publishProgress(50);
                        InputStream inputStream = conn.getInputStream();
                        String json = StringUtils.ReadStream(inputStream);
                        publishProgress(80);
                        JSONObject jsonObject = new JSONObject(json);
                        inputStream.close();
                        publishProgress(100);
                        return jsonObject;
                    }
                } catch (MalformedURLException e) {
                    Log.d(TAG, e.toString());
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(final JSONObject jsonObject) {
                try {
                    if (jsonObject == null) {
                        Toast.makeText(SplashActivity.this, "获取升级信息失败", Toast.LENGTH_SHORT).show();
                        IntentUtils.startActivityAndFinish(SplashActivity.this, HomeActivity.class);
                    } else {
                        int newVer = jsonObject.getInt("version");
                        if (PackageUtils.getVersionCode(SplashActivity.this) < newVer) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
                            alertDialog.setTitle("升级").setMessage("发现新版本，是否下载？").setPositiveButton("马上下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    IntentUtils.startActivityAndFinish(SplashActivity.this, HomeActivity.class);
                                    Intent updateIntent = new Intent(SplashActivity.this, UpdateService.class);
                                    try {
                                        Log.d(TAG, "下载地址是：" + jsonObject.getString("downloadurl"));
                                        updateIntent.putExtra("downloadurl", jsonObject.getString("downloadurl"));
                                        startService(updateIntent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    IntentUtils.startActivityAndFinish(SplashActivity.this, HomeActivity.class);
                                }
                            }).show();
                        } else {
                            IntentUtils.startActivityForDelayAndFinish(SplashActivity.this, HomeActivity.class, 1000);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mProgressBar.setProgress(values[0]);
            }
        }.execute();
    }
}
