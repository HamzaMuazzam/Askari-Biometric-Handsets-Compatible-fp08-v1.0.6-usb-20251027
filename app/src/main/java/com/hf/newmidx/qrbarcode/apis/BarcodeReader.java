package com.hf.newmidx.qrbarcode.apis;


import android.hibory.CommonApi;
import android.util.Log;

public class BarcodeReader {
    private static String TAG = BarcodeReader.class.getSimpleName();
    private static BarcodeReader instance;

    private ReadThread mReadThread;
    private CommonApi mCommonApi;

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILED = -1;
    public static final int CODE_NOT_SUPPORT = -2;


    private int mComFd = -1;

    private int GPIO_SCANNER_POWER = 32;
    private int GPIO_SCANNER_POWER2 = 34;
    private int GPIO_SCANNER_TRIGGER = 31;

    private boolean isStart = false;

    private OnScannerListener scannerListener;

    public static BarcodeReader getInstance() {
        if (null == instance) {
            instance = new BarcodeReader();
        }
        return instance;
    }

    public BarcodeReader() {
        mCommonApi = new CommonApi();
    }


    public void setScannerListener(OnScannerListener scannerListener) {
        this.scannerListener = scannerListener;
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isStart) {
                try {
                    byte[] buffer = new byte[256];
                    int readLen = mCommonApi.readCom(mComFd, buffer, 256);
                    if (readLen > 0) {
                        byte[] realData = new byte[readLen];
                        System.arraycopy(buffer, 0, realData, 0, readLen);
                        if (scannerListener != null) {
                            scannerListener.onScan(realData);
                        }
                    }

                    Thread.sleep(50);
                    if (scannerListener != null) {
                        // scannerListener.onRecv(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    public int BarcodeOpen() {
        mCommonApi.setGpioMode(11, 0);
        mCommonApi.setGpioDir(11, 1);
        mCommonApi.setGpioOut(11, 1);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCommonApi.setGpioMode(172, 0);
        mCommonApi.setGpioDir(172, 1);
        mCommonApi.setGpioOut(172, 1);

        mCommonApi.setGpioMode(173, 0);
        mCommonApi.setGpioDir(173, 1);
        mCommonApi.setGpioOut(173, 1);


        mComFd = mCommonApi.openCom("/dev/ttyS1", 9600, 8, 'N', 1);
        if (mComFd < 0) {
            Log.d(TAG, "BarcodeOpen: open com fail");
            //return CODE_FAILED;
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] cmd = packUnifyCommand("SCNMOD0".getBytes());
        Log.d("hello", "SCNMOD0:" + Conversion.Bytes2HexString(cmd));
        mCommonApi.writeCom(mComFd, cmd, cmd.length);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] cmd2 = packUnifyCommand("AMLENA1".getBytes());
        Log.d("hello", "AMLENA1:" + Conversion.Bytes2HexString(cmd2));
        mCommonApi.writeCom(mComFd, cmd2, cmd2.length);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] cmd3 = packUnifyCommand("ILLSCN1".getBytes());
        Log.d("hello", "ILLSCN1:" + Conversion.Bytes2HexString(cmd3));
        mCommonApi.writeCom(mComFd, cmd3, cmd3.length);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] cmd4 = packUnifyCommand("SCNTCE1".getBytes());
        Log.d("hello", "SCNTCE1:" + Conversion.Bytes2HexString(cmd4));
        mCommonApi.writeCom(mComFd, cmd4, cmd4.length);
        isStart = true;
        mReadThread = new ReadThread();
        mReadThread.start();
        return CODE_SUCCESS;
    }

    private static byte[] packUnifyCommand(byte[] data) {
        if (data == null || data.length == 0) return null;
        int len = data.length;
        int dpos = 0;
        if (data[len - 1] == (byte) ';') --len;
        if (data[0] == (byte) '@' || data[0] == (byte) '#') {
            dpos = 1;
            --len;
        }
        byte[] buffer = new byte[9 + len];
        buffer[0] = 0x7e;
        buffer[1] = 1;
        buffer[2] = 0x30;
        buffer[3] = 0x30;
        buffer[4] = 0x30;
        buffer[5] = 0x30;
        buffer[6] = (byte) '#';
        if (dpos != 0) buffer[6] = data[0];
        System.arraycopy(data, dpos, buffer, 7, len);
        buffer[len + 7] = (byte) ';';
        buffer[len + 8] = 0x03;
        return buffer;
    }

    public void sendHeat() {
        String devName = android.os.Build.MODEL;

        Log.d(TAG, "BarcodeOpen:" + devName);
        byte[] cmd = packUnifyCommand("SCNMOD2".getBytes());
        Log.d("hello", "SCNMOD2:" + Conversion.Bytes2HexString(cmd));
        mCommonApi.writeCom(mComFd, cmd, cmd.length);

    }

    public void BarcodeClose() {
        isStart = false;
        mCommonApi.setGpioOut(172, 0);
        mCommonApi.setGpioOut(173, 0);
        mCommonApi.setGpioOut(11, 0);
    }

    public void BarcodeTrigger(boolean trigger) {
        if (trigger) {
            //mCommonApi.setGpioOut(11, 0);
            byte[] cmd0 = packUnifyCommand("SCNTRG1".getBytes());
            Log.d("hello", "SCNTRG1:" + Conversion.Bytes2HexString(cmd0));
            mCommonApi.writeCom(mComFd, cmd0, cmd0.length);
        } else {
            //mCommonApi.setGpioOut(11, 1);
            //SCNTRG0
            byte[] cmd1 = packUnifyCommand("SCNTRG0".getBytes());
            Log.d("hello", "SCNTRG0:" + Conversion.Bytes2HexString(cmd1));
            mCommonApi.writeCom(mComFd, cmd1, cmd1.length);
        }
    }


    public int crc_cal_by_bit(byte[] ptr, int len) {
        int crc = 0;
        int n = 0;
        while (len-- != 0) {
            for (char i = 0x80; i != 0; i /= 2) {
                crc *= 2;
                if ((crc & 0x10000) != 0) crc ^= 0x11021;
                if ((ptr[n] & i) != 0) crc ^= 0x1021;
            }
            n++;
        }
        return crc;
    }

    public void SetUartOutput() {
        byte[] cmd = new byte[9];
        cmd[0] = (0x7e);
        cmd[1] = (0x00);
        cmd[2] = (0x08);
        cmd[3] = (0x01);
        cmd[4] = (0x00);
        cmd[5] = (0x0D);
        cmd[6] = (0x00);
        cmd[7] = (byte) (0xab);
        cmd[8] = (byte) (0xcd);
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, cmd, cmd.length);
        }
    }

    public void SetCommandMode() {
        byte[] cmd = new byte[9];
        cmd[0] = (0x7e);
        cmd[1] = (0x00);
        cmd[2] = (0x08);
        cmd[3] = (0x01);
        cmd[4] = (0x00);
        cmd[5] = (0x00);
        //cmd[6]=(0x3E);
        cmd[6] = (0x15);
        cmd[7] = (byte) (0xab);
        cmd[8] = (byte) (0xcd);
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, cmd, cmd.length);
        }
    }

    public void ResetConfg() {
        byte[] cmd = new byte[9];
        cmd[0] = (0x7e);
        cmd[1] = (0x00);
        cmd[2] = (0x09);
        cmd[3] = (0x01);
        cmd[4] = (0x00);
        cmd[5] = (0x00);
        cmd[6] = (byte) (0xFF);
        cmd[7] = (byte) (0xab);
        cmd[8] = (byte) (0xcd);
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, cmd, cmd.length);
        }
    }

    public void SaveConfg() {
        byte[] cmd = new byte[9];
        cmd[0] = (0x7e);
        cmd[1] = (0x00);
        cmd[2] = (0x08);
        cmd[3] = (0x01);
        cmd[4] = (0x00);
        cmd[5] = (byte) (0xD9);
        cmd[6] = (byte) (0x56);
        cmd[7] = (byte) (0xab);
        cmd[8] = (byte) (0xcd);
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, cmd, cmd.length);
        }
    }

    public void BarcodeRead() {
        byte[] cmd = new byte[9];
        cmd[0] = (0x7e);
        cmd[1] = (0x00);
        cmd[2] = (0x08);
        cmd[3] = (0x01);
        cmd[4] = (0x00);
        cmd[5] = (0x02);
        cmd[6] = (0x01);
        cmd[7] = (byte) (0xab);
        cmd[8] = (byte) (0xcd);
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, cmd, cmd.length);
        }

    }

    public void BarcodeReadEx() {
        //byte[] cmd=new byte[2];
        //cmd[0]=(0x1b);
        //cmd[1]=(0x31);
        byte[] cmd = new byte[9];
        cmd[0] = (0x7e);
        cmd[1] = (0x00);
        cmd[2] = (0x08);
        cmd[3] = (0x01);
        cmd[4] = (0x00);
        cmd[5] = (0x02);
        cmd[6] = (0x01);
        cmd[7] = (byte) (0xab);
        cmd[8] = (byte) (0xcd);
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, cmd, cmd.length);
        }
    }

    public void BarcodeGetScanData() {
        byte[] command = new byte[12];
        command[0] = (byte) 0xef;
        command[1] = 0x01;
        command[2] = (byte) 0xff;
        command[3] = (byte) 0xff;
        command[4] = (byte) 0xff;
        command[5] = (byte) 0xff;
        command[6] = 0x01;
        command[7] = 0x00;
        command[8] = 0x03;
        command[9] = (byte) 0x99;
        command[10] = 0x00;
        command[11] = (byte) 0x9d;
        if (mCommonApi != null) {
            mCommonApi.writeCom(mComFd, command, command.length);
        }
    }

    public interface OnScannerListener {
        void onScan(byte[] data);

    }

}
