package com.hdlovefork.mobilesafe.activities;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.db.dao.AddressDao;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/10/20.
 */
public class AddressQueryActivity extends AppCompatActivity {
    private static final String TAG = "AddressQueryActivity";
    @Bind(R.id.et_address_query_number)
    EditText mEtAddressQueryNumber;
    @Bind(R.id.tv_address_query_result)
    TextView mTvAddressQueryResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_query);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.bt_address_query_query)
    public void queryClick(View view) {
        String number = mEtAddressQueryNumber.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            shakeEditView();
            Vibrator vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(200);
            return;
        }
        AddressDao dao = new AddressDao(this);
        String addr = dao.findAddress(number);
        mTvAddressQueryResult.setText("归属地：" + addr);
    }

    //震动输入框
    private void shakeEditView() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        mEtAddressQueryNumber.startAnimation(shake);
    }
}
