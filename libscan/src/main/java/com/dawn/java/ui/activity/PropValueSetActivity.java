package com.dawn.java.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.decoderapijni.bean.AttrHelpBean;
import com.dawn.decoderapijni.bean.PropValueHelpBean;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.decoderapijni.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropValueSetActivity extends DawnActivity {
    private AttrHelpBean mAttrHelpBean;
    private String mCodeName;
    private LinearLayout layout_return;
    private TextView tv_title;
    private TextView tv_confirm;
    private LinearLayout ll_container;
    private TextView tv_attr_name;
    private TextView tv_radio_name;

    private EditText edit_attr_value;
    private LinearLayout ll_edit;
    private LinearLayout ll_radio;
    private RadioGroup rg_radio;
    private List<RadioButton> radioButtonList = new ArrayList<>();
    private Map<Integer, Integer> mapPropValuePosition = new HashMap<>();   //value  -  position
    private List<PropValueHelpBean> listPropValueBean = new ArrayList<>();
//    private int checkPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_prop_value_set;
    }

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mAttrHelpBean = (AttrHelpBean) bundle.getSerializable(ScanApplication.BUNDLE_KEY_ATTR_BEAN);
        mCodeName = bundle.getString(ScanApplication.BUNDLE_KEY_CODE_NAME);

        listPropValueBean = ServiceTools.getInstance().getPropHelpsBeans(mCodeName, mAttrHelpBean.getName());

        for (int i = 0; i < listPropValueBean.size(); i++) {
            mapPropValuePosition.put(listPropValueBean.get(i).getValue(), i);
        }

    }

    private void initView() {
        if (mAttrHelpBean == null)
            return;
        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(mCodeName);
        ll_container = findViewById(R.id.ll_container);
        layout_return = findViewById(R.id.layout_return);
        layout_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ll_edit = findViewById(R.id.ll_edit);
        ll_radio = findViewById(R.id.ll_radio);
        tv_attr_name = findViewById(R.id.tv_attr_name);
        tv_radio_name = findViewById(R.id.tv_radio_name);
        edit_attr_value = findViewById(R.id.edit_attr_value);
        rg_radio = findViewById(R.id.rg_radio);

        switch (mAttrHelpBean.getType()) {
            case ScanApplication.ATTR_HELP_BEAM_TYPE_EDIT:
                Log.v(ScanApplication.TAG, "ATTR_HELP_BEAM_TYPE_EDIT");
                ll_edit.setVisibility(View.VISIBLE);
                ll_radio.setVisibility(View.GONE);
                tv_attr_name.setText(mAttrHelpBean.getCnName());
                edit_attr_value.setHint(mAttrHelpBean.getSaveValue() + "");
                break;
            case ScanApplication.ATTR_HELP_BEAM_TYPE_RADIO:
                Log.v(ScanApplication.TAG, "ATTR_HELP_BEAM_TYPE_RADIO");
                ll_edit.setVisibility(View.GONE);
                ll_radio.setVisibility(View.VISIBLE);
                tv_radio_name.setText(mAttrHelpBean.getCnName());
                for (PropValueHelpBean propBean : listPropValueBean) {
                    RadioButton radioButton = new RadioButton(getApplicationContext());
                    radioButton.setText(propBean.getCnName());
                    radioButtonList.add(radioButton);
                    rg_radio.addView(radioButton);
                }
                //初始化选中项
                int checkPos = mapPropValuePosition.get(mAttrHelpBean.getSaveValue());
                if (checkPos >= 0) {
                    radioButtonList.get(checkPos).setChecked(true);
                }
                break;
            default:
                Log.e(ScanApplication.TAG, "unknown type!");
                break;
        }

        tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveValue();
                finish();
            }
        });

//        rg_radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                Log.v(MyApplication.TAG, "onCheckedChanged:" + i);
//                checkPos = i;
//            }
//        });


    }


    private void saveValue() {
        String strValue = "";
        switch (mAttrHelpBean.getType()) {
            case ScanApplication.ATTR_HELP_BEAM_TYPE_EDIT:
                strValue = edit_attr_value.getText().toString();
                break;
            case ScanApplication.ATTR_HELP_BEAM_TYPE_RADIO:
                int checkPos = -1;
                for (int i = 0; i < radioButtonList.size(); i++) {
                    if (rg_radio.getCheckedRadioButtonId() == radioButtonList.get(i).getId()) {
                        checkPos = i;
                        break;
                    }
                }
                for (Map.Entry<Integer, Integer> entry : mapPropValuePosition.entrySet()) {
                    if (entry.getValue() == checkPos) {
                        strValue = entry.getKey().toString();
                        break;
                    }
                }
                break;
            default:
                Log.e(ScanApplication.TAG, "unknown type!");
                break;
        }

        Log.v(ScanApplication.TAG, "strValue:" + strValue);
        if (strValue.isEmpty()) {
            return;
        } else {
            Log.v(ScanApplication.TAG, "ScanSet:" + mCodeName + " " + mAttrHelpBean.getName() + " " + strValue);
            int rc = SoftEngine.getInstance().ScanSet(mCodeName, mAttrHelpBean.getName(), strValue);
            if(rc < 0){
                Toast.makeText(getApplicationContext(),"Invalid value ！return code: "+ rc,Toast.LENGTH_SHORT).show();

            }
        }
    }
}
