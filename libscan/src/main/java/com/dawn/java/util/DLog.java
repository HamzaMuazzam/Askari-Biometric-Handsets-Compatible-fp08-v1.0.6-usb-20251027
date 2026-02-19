package com.dawn.java.util;

import android.util.Log;

import java.util.Arrays;

public class DLog {

    private static boolean debugValue = true;

    public static void setDebug(boolean value) {
        debugValue = value;
    }

    public static boolean isDebug() {
        return debugValue;
    }

    public static void d(String TAG, String message) {

        if(!isDebug()) {
            return;
        }
        Log.d(TAG, message);
    }

    public static void d(String TAG, String message, Exception e) {
        if(!isDebug()) {
            return;
        }
        Log.d(TAG, message, e);
    }

    public static void e(String TAG, String message) {
        if(!isDebug()) {
            return;
        }
        Log.e(TAG, message);
    }

    public static void e(String TAG, String message, Throwable e) {
        if(!isDebug()) {
            return;
        }
        Log.e(TAG, message, e);
    }

    public static void e(String TAG, Throwable e) {
        if(!isDebug()) {
            return;
        }
        Log.e(TAG, Arrays.toString(e.getStackTrace()));
    }

    public static void i(String TAG, String message) {
        if(!isDebug()) {
            return;
        }
        Log.i(TAG, message);
    }

}
