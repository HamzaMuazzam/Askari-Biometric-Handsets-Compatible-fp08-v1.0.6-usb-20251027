package com.dawn.java.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.decoderapijni.bean.AttrHelpBean;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.AttrSettingShowView;
import com.dawn.java.ui.widget.AttrSettingSwitchView;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;

import java.util.ArrayList;
import java.util.List;

public class ParamSetActivity extends DawnActivity {
    private String codeName;
    //    private String codeJson;
    private LinearLayout ll_container;
    //    private HelpDocBean mHelpDocBean;
//    private CodeHelpBean mCodeHelpBean;
    private TopToolbar topBar;
    //    private Map<String, AttrHelpBean> mapAttrHelpBean = new HashMap<>();
    private List<AttrHelpBean> listAttrHelpBean = new ArrayList<>();
    private List<AttrSettingSwitchView> listSwitchView = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_param_set;
    }

    public void refreshData(){
        listAttrHelpBean = ServiceTools.getInstance().getAttrHelpsBeans(codeName);
        initAttrItem();
    }

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        codeName = bundle.getString(ScanApplication.BUNDLE_KEY_CODE_NAME);
    }

    private void initView() {
        if (codeName == null)
            return;
        topBar = findViewById(R.id.topBar);
        ll_container = findViewById(R.id.ll_container);
        topBar.setMainTitle(codeName);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {

            }
        });
        initAttrItem();
    }

    //重新读取码制值， 刷新
    private void checkSwitchValue(){
        for( AttrSettingSwitchView switchView:listSwitchView){
            AttrHelpBean attrHelpBean = switchView.getmAttrHelpBean();
            String curValue = SoftEngine.getInstance().ScanGet(codeName, attrHelpBean.getName());
            attrHelpBean.setSaveValue(Integer.valueOf(curValue));
            switchView.initValueAndListener(curValue.equals("1"));
        }
    }
    //
    private void initAttrItem() {
        ll_container.removeAllViews();
        listSwitchView.clear();
        for (AttrHelpBean attrHelpBean : listAttrHelpBean) {
            String curValue = SoftEngine.getInstance().ScanGet(codeName, attrHelpBean.getName());
            attrHelpBean.setSaveValue(Integer.valueOf(curValue));
            Log.v(ScanApplication.TAG, "curValue : " + curValue);
            switch (attrHelpBean.getType()) {
                case ScanApplication.ATTR_HELP_BEAM_TYPE_SWITCH:
                    AttrSettingSwitchView switchView = new AttrSettingSwitchView(this, codeName, attrHelpBean) {
                        @Override
                        public void afterChangeCheckedAction() {
                            checkSwitchValue();
                        }
                    };
                    switchView.initValueAndListener(curValue.equals("1"));
                    ll_container.addView(switchView);
                    listSwitchView.add(switchView);
                    break;
                case ScanApplication.ATTR_HELP_BEAM_TYPE_EDIT:
                    AttrSettingShowView paramView = new AttrSettingShowView(this, codeName, attrHelpBean);
                    paramView.setValue(curValue);
                    ll_container.addView(paramView);
                    break;
                case ScanApplication.ATTR_HELP_BEAM_TYPE_RADIO:
                    AttrSettingShowView paramSpainView = new AttrSettingShowView(this, codeName, attrHelpBean);
                    paramSpainView.setValue(attrHelpBean.getValueText());
                    ll_container.addView(paramSpainView);
                    break;
            }
        }
    }


    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(ScanApplication.TAG, "onActivityResult" + requestCode + " " + resultCode);

    }

    //
    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void finish() {
//        super.finish();
        Intent resIntent = new Intent();
        resIntent.putExtra(ScanApplication.BUNDLE_KEY_CODE_NAME, codeName);
        setResult(ScanApplication.CODE_SETTING_SUCCESS_RESULT_CODE, resIntent);
        super.finish();
    }


}
