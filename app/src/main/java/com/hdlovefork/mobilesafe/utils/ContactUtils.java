package com.hdlovefork.mobilesafe.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/14.
 */
public class ContactUtils {
    /**
     * 获取手机所有联系人
     *
     * @param context
     * @return
     */
    public static List<ContactInfo> getContacts(Context context) {
        List<ContactInfo> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[]{
                ContactsContract.RawContacts.CONTACT_ID
        }, null, null, null);
        //查询联系人ID
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(0);
            if (TextUtils.isEmpty(contactId)) {
                continue;
            }
            ContactInfo contactInfo = new ContactInfo();
            //查询数据表，取得姓名和号码
            Cursor dataCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1},
                    ContactsContract.Data.CONTACT_ID + " = ?",
                    new String[]{contactId}, null);
            while (dataCursor.moveToNext()) {
                String mimeType = dataCursor.getString(0);
                String data1 = dataCursor.getString(1);
                switch (mimeType) {
                    case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                        contactInfo.setName(data1);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                        contactInfo.setPhone(data1);
                        break;
                    case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                        contactInfo.setEmail(data1);
                        break;
                }
            }
            dataCursor.close();
            list.add(contactInfo);
        }
        cursor.close();
        return list;
    }

    public static class ContactInfo {
        private String mName;
        private String mPhone;

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String email) {
            mEmail = email;
        }

        private String mEmail;

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getPhone() {
            return mPhone;
        }

        public void setPhone(String phone) {
            mPhone = phone;
        }
    }
}
