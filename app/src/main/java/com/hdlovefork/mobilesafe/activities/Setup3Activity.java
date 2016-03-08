package com.hdlovefork.mobilesafe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.IntentUtils;

/**
 * Created by Administrator on 2015/10/13.
 */
public class Setup3Activity extends SetupBaseActivity {

    private EditText mEtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup3);
        mEtPhone = (EditText) findViewById(R.id.et_setup3_phone);
        //显示上次保存的号码
        mEtPhone.setText(mSharedPreferences.getString("phone", ""));
    }

    @Override
    public void displayNextPage() {
        String phone = mEtPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "安全号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //保存安全号码
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("phone", phone);
        editor.commit();
        IntentUtils.startActivityAndFinish(this, Setup4Activity.class);
    }

    @Override
    public void displayPrevPage() {
        String phone = mEtPhone.getText().toString();
        if (!TextUtils.isEmpty(phone)){
            //保存安全号码
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("phone", phone);
            editor.commit();
        }

        IntentUtils.startActivityAndFinish(this, Setup2Activity.class);
    }

    //点击了选择联系人
    public void selectContact(View view){
        Intent intent=new Intent(this,ContactActivity.class);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null) {
            String phone = data.getStringExtra("phone");
            mEtPhone.setText(phone.replace("-", "").trim());
        }
    }
}
