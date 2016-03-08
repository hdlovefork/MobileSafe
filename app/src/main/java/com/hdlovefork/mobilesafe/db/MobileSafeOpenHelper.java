package com.hdlovefork.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2015/10/16.
 */
public class MobileSafeOpenHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_BLACKLIST = "create table blacklist (" +
            "_id integer primary key autoincrement," +  //自增主键
            "phone varchar(20)," +                          //要拦截的电话号码
            "mode varchar(2))";                             //1：短信拦截 2.电话拦截 3.全部拦截
    private static final String CREATE_TABLE_APPLOCK="create table applock(" +
            "_id integer primary key autoincrement," +
            "package_name varchar(20)" +
            ")";

    private static final String DB_NAME = "mobilesafe.db";

    public MobileSafeOpenHelper(Context context) {
        this(context, DB_NAME, null, 2);
    }

    public MobileSafeOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BLACKLIST);
        db.execSQL(CREATE_TABLE_APPLOCK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                //增加程序锁功能
                db.execSQL(CREATE_TABLE_APPLOCK);
        }
    }
}
