package com.hdlovefork.mobilesafe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.services.AppLockService;
import com.hdlovefork.mobilesafe.utils.Md5Utils;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 程序锁功能配套的解锁界面,当用户打开被锁定的应用时弹出这个界面
 */
public class EnterPwdActivity extends AppCompatActivity {
    @Bind(R.id.iv_icon)
    ImageView mIvIcon;
    @Bind(R.id.et_dialog_enter_password)
    EditText mEtDialogEnterPassword;
    @Bind(R.id.tv_app_name)
    TextView mTvAppName;
    private String mPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pwd);
        ButterKnife.bind(this);
        //获取当前被锁定的应用包名,该值是由程序锁服务(AppLockService)传递过来的
        //程序锁服务监视到这个应用需要解锁才能使用
        mPackageName = getIntent().getStringExtra("package_name");
        PackageManager packageManager = getPackageManager();
        try {
            //设置被锁定程序的图标和名称
            //用于提示用户哪个应用被锁定了现在需要输入在程序锁功能界面设置的解锁密码才能使用
            PackageInfo packageInfo = packageManager.getPackageInfo(mPackageName, 0);
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            mIvIcon.setImageDrawable(icon);
            String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            mTvAppName.setText(appName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @OnClick(R.id.bt_dialog_enter_ok)
    public void clickOK(View view) {
        String inputPwd = mEtDialogEnterPassword.getText().toString();
        if (TextUtils.isEmpty(inputPwd)) {
            ToastUtils.show(EnterPwdActivity.this,"密码不能为空");
            return;
        }
        inputPwd= Md5Utils.encode(inputPwd);
        //如果密码正确就关闭这个对话框
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        String configPwd = sharedPreferences.getString("app_lock_pwd", null);
        if(inputPwd.equals(configPwd)){
            //用户输入了正确的密码,结束本对话框,发个广播临时放行
            Intent intent=new Intent(AppLockService.ACTION_GRANT_APP);
            intent.putExtra("package_name",mPackageName);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            finish();
        }else{
            ToastUtils.show(EnterPwdActivity.this,"您输入的密码不正确");
        }
    }

    @Override
    public void onBackPressed() {
        //按返回按钮回到桌面
        showDesktop();
    }

    @OnClick(R.id.bt_dialog_enter_cancel)
    public void clickCancel(View view) {
        //点击取消按钮后回到桌面
        showDesktop();
    }

    //显示桌面
    private void showDesktop(){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.MONKEY");
        startActivity(intent);
    }
}
