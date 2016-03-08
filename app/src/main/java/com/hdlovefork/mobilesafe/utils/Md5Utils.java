package com.hdlovefork.mobilesafe.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2015/10/14.
 */
public class Md5Utils {

    public static String encode(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            byte[] bytes = messageDigest.digest(text.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : bytes) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
