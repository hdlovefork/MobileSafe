package com.hdlovefork.mobilesafe.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.AppUtils;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/10/23.
 */
public class AppManagerActivity extends AppCompatActivity {
    private static final String TAG = "AppManagerActivity";
    @Bind(R.id.tv_appmanager_free_rom)
    TextView mTvAppmanagerFreeRom;
    @Bind(R.id.tv_appmanager_free_ext)
    TextView mTvAppmanagerFreeExt;
    @Bind(R.id.lv_appmanager)
    ListView mLvAppmanager;
    @Bind(R.id.ll_appmanager_loading)
    LinearLayout mLlAppmanagerLoading;
    @Bind(R.id.tv_appmanager_title)
    TextView mTvAppmanagerTitle;
    @Bind(R.id.ll_appmanager_container)
    LinearLayout mLlAppmanagerContainer;
    private List<AppUtils.AppInfo> mUserAppInfos;
    private List<AppUtils.AppInfo> mSystemAppInfos;
    private PopupWindow mPopupWindow;
    private InnerUninstallReceiver mInnerUninstallReceiver;
    private AppInfoAdapter mAppInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        ButterKnife.bind(this);
        mAppInfoAdapter = new AppInfoAdapter();
        mLvAppmanager.setAdapter(mAppInfoAdapter);
        initView();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        mInnerUninstallReceiver = new InnerUninstallReceiver();
        registerReceiver(mInnerUninstallReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mInnerUninstallReceiver);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initView() {
        long romSize = Environment.getDataDirectory().getFreeSpace();
        mTvAppmanagerFreeRom.setText("内存可用:" + Formatter.formatFileSize(this, romSize));
        long extSize = Environment.getExternalStorageDirectory().getFreeSpace();
        mTvAppmanagerFreeExt.setText("SD卡可用：" + Formatter.formatFileSize(this, extSize));
        mLvAppmanager.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mUserAppInfos == null || mSystemAppInfos == null) return;
                if (firstVisibleItem < mUserAppInfos.size() + 1) {
                    mTvAppmanagerTitle.setText("用户应用" + mUserAppInfos.size() + "个");
                } else {
                    mTvAppmanagerTitle.setText("系统应用" + mSystemAppInfos.size() + "个");
                }
            }
        });
        //应用程序列表中的某一项被点击，弹出气泡菜单
        mLvAppmanager.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            private PopupItemClick mPopupItemClick;
            private LinearLayout mLlShare;
            private LinearLayout mLlLaunch;
            private LinearLayout mLlUninstall;
            private LinearLayout mLlInfo;

            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                //APP类型分隔符不弹气泡
                if (view instanceof TextView) return;
                //保证只有一个气泡实例
                if (mPopupWindow == null) {
                    View popupView = View.inflate(getApplicationContext(), R.layout.popup_app_manager, null);
                    mLlInfo = (LinearLayout) popupView.findViewById(R.id.ll_popup_info);
                    mLlUninstall = (LinearLayout) popupView.findViewById(R.id.ll_popup_uninstall);
                    mLlLaunch = (LinearLayout) popupView.findViewById(R.id.ll_popup_launch);
                    mLlShare = (LinearLayout) popupView.findViewById(R.id.ll_popup_share);
                    mPopupWindow = new PopupWindow(popupView, -2, -2);
                    //没有背景的话它里面的孩子不能做动画
                    mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                //注册弹出菜单项的点击事件
                mPopupItemClick = new PopupItemClick(position);
                mLlInfo.setOnClickListener(mPopupItemClick);
                mLlUninstall.setOnClickListener(mPopupItemClick);
                mLlShare.setOnClickListener(mPopupItemClick);
                mLlLaunch.setOnClickListener(mPopupItemClick);

                //添加气泡出现时显示的动画效果
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1);
                alphaAnimation.setDuration(300);
                ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(300);

                AnimationSet set = new AnimationSet(false);
                set.addAnimation(alphaAnimation);
                set.addAnimation(scaleAnimation);

                mPopupWindow.getContentView().startAnimation(set);

                //气泡默认位于父窗口的左上角，相当于将坐标系调整到左上为0，0
                //获取当前点击的项的位置，动态调整气泡相应位置
                int[] location = new int[2];
                view.getLocationInWindow(location);

                mPopupWindow.showAtLocation(mLlAppmanagerContainer, Gravity.LEFT | Gravity.TOP, 150, location[1]);
            }

            //气泡某项被点击事件
            class PopupItemClick implements View.OnClickListener {

                //被点击项的索引
                private final int mPosition;

                public PopupItemClick(int position) {
                    mPosition = position;
                }

                //气泡中的某项被点击
                @Override
                public void onClick(View v) {
                    AppUtils.AppInfo appInfo = (AppUtils.AppInfo) mLvAppmanager.getAdapter().getItem(mPosition);
                    switch (v.getId()) {
                        case R.id.ll_popup_info:
                            AppUtils.detail(AppManagerActivity.this, appInfo.getAppPackageName());
                            break;
                        case R.id.ll_popup_launch:
                            //运行应用程序
                            if (!AppUtils.launch(AppManagerActivity.this, appInfo.getAppPackageName()))
                                ToastUtils.show(AppManagerActivity.this, "启动失败");
                            break;
                        case R.id.ll_popup_uninstall:
                            if (appInfo.isUserApp()) {
                                //用户安装的应用可以被卸载
                                AppUtils.uninstall(AppManagerActivity.this, appInfo.getAppPackageName());
                            } else {
                                //系统内置的应用没有ROOT权限无法被卸载
                                ToastUtils.show(AppManagerActivity.this, "需要ROOT手机后才能卸载系统应用");
                            }
                            break;
                        case R.id.ll_popup_share:
                            //分享应用程序
                            AppUtils.share(getApplicationContext(), "我正在使用" + appInfo.getAppName() + "，真的不错，你也可以下载来试试哦！");
                            break;
                    }
                    mPopupWindow.dismiss();
                }
            }

        });
        fillData();
    }

    private void fillData() {
        mLlAppmanagerLoading.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                mUserAppInfos = AppUtils.getUserAppInfos(getApplicationContext());
                mSystemAppInfos = AppUtils.getSystemAppInfos(getApplicationContext());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAppInfoAdapter.notifyDataSetChanged();
                mLlAppmanagerLoading.setVisibility(View.GONE);
            }
        }.execute();
    }

    class AppInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mUserAppInfos == null || mSystemAppInfos == null) {
                return 0;
            }
            return mUserAppInfos.size() + mSystemAppInfos.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0 || position == mUserAppInfos.size() + 1) {
                //显示程序类型标签
                TextView textView = new TextView(getApplicationContext());
                textView.setBackgroundColor(Color.GRAY);
                textView.setTextColor(Color.WHITE);
                textView.setPadding(5, 5, 5, 5);
                textView.setTextSize(16);
                if (position == 0) {
                    textView.setText("用户应用" + mUserAppInfos.size() + "个");
                } else {
                    textView.setText("系统应用" + mSystemAppInfos.size() + "个");
                }
                return textView;
            } else {
                //显示用户或系统程序
                if (position < mUserAppInfos.size() + 1) {
                    //获取用户程序对象
                    return mUserAppInfos.get(position - 1);
                } else {
                    //获取系统程序对象
                    return mSystemAppInfos.get(position - 2 - mUserAppInfos.size());
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            Object object = getItem(position);
            if (object instanceof TextView) {
                //是一个程序类型分隔符标签
                TextView textView = (TextView) object;
                return textView;
            }
            AppUtils.AppInfo appInfo = (AppUtils.AppInfo) object;
            if (convertView == null || convertView instanceof TextView) {
                convertView = View.inflate(AppManagerActivity.this, R.layout.item_app_manager, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mIvAppmanagerIcon.setImageDrawable(appInfo.getAppIcon());
            viewHolder.mTvAppmanagerAppName.setText(appInfo.getAppName());
            viewHolder.mTvAppmanagerInstallLocation.setText(appInfo.isInRom() ? "内部存储" : "外部存储");
            viewHolder.mTvAppmanagerApkSize.setText(Formatter.formatFileSize(getApplicationContext(), appInfo.getApkSize()));
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return !(getItem(position) instanceof TextView);
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_app_managerr.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.iv_app_manager_icon)
        ImageView mIvAppmanagerIcon;
        @Bind(R.id.tv_app_manager_app_name)
        TextView mTvAppmanagerAppName;
        @Bind(R.id.tv_app_manager_install_location)
        TextView mTvAppmanagerInstallLocation;
        @Bind(R.id.tv_app_manager_apk_size)
        TextView mTvAppmanagerApkSize;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    class InnerUninstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_REMOVED:
                    uninstall(intent);
                    break;
                case Intent.ACTION_PACKAGE_ADDED:
                    fillData();
                    break;
            }
        }

        private void uninstall(Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            AppUtils.AppInfo appInfo = null;
            boolean isUserApp = false;
            for (AppUtils.AppInfo info : mUserAppInfos) {
                if (info.getAppPackageName().equals(packageName)) {
                    appInfo = info;
                    isUserApp = true;
                    break;
                }
            }
            if (appInfo == null) {
                for (AppUtils.AppInfo info : mSystemAppInfos) {
                    if (info.getAppPackageName().equals(packageName)) {
                        appInfo = info;
                        break;
                    }
                }
            }
            if (appInfo != null) {
                if (isUserApp) {
                    mUserAppInfos.remove(appInfo);
                } else {
                    mSystemAppInfos.remove(appInfo);
                }
                mAppInfoAdapter.notifyDataSetChanged();
            }
        }
    }


}
