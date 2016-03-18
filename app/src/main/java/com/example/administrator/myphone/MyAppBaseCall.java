package com.example.administrator.myphone;

import android.content.SharedPreferences;

import com.example.administrator.myphone.a.a.a.a.a;

import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;
import java.util.List;

import gen.TbBuddyDao;
import gen.TbUserDao;

/**
 * Created by Administrator on 2016/3/8.
 */
public class MyAppBaseCall extends MyAppBase implements MyAppObserver{
    private static final String TAG = "MyAppBaseCall";
    /* when force disconnect call,save it in mListCall until auto disconnected,not for incoming call  */
    private List<MyCall> mListCall = new ArrayList<MyCall>();

    public void init() {
        mListCall.clear();
        observer = this;

        super.init();
    }

    public boolean isCallAlreadyExist(String sipId,boolean isSetCurrentCall){
         /* if alreadly exist then get it */
        try {
            if (mListCall.size()>0){
                for (int i =0;i<mListCall.size();i++){
                    MyCall call = mListCall.get(i);
                    CallInfo info = call.getInfo();
                    String uri = "sip:" + sipId + "@" + mTbUser.getDomain();
                    if (uri.equals(info.getRemoteUri())){
                        /* must add this */
                        if (isSetCurrentCall)
                            currentCall = call;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCallAlreadyExist(String sipId){
         return  isCallAlreadyExist(sipId,false);
    }

    public void makeCall(String sipId) {
        if (mAcc == null) return;

        /* Only one call at anytime */
        if (currentCall != null) {
            return;
        }

        if (isCallAlreadyExist(sipId)){
            a.b4(TAG,"call already exist");
            return;
        }

        MyCall call = new MyCall(mAcc, -1);
        CallOpParam prm = new CallOpParam(true);
        try {
            String uri = "sip:" + sipId + "@" + mTbUser.getDomain();
            call.makeCall(uri, prm);
            a.b4(TAG,"new call:" + uri);
        } catch (Exception e) {
            e.printStackTrace();
            call.delete();
            return;
        }

        currentCall = call;
        mListCall.add(currentCall);
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
        currentCall = null;
    }


    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        a.b4(TAG,"notifyRegState:"+code);
        regStatues = code;
        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).notifyRegState(code, reason, expiration);
        }
        /* if disconnected re register */
        if (code != pjsip_status_code.PJSIP_SC_OK){
            if (MyUtil.isNetworkConnected(mCon)){
                try {
                    mAcc.setRegistration(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void notifyIncomingCall(MyCall call) {
        a.b("notifyIncomingCall");
        for (int i = 0; i < observerList.size(); i++) {
            observerList.get(i).notifyIncomingCall(call);
        }

        if (mListCall.size()>0){
            a.b3(TAG,"mListCall.size:"+mListCall.size()+",cancle incoming call");
            CallOpParam prm = new CallOpParam(true);
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
            try {
                call.answer(prm);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        mListCall.add(call);

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
        for (int i = 0; i < mListCall.size(); i++) {
            MyCall getCall = mListCall.get(i);
            if (getCall.getId() == call.getId()){
                CallInfo ci;
                try {
                    ci = call.getInfo();
                } catch (Exception e) {
                    ci = null;
                }

                if (ci != null &&
                        ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                    a.b4(TAG,"now disconnected call:"+ci.getRemoteUri());
                    mListCall.remove(getCall);
                    break;
                }
            }
        }

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
