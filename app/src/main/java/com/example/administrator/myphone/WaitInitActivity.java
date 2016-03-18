package com.example.administrator.myphone;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.example.administrator.myphone.a.a.a.a.a;

/**
 * Created by Administrator on 2016/3/3.
 */
public class WaitInitActivity extends AppCompatActivity {
    private MyApp  myApp= MyService.myApp;

    private boolean isExist = false;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    MyUtil.startIntent(WaitInitActivity.this,MainActivity.class);
                    finish();
                    break;
            }
        }
    };
    private long mStartTime = System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo);
        startThread();
    }

    private void startThread(){
        /* use runnable not work, so use thread instead */
        new Thread(){
            @Override
            public void run(){
                while (!isExist){
                    if ((System.currentTimeMillis()-mStartTime)> 1000 && myApp.isStarted()){
                        handler.sendEmptyMessage(1);
                        break;
                    }else {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroy(){
        isExist = true;
        super.onDestroy();
    }
}
