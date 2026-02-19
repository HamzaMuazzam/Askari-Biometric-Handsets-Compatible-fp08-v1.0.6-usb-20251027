package com.dawn.java.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.dawn.decoderapijni.SoftEngine;
import com.dawn.decoderapijni.bean.AttrHelpBean;
import com.dawn.decoderapijni.R;

public abstract class AttrSettingSwitchView extends LinearLayout {
    private static final String TAG = "ScanJni DLScan";
    private LinearLayout ll_param_setting;
    private TextView tv_param_name;
    private Switch switch_enable;
    private AttrHelpBean mAttrHelpBean;
    private String mCodeName;

    public AttrSettingSwitchView(Context context, String codeName, AttrHelpBean attrHelpBean) {
        super(context);
        mCodeName = codeName;
        mAttrHelpBean = attrHelpBean;
        initView(context);
    }

    public AttrSettingSwitchView(Context context, AttributeSet attrs, String codeName, AttrHelpBean attrHelpBean) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_switch_view, this, true);
        ll_param_setting = findViewById(R.id.ll_param_setting);
        tv_param_name = findViewById(R.id.tv_param_name);
        switch_enable = findViewById(R.id.switch_enable);

        tv_param_name.setText(mAttrHelpBean.getCnName());


    }

    // 设置初始值 和 listener
    public void initValueAndListener(Boolean isEnable) {
        switch_enable.setOnCheckedChangeListener(null);
        switch_enable.setChecked(isEnable);
        // 在初始值设置完成后再设置CheckedChangeListener
        switch_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.v(TAG, "onCheckedChanged");
                String value = "0";
                if (b) {
                    value = "1";
                }
                SoftEngine.getInstance().ScanSet(mCodeName, mAttrHelpBean.getName(), value);
                afterChangeCheckedAction();
            }
        });
    }

//    public void changeCheckState(Boolean isEnable) {
//        switch_enable.setChecked(isEnable);
//        afterChangeCheckedAction();
//    }

    abstract public void afterChangeCheckedAction();

    public AttrHelpBean getmAttrHelpBean() {
        return mAttrHelpBean;
    }
}
