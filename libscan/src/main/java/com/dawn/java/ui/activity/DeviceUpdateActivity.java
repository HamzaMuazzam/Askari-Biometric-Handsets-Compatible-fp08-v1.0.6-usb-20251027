package com.dawn.java.ui.activity;

import static com.dawn.decoderapijni.ServiceTools.MSG_INIT_DONE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;


public class DeviceUpdateActivity extends DawnActivity implements View.OnClickListener{
    private TopToolbar topBar;
    private Button btn_update;
    private TextView tvFwPath, tvCurrVer;
    private ProgressDialog progressDialogForUpdate = null;

    private int REQ_CODE_SELECT_FW = 0x1823;
    private final int MSG_GET_UPDATE_PROGRESS_VALUE = 0x01;

    private String newFwPath = null;

    private boolean isHwV3Version = false;
    private boolean isV3UpgradeFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_device_update;
    }

    private void initView() {
        topBar = findViewById(R.id.topBar);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {
                Toast.makeText(DeviceUpdateActivity.this, "Loading", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQ_CODE_SELECT_FW);
            }
        });
        tvFwPath = findViewById(R.id.tv_fw_path);
        tvCurrVer = findViewById(R.id.tv_curr_ver);

        btn_update = findViewById(R.id.btn_update);
        btn_update.setOnClickListener(this);
        setUpgradeCallback();
    }

    public void initData() {
        showCurrVersion();
    }
    private void showCurrVersion() {
        try {
            String scannerVersion = SoftEngine.getInstance().getScannerVersion();
            isHwV3Version = scannerVersion.contains(" HV_V3.");
            tvCurrVer.setText(scannerVersion);
        }catch (Exception e){
            tvCurrVer.setText("unknown");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SELECT_FW && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = uri.getPath();
                if (path != null) {
                    isV3UpgradeFile = path.contains("CM60_V3.");
                    if ((!isHwV3Version && isV3UpgradeFile) || (isHwV3Version && !isV3UpgradeFile)) {
                        Toast.makeText(this, R.string.update_version_fail, Toast.LENGTH_SHORT).show();
                        newFwPath = null;
                        tvFwPath.setText(newFwPath);
                    } else {
                        newFwPath = path;
                        tvFwPath.setText(newFwPath);
                    }
                }
            }
        }
    }

    private boolean startUpdate(String fwFileAbsPath) {
        return ServiceTools.getInstance().updateFirmware(fwFileAbsPath);
    }

    private void setUpgradeCallback() {
        SoftEngine.getInstance().setUpgradeCallback(new SoftEngine.UpgradeProgressCallback() {
            @Override
            public void onUpgradeCallback(int progressValue, int totalValue) {
                Message msg = new Message();
                msg.what = MSG_GET_UPDATE_PROGRESS_VALUE;
                msg.arg1 = progressValue;
                msg.arg2 = totalValue;
                handler.sendMessage(msg);
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_GET_UPDATE_PROGRESS_VALUE:
                    if (progressDialogForUpdate != null && progressDialogForUpdate.isShowing()) {
                        progressDialogForUpdate.setMessage(getResources().getString(R.string.update_upgrading));
                        progressDialogForUpdate.setProgress(message.arg1);
                        progressDialogForUpdate.setMax(message.arg2);
                    }
                    return true;
                case MSG_INIT_DONE:
                    if (progressDialogForUpdate!=null) {
                        progressDialogForUpdate.dismiss();
                        progressDialogForUpdate = null;
                    }
                    Toast.makeText(DeviceUpdateActivity.this, R.string.update_success,
                            Toast.LENGTH_LONG).show();
                    return true;
            }
            return false;
        }
    });

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_update) {
            if (newFwPath == null) {
                Toast.makeText(this, R.string.update_choose_fw_first, Toast.LENGTH_SHORT).show();
                return;
            }
            if (progressDialogForUpdate == null) {
                progressDialogForUpdate = new ProgressDialog(this);
                progressDialogForUpdate.setTitle("Please wait");
                progressDialogForUpdate.setMessage("Prepare...");
                progressDialogForUpdate.setCancelable(false);
                progressDialogForUpdate.setMax(1);
                progressDialogForUpdate.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
                progressDialogForUpdate.show();
                final String currFwPath = newFwPath;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean updateIsOk = startUpdate(currFwPath);
                        try {
                            // Make sure scanner reboot already
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (updateIsOk) {
                                    if (progressDialogForUpdate != null) {
                                        progressDialogForUpdate.setMessage(getString(R.string.init_prog_start));
                                    }
                                    ServiceTools.getInstance().deInit();
                                    ServiceTools.getInstance().startInit(getApplicationContext(),
                                            getDir("nlscan", MODE_PRIVATE).getAbsolutePath(),
                                            false, handler);
                                } else {
                                    if (progressDialogForUpdate != null) {
                                        progressDialogForUpdate.dismiss();
                                        progressDialogForUpdate = null;
                                    }
                                    Toast.makeText(DeviceUpdateActivity.this, R.string.update_failure,
                                            Toast.LENGTH_LONG).show();
//                                      showCurrVersion();
                                }

                            }
                        });
                    }
                }).start();
            }
        }
    }
}
