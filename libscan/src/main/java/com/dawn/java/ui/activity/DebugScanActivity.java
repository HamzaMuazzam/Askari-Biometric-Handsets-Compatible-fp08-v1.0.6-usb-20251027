package com.dawn.java.ui.activity;

import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_SUCC;
import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_TIMEOUT;
import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_SCANNER_OVERHEAT;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.dawn.decoderapijni.R;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DebugScanActivity extends DawnActivity implements View.OnClickListener {
    private final int HANDLER_ADD_NEW_MESSAGE = 1;
    private final int HANDLER_SCANNER_OTHER_ERROR = 2;
    private final int HANDLER_SCANNER_OVERHEAT = 3;
    private final int HANDLER_SCANNER_TIME_OUT = 4;
    private final int HANDLER_DEBUG_TOTAL_TIME_UPDATE = 5;
    //    private final int HANDLER_DEBUG_TIME_UPDATE = 5;
    private static final int SINGLE_MODE = 0;
    private static final int CONTINUOUS_MODE = 1;

    private TopToolbar topBar;
    private CheckBox checkBox_mode;
    private CheckBox checkBox_music;
    private TextView tv_debug_scan_count, tv_debug_scan_show, tv_debug_scan_total_time;
    private EditText et_debug_scan_delay;
    private EditText et_debug_time;

    private Random random = new Random();
    FloatingActionButton fab;

    private boolean scanStatus = false;
    private int scanMode = SINGLE_MODE;
    private boolean scanMusic = false;
    private int timeGap = 0;
    private int count = 0;
    private int debugTime = -1;
    private int totalTime = 0;
    private boolean isCountTotalTime = false;
    //计时线程池
    ScheduledExecutorService totalTimeScheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_debug_scan;
    }

    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        topBar = findViewById(R.id.topBar);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {

            }
        });
        fab = findViewById(R.id.debug_scan_fab);
        fab.setOnClickListener(this);
        checkBox_mode = findViewById(R.id.cb_continuous);
        checkBox_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    scanMode = CONTINUOUS_MODE;
                } else {
                    scanMode = SINGLE_MODE;
                }
            }
        });
        checkBox_mode.setChecked(true);
        checkBox_music = findViewById(R.id.cb_music);
        checkBox_music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    scanMusic = true;
                } else {
                    scanMusic = false;
                }
            }
        });
        tv_debug_scan_count = findViewById(R.id.tv_debug_scan_count);
        tv_debug_scan_show = findViewById(R.id.tv_debug_scan_show);
        tv_debug_scan_count.setText(count + "");
        tv_debug_scan_total_time = findViewById(R.id.tv_debug_scan_total_time);
        et_debug_time = findViewById(R.id.et_debug_time);
        et_debug_scan_delay = findViewById(R.id.et_debug_scan_delay);
        et_debug_scan_delay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence == null || charSequence.toString().equals("")) {
                    timeGap = 0;
                } else {
                    timeGap = Integer.valueOf(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initData() {
        setScanCallback();

        //计时器线程
        totalTimeScheduledExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isCountTotalTime) {
                    totalTime++;
                    sendMsg("", HANDLER_DEBUG_TOTAL_TIME_UPDATE);

                    if (debugTime > 0 && totalTime == debugTime) {
//                        checkBox_mode.setChecked(false);
                        stopDebugScan();
                    }
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.debug_scan_fab) {
            if (scanStatus) {
                //停止测试
                stopDebugScan();
            } else {
//                    开始测试
                startDebugScan();
            }
        }
    }

    private void startDebugScan() {
        totalTime = 0;
        tv_debug_scan_total_time.setText(0 + " s");
        if (scanMode == CONTINUOUS_MODE) {
            //连续扫码时 开启计时器
            isCountTotalTime = true;


            //设置测试时间
            String strDebugTime = et_debug_time.getText().toString();
            if (!strDebugTime.equals("") && !strDebugTime.equals("0")) {
                debugTime = Integer.valueOf(strDebugTime);
            }
        }


        scanStatus = true;
        fab.setImageResource(R.drawable.ic_media_pause);
        ServiceTools.getInstance().startScan();
        count = 0;
    }

    private void stopDebugScan() {
        scanStatus = false;
        fab.setImageResource(R.drawable.ic_media_play);
        ServiceTools.getInstance().stopScan();
        //停止计时器
        isCountTotalTime = false;
    }

    private void afterScanAction() {
        if (scanMode == CONTINUOUS_MODE && scanStatus) {
            if (timeGap == 0) {
                ServiceTools.getInstance().startScan();
            } else {
                scanDelayThreadPool.execute(scanDelayRunnable);
            }
        } else {
            fab.setImageResource(R.drawable.ic_media_play);
            //停止计时器
            isCountTotalTime = false;

            scanStatus = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private Handler handler = new Handler(new WeakReference<Handler.Callback>(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            Log.v(MyApplication.TAG,"Handler Thread:" + Thread.currentThread().getName());
            switch (msg.what) {
                case HANDLER_ADD_NEW_MESSAGE:
                    String strResult = msg.getData().getString("msg_str");
                    tv_debug_scan_show.setText(strResult);
                    tv_debug_scan_show.setTextColor(Color.argb(255, random.nextInt(256),
                            random.nextInt(256), random.nextInt(256)));

                    count++;
                    tv_debug_scan_count.setText(count + "");
                    afterScanAction();

                    break;
                case HANDLER_SCANNER_TIME_OUT:
                    afterScanAction();
                    break;
                case HANDLER_SCANNER_OVERHEAT:
                    afterScanAction();
                    break;
                case HANDLER_SCANNER_OTHER_ERROR:
                    afterScanAction();
                    break;

                case HANDLER_DEBUG_TOTAL_TIME_UPDATE:
                    tv_debug_scan_total_time.setText(totalTime + " s");
                    break;
            }
            return false;
        }
    }).get());

    ExecutorService scanDelayThreadPool = Executors.newFixedThreadPool(1);
    Runnable scanDelayRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(timeGap);
                if (scanStatus) {
                    ServiceTools.getInstance().startScan();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void setScanCallback() {
        SoftEngine.getInstance().setScanningCallback(new SoftEngine.ScanningCallback() {
            @Override
            public int onScanningCallback(int eventCode, int param1, byte[] param2, int length) {
                Log.d(ScanApplication.TAG, "sendScanningResultFromNative  eventCode= " + eventCode);

                String strResult;
                byte[] data = param2;
                switch (eventCode) {
                    case SCN_EVENT_DEC_SUCC:
                        if (data != null) {
                            if (scanMusic) {
                                ScanApplication.mSoundPool.play(ScanApplication.soundId, 1, 1, 0, 0, (float) 1.0); // 播放声音
                            }
                            int i = 0;
                            for (i = 0; data[i] != 0; i++) {
                            }
                            strResult = new String(param2, 128, length - 128);
                            sendMsg(strResult, HANDLER_ADD_NEW_MESSAGE);
                        }
                        break;
                    case SCN_EVENT_DEC_TIMEOUT:
                        sendMsg("Scanner Time Out", HANDLER_SCANNER_TIME_OUT);
                        break;
                    case SCN_EVENT_SCANNER_OVERHEAT:
                        sendMsg("Scanner is overheat", HANDLER_SCANNER_OVERHEAT);
                        break;
                    default:
                        sendMsg("Error event code:" + eventCode, HANDLER_SCANNER_OTHER_ERROR);
                        break;
                }
                if (timeGap > 500)
                    return 0;// 0 正常灯光模式 ，1连扫模式，不关灯
                return scanMode;
            }
        });
    }

    private void sendMsg(String data, int flag) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg_str", data);
        msg.setData(bundle);
        msg.what = flag;
        handler.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanStatus = false;
        ServiceTools.getInstance().stopScan();
    }
}
