package com.hdlovefork.mobilesafe.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.hdlovefork.mobilesafe.R;

/**
 * Created by Administrator on 2015/10/13.
 */
public abstract class SetupBaseActivity extends AppCompatActivity {
    private static final String TAG = "SetupBaseActivity";
    protected SharedPreferences mSharedPreferences;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "onFling() called with: " + "velocityX = [" + velocityX + "], velocityY = [" + velocityY + "]");
                float diffx = Math.abs(e1.getX() - e2.getX());
                float diffy = Math.abs(e1.getY() - e2.getY());
                if (diffx > diffy) {
                    //横向滑动屏幕
                    if (e1.getX() - e2.getX() > 0) {
                        //向左滑动屏幕
                        next(null);
                    } else {
                        //向右滑动屏幕
                        prev(null);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 子类使用Activity显示下一页，父类负责做动画切换效果
     */
    public abstract void displayNextPage();

    /**
     * 子类使用Activity显示上一页，父类负责做动画切换效果
     */
    public abstract void displayPrevPage();

    /**
     * 下一步按钮点击事件，在XML文件中直接定义了事件处理函数
     * @param view
     */
    public void next(View view) {
        displayNextPage();
        //动画处理
        overridePendingTransition(R.anim.next_page_in, R.anim.next_page_out);
    }

    /**
     *  上一步按钮点击事件，在XML文件中直接定义了事件处理函数
     * @param view
     */
    public void prev(View view) {
        displayPrevPage();
        //动画处理
        overridePendingTransition(R.anim.prev_page_in, R.anim.prev_page_out);
    }
}
