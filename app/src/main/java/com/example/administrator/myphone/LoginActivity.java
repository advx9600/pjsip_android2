package com.example.administrator.myphone;

/**
 * Created by Administrator on 2016/2/24.
 */
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myphone.a.a.a.a.a;

import org.pjsip.pjsua2.pjsip_status_code;

import butterknife.ButterKnife;
import butterknife.Bind;
import gen.TbConfig;
import gen.TbConfigDao;
import gen.TbUser;
import gen.TbUserDao;

public class LoginActivity extends AppCompatActivity  implements MyAppObserver{
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    public static TbUserDao mUserDao;

    private ProgressDialog progressDialog ;
    private MyApp myApp = MyService.myApp;

    private String mReason = "";
    private TbUser mUser;

    @Bind(R.id.input_username) EditText _usernameText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.input_domain) EditText _domainText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;


    /* may receive pre account msg */
    private boolean mIsLogin = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        myApp.addObserver(this);

        progressDialog = new ProgressDialog(LoginActivity.this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                a.b("signupLink");
//                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
//                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        _domainText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login();
                return false;
            }
        });
    }

    @Override
    public void onDestroy(){
        myApp.rmObserver(this);
        super.onDestroy();
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

//        _loginButton.setEnabled(false);


        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.registering)+ "...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();
        String domain = _domainText.getText().toString();

        TbUser user = new TbUser();
        user.setUsername(username);
        user.setPwd(password);
        user.setDomain(domain);
        mUser = user;
        // TODO: Implement your own authentication logic here.
        mIsLogin  = true;
        myApp.resetAccount(user);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
//        moveTaskToBack(true);
        super.onBackPressed();
        MyUtil.stopMyService(this);
    }

    public void onLoginSuccess() {
        mUserDao.insert(mUser);
        _loginButton.setEnabled(true);
        progressDialog.dismiss();
        finish();
        MyUtil.startIntent(this,MainActivity.class);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.login_failed)+":"+mReason, Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();
        String domain = _domainText.getText().toString();

        if (username.isEmpty()){
            _usernameText.setError("enter a valid username");
            valid = false;
        }else {
            _usernameText.setError(null);
        }

        if (password.isEmpty()){
            _passwordText.setError("enter a valid password");
            valid = false;
        }else {
            _passwordText.setError(null);
        }

        if (domain.isEmpty()){
            _domainText.setError("enter a valid domain");
            valid = false;
        }else {
            _domainText.setError(null);
        }

//        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            _usernameText.setError("enter a valid email address");
//            valid = false;
//        } else {
//            _usernameText.setError(null);
//        }
//
//        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
//            _passwordText.setError("between 4 and 10 alphanumeric characters");
//            valid = false;
//        } else {
//            _passwordText.setError(null);
//        }

        return valid;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (!mIsLogin){
                return;
            }

            mIsLogin = false;

            switch (msg.what){
                case 1:
                    onLoginSuccess();
                    break;
                case 2:
                    onLoginFailed();
                    break;
            }
        }
    };
    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        if (code == pjsip_status_code.PJSIP_SC_OK){
            handler.sendEmptyMessage(1);
        }else{
            handler.sendEmptyMessage(2);
            mReason = reason;
        }
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