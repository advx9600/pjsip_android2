package com.example.administrator.myphone;

import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.example.administrator.myphone.a.a.a.a.a;

/**
 * Created by Administrator on 2016/2/29.
 */
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG ="BroadcastReceiver" ;
    public static final String ACTION_ALARM_REPEART = "action_alarm_repeat";
    public static final String ACTION_ALARM_REPEART_KEEP_PORT = "action_alarm_repeat_keep_port";
    private MyApp myApp = MyService.myApp;
    @Override
    public void onReceive(Context context, Intent intent) {
//        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        String act = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(act)) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                if ( state == NetworkInfo.State.CONNECTED){
                    a.b4(TAG,"receive wifi connected");
                    myApp.setRegistration(true);
                } else if (state == NetworkInfo.State.DISCONNECTED){
//                    myApp.setRegistration(false);
                }
            }else {
                a.b("action:"+intent.getAction());
            }
        }else if (ACTION_ALARM_REPEART.equals(act)){
            a.b4(TAG, "receive :" + ACTION_ALARM_REPEART);
            myApp.setRegistration(true, false);

        }else  if (ACTION_ALARM_REPEART_KEEP_PORT.equals(act)){
//            KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            a.b("now not very good,need to improvement");
            a.b4(TAG,"keep alive alarm,isWifiConnected:"+MyUtil.isWifiConnected(context));
            if (MyUtil.isWifiConnected(context)){
                myApp.setRegistration(true, false);
            }
        }else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(act)){
            String id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)+"";
            a.b4(TAG,"id:"+id+",MyUtilDownloadId:"+MyUtil.getDownloadId(context));
            if (id.equals(MyUtil.getDownloadId(context))){
                a.b4(TAG,"download finished");
                MyUtil.installAPK(context,id);
            }
        }

    }
}
