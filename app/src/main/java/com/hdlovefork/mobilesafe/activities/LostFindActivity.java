package com.hdlovefork.mobilesafe.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.IntentUtils;

/**
 * 找回手机功能介绍
 */
public class LostFindActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_find);
        SharedPreferences sharedPreferences=getSharedPreferences(getString(R.string.config_file_name),MODE_PRIVATE);
        //是否开启保护
        boolean protecting=sharedPreferences.getBoolean("protecting",false);
        //安全号码
        String phone=sharedPreferences.getString("phone","");
        ImageView ivProtect = (ImageView) findViewById(R.id.iv_lostfind_status);
        if(protecting){
            ivProtect.setImageResource(R.drawable.lock);
        }else{
            ivProtect.setImageResource(R.drawable.unlock);
        }
        TextView tvPhone = (TextView) findViewById(R.id.tv_lostfind_number);
        tvPhone.setText(phone);
    }

    //点击重新进入设置向导按钮
    public void reEntrySetup(View view){
        IntentUtils.startActivityAndFinish(this,Setup1Activity.class);
    }
}
