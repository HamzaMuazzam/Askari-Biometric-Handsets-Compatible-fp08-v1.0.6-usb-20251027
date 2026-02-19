package com.hf.newmidx.fingerprint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.R;
import com.hf.newmidx.databinding.ActivityHowToUseBinding;

public class HowToUseActivity extends BaseActivity {
    ActivityHowToUseBinding howToUseBinding;

    @Override
    public void onCreateBase(Bundle savedInstanceState) {
        howToUseBinding = ActivityHowToUseBinding.inflate(getLayoutInflater());
        setContentView(howToUseBinding.getRoot());

        howToUseBinding.actionbar.backTitleStyle(getString(R.string.fp));

        howToUseBinding.own.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HowToUseActivity.this, FingerPrintWithDBActivity.class));
            }
        });

        howToUseBinding.sdk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HowToUseActivity.this, FingerPrintActivity.class));
            }
        });

    }
}