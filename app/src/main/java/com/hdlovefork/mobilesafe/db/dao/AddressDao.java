package com.hdlovefork.mobilesafe.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by Administrator on 2015/10/20.
 */
public class AddressDao {
    private String mDbPath;

    public AddressDao(Context context) {
        mDbPath = new File(context.getFilesDir(), "address.db").getAbsolutePath();
        Log.d("AddressDao", "数据库文件路径是：" + mDbPath);
    }

    public String findAddress(String number) {
        String result = "";
        String telExp = "^1[3578]\\d{5,9}$";

        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(mDbPath, null, SQLiteDatabase.OPEN_READONLY);
            if (number.matches(telExp)) {
                //手机号码
                String outkey = "";
                String tel = number.substring(0, 7);
                Cursor cursor = db.query("data1", new String[]{"outkey"}, "id=?", new String[]{tel}, null, null, null);
                if (cursor.moveToFirst()) {
                    outkey = cursor.getString(0);
                }
                cursor.close();
                cursor = db.query("data2", new String[]{"location"}, "id=?", new String[]{outkey}, null, null, null);
                if (cursor.moveToFirst()) {
                    result = cursor.getString(0);
                }
                cursor.close();
            } else {
                int len = number.length();
                switch (len) {
                    case 3:
                        switch (number) {
                            case "110":
                                result = "匪警";
                                break;
                            case "120":
                                result = "急救电话";
                                break;
                            case "119":
                                result = "火警电话";
                                break;
                        }
                        break;
                    case 4:
                        result = "模拟器";
                        break;
                    case 5:
                        result = "客服电话";
                        break;
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                        if (number.startsWith("0")) {
                            String area = number.substring(1, 3);
                            Cursor cursor = db.query("data2", new String[]{"location"}, "area=?", new String[]{area}, null, null, null);
                            if (cursor.moveToFirst()) {
                                result = cursor.getString(0);
                            }
                            cursor.close();
                            if (TextUtils.isEmpty(result)) {
                                area=number.substring(1, 4);
                                cursor = db.query("data2", new String[]{"location"}, "area=?", new String[]{area}, null, null, null);
                                if(cursor.moveToFirst()){
                                    result=cursor.getString(0);
                                }
                                cursor.close();
                            }
                        }
                        break;

                }
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(result) ? "未知" : result;
    }

}
