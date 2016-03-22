package com.example.administrator.myphone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2016/3/17.
 */
public class MyAppBaseAlarm {
    private Context mCon;
    private AlarmManager mAlarmManager;
    PendingIntent pi;
    PendingIntent pi2;
    private long mAlarmTime=0;

    public MyAppBaseAlarm(Context con,long time){
        mCon= con;
        mAlarmManager = (AlarmManager) mCon.getSystemService(Service.ALARM_SERVICE);
        mAlarmTime = time*1000;
    }

    public void startAlarm(){
        startAlarm1();
        startAlarm2();
    }

    private void startAlarm1(){
        Intent intent = new Intent();
        intent.setClass(mCon, MyReceiver.class);
        intent.setAction(MyReceiver.ACTION_ALARM_REPEART);
        pi = PendingIntent.getBroadcast(mCon,0,intent,0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), mAlarmTime, pi);
    }

    private void startAlarm2(){
        Intent intent = new Intent();
        intent.setClass(mCon, MyReceiver.class);
        intent.setAction(MyReceiver.ACTION_ALARM_REPEART_KEEP_PORT);
        pi2 = PendingIntent.getBroadcast(mCon,0,intent,0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30*1000, pi2);
    }

    public void stopAlarm(){
        if (pi!=null)
            mAlarmManager.cancel(pi);
        pi = null;

        if (pi2 !=null)
            mAlarmManager.cancel(pi2);
        pi=null;
    }
}
