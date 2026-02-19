package com.hf.newmidx.fingerprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.R;
import com.hf.newmidx.databinding.ActivityFingerprintBinding;
import com.hf.newmidx.databinding.ActivityFingerprintWithDbBinding;
import com.hfteco.finger.FingerSDK;
import com.hfteco.finger.OnCaptureBytesListener;
import com.hfteco.finger.OnSdkInitListener;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class FingerPrintWithDBActivity extends BaseActivity {
    private FingerSDK fingerSDK;

    private boolean deviceModelNameCheck = false;

    HashMap<String,String> thisIsYourOwnDB = new HashMap<>();

    List<FingerSDK.TEMPLEATES> templeatesList = FingerSDK.TEMPLEATES.getTempleatesList();

    ActivityFingerprintWithDbBinding activityFingerprintWithDbBinding;

    @Override
    public void onCreateBase(Bundle savedInstanceState) {
        activityFingerprintWithDbBinding = ActivityFingerprintWithDbBinding.inflate(getLayoutInflater());
        setContentView(activityFingerprintWithDbBinding.getRoot());

        activityFingerprintWithDbBinding.actionbar.backTitleStyle(getString(R.string.fp));

        deviceModelNameCheck = FingerSDK.licenceDevice();

        initView();

        fingerSDK = new FingerSDK(this,new OnSdkInitListener() {
            @Override
            public void initResult(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i != FingerSDK.RESULT_OK) {
                            AlertDialog retryDialog =
                                    new AlertDialog.Builder(FingerPrintWithDBActivity.this)
                                            .setCancelable(false)
                                            .setTitle("INIT").setMessage(s)
                                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    finish();
                                                }
                                            })
                                            .setPositiveButton("Try Again",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            fingerSDK.launch();
                                                        }
                                                    }).create();
                            retryDialog.show();
                        }
                    }
                });
            }

            @Override
            public void onOpticalSensorInterrupt() {

            }

            @Override
            public void onOpticalSensorLost() {

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fingerSDK.launch();
    }

    @Override
    public void onPause(){
        super.onPause();
        fingerSDK.release();
    }

    private void hostCapture(){
        showDialog();
        fingerSDK.captureBytes((FingerSDK.TEMPLEATES) activityFingerprintWithDbBinding.hostSpTemplate.getSelectedItem(), new OnCaptureBytesListener() {
            @Override
            public void capture(int i, byte[] bytes, Bitmap bitmap, byte[] temp) {
                closeDialog();
                if (i==FingerSDK.RESULT_OK) {
                    updateFingerBitmap(bitmap);
                    showLogText("[capture]\nSuccess\n");

                    try {
                        String tempString = new String(temp,"ISO8859-1");
                        //now！you can save this tempString to your own db.
                        //I will use a hashmap to save it for example
                        thisIsYourOwnDB.put(activityFingerprintWithDbBinding.hostEtUserId.getText().toString(),tempString);

                    } catch (UnsupportedEncodingException e) {

                    }

                } else {
                    showLogText("[capture]\nFailed\n");
                    if (bitmap != null) {
                        updateFingerBitmap(bitmap);
                    }
                }
            }
        });
    }

    private void hostVerify(){
        showDialog();
        fingerSDK.captureBytes((FingerSDK.TEMPLEATES) activityFingerprintWithDbBinding.hostSpTemplate.getSelectedItem(), new OnCaptureBytesListener() {
            @Override
            public void capture(int i, byte[] bytes, Bitmap bitmap, byte[] temp) {
                closeDialog();
                if (i==FingerSDK.RESULT_OK) {
                    updateFingerBitmap(bitmap);

                    try {
                        String tempString = new String(temp,"ISO8859-1");
                        //now！you can compare the tempString with each other
                        //for each your db to compare
                        for(String userId:thisIsYourOwnDB.keySet()){
                            int score = fingerSDK.compareTemplateBytes((FingerSDK.TEMPLEATES) activityFingerprintWithDbBinding.hostSpTemplate.getSelectedItem(),tempString.getBytes("ISO8859-1"),thisIsYourOwnDB.get(userId).getBytes("ISO8859-1"));
                            if(score>80){
                                showLogText("[verify]\nSuccess\n you are "+ userId);
                                return;
                            }
                        }

                        showLogText("[verify]\nSuccess\n no fingerprint matched");
                    } catch (UnsupportedEncodingException e) {

                    }

                } else {
                    showLogText("[capture]\nFailed\n");
                    if (bitmap != null) {
                        updateFingerBitmap(bitmap);
                    }
                }
            }
        });
    }

    private void updateFingerBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityFingerprintWithDbBinding.ivFinger.setImageBitmap(bitmap);
            }
        });
    }

    private void initView() {
        activityFingerprintWithDbBinding.actionbar.backTitleStyle(getString(R.string.fp));

        activityFingerprintWithDbBinding.hostBtnCapture.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View v) {
                hostCapture();
            }

            @Override
            public void onRejectClick() {
                showLogText("[HOST CAPTURE]\nFailed\nDEVICE ERROR");
            }
        });
        activityFingerprintWithDbBinding.hostBtnVerify.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                hostVerify();
            }

            @Override
            public void onRejectClick() {
                showLogText("[VERIFY]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintWithDbBinding.hostSpTemplate.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return templeatesList.size();
            }

            @Override
            public Object getItem(int position) {
                return templeatesList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                ((CheckedTextView) v).setText(templeatesList.get(position).name());
                return v;
            }
        });
    }

    private void showDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressDialogUtils.showProgressDialog(FingerPrintWithDBActivity.this, getString(R.string.please_press_finger));
            }
        });
    }

    private void closeDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressDialogUtils.dismissProgressDialog();
            }
        });
    }

    private void showLogText(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //tvLog.append(msg);
                activityFingerprintWithDbBinding.tvLog.setText(msg);
            }
        });
    }
}
