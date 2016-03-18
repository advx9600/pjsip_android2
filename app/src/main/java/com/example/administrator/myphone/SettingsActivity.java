package com.example.administrator.myphone;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.example.administrator.myphone.a.a.a.a.a;

import java.util.List;

/**
 * Created by Administrator on 2016/2/29.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";
    private MyApp myApp = MyService.myApp;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    /* is preference value changed */
    private static boolean mIsChanged = false;
    private static int mCreateTimes = 0;
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof EditTextPreference ) {
                if (preference.getSummary() != null && !stringValue.equals(preference.getSummary().toString())) {
                    mIsChanged = true;
                }
            }else if (preference instanceof CheckBoxPreference){
                if (!stringValue.equals(""+((CheckBoxPreference) preference).isChecked())){
                    mIsChanged=true;
                }
            }

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                }else if (preference instanceof CheckBoxPreference) {

                }else
                 {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof CheckBoxPreference) {

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        SharedPreferences sharePre = PreferenceManager
                .getDefaultSharedPreferences(preference.getContext());
        String value = "";
        try {
            value = sharePre.getString(preference.getKey(), "");
        } catch (Exception e1) {
//            a.b("e1:"+e1.getMessage());
            try {
                value = sharePre.getLong(preference.getKey(), -1) + "";
            } catch (Exception e2) {
//                a.b("e2:"+e2.getMessage());
                try {
                    value = sharePre.getBoolean(preference.getKey(), false) + "";
                } catch (Exception e3) {
//                    a.b("e3:"+e3.getMessage());
                }
            }
        }
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                value);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        mCreateTimes++;
        if (mCreateTimes == 1) {
            mIsChanged = false;
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GenneralPreferenceFragment.class.getName().equals(fragmentName)
                || PhonePreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public final static String KEY_LOG_LEVEL = "log_level";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GenneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(KEY_LOG_LEVEL));
        }
    }

    public final static String KEY_REGISTER_EXPIRE_TIME = "register_expire_text";
    public final static String KEY_ENABLE_STUN_SERVER = "stun_server_enable";
    public final static String KEY_ENABLE_TURN_SERVER = "turn_server_enable";
    public final static String KEY_ENABLE_ICE = "ice_enable";
    public final static String KEY_STUN_SERVER = "stun_server";
    public final static String KEY_TURN_SERVER = "turn_server";
    public final static String KEY_SIP_PORT = "sip_port";
//    public final static String KEY_ENABLE_WAKELOCK = "wakelock_enable";

    public final static String KEY_ENABLE_VIDEO = "video_enable";
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PhonePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_phone);
//            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(KEY_REGISTER_EXPIRE_TIME));
            bindPreferenceSummaryToValue(findPreference(KEY_STUN_SERVER));
            bindPreferenceSummaryToValue(findPreference(KEY_SIP_PORT));
            bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_STUN_SERVER));
            bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_ICE));
            bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_VIDEO));
            bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_TURN_SERVER));
            bindPreferenceSummaryToValue(findPreference(KEY_TURN_SERVER));
//            bindPreferenceSummaryToValue(findPreference(KEY_ENABLE_WAKELOCK));
        }
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                //do your action here.
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCreateTimes--;
        if (mCreateTimes == 0 && mIsChanged) {
            a.b4(TAG,"reset sip lib");
            myApp.saveCurData();
            myApp.resetSipParam();
            myApp.restoreSaveData();
        }
    }
}
