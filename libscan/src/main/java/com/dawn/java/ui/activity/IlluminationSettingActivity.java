package com.dawn.java.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.java.util.IlluminationUtil;
import com.dawn.decoderapijni.R;

public class IlluminationSettingActivity extends DawnActivity implements View.OnClickListener{
    private TopToolbar topBar;
    private static final String TAG = "ScanJni DLScan";
    private Switch illumintion_enable,effect_enable;
    private EditText et_exp_brightness,et_light_time;
    private Spinner sp_gain,sp_exposure;
    private Button btn_save,btn_reset;
    private static final String[] weight={"1","2","3","4","5","6","7","8","9","10"};
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void effectEnableJudge(boolean enable,int startFlag)
    {
        //首次进入获取值，后续切换开关不赋值
        if(startFlag == 0)
        {
            sp_exposure.setSelection(IlluminationUtil.getExposureWeight()-1);
            sp_gain.setSelection(IlluminationUtil.getGainWeight()-1);
        }
        if(enable == true)
        {
//            et_light_time.setEnabled(true);
            effect_enable.setChecked(true);
            sp_exposure.setEnabled(true);
            sp_gain.setEnabled(true);
            et_exp_brightness.setEnabled(false);
            et_exp_brightness.setTextColor(Color.parseColor("#A9A9A9"));
//            et_light_time.setText(String.valueOf(IlluminationUtil.getLightTime(1)));
        }
        else
        {
//            et_light_time.setEnabled(false);
            sp_exposure.setEnabled(false);
            sp_gain.setEnabled(false);
            et_exp_brightness.setEnabled(true);
            et_exp_brightness.setTextColor(Color.parseColor("#000000"));
//            et_light_time.setText(" ");
        }
    }

    public void lightDataSave()
    {
        int value;
        if(IlluminationUtil.getEffectEnable() == true)
        {
//            value = Integer.parseInt(et_light_time.getText().toString());
//            IlluminationUtil.setLightTime(value);

            value = sp_exposure.getSelectedItemPosition() + 1;
            IlluminationUtil.setExposureWeight(value);

            value = sp_gain.getSelectedItemPosition() + 1;
            IlluminationUtil.setGainWeight(value);
        }
        else
        {
            value = Integer.parseInt(et_exp_brightness.getText().toString());
            if(value > 255) {
                Toast.makeText(IlluminationSettingActivity.this, "范围超出", Toast.LENGTH_SHORT).show();
                return;
            }
            else
                IlluminationUtil.setExcpectBrightness(value);
        }
        Toast.makeText(IlluminationSettingActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
    }

    public void resetLightData()
    {
        int value;
        value = IlluminationUtil.getExcpectBrightness(0);
        IlluminationUtil.setExcpectBrightness(value);
        et_exp_brightness.setText(String.valueOf(value));

        value = IlluminationUtil.getLightTime(0);
        IlluminationUtil.setLightTime(value);

        value = IlluminationUtil.getMaxExposure(0);
        IlluminationUtil.setMaxExposure(value);

        value = IlluminationUtil.getMinExposure(0);
        IlluminationUtil.setMinExposure(value);

        value = IlluminationUtil.getMaxGain(0);
        IlluminationUtil.setMaxGain(value);

        value = IlluminationUtil.getMinGain(0);
        IlluminationUtil.setMinGain(value);

//        et_light_time.setEnabled(false);
        sp_exposure.setSelection(IlluminationUtil.getExposureWeight()-1);
        sp_gain.setSelection(IlluminationUtil.getGainWeight()-1);
        sp_exposure.setEnabled(false);
        sp_gain.setEnabled(false);
        effect_enable.setChecked(false);

        Toast.makeText(IlluminationSettingActivity.this, "恢复成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_illumination_setting;
    }

    private void initView() {
        et_exp_brightness = findViewById(R.id.et_exp_brightness);
//        et_light_time = findViewById(R.id.et_light_time);
        sp_exposure = findViewById(R.id.sp_exposure);
        sp_gain = findViewById(R.id.sp_gain);
        btn_save = findViewById(R.id.btn_save);
        btn_reset = findViewById(R.id.btn_reset);
        btn_save.setOnClickListener(this);
        btn_reset.setOnClickListener(this);
        topBar = findViewById(R.id.topBar);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {
            }
        });

        illumintion_enable = findViewById(R.id.illumination_enable);
        illumintion_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.v(TAG, "illumintion_enable onCheckedChanged");
                if (b) {
                    SoftEngine.getInstance().setIlluminationEnable(1);
                }else{
                    SoftEngine.getInstance().setIlluminationEnable(0);
                }
            }
        });

        effect_enable = findViewById(R.id.effect_enable);
        effect_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.v(TAG, "effect_enable onCheckedChanged");
                effectEnableJudge(b,1);
                IlluminationUtil.setEffectEnable(b);
            }
        });

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,weight);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_exposure.setAdapter(adapter);
        sp_gain.setAdapter(adapter);
    }

    private void initData(){
        int rc;
        rc = SoftEngine.getInstance().getIlluminationEnable();
        Log.i(TAG,"getIlluminationEnablerc:"+rc);
        if (rc == 1) {
            illumintion_enable.setChecked(true);
        }
        et_exp_brightness.setText(String.valueOf(IlluminationUtil.getExcpectBrightness(1)));
        effectEnableJudge(IlluminationUtil.getEffectEnable(),0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_save) {
            lightDataSave();
        } else if (id == R.id.btn_reset) {
            resetLightData();
        }
    }
}
