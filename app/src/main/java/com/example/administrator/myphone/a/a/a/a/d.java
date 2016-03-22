package com.example.administrator.myphone.a.a.a.a;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import com.example.administrator.myphone.MyUtil;
import com.example.administrator.myphone.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

/**
 * Created by Administrator on 2016/3/22.
 */
public class d extends AsyncHttpResponseHandler{
    Context mCon;
    private String mDownUri;
    public d(Context context,String downUri){
        mCon=context;
        mDownUri = downUri;
    }

    @Override
    public void onSuccess(int i, Header[] headers, byte[] bytes) {
        String getVer = new String(bytes);
        if (getVer.equals(MyUtil.getVer(mCon))){
            MyUtil.alertConfirm(mCon,mCon.getString(R.string.alread_the_lastest_ver));
        }else {
            MyUtil.startDownloadApk(mCon,mDownUri);
            MyUtil.alertConfirm(mCon,mCon.getString(R.string.already_start_download));
        }
    }

    @Override
    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
        MyUtil.alertConfirm(mCon,mCon.getString(R.string.upgrade_failed)+"\n"+throwable.getMessage());
    }
}
