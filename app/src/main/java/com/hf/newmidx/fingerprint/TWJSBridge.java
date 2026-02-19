package com.hf.newmidx.fingerprint;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class TWJSBridge {
    private Context context;

    public TWJSBridge(Context context2, String base64Data2) {
        this.context = context2;
    }

    @JavascriptInterface
    public void onButtonClick(String buttonId) {
        Log.i("JSBridge", " handleButtonClick");
        ((FingerPrintWithDBActivity) context).onWebButtonClicked(buttonId);
    }
}
