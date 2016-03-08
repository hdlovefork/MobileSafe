package com.hdlovefork.mobilesafe.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.db.dao.BlackListDao;
import com.hdlovefork.mobilesafe.domain.BlackList;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/10/16.
 */
public class BlackListActivity extends AppCompatActivity {
    private static final String[] MODE_LIST = {"", "短信拦截", "电话拦截", "全部拦截"};
    @Bind(R.id.lv_blacklist)
    ListView mLvBlacklist;
    @Bind(R.id.ll_blacklist_tip)
    LinearLayout mLlBlacklistTip;
    private ArrayAdapter<BlackList> mAdapter;
    BlackListDao mDao;
    private List<BlackList> mBlackLists;
    //当前页，从0开始~总页数-1
    private int mPageCur = 0;
    //每页数量
    private static final int PAGE_COUNT = 20;
    //总页数从1开始
    private int mPageTotal;
    //向下翻页
    private boolean mPageDown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
        ButterKnife.bind(this);
        mBlackLists = new ArrayList<>();
        mDao = new BlackListDao(this);
        int recordCount = mDao.getRows();
        Log.d("BlackListActivity", "总记录数是" + recordCount);
        mPageTotal = (recordCount + PAGE_COUNT - 1) / PAGE_COUNT;
        Log.d("BlackListActivity", "总页数是" + mPageTotal);
        mAdapter = new MyAdapter(this, R.layout.item_blacklist, mBlackLists);
        mLvBlacklist.setAdapter(mAdapter);
        //查询数据库显示黑名单号码
        fillData(0);
        //现在应该到达第2页了
        mLvBlacklist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (mLvBlacklist.getLastVisiblePosition() == mBlackLists.size() - 1) {
                        if (mPageCur >= mPageTotal - 1) {
                            ToastUtils.show(BlackListActivity.this, "没有更多数据了！");
                        } else {
                            fillData(++mPageCur);
                        }
                    }
                }
            }

            //不需要实现方法体
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mAdapter.notifyDataSetChanged();
            //隐藏拼命加载中
            mLlBlacklistTip.setVisibility(View.INVISIBLE);
        }
    };

    private void fillData(final int page, final int count) {
        //显示拼命加载中
        mLlBlacklistTip.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                List<BlackList> part = mDao.findPage(page, count);
                if (part.size() > 0) {
                    mBlackLists.addAll(part);
                } else {
                    ToastUtils.show(BlackListActivity.this, "没有更多数据了！");
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void fillData(final int page) {
        fillData(page, PAGE_COUNT);
    }

    //添加电话号码至黑名单
    @OnClick(R.id.bt_blacklist_add)
    public void addBlackList(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = View.inflate(this, R.layout.dialog_add_blacklist, null);
        final AlertDialog dialog = builder.setView(v).show();
        final EditText etPhone = (EditText) dialog.findViewById(R.id.et_dialog_blacklist_phone);
        final RadioGroup rgMode = (RadioGroup) dialog.findViewById(R.id.rg_blacklist_mode);
        Button btOK = (Button) dialog.findViewById(R.id.bt_dialog_blacklist_ok);
        final Button btCancel = (Button) dialog.findViewById(R.id.bt_dialog_blacklist_cancel);
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取选中了哪种拦截方式
                int rbId = rgMode.getCheckedRadioButtonId();
                //默认添加号码时拦截短信
                int mode = BlackListDao.MODE_SMS;
                switch (rbId) {
                    case R.id.rb_dialog_blacklist_sms:
                        mode = BlackListDao.MODE_SMS;
                        break;
                    case R.id.rb_dialog_blacklist_phone:
                        mode = BlackListDao.MODE_PHO;
                        break;
                    case R.id.rb_dialog_blacklist_all:
                        mode = BlackListDao.MODE_ALL;
                        break;
                }
                //获取填写的电话号码
                String phone = etPhone.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    ToastUtils.show(BlackListActivity.this, "电话号码是必填的");
                } else {
                    if (mDao.add(phone, mode) > 0) {
                        Toast.makeText(BlackListActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        BlackList blackList = new BlackList(phone, mode);
                        //直接在List的开始添加，目的是将新添加的数据排在最上面
                        mBlackLists.add(0, blackList);
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(BlackListActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    class MyAdapter extends ArrayAdapter<BlackList> {
        private int mRes;

        public MyAdapter(Context context, int resource, List<BlackList> objects) {
            super(context, resource, objects);
            mRes = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), mRes, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BlackList blackList = getItem(position);
            holder.mTvBlacklistPhone.setText(blackList.getPhone());
            holder.mTvBlacklistMode.setText(MODE_LIST[blackList.getMode()]);
            holder.mIvBlacklistDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BlackListActivity.this);
                    final String phone = getItem(position).getPhone();
                    builder.setTitle("警告")
                            .setMessage("您确定要删除" + phone + "吗？");
                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mDao.delete(phone)) {
                                //直接在LIST中删除，再通知界面更新，少一次读数据库操作
                                mBlackLists.remove(position);
                                mAdapter.notifyDataSetChanged();
                                ToastUtils.show(BlackListActivity.this, "删除成功");
                            } else {
                                ToastUtils.show(BlackListActivity.this, "删除失败");
                            }
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });
            return convertView;
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_blacklist.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.tv_blacklist_phone)
        TextView mTvBlacklistPhone;
        @Bind(R.id.tv_blacklist_mode)
        TextView mTvBlacklistMode;
        @Bind(R.id.iv_blacklist_delete)
        ImageView mIvBlacklistDelete;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
