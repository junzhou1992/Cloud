package com.thisway.xunfeicloud;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.EditText;

/**
 * Created by jun on 2018/1/23.
 */

public class settingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

    public static final String PREFER_NAME = "com.iflytek.setting";
    private EditTextPreference mSpeedPreference;
    private EditTextPreference mPitchPreference;
    private EditTextPreference mVolumePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFER_NAME);//改变默认保存设置的文件
        this.addPreferencesFromResource(R.xml.activity_setting);

        mSpeedPreference = (EditTextPreference)findPreference("speed_preference");
        mSpeedPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(),mSpeedPreference,0,100));

        mPitchPreference = (EditTextPreference)findPreference("pitch_preference");
        mPitchPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(),mPitchPreference,0,100));

        mVolumePreference = (EditTextPreference)findPreference("volume_preference");
        mVolumePreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(),mVolumePreference,0,100));

    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return true;
    }
}
