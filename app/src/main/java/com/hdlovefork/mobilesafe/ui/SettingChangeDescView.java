package com.hdlovefork.mobilesafe.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/10/21.
 */
public class SettingChangeDescView extends LinearLayout implements View.OnClickListener {

    private DialogListener mDialogListener;

    @Bind(R.id.tv_ui_settingchangeview_title)
    TextView mTvUiSettingchangeviewTitle;
    @Bind(R.id.tv_ui_settingchangeview_desc)
    TextView mTvUiSettingchangeviewDesc;
    @Bind(R.id.iv_ui_settingview_divider)
    ImageView mIvUiSettingviewDivider;

    public SettingChangeDescView(Context context) {
        this(context, null);
    }

    public SettingChangeDescView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.ui_settingchangedescview, this);
        ButterKnife.bind(this);
        setOrientation(VERTICAL);
        setBackgroundResource(R.drawable.settingview_bg);
        setOnClickListener(this);
        //获取自定义属性信息
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingChangeDescView);
        //设置界面的标题
        String title = a.getString(R.styleable.SettingCheckView_title);
        //是否有分隔线
        boolean allowDivider = a.getBoolean(R.styleable.SettingChangeDescView_allowDivider, true);
        //标题下面的描述文字
        String desc = a.getString(R.styleable.SettingChangeDescView_desc);
        //设置标题
        mTvUiSettingchangeviewTitle.setText(title);
        //设置描述
        mTvUiSettingchangeviewDesc.setText(desc);
        //获取自定义布局视图
        if (!allowDivider) {
            //隐藏分隔条
            mIvUiSettingviewDivider.setVisibility(GONE);
        }
        a.recycle();
    }

    /**
     * 设置描述文本信息
     *
     * @param text
     */
    public void setDescText(String text) {
        mTvUiSettingchangeviewDesc.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (mDialogListener != null) {
            Dialog dialog = mDialogListener.getDialog(this);
            dialog.show();
        }
    }


    /**
     * 设置显示对话框事件
     *
     * @param dialogListener
     */
    public void setDialogListener(DialogListener dialogListener) {
        mDialogListener = dialogListener;
    }

    public interface DialogListener {
        /**
         * 当需要显示对话框时通知
         *
         * @param view
         */
        Dialog getDialog(SettingChangeDescView view);
    }


}
