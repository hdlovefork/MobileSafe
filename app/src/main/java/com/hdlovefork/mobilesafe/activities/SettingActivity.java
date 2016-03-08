package com.hdlovefork.mobilesafe.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.services.AddressTipService;
import com.hdlovefork.mobilesafe.services.AppLockService;
import com.hdlovefork.mobilesafe.services.TelInterceptService;
import com.hdlovefork.mobilesafe.ui.SettingChangeDescView;
import com.hdlovefork.mobilesafe.ui.SettingCheckView;
import com.hdlovefork.mobilesafe.utils.ServiceUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 设置中心
 */
public class SettingActivity extends AppCompatActivity implements SettingCheckView.OnCheckedChangeListener, SettingChangeDescView.DialogListener {

    @Bind(R.id.scv_allow_update)
    SettingCheckView mScvAllowUpdate;
    @Bind(R.id.scv_allow_intercept_service)
    SettingCheckView mScvAllowInterceptService;
    @Bind(R.id.scv_address_tip)
    SettingCheckView mScvAddressTipService;
    @Bind(R.id.scv_address_style)
    SettingChangeDescView mScvAddressStyle;
    @Bind(R.id.scv_app_lock)
    SettingCheckView mScvAppLock;
    private SharedPreferences mSharedPreferences;
    private String[] mAddrBoxStyleItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mAddrBoxStyleItems = getResources().getStringArray(R.array.addrBoxStyleDescArray);
        ButterKnife.bind(this);
        //自动更新选项事件监听
        mScvAllowUpdate.setOnCheckedChangeListener(this);
        mScvAllowInterceptService.setOnCheckedChangeListener(this);
        mScvAddressTipService.setOnCheckedChangeListener(this);
        mScvAddressStyle.setDialogListener(this);
        mScvAppLock.setOnCheckedChangeListener(this);

        //读取上次保存的配置
        mSharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        //设置自动更新选项是否选中
        if (mSharedPreferences.getBoolean(getString(R.string.config_update), false))
            mScvAllowUpdate.setChecked(true);
        int addrBoxStyle = mSharedPreferences.getInt("addr_box_style", 0);
        addrBoxStyle = addrBoxStyle < 0 ? 0 : addrBoxStyle;
        addrBoxStyle = addrBoxStyle > mAddrBoxStyleItems.length ? 0 : addrBoxStyle;
        mScvAddressStyle.setDescText(mAddrBoxStyleItems[addrBoxStyle]);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //电话拦截服务
        if (ServiceUtils.existsService(this, TelInterceptService.class)) {
            mScvAllowInterceptService.setChecked(true);
        } else {
            mScvAllowInterceptService.setChecked(false);
        }
        //来去电归属地显示
        if (ServiceUtils.existsService(this, AddressTipService.class)) {
            mScvAddressTipService.setChecked(true);
        } else {
            mScvAddressTipService.setChecked(false);
        }
        //应用锁服务是否开启
        if(ServiceUtils.existsService(this,AppLockService.class)){
            mScvAppLock.setChecked(true);
        }else{
            mScvAppLock.setChecked(false);
        }
    }

    @Override
    public void onCheckedChanged(SettingCheckView view, boolean isChecked) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        switch (view.getId()) {
            //点击了自动更新保存到配置
            case R.id.scv_allow_update:
                editor.putBoolean(getString(R.string.config_update), isChecked);
                break;
            case R.id.scv_allow_intercept_service:
                if (isChecked) {
                    //开启拦截服务
                    Intent telInterceptService = new Intent(SettingActivity.this, TelInterceptService.class);
                    startService(telInterceptService);
                } else {
                    //停止拦截服务
                    Intent telInterceptService = new Intent(SettingActivity.this, TelInterceptService.class);
                    stopService(telInterceptService);
                }
                break;
            case R.id.scv_address_tip://来去电号码提醒
                if (isChecked) {
                    //开启来去电显示服务
                    Intent addressTipService = new Intent(SettingActivity.this, AddressTipService.class);
                    startService(addressTipService);
                } else {
                    //停止来去电显示服务
                    Intent addressTipService = new Intent(SettingActivity.this, AddressTipService.class);
                    stopService(addressTipService);
                }
                break;
            case R.id.scv_app_lock:
                if(isChecked){
                    //开启程序锁功能,启动监视服务
                    Intent intent=new Intent(this, AppLockService.class);
                    startService(intent);
                }else{
                    //关闭程序锁功能,停止监视服务
                    Intent intent=new Intent(this, AppLockService.class);
                    stopService(intent);
                }
                break;
        }
        editor.commit();
    }

    @Override
    public Dialog getDialog(SettingChangeDescView view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int styleIndex = mSharedPreferences.getInt("addr_box_style", 0);
        builder.setSingleChoiceItems(mAddrBoxStyleItems, styleIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("addr_box_style", which);
                editor.commit();
                mScvAddressStyle.setDescText(mAddrBoxStyleItems[which]);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);
        Dialog dialog = builder.create();
        return dialog;
    }
}
