package com.eyecool.iris;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eyecool.fragment.IrisBinocularFragment;
import com.eyecool.fragment.IrisFragment;
import com.eyecool.iris.api.IrisResult;
import com.eyecool.iris.api.IrisSDK;
import com.eyecool.utils.ExecutorUtil;
import com.eyecool.utils.FileUtils;
import com.eyecool.utils.ImageUtil;
import com.eyecool.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris Binocular
 */
public class IrisBinocularActivity extends AppCompatActivity implements IrisBinocularFragment.CameraCallback {


    private static final String TAG = IrisBinocularActivity.class.getSimpleName();

    private LinearLayout mRootLayout;
    private FrameLayout mContentLayout;
    private IrisBinocularFragment mIrisFragment;
    private TextView mVersionTv;
    private ProgressBar mProgressBar;
    private TextView mHintTv;
    private Switch mMirrorSwitch;

    private Context mContext;

    private List<Feature> mFeatures = new ArrayList<>();

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    private RadioGroup mImageTypeRg;
    private EditText mQualityEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris_binocular);
        mContext = this;
        mProgressDialog = new ProgressDialog(mContext);

        mRootLayout = findViewById(R.id.rootLayout);
        mContentLayout = findViewById(R.id.contentLayout);
        mVersionTv = findViewById(R.id.versionTv);
        mProgressBar = findViewById(R.id.progressBar);
        mHintTv = findViewById(R.id.hintTv);
        mMirrorSwitch = findViewById(R.id.mirrorSwitch);
        mImageTypeRg = findViewById(R.id.imageTypeRg);
        mQualityEt = findViewById(R.id.qualityEt);

        findViewById(R.id.enrollBtn).setOnClickListener(view -> showFillIdDialog(0));
        findViewById(R.id.verifyBtn).setOnClickListener(view -> verify());
        findViewById(R.id.openCameraBtn).setOnClickListener(view -> mIrisFragment.startCamera());
        findViewById(R.id.closeCameraBtn).setOnClickListener(view -> mIrisFragment.stopCamera());
        findViewById(R.id.splitBtn).setOnClickListener(view -> splitImage());
        findViewById(R.id.acquireImageBtn).setOnClickListener(view -> showFillIdDialog(1));
        findViewById(R.id.captureBtn).setOnClickListener(view -> capture());
        findViewById(R.id.cancelBtn).setOnClickListener(view -> cancel());

        mIrisFragment = IrisBinocularFragment.newInstance();
        mIrisFragment.setCameraCallback(this);
        getFragmentManager().beginTransaction()
                .add(R.id.contentLayout, mIrisFragment)
                .commit();

        mVersionTv.setText(IrisSDK.getInstance().getVersion());
        mMirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mIrisFragment.setMirror(isChecked));

        layoutView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIrisFragment.cancel();
    }

    private void layoutView() {
        mRootLayout.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams lp = mRootLayout.getLayoutParams();
                int width = mRootLayout.getMeasuredWidth();
                int height = mRootLayout.getMeasuredHeight();
                if (width > height) {
                    lp.width = width / 3;
                    lp.height = height;
                } else {
                    lp.width = width;
                    lp.height = height;
                }
                Logs.i(TAG, "lp.width = " + lp.width + ", lp.height = " + lp.height);
                mRootLayout.setLayoutParams(lp);
            }
        });
    }

    /**
     * enroll
     */
    private void enroll(final String name) {
        mHintTv.setText(R.string.enroll);
        mIrisFragment.enroll(10 * 1000, new IrisBinocularFragment.EnrollCallback() {

            @Override
            public void onSuccess(byte[] template) {
                mProgressBar.setProgress(100);

                Person person = new Person(name, template);
                mFeatures.add(person);

                Toast.makeText(mContext, getString(R.string.enroll_success) + " template len:" + template.length, Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.enroll_success));
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.enroll_failed) + " error:" + msg);
            }

            @Override
            public void onProcess(int progress, int state) {
                mProgressBar.setProgress(progress);
                switch (state) {
                    case IrisFragment.DISTANCE_OK:
                        mHintTv.setText(getString(R.string.enroll_progress) + " " + progress);
                        break;
                    case IrisFragment.DISTANCE_CLOSER:
                        mHintTv.setText(getString(R.string.keep_away));
                        break;
                    case IrisFragment.DISTANCE_FUTHER:
                        mHintTv.setText(getString(R.string.be_closer));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * verify
     */
    private void verify() {
        if (mFeatures.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.not_enroll), Toast.LENGTH_SHORT).show();
            return;
        }
        mHintTv.setText(R.string.verify);
        mIrisFragment.verifyX(10 * 1000, mFeatures, new IrisBinocularFragment.VerifyCallback() {
            @Override
            public void onSuccess(int position) {
                Person person = (Person) mFeatures.get(position);
                mHintTv.setText(getString(R.string.verify_success) + " name = " + person.getName());
                Toast.makeText(mContext, "name = " + person.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(mContext, getString(R.string.verify_failed) + " error：" + msg, Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.verify_failed) + " error:" + msg);
            }
        });
    }

    int tempWitch;

    /**
     * Split image
     */
    private void splitImage() {
        byte[] imageData = mIrisFragment.getMergeCameraBytes();
        if (imageData == null) {
            Toast.makeText(this, "camera data is null", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedRadioButtonId = mImageTypeRg.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.k1RBtn) {
            getK1Image();
        } else if (checkedRadioButtonId == R.id.k2RBtn) {
            getK2K3K7Image(1);
        } else if (checkedRadioButtonId == R.id.k3RBtn) {
            getK2K3K7Image(2);
        } else if (checkedRadioButtonId == R.id.k7RBtn) {
            getK2K3K7Image(3);
        }
    }

    private void getK1Image() {
        byte[] imageData = mIrisFragment.getMergeCameraBytes();
        if (imageData == null) {
            Toast.makeText(this, "camera data is null", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.setMessage("Splitting...");
        mProgressDialog.show();

        String qualityStr = mQualityEt.getText().toString();
        if (TextUtils.isEmpty(qualityStr)) {
            Toast.makeText(this, getString(R.string.quality_score) + " is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Quality Score
        int quality = 50;
        try {
            quality = Integer.valueOf(qualityStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        int finalQuality = quality;
        ExecutorUtil.exec(() -> {
            int[] size = new int[2];

            // Left and right eye iris fraction int[6]
            // [left eye mass fraction, iris diameter, effective area, right eye mass fraction, iris diameter, effective area]
            int[] out_lr_score = new int[6];

            // get k1 image
            byte[] L_Iris = new byte[imageData.length];
            byte[] R_Iris = new byte[0];
            int status = IrisSDK.getInstance().split(imageData, L_Iris, R_Iris, size, out_lr_score, 0, IrisSDK.Pic.BMP, finalQuality);

            if (status >= 0) {
                Logs.i(TAG, "left:" + size[0] + " right:" + size[1]);
                runOnUiThread(() -> mHintTv.setText("Split k1 success!"));

                String name = System.currentTimeMillis() + "";

                IrisResult result = new IrisResult();
                result.setLeftQualityScore(out_lr_score[0]);
                result.setLeftDiameter(out_lr_score[1]);
                result.setLeftArea(out_lr_score[2]);

                result.setRightQualityScore(out_lr_score[3]);
                result.setRightDiameter(out_lr_score[4]);
                result.setRightArea(out_lr_score[5]);

                // save k1 image
                byte[] k1_image = new byte[size[0]];
                System.arraycopy(L_Iris, 0, k1_image, 0, k1_image.length);
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/K1/" + name + ".bmp", k1_image);
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/K1/" + name + ".txt", result.toString());
            } else {
                runOnUiThread(() -> {
                    switch (status) {
                        case -1: // too closer
                            mHintTv.setText(status + "，" + getString(R.string.too_closer));
                            break;
                        case -2: // too far
                            mHintTv.setText(status + "，" + getString(R.string.too_far));
                            break;
                        case -5: // No iris detected
                            mHintTv.setText(status + "，" + getString(R.string.no_iris_detected));
                            break;
                        case -15: // Poor quality
                            mHintTv.setText(status + "，" + getString(R.string.iris_pool_quality));
                            break;
                    }
                });
            }
            runOnUiThread(() -> mProgressDialog.dismiss());
        });
    }

    private void getK2K3K7Image(int irisType) {
        byte[] imageData = mIrisFragment.getMergeCameraBytes();
        if (imageData == null) {
            Toast.makeText(this, "camera data is null", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.setMessage("Splitting...");
        mProgressDialog.show();

        String qualityStr = mQualityEt.getText().toString();
        if (TextUtils.isEmpty(qualityStr)) {
            Toast.makeText(this, getString(R.string.quality_score) + " is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Quality Score
        int quality = 50;
        try {
            quality = Integer.valueOf(qualityStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        int finalQuality = quality;
        ExecutorUtil.exec(() -> {

            byte[] L_Iris = new byte[mIrisFragment.getPreviewWidth() * mIrisFragment.getPreviewHeight() + 1078];// left iris
            byte[] R_Iris = new byte[mIrisFragment.getPreviewWidth() * mIrisFragment.getPreviewHeight() + 1078];// right iris
            int[] size = new int[2];

            // Left and right eye iris fraction int[6]
            // [left eye mass fraction, iris diameter, effective area, right eye mass fraction, iris diameter, effective area]
            int[] out_lr_score = new int[6];
            String imageType = "K2";
            switch (irisType) {
                case 1:
                    imageType = "K2";
                    break;
                case 2:
                    imageType = "K3";
                    break;
                case 3:
                    imageType = "K7";
                    break;
            }

            int status = IrisSDK.getInstance().split(imageData, L_Iris, R_Iris, size, out_lr_score, irisType, IrisSDK.Pic.BMP, finalQuality);

            if (status >= 0) {
                Logs.i(TAG, "left:" + size[0] + " right:" + size[1]);
                String finalImageType = imageType;
                runOnUiThread(() -> mHintTv.setText("Split " + finalImageType + " success!"));

                byte[] left = new byte[size[0]];
                byte[] right = new byte[size[1]];
                System.arraycopy(L_Iris, 0, left, 0, left.length);
                System.arraycopy(R_Iris, 0, right, 0, right.length);

                String name = System.currentTimeMillis() + "";

                IrisResult result = new IrisResult();
                result.setLeftImage(left);
                result.setLeftQualityScore(out_lr_score[0]);
                result.setLeftDiameter(out_lr_score[1]);
                result.setLeftArea(out_lr_score[2]);

                result.setRightImage(right);
                result.setRightQualityScore(out_lr_score[3]);
                result.setRightDiameter(out_lr_score[4]);
                result.setRightArea(out_lr_score[5]);

                saveImage(finalImageType, name, result);
            } else {
                runOnUiThread(() -> {
                    switch (status) {
                        case -1: // too closer
                            mHintTv.setText(status + "，" + getString(R.string.too_closer));
                            break;
                        case -2: // too far
                            mHintTv.setText(status + "，" + getString(R.string.too_far));
                            break;
                        case -5: // No iris detected
                            mHintTv.setText(status + "，" + getString(R.string.no_iris_detected));
                            break;
                        case -15: // Poor quality
                            mHintTv.setText(status + "，" + getString(R.string.iris_pool_quality));
                            break;
                    }
                });
            }
            runOnUiThread(() -> mProgressDialog.dismiss());
        });
    }

    private void showFillIdDialog(int type) {
        final EditText editText = new EditText(this);
        mAlertDialog = new AlertDialog.Builder(this)
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
                            acquireImage(id);
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
     * Cycle image acquisition
     */
    private void acquireImage(String idName) {
        if (TextUtils.isEmpty(idName)) {
            Toast.makeText(this, "idName is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Logs.i(TAG, "idName = " + idName);

        String qualityStr = mQualityEt.getText().toString();
        if (TextUtils.isEmpty(qualityStr)) {
            Toast.makeText(this, getString(R.string.quality_score) + " is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Quality Score
        int quality = 50;
        try {
            quality = Integer.valueOf(qualityStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        int imageType = 0;
        int checkedRadioButtonId = mImageTypeRg.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.k1RBtn) { // K1
            imageType = 0;
        } else if (checkedRadioButtonId == R.id.k2RBtn) { // K2
            imageType = 1;
        } else if (checkedRadioButtonId == R.id.k3RBtn) { // K3
            imageType = 2;
        } else if (checkedRadioButtonId == R.id.k7RBtn) { // K7
            imageType = 3;
        }

        mHintTv.setText(R.string.acquire_image);
        mIrisFragment.acquireImage(10 * 1000, quality, imageType, new IrisBinocularFragment.AcquireImageCallback() {
            @Override
            public void onSuccess(IrisResult result) {
                Toast.makeText(mContext, getString(R.string.acquire_success), Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.acquire_success) + "\n" + result.toString());

                switch (result.getImageType()) {
                    case 0: // K1
                        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/K1/" + idName + ".bmp", result.getLeftImage());
                        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/K1/" + idName + ".txt", result.toString());
                        break;
                    case 1: // K2
                        saveImage("K2", idName, result);
                        break;
                    case 2: // K3
                        saveImage("K3", idName, result);
                        break;
                    case 3: // K7
                        saveImage("K7", idName, result);
                        break;
                }
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(mContext, getString(R.string.acquire_failed) + ", " + msg, Toast.LENGTH_SHORT).show();
                mHintTv.setText(getString(R.string.acquire_failed) + ", " + msg);
            }

            @Override
            public void onProcess(int state) {
                switch (state) {
                    case IrisFragment.DISTANCE_CLOSER:
                        mHintTv.setText(getString(R.string.keep_away));
                        break;
                    case IrisFragment.DISTANCE_FUTHER:
                        mHintTv.setText(getString(R.string.be_closer));
                        break;
                    case IrisFragment.NO_IRIS:
                        mHintTv.setText(getString(R.string.no_iris_detected));
                        break;
                    case IrisFragment.LOCATION_FAILED:
                        mHintTv.setText(getString(R.string.iris_location_failed));
                        break;
                    case IrisFragment.POOL_QUALITY:
                        mHintTv.setText(getString(R.string.iris_pool_quality));
                        break;
                }
            }
        });
    }

    private void capture() {
        ImageUtil imageUtil = new ImageUtil();

        byte[] leftYuv = mIrisFragment.getCameraYuvL();
        byte[] rightYuv = mIrisFragment.getCameraYuvR();
        byte[] mergeYuv = mIrisFragment.getMergeCameraBytes();
        if (leftYuv == null || rightYuv == null || mergeYuv == null) {
            Toast.makeText(this, "camera data is null", Toast.LENGTH_SHORT).show();
            return;
        }

        imageUtil.saveRawAsBmpBuf(leftYuv, mIrisFragment.getPreviewWidth(), mIrisFragment.getPreviewHeight());
        byte[] leftBmp = imageUtil.getBmpImgBuf();

        imageUtil.saveRawAsBmpBuf(rightYuv, mIrisFragment.getPreviewWidth(), mIrisFragment.getPreviewHeight());
        byte[] rightBmp = imageUtil.getBmpImgBuf();

        imageUtil.saveRawAsBmpBuf(mergeYuv, mIrisFragment.getPreviewWidth() * 2, mIrisFragment.getPreviewHeight());
        byte[] mergeBmp = imageUtil.getBmpImgBuf();


        String name = System.currentTimeMillis() + "";
        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/Capture/" + name + "_left.bmp", leftBmp);
        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/Capture/" + name + "_right.bmp", rightBmp);
        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/Capture/" + name + "_merge.bmp", mergeBmp);
    }

    private void cancel() {
        mIrisFragment.cancel();
    }

    private void saveImage(String imageTypeStr, String idName, IrisResult result) {
        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/" + imageTypeStr + "/" + idName + "_1L.bmp", result.getLeftImage());
        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/" + imageTypeStr + "/" + idName + "_0R.bmp", result.getRightImage());
        FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/" + imageTypeStr + "/" + idName + ".txt", result.toString());
    }

    @Override
    public void onOpen() {
        Logs.i(TAG, "onOpen...");
        mVersionTv.setText("version:" + IrisSDK.getInstance().getVersion());
        mHintTv.setText("Open success");
        mMirrorSwitch.setChecked(true);
    }

    @Override
    public void onPreview() {
        Logs.i(TAG, "onPreview...");
    }

    @Override
    public void onPreviewError() {

    }

    @Override
    public void onOpenError(int error) {
        mHintTv.setText("open error: " + error);
    }

    @Override
    public void onClose() {
        mHintTv.setText("");
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
}