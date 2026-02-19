package com.eyecool.iris;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.eyecool.fragment.IrisFragment;
import com.eyecool.iris.api.ColorCardDetectResult;
import com.eyecool.iris.api.IrisSDK;
import com.eyecool.utils.ExecutorUtil;
import com.eyecool.utils.FileUtils;



/**
 * 检测色卡
 */
public class ColorCardDetectActivity extends AppCompatActivity {

    private IrisFragment mIrisFragment;
    private TextView mLogTv;
    private TextView mInfoTv;

    private ProgressDialog mProgressDialog;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris_color_card_detect);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Testing...");
        mIrisFragment = (IrisFragment) getFragmentManager().findFragmentById(R.id.irisFragment);
        mIrisFragment.addCallback(mCameraCallbackX);
        mIrisFragment.setMirror(true);
        mIrisFragment.setGetSn(true);
        mLogTv = findViewById(R.id.logTv);
        mInfoTv = findViewById(R.id.infoTv);

        findViewById(R.id.startTest).setOnClickListener(v -> startTest());
        findViewById(R.id.exitBtn).setOnClickListener(v -> finish());

        findViewById(R.id.openBtn).setOnClickListener(v -> mIrisFragment.openCamera());
        findViewById(R.id.closeBtn).setOnClickListener(v -> mIrisFragment.closeCamera());
        findViewById(R.id.captureBtn).setOnClickListener(v -> capture());
        findViewById(R.id.openLightBtn).setOnClickListener(v -> mIrisFragment.openLight());
        findViewById(R.id.closeLightBtn).setOnClickListener(v -> mIrisFragment.closeLight());

        toolbar = (Toolbar)this.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorCardDetectActivity.this.finish();
            }
        });
    }

    private void startTest() {
        mProgressDialog.show();
        ExecutorUtil.exec(() -> {
            byte[] cameraData = mIrisFragment.getCameraBytes();
            if (cameraData != null) {
                ColorCardDetectResult result = IrisSDK.getInstance().detectColorCard(cameraData, IrisSDK.getInstance().getWidth(), IrisSDK.getInstance().getHeight());
                String sn = mIrisFragment.getSn();
                showLog("score = " + result.getScore() + ", sn = " + mIrisFragment.getSn());
                showInfo(result.getScore());
                // 保存色卡图片到SD卡中
                FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/ColorCardDetection/" + sn + "_" + result.getScore() + "_" + System.currentTimeMillis() + ".bmp", result.getImage());
            } else {
                showLog("camera data is null...");
            }

            runOnUiThread(() -> mProgressDialog.dismiss());
        });
    }

    private void capture() {
        byte[] cameraData = mIrisFragment.getCameraBytes();
        if (cameraData != null) {
            FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Iris/YUV/" + System.currentTimeMillis() + ".bin", cameraData);
            Toast.makeText(this, "save success", Toast.LENGTH_SHORT).show();
        } else {
            showLog("camera data is null...");
        }
    }

    private void showLog(String text) {
        mLogTv.post(() -> mLogTv.setText(text));
    }

    private void showInfo(int score) {
        mInfoTv.post(() -> {
            if (score >= 5000) {
                mInfoTv.setText("PASS");
                mInfoTv.setTextColor(Color.GREEN);
            } else {
                mInfoTv.setText("FAIL");
                mInfoTv.setTextColor(Color.RED);
            }
        });
    }

    IrisFragment.CameraCallbackX mCameraCallbackX = new IrisFragment.CameraCallbackX() {

        @Override
        public void onOpen() {

        }

        @Override
        public void onOpenError(int error) {

        }

        @Override
        public void onClose() {

        }

        @Override
        public void onPreview() {
            mIrisFragment.closeLight();
        }

        @Override
        public void onPreviewFailed() {
        }
    };
}