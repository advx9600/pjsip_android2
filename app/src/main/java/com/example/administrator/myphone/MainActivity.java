package com.example.administrator.myphone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.administrator.myphone.a.a.a.a.a;
import com.example.administrator.myphone.a.a.a.a.d;
import com.example.administrator.myphone.dao.TbUserDaoImp;
import com.example.administrator.myphone.db.DB;
import com.example.administrator.myphone.fragment.MeFragment;
import com.example.administrator.myphone.fragment.PhoneFragment;
import com.loopj.android.http.AsyncHttpClient;

import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.pjsip_status_code;

import gen.DaoSession;
import gen.TbBuddyDao;
import gen.TbConfigDao;
import gen.TbUser;
import gen.TbUserDao;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainActivityInt, MyAppObserver {
    private TbConfigDao mConfigDao;
    private TbUserDao mUserDao;
    private TbUser mTbUser;
    private TbBuddyDao mBuddyDao;

    private MyApp myApp = MyService.myApp;
    /* navigation bar top text show,as registered,not registered */
    private TextView mTextStatus;
    private Fragment mCurFragment;

    private FloatingActionButton mFab;

    private void stopMyService() {
        MyUtil.stopMyService(this);
    }

    private static AsyncHttpClient mHTTPClient = new AsyncHttpClient();

    /* wait for myApp init */
    private boolean isNeedWait() {
        if (!myApp.isStarted()) {
            return true;
        }
        return false;
    }

    private void initDB() {
        /* must use service's db else data not synchronized */
        mConfigDao = myApp.getTbConfigDao();
        mUserDao = myApp.getTbUserDao();
        mBuddyDao = myApp.getTbBuddyDao();
//        DaoSession dao = DB.getDaoSession(this);
//        mConfigDao=dao.getTbConfigDao();
//        mUserDao = dao.getTbUserDao();
//        mBuddyDao = dao.getTbBuddyDao();
        mTbUser = TbUserDaoImp.getUser(mUserDao);

    }

    Toolbar mToolbar;

    private void setUI() {
        // ui setup
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mToolbar = toolbar;

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView view = (TextView) headerLayout.findViewById(R.id.textView);
        view.setText("sip:" + mTbUser.getUsername() + "@" + mTbUser.getDomain());

        mTextStatus = (TextView) headerLayout.findViewById(R.id.text_statues);
    }

    /* this is the last step */
    private void setInitData() {
        handler.sendEmptyMessage(MSG_REGISTE_NOTIFY);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (MyUtil.getErrLog(prefs).length() > 0){
            MyUtil.alertConfirm(this,MyUtil.processErrMsg(this,MyUtil.getErrLog(prefs)));
            MyUtil.setErrLog(prefs, "", true);
        }
    }

    private final static int MSG_REGISTE_NOTIFY = 1;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTE_NOTIFY:
                    String text = "";
                    if (myApp.getRegStatus() == null) {
                        text = getString(R.string.registering);
                    } else if (myApp.getRegStatus() == pjsip_status_code.PJSIP_SC_OK) {
                        text = getString(R.string.registered);
                    } else {
                        text = getString(R.string.register_failed) + ":" + myApp.getRegStatus();
                    }
                    mTextStatus.setText(text);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isContinue = true;

        if (isNeedWait()) {
            isContinue = false;
            MyUtil.startIntent(this, WaitInitActivity.class);
        }

        MyUtil.startMySevice(this);

        /* start background service */
        if (isContinue) initDB();

        /* if no user configured */
        if (isContinue && mUserDao.queryBuilder().count() == 0) {
            isContinue = false;
            LoginActivity.mUserDao = mUserDao;
            MyUtil.startIntent(this, LoginActivity.class);
        }
        /* if need to close window */
        if (!isContinue) {
            finish();
            return;
        }

        /* add observer */
        if (isContinue) myApp.addObserver(this);
        /* setup UI */
        setUI();
        /* set init data */
        setInitData();
    }

    @Override
    public void onDestroy() {
        myApp.rmObserver(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        if (mCurFragment !=null){
//            if (mCurFragment instanceof PhoneFragment){
//                return  true;
//            }
//        }
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_total_quit) {
            stopMyService();
            finish();
            return true;
        } else if (id == R.id.action_settings) {
            MyUtil.startIntent(this, SettingsActivity.class);
        } else if (id == R.id.action_log_out) {
            myApp.logOut(this);
            finish();
        } else if (id == R.id.action_upgrade){
            /* check and then download */
            mHTTPClient.get("http://120.24.77.212/getInfo.php",new d(this,"http://120.24.77.212/download.php"));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (id == R.id.nav_phone) {
            // Handle the camera action
            fragment = new PhoneFragment(this, mBuddyDao);
//            MyUtil.startIntent(this, PhoneActivity.class);
        } else if (id == R.id.nav_me) {
            fragment = new MeFragment();
        }

        if (fragment != null) {
            mCurFragment = fragment;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (fragment != null) {
            if (fragment instanceof PhoneFragment) {
                mFab.setVisibility(View.INVISIBLE);
            } else {
                mFab.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }


    @Override
    public void doFinish() {
        finish();
    }


    /* call statues */
    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        handler.sendEmptyMessage(MSG_REGISTE_NOTIFY);
    }

    @Override
    public void notifyIncomingCall(MyCall call) {
    }

    @Override
    public void notifyCallState(MyCall call) {

    }

    @Override
    public void notifyCallMediaState(MyCall call) {

    }
}
