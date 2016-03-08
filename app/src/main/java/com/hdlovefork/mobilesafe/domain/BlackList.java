package com.hdlovefork.mobilesafe.domain;

/**
 * 对应数据库BlackList表的实体类
 */
public class BlackList {
    public BlackList(String phone, int mode) {
        mPhone = phone;
        mMode = mode;
    }

    private String mPhone;
    private int mMode;

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode = mode;
    }
}
