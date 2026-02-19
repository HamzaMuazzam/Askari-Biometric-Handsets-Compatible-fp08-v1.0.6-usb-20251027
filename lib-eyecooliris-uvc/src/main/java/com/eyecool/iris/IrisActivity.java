package com.eyecool.iris;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hibory.CommonApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.eyecool.fragment.IrisFragment;
import com.eyecool.iris.api.IrisResult;
import com.eyecool.iris.api.IrisSDK;
import com.eyecool.utils.ExecutorUtil;
import com.eyecool.utils.FileUtils;
import com.eyecool.utils.ImageUtil;
import com.eyecool.utils.Logs;
import com.eyecool.widget.CustomProgressCircle;

import java.util.ArrayList;
import java.util.List;

/**
 * 虹膜测试
 * <p>
 * created by wangzhi 2017/8/29
 */
public class IrisActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = IrisActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSIONS_CODE = 11;

    private IrisFragment mIrisFragment;
    private Context mContext;
    private CustomProgressCircle leftEnrollProgress;
    private CustomProgressCircle rightEnrollProgress;
    private Button mEnrollBtn;
    private Button mVerifyBtn;
    private TextView mHintTv;
    private TextView mInfoTv;
    private Switch mMirrorSwitch;
    private Switch mFlipSwitch;
    private Toolbar toolbar;

    private List<Feature> mFeatures = new ArrayList<>();
    private int index = 0;

    private IrisSDK mIrisSDK;

    private ProgressDialog mProgressDialog;
    private EditText mQualityEt;
    private ImageUtil mImageUtil = new ImageUtil();
    private boolean isPlaySound = true;

    private CommonApi commonApi;

    int irisGpio = 28;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris_uvc);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mContext = this;

        String model = Build.MODEL.replace("-","");
        if(model.contains("X05")){
            irisGpio = 28;
        }else if(model.contains("FP08")){
            irisGpio = 159;
        }
        USBPowerControl(true);

        leftEnrollProgress = findViewById(R.id.leftEnrollProgress);
        rightEnrollProgress = findViewById(R.id.rightEnrollProgress);
        mEnrollBtn = findViewById(R.id.enrollBtn);
        mVerifyBtn = findViewById(R.id.verifyBtn);
        mHintTv = findViewById(R.id.hintTv);
        mInfoTv = findViewById(R.id.infoTv);
        mMirrorSwitch = findViewById(R.id.mirrorSwitch);
        mFlipSwitch = findViewById(R.id.flipSwitch);
        mQualityEt = findViewById(R.id.qualityEt);
        mEnrollBtn.setOnClickListener(this);
        mVerifyBtn.setOnClickListener(this);
        findViewById(R.id.openBtn).setOnClickListener(this);
        findViewById(R.id.closeBtn).setOnClickListener(this);
        findViewById(R.id.captureBtn).setOnClickListener(this);
        findViewById(R.id.splitBtn).setOnClickListener(this);
        findViewById(R.id.acquireImageBtn).setOnClickListener(this);
        findViewById(R.id.cancelBtn).setOnClickListener(this);

        toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IrisActivity.this.finish();
            }
        });

        resetProgressBar();

        mProgressDialog = new ProgressDialog(mContext);

        SoundPlayUtils.init(this);
        requestPermissions();

    }

    private void playSound(int res) {
        if (!isPlaySound) return;
        SoundPlayUtils.loadAndPlay(res, 1, 0);
    }

    ////power control
    private void USBPowerControl(boolean on) {
        if (commonApi == null) {
            commonApi = new CommonApi();
        }
        commonApi.setGpioDir(irisGpio, 1);
        if (on) {
            commonApi.setGpioOut(irisGpio, 1);
            commonApi.setUsbHost(1);
        } else {
            commonApi.setGpioOut(irisGpio, 0);
            commonApi.setUsbHost(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void resetProgressBar() {
        leftEnrollProgress.setMax(100);
        rightEnrollProgress.setMax(100);
        leftEnrollProgress.setProgress(0);
        rightEnrollProgress.setProgress(0);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = null;
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions = new ArrayList<>();
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (permissions == null) {
                    permissions = new ArrayList<>();
                }
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions == null) {
                initIrisSDK();
            } else {
                String[] permissionArray = new String[permissions.size()];
                permissions.toArray(permissionArray);
                // Request the permission. The result will be received
                // in onRequestPermissionResult()
                requestPermissions(permissionArray, REQUEST_PERMISSIONS_CODE);
            }
        } else {
            initIrisSDK();
        }
    }

    private void initIrisSDK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                //power control
                mIrisSDK = IrisSDK.getInstance();
                mIrisSDK.init(getApplicationContext(), "", new IrisSDK.IrisSDKCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(IrisActivity.this, getString(R.string.text_auth_success), Toast.LENGTH_SHORT).show();
                                mHintTv.setText(getString(R.string.text_auth_success));

                                FragmentManager manager = getFragmentManager();
                                mIrisFragment = new IrisFragment();
                                mIrisFragment.addCallback(mCameraCallbackX);
                                mIrisFragment.setDebug(false);
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.add(R.id.fragmentContainer, mIrisFragment, IrisFragment.class.getSimpleName());
                                transaction.commit();
                                mMirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mIrisFragment.setMirror(isChecked));
                                mMirrorSwitch.setChecked(true);
                                mFlipSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) {
                                        mIrisFragment.rotate(IrisFragment.ROTATION_180);
                                    } else {
                                        mIrisFragment.rotate(IrisFragment.ROTATION_0);
                                    }
                                });
                                mFlipSwitch.setChecked(false);
                                mInfoTv.setText("version:" + mIrisSDK.getVersion() + "_" + mIrisFragment.getDeviceModel());

                                launchCamera();

                            }
                        });
                    }

                    @Override
                    public void onError(int code, String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(IrisActivity.this, "code:" + code + " " + msg, Toast.LENGTH_SHORT).show();
                                mHintTv.setText(getString(R.string.text_auth_fail) + " code:" + code + " " + msg);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void launchCamera() {
        ProgressDialog progressDialog =ProgressDialog.show(IrisActivity.this,TAG,getString(R.string.start_iris),false,false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIrisFragment.closeCamera();
                    }
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIrisFragment.openCamera();
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.enrollBtn) {
            showFillIdDialog(0);
        } else if (id == R.id.verifyBtn) {
            verifys();
        } else if (id == R.id.openBtn) {
            mIrisFragment.openCamera();
        } else if (id == R.id.closeBtn) {
            mIrisFragment.closeCamera();
            mHintTv.setText("");
        } else if (id == R.id.captureBtn) {
            capture();
        } else if (id == R.id.splitBtn) {
            splitImage();
        } else if (id == R.id.acquireImageBtn) {
            acquireImage(System.currentTimeMillis() + "", index);
        } else if (id == R.id.cancelBtn) {
            mIrisFragment.cancel();
        }
    }

    private void showFillIdDialog(int type) {
        final EditText editText = new EditText(this);
        AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setView(editText)
                .setTitle("Enter Name")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String id = editText.getText().toString();
                        editText.setText("");
                        if (type == 0) {
                            enroll(id);
                        } else {
                            acquireImage(id, 0);
                        }

                        HideKeyboard(mQualityEt);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        HideKeyboard(mQualityEt);
                    }
                })
                .show();
    }

    /**
     * 注册
     */
    private void enroll(final String name) {
        mIrisFragment.enroll(15000, new IrisFragment.EnrollCallback() {
            @Override
            public void onSuccess(byte[] template) {
                leftEnrollProgress.setProgress(100);
                rightEnrollProgress.setProgress(100);
                playSound(R.raw.enrollment_succeeded);
                Toast.makeText(mContext, getString(R.string.enroll_success) + " template len:" + template.length, Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.enroll_success));

                Person person = new Person(name, template);
                mFeatures.add(person);

                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/iris_template.txt", template);

                mHintTv.postDelayed(() -> resetProgressBar(), 2000);
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                playSound(R.raw.enrollment_failed);
                mHintTv.setText(getString(R.string.enroll_failed) + " error:" + msg);
                resetProgressBar();
            }

            @Override
            public void onProcess(int progress, int state) {
                leftEnrollProgress.setProgress(progress);
                rightEnrollProgress.setProgress(progress);
                switch (state) {
                    case IrisFragment.DISTANCE_OK:
                        mHintTv.setText(getString(R.string.enroll_progress) + " " + progress);
                        break;
                    case IrisFragment.DISTANCE_CLOSER:
                        mHintTv.setText(getString(R.string.keep_away));
                        playSound(R.raw.move_backwards);
                        break;
                    case IrisFragment.DISTANCE_FUTHER:
                        mHintTv.setText(getString(R.string.be_closer));
                        playSound(R.raw.get_closer);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 1:N比对
     */
    private void verifys() {
        if (mFeatures.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.not_enroll), Toast.LENGTH_SHORT).show();
            playSound(R.raw.no_iris_please_enroll);
            return;
        }
        mHintTv.setText(getString(R.string.verifying));
        mIrisFragment.verify(5000, mFeatures, new IrisFragment.VerifyCallbackX() {

            @Override
            public void onSuccess(int position) {
                Person person = (Person) mFeatures.get(position);
                mHintTv.setText(getString(R.string.verify_success) + " name = " + person.getName());
                playSound(R.raw.verification_succeeded);
                Toast.makeText(mContext, "name = " + person.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProcess(int state) {
                switch (state) {
                    case IrisFragment.DISTANCE_CLOSER:
                        mHintTv.setText(getString(R.string.keep_away));
                        playSound(R.raw.move_backwards);
                        break;
                    case IrisFragment.DISTANCE_FUTHER:
                        mHintTv.setText(getString(R.string.be_closer));
                        playSound(R.raw.get_closer);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(mContext, getString(R.string.verify_failed) + " error：" + msg, Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.verify_failed) + " error:" + msg);
                playSound(R.raw.verification_failed);
            }
        });
    }

    /**
     * 拆分图像
     */
    private void splitImage() {
        byte[] imageData = mIrisFragment.getCameraBytes();
        if (imageData == null) {
            Toast.makeText(this, "camera data is null", Toast.LENGTH_SHORT).show();
            return;
        }
        String qualityStr = mQualityEt.getText().toString();
        if (TextUtils.isEmpty(qualityStr)) {
            Toast.makeText(this, getString(R.string.quality_score) + " is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // 质量分数
        int quality = 40;
        try {
            quality = Integer.valueOf(qualityStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mProgressDialog.setMessage("Splitting...");
        mProgressDialog.show();
        int finalQuality = quality;
        ExecutorUtil.exec(() -> {
            byte[] L_Iris = new byte[640 * 480 + 1078];// left iris
            byte[] R_Iris = new byte[640 * 480 + 1078];// right iris
            int[] size = new int[2];

            // 左右眼虹膜分数 int[6]，【左眼 质量分数，虹膜直径大小，有效面积，右眼 质量分数，虹膜直径大小，有效面积】
            int[] out_lr_score = new int[6];
            int status = IrisSDK.getInstance().split(imageData, L_Iris, R_Iris, size, out_lr_score, 1, IrisSDK.Pic.BMP, finalQuality);

            if (status >= 0) {
                Logs.i(TAG, "left:" + size[0] + " right:" + size[1]);
                runOnUiThread(() -> mHintTv.setText("Split success!"));

                byte[] left = new byte[size[0]];
                byte[] right = new byte[size[1]];
                System.arraycopy(L_Iris, 0, left, 0, left.length);
                System.arraycopy(R_Iris, 0, right, 0, right.length);

                IrisResult result = new IrisResult();
                result.setLeftImage(left);
                result.setLeftQualityScore(out_lr_score[0]);
                result.setLeftDiameter(out_lr_score[1]);
                result.setLeftArea(out_lr_score[2]);

                result.setRightImage(right);
                result.setRightQualityScore(out_lr_score[3]);
                result.setRightDiameter(out_lr_score[4]);
                result.setRightArea(out_lr_score[5]);

                String name = System.currentTimeMillis() + "";
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/BMP/" + name + "_1L.bmp", left);
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/BMP/" + name + "_0R.bmp", right);
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/BMP/" + name + ".txt", result.toString());
            } else {
                runOnUiThread(() -> {
                            switch (status) {
                                case -1: // 太近
                                    mHintTv.setText(status + "，" + getString(R.string.too_closer));
                                    break;
                                case -2: // 太远
                                    mHintTv.setText(status + "，" + getString(R.string.too_far));
                                    playSound(R.raw.too_far);
                                    break;
                                case -5: // 未检测到虹膜
                                    mHintTv.setText(status + "，" + getString(R.string.no_iris_detected));
                                    playSound(R.raw.no_iris_detected);
                                    break;
                                case -15: // 质量差
                                    mHintTv.setText(status + "，" + getString(R.string.iris_pool_quality));
                                    break;
                            }
                        }
                );
            }
            runOnUiThread(() -> mProgressDialog.dismiss());
        });
    }

    /**
     * 采集虹膜图像，返回虹膜坐标
     */
    private void capture() {
        byte[] irisImageData = mIrisFragment.getCameraBytes();
        if (irisImageData != null) {
            mImageUtil.saveRawAsBmpBuf(irisImageData, mIrisFragment.getPreviewWidth(), mIrisFragment.getPreviewHeight());
            byte[] bmpImgBuf = mImageUtil.getBmpImgBuf();
            String name = System.currentTimeMillis() + "";
            FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/Capture/" + name + ".bmp", bmpImgBuf);
            FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/Capture/" + name + ".bin", irisImageData);
            mHintTv.setText(R.string.capture_success);
            Toast.makeText(this, R.string.capture_success, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取图像
     */
    private void acquireImage(String id, int eyeType) {
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "id is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Logs.i(TAG, "id = " + id);

        String qualityStr = mQualityEt.getText().toString();
        if (TextUtils.isEmpty(qualityStr)) {
            Toast.makeText(this, getString(R.string.quality_score) + " is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // 质量分数
        int quality = 40;
        try {
            quality = Integer.valueOf(qualityStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mHintTv.setText("");
        mIrisFragment.acquireImage(10 * 1000, quality, eyeType, new IrisFragment.AcquireImageCallback() {
            @Override
            public void onSuccess(IrisResult result) {
                Toast.makeText(mContext, getString(R.string.acquire_success), Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.acquire_success) + "\n" + result.toString());

                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/BMP/" + id + "_1L.bmp", result.getLeftImage());
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/BMP/" + id + "_0R.bmp", result.getRightImage());
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/BMP/" + id + ".txt", result.toString());
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(mContext, getString(R.string.acquire_failed) + ", " + msg, Toast.LENGTH_SHORT).show();
                playSound(R.raw.failed_to_get_image);
                mHintTv.setText(msg);
            }

            @Override
            public void onProcess(int state) {
                switch (state) {
                    case IrisFragment.DISTANCE_CLOSER:
                        mHintTv.setText(R.string.keep_away);
                        playSound(R.raw.move_backwards);
                        break;
                    case IrisFragment.DISTANCE_FUTHER:
                        mHintTv.setText(R.string.be_closer);
                        playSound(R.raw.get_closer);
                        break;
                    case IrisFragment.NO_IRIS:
                        mHintTv.setText(R.string.no_iris_detected);
                        playSound(R.raw.no_iris_detected);
                        break;
                    case IrisFragment.LOCATION_FAILED:
                        mHintTv.setText(R.string.iris_location_failed);
                        break;
                    case IrisFragment.POOL_QUALITY:
                        mHintTv.setText(R.string.iris_pool_quality);
                        break;
                    case IrisFragment.OPEN_EYES:
                        mHintTv.setText(R.string.open_eyes_wide);
                        playSound(R.raw.please_open_eyes_wide);
                        break;
                }
            }
        });
    }

    IrisFragment.CameraCallbackX mCameraCallbackX = new IrisFragment.CameraCallbackX() {

        @Override
        public void onOpen() {

        }

        @Override
        public void onOpenError(int error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHintTv.setText(getString(R.string.open_camera) + ":failed" + error);
                }
            });
        }

        @Override
        public void onClose() {
        }

        @Override
        public void onPreview() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIrisFragment.openLight();
                }
            });
        }

        @Override
        public void onPreviewFailed() {
        }
    };

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            // Request for WRITE_EXTERNAL_STORAGE permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initIrisSDK();
            } else {
                // Permission request was denied.
                Toast.makeText(this, "No permissioned", Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }

    private class Person implements Feature {

        private String name;
        private byte[] feature;

        public Person(String name, byte[] feature) {
            this.name = name;
            this.feature = feature;
        }

        public String getName() {
            return name;
        }

        @Override
        public byte[] getIrisFeature() {
            return feature;
        }
    }

    public void HideKeyboard(View v) {
        InputMethodManager m = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundPlayUtils.releaseAll();
        USBPowerControl(false);
    }
}
