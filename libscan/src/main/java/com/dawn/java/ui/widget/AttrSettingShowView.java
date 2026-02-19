package com.dawn.java.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dawn.decoderapijni.bean.AttrHelpBean;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.PropValueSetActivity;
import com.dawn.decoderapijni.R;

public class AttrSettingShowView extends LinearLayout {
    private LinearLayout ll_param_setting;
    private TextView tv_param_name;
    private TextView tv_param_detail;
    private AttrHelpBean mAttrHelpBean;
    private String mCodeName;

    public AttrSettingShowView(Context context, String codeName, AttrHelpBean attrHelpBean) {
        super(context);
        mCodeName = codeName;
        mAttrHelpBean = attrHelpBean;
        initView(context);
    }

    public AttrSettingShowView(Context context, AttributeSet attrs, String codeName, AttrHelpBean attrHelpBean) {
        super(context, attrs);
        mCodeName = codeName;
        mAttrHelpBean = attrHelpBean;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_param_view, this, true);
        ll_param_setting = findViewById(R.id.ll_param_setting);
        tv_param_name = findViewById(R.id.tv_param_name);
        tv_param_detail = findViewById(R.id.tv_param_detail);
        tv_param_name.setText(mAttrHelpBean.getCnName());
        ll_param_setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PropValueSetActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString(ScanApplication.BUNDLE_KEY_CODE_NAME, mCodeName);
                bundle.putSerializable(ScanApplication.BUNDLE_KEY_ATTR_BEAN, mAttrHelpBean);
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });
    }



    public void setValue(String paramDetail) {
        tv_param_detail.setText(paramDetail);
    }
}
