package com.dawn.java.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;

public class HighLightSettingActivity extends DawnActivity {
    private TopToolbar topBar;
    private static final String TAG = "ScanJni DLScan";
    private Switch highlight_enable;
    private LinearLayout linearLayout_frames;
    private EditText et_frames;
    private Button btn_frames;
    int ret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_highlight_setting;
    }

    private void initView() {
        linearLayout_frames = findViewById(R.id.layout_frames);
        et_frames = findViewById(R.id.et_frames);
        btn_frames = findViewById(R.id.btn_frames);
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

        highlight_enable = findViewById(R.id.highlight_enable);
        highlight_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.v(TAG, "onCheckedChanged");
                if (b) {
                    ret = SoftEngine.getInstance().setHighlightEnable(1);
                    if(ret == 0) {
                        linearLayout_frames.setVisibility(View.VISIBLE);
                        btn_frames.setVisibility(View.VISIBLE);
                        et_frames.setText(String.valueOf(SoftEngine.getInstance().getHighlightFrames()));
                    }
                    else {
                        Toast.makeText(HighLightSettingActivity.this, "固件版本不支持", Toast.LENGTH_SHORT).show();
                        highlight_enable.setChecked(false);
                    }
                }else{
                    SoftEngine.getInstance().setHighlightEnable(0);
                    linearLayout_frames.setVisibility(View.GONE);
                    btn_frames.setVisibility(View.GONE);
                }
            }
        });

        btn_frames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frames;
                frames = Integer.parseInt(et_frames.getText().toString());
                if(et_frames.length() != 0) {
                    frames = SoftEngine.getInstance().setHighlightFrames(frames);
                    if(frames != 0)
                        Toast.makeText(HighLightSettingActivity.this, "帧数设置失败", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(HighLightSettingActivity.this, "帧数设置成功", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(HighLightSettingActivity.this, "帧数设置失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initData(){
        int rc;
        rc = SoftEngine.getInstance().getHighlightEnable();
        Log.i(TAG,"getHighlightEnable:"+rc);
        if (rc == 1) {
            highlight_enable.setChecked(true);
            linearLayout_frames.setVisibility(View.VISIBLE);
            btn_frames.setVisibility(View.VISIBLE);
            et_frames.setText(String.valueOf(SoftEngine.getInstance().getHighlightFrames()));
        }
        else {
            linearLayout_frames.setVisibility(View.GONE);
            btn_frames.setVisibility(View.GONE);
        }
    }

}
