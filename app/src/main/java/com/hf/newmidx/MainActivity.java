package com.hf.newmidx;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.dawn.java.ui.homePage.HomeActivity;
import com.eyecool.iris.IrisActivity;
import com.eyecool.iris.IrisMainActivity;
import com.hf.newmidx.databinding.ActivityMainBinding;
import com.hf.newmidx.facepass.ScanFaceActivity;
import com.hf.newmidx.fingerprint.HowToUseActivity;
import com.hf.newmidx.qrbarcode.ScanActivity;
import com.hf.newmidx.uhf.UhfActivity;

public class MainActivity extends BaseActivity {

    ActivityMainBinding mainBinding;

    EasyPermission easyPermission;

    @Override
    public void onCreateBase(Bundle bundle) {
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        easyPermission = new EasyPermission(MainActivity.this);

        mainBinding.actionbar.titleStyle(getString(R.string.app_name));

        mainBinding.module.setText(Build.MODEL + "-" + Build.DISPLAY);
        mainBinding.vn.setText("version_name:" + getVersionName());
        mainBinding.vc.setText("version_code:" + getVersionCode() + "");

        mainBinding.llFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!easyPermission.manage_storage(MainActivity.this)) {
                    return;
                }
                easyPermission.storage(new EasyPermission.IPermissionResult() {
                    @Override
                    public void result(boolean allGranted) {
                        if (allGranted) {
                            easyPermission.phone(new EasyPermission.IPermissionResult() {
                                @Override
                                public void result(boolean allGranted2) {
                                    if (allGranted2) {
                                        startActivity(new Intent(MainActivity.this, HowToUseActivity.class));
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        mainBinding.llScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!easyPermission.manage_storage(MainActivity.this)) {
                    return;
                }
                easyPermission.storageAndCameraPhoneState(new EasyPermission.IPermissionResult() {
                    @Override
                    public void result(boolean allGranted) {
                        if (allGranted) {
                            if (MODEL.contains("FP07")) {
                                startActivity(new Intent(MainActivity.this, ScanActivity.class));
                            } else {
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            }
                        }
                    }
                });
            }
        });

        mainBinding.llUhf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UhfActivity.class));
            }
        });

        mainBinding.llIris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!easyPermission.manage_storage(MainActivity.this)) {
                    return;
                }
                easyPermission.storageAndCameraPhoneState(new EasyPermission.IPermissionResult() {
                    @Override
                    public void result(boolean allGranted) {
                        if (allGranted) {
                            startActivity(new Intent(MainActivity.this, IrisMainActivity.class));
                        }
                    }
                });
            }
        });

        mainBinding.llScanface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!easyPermission.manage_storage(MainActivity.this)) {
                    return;
                }
                easyPermission.storageAndCameraPhoneState(new EasyPermission.IPermissionResult() {
                    @Override
                    public void result(boolean allGranted) {
                        if (allGranted) {
                            startActivity(new Intent(MainActivity.this, ScanFaceActivity.class));
                        }
                    }
                });
            }
        });
    }
}
