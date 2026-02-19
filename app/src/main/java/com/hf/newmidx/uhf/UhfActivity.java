package com.hf.newmidx.uhf;


import android.hibory.CommonApi;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.R;
import com.hf.newmidx.databinding.ActivityRfidBinding;
import com.hf.newmidx.uhf.reader.EPC;
import com.hf.newmidx.uhf.reader.Tools;
import com.hf.newmidx.uhf.reader.UhfReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UhfActivity extends BaseActivity {
    ActivityRfidBinding rfidBinding;

    private UhfReader reader;
    private CommonApi mCommonApi;
    private ArrayList<EPC> listEPC;
    private ArrayList<Map<String, Object>> listMap;

    private InventoryThread inventoryThread;
    private boolean runFlag = false;
    private boolean startFlag = false;
    ToneGenerator tonePlayer = null;

    @Override
    public void onCreateBase(Bundle bundle) {
        rfidBinding = ActivityRfidBinding.inflate(getLayoutInflater());
        setContentView(rfidBinding.getRoot());

        rfidBinding.actionbar.backTitleStyle(getString(R.string.item_rfid));

        listEPC = new ArrayList<EPC>();

        mCommonApi = new CommonApi();

        reader = UhfReader.getInstance();

        rfidBinding.tvState.setText(String.format(getString(R.string.uhf_state), ""));
        rfidBinding.tvVer.setText(String.format(getString(R.string.uhf_rom_version), ""));
        //Util.initSoundPool(this);
        tonePlayer = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);

        rfidBinding.btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(runFlag){
                    return;
                }
                openPower(true);

                byte[] data = reader.getFirmware();
                if(data!=null){
                    rfidBinding.tvState.setText(String.format(getString(R.string.uhf_state), getString(R.string.uhf_state_connected)));
                    rfidBinding.tvVer.setText(String.format(getString(R.string.uhf_rom_version), new String(data)));
                    runFlag = true;
                    inventoryThread = new InventoryThread();
                    inventoryThread.start();
                }else{
                    Log.d("hello","error" );
                    rfidBinding.tvState.setText(String.format(getString(R.string.uhf_state), getString(R.string.uhf_state_disconnected)));
                }
            }
        });

        rfidBinding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFlag = false;
                runFlag = false;
                openPower(false);

                rfidBinding.tvState.setText(String.format(getString(R.string.uhf_state), ""));
                rfidBinding.tvVer.setText(String.format(getString(R.string.uhf_rom_version), ""));
                clearData();

                rfidBinding.btnStart.setText(getString(R.string.rfid_search_start));
            }
        });

        rfidBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!runFlag){
                    return;
                }
                if(!startFlag){
                    startFlag = true;
                    rfidBinding.btnStart.setText(getString(R.string.rfid_search_stop));
                }else{
                    startFlag = false;
                    rfidBinding.btnStart.setText(getString(R.string.rfid_search_start));
                }

            }
        });
    }


    private void openPower(boolean onoff){
        //open uhf power
        if(onoff){
            mCommonApi.setGpioDir(15, 1);
            mCommonApi.setGpioOut(15, 1);
            try{
                Thread.sleep(300);
            }catch (Exception e){
                e.printStackTrace();
            }
            mCommonApi.setGpioDir(173, 1);
            mCommonApi.setGpioOut(173, 1);
        }else{
            mCommonApi.setGpioDir(15, 1);
            mCommonApi.setGpioOut(15, 0);
            try{
                Thread.sleep(300);
            }catch (Exception e){
                e.printStackTrace();
            }
            mCommonApi.setGpioDir(173, 1);
            mCommonApi.setGpioOut(173, 0);
        }
        try{
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addToList(final List<EPC> list, final String epc){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //��һ�ζ�������
                if(list.isEmpty()){
                    EPC epcTag = new EPC();
                    epcTag.setEpc(epc);
                    epcTag.setCount(1);
                    list.add(epcTag);
                }else{
                    for(int i = 0; i < list.size(); i++){
                        EPC mEPC = list.get(i);
                        //list���д�EPC
                        if(epc.equals(mEPC.getEpc())){
                            mEPC.setCount(mEPC.getCount() + 1);
                            list.set(i, mEPC);
                            break;
                        }else if(i == (list.size() - 1)){
                            //list��û�д�epc
                            EPC newEPC = new EPC();
                            newEPC.setEpc(epc);
                            newEPC.setCount(1);
                            list.add(newEPC);
                        }
                    }
                }

                listMap = new ArrayList<Map<String,Object>>();
                int idcount = 1;
                for(EPC epcdata:list){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("ID", idcount);
                    map.put("EPC", epcdata.getEpc());
                    map.put("COUNT", epcdata.getCount());
                    idcount++;
                    listMap.add(map);
                }
                rfidBinding.lv.setAdapter(new SimpleAdapter(UhfActivity.this,
                        listMap, R.layout.listview_epc_item,
                        new String[]{"ID", "EPC", "COUNT"},
                        new int[]{R.id.textView_id, R.id.textView_epc, R.id.textView_count}));
            }
        });
    }

    class InventoryThread extends Thread{
        private List<byte[]> epcList;

        @Override
        public void run() {
            super.run();
            while(runFlag){
                if(startFlag){
//					reader.stopInventoryMulti()
                    epcList = reader.inventoryRealTime(); //ʵʱ�̴�
                    if(epcList != null && !epcList.isEmpty()){
                        //������ʾ��
                        //Util.play(1, 0);
                        //tonePlayer.stopTone();
                        tonePlayer.startTone(ToneGenerator.TONE_PROP_BEEP,50);
                        for(byte[] epc:epcList){
                            if (epc != null)
                            {
                                String epcStr = Tools.Bytes2HexString(epc, epc.length);
                                Log.d("hello",epcStr);
                                addToList(listEPC, epcStr);
                            }
                        }
                    }
                    epcList = null ;
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            Log.d("hello","exit thread!!");
        }
    }
    private void clearData(){
        listEPC.removeAll(listEPC);
        rfidBinding.lv.setAdapter(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startFlag = false;
        runFlag = false;
        openPower(false);
        mCommonApi = null;
        if(reader != null){
            reader.close();
        }
        //Util.closeSoundPool();
        if(tonePlayer!=null){
            tonePlayer.stopTone();
            tonePlayer.release();
        }
    }
}
