package com.hf.newmidx.fingerprint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.R;
import com.hf.newmidx.databinding.ActivityFingerprintWithDbBinding;
import com.hfteco.finger.FingerSDK;
import com.hfteco.finger.OnSdkInitListener;
import com.neutral.fingerprint.ISOANSI;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FingerPrintWithDBActivity extends BaseActivity implements OnWebClickListener{
    private FingerSDK fingerSDK;
    ActivityFingerprintWithDbBinding activityFingerprintWithDbBinding;
    private WebView webView;
    private ProgressDialog progressDialog;
    private static final int CAMERA_PERMISSION = 2001;
    private boolean wasFrontCamera;

    @Override
    public void onCreateBase(Bundle savedInstanceState) {
        activityFingerprintWithDbBinding = ActivityFingerprintWithDbBinding.inflate(getLayoutInflater());
        setContentView(activityFingerprintWithDbBinding.getRoot());
        webView = (WebView) findViewById(R.id.mainWebview);
        fingerSDK = new FingerSDK(this,new OnSdkInitListener() {
            @Override
            public void initResult(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i != FingerSDK.RESULT_OK) {
                            AlertDialog retryDialog =
                                    new AlertDialog.Builder(FingerPrintWithDBActivity.this)
                                            .setCancelable(false)
                                            .setTitle("INIT").setMessage(s)
                                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    finish();
                                                }
                                            })
                                            .setPositiveButton("Try Again",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            fingerSDK.launch();
                                                        }
                                                    }).create();
                            retryDialog.show();
                        }
                    }
                });
            }

            @Override
            public void onOpticalSensorInterrupt() {

            }

            @Override
            public void onOpticalSensorLost() {

            }
        });
        checkCameraPermission();
        loadWebsite();

    }
    @Override
    public void onResume(){
        super.onResume();
        fingerSDK.launch();
    }

    @Override
    public void onPause(){
        super.onPause();
        fingerSDK.release();
    }

    private void hostCapture(){
        fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, (i, bytes, bitmap, temp) -> {
            if (i==FingerSDK.RESULT_OK) {

                try {
//                    String tempString = new String(temp,"ISO8859-1");
//                    String hexString = android.hibory.Conversion.Bytes2HexString(temp);
//                    Log.e("hexString",hexString);
                    String base64String = android.util.Base64.encodeToString(temp, android.util.Base64.NO_WRAP);
                    Log.e("base64String",base64String);
                    uploadDataToWeb(base64String,bitmap);
                    int result = ISOANSI
                            .zzTransISO2005_ANSI2004(temp, temp);
                    if (result == 0) {
                        String ansiString = new String(temp, "ISO8859-1");
                    }
                    //now！you can save this tempString to your own db.
                    //I will use a hashmap to save it for example

                } catch (UnsupportedEncodingException e) {

                }

            } else {

            }
        });
    }


    private boolean isConnectedToWifi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected()
                    && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    @SuppressLint("NewApi")
    private void loadWebsite() {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
//        settings.setPluginState(WebSettings.PluginState.ON);
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();

        WebView.setWebContentsDebuggingEnabled(true);

        // JS Bridge
        webView.addJavascriptInterface(new TWJSBridge(this, ""), "TWJSBridge");

        webView.getSettings().setSupportMultipleWindows(false);
        WebView.enableSlowWholeDocumentDraw();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            // OPTIONAL: ignore SSL errors (⚠️ only for testing)
            @Override
            public void onReceivedSslError(
                    WebView view,
                    SslErrorHandler handler,
                    SslError error
            ) {
                handler.proceed();
            }
            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {
                Log.e("WEBVIEW_ERR",
                        error.getErrorCode() + " : " + error.getDescription());
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse errorResponse) {
                Log.e("HTTP_ERR",
                        errorResponse.getStatusCode() + "");
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("JS_CONSOLE", consoleMessage.message());
                return true;
            }

        });

        settings.setDatabaseEnabled(true);


        // Load URL based on network type
        String url;
        if (isConnectedToWifi()) {
//            url = "https://mbibtest.askaribank.com.pk/AccountOpeningApp/";
            url = "https://plive.askaribank.com/AccountOpeningApp/";
        } else {
            url = "https://192.168.32.86/AccountOpeningApp/";
        }
        webView.loadUrl(url);
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Processing image...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION
            );
            return false;
        }

        return true;
    }
    private Uri imageUri;

    private void openCamera(boolean front) {
        boolean checkCameraPermission = checkCameraPermission();
        if (!checkCameraPermission) return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        wasFrontCamera = front;

        if (front) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        }

        File imageFile = new File(getCacheDir(), "camera.jpg");
        imageUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                imageFile
        );

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("IsFrontCamera", front);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(intent, CAMERA_REQUEST);
    }
    private static final int CAMERA_REQUEST = 1001;
    private Bitmap capturedBitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                capturedBitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        imageUri
                );
                onImageCaptured(capturedBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void senData(final String datas, final OnIsoDataSubmit callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                webView.post(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        webView.evaluateJavascript(
                                "collectIsoData(" + JSONObject.quote(datas) + ")",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        callback.onSubmit(value);
                                    }
                                }
                        );
                    }
                });
            }
        }).start();
    }

    private void uploadDataToWeb(String matstring, Bitmap bitmap) {
        senData(matstring, new OnIsoDataSubmit() {
            @SuppressLint("NewApi")
            @Override
            public void onSubmit(String value) {
                String base64 = bitmap != null ? bitmapToBase64(bitmap) : "";
                webView.evaluateJavascript(
                        "javascript:onBiometricCompletion('" + base64 + "')",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.e("onBiometricCompletion", value);
                                Log.d("ISO_DATA", "onBiometricCompletion: " + value);
                            }
                        }
                );
            }
        });

    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
    private void onImageCaptured(final Bitmap bitmap) {

        // UI thread → show loader
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress();
            }
        });

        // 🔵 BACKGROUND THREAD (heavy work)
        new Thread(new Runnable() {
            @Override
            public void run() {

                final String type = wasFrontCamera ? "FRONT_CAMERA" : "BACK_CAMERA";
                Log.e("onImageCaptured", type);

                String base64 = bitmap != null ? bitmapToBase64(bitmap) : "";

                // Escape base64 for JS safety
                base64 = base64.replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "");

                final String js =
                        "javascript:onImageReceived('" + type + "','" + base64 + "')";

                // 🔴 BACK TO UI THREAD (WebView + dialog)
                runOnUiThread(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {

                        webView.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {

                                dismissProgress(); // ✅ dismiss loader

                                Log.d("JS_CALLBACK",
                                        "onImageReceived returned: " + value);

                                if (value == null || "null".equals(value)) {
                                    Log.e("JS_CALLBACK", "JS execution failed");
                                    return;
                                }

                                if ("true".equalsIgnoreCase(
                                        value.replace("\"", ""))) {
                                    Log.d("JS_CALLBACK",
                                            "Image received successfully");
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public void onWebButtonClicked(@NonNull String buttonId) {
        Log.e("onWebButtonClicked", buttonId);
        if (buttonId.contains("FINGERPRINT")) {
            hostCapture();
        } else if (buttonId.contains("FRONT_CAMERA")) {
            openCamera(true);
        } else if (buttonId.contains("BACK_CAMERA")) {
            openCamera(false);
        }
    }


}
