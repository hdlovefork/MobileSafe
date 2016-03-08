package com.hdlovefork.mobilesafe.activities;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.receiver.MyAdminReceiver;
import com.hdlovefork.mobilesafe.ui.SettingCheckView;
import com.hdlovefork.mobilesafe.utils.AdminPermission;
import com.hdlovefork.mobilesafe.utils.IntentUtils;

/**
 * Created by Administrator on 2015/10/13.
 */
public class Setup4Activity extends SetupBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);
        boolean protecting = mSharedPreferences.getBoolean("protecting",false);
        SettingCheckView sckProtecting = (SettingCheckView) findViewById(R.id.tv_setup4_status);
        //回显保护状态
        sckProtecting.setChecked(protecting);
        sckProtecting.setOnCheckedChangeListener(new SettingCheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SettingCheckView view, boolean isChecked) {
                SharedPreferences.Editor editor=mSharedPreferences.edit();
                editor.putBoolean("protecting",isChecked);
                editor.commit();
            }
        });
    }

    @Override
    public void displayNextPage() {
        DevicePolicyManager devicePolicyManager= (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName componentName=new ComponentName(this, MyAdminReceiver.class);
        if(!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(this, "请开启管理员权限后再点击完成", Toast.LENGTH_SHORT).show();
            return;
        }
        //设置完成 写入配置文件
        SharedPreferences.Editor editor=mSharedPreferences.edit();
        editor.putBoolean("finish_setup",true);
        editor.commit();
        IntentUtils.startActivityAndFinish(this, LostFindActivity.class);
    }

    @Override
    public void displayPrevPage() {
        IntentUtils.startActivityAndFinish(this, Setup3Activity.class );
    }

    public void active(View view){
        DevicePolicyManager devicePolicyManager= (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName componentName=new ComponentName(this, MyAdminReceiver.class);
        AdminPermission.active(devicePolicyManager, this, componentName);
    }

    public void uninstall(View view){
        AdminPermission.uninstall(this);
    }
}
