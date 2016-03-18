package com.example.administrator.myphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.example.administrator.myphone.a.a.a.a.a;

/**
 * Created by Administrator on 2016/3/16.
 */
public class MyAppBaseWakeLock {
    private  static final String TAG = "MyAppBaseWakeLock";

    /* while sleep mode,also need receive data and send data */
    PowerManager.WakeLock wakeLock = null;
    WifiManager.WifiLock wifiLock =null;
    WifiManager.MulticastLock wifiMultiLock =null;
    Context mCon;
    SharedPreferences mPref;
    public MyAppBaseWakeLock(Context con,SharedPreferences pref){
        mCon =con;
        mPref = pref;
    }
    public void acquireWakeLock()
    {
//        if (!mPref.getBoolean(SettingsActivity.KEY_ENABLE_WAKELOCK,false)){
//            return;
//        }
        a.b4(TAG,"start acquireWakLock");
        /** strain battery **/
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)mCon.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }

        WifiManager mg = (WifiManager)mCon.getSystemService(Context.WIFI_SERVICE);
        if (null == wifiLock){
            wifiLock = mg.createWifiLock(TAG);
            if (null != wifiLock){
                wifiLock.acquire();
            }
        }
        if (null == wifiMultiLock){
            wifiMultiLock = mg.createMulticastLock(TAG);
            if (null != wifiMultiLock){
                wifiMultiLock.acquire();
            }
        }
    }

    //释放设备电源锁
    public void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }

        if (null != wifiLock){
            wifiLock.release();
            wifiLock = null;
        }

        if (null != wifiMultiLock){
            wifiMultiLock.release();
            wifiMultiLock = null;
        }
    }


}
