package com.hdlovefork.mobilesafe.activities;

import android.os.Bundle;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.IntentUtils;

/**
 * Created by Administrator on 2015/10/13.
 */
public class Setup1Activity extends SetupBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup1);
    }

    @Override
    public void displayNextPage() {
        IntentUtils.startActivityAndFinish(this,Setup2Activity.class);
    }

    @Override
    public void displayPrevPage() {

    }
}
