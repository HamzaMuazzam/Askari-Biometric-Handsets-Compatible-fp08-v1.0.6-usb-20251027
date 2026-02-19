package com.dawn.java;

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;

import com.dawn.decoderapijni.R;

import java.util.Locale;

public class ScanApplication extends Application {
    public static final String TAG = "ScanJni DLScan";
    public static final String BUNDLE_KEY_CODE_NAME = "codeName";
    public static final String BUNDLE_KEY_ATTR_BEAN = "attrHelpBean";
    public static final String BUNDLE_KEY_CUR_FRAGMENT = "curFragment";
    public static final int CODE_SETTING_REQUEST_CODE = 1001;
    public static final int PARAM_SETTING_REQUEST_CODE = 2001;
    public static final int CODE_SETTING_SUCCESS_RESULT_CODE = 1;

    public static final String ATTR_HELP_BEAM_TYPE_SWITCH = "switch";
    public static final String ATTR_HELP_BEAM_TYPE_EDIT = "edit";
    public static final String ATTR_HELP_BEAM_TYPE_RADIO = "radio";


    public static SoundPool mSoundPool;
    public static Vibrator vibrator;
    public static int soundId = 1;
    public static Context mContext;

    public static Locale systemLocale;

    @Override
    public void onCreate()
    {
        super.onCreate();
        initSound();
        getLocalLang();
        mContext = getApplicationContext();
    }

    private void getLocalLang(){
        systemLocale = getResources().getConfiguration().getLocales().get(0);
    }

    private void initSound(){
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 10);
        vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        soundId = mSoundPool.load(getApplicationContext(), R.raw.dingdj5, 1);
    }
}
