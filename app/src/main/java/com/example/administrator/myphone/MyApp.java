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
import org.pjsip.pjsua2.VideoWindowInfo;
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

public class MyApp extends MyAppBaseCall {
    private static final String TAG ="MyApp" ;

    public void init(MyService myService, SharedPreferences prefs, TbUserDao userDao, TbBuddyDao buddyDao) {
        mCon = myService;
        mMyService = myService;
        observer = this;
        mPref = prefs;
        mUserDao = userDao;
        mBuddyDao = buddyDao;
        super.init();
    }

    @Override
    public void deinit() {
        super.deinit();
    }

    public void resetSipParam() {
//        new Thread(){
//            public void run(){
        deinit();
        resetSip();
//            }
//        }.start();
    }


    public void setRegistration(boolean isTrue) {
        setRegistration(isTrue, true);
    }

    public void setRegistration(boolean isTrue, boolean isForce) {
        if (mAcc != null) {
//            a.b("currentCall:"+currentCall+",isNetworkConnected:"+MyUtil.isNetworkConnected(mCon));
            if (isForce ||
                    (currentCall == null && MyUtil.isNetworkConnected(mCon))
                    ) {
                try {
                    a.b4(TAG,"setRegistration:"+isTrue);
                    mAcc.setRegistration(isTrue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean isHasVideWindow() {
        if (currentCall != null &&
                currentCall.vidWin != null &&
                currentCall.vidPrev != null) {
            return true;
        }
        return false;
    }

    public void stopVideoWindow() {
        if (isHasVideWindow()) {
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

    public void updateCaptureVideoWindow(Surface surface) {
        if (isHasVideWindow()) {
            VideoWindowHandle vidWH = new VideoWindowHandle();
            vidWH.getHandle().setWindow(surface);
            VideoPreviewOpParam vidPrevParam = new VideoPreviewOpParam();
            vidPrevParam.setWindow(vidWH);
            try {
//                VideoWindowInfo info = currentCall.vidPrev.getVideoWindow().getInfo();
//                a.b("height:"+info.getSize().getH()+",width:"+info.getSize().getW());
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
        if (currentCall == null) {
            return null;
        }
        CallInfo info = null;
        try {
            info = currentCall.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
