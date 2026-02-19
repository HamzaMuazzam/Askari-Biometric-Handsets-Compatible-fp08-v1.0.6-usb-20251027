package com.hf.newmidx.uhf.reader;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReaderPort {
    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private boolean trig_on = false;

    static {
        System.loadLibrary("SerialPort");
    }

    public ReaderPort(String port, int baudrate, int flags) throws SecurityException, IOException {
        this.mFd = open(port, baudrate, flags);
        if (this.mFd == null) {
            Log.e("SerialPort", "native open returns null");
            throw new IOException();
        } else {
            this.mFileInputStream = new FileInputStream(this.mFd);
            this.mFileOutputStream = new FileOutputStream(this.mFd);
        }
    }

    public InputStream getInputStream() {
        return this.mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return this.mFileOutputStream;
    }

    private static native FileDescriptor open(String var0, int var1, int var2);

    public native void close();
}
