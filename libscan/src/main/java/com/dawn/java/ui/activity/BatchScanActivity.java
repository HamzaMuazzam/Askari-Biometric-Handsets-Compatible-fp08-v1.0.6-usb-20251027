package com.dawn.java.ui.activity;

import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_SUCC;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.dawn.decoderapijni.R;
import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchScanActivity extends DawnActivity {
    private final int HANDLER_ADD_NEW_MESSAGE = 1;
    private final int HANDLER_SCANNER_OTHER_ERROR = 2;
    private static final int SINGLE_MODE = 0;
    private static final int CONTINUOUS_MODE = 1;
    private TopToolbar topBar;
    private CheckBox checkBox_music;
    private ListView lv_scan_result;
    private Set<String> setScanResults = new HashSet<>();
    private List<String> listScanResults = new ArrayList<>();
    private ScanResultsAdapter mScanResultsAdapter;

//    private CheckBox checkBox_mode;
//    FloatingActionButton fab_start_pause;
    private Button btnScan;
    private int scanMode = SINGLE_MODE;
    private boolean scanMusic = false;
    private boolean scanStatus = false;
//    private boolean isScanPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_batch_scan;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        topBar = findViewById(R.id.topBar);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {
//                AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(BatchScanActivity.this);
//                deleteDialogBuilder
//                        .setTitle(R.string.dialog_is_clear_scan_result)
//                        .setPositiveButton(R.string.common_confirm, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
                setScanResults.clear();
                listScanResults.clear();
                mScanResultsAdapter.notifyDataSetChanged();
                Toast.makeText(getApplication(), R.string.dialog_clear_result_complete, Toast.LENGTH_SHORT).show();
//                            }
//                        }).setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//                deleteDialogBuilder.create().show();

            }
        });
//        fab_start_pause = findViewById(R.id.fab_start_pause);
//        fab_start_pause.setOnClickListener(this);
        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
//                        isScanPressed = true;
//                        Log.d(MyApplication.TAG, "App button touch down");
                        scanStatus = true;
//                        fab_start_pause.setImageResource(R.drawable.ic_media_pause);
                        ServiceTools.getInstance().startScan();

                        break;
                    case MotionEvent.ACTION_CANCEL:
//                        Log.d(MyApplication.TAG, "App button touch CANCEL");
                        scanStatus = false;
                    case MotionEvent.ACTION_UP:
//                        Log.d(MyApplication.TAG, "App button touch up");
//                        isScanPressed = false;
                        scanStatus = false;
//                        fab_start_pause.setImageResource(R.drawable.ic_media_play);
                        ServiceTools.getInstance().stopScan();
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
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
//        checkBox_mode = findViewById(R.id.cb_continuous);
//        checkBox_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                if (isChecked) {
//                    scanMode = CONTINUOUS_MODE;
//                } else {
//                    scanMode = SINGLE_MODE;
//                }
//            }
//        });
//        checkBox_mode.setChecked(true);
        lv_scan_result = findViewById(R.id.lv_scan_result);
        mScanResultsAdapter = new ScanResultsAdapter(BatchScanActivity.this, listScanResults);
        lv_scan_result.setAdapter(mScanResultsAdapter);
    }

    private void initData() {
        setScanCallback();
//        setFocusDecodeEnable(1);
    }

//    public void setFocusDecodeEnable(int enable) {
//        SoftEngine.getInstance().setFocusDecodeEnable(enable);
//    }

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.fab_start_pause:
//                if (scanStatus) {
//                    scanStatus = false;
//                    fab_start_pause.setImageResource(R.drawable.ic_media_play);
//                    ServiceTools.getInstance().stopScan();
//                } else {
//                    scanStatus = true;
//                    fab_start_pause.setImageResource(R.drawable.ic_media_pause);
//                    ServiceTools.getInstance().startScan();
//                }
//                break;
//        }
//    }

    private static class ScanResultsAdapter extends BaseAdapter {
        private List<String> mList;
        private LayoutInflater mInflater;

        public ScanResultsAdapter(Context context, List<String> mList) {
            this.mList = mList;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            if (this.mList == null)
                return null;
            return this.mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.layout_scan_result_item, null);

                holder = new ViewHolder();

                holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
                holder.tv_result = (TextView) convertView.findViewById(R.id.tv_result);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String scanResult = mList.get(position);
            holder.tv_index.setText((position + 1) + "");
            holder.tv_result.setText(scanResult);
            return convertView;
        }

        private static class ViewHolder {
            private TextView tv_index;
            private TextView tv_result;

        }

    }

    //===============扫码回调======================
    private Handler handler = new Handler(new WeakReference<Handler.Callback>(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            Log.v(MyApplication.TAG,"Handler Thread:" + Thread.currentThread().getName());
            switch (msg.what) {
                case HANDLER_ADD_NEW_MESSAGE:
                    String strResult = msg.getData().getString("msg_str");
                    if (!setScanResults.contains(strResult)) {
                        if (scanMusic) {
                            ScanApplication.mSoundPool.play(ScanApplication.soundId, 1, 1, 0, 0, (float) 1.0); // 播放声音
                        }
                        setScanResults.add(strResult);
                        listScanResults.add(strResult);
                        mScanResultsAdapter.notifyDataSetChanged();
                    }
                    //.........
                    afterScanAction();

                    break;
                case HANDLER_SCANNER_OTHER_ERROR:
                    afterScanAction();
                    break;

            }
            return false;
        }
    }).get());

    //延时
    ExecutorService scanDelayThreadPool = Executors.newFixedThreadPool(1);
    Runnable scanDelayRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
                if (scanStatus) {
                    ServiceTools.getInstance().startScan();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void afterScanAction() {
//        if (scanMode == CONTINUOUS_MODE && scanStatus) {
        if (scanStatus) {
//            scanDelayThreadPool.execute(scanDelayRunnable);
            ServiceTools.getInstance().startScan();
        } else {
//            fab_start_pause.setImageResource(R.drawable.ic_media_play);
            scanStatus = false;
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

                            int i = 0;
                            for (i = 0; data[i] != 0; i++) {
                            }
                            strResult = new String(param2, 128, length - 128);
                            sendMsg(strResult, HANDLER_ADD_NEW_MESSAGE);
//                            gHasScanResult = true;
                        }
                        break;
                    default:
                        sendMsg("Error event code:" + eventCode, HANDLER_SCANNER_OTHER_ERROR);
                        break;
                }
                return 1;// 0不连扫 / 1 连扫模式，不关灯
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
//        setFocusDecodeEnable(0);
    }
}
