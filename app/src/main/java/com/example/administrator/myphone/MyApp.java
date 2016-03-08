package com.example.administrator.myphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Surface;

import com.example.administrator.myphone.a.a.a.a.a;
import com.example.administrator.myphone.dao.TbUserDaoImp;
import com.example.administrator.myphone.dao.mBuddyDaoImp;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.ArrayList;
import java.util.List;

import gen.TbBuddy;
import gen.TbBuddyDao;
import gen.TbConfigDao;
import gen.TbUser;
import gen.TbUserDao;

/**
 * Created by Administrator on 2016/2/24.
 */

class MyLogWriter extends LogWriter {
    @Override
    public void write(LogEntry entry) {
        System.out.println(entry.getMsg());
    }
}

class MyAccount extends Account {
    public AccountConfig cfg;

    public MyAccount(AccountConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        MyApp.observer.notifyRegState(prm.getCode(), prm.getReason(),
                prm.getExpiration());
//        a.b(prm.getCode()+","+prm.getReason()+","+prm.getExpiration());
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        System.out.println("======== Incoming call ======== ");
        MyCall call = new MyCall(this, prm.getCallId());
//        CallOpParam callPrm=new CallOpParam(true);
//        callPrm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
//        try {
//            call.answer(callPrm);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        MyApp.observer.notifyIncomingCall(call);
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
        System.out.println("======== Incoming pager ======== ");
        System.out.println("From     : " + prm.getFromUri());
        System.out.println("To       : " + prm.getToUri());
        System.out.println("Contact  : " + prm.getContactUri());
        System.out.println("Mimetype : " + prm.getContentType());
        System.out.println("Body     : " + prm.getMsgBody());
    }
}

class MyAppBase implements MyAppObserver {
    static {
        System.loadLibrary("openh264");
        System.loadLibrary("yuv");
        System.loadLibrary("pjsua2");
    }

    public static Endpoint ep;
    public static MyAppObserver observer;
    public  List<MyAppObserver> observerList = new ArrayList<MyAppObserver>();

    protected EpConfig epConfig = new EpConfig();
    protected TransportConfig sipTpConfig = new TransportConfig();
    protected MyLogWriter logWriter;
    protected MyAccount mAcc = null;
    protected MyCall currentCall = null;
    private  Context mCon;
    protected MyService mMyService;

    protected TbUserDao mUserDao;
    protected TbUser mTbUser;
    protected TbBuddyDao mBuddyDao;
    protected SharedPreferences mPref;

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
        logWriter = new MyLogWriter();
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
        accCfg.getNatConfig().setIceEnabled(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);

        accCfg.getRegConfig().setTimeoutSec(Long.parseLong(mPref.getString(SettingsActivity.KEY_REGISTER_EXPIRE_TIME, mCon.getString(R.string.pref_default_register_expire_time))));
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


    public void init(MyService myService, SharedPreferences prefs, TbUserDao userDao, TbBuddyDao buddyDao) {

        mCon = myService;
        mMyService = myService;
        observer = this;
        mPref = prefs;
        mUserDao = userDao;
        mBuddyDao = buddyDao;
        resetSip();
    }

    public void deinit() {
        /* this need to remove else will "./src/pj/os_core_unix.c:693: pj_thread_this: assertion "!"Calling pjlib from unknown/external thread"  error
        * include Endpoint.java's Runtime.getRuntime().gc() method
        * */
//        Runtime.getRuntime().gc();
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


    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        regStatues = code;
        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).notifyRegState(code, reason, expiration);
        }
    }

    @Override
    public void notifyIncomingCall(MyCall call) {
        a.b("notifyIncomingCall");
        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).notifyIncomingCall(call);
        }

        if (currentCall !=null){
            CallOpParam prm = new CallOpParam(true);
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
            try {
                call.answer(prm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            currentCall = call;
            CallOpParam prm = new CallOpParam(true);
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            try {
                call.answer(prm);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMyService.startInComingWindow();
        }
    }

    @Override
    public void notifyCallState(MyCall call) {
        if (currentCall == null || call.getId() != currentCall.getId())
            return;

        CallInfo ci;
        try {
            ci = call.getInfo();
        } catch (Exception e) {
            ci = null;
        }

        if (ci != null &&
                ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            currentCall = null;
        }

        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).notifyCallState(call);
        }
    }

    @Override
    public void notifyCallMediaState(MyCall call) {
        a.b("notifyCallMediaState");
        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).notifyCallMediaState(call);
        }
    }
}


