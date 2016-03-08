package com.hdlovefork.mobilesafe.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;

/**
 * Created by Administrator on 2015/10/12.
 */
public class SettingCheckView extends LinearLayout implements View.OnClickListener {

    private CheckBox mCbStatus;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public SettingCheckView(Context context) {
        this(context, null);
    }

    public SettingCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.ui_settingcheckview, this);
        setOrientation(VERTICAL);
        setBackgroundResource(R.drawable.settingview_bg);
        setOnClickListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingCheckView);
        //读取布局配置中的标题设置
        String title = a.getString(R.styleable.SettingCheckView_title);
        //读取布局配置默认显示分隔线
        boolean allowDivider = a.getBoolean(R.styleable.SettingCheckView_allowDivider, true);

        TextView tvTitle = (TextView) findViewById(R.id.tv_ui_settingcheckview_title);
        ImageView ivDivider = (ImageView) findViewById(R.id.iv_ui_settingcheckview_divider);
        mCbStatus = (CheckBox) findViewById(R.id.cb_ui_settingcheckview_status);
        //设置标题
        tvTitle.setText(title);
        //控件布局设置中指定不显示分隔线
        if (!allowDivider)
            ivDivider.setVisibility(View.GONE);
        a.recycle();
    }

    /**
     * 该选项是否已经被选中
     *
     * @return
     */
    public boolean isChecked() {
        return mCbStatus.isChecked();
    }

    /**
     * 设置选项为选中状态
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        mCbStatus.setChecked(checked);
    }

    /**
     * 设置选中后的回调方法
     *
     * @param listener
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    //选项被点击后更改复选框状态，同时引发选中状态被更改事件
    @Override
    public void onClick(View v) {
        boolean checked = isChecked();
        checked = !checked;
        mCbStatus.setChecked(checked);
        if (mOnCheckedChangeListener != null)
            mOnCheckedChangeListener.onCheckedChanged(this, checked);
    }

    /**
     * 选中状态更改事件定义
     */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(SettingCheckView view, boolean isChecked);
    }
}
