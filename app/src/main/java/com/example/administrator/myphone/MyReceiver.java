package com.example.administrator.myphone;

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
    private MyApp myApp = MyService.myApp;
    @Override
    public void onReceive(Context context, Intent intent) {
//        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifi_service.getConnectionInfo();

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                if ( state == NetworkInfo.State.CONNECTED){
                    myApp.setRegistration(true);
                } else if (state == NetworkInfo.State.DISCONNECTED){
//                    myApp.setRegistration(false);
                }
            }else {
                a.b("action:"+intent.getAction());
            }
        }


    }
}
