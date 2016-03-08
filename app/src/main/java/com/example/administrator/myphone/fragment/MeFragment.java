package com.example.administrator.myphone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.myphone.R;

/**
 * Created by Administrator on 2016/2/26.
 */
public class MeFragment extends android.support.v4.app.Fragment {

    private View mView;
    private TextView mText;

    public MeFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        mView = view;
        setHasOptionsMenu(true);

        mText = (TextView) view.findViewById(R.id.text_show);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.fragment_phone, menu);
//        super.onCreateOptionsMenu(menu,inflater);
    }
}
