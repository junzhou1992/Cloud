package com.thisway.xunfeicloud;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jun on 2018/1/23.
 */

public class settingActivity extends AppCompatActivity{

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getFragmentManager().beginTransaction()
                .replace(android.R.id.content,new settingFragment())
                .commit();
    }


}
