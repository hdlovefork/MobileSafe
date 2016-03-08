package com.hdlovefork.mobilesafe.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hdlovefork.mobilesafe.R;
import com.hdlovefork.mobilesafe.utils.ContactUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 选择联系人对话框
 */
public class ContactActivity extends AppCompatActivity {
    @Bind(R.id.lv_contact)
    ListView mLvContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);
        //获取联系人信息
        final List<ContactUtils.ContactInfo> contacts = ContactUtils.getContacts(this);
        mLvContact.setAdapter(new MyAdapter(this, R.layout.item_select_contact, contacts));
        mLvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("phone", contacts.get(position).getPhone());
                setResult(0, intent);
                finish();
            }
        });
    }

    class MyAdapter extends ArrayAdapter<ContactUtils.ContactInfo> {

        private final int mRes;

        public MyAdapter(Context context, int resource, List<ContactUtils.ContactInfo> objects) {
            super(context, resource, objects);
            mRes = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), mRes, null);
                holder = new ViewHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_contact_name);
                holder.tvPhone = (TextView) convertView.findViewById(R.id.tv_contact_phone);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = getItem(position).getName();
            String phone = getItem(position).getPhone();
            holder.tvName.setText(name);
            holder.tvPhone.setText(phone);
            return convertView;
        }

        class ViewHolder {
            TextView tvName;
            TextView tvPhone;
        }
    }


}