public class MyApp extends MyAppBase {

    public void resetSipParam() {
//        new Thread(){
//            public void run(){
        deinit();
        resetSip();
//            }
//        }.start();
    }


    public void setRegistration(boolean isTrue) {
        if (mAcc != null) {
            try {
                mAcc.setRegistration(isTrue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isHasVideWindow(){
        if (currentCall != null &&
                currentCall.vidWin != null &&
                currentCall.vidPrev != null) {
            return true;
        }
        return false;
    }
    public void stopVideoWindow(){
        if (isHasVideWindow()){
            try {
                currentCall.vidPrev.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                VideoWindowHandle vidWH = new VideoWindowHandle();
                currentCall.vidWin.setWindow(vidWH);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void updateCaptureVideoWindow(Surface surface){
        if (isHasVideWindow()) {
            VideoWindowHandle vidWH = new VideoWindowHandle();
            vidWH.getHandle().setWindow(surface);
            VideoPreviewOpParam vidPrevParam = new VideoPreviewOpParam();
            vidPrevParam.setWindow(vidWH);
            try {
                currentCall.vidPrev.start(vidPrevParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateVideoWindow(Surface surface, boolean show) {
        if (isHasVideWindow()) {
            VideoWindowHandle vidWH = new VideoWindowHandle();
            if (show) {
                vidWH.getHandle().setWindow(surface);
            } else {
                vidWH.getHandle().setWindow(null);
            }

            try {
                currentCall.vidWin.setWindow(vidWH);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void makeCall(String sipId) {
        if (mAcc == null) return;

        /* Only one call at anytime */
        if (currentCall != null) {
            return;
        }

        MyCall call = new MyCall(mAcc, -1);
        CallOpParam prm = new CallOpParam(true);
        try {
            String uri = "sip:" + sipId + "@" + mTbUser.getDomain();
            call.makeCall(uri, prm);
        } catch (Exception e) {
            e.printStackTrace();
            call.delete();
            return;
        }

        currentCall = call;
    }

    public void hangup() {
        if (currentCall == null) {
            return;
        }

        CallOpParam prm = new CallOpParam(true);
        try {
            currentCall.hangup(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        forceDisconnectCall();
    }

    public void acceptCall(){
        if (currentCall == null) {
            return;
        }
        CallOpParam prm = new CallOpParam(true);
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
            currentCall.answer(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void forceDisconnectCall() {
//        if (currentCall !=null){
//            currentCall.delete();
//        }
        currentCall = null;
    }

    public void logOut(Context con) {
        mUserDao.deleteAll();
        LoginActivity.mUserDao = mUserDao;
        MyUtil.startIntent(con, LoginActivity.class);
    }

    public pjsip_status_code getRegStatus() {
        return regStatues;
    }

    public void addBudy(TbBuddy buddy) {
        mBuddyDaoImp.addOrUpdate(mBuddyDao, buddy);
    }

    public boolean isStarted() {
        return ep == null ? false : true;
    }

    public TbConfigDao getTbConfigDao() {
        return null;
    }

    public TbUserDao getTbUserDao() {
        return mUserDao;
    }

    public TbBuddyDao getTbBuddyDao() {
        return mBuddyDao;
    }

    public CallInfo getCallInfo() {
        if (currentCall==null){
            return  null;
        }
        CallInfo info = null;
        try {
            info=currentCall.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
