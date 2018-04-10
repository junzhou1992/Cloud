package com.thisway.xunfeicloud;

import android.app.Application;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import com.iflytek.cloud.SpeechUtility;

import org.litepal.LitePal;


/**
 * Created by jun on 2018/1/17.
 */

public class MyApplication extends Application{

    private static Context context;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        LitePal.initialize(context);
        SpeechUtility.createUtility(context, "appid=" + getString(R.string.app_id));
        //LogUtil.d("tag","myapplication");
        super.onCreate();
    }

    public static Context getContext(){
        return context;
    }


}
