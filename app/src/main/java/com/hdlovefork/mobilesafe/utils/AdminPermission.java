package com.hdlovefork.mobilesafe.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hdlovefork.mobilesafe.receiver.MyAdminReceiver;

/**
 * 管理员权限激活工具
 */
public class AdminPermission {
    public static void active(DevicePolicyManager devicePolicyManager,Context context,ComponentName componentName){
        //如果已经获得管理员权限直接返回
        if (devicePolicyManager.isAdminActive(componentName)) return;
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "开启之后才能使用手机防盗功能");
        context.startActivity(intent);
    }

    public static void uninstall(Context context){
        //移除管理员权限
        DevicePolicyManager devicePolicyManager= (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName=new ComponentName(context, MyAdminReceiver.class);
        devicePolicyManager.removeActiveAdmin(componentName);
        //打开系统卸载窗口
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

}
