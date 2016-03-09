package com.example.administrator.myphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.administrator.myphone.a.a.a.a.a;

import org.pjsip.pjsua2.pjsip_status_code;

import java.io.File;

/**
 * Created by Administrator on 2016/2/24.
 */
public class MyUtil {
    public static void startIntent(Context con,Class<?> cls ){
        Intent intent = new Intent(con, cls);
        con.startActivity(intent);
    }
    public static void startIntent(Context con,Intent intent){
        con.startActivity(intent);
    }

    public static void startMySevice(Context con){
        Intent intent = new Intent(con, MyService.class);
        con.startService(intent);
    }
    public static void stopMyService(Context con){
        Intent intent = new Intent(con, MyService.class);
        con.stopService(intent);
    }

    public static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    public static void alertYesCanel( View layout, DialogInterface.OnClickListener onConfirmClickListener) {
        AlertDialog ad = new AlertDialog.Builder(layout.getContext()).setView(layout)
                .setPositiveButton(layout.getContext().getString(android.R.string.ok), onConfirmClickListener)
                .setNegativeButton(layout.getContext().getString(android.R.string.cancel), null).show();
//        ad.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//        ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void toast(Context context, int resourceId) {
        Toast.makeText(context,resourceId,Toast.LENGTH_LONG).show();
    }

//    public static String getRegStatus(Context con,pjsip_status_code regStatus) {
//        String text = "";
//        if (regStatus == null){
//            text=con.getString(R.string.registering);
//        }else if (regStatus == pjsip_status_code.PJSIP_SC_OK){
//            text = con.getString(R.string.registered);
//        }else  if (regStatus == pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT){
//            text = con.getString(R.string.request_timeout);
//        }
//        return text;
//    }

    /**
     * 检测网络是否可用
     * @return
     */
    public static boolean isNetworkConnected(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

}
