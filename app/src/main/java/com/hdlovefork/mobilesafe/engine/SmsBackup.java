package com.hdlovefork.mobilesafe.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * Created by Administrator on 2015/10/22.
 */
public class SmsBackup {
    /**
     * 短信备份/还原时的回调
     */
    public interface SmsCallback {
        /**
         * 开始工作前回调
         *
         * @param max 总共有多少条短信待处理
         */
        void onPrepare(int max);

        /**
         * 正在工作中回调
         *
         * @param progress 已经处理了多少个条目
         */
        void onProgress(int progress);
    }

    public static boolean backupSms(Context context, String fileName, SmsCallback callback) {
        try {
            File outFile = new File(Environment.getExternalStorageDirectory(), fileName);
            Log.d("SmsBackup", outFile.getAbsolutePath());
            outFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(outFile);
            XmlSerializer xmlSerializer = Xml.newSerializer();

            xmlSerializer.setOutput(outputStream, "utf-8");
            xmlSerializer.startDocument("utf-8", true);
            Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms"),
                    new String[]{"address", "date", "type", "body"},
                    null, null, null);
            int max = cursor.getCount();
            if (callback != null)
                callback.onPrepare(max);
            xmlSerializer.startTag(null, "root");
            xmlSerializer.attribute(null, "total", String.valueOf(max));
            int progress = 0;
            while (cursor.moveToNext()) {
                xmlSerializer.startTag(null, "sms");

                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(cursor.getString(0));
                xmlSerializer.endTag(null, "address");

                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(cursor.getString(1));
                xmlSerializer.endTag(null, "date");

                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(cursor.getString(2));
                xmlSerializer.endTag(null, "type");

                xmlSerializer.startTag(null, "body");
                xmlSerializer.text(cursor.getString(3));
                xmlSerializer.endTag(null, "body");

                xmlSerializer.endTag(null, "sms");
                progress++;
                if (callback != null)
                    callback.onProgress(progress);
            }
            cursor.close();
            xmlSerializer.endTag(null, "root");
            xmlSerializer.endDocument();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean restore(Context context, String fileName, SmsCallback callback) {
        try {
            File inFile = new File(Environment.getExternalStorageDirectory(), fileName);
            FileInputStream inputStream = new FileInputStream(inFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            int eventType = parser.getEventType();
            int progress = 0;
            String address = null;
            String body = null;
            String date = null;
            String type = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        switch (parser.getName()) {
                            case "root":
                                int max = Integer.valueOf(parser.getAttributeValue(null, "total"));
                                if (callback != null)
                                    callback.onPrepare(max);
                                break;
                            case "address":
                                address = parser.nextText();
                                break;
                            case "body":
                                body = parser.nextText();
                                break;
                            case "date":
                                date = parser.nextText();
                                break;
                            case "type":
                                type = parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("sms")) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("address", address);
                            contentValues.put("body", body);
                            contentValues.put("date", date);
                            contentValues.put("type", type);
                            context.getContentResolver().insert(Uri.parse("content://sms"), contentValues);
                            progress++;
                            if (callback != null)
                                callback.onProgress(progress);
                        }
                        break;
                }
                eventType = parser.next();
            }
            inputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
