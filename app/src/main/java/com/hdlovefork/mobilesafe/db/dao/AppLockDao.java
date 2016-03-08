package com.hdlovefork.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.hdlovefork.mobilesafe.db.MobileSafeOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/28.
 */
public class AppLockDao {
    /**
     * applock表的内容提供器
     */
    public static Uri CONTENT_URI = Uri.parse("content://com.hdlovefork.mobilesafe.APPLOCK");
    private static AppLockDao mAppLockDao;
    private final SQLiteDatabase mDB;
    public static final String TABLE_NAME = "applock";
    private Context mContext;


    private AppLockDao(Context context) {
        mContext = context;
        MobileSafeOpenHelper helper = new MobileSafeOpenHelper(context);
        mDB = helper.getWritableDatabase();
    }

    public synchronized static AppLockDao getInstance(Context context) {
        if (mAppLockDao == null)
            mAppLockDao = new AppLockDao(context);
        return mAppLockDao;
    }

    /**
     * 将指定包名的应用变成加锁应用
     *
     * @param packageName
     * @return
     */
    public boolean add(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("package_name", packageName);
        long id = mDB.insert(TABLE_NAME, null, contentValues);
        mContext.getContentResolver().notifyChange(CONTENT_URI, null);
        return id > 0;
    }

    /**
     * 将指定包名的应用变成未加锁应用
     *
     * @param packageName
     * @return
     */
    public boolean delete(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        int rows = mDB.delete(TABLE_NAME, "package_name=?", new String[]{packageName});
        mContext.getContentResolver().notifyChange(CONTENT_URI, null);
        return rows > 0;
    }

    /**
     * 查询所有需要加锁的包名
     *
     * @return
     */
    public List<String> findAll() {
        Cursor cursor = mDB.query(TABLE_NAME, new String[]{"package_name"}, null, null, null, null, null);
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    /**
     * 指定的包名是否已经存在
     *
     * @param packageName
     * @return
     */
    public boolean exists(String packageName) {
        Cursor cursor = mDB.query(TABLE_NAME, null, "package_name=?", new String[]{packageName}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
