package com.hdlovefork.mobilesafe.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtils {

    public static String ReadStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String s = "";
        try {
            while ((s = reader.readLine()) != null) {
                stringBuilder.append(s);
            }
        } catch (IOException e) {
            return null;
        }
        return stringBuilder.toString();
    }
}
