package com.example.administrator.myphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/3/17.
 */

class DesktopLayout extends LinearLayout{

    public DesktopLayout(Context context){
        super(context);
        setOrientation(LinearLayout.VERTICAL);// 水平排列


        //设置宽高
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));

        View view=LayoutInflater.from(context).inflate(
        R.layout.desk_layout,null);
        this.addView(view);
    }
}

public class MyAppBaseFloatWindow  {
    private static final String TAG = "FloatWindowTest";
    private Context mCon;
    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    Button mFloatView;

    public MyAppBaseFloatWindow(Context con){
        mCon = con;
        createFloatView();
    }

    private void createFloatView()
    {
        wmParams = new WindowManager.LayoutParams();
        //获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager)mCon.getSystemService(Context.WINDOW_SERVICE);
        Log.i(TAG, "mWindowManager--->" + mWindowManager);
        //设置window type
        wmParams.type = LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

         /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/

        LayoutInflater inflater = LayoutInflater.from(mCon);
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.desk_layout, null);
//        mFloatLayout.setVisibility(View.GONE);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        //浮动窗口按钮
//        mFloatView = (Button)mFloatLayout.findViewById(R.id.float_id);
//
//        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
//                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
//                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
//        Log.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight()/2);
//        //设置监听浮动窗口的触摸移动
//        mFloatView.setOnTouchListener(new OnTouchListener()
//        {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event)
//            {
//                // TODO Auto-generated method stub
//                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
//                wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth()/2;
//                Log.i(TAG, "RawX" + event.getRawX());
//                Log.i(TAG, "X" + event.getX());
//                //减25为状态栏的高度
//                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight()/2 - 25;
//                Log.i(TAG, "RawY" + event.getRawY());
//                Log.i(TAG, "Y" + event.getY());
//                //刷新
//                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
//                return false;  //此处必须返回false，否则OnClickListener获取不到监听
//            }
//        });
//
//        mFloatView.setOnClickListener(new OnClickListener()
//        {
//
//            @Override
//            public void onClick(View v)
//            {
//                // TODO Auto-generated method stub
//                Toast.makeText(FxService.this, "onClick", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

}
