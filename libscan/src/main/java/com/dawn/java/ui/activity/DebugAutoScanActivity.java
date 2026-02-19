package com.dawn.java.ui.activity;

import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_SUCC;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.AutoTestItemView;
import com.dawn.java.ui.widget.TopToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.dawn.decoderapijni.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DebugAutoScanActivity extends DawnActivity implements View.OnClickListener {
    private final int HANDLER_ADD_NEW_MESSAGE = 1;
    private final int HANDLER_SCANNER_OTHER_ERROR = 2;
    private TopToolbar topBar;
    private LinearLayout ll_container;
    private AlertDialog addTestItemDialog;
    private List<AutoTestItemView> listTestViews = new ArrayList<>();
    private AlertDialog.Builder deleteDialogBuilder;
    private TextView tv_empty;

    private Random random = new Random();
    private TextView tv_scan_result_cur;
    private FloatingActionButton fab;
    private boolean scanMusic = false;
    private boolean scanStatus = false;
    private int timeGap = 0;
    private CheckBox checkBox_music;
    private AutoTestItemView curTestView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_debug_auto_scan;
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
                addTestItemDialog.show();
            }
        });
        ll_container = findViewById(R.id.ll_container);
        tv_empty = findViewById(R.id.tv_empty);
        tv_scan_result_cur = findViewById(R.id.tv_scan_result_cur);
        fab = findViewById(R.id.debug_scan_fab);
        fab.setOnClickListener(this);
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

        initDialog();
    }

    private void initData() {
        setScanCallback();
    }

    private void initDialog() {
        View view = View.inflate(this, R.layout.layout_add_auto_item, null);
        final EditText et_delay = view.findViewById(R.id.et_delay);
        final EditText et_total_count = view.findViewById(R.id.et_total_count);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.common_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(getApplicationContext(),et_delay.getText().toString(),Toast.LENGTH_SHORT).show();
                        if (et_delay.getText().toString().isEmpty() || et_total_count.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), R.string.dialog_please_enter_completely, Toast.LENGTH_SHORT).show();
                        } else {
                            addTestItem(Integer.valueOf(et_delay.getText().toString()), Integer.valueOf(et_total_count.getText().toString()));
                            et_delay.setText("");
                            et_total_count.setText("");
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        addTestItemDialog = builder.create();
        deleteDialogBuilder = new AlertDialog.Builder(this);
    }

    private void checkListItemEmpty() {
        if (listTestViews.size() == 0) {
            tv_empty.setVisibility(View.VISIBLE);
        } else {
            tv_empty.setVisibility(View.GONE);
        }
    }

    private void addTestItem(int delay, int count) {
        final AutoTestItemView itemView = new AutoTestItemView(getApplicationContext(), delay, count);
        itemView.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteDialogBuilder
                        .setTitle(R.string.dialog_is_delete_this_item)
                        .setPositiveButton(R.string.common_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplication(), R.string.dialog_delete_complete, Toast.LENGTH_SHORT).show();
                                ll_container.removeView(itemView);
                                listTestViews.remove(itemView);
                                checkListItemEmpty();
                            }
                        }).setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                deleteDialogBuilder.create().show();
                return false;
            }
        });
        listTestViews.add(itemView);
        ll_container.addView(itemView);
        checkListItemEmpty();
    }

    private AutoTestItemView getCurTestView() {
        if (listTestViews.size() <= 0)
            return null;
        for (AutoTestItemView item : listTestViews) {
            if ((item.getState() == AutoTestItemView.ITEM_STATE_WAIT || item.getState() == AutoTestItemView.ITEM_STATE_PROCESSING)
                    && item.getCurCount() < item.getTotalCount()) {
                item.setItemState(AutoTestItemView.ITEM_STATE_PROCESSING);
                return item;
            }
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.debug_scan_fab) {
            if (scanStatus) {
                scanStatus = false;
                fab.setImageResource(R.drawable.ic_media_play);
                ServiceTools.getInstance().stopScan();
            } else {
                curTestView = getCurTestView();
                if (curTestView != null && curTestView.getCurCount() < curTestView.getTotalCount()) {
                    timeGap = curTestView.getDelay();
                    scanStatus = true;
                    fab.setImageResource(R.drawable.ic_media_pause);
                    ServiceTools.getInstance().startScan();
                }
            }
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
                    tv_scan_result_cur.setText(strResult);
                    tv_scan_result_cur.setTextColor(Color.argb(255, random.nextInt(256),
                            random.nextInt(256), random.nextInt(256)));
                    afterScanAction(true);
                    break;
                case HANDLER_SCANNER_OTHER_ERROR:
                    afterScanAction(false);
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

    private void afterScanAction(boolean isScanSuccess) {
        if (curTestView != null && scanStatus) {
            curTestView.setCurCount(curTestView.getCurCount() + 1);
            if (!isScanSuccess) {
                curTestView.setErrorCount(curTestView.getErrorCount() + 1);
            }

            if (curTestView.getCurCount() >= curTestView.getTotalCount()) {
                if (curTestView.getErrorCount() == 0) {
                    curTestView.setItemState(AutoTestItemView.ITEM_STATE_COMPLETE);  //无失败次数
                }else{
                    curTestView.setItemState(AutoTestItemView.ITEM_STATE_ERROR);     //有失败次数
                }
                curTestView = getCurTestView();
                if (curTestView == null) {
                    fab.setImageResource(R.drawable.ic_media_play);
                    scanStatus = false;
                    ServiceTools.getInstance().stopScan();
                    return;
                }

                timeGap = curTestView.getDelay();
            }
            if (timeGap == 0) {
                ServiceTools.getInstance().startScan();
            } else {
                scanDelayThreadPool.execute(scanDelayRunnable);
            }
        } else {
            fab.setImageResource(R.drawable.ic_media_play);
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

}
