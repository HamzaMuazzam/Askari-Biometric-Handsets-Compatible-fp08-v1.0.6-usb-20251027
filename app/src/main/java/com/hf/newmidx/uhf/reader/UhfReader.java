package com.hf.newmidx.uhf.reader;

import android.serialport.SerialPort;

import com.hf.newmidx.uhf.readerInterface.CommendManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;



public class UhfReader implements CommendManager {
    private static NewSendCommendManager manager;
    //private static ReaderPort serialPort;
    private static SerialPort serialPort;
    private static String port = "/dev/ttyS1";
    private static int baudRate = 115200;
    private static InputStream in;
    private static OutputStream os;
    private static UhfReader reader;

    public UhfReader() {
    }

    public static UhfReader getInstance() {
        if (serialPort == null) {
            try {
                serialPort = new SerialPort(new File(port), baudRate);
               // serialPort.OpenDevice(new File(port), baudRate, 0, 0);
            } catch (Exception var1) {
                return null;
            }

            in = serialPort.getInputStream();
            os = serialPort.getOutputStream();
        }

        if (manager == null) {
            manager = new NewSendCommendManager(in, os);
        }

        if (reader == null) {
            reader = new UhfReader();
        }

        return reader;
    }

    public static void setPortPath(String portPath) {
        port = portPath;
    }

    public String getPortPath() {
        return port;
    }

    public boolean setBaudrate() {
        return manager.setBaudrate();
    }

    public byte[] getFirmware() {
        return manager.getFirmware();
    }

    public boolean setOutputPower(int value) {
        return manager.setOutputPower(value);
    }

    public List<byte[]> inventoryRealTime() {
        return manager.inventoryRealTime();
    }

    public void setSelectMode(byte mode) {
        manager.setSelectMode(mode);
    }

    public void selectEpc(byte[] epc) {
        manager.selectEpc(epc);
    }

    public void setSelectPara(byte target, byte action, byte memBank, int pointer, byte maskLen, boolean truncated, byte[] mask) {
        manager.setSelectPara(target, action, memBank, pointer, maskLen, truncated, mask);
    }

    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        return manager.readFrom6C(memBank, startAddr, length, accessPassword);
    }

    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        return manager.writeTo6C(password, memBank, startAddr, dataLen, data);
    }

    public void setSensitivity(int value) {
        manager.setSensitivity(value);
    }

    public boolean lock6C(byte[] password, int memBank, int lockType) {
        return manager.lock6C(password, memBank, lockType);
    }

    public boolean lock6CwithPayload(byte[] password, int payload) {
        return manager.lock6CwithPayload(password, payload);
    }

    public boolean kill6C(byte[] password) {
        return manager.kill6C(password);
    }

    public boolean kill6C(byte[] password, int rfu) {
        return manager.kill6C(password, rfu);
    }

    public void close() {
        if (manager != null) {
            manager.close();
            manager = null;
        }

        if (serialPort != null) {
            serialPort.closePort();
            serialPort = null;
        }

        if (reader != null) {
            reader = null;
        }

    }

    public byte checkSum(byte[] paramArrayOfByte) {
        byte checksum = 0;
        byte[] var6 = paramArrayOfByte;
        int var5 = paramArrayOfByte.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            byte b = var6[var4];
            checksum += b;
        }

        return checksum;
    }

    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        return manager.setFrequency(startFrequency, freqSpace, freqQuality);
    }

    public void setDistance(int distance) {
    }

    public void close(InputStream input, OutputStream output) {
        if (manager != null) {
            manager = null;

            try {
                input.close();
                output.close();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

    }

    public int setWorkArea(int area) {
        return manager.setWorkArea(area);
    }

    public List<byte[]> inventoryMulti() {
        return manager.inventoryMulti();
    }

    public void stopInventoryMulti() {
        manager.stopInventoryMulti();
    }

    public int getFrequency() {
        return manager.getFrequency();
    }

    public boolean unSelect() {
        return manager.unSelect();
    }

    public void setRecvParam(int mixer_g, int if_g, int trd) {
        manager.setRecvParam(mixer_g, if_g, trd);
    }
}
