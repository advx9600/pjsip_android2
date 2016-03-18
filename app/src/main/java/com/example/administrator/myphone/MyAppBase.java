package com.example.administrator.myphone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.administrator.myphone.a.a.a.a.a;
import com.example.administrator.myphone.dao.TbUserDaoImp;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pj_turn_tp_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gen.TbBuddyDao;
import gen.TbUser;
import gen.TbUserDao;

/**
 * Created by Administrator on 2016/3/8.
 */

class MyLogWriter extends LogWriter {
    private static final String TAG = "MyLogWriter";
    Context mCon;
    SharedPreferences mPref;
    public MyLogWriter(Context con,SharedPreferences pref){
        this.mCon = con;
        mPref = pref;
    }
    @Override
    public void write(LogEntry entry) {
        String log = entry.getMsg();
        if (entry.getLevel() == 1){
            if (log.contains("Operation not permitted [status=120001]")){
                    /*
                     Code:        120001
                     Description: Operation not permitted
                     */
                a.b2(TAG, "Operation not permitted,start help activity");
                a.b("getString:"+MyUtil.getErrLog(mPref));
                MyUtil.setErrLog(mPref,log, false);
            }
        }
        System.out.println(log);
    }
}

public class MyAppBase  {
    static {
        System.loadLibrary("openh264");
        System.loadLibrary("yuv");
        System.loadLibrary("pjsua2");
    }

    public static Endpoint ep;
    public static MyAppObserver observer;
    public List<MyAppObserver> observerList = new ArrayList<MyAppObserver>();
    private List<MyAppObserver> observerListSave = new ArrayList<MyAppObserver>();

    protected EpConfig epConfig = new EpConfig();
    protected TransportConfig sipTpConfig = new TransportConfig();
    protected MyLogWriter logWriter;
    protected MyAccount mAcc = null;
    protected MyCall currentCall = null;
    protected Context mCon;
    protected MyService mMyService;

    protected TbUserDao mUserDao;
    protected TbUser mTbUser;
    protected TbBuddyDao mBuddyDao;
    protected SharedPreferences mPref;

    /* for keep register  when in sleep */
    private MyAppBaseAlarm mAlarm ;
    public void saveCurData(){
        observerListSave.clear();
        for (int i=0;i<observerList.size();i++){
            observerListSave.add(observerList.get(i));
        }
    }
    public void restoreSaveData(){
        for (int i=0;i<observerListSave.size();i++){
            observerList.add(observerListSave.get(i));
        }
    }

    public void addObserver(MyAppObserver obs) {
        observerList.add(obs);
    }

    public void rmObserver(MyAppObserver obs) {
        observerList.remove(obs);
    }

    private void resetLogLevel() {
        int log_level = Integer.parseInt(mPref.getString(SettingsActivity.KEY_LOG_LEVEL, mCon.getString(R.string.pref_default_log_level)));
        epConfig.getLogConfig().setLevel(log_level);
        epConfig.getLogConfig().setConsoleLevel(log_level);

        /* Set log config. */
        LogConfig log_cfg = epConfig.getLogConfig();
        logWriter = new MyLogWriter(mCon,mPref);
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor() &
                ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));
    }

    private void resetStun() {
        UaConfig ua_cfg = epConfig.getUaConfig();
        ua_cfg.setUserAgent("Pjsua2 Android " + ep.libVersion().getFull());
        StringVector stun_servers = new StringVector();
        if (mPref.getBoolean(SettingsActivity.KEY_ENABLE_STUN_SERVER, true)){
            stun_servers.add(mPref.getString(SettingsActivity.KEY_STUN_SERVER, mCon.getString(R.string.pref_default_stun_server)));
        }
        ua_cfg.setStunServer(stun_servers);
    }

    private void resetSipPort() {
                /* Create transports. */
        int port = Integer.parseInt(mPref.getString(SettingsActivity.KEY_SIP_PORT, mCon.getString(R.string.pref_default_sip_port)));
        try {
            sipTpConfig.setPort(port);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                    sipTpConfig);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //        try {
//            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
//                    sipTpConfig);
//        } catch (Exception e) {
//            System.out.println(e);
//        }
    }

    public void resetSip() {
        try {
            ep = new Endpoint();
            ep.libCreate();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        /* must in order  else stun will not be enabled */
        resetLogLevel();
        resetStun();
        /* Init endpoint */
        try {
            ep.libInit(epConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        resetSipPort();
        /* account */
        resetAccount();
         /* Start. */
        try {
            ep.libStart();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void resetAccount() {
        mTbUser = TbUserDaoImp.getUser(mUserDao);
        if (mTbUser != null)
            resetAccount(mTbUser);
    }

    public void resetAccount(TbUser tbUser) {
        String accId = "sip:" + tbUser.getUsername() + "@" + tbUser.getDomain();
        String registrar = "sip:" + tbUser.getDomain();
        String username = tbUser.getUsername();
        String pwd = tbUser.getPwd();
        String proxy = "";

        AccountConfig accCfg = new AccountConfig();
        accCfg.setIdUri(accId);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().
                getAuthCreds();
        creds.clear();
        if (username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0,
                    pwd));
        }


        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }
        /* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(mPref.getBoolean(SettingsActivity.KEY_ENABLE_ICE, true));
        boolean isVideo = mPref.getBoolean(SettingsActivity.KEY_ENABLE_VIDEO, true);
        accCfg.getVideoConfig().setAutoShowIncoming(isVideo);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(isVideo);

        accCfg.getNatConfig().setTurnEnabled(mPref.getBoolean(SettingsActivity.KEY_ENABLE_TURN_SERVER, true));
        accCfg.getNatConfig().setTurnConnType(pj_turn_tp_type.PJ_TURN_TP_UDP);
        accCfg.getNatConfig().setTurnServer(mPref.getString(SettingsActivity.KEY_TURN_SERVER, mCon.getString(R.string.pref_default_turn_server)));

        long timeOut = Long.parseLong(mPref.getString(SettingsActivity.KEY_REGISTER_EXPIRE_TIME, mCon.getString(R.string.pref_default_register_expire_time)));
        accCfg.getRegConfig().setTimeoutSec(timeOut);
        /*
        * the network will time out quickly,so set small number instead
        * */
        mAlarm = new MyAppBaseAlarm(mCon,60);
        mAlarm.startAlarm();

        regStatues = null;

        try {
            if (mAcc == null) {
                mAcc = new MyAccount(accCfg);
                mAcc.create(accCfg);
            } else {
                mAcc.modify(accCfg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    protected pjsip_status_code regStatues = null;


    public void init() {
        resetSip();
    }

    public void deinit() {
        /* this need to remove else will "./src/pj/os_core_unix.c:693: pj_thread_this: assertion "!"Calling pjlib from unknown/external thread"  error
        * include Endpoint.java's Runtime.getRuntime().gc() method
        * */
//        Runtime.getRuntime().gc();
        if (mAlarm != null){
            mAlarm.stopAlarm();
            mAlarm = null;
        }

        if (currentCall != null) {
            currentCall.delete();
            currentCall = null;
        }
        /* delete account */
        if (mAcc !=null){
            mAcc.cfg.delete();
            mAcc.delete();
            mAcc = null;
        }

//        mAcc.cfg.d
        try {
            ep.libDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Force delete Endpoint here, to avoid deletion from a non-
        * registered thread (by GC?).
        */
        ep.delete();
        ep = null;
        observerList.clear();
    }



}
