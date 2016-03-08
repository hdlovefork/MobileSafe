package com.hdlovefork.mobilesafe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.services.AutoKillService;
import com.hdlovefork.mobilesafe.ui.SettingCheckView;
import com.hdlovefork.mobilesafe.utils.ServiceUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/10/26.
 */
public class ProcessSettingActivity extends AppCompatActivity {
    @Bind(R.id.scv_show_system_process)
    SettingCheckView mScvShowSystemProcess;
    @Bind(R.id.scv_auto_kill_process)
    SettingCheckView mScvAutoKillProcess;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_setting);
        mSharedPreferences = getSharedPreferences(getString(R.string.config_file_name),MODE_PRIVATE);
        ButterKnife.bind(this);
        mScvShowSystemProcess.setChecked(mSharedPreferences.getBoolean("show_system_process", true));
        mScvAutoKillProcess.setChecked(ServiceUtils.existsService(this,AutoKillService.class));
        mScvShowSystemProcess.setOnCheckedChangeListener(new SettingCheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SettingCheckView view, boolean isChecked) {
                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putBoolean("show_system_process", isChecked);
                edit.commit();
            }
        });

        mScvAutoKillProcess.setOnCheckedChangeListener(new SettingCheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SettingCheckView view, boolean isChecked) {
                if(isChecked){
                    Intent intent=new Intent(getApplicationContext(), AutoKillService.class);
                    startService(intent);
                }else{
                    Intent intent=new Intent(getApplicationContext(), AutoKillService.class);
                    stopService(intent);
                }
            }
        });
    }
}
