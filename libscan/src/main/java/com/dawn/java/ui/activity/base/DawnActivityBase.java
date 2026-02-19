package com.dawn.java.ui.activity.base;


import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.dawn.decoderapijni.R;


/**
 *  DawnActivityBase :
 *  设置 状态栏 背景颜色 和 字体颜色
 *  绘制 基础 界面
 */
public abstract class DawnActivityBase extends LifeCycleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setStatusBar(getResources().getColor(R.color.general_actionbar_bg));
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void setStatusBar(@ColorInt int color) {

        // 设置状态栏底色颜色
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(color);
        }

        // 如果亮色，设置状态栏文字为黑色
        if(ColorUtils.calculateLuminance(color) >= 0.5) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    // layout Id
    public abstract int getLayoutId();

}
