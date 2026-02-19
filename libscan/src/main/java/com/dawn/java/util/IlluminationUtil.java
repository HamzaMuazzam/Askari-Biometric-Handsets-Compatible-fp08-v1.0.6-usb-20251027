package com.dawn.java.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dawn.decoderapijni.SoftEngine;


public class IlluminationUtil {
    private static final String TAG = "IlluminationUtil";
    private static final String LIGHT_DATAFILE = "light_data_file";
    private static final String LIGHT_EFFECT_ENABLE = "light_effect_enable";
    private static final String LIGHT_DEF_EXCPECT_BRIGHTNESS = "light_default_excpect_brightness";
    private static final String LIGHT_DEF_TIME = "light_default_time";
    private static final String LIGHT_DEF_MAX_EXPOSURE = "light_default_max_exposure";
    private static final String LIGHT_DEF_MIN_EXPOSURE = "light_default_min_exposure";
    private static final String LIGHT_DEF_MAX_GAIN = "light_default_max_gain";
    private static final String LIGHT_DEF_MIN_GAIN = "light_default_min_gain";
    private static final String LIGHT_EXCPECT_BRIGHTNESS = "light_excpect_brightness";
    private static final String LIGHT_TIME = "light_time";
    private static final String LIGHT_MAX_EXPOSURE = "light_max_exposure";
    private static final String LIGHT_MIN_EXPOSURE = "light_min_exposure";
    private static final String LIGHT_MAX_GAIN = "light_max_gain";
    private static final String LIGHT_MIN_GAIN = "light_min_gain";
    private static final String LIGHT_INIT_FLAG = "light_init_flag";
    private static final int EXPOSRUEMAX = 14000;
    private static final int GAINMAX = 64;

    static SharedPreferences sp_lightdata;
    static SharedPreferences.Editor sp_lightdata_edit;

    public static void InitSPData(Context context)
    {
        int init_flag;
        sp_lightdata = context.getSharedPreferences(LIGHT_DATAFILE, MODE_PRIVATE);;
        sp_lightdata_edit = sp_lightdata.edit();
        init_flag = sp_lightdata.getInt(LIGHT_INIT_FLAG,0);
        if(init_flag == 0) {
            sp_lightdata_edit.putBoolean(LIGHT_EFFECT_ENABLE, false);

            sp_lightdata_edit.putInt(LIGHT_DEF_EXCPECT_BRIGHTNESS, SoftEngine.getInstance().getExpectBrightness());
            sp_lightdata_edit.putInt(LIGHT_DEF_TIME, SoftEngine.getInstance().getLightTime());
            sp_lightdata_edit.putInt(LIGHT_DEF_MAX_EXPOSURE,SoftEngine.getInstance().getMaxExposure());
            sp_lightdata_edit.putInt(LIGHT_DEF_MIN_EXPOSURE,SoftEngine.getInstance().getMinExposure());
            sp_lightdata_edit.putInt(LIGHT_DEF_MAX_GAIN, SoftEngine.getInstance().getMaxGain());
            sp_lightdata_edit.putInt(LIGHT_DEF_MIN_GAIN, SoftEngine.getInstance().getMinGain());

            sp_lightdata_edit.putInt(LIGHT_EXCPECT_BRIGHTNESS, SoftEngine.getInstance().getExpectBrightness());
            sp_lightdata_edit.putInt(LIGHT_TIME, SoftEngine.getInstance().getLightTime());
            sp_lightdata_edit.putInt(LIGHT_MAX_EXPOSURE,SoftEngine.getInstance().getMaxExposure());
            sp_lightdata_edit.putInt(LIGHT_MIN_EXPOSURE,SoftEngine.getInstance().getMinExposure());
            sp_lightdata_edit.putInt(LIGHT_MAX_GAIN, SoftEngine.getInstance().getMaxGain());
            sp_lightdata_edit.putInt(LIGHT_MIN_GAIN, SoftEngine.getInstance().getMinGain());

            sp_lightdata_edit.putInt(LIGHT_INIT_FLAG, 1);
            sp_lightdata_edit.commit();
        }
        Log.i(TAG,"InitSPData");
    }

    static public void DataLoad()
    {
        int value;
        value = getExcpectBrightness(1);
        setExcpectBrightness(value);

//        value = getLightTime(1);
//        setLightTime(value);

        value = getMaxExposure(1);
        setMaxExposure(value);

        value = getMinExposure(1);
        setMinExposure(value);

        value = getMaxGain(1);
        setMaxGain(value);

        value = getMinGain(1);
        setMinGain(value);
    }

    public static int weight2value(int weight,int max)
    {
        float avg = (float)max/10;
        int i;
        float sum[] = new float[11];
        sum[0] = 0;

        if(weight < 1 && weight >10)
            return -1;

        for(i =0 ;i < 10;i++)
        {
            sum[i+1] = sum[i] + avg;
        }
        return (int)((sum[weight] + sum[weight - 1])/2);
    }

