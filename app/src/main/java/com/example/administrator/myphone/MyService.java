package com.example.administrator.myphone;

/**
 * Created by Administrator on 2016/2/25.
 */

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.example.administrator.myphone.a.a.a.a.a;
import com.example.administrator.myphone.db.DB;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

import gen.DaoSession;
import gen.TbBuddyDao;
import gen.TbConfigDao;
import gen.TbUserDao;

public class MyService extends Service {
    public MyService() {
    }

    public static int NOTIFY_ID = 200;

    private boolean mIsStaredForeground = false;

    private TbConfigDao mConfigDao;
    private TbUserDao mUserDao;
    private TbBuddyDao mBuddyDao;
    public static MyApp myApp = new MyApp();

    private static final int MSG_START_INCOMING_ACTIVITY = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_INCOMING_ACTIVITY:
                    Intent dialogIntent = new Intent(getBaseContext(), CallActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(dialogIntent);
                    break;
            }
        }
    };

    public void startInComingWindow() {
        handler.sendEmptyMessage(MSG_START_INCOMING_ACTIVITY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runAsForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        initDB();
        runAsForeground();
    }

    @Override
    public void onDestroy() {
        // Make sure our notification is gone.
        stopAsForground();
    }

    private void initDB() {
        DaoSession dao = DB.getDaoSession(this);
        mConfigDao = dao.getTbConfigDao();
        mUserDao = dao.getTbUserDao();
        mBuddyDao = dao.getTbBuddyDao();
    }
//
//    class UdpThread extends Thread {
//        @Override
//        public void run() {
//
//            int t = 0;
//
//            t = 0;
//            try {
//                DatagramSocket client_socket = new DatagramSocket(6001);
//
//                InetAddress IPAddress = InetAddress.getByName("120.24.77.212");
//                while (mIsStaredForeground) {
//                    t++;
//                    DatagramPacket send_packet = new DatagramPacket((t + "").getBytes(), (t + "").length(), IPAddress, 6001);
//                    client_socket.send(send_packet);
//                    a.b("now send times:" + t);
//                    sleep(2000);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void runAsForeground() {
//        mLoopThread.resetParam(ZheTuWifiActivity.MyParam.getUser(this), ZheTuWifiActivity.MyParam.getPwd(this), ZheTuWifiActivity.MyParam.getInterval(this),MyUtil.getWifiIp(this));

        if (mIsStaredForeground) return;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Notification notification = builder.setContentTitle(getText(R.string.app_name)).setContentText(""/*getText(R.string.to_main_page)*/)
                .setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher).setWhen(System.currentTimeMillis()).build();

        startForeground(NOTIFY_ID, notification);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        myApp.init(this, prefs, mUserDao, mBuddyDao);

        mIsStaredForeground = true;

    }

    private void stopAsForground() {
        if (!mIsStaredForeground) return;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFY_ID);

        stopForeground(true);

        myApp.deinit();
        mIsStaredForeground = false;
    }

}