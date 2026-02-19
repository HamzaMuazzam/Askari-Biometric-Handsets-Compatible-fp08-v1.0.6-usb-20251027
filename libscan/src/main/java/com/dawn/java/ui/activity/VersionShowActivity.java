package com.dawn.java.ui.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;

public class VersionShowActivity extends DawnActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TopToolbar topBar = findViewById(R.id.topBar);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {

            }
        });

        TextView tv = findViewById(R.id.tv_show);
        tv.setText(SoftEngine.getInstance().SDKVersion()+"\n"
                    +SoftEngine.getInstance().getScannerVersion()+"\n"
                +SoftEngine.getInstance().getDecodeVersion());
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_version_show;
    }
}
