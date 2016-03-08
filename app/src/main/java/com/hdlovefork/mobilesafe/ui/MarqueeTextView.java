package com.hdlovefork.mobilesafe.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/10/11.
 */
public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setSingleLine();
        setFocusableInTouchMode(true);
        setFocusable(true);
    }

    public MarqueeTextView(Context context) {
        this(context,null);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
