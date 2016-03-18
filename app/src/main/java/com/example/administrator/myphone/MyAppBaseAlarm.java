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
    private long mAlarmTime=0;

    public MyAppBaseAlarm(Context con,long time){
        mCon= con;
        mAlarmManager = (AlarmManager) mCon.getSystemService(Service.ALARM_SERVICE);
        mAlarmTime = time*1000;
    }

    public void startAlarm(){
        Intent intent = new Intent();
        intent.setClass(mCon, MyReceiver.class);
        intent.setAction(MyReceiver.ACTION_ALARM_REPEART);
        pi = PendingIntent.getBroadcast(mCon,0,intent,0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), mAlarmTime, pi);
    }

    public void stopAlarm(){
        if (pi!=null)
            mAlarmManager.cancel(pi);
        pi = null;
    }
}
