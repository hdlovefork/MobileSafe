package com.hdlovefork.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hdlovefork.mobilesafe.db.MobileSafeOpenHelper;
import com.hdlovefork.mobilesafe.domain.BlackList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/16.
 */
public class BlackListDao {

    public static final int MODE_SMS = 1;
    public static final int MODE_PHO = 2;
    public static final int MODE_ALL = 3;

    protected final MobileSafeOpenHelper mHelper;
    protected static final String TABLE_NAME = "blacklist";

    public BlackListDao(Context context) {
        mHelper = new MobileSafeOpenHelper(context);
    }

    /**
     * 往黑名单中添加一个电话号码
     *
     * @param phone 电话号码
     * @param mode  1：短信拦截 2.电话拦截 3.全部拦截
     * @return 失败时返回-1，成功时返回ID
     */
    public long add(String phone, int mode) {
        if (TextUtils.isEmpty(phone) || mode < 1 || mode > 3) {
            return -1;
        }
        SQLiteDatabase writableDatabase = mHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("mode", mode);
        long row = writableDatabase.insert(TABLE_NAME, null, contentValues);
        writableDatabase.close();
        return row;
    }

    /**
     * 从黑名单中删除一个电话号码
     *
     * @param phone 电话号码
     * @return 成功返回TRUE
     */
    public boolean delete(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        SQLiteDatabase writableDatabase = mHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        int row = writableDatabase.delete(TABLE_NAME, "phone=?", new String[]{phone});
        writableDatabase.close();
        return row > 0;
    }

    /**
     * 修改指定黑名单号码的拦截模式
     *
     * @param phone 电话号码
     * @param mode  拦截模式
     * @return 成功返回TRUE
     */
    public boolean update(String phone, String mode) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(mode)) {
            return false;
        }
        SQLiteDatabase writableDatabase = mHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mode", mode);
        int row = writableDatabase.update(TABLE_NAME, contentValues, "phone=?", new String[]{phone});
        writableDatabase.close();
        return row > 0;
    }

    /**
     * 查询指定号码的拦截模式
     *
     * @param phone 要查询的电话号码
     * @return 返回0表示不拦截
     */
    public int findMode(String phone) {
        int r = 0;
        if (TextUtils.isEmpty(phone)) {
            return r;
        }
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(TABLE_NAME, null, "phone=?", new String[]{phone}, null, null, null);
        while (cursor.moveToNext()) {
            r = cursor.getInt(cursor.getColumnIndex("mode"));
        }
        cursor.close();
        readableDatabase.close();
        return r;
    }

    /**
     * 查询所有黑名单号码（按ID的倒序排列）
     *
     * @return 所有黑名单对象
     */
    public List<BlackList> findAll() {
        List<BlackList> lists = new ArrayList<>();
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();
        //最后添加的排最上面
        Cursor cursor = readableDatabase.query(TABLE_NAME, new String[]{"phone,mode"}, null, null, null, null, "_id desc");
        while (cursor.moveToNext()) {
            String phone = cursor.getString(0);
            int mode = cursor.getInt(1);
            BlackList blackList = new BlackList(phone, mode);
            lists.add(blackList);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        readableDatabase.close();
        return lists;
    }

    /**
     * 分页查找数据
     */
    public List<BlackList> findPage(int page,int pageCount) {
        List<BlackList> lists = new ArrayList<>();
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();
        //最后添加的排最上面
        Cursor cursor = readableDatabase.rawQuery(
                "select phone,mode from " + TABLE_NAME + " order by _id desc limit ? offset ?",
                new String[]{String.valueOf(pageCount), String.valueOf(pageCount * page)});
        while (cursor.moveToNext()) {
            String phone = cursor.getString(0);
            int mode = cursor.getInt(1);
            BlackList blackList = new BlackList(phone, mode);
            lists.add(blackList);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        readableDatabase.close();
        return lists;
    }


    /**
     * 分批查找数据
     *
     * @param startIndex 从0开始的索引
     * @param maxCount   最多获取多少条数据
     * @return
     */
    public List<BlackList> findPart(int startIndex, int maxCount) {
        List<BlackList> lists = new ArrayList<>();
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();
        //最后添加的排最上面
        Cursor cursor = readableDatabase.rawQuery(
                "select phone,mode from " + TABLE_NAME + " order by _id desc limit ? offset ?",
                new String[]{String.valueOf(maxCount), String.valueOf(startIndex)});
        while (cursor.moveToNext()) {
            String phone = cursor.getString(0);
            int mode = cursor.getInt(1);
            BlackList blackList = new BlackList(phone, mode);
            lists.add(blackList);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        readableDatabase.close();
        return lists;
    }

    /**
     * 查询所有黑名单记录总数
     *
     * @return
     */
    public int getRows() {
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery("select count(_id) from " + TABLE_NAME, null);
        cursor.moveToFirst();
        int rows = cursor.getInt(0);
        cursor.close();
        readableDatabase.close();
        return rows;
    }

}
