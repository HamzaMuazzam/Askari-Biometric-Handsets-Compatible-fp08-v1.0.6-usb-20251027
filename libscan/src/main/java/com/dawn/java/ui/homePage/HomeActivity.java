package com.dawn.java.ui.homePage;

import static com.dawn.decoderapijni.ServiceTools.MSG_INIT_DONE;
import static com.dawn.decoderapijni.ServiceTools.MSG_INIT_FIRMWARE_UPGRADE;
import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_DEC_SUCC;
import static com.dawn.decoderapijni.SoftEngine.SCN_EVENT_SCANNER_OVERHEAT;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.BatchScanActivity;
import com.dawn.java.ui.activity.CenterScanSettingActivity;
import com.dawn.java.ui.activity.CodeSetActivity;
import com.dawn.java.ui.activity.DebugAccuracyActivity;
import com.dawn.java.ui.activity.DebugAutoScanActivity;
import com.dawn.java.ui.activity.DebugScanActivity;
import com.dawn.java.ui.activity.DebugUpdateActivity;
import com.dawn.java.ui.activity.DeviceUpdateActivity;
import com.dawn.java.ui.activity.HighLightSettingActivity;
import com.dawn.java.ui.activity.IlluminationSettingActivity;
import com.dawn.java.ui.activity.ImageShowActivity;
import com.dawn.java.ui.activity.VersionShowActivity;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.java.util.IlluminationUtil;
import com.google.android.material.navigation.NavigationView;
import com.dawn.decoderapijni.R;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class HomeActivity extends DawnActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final int HANDLER_SCAN_NEW_MESSAGE = 1;
    private final int HANDLER_SCANNER_OVERHEAT = 2;
    private final int HANDLER_SCANNER_OTHER_ERROR = 3;
    private boolean illuminationDataLoadFlag = false;

    private TopToolbar topBar;
    private Button btn_scan;
    private TextView tv_scanResult,tv_pointResult;
    private TextView tv_scanType;
    private TextView tv_scanTime;
    private TextView tv_sanTen;
    DrawerLayout drawer;
    NavigationView navigationView;
    private AtomicLong decodeTime = new AtomicLong(0);

    private ProgressDialog initProgressDialog = null;
    private boolean needToResetScanCallback = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_navi_home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(screenOnOffReceiver, filter);

        initView();

        initData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    if(illuminationDataLoadFlag == true)
                    {
                        IlluminationUtil.InitSPData(getApplicationContext());
                        IlluminationUtil.DataLoad();
                        break;
    }
                }

            }
        }).start();
    }
    private void initData() {
        if(ScanApplication.systemLocale.getLanguage().equals("zh")){  //中文
            SoftEngine.getInstance().setNdkSystemLanguage(0);  //中文
        }else{
            SoftEngine.getInstance().setNdkSystemLanguage(1);  //英文
        }
        setScanCallback();
        initProgressDialog = ProgressDialog.show(this, getString(R.string.init_prog_title),
                getString(R.string.init_prog_start), true, false);
        // 接下来的初始化操作，有可能会自动更新模组固件，需要保持屏幕常亮，避免整机休眠。
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ServiceTools.getInstance().startInit(this,
                getDir("nlscan", MODE_PRIVATE).getAbsolutePath(), true, mainThreadHandler);
    }

    private void initView() {
        topBar = findViewById(R.id.topBar);
        topBar.setMainTitle(getResources().getString(R.string.common_scan));
        tv_scanResult = findViewById(R.id.tv_scan_result);
        tv_scanResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_pointResult = findViewById(R.id.tv_point_result);
        tv_pointResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_scanTime = findViewById(R.id.tv_scan_time);
        tv_scanType = findViewById(R.id.tv_scan_type);
        tv_sanTen = findViewById(R.id.tv_san_len);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {
//                Intent intent = new Intent(HomeActivity.this, CodeSetActivity.class);
//                startActivity(intent);
                drawer.openDrawer(GravityCompat.END);
            }
        });
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(ScanApplication.TAG, "App button touch down");
                        decodeTime.set(System.currentTimeMillis());
                        ServiceTools.getInstance().startScan();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        Log.d(ScanApplication.TAG, "App button touch up");
                        ServiceTools.getInstance().stopScan();
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }


    private void setScanCallback() {
        SoftEngine.getInstance().setScanningCallback(new SoftEngine.ScanningCallback() {
            @Override
            public int onScanningCallback(int eventCode, int param1, byte[] param2, int length) {
                Log.d(TAG, "onScanningCallback");
                String strResult;
                String strAim;
                String strPoint;
                byte[] data = param2;
                if (data == null) {
                    return 0;
                }
                if (eventCode == SCN_EVENT_DEC_SUCC && data.length > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("time", System.currentTimeMillis() - decodeTime.get());
                    ScanApplication.mSoundPool.play(ScanApplication.soundId, 1, 1, 0, 0, (float) 1.0); // 播放声音
                    ScanApplication.vibrator.vibrate(100);
                    int i = 0;
                    for (i = 0; data[i] != 0; i++) {
                    }
                    strAim = new String(data, 0, i);
                    Log.d(ScanApplication.TAG, "Aim:" + strAim);
                    strResult = new String(param2, 128, length - 128);
                    Log.d(ScanApplication.TAG, "strResult:" + strResult);

                    int[][] point=new int[4][2];
                    for(i = 0;i < 4;i++) {
                        point[i][0] = (param2[28 + i * 8] & 0xff | param2[29 + i * 8] << 8 & 0xff00 | param2[30 + i * 8] << 16 & 0xff0000 | param2[31 + i * 8] << 24 & 0xff000000);
                        point[i][1] = (param2[32 + i * 8] & 0xff | param2[33 + i * 8] << 8 & 0xff00 | param2[34 + i * 8] << 16 & 0xff0000 | param2[35 + i * 8] << 24 & 0xff000000);
                    }
                    strPoint = "坐标左上("+point[0][0]+","+point[0][1]+")"
                                +"\n坐标右上("+point[1][0]+","+point[1][1]+")"
                                +"\n坐标左下("+point[2][0]+","+point[2][1]+")"
                                +"\n坐标右下("+point[3][0]+","+point[3][1]+")";
                    Message msg = new Message();
                    bundle.putString("msgType", strAim);
                    bundle.putString("msgResult", strResult);
                    bundle.putString("msgPoint", strPoint);
                    msg.setData(bundle);
                    msg.what = HANDLER_SCAN_NEW_MESSAGE;
                    mainThreadHandler.sendMessage(msg);
                } else if (eventCode == SCN_EVENT_SCANNER_OVERHEAT) {
                    sendMsg("Scanner is overheat", HANDLER_SCANNER_OVERHEAT);
                } else {
                    sendMsg("Error event code:" + eventCode, HANDLER_SCANNER_OTHER_ERROR);
                }
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
        mainThreadHandler.sendMessage(msg);
    }

    private Handler mainThreadHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_SCAN_NEW_MESSAGE:
                    Bundle b = msg.getData();
                    String strResult = b.getString("msgResult");
                    String strAim = b.getString("msgType");
                    String strPoint = b.getString("msgPoint");
                    Log.d(TAG, "Result:" + msg.arg2 + " body:" + strResult);
                    if (strResult != null) {
                        Random random = new Random();
                        tv_scanResult.setText(strResult);
                        tv_scanResult.setTextColor(Color.argb(255, random.nextInt(256),
                                random.nextInt(256), random.nextInt(256)));
                        tv_scanType.setText(strAim);
                        tv_scanTime.setText(String.valueOf(b.getLong("time")));
                        tv_sanTen.setText("" + strResult.length());
                    }
                    if (strPoint != null)
                        tv_pointResult.setText(strPoint);
                    return true;
                case HANDLER_SCANNER_OVERHEAT:
                    Toast.makeText(HomeActivity.this, msg.getData().getString("msg_str"), Toast.LENGTH_SHORT).show();
                    return true;
                case HANDLER_SCANNER_OTHER_ERROR:
                    Toast.makeText(HomeActivity.this, msg.getData().getString("msg_str"), Toast.LENGTH_SHORT).show();
                    return true;

                case MSG_INIT_DONE:
                    if (initProgressDialog!=null) {
                        initProgressDialog.dismiss();
                        illuminationDataLoadFlag = true;
                        initProgressDialog = null;
                    }
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    return true;
                case MSG_INIT_FIRMWARE_UPGRADE:
                    if (initProgressDialog!=null) {
                        initProgressDialog.setMessage(getString(R.string.init_prog_fw_upgrade));
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        if (needToResetScanCallback) {
            setScanCallback();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        needToResetScanCallback = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        try{
            unregisterReceiver(screenOnOffReceiver);
            ServiceTools.getInstance().deInit();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                SoftEngine.getInstance().Open();
                IlluminationUtil.DataLoad();
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                SoftEngine.getInstance().Close();
            }
        }
    };

    static boolean BtnFlg = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(TAG, "down code: " + keyCode);
        if (keyCode == 242 || keyCode == 241 || keyCode == 134 || keyCode == 135) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (!BtnFlg) {
                        BtnFlg = true;
                        Log.d(ScanApplication.TAG, "App button key down");
                        decodeTime.set(System.currentTimeMillis());
                        ServiceTools.getInstance().startScan();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    BtnFlg = false;
                    break;

                default:
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.d(TAG, " up code: " + keyCode);
//        if (keyCode == 242 || keyCode == 241) {
        if (keyCode == 242 || keyCode == 241 || keyCode == 134 || keyCode == 135) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    if (BtnFlg) {
                        BtnFlg = false;
                        ServiceTools.getInstance().stopScan();
                    }
                    break;

                default:
                    break;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, " onNavigationItemSelected: " + item.getItemId());
        int itemId = item.getItemId();
        if (itemId == R.id.nav_last_image) {
            startActivity(new Intent(HomeActivity.this, ImageShowActivity.class));
        } else if (itemId == R.id.nav_batch_scan) {
            startActivity(new Intent(HomeActivity.this, BatchScanActivity.class));
        } else if (itemId == R.id.nav_code_setting) {
            startActivity(new Intent(HomeActivity.this, CodeSetActivity.class));
        } else if (itemId == R.id.nav_fw_upgrade) {
            startActivity(new Intent(HomeActivity.this, DeviceUpdateActivity.class));
        } else if (itemId == R.id.nav_debug_scan) {
            startActivity(new Intent(HomeActivity.this, DebugScanActivity.class));
        } else if (itemId == R.id.nav_debug_accuracy) {
            startActivity(new Intent(HomeActivity.this, DebugAccuracyActivity.class));
        } else if (itemId == R.id.nav_debug_auto) {
            startActivity(new Intent(HomeActivity.this, DebugAutoScanActivity.class));
        } else if (itemId == R.id.nav_debug_upgrade) {
            startActivity(new Intent(HomeActivity.this, DebugUpdateActivity.class));
        } else if (itemId == R.id.nav_version_show) {
            startActivity(new Intent(HomeActivity.this, VersionShowActivity.class));
        } else if (itemId == R.id.nav_center_scan) {
            startActivity(new Intent(HomeActivity.this, CenterScanSettingActivity.class));
        } else if (itemId == R.id.nav_illumination) {
            startActivity(new Intent(HomeActivity.this, IlluminationSettingActivity.class));
        } else if (itemId == R.id.nav_highlight) {
            startActivity(new Intent(HomeActivity.this, HighLightSettingActivity.class));
        }

        drawer.closeDrawer(GravityCompat.END);
        return true;
    }
}
