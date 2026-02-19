package com.dawn.java.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;

public class CenterScanSettingActivity extends AppCompatActivity {
    private Button btnCalibration;
//    private Button btnEnable;
//    private Button btnDisable;
    private TopToolbar topBar;
    private static final String TAG = "ScanJni DLScan";
    private Switch switch_enable;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_scan_setting);
        initView();
        initData();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
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
        switch_enable = findViewById(R.id.switch_enable);
        switch_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.v(TAG, "onCheckedChanged");
                if (b) {
                    SoftEngine.getInstance().setFocusDecodeEnable(1);
                    btnCalibration.setVisibility(View.VISIBLE);
                }else{
                    SoftEngine.getInstance().setFocusDecodeEnable(0);
                    btnCalibration.setVisibility(View.INVISIBLE);
                }
            }
        });
//        btnEnable= findViewById(R.id.btn_enable);
//        btnEnable.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SoftEngine.getInstance().setFocusDecodeEnable(1);
////                Toast.makeText(MyApplication.mContext,"已开启中心区域解码",Toast.LENGTH_SHORT).show();
//            }
//        });
//        btnDisable= findViewById(R.id.btn_disable);
//        btnDisable.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SoftEngine.getInstance().setFocusDecodeEnable(0);
////                Toast.makeText(MyApplication.mContext,"已关闭中心区域解码",Toast.LENGTH_SHORT).show();
//            }
//        });
        btnCalibration = findViewById(R.id.btn_calibration);
        btnCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoftEngine.getInstance().setFocusDecodeCalibration();
//                Toast.makeText(MyApplication.mContext,"校准",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData(){
        int rc = SoftEngine.getInstance().getFocusDecodeEnable();
        if (rc == 1){
            switch_enable.setChecked(true);
            btnCalibration.setVisibility(View.VISIBLE);
        }else{
            btnCalibration.setVisibility(View.INVISIBLE);
        }
    }

}
