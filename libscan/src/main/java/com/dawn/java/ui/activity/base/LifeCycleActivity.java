package com.dawn.java.ui.activity.base;

import android.os.Bundle;

import androidx.annotation.Nullable;


/**
 * LifeCycleActivity : 检测 主界面 是否被后台回收
 */
public class LifeCycleActivity extends ActivityLifeBase{
    public static final String TAG = LifeCycleActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*
		 * TODO 打包的时候注释
		 * RefWatcher refWatcher = WeijuApplication.getRefWatcher(this);
	    refWatcher.watch(this);*/

        // 在后台的时候被系统回收了，应用重启

    }
}
