package com.hdlovefork.mobilesafe.activities;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.IntentUtils;
import com.hdlovefork.mobilesafe.utils.ProcessUtils;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/10/25.
 */
public class ProcessManagerActivity extends AppCompatActivity {
    @Bind(R.id.tv_process_manager_process_count)
    TextView mTvProcessmanagerProcessCount;
    @Bind(R.id.tv_process_manager_memory_info)
    TextView mTvProcessmanagerMemoryInfo;
    @Bind(R.id.lv_process_manager)
    ListView mLvProcessmanager;
    @Bind(R.id.ll_process_manager_loading)
    LinearLayout mLlProcessmanagerLoading;
    @Bind(R.id.tv_process_manager_title)
    TextView mTvProcessmanagerTitle;
    @Bind(R.id.ll_appmanager_container)
    LinearLayout mLlAppmanagerContainer;
    @Bind(R.id.rl_process_manager_process_info)
    RelativeLayout mRlProcessManagerProcessInfo;
    @Bind(R.id.ll_control_panel)
    LinearLayout mLlControlPanel;
    private List<ProcessUtils.ProcessInfo> mUserProcess;
    private List<ProcessUtils.ProcessInfo> mSystemProcess;
    private ProcessAdapter mProcessAdapter;
    private long mUsedMemorySize;
    private long mTotalMemorySize;
    private int mTotalProcessCount;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_manager);
        ButterKnife.bind(this);
        mSharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        //设置ListView的适配器
        mProcessAdapter = new ProcessAdapter();
        mLvProcessmanager.setAdapter(mProcessAdapter);
        //列表滚动时显示用户/系统进程数量标签
        mLvProcessmanager.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mUserProcess == null || mSystemProcess == null)
                    return;
                if (firstVisibleItem < mUserProcess.size() + 1) {
                    //显示用户进程数量
                    mTvProcessmanagerTitle.setText("用户进程：" + mUserProcess.size() + "个");
                } else {
                    mTvProcessmanagerTitle.setText("系统进程：" + mSystemProcess.size() + "个");
                }
            }
        });
        //点击条目时选中/取消复选框勾选状态
        mLvProcessmanager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProcessUtils.ProcessInfo info = (ProcessUtils.ProcessInfo) mProcessAdapter.getItem(position);
                if (info == null)
                    return;
                //本进程不显示复选框，不能被用户清理
                if (info.getPackageName().equals(getPackageName()))
                    return;
                info.setCheck(!info.isCheck());
                mProcessAdapter.notifyDataSetChanged();
            }
        });

    }

    private void fillData() {
        //显示正在加载进度对话框
        mLlProcessmanagerLoading.setVisibility(View.VISIBLE);
        mTvProcessmanagerTitle.setVisibility(View.GONE);
        mRlProcessManagerProcessInfo.setVisibility(View.GONE);
        mLlControlPanel.setVisibility(View.GONE);
        new AsyncTask<Void, Void, Void>() {

            private String mStrTotalMemorySize;
            private String mStrUsedMemorySize;


            @Override
            protected Void doInBackground(Void... params) {
                //获取总进程数量
                List<ProcessUtils.ProcessInfo> allProcess = ProcessUtils.getAllProcess(getApplicationContext());
                mTotalProcessCount = allProcess.size();
                //获取内存占用大小
                mUsedMemorySize = ProcessUtils.getUsedMemorySize(getApplicationContext());
                mStrUsedMemorySize = Formatter.formatFileSize(getApplicationContext(), mUsedMemorySize);
                //获取总内存大小
                mTotalMemorySize = ProcessUtils.getTotalMemorySize(getApplicationContext());
                mStrTotalMemorySize = Formatter.formatFileSize(getApplicationContext(), mTotalMemorySize);
                //获取用户进程
                mUserProcess = ProcessUtils.getUserProcess(allProcess);
                //获取系统进程
                mSystemProcess = ProcessUtils.getSystemProcess(allProcess);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mTvProcessmanagerProcessCount.setText("总进程：" + mTotalProcessCount + "个");
                mTvProcessmanagerMemoryInfo.setText("已用内存/内存总量：" + mStrUsedMemorySize + "/" + mStrTotalMemorySize);
                //刷新列表
                mProcessAdapter.notifyDataSetChanged();
                //隐藏正在加载进度对话框
                mLlProcessmanagerLoading.setVisibility(View.GONE);
                mTvProcessmanagerTitle.setVisibility(View.VISIBLE);
                mRlProcessManagerProcessInfo.setVisibility(View.VISIBLE);
                mLlControlPanel.setVisibility(View.VISIBLE);
            }
        }.execute();

    }


    class ProcessAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mSystemProcess == null || mUserProcess == null)
                return 0;
            if (mSharedPreferences.getBoolean("show_system_process", true))
                return mSystemProcess.size() + mUserProcess.size() + 2;
            else
                return mUserProcess.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0 || position == mUserProcess.size() + 1) {
                TextView textView = new TextView(getApplicationContext());
                textView.setPadding(5, 5, 5, 5);
                textView.setBackgroundColor(Color.GRAY);
                textView.setTextSize(16);
                textView.setTextColor(Color.WHITE);
                String text = position == 0 ? "用户进程：" + mUserProcess.size() + "个" : "系统进程：" + mSystemProcess.size() + "个";
                textView.setText(text);
                return textView;
            }
            if (position <= mUserProcess.size()) {
                //返回用户进程
                return mUserProcess.get(position - 1);
            } else {
                return mSystemProcess.get(position - 2 - mUserProcess.size());
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object object = getItem(position);
            if (object instanceof TextView)
                return (TextView) object;
            ViewHolder viewHolder;
            if (convertView == null || convertView instanceof TextView) {
                convertView = View.inflate(ProcessManagerActivity.this, R.layout.item_process_manager, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ProcessUtils.ProcessInfo processInfo = (ProcessUtils.ProcessInfo) object;
            //设置图标
            viewHolder.mIvIcon.setImageDrawable(processInfo.getIcon());
            //设置占用内存大小文字
            String memoryInfo = "占用内存：" + Formatter.formatFileSize(getApplicationContext(), processInfo.getMemorySize());
            viewHolder.mTvCacheSize.setText(memoryInfo);
            //设置进程名称
            viewHolder.mTvProcessName.setText(processInfo.getProcessName());
            if (processInfo.getPackageName().equals(getPackageName())) {
                //本进程不显示复选框，不能被用户清理
                viewHolder.mCbClear.setVisibility(View.GONE);
            } else {
                viewHolder.mCbClear.setVisibility(View.VISIBLE);
                //设置勾选状态
                viewHolder.mCbClear.setChecked(processInfo.isCheck());
            }
            return convertView;
        }


    }

    @OnClick(R.id.bt_select_all)
    public void selectAll(View view) {
        for (ProcessUtils.ProcessInfo info : mUserProcess) {
            info.setCheck(true);
        }
        for (ProcessUtils.ProcessInfo info : mSystemProcess) {
            info.setCheck(true);
        }
        mProcessAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.bt_select_inverse)
    public void selectInverse(View view) {
        for (ProcessUtils.ProcessInfo info : mUserProcess) {
            info.setCheck(!info.isCheck());
        }
        for (ProcessUtils.ProcessInfo info : mSystemProcess) {
            info.setCheck(!info.isCheck());
        }
        mProcessAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.bt_process_clear)
    public void processClear(View view) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ProcessUtils.ProcessInfo> clearUserProcess = new ArrayList<>();
        List<ProcessUtils.ProcessInfo> clearSystemProcess = new ArrayList<>();
        //统计杀死的进程数量
        int processCount = 0;
        //统计节省的内存大小
        long saveMem = 0;
        //杀死选中的用户进程
        for (ProcessUtils.ProcessInfo info : mUserProcess) {
            if (!info.isCheck())
                continue;
            if (info.getPackageName().equals(getPackageName()))
                continue;
            activityManager.killBackgroundProcesses(info.getPackageName());
            //将杀死的用户进程记录下来，以便后续从mUserProcess中删除
            clearUserProcess.add(info);
            processCount++;
            saveMem += info.getMemorySize();
        }
        //杀死选中的系统进程
        for (ProcessUtils.ProcessInfo info : mSystemProcess) {
            if (!info.isCheck())
                continue;
            activityManager.killBackgroundProcesses(info.getPackageName());
            //将杀死的系统进程记录下来，以便后续从mSystemProcess中删除
            clearSystemProcess.add(info);
            processCount++;
            saveMem += info.getMemorySize();
        }
        //真正的从全局用户进程变量中移除
        for (ProcessUtils.ProcessInfo info : clearUserProcess) {
            mUserProcess.remove(info);
        }
        //真正的从全局系统进程变量中移除
        for (ProcessUtils.ProcessInfo info : clearSystemProcess) {
            mSystemProcess.remove(info);
        }
        //提示用户并更新列表
        ToastUtils.show(ProcessManagerActivity.this, "清理了" + processCount + "个进程，释放了" + Formatter.formatFileSize(getApplicationContext(), saveMem) + "内存空间");
        mProcessAdapter.notifyDataSetChanged();
        //更新顶部内存信息
        mTotalProcessCount -= processCount;
        mUsedMemorySize -= saveMem;
        mTvProcessmanagerProcessCount.setText("总进程：" + mTotalProcessCount + "个");
        mTvProcessmanagerMemoryInfo.setText("已用内存/内存总量：" + Formatter.formatFileSize(getApplicationContext(), mUsedMemorySize) + "/" + Formatter.formatFileSize(getApplicationContext(), mTotalMemorySize));
        //记录清理时间，下次需要隔一定时间后才能清理
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("clear_time", System.currentTimeMillis());
        editor.commit();
    }

    @OnClick(R.id.bt_open_setting)
    public void openSetting(View view) {
        IntentUtils.startActivity(this, ProcessSettingActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fillData();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_process_manager.xml.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.iv_icon)
        ImageView mIvIcon;
        @Bind(R.id.tv_process_name)
        TextView mTvProcessName;
        @Bind(R.id.tv_cache_size)
        TextView mTvCacheSize;
        @Bind(R.id.cb_clear)
        CheckBox mCbClear;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
