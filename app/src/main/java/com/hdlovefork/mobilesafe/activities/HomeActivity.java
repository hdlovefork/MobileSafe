package com.hdlovefork.mobilesafe.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.IntentUtils;
import com.hdlovefork.mobilesafe.utils.Md5Utils;
import com.hdlovefork.mobilesafe.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 主页
 */
public class HomeActivity extends AppCompatActivity {

    public static final String ACTION_LAUNCH="com.hdlovefork.mobilesafe.intent.action.HOME";
    private static final String TAG = "HomeActivity";
    private GridView mGridView;

    private static final String[] mNames = {"手机防盗", "通讯卫士", "软件管理", "进程管理", "流量统计",
            "手机杀毒", "缓存清理", "高级工具", "设置中心"};
    private static int[] mIcons = {
            R.drawable.safe, R.drawable.callmsgsafe, R.drawable.app_selector,
            R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
            R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mGridView = (GridView) findViewById(R.id.gv_home);
        mGridView.setAdapter(new MyAdapter(this, R.layout.item_homeactivity_gridview, mNames));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //手机防盗
                        //读取配置，判断用户是否完成过设置向导
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
                        String password = sharedPreferences.getString("password", null);
                        if (TextUtils.isEmpty(password)) {
                            //提示设置密码
                            showSetupPasswordDialog();
                        } else {
                            //提示键入密码
                            showInputPasswordDialog();
                        }
                        break;
                    case 1:
                        //通讯卫士
                        IntentUtils.startActivity(HomeActivity.this, BlackListActivity.class);
                        break;
                    case 2:
                        //软件管理
                        IntentUtils.startActivity(HomeActivity.this, AppManagerActivity.class);
                        break;
                    case 3:
                        //进程管理
                        sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
                        long clear_time = sharedPreferences.getLong("clear_time", 0);
                        if (System.currentTimeMillis() - clear_time < 5 * 1000) {
                            //5秒内清理过
                            ToastUtils.show(HomeActivity.this, "手机已达到最佳性能，稍候再来清理吧！");
                        } else {
                            IntentUtils.startActivity(HomeActivity.this, ProcessManagerActivity.class);
                        }
                        break;
                    case 6:
                        //缓存清理
                        IntentUtils.startActivity(HomeActivity.this,CacheCleanActivity.class);
                        break;
                    case 7:
                        //高级工具
                        IntentUtils.startActivity(HomeActivity.this, AdvToolsActivity.class);
                        break;
                    case 8://设置中心
                        IntentUtils.startActivity(HomeActivity.this, SettingActivity.class);
                        break;
                }
            }
        });
    }

    //弹出输入密码对话框
    private void showInputPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_enter_pwd, null);
        builder.setView(view);
        final Dialog dialog = builder.show();
        Button btnOK = (Button) view.findViewById(R.id.bt_dialog_enter_ok);
        Button btnCancel = (Button) view.findViewById(R.id.bt_dialog_enter_cancel);
        final EditText etPwd = (EditText) view.findViewById(R.id.et_dialog_enter_password);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curPwd = etPwd.getText().toString();
                if (TextUtils.isEmpty(curPwd)) {
                    Toast.makeText(HomeActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
                //获取用户上次设置的密码进行比较
                String pwd = sharedPreferences.getString("password", null);
                curPwd = Md5Utils.encode(curPwd);
                if (pwd.equals(curPwd)) {
                    boolean finishSetup = sharedPreferences.getBoolean("finish_setup", false);
                    if (finishSetup) {
                        //待完成
                        IntentUtils.startActivity(HomeActivity.this, LostFindActivity.class);
                    } else {
                        IntentUtils.startActivity(HomeActivity.this, Setup1Activity.class);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //弹出设置密码对话框
    private void showSetupPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_setup_pwd, null);
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
                    Toast.makeText(HomeActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pwd.equals(pwdConfirm)) {
                    Toast.makeText(HomeActivity.this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                //MD5加密
                pwd = Md5Utils.encode(pwd);
                //保存用户输入的密码
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.config_file_name), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("password", pwd);
                editor.commit();
                //进入设置向导
                IntentUtils.startActivity(HomeActivity.this, Setup1Activity.class);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    class MyAdapter extends ArrayAdapter<String> {
        private int mResource;
        private Context mContext;

        public MyAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mContext, mResource, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mIvItemHomeactivityGridviewIcon.setImageResource(mIcons[position]);
            holder.mTvItemHomeactivityGridviewTitle.setText(getItem(position));
            return convertView;
        }
    }
    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_homeactivity_gridview.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.iv_item_homeactivity_gridview_icon)
        ImageView mIvItemHomeactivityGridviewIcon;
        @Bind(R.id.tv_item_homeactivity_gridview_title)
        TextView mTvItemHomeactivityGridviewTitle;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
