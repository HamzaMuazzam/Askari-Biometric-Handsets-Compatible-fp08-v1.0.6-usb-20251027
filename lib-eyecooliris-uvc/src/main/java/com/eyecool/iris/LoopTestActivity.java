package com.eyecool.iris;

import android.hibory.CommonApi;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.eyecool.fragment.IrisFragment;
import com.eyecool.utils.Logs;

import java.lang.ref.WeakReference;

/**
 * 压力测试
 */
public class LoopTestActivity extends AppCompatActivity {

    private static final String TAG = LoopTestActivity.class.getSimpleName();

    private IrisFragment mIrisFragment;
    private TextView mLogTv;
    private LoopTask mLoopTask;

    private EditText mOpenTimeEt;
    private EditText mIntervalTimeEt;

    private Toolbar toolbar;

    private CommonApi commonApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris_loop_test);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        power_onoff(true);
        mIrisFragment = (IrisFragment) getFragmentManager().findFragmentById(R.id.irisFragment);
        mLogTv = findViewById(R.id.logTv);

        mOpenTimeEt = findViewById(R.id.openTimeEt);
        mIntervalTimeEt = findViewById(R.id.intervalTimeEt);
        findViewById(R.id.startTest).setOnClickListener(v -> startTest());
        findViewById(R.id.exitBtn).setOnClickListener(v -> finish());

        toolbar = (Toolbar)this.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoopTestActivity.this.finish();
            }
        });
    }

    private void startTest() {
        if (null == mLoopTask) {
            String openTimeStr = mOpenTimeEt.getText().toString();
            if (TextUtils.isEmpty(openTimeStr)) {
                Toast.makeText(this, "open time is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            int openTime = 30;
            try {
                openTime = Integer.parseInt(openTimeStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String intervalTimeStr = mIntervalTimeEt.getText().toString();
            if (TextUtils.isEmpty(intervalTimeStr)) {
                Toast.makeText(this, "interval time is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            int intervalTime = 30;
            try {
                intervalTime = Integer.parseInt(intervalTimeStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Logs.i(TAG, "openTime = " + openTime + ", interval time = " + intervalTime);

            mLoopTask = new LoopTask(this);
            mLoopTask.setOpenTime(openTime * 1000);
            mLoopTask.setIntervalTime(intervalTime * 1000);
            mLoopTask.start();
        } else {
            Logs.e(TAG, "loop task is running...");
        }
    }
    private void  power_onoff(boolean on){
        if(commonApi == null){
            commonApi = new CommonApi();
        }
        if(android.os.Build.MODEL.equals("X05") || android.os.Build.MODEL.equals("TAYAL2023")|| android.os.Build.MODEL.equals("HF-X05")){
            if(on){
                commonApi.setGpioDir(28,1);
                commonApi.setGpioOut(28,1);
            }else{
                commonApi.setGpioDir(28,1);
                commonApi.setGpioOut(28,0);
            }
        }
    }

    @Override
    protected void onResume() {
        power_onoff(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTask();
        power_onoff(false);
    }

    private void stopTask() {
        if (mLoopTask != null) {
            mLoopTask.close();
            mLoopTask.interrupt();
            mLoopTask = null;
        }
    }

    private static class LoopTask extends Thread {

        private final WeakReference activitys;
        private boolean isStop = false;
        private long openTime = 30 * 1000;
        private long intervalTime = 30 * 1000;
        private int mTestNum = 0;

        public LoopTask(LoopTestActivity activity) {
            activitys = new WeakReference(activity);
        }

        public void close() {
            isStop = true;
        }

        public void setOpenTime(long openTime) {
            this.openTime = openTime;
        }

        public void setIntervalTime(long intervalTime) {
            this.intervalTime = intervalTime;
        }

        @Override
        public void run() {
            Logs.i(TAG, "开始测试任务...");
            while (!isStop) {
                if (activitys.get() == null) {
                    break;
                }
                if (isInterrupted()) {
                    Logs.e(TAG, "线程中断...");
                    break;
                }
                try {
                    mTestNum++;
                    LoopTestActivity activity = (LoopTestActivity) activitys.get();
                    activity.runOnUiThread(() -> {
                        activity.mIrisFragment.openCamera();
                        activity.mLogTv.setText("Test times " + mTestNum + ", open camera...");
                    });
                    sleep(openTime);
                    if (isStop) {
                        break;
                    }

                    activity.runOnUiThread(() -> {
                        activity.mIrisFragment.closeCamera();
                        activity.mLogTv.setText("Test times " + mTestNum + ", close camera...");
                    });
                    sleep(intervalTime);
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
            Logs.e(TAG, "退出测试任务...");
        }
    }
}