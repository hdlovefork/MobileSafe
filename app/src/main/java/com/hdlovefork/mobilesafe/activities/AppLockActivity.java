package com.hdlovefork.mobilesafe.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.db.dao.AppLockDao;
import com.hdlovefork.mobilesafe.utils.AppUtils;
import com.hdlovefork.mobilesafe.utils.Md5Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/10/28.
 */
public class AppLockActivity extends AppCompatActivity {

    @Bind(R.id.tv_unlock_info)
    TextView mTvUnlockInfo;
    @Bind(R.id.lv_unlock)
    ListView mLvUnlock;
    @Bind(R.id.ll_unlock)
    LinearLayout mLlUnlock;
    @Bind(R.id.tv_lock_info)
    TextView mTvLockInfo;
    @Bind(R.id.lv_lock)
    ListView mLvLock;
    @Bind(R.id.ll_lock)
    LinearLayout mLlLock;
    @Bind(R.id.tv_unlock)
    TextView mTvUnlock;
    @Bind(R.id.tv_lock)
    TextView mTvLock;
    private AppLockDao mAppLockDao;
    private List<AppUtils.AppInfo> mLockInfos;
    private List<AppUtils.AppInfo> mUnlockInfos;
    private AppLockAdapter mLockAdapter;
    private AppLockAdapter mUnlockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        ButterKnife.bind(this);
        mAppLockDao = AppLockDao.getInstance(this);
        //获取所有活动进程
        List<AppUtils.AppInfo> infos = AppUtils.getAllAppInfos(this);
        mLockInfos = new ArrayList<>();
        mUnlockInfos = new ArrayList<>();
        for (AppUtils.AppInfo info : infos) {
            //判断已经在数据库中存在该进程,如果存在说明是加锁进程,则添加到加锁列表,否则添加到未加锁列表
            if (mAppLockDao.exists(info.getAppPackageName())) {
                //添加到加锁进程列表
                mLockInfos.add(info);
            } else {
                //添加到未加锁进程列表
                mUnlockInfos.add(info);
            }
        }
        mLockAdapter = new AppLockAdapter(this, R.layout.item_app_lock, mLockInfos, true);
        mUnlockAdapter = new AppLockAdapter(this, R.layout.item_app_lock, mUnlockInfos, false);
        mLvLock.setAdapter(mLockAdapter);
        mLvUnlock.setAdapter(mUnlockAdapter);
    }


    //点击加锁标签显示加锁列表
    @OnClick(R.id.tv_lock)
    public void clickLock(View view) {
        view.setBackgroundResource(R.drawable.tab_right_pressed);
        mTvUnlock.setBackgroundResource(R.drawable.tab_left_default);
        mLlLock.setVisibility(View.VISIBLE);
        mLlUnlock.setVisibility(View.GONE);
    }

    //点击未加锁标签显示未加锁列表
    @OnClick(R.id.tv_unlock)
    public void clickUnlock(View view) {
        view.setBackgroundResource(R.drawable.tab_left_pressed);
        mTvLock.setBackgroundResource(R.drawable.tab_right_default);
        mLlLock.setVisibility(View.GONE);
        mLlUnlock.setVisibility(View.VISIBLE);
    }

    class AppLockAdapter extends ArrayAdapter<AppUtils.AppInfo> {

        private int mResId;
        /**
         * 指示该数据集是否是加锁列表,用于更新标题
         */
        private boolean mIsLock;

        public AppLockAdapter(Context context, int resource, List<AppUtils.AppInfo> objects, boolean isLock) {
            super(context, resource, objects);
            mResId = resource;
            mIsLock = isLock;
        }

        @Override
        public int getCount() {
            int count = super.getCount();
            if (mIsLock) {
                mTvLockInfo.setText("已加锁应用:" + count + "个");
            } else {
                mTvUnlockInfo.setText("未加锁应用:" + count + "个");
            }
            return count;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), mResId, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final AppUtils.AppInfo info = getItem(position);
            viewHolder.ivIcon.setImageDrawable(info.getAppIcon());
            viewHolder.tvProcessName.setText(info.getAppName());
            //mIsLock为真代表已经加锁的应用,应该显示解锁图标
            viewHolder.ivLockIcon.setImageResource(mIsLock ? R.drawable.list_button_unlock_pressed : R.drawable.list_button_lock_pressed);
            final View finalConvertView = convertView;
            viewHolder.ivLockIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsLock) {
                        //已加锁的变成未加锁
                        //动画向左移动
                        moveAnimation(info, finalConvertView,false);
                    } else {
                        //未加锁的变成已加锁
                        //判断用户是否已经设置过解锁密码
                        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
                        String pwd = sharedPreferences.getString("app_lock_pwd", null);
                        if (TextUtils.isEmpty(pwd)) {
                            //没有设置过程序锁密码,弹出设置密码的对话框设置密码
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            View view = View.inflate(getContext(), R.layout.dialog_setup_pwd, null);
                            builder.setView(view);
                            final Dialog dialog = builder.show();
                            Button btnOK = (Button) view.findViewById(R.id.bt_dialog_setup_ok);
                            Button btnCancel = (Button) view.findViewById(R.id.bt_dialog_setup_cancel);
                            final EditText etPwd = (EditText) view.findViewById(R.id.et_dialog_setup_password);
                            final EditText etPwdConfirm = (EditText) view.findViewById(R.id.et_dialog_setup_password_confirm);
                            btnOK.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String pwd = etPwd.getText().toString();
                                    String pwdConfirm = etPwdConfirm.getText().toString();
                                    if (TextUtils.isEmpty(pwd)) {
                                        Toast.makeText(getContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (!pwd.equals(pwdConfirm)) {
                                        Toast.makeText(getContext(), "两次输入密码不一致", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //MD5加密
                                    pwd = Md5Utils.encode(pwd);
                                    //保存用户输入的密码
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("app_lock_pwd", pwd);
                                    editor.commit();
                                    dialog.dismiss();
                                    //动画向右移动
                                    moveAnimation(info, finalConvertView,true);
                                }
                            });

                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                        } else {
                            //用户已经设置过程序锁解锁密码
                            //动画向右移动
                            moveAnimation(info, finalConvertView,true);
                        }
                    }

                }
            });
            return convertView;
        }

        private void moveAnimation(AppUtils.AppInfo info, View finalConvertView, boolean isToLocked) {
            float toX = isToLocked ? 1.0f : -1.0f;
            TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, toX, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            translateAnimation.setAnimationListener(new AnimationListener(info, isToLocked));
            translateAnimation.setDuration(300);
            finalConvertView.startAnimation(translateAnimation);
        }

        //列表项动画移除完后的回调,刷新界面
        class AnimationListener implements Animation.AnimationListener {

            private final AppUtils.AppInfo mInfo;
            private final boolean mToLocked;

            /**
             * @param info       要在集合中操作的对象
             * @param isToLocked TRUE代表info对象变成加锁应用,FALSE代表info对象变成未加锁应用
             */
            public AnimationListener(AppUtils.AppInfo info, boolean isToLocked) {
                mInfo = info;
                mToLocked = isToLocked;
            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mToLocked) {
                    //从未加锁变成已加锁应用,数据库添加一条记录,修改集合内容
                    mUnlockInfos.remove(mInfo);
                    mLockInfos.add(mInfo);
                    mAppLockDao.add(mInfo.getAppPackageName());
                } else {
                    //从已加锁变成未加锁应用,数据库删除一条记录,修改集合内容
                    mLockInfos.remove(mInfo);
                    mUnlockInfos.add(mInfo);
                    mAppLockDao.delete(mInfo.getAppPackageName());
                }
                mLockAdapter.notifyDataSetChanged();
                mUnlockAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        }

    }


    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_app_lock.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.iv_icon)
        ImageView ivIcon;
        @Bind(R.id.tv_process_name)
        TextView tvProcessName;
        @Bind(R.id.iv_lock_icon)
        ImageView ivLockIcon;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
