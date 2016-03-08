package com.hdlovefork.mobilesafe.activities;

import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.AppUtils;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/11/1.
 */
public class CacheCleanActivity extends AppCompatActivity {
    private static final int STATE_PROGRESS = 1;
    private static final int STATE_ADD_CACHE = 2;
    private static final int STATE_DELETE_CACHE = 3;
    private static final String TAG = "CacheCleanActivity";
    @Bind(R.id.lv_cache)
    ListView mLvCache;
    @Bind(R.id.pb_progress)
    ProgressBar mPbProgress;
    @Bind(R.id.tv_scan_info)
    TextView mTvScanInfo;
    @Bind(R.id.rl_panel_info)
    RelativeLayout mRlPanelInfo;
    @Bind(R.id.tv_cache_size)
    TextView mTvCacheSize;
    @Bind(R.id.ll_panel_control)
    LinearLayout mLlPanelControl;

    private List<CacheInfo> mCacheInfoList;
    private PackageManager mPackageManager;
    private CacheSizeObserver mCacheSizeObserver;
    private CacheCleanAdapter mAdapter;
    private int mTotalProgress;
    private int mProgress;
    private long mTotalCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_clean);
        ButterKnife.bind(this);
        mCacheInfoList = new ArrayList<>();

        mPackageManager = getPackageManager();
        mCacheSizeObserver = new CacheSizeObserver();
        mAdapter = new CacheCleanAdapter(this, R.layout.item_cache_clean, mCacheInfoList);
        mLvCache.setAdapter(mAdapter);
        //故意产生缓存数据
        File file = new File(getCacheDir(), "cache");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write("cxsdfsdfsdcxvwswerxcgfhgwerwerw".getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fillData();
    }

    private void fillData() {
        //清除已经获取到的缓存信息
        mCacheInfoList.clear();
        //隐藏清理面板
        mLlPanelControl.setVisibility(View.GONE);
        new Thread() {
            @Override
            public void run() {
                try {
                    Method getPackageSizeInfo = PackageManager.class.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(0);
                    //初始化进度,用于进度条的同步更新
                    mTotalProgress = installedPackages.size();
                    Log.d("CacheCleanActivity", "总进度:" + mTotalProgress);
                    mPbProgress.setMax(mTotalProgress);
                    mProgress = 0;
                    mTotalCache = 0;
                    for (PackageInfo info : installedPackages) {
                        getPackageSizeInfo.invoke(mPackageManager, info.applicationInfo.packageName, mCacheSizeObserver);
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    class CacheCleanAdapter extends ArrayAdapter<CacheInfo> {

        private int mResId;

        public CacheCleanAdapter(Context context, int resource, List<CacheInfo> objects) {
            super(context, resource, objects);
            mResId = resource;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                Log.d("CacheCleanAdapter", "getContext():" + getContext());
                Log.d("CacheCleanAdapter", "getApplicationContext():" + getApplicationContext());
                convertView = View.inflate(CacheCleanActivity.this, mResId, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final CacheInfo cacheInfo = getItem(position);
            viewHolder.mIvIcon.setImageDrawable(cacheInfo.icon);
            viewHolder.mTvCacheSize.setText("缓存大小:" + Formatter.formatFileSize(getContext(), cacheInfo.cacheSize));
            viewHolder.mTvProcessName.setText(cacheInfo.appName);
            viewHolder.mIvClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppUtils.detail(CacheCleanActivity.this, cacheInfo.pkgName);
                }
            });

            return convertView;

        }
    }

    //清理所有缓存
    @OnClick(R.id.bt_clear_all)
    public void clickClearAll(View view) {
        try {
            Method freeStorageAndNotify = PackageManager.class.getDeclaredMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);
            freeStorageAndNotify.invoke(mPackageManager, Integer.MAX_VALUE, new ClearCacheObserver());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged() called with: " + "newConfig = [" + newConfig + "]");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fillData();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_PROGRESS:
                    //设置进度条上面的应用名
                    mTvScanInfo.setText("正在扫描:" + (msg.obj == null ? "未知应用" : msg.obj));
                    //设置进度条值
                    break;
                case STATE_ADD_CACHE:
                    //扫描到新缓存时更新界面
                    //当前进度小于总进度时显示进度面板
                    CacheInfo cacheInfo = (CacheInfo) msg.obj;
                    //设置进度条值
                    mCacheInfoList.add(cacheInfo);
                    //设置进度条上面的应用名
                    mTvScanInfo.setText("正在扫描:" + cacheInfo.appName);
                    mAdapter.notifyDataSetChanged();
                    break;
                case STATE_DELETE_CACHE:
                    mAdapter.notifyDataSetChanged();
                    ToastUtils.show(CacheCleanActivity.this, "缓存清理完毕!");
                    break;
            }
            //设置进度
            mPbProgress.setProgress(mProgress);
            //当前进度小于总进度时显示进度面板,否则隐藏
            mRlPanelInfo.setVisibility(mProgress < mTotalProgress ? View.VISIBLE : View.GONE);
            if (mProgress >= mTotalProgress) {
                //显示待清理缓存总大小
                if (mTotalCache > 0) {
                    //有可清除的缓存时显示清理面板
                    mLlPanelControl.setVisibility(View.VISIBLE);
                    mTvCacheSize.setText("共扫描到" + mCacheInfoList.size() + "项缓存数据,总大小:" + Formatter.formatFileSize(getApplicationContext(), mTotalCache));
                } else {
                    //有可清除的缓存时显示清理
                    mLlPanelControl.setVisibility(View.GONE);
                    mTvCacheSize.setText("暂无可清理的缓存");
                }
            }
        }
    };

    //删除缓存回调
    class ClearCacheObserver extends IPackageDataObserver.Stub {
        @Override
        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
            mCacheInfoList.clear();
            mProgress = 0;
            mTotalProgress = 0;
            mTotalCache = 0;
            Message message = Message.obtain();
            message.what = STATE_DELETE_CACHE;
            mHandler.sendMessage(message);
        }
    }

    //查询缓存回调
    class CacheSizeObserver extends IPackageStatsObserver.Stub {


        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            try {
                Message message = Message.obtain();
                //进度原子加1
                synchronized (mCacheSizeObserver) {
                    mProgress++;
                }
                //没有缓存的情况不能清除,只更新信息面板
                if (pStats.cacheSize == 0) {
                    message.what = STATE_PROGRESS;
                    message.obj = mPackageManager.getApplicationInfo(pStats.packageName, 0).loadLabel(mPackageManager).toString();
                    mHandler.sendMessage(message);
                    return;
                }
                CacheInfo cacheInfo = new CacheInfo();
                cacheInfo.icon = mPackageManager.getApplicationInfo(pStats.packageName, 0).loadIcon(mPackageManager);
                cacheInfo.appName = mPackageManager.getApplicationInfo(pStats.packageName, 0).loadLabel(mPackageManager).toString();
                cacheInfo.cacheSize = pStats.cacheSize;
                cacheInfo.pkgName = pStats.packageName;
                cacheInfo.dataSize = pStats.dataSize;
                cacheInfo.codeSize = pStats.codeSize;
                //累计缓存总大小
                synchronized (mCacheSizeObserver) {
                    mTotalCache += cacheInfo.cacheSize;
                }
                //发消息更新ListView
                message.what = STATE_ADD_CACHE;
                message.obj = cacheInfo;
                mHandler.sendMessage(message);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    class CacheInfo {
        public String appName;
        public String pkgName;
        public Drawable icon;
        public long dataSize;
        public long codeSize;
        public long cacheSize;

    }

    static class ViewHolder {
        @Bind(R.id.iv_icon)
        ImageView mIvIcon;
        @Bind(R.id.tv_process_name)
        TextView mTvProcessName;
        @Bind(R.id.tv_cache_size)
        TextView mTvCacheSize;
        @Bind(R.id.iv_clear)
        ImageView mIvClear;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
