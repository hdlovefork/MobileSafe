package com.hdlovefork.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 点击时自动改变图片透明度
 */
public class ClickAlphaImageView extends ImageView {
    private boolean mIsDown=true;

    public ClickAlphaImageView(Context context) {
        super(context);
    }

    public ClickAlphaImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if(mIsDown) {
            this.setAlpha(50);
        }
        else {
            this.setAlpha(255);
        }
        mIsDown=!mIsDown;
    }
}
