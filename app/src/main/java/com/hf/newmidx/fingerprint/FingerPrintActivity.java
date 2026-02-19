package com.hf.newmidx.fingerprint;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.R;
import com.hf.newmidx.databinding.ActivityFingerprintBinding;
import com.hfteco.finger.FingerSDK;
import com.hfteco.finger.OnCaptureListener;
import com.hfteco.finger.OnEnrollListener;
import com.hfteco.finger.OnExportImgListener;
import com.hfteco.finger.OnExportTemplatesListener;
import com.hfteco.finger.OnSdkInitListener;
import com.hfteco.finger.OnSearchListener;
import com.hfteco.finger.OnVerifyListener;

import java.util.List;

public class FingerPrintActivity extends BaseActivity {

    ActivityFingerprintBinding activityFingerprintBinding;
    private FingerSDK fingerSDK;

    private boolean deviceModelNameCheck = false;

    List<FingerSDK.TEMPLEATES> templeatesList = FingerSDK.TEMPLEATES.getTempleatesList();

    @Override
    public void onCreateBase(Bundle savedInstanceState) {
        activityFingerprintBinding = ActivityFingerprintBinding.inflate(getLayoutInflater());
        setContentView(activityFingerprintBinding.getRoot());

        activityFingerprintBinding.actionbar.backTitleStyle(getString(R.string.fp));

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
                                    new AlertDialog.Builder(FingerPrintActivity.this)
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

    private void initView() {
        activityFingerprintBinding.actionbar.backTitleStyle(getString(R.string.fp));
        activityFingerprintBinding.hostBtnCapture.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View v) {
                hostCapture();
            }

            @Override
            public void onRejectClick() {
                showLogText("[HOST CAPTURE]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnDbShow.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                hostShowDB();
            }

            @Override
            public void onRejectClick() {
                showLogText("[HOST DB SHOW]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnRemove.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                hostRemove();
            }

            @Override
            public void onRejectClick() {
                showLogText("[HOST REMOVE]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnClear.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                hostClear();
            }

            @Override
            public void onRejectClick() {
                showLogText("[HOST CLEAR]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnEnroll.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View v) {
                hostEnroll();
            }

            @Override
            public void onRejectClick() {
                showLogText("[ENROLL]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnVerify.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                hostVerify();
            }

            @Override
            public void onRejectClick() {
                showLogText("[VERIFY]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnSearch.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View v) {
                hostSearch();
            }

            @Override
            public void onRejectClick() {
                showLogText("[SEARCH]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnExportTemplate.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                exportAsTemplate();
            }

            @Override
            public void onRejectClick() {
                showLogText("[GET TEMPLATE DATA]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostBtnExportImage.setOnClickListener(new OnDeviceCheckClickListener(deviceModelNameCheck) {
            @Override
            public void onPassClick(View view) {
                exportAsImage();
            }

            @Override
            public void onRejectClick() {
                showLogText("[GET IMAGE DATA]\nFailed\nDEVICE ERROR");
            }
        });

        activityFingerprintBinding.hostSpTemplate.setAdapter(new BaseAdapter() {
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

    private void showLogText(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityFingerprintBinding.tvLog.setText(msg);
            }
        });
    }

    private void showDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressDialogUtils.showProgressDialog(FingerPrintActivity.this, getString(R.string.please_press_finger));
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

    private void updateFingerBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityFingerprintBinding.ivFinger.setImageBitmap(bitmap);
            }
        });
    }

    public void hostCapture() {
        showDialog();
        fingerSDK.captureBitmap(new OnCaptureListener() {
            @Override
            public void capture(int i, Bitmap bitmap) {
                closeDialog();
                if (i == FingerSDK.RESULT_OK) {
                    updateFingerBitmap(bitmap);
                    showLogText("[CAPTURE]\nSuccess");
                } else {
                    showLogText("[CAPTURE]\nFailed");
                    if (bitmap != null) {
                        updateFingerBitmap(bitmap);
                    }
                }
            }
        });
    }

    public void hostEnroll() {
        showDialog();
        fingerSDK.enroll(activityFingerprintBinding.hostEtUserId.getText().toString().trim(), new OnEnrollListener() {
            @Override
            public void enroll(int i, Bitmap bitmap, String s) {
                closeDialog();
                if (i == FingerSDK.RESULT_OK) {
                    updateFingerBitmap(bitmap);
                    showLogText("[ENROLL]\nSuccess\n" + s);
                } else {
                    showLogText("[ENROLL]\nFailed\n" + s);
                    if (bitmap != null) {
                        updateFingerBitmap(bitmap);
                    }
                }
            }
        }, (FingerSDK.TEMPLEATES) activityFingerprintBinding.hostSpTemplate.getSelectedItem());
    }

    public void hostSearch() {
        showDialog();
        fingerSDK.search(new OnSearchListener() {
            @Override
            public void search(String s, Bitmap bitmap) {
                closeDialog();
                if (s != null) {
                    updateFingerBitmap(bitmap);
                    showLogText("[SEARCH]\nSuccess\n" + s);
                } else {
                    showLogText("[SEARCH]\nFailed\n" + s);
                    if (bitmap != null) {
                        updateFingerBitmap(bitmap);
                    }
                }
            }
        }, (FingerSDK.TEMPLEATES) activityFingerprintBinding.hostSpTemplate.getSelectedItem());
    }

    public void hostShowDB() {
        showLogText("[HOST DB SHOW]\nSuccess\nEnrolled Users: " + fingerSDK.showDb());
    }

    public void hostRemove() {
        boolean result = fingerSDK.remove(activityFingerprintBinding.hostEtUserId.getText().toString().trim());
        if (result) {
            activityFingerprintBinding.tvLog.setText("[HOST REMOVE]\nSuccess");
        } else {
            activityFingerprintBinding.tvLog.setText("[HOST REMOVE]\nFailed");
        }

    }

    public void hostClear() {
        boolean result = fingerSDK.clear();
        if (result) {
            activityFingerprintBinding.tvLog.setText("[HOST Clear]\nSuccess");
        } else {
            activityFingerprintBinding.tvLog.setText("[HOST Clear]\nFailed");
        }

    }

    public void hostVerify() {
        showDialog();
        fingerSDK.verify(activityFingerprintBinding.hostEtUserId.getText().toString().trim(), (FingerSDK.TEMPLEATES) activityFingerprintBinding.hostSpTemplate.getSelectedItem(), new OnVerifyListener() {
            @Override
            public void verify(int i, String s, Bitmap bitmap) {
                closeDialog();
                if (i == FingerSDK.RESULT_OK) {
                    updateFingerBitmap(bitmap);
                    showLogText("[VERIFY]\nSuccess\n" + s);
                } else {
                    showLogText("[VERIFY]\nFailed\n" + s);
                    if (bitmap != null) {
                        updateFingerBitmap(bitmap);
                    }
                }
            }
        });
    }

    public void exportAsTemplate() {
        showDialog();
        fingerSDK.exportAsTemplate(activityFingerprintBinding.hostEtUserId.getText().toString(),(FingerSDK.TEMPLEATES) activityFingerprintBinding.hostSpTemplate.getSelectedItem(), new OnExportTemplatesListener() {
            @Override
            public void export(int i, String s, String s1, Bitmap bitmap) {
                closeDialog();
                if (i == FingerSDK.RESULT_OK) {
                    showLogText("[EXPORT]\nSuccess\n" + s + "\nPath=" + s1);
                    updateFingerBitmap(bitmap);
                } else {
                    showLogText("[EXPORT]\nFailed\n" + s);
                }
            }
        });
    }

    public void exportAsImage() {
        showDialog();
        fingerSDK.exportAsImage(activityFingerprintBinding.hostEtUserId.getText().toString(),(FingerSDK.TEMPLEATES) activityFingerprintBinding.hostSpTemplate.getSelectedItem(), new OnExportImgListener() {
            @Override
            public void export(int i, String s, String s1, Bitmap bitmap) {
                closeDialog();
                if (i == FingerSDK.RESULT_OK) {
                    showLogText("[EXPORT]\nSuccess\n" + s + "\nPath=" + s1);
                    updateFingerBitmap(bitmap);
                } else {
                    showLogText("[EXPORT]\nFailed\n" + s);
                }
            }
        });
    }

}
