package com.dawn.java.ui.activity;

import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_SUCC;
import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_TIMEOUT;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebugAccuracyActivity extends DawnActivity implements View.OnClickListener {
    private final int HANDLER_ADD_NEW_MESSAGE = 1;
    private final int HANDLER_SCANNER_OTHER_ERROR = 2;
    private final int HANDLER_SCANNER_OVERHEAT = 3;
    private final int HANDLER_SCANNER_TIME_OUT = 4;
    private TopToolbar topBar;
    private CheckBox checkBox_music;
    private CheckBox checkBox_save_error_img;
    private byte[] bmpBytes = null;

    private TextView tv_scan_result_first, tv_scan_result_cur, tv_count, tv_error, tv_missing;
    private EditText et_debug_scan_delay;
    private Random random = new Random();
    FloatingActionButton fab_start_pause;
    private boolean scanMusic = false;
    private boolean scanStatus = false;
    private int timeGap = 0;
    private int count_total = 0;
    private int count_error = 0;
    private int count_missing = 0;

    private String strFirstScanResult = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_debug_accuracy;
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
        fab_start_pause = findViewById(R.id.fab_start_pause);
        fab_start_pause.setOnClickListener(this);
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
        checkBox_save_error_img = findViewById(R.id.cb_save_error_img);

        tv_scan_result_first = findViewById(R.id.tv_scan_result_first);
        tv_scan_result_cur = findViewById(R.id.tv_scan_result_cur);
        tv_count = findViewById(R.id.tv_count);
        tv_error = findViewById(R.id.tv_error);
        tv_missing = findViewById(R.id.tv_missing);
        tv_count.setText(count_total + "");
        tv_error.setText(count_error + "");
        tv_missing.setText(count_missing + "");
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
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_start_pause) {
            if (scanStatus) {
                scanStatus = false;
                fab_start_pause.setImageResource(R.drawable.ic_media_play);
                ServiceTools.getInstance().stopScan();
            } else {
                scanStatus = true;
                fab_start_pause.setImageResource(R.drawable.ic_media_pause);
                ServiceTools.getInstance().startScan();
                count_total = 0;
                count_error = 0;
                count_missing = 0;
            }
        }
    }

    private Handler handler = new Handler(new WeakReference<Handler.Callback>(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            Log.v(MyApplication.TAG,"Handler Thread:" + Thread.currentThread().getName());
            switch (msg.what) {
                case HANDLER_ADD_NEW_MESSAGE:
                    String strResult = msg.getData().getString("msg_str");
                    Log.v(ScanApplication.TAG, "strResult:" + strResult);
                    tv_scan_result_cur.setText(strResult);
                    tv_scan_result_cur.setTextColor(Color.argb(255, random.nextInt(256),
                            random.nextInt(256), random.nextInt(256)));
                    if (count_total == 0 || strFirstScanResult.equals("")) {
                        strFirstScanResult = strResult;
                        tv_scan_result_first.setText(strResult);
                    } else {
                        if (!strResult.equals(strFirstScanResult)) {
                            count_error++;
                            //保存误码图片
                            if (checkBox_save_error_img.isChecked()) {
                                saveErrorImage(strResult);
                            }
                        }
                    }
                    afterScanAction();
                    break;
                case HANDLER_SCANNER_TIME_OUT:
                    count_missing++;
                    afterScanAction();
                    break;
                case HANDLER_SCANNER_OTHER_ERROR:
                    count_error++;
                    afterScanAction();
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
                ServiceTools.getInstance().startScan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void afterScanAction() {
        count_total++;
        tv_count.setText(count_total + "");
        tv_error.setText(count_error + "");
        tv_missing.setText(count_missing + "");

        if (scanStatus) {
            if (timeGap == 0) {
                ServiceTools.getInstance().startScan();
            } else {
                scanDelayThreadPool.execute(scanDelayRunnable);
//                if (scanStatus) {
//                    ServiceTools.getInstance().startScan();
//                }
            }
        } else {
            fab_start_pause.setImageResource(R.drawable.ic_media_play);
            scanStatus = false;
        }
    }

    private void saveErrorImage(String decodeStr) {
        bmpBytes = ServiceTools.getInstance().getImageLast();
        if (bmpBytes == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length);
        if (bitmap == null) {
            Log.d(TAG, "bitmap is null ....");
            return;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        Log.i(TAG, "bitmap.getWidth()" + bitmap.getWidth());
        Log.i(TAG, "bitmap.getHeight()" + bitmap.getHeight());

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        String dirPath = "/sdcard/newland/saveErrorImages";
        isDirPathExist(dirPath);

        //图片文件名为时间
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss_S");
        String sd = sdf.format(new Date(timeStamp));
        String imgFileName = sd + ".bmp";
        String txtFileName = "error_image_data.txt";
        
        File f = new File(dirPath, imgFileName);

        if (f.exists()) {
            f.delete();
        }
        try {
            String errorResult = String.format("[%s] %s\n",imgFileName,decodeStr);
            writeTxtAppend(dirPath, txtFileName, errorResult);
//            Toast.makeText(getApplicationContext(), R.string.common_save_success, Toast.LENGTH_SHORT).show();

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f)) ;
            out.write(bmpBytes);
            out.close();

            Log.i(TAG, "save success");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

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
                    default:
                        sendMsg("Error event code:" + eventCode, HANDLER_SCANNER_OTHER_ERROR);
                        break;
                }
                if (timeGap < 500)
                    return 1;//连扫模式，不关灯
                return 0;
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
//        scanDelayThreadPool.shutdown();
    }

    public static void writeTxtAppend(String dirPath, String txtPath, String content) {
        FileOutputStream fileOutputStream = null;
        File file = new File(dirPath, txtPath);
        try {
            if (!file.exists()) {
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void isDirPathExist(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
