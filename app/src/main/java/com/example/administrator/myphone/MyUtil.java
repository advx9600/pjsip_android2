package com.example.administrator.myphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    public static void alertConfirm( Context con,String msg) {
        AlertDialog ad = new AlertDialog.Builder(con).setMessage(msg).setPositiveButton(android.R.string.ok,null).show();
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

    public static void setNeverSleepPolicy(Context context){
        ContentResolver cr = context.getContentResolver();
        int set = android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER;
        android.provider.Settings.System.putInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, set);
    }


    public static void setErrLog(SharedPreferences pref, String log, boolean isForce) {
        if (!isForce)
            if (getErrLog(pref).trim().length() > 0) {
                return;
            }

        SharedPreferences.Editor edit = pref.edit();
        edit.putString(SettingsActivity.KEY_PROGRAM_ERROR_LOG, log);
        edit.commit();
    }

    public static String getErrLog(SharedPreferences pref){
        return pref.getString(SettingsActivity.KEY_PROGRAM_ERROR_LOG,"").trim();
    }

    public static String processErrMsg(Context con,String errLog) {
        String msg = "";
        if (errLog.contains("Operation not permitted [status=120001]")){
            msg += con.getString(R.string.peration_not_permitted_status_120001)+"\n";
        }
        msg += errLog;
        return msg;
    }

    public static boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }

        return false ;
    }

    public static String getVer(Context context) {
        String ver="";
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            ver =""+pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ver;
    }

    public static void startDownloadApk(Context con, String downUri) {
        DownloadManager downloadManager = (DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downUri);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        String sDest = "file://"+android.os.Environment.getExternalStorageDirectory().toString()+"/Download/Install.apk";
        request.setDestinationUri(Uri.parse(sDest));
        long id=downloadManager.enqueue(request);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(con);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(SettingsActivity.KEY_DOWNLOAD_ID,id+"");
        edit.commit();
    }

    public static String getDownloadId(Context con){
        return PreferenceManager.getDefaultSharedPreferences(con).getString(SettingsActivity.KEY_DOWNLOAD_ID,"");
    }

    public static void installAPK(Context context,String sId) {
        Long id = Long.parseLong(sId);
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri downloadFileUri = dManager.getUriForDownloadedFile(id);
        install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }
}
