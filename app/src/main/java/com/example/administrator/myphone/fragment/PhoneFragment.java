package com.example.administrator.myphone.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.myphone.CallActivity;
import com.example.administrator.myphone.MainActivityInt;
import com.example.administrator.myphone.MyAdActivity;
import com.example.administrator.myphone.MyApp;
import com.example.administrator.myphone.MyAppObserver;
import com.example.administrator.myphone.MyCall;
import com.example.administrator.myphone.MyService;
import com.example.administrator.myphone.MyUtil;
import com.example.administrator.myphone.R;
import com.example.administrator.myphone.a.a.a.a.a;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AuthCredInfo;
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
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import gen.TbBuddy;
import gen.TbBuddyDao;

/**
 * Created by Administrator on 2016/2/23.
 */
public class PhoneFragment extends Fragment  {
    private View mView;
    private ListView mListView;
    private MyApp myApp = MyService.myApp;

    private TbBuddyDao mBuddyDao;

    public PhoneFragment(MainActivityInt mainInt,TbBuddyDao buddyDao){
//        mInt = mainInt;
        mBuddyDao=buddyDao;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupUI(){
        mListView = (ListView) mView.findViewById(R.id.list_main);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        mView = view;
        setHasOptionsMenu(true);
        setupUI();
        refreshList();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_phone, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add){
            final View view=getActivity().getLayoutInflater().inflate(R.layout.add_contact, null);
            final EditText textPhone= (EditText) view.findViewById(R.id.input_phone);
            final TextView textName= (TextView) view.findViewById(R.id.input_name);
            MyUtil.alertYesCanel(view, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String phone = textPhone.getText().toString();
                    String name = textName.getText().toString();
                    if (phone.length() > 0 && name.length() > 0) {
                        TbBuddy buddy = new TbBuddy();
                        buddy.setName(name);
                        buddy.setPhone(phone);
                        myApp.addBudy(buddy);
                        refreshList();
                    }
                }
            });

        }
        return true;
    }

    private List<TbBuddy> mListBuddyData;
    private TbBuddy mCurBuddy;
    private void refreshList(){
        mListView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1,getData()));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (myApp.getRegStatus() !=null && myApp.getRegStatus() == pjsip_status_code.PJSIP_SC_OK) {
                    mCurBuddy = mListBuddyData.get(position);
                    Intent intent = new Intent(getContext(), CallActivity.class);
                    intent.putExtra(CallActivity.EXTRA_BUDDY_ID, mCurBuddy.getId());
                    MyUtil.startIntent(getContext(), intent);
                }else {
                    MyUtil.toast(getContext(),R.string.account_no_registered);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                    public void onCreateContextMenu(ContextMenu menu, View v,
                                                    ContextMenu.ContextMenuInfo menuInfo) {
                        mCurBuddy = mListBuddyData.get(position);
                        menu.setHeaderTitle(mCurBuddy.getName());
                        menu.add(0, R.string.del, 0, R.string.del);
//                        menu.add(0, R.string.modify, 1, R.string.modify);
                    }
                });
                return false;
            }
        });

        this.registerForContextMenu(mListView);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.string.del){
            mBuddyDao.delete(mCurBuddy);
            refreshList();
        }
        return super.onContextItemSelected(item);
    }

    private List<String> getData(){
        List<String> data = new ArrayList<String>();
        List<TbBuddy> list = mBuddyDao.queryBuilder().orderAsc(TbBuddyDao.Properties.Phone).list();
        for (int i=0;i<list.size();i++){
            TbBuddy buddy = list.get(i);
            data.add(buddy.getName()+"("+buddy.getPhone()+")");
        }
        mListBuddyData = list;
        return data;
    }

}
