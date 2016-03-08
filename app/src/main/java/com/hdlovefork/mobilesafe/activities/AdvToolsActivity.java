package com.hdlovefork.mobilesafe.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.engine.SmsBackup;
import com.hdlovefork.mobilesafe.utils.IntentUtils;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/10/20.
 */
public class AdvToolsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advtools);
        ButterKnife.bind(this);
    }

    //归属地查询
    @OnClick(R.id.tv_address_query)
    public void clickAddressQuery(View view) {
        IntentUtils.startActivity(AdvToolsActivity.this, AddressQueryActivity.class);
    }

    //短信备份
    @OnClick(R.id.tv_sms_backup)
    public void clickSmsBackup(View view) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage("正在备份...");
        dialog.setCancelable(false);
        dialog.show();
        new Thread() {
            @Override
            public void run() {
                boolean ok = SmsBackup.backupSms(AdvToolsActivity.this, "smsbackup.xml", new SmsBackup.SmsCallback() {
                    @Override
                    public void onPrepare(int max) {
                        dialog.setMax(max);
                    }

                    @Override
                    public void onProgress(int progress) {
                        dialog.setProgress(progress);
                    }
                });
                dialog.dismiss();
                ToastUtils.show(AdvToolsActivity.this, ok ? "备份成功" : "备份失败");
            }
        }.start();
    }

    //短信还原
    @OnClick(R.id.tv_sms_restore)
    public void clickSmsRestore(View view) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage("正在还原...");
        dialog.setCancelable(false);
        dialog.show();
        new Thread() {
            @Override
            public void run() {
                boolean ok = SmsBackup.restore(AdvToolsActivity.this, "smsbackup.xml", new SmsBackup.SmsCallback() {
                    @Override
                    public void onPrepare(int max) {
                        dialog.setMax(max);
                    }

                    @Override
                    public void onProgress(int progress) {
                        dialog.setProgress(progress);
                    }
                });
                dialog.dismiss();
                ToastUtils.show(AdvToolsActivity.this, ok ? "还原成功" : "还原失败");
            }
        }.start();
    }

    //程序锁
    @OnClick(R.id.tv_app_lock)
    public void clickAppLock(View view) {
        IntentUtils.startActivity(this, AppLockActivity.class);
    }
}
