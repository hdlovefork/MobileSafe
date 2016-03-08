package com.hdlovefork.mobilesafe.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.ui.SettingCheckView;
import com.hdlovefork.mobilesafe.utils.IntentUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/10/13.
 */
public class Setup2Activity extends SetupBaseActivity {
    @Bind(R.id.scv_setup2_bind)
    SettingCheckView mScvSetup2Bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
        ButterKnife.bind(this);
        //将上次保存的SIM卡号读出来，不为空的话就打上勾
        String sim=mSharedPreferences.getString("sim", null);
        Log.d("Setup2Activity", "sim号码为：" + sim);
        mScvSetup2Bind.setChecked(!TextUtils.isEmpty(sim));
        mScvSetup2Bind.setOnCheckedChangeListener(new SettingCheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SettingCheckView view, boolean isChecked) {
                //保存SIM卡号
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if(isChecked){
                    TelephonyManager tm= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    String sim = tm.getSimSerialNumber();
                    editor.putString("sim",sim);
                }else{
                    editor.putString("sim",null);
                }
                editor.commit();
            }
        });
    }

    @Override
    public void displayNextPage() {
        if(mScvSetup2Bind.isChecked()) {
            IntentUtils.startActivityAndFinish(this, Setup3Activity.class);
        }else {
            Toast.makeText(this, "请勾选绑定SIM卡，否则不能进入下一步操作", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void displayPrevPage() {
        IntentUtils.startActivityAndFinish(this, Setup1Activity.class);
    }
}
