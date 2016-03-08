package com.hdlovefork.mobilesafe.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Administrator on 2015/10/16.
 */
public class GPSService extends Service {
    private static final String TAG = "GPSService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called with: " + "");
        super.onCreate();
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "提供者是：" + provider);
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                String msg = "W:" + location.getLatitude() + ",J:" + location.getLongitude();
                Log.d(TAG, "最后位置是：" + msg);
            }
            locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //读取配置中的用户安全号码
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    String phone = sharedPreferences.getString("phone", "");
                    String text = "W:" + location.getLatitude() + ",J:" + location.getLongitude();
                    Log.d("GPSService", "发送坐标短信:" + text);
                    //SmsManager.getDefault().sendTextMessage(phone, null, text, null, null);
                    try {
                        locationManager.removeUpdates(this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
}