    public static int value2weight(int value,int max)
    {
        float avg = (float)max/10;
        int i;
        float sum[] = new float[11];;
        sum[0] = 0;

        if(value < 0 && value >max)
            return -1;

        for(i =0 ;i < 10;i++)
        {
            sum[i+1] = sum[i] + avg;
            if(value < sum[i+1])
                break;
        }
        if(i == 10)
            i=9;
        return i+1;
    }

    public static void setEffectEnable(boolean enable)
    {
        sp_lightdata_edit.putBoolean(LIGHT_EFFECT_ENABLE,enable);
        sp_lightdata_edit.commit();
    }

    public static boolean getEffectEnable()
    {
        return sp_lightdata.getBoolean(LIGHT_EFFECT_ENABLE,false);
    }

    public static void setExcpectBrightness(int value)
    {
        sp_lightdata_edit.putInt(LIGHT_EXCPECT_BRIGHTNESS,value);
        sp_lightdata_edit.commit();
        SoftEngine.getInstance().setExpectBrightness(value);
    }

    public static int getExcpectBrightness(int flag)
    {
        int value;
        if(flag == 0)
            value = sp_lightdata.getInt(LIGHT_DEF_EXCPECT_BRIGHTNESS,0);
        else
            value = sp_lightdata.getInt(LIGHT_EXCPECT_BRIGHTNESS,0);
        return value;
    }

    public static void setLightTime(int value)
    {
        sp_lightdata_edit.putInt(LIGHT_TIME,value);
        sp_lightdata_edit.commit();
        SoftEngine.getInstance().setLightTime(value);
    }

    public static int getLightTime(int flag)
    {
        int value;
        if(flag == 0)
            value = sp_lightdata.getInt(LIGHT_DEF_TIME,0);
        else
            value = sp_lightdata.getInt(LIGHT_TIME,0);
        return value;
    }

    public static void setMaxExposure(int value)
    {
        sp_lightdata_edit.putInt(LIGHT_MAX_EXPOSURE,value);
        sp_lightdata_edit.commit();
        SoftEngine.getInstance().setMaxExposure(value);
    }

    public static int getMaxExposure(int flag)
    {
        int value;
        if(flag == 0)
            value = sp_lightdata.getInt(LIGHT_DEF_MAX_EXPOSURE,0);
        else
            value = sp_lightdata.getInt(LIGHT_MAX_EXPOSURE,0);
        return value;
    }

    public static void setMinExposure(int value)
    {
        sp_lightdata_edit.putInt(LIGHT_MIN_EXPOSURE,value);
        sp_lightdata_edit.commit();
        SoftEngine.getInstance().setMinExposure(value);
    }

    public static int getMinExposure(int flag)
    {
        int value;
        if(flag == 0)
            value = sp_lightdata.getInt(LIGHT_DEF_MIN_EXPOSURE,0);
        else
            value = sp_lightdata.getInt(LIGHT_MIN_EXPOSURE,0);
        return value;
    }

    public static void setMaxGain(int value)
    {
        sp_lightdata_edit.putInt(LIGHT_MAX_GAIN,value);
        sp_lightdata_edit.commit();
        SoftEngine.getInstance().setMaxGain(value);
    }

    public static int getMaxGain(int flag)
    {
        int value;
        if(flag == 0)
            value = sp_lightdata.getInt(LIGHT_DEF_MAX_GAIN,0);
        else
            value = sp_lightdata.getInt(LIGHT_MAX_GAIN,0);
        return value;
    }

    public static void setMinGain(int value)
    {
        sp_lightdata_edit.putInt(LIGHT_MIN_GAIN,value);
        sp_lightdata_edit.commit();
        SoftEngine.getInstance().setMinGain(value);
    }

    public static int getMinGain(int flag)
    {
        int value;
        if(flag == 0)
            value = sp_lightdata.getInt(LIGHT_DEF_MIN_GAIN,0);
        else
            value = sp_lightdata.getInt(LIGHT_MIN_GAIN,0);
        return value;
    }

    public static void setGainWeight(int weight)
    {
        int value;
        value = weight2value(weight,GAINMAX);
        setMaxGain(value);
        setMinGain(value);
    }

    public static int getGainWeight()
    {
        return value2weight(getMaxGain(1),GAINMAX);
    }

    public static void setExposureWeight(int weight)
    {
        int value;
        value = weight2value(weight,EXPOSRUEMAX);
        setMaxExposure(value);
        setMinExposure(value);
    }

    public static int getExposureWeight()
    {
        return value2weight(getMaxExposure(1),EXPOSRUEMAX);
    }

}
