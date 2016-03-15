package com.example.administrator.myphone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.os.Message;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.Layout;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myphone.a.a.a.a.a;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjmedia_orient;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import gen.TbBuddy;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CallActivity extends Activity implements MyAppObserver, SoundPool.OnLoadCompleteListener {

    public static final String EXTRA_BUDDY_ID = "buddy_id";

    private pjsip_status_code mLastCode;

    private String mStatues;
    private SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_RING, 0);
    private MediaPlayer mMediaPlayer;
    private int mSoundWaitId = 0;

    private MyApp myApp = MyService.myApp;

    private TbBuddy mBuddy;

    enum TYPE {
        CALL, INCOMING,
    }

    /* incoming call or call others */
    private TYPE mType;

    @Bind(R.id.layout_btns)
    LinearLayout mBtnLayout;
    @Bind(R.id.btn_accept)
    Button mAcceptBtn;
    @Bind(R.id.btn_hangup)
    Button mHangupBtn;
    @Bind(R.id.text_info)
    TextView textInfo;
    @Bind(R.id.surfaceIncomingVideo)
    SurfaceView mVidRender;
    @Bind(R.id.surfacePreviewCapture)
    SurfaceView mVidCapture;
    @Bind(R.id.layout_video)
    RelativeLayout mVidLayout;


    private PowerManager.WakeLock mWakLock;
    private KeyguardManager.KeyguardLock mKeyguardLock;

    private int mRingMode = 0;
    private Vibrator mVibrator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        setType();

        ButterKnife.bind(this);

        setRingMode();

        setKeyGuardAndWakLock();

        setupUI();

        /* myApp.makeCall() is in onLoadComplete
        *  because of must after sound loaded
        * */
        myApp.addObserver(this);

        setVideoRotation();

        startIncomingRing();
    }

    @Override
    public void onDestroy() {
        mWakLock.release();
        if (mMediaPlayer != null) mMediaPlayer.release();
        mVibrator.cancel();
        mKeyguardLock.reenableKeyguard();
        myApp.rmObserver(this);
        mSoundPool.release();
        myApp.stopVideoWindow();
        super.onDestroy();
    }

    private void startIncomingRing() {
        if (mType == TYPE.INCOMING) {
            switch (mRingMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    if (mMediaPlayer == null)
                        mMediaPlayer = MediaPlayer.create(this, RingtoneManager.getActualDefaultRingtoneUri(this,
                                RingtoneManager.TYPE_RINGTONE));
                    mMediaPlayer.setLooping(true);
                    try {
                        mMediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.start();
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    long[] pattern = {400, 1000, 400, 1000};
                    mVibrator.vibrate(pattern, 2);
                    break;
            }
        }
    }

    private void stopIncomingRing() {
        if (mType == TYPE.INCOMING) {
            switch (mRingMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    if (mMediaPlayer.isPlaying())
                        mMediaPlayer.stop();
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    mVibrator.cancel();
                    break;
            }
        }
    }

    private void setupUI() {
        if (mType == TYPE.INCOMING) {
            mAcceptBtn.setVisibility(View.VISIBLE);
            textInfo.setText(getString(R.string.from) + myApp.getCallInfo().getRemoteUri());
        } else {
            textInfo.setText(getString(R.string.is_calling) + mBuddy.getName());
        }

        mSoundWaitId = mSoundPool.load(this, R.raw.ringback, 1);
        mSoundPool.setOnLoadCompleteListener(this);
    }

    private void setType() {
        Serializable id = getIntent().getSerializableExtra(EXTRA_BUDDY_ID);
        if (id != null) {
            mType = TYPE.CALL;
            mBuddy = myApp.getTbBuddyDao().findById((Long) id);
        } else {
            mType = TYPE.INCOMING;
        }
    }

    private void setKeyGuardAndWakLock() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        //得到键盘锁管理器对象
        mKeyguardLock = km.newKeyguardLock("unLock");
        //参数是LogCat里用的Tag
        mKeyguardLock.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        mWakLock.acquire();
    }

    private void setRingMode() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRingMode = audio.getRingerMode();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void startRingBackSound() {
        if (mType == TYPE.CALL)
            mSoundPool.play(mSoundWaitId, 1, 1, 0, 999, 1);
    }

    private void stopRingBackSound() {
        if (mType == TYPE.CALL)
            mSoundPool.stop(mSoundWaitId);
    }

    private boolean mIsNeedHanup = true;
    private void makeCall(String id) {
        if (!MyUtil.isNetworkConnected(this)){
            mIsNeedHanup = false;
            textInfo.setText(R.string.no_network_connected);
        }else if (myApp.isCallAlreadyExist(id)){
            mIsNeedHanup = false;
            textInfo.setText(R.string.call_not_disconnected_completely);
        }else{
            mIsNeedHanup = true;
            myApp.makeCall(id);
        }
    }

    public void hangupCall(View v) {
        if (mIsNeedHanup) myApp.hangup();
        finish();
    }

    public void acceptCall(View v) {
        myApp.acceptCall();
    }

    private void uiAcceptCall() {
        mBtnLayout.setVisibility(View.GONE);
        mVidLayout.setVisibility(View.VISIBLE);
        myApp.updateVideoWindow(mVidRender.getHolder().getSurface(), true);
//        myApp.updateCaptureVideoWindow(mVidCapture.getHolder().getSurface());
    }

    private void setVideoRotation() {
        WindowManager wm;
        Display display;
        int rotation;
        pjmedia_orient orient;

        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        rotation = display.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:   // Portrait
                orient = pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG;
                break;
            case Surface.ROTATION_90:  // Landscape, home button on the right
                orient = pjmedia_orient.PJMEDIA_ORIENT_NATURAL;
                break;
            case Surface.ROTATION_180:
                orient = pjmedia_orient.PJMEDIA_ORIENT_ROTATE_90DEG;
                break;
            case Surface.ROTATION_270: // Landscape, home button on the left
                orient = pjmedia_orient.PJMEDIA_ORIENT_ROTATE_180DEG;
                break;
            default:
                orient = pjmedia_orient.PJMEDIA_ORIENT_UNKNOWN;
        }

        if (MyApp.ep != null && myApp.mAcc != null) {
            try {
                AccountConfig cfg = myApp.mAcc.cfg;
                int cap_dev = cfg.getVideoConfig().getDefaultCaptureDevice();
                MyApp.ep.vidDevManager().setCaptureOrient(cap_dev, orient,
                        true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setVideoRotation();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (mType == TYPE.CALL) makeCall(mBuddy.getPhone());
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mLastCode == pjsip_status_code.PJSIP_SC_RINGING) {
                        startRingBackSound();
                    } else if (mLastCode == pjsip_status_code.PJSIP_SC_OK) {
                        stopRingBackSound();
                        stopIncomingRing();
                        uiAcceptCall();
                    }

                    break;
                case 2:
                    String text = "";
                    if (mLastCode == pjsip_status_code.PJSIP_SC_NOT_FOUND) {
                        text = getString(R.string.sc_not_found);
                    }else if (mLastCode == pjsip_status_code.PJSIP_SC_TEMPORARILY_UNAVAILABLE){
                        text=getString(R.string.sc_temporarily_unavailable);
                    }else  if( mLastCode == pjsip_status_code.PJSIP_SC_BUSY_HERE){
                        text=getString(R.string.sc_busy_here);
                    }else if (mLastCode == pjsip_status_code.PJSIP_SC_OK || mLastCode == pjsip_status_code.PJSIP_SC_DECLINE){
                        text = "";
                    }
                    else{
                        text = ""+mLastCode;
                    }
                    if (text != null && text.length() > 0) {
                        showMsg(text);
                    }
                    a.b("mLastCode:" + mLastCode);
                    hangupCall(null);
                    break;
            }
        }
    };

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {

    }

    @Override
    public void notifyIncomingCall(MyCall call) {

    }

    @Override
    public void notifyCallState(MyCall call) {
        CallInfo ci;
        try {
            ci = call.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
            ci = null;
        }

        if (ci != null) {

            // must add try catch
            try {
                pjsip_status_code code = ci.getLastStatusCode();
                mStatues = "" + code;
                mLastCode = code;
                a.b("code:" + mLastCode);
                handler.sendEmptyMessage(1);
            } catch (Exception e) {

            }
        }

        if (ci != null &&
                ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            handler.sendEmptyMessage(2);
        }
    }

    @Override
    public void notifyCallMediaState(MyCall call) {

    }


}
