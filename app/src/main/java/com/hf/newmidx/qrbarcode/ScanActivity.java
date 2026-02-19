package com.hf.newmidx.qrbarcode;


import android.hibory.Conversion;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.R;
import com.hf.newmidx.databinding.ActivityScanBinding;
import com.hf.newmidx.qrbarcode.apis.BarcodeReader;


public class ScanActivity extends BaseActivity implements BarcodeReader.OnScannerListener {
    ActivityScanBinding scanBinding;

    private BarcodeReader barcodeReader;

    ToneGenerator tonePlayer = null;

    boolean isOpen = false;

    @Override
    public void onCreateBase(Bundle bundle) {
        scanBinding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(scanBinding.getRoot());

        initViews();

        barcodeReader = BarcodeReader.getInstance();
        barcodeReader.setScannerListener(this);

        tonePlayer = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
    }


    private void initViews(){
        scanBinding.actionbar.backTitleStyle(getString(R.string.item_scanner));

        scanBinding.btnTrigger.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_UP:
                        if(barcodeReader!=null){
                            barcodeReader.BarcodeTrigger(false);

                            //Toast.makeText(ScanActivity.this,"up",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        if(barcodeReader!=null){
                            barcodeReader.BarcodeTrigger(true);

                            //Toast.makeText(ScanActivity.this,"down",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:break;
                }

                return false;
            }
        });

        scanBinding.btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpen){
                    scanBinding.btnClose.performClick();
                }
                int result = barcodeReader.BarcodeOpen();
                switch (result){
                    case BarcodeReader.CODE_SUCCESS:
                        isOpen = false;
                        scanBinding.tvScannerState.setText(String.format(getString(R.string.scan_state),getString(R.string.scan_open_success)));
                        scanBinding.btnClose.setEnabled(true);
                        scanBinding.btnTrigger.setEnabled(true);
                        break;
                    case BarcodeReader.CODE_FAILED:
                        scanBinding.tvScannerState.setText(String.format(getString(R.string.scan_state),getString(R.string.scan_open_fail)));
                        break;
                    case BarcodeReader.CODE_NOT_SUPPORT:
                        scanBinding.tvScannerState.setText(String.format(getString(R.string.scan_state),"not support this device"));
                        break;
                }
            }
        });

        scanBinding.btnHeartbeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeReader.sendHeat();
            }
        });

        scanBinding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeReader.BarcodeClose();
                scanBinding.tvScannerState.setText("");
                scanBinding.tvRecv.setText("");
                scanBinding.btnClose.setEnabled(false);
                scanBinding.btnTrigger.setEnabled(false);
                isOpen = false;
            }
        });
    }



    @Override
    public void onScan(byte[] data) {
        if(data!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(ScanActivity.this,""+data,Toast.LENGTH_SHORT).show();
                    scanBinding.tvRecv.append(""+ Conversion.Bytes2HexString(data)+"\n");
                    scanBinding.tvRecv.append(""+new String(data)+"\n");
                    //tonePlayer.startTone(ToneGenerator.TONE_PROP_BEEP,100);
                    if(barcodeReader!=null) {
                        barcodeReader.BarcodeTrigger(false);
                    }
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(barcodeReader!=null){
            barcodeReader.BarcodeClose();
        }
        if(tonePlayer!=null){
            tonePlayer.stopTone();
            tonePlayer.release();
        }
    }
}
