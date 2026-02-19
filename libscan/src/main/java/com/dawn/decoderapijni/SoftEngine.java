package com.dawn.decoderapijni;

import android.content.Context;
import android.graphics.ImageFormat;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SoftEngine 解码引擎类
 * <p>
 * 主要包括解码库相关的初始化、开始扫码、停止扫码、码制设置等功能。此类的代码建议保持原样，不要修改。
 * Include the initialization/StartScan/StopScan/CodeSetting and other functions related to
 * the decoding library. We do NOT recommend you modify this file.
 */
public class SoftEngine {

    public static final int SCN_EVENT_NONE = 0;
    public static final int SCN_EVENT_DEC_SUCC = 1;
    public static final int SCN_EVENT_DEC_CANCEL = 2;
    public static final int SCN_EVENT_NO_IMAGE = 3;
    public static final int SCN_EVENT_DEC_TIMEOUT = 4;
    public static final int SCN_EVENT_ERROR = 5;
    public static final int SCN_EVENT_SCANNER_OVERHEAT = 6;

    private static ScanningCallback mScanningCallback;
    private static UpgradeProgressCallback mUpgradeProgressCallback;
    private static InterfaceCodeAttrProp mInterfaceCodeAttrProp;
    private final static String TAG = "ScanJni SoftEngine";

    public final static int JNI_IOCTRL_SET_DECODE_IMG = 0x02ef;
    public final static int JNI_IOCTRL_SET_CONTEXT = 0x02f2;
    public final static int JNI_IOCTRL_SET_SCAN_TIMEOUT = 0x02F9;
    public final static int JNI_IOCTRL_SET_SCAN_ILLUMINATION_ON_OFF = 0x02FA;
    public final static int JNI_IOCTRL_RESET_ALL_CODE_SETTINGS = 0x02FB;
    public final static int JNI_SOFTENGINE_IOCTRL_SET_CAMERA_ID = 0x02fe;
    public final static int JNI_IOCTRL_SET_MULTICODE_SEPARATOR = 0x02C0;

    private final static int JNI_IOCTRL_GET_SCANNER_VERSION = 0x02D4;
    private final static int JNI_IOCTRL_GET_SDK_VERSION = 0x02D5;
    private final static int JNI_IOCTRL_GET_DECODE_VERSION = 0x02D6;
    private final static int JNI_IOCTRL_GET_SCANNER_TEMP = 0x02D7;

    private final static int JNI_IOCTRL_SET_NLSCAN_DATA_DIR = 0x03F3;
    private final static int JNI_IOCTRL_SETTING_SYSTEM_LANGUAGE = 0x03F2;
    private final static int JNI_IOCTRL_SET_FOCUS_DECODE_ENABLE = 0x02FC;
    private final static int JNI_IOCTRL_SET_FOCUS_DECODE_CALIBRATION = 0x02FD;

    private final static int JNI_IOCTRL_GET_FOCUS_DECODE_ENABLE = 0x02FF;
    public final static int JNI_IOCTRL_GET_HIGHLIGHT_SUPPORT = 0x02C6;
    public final static int JNI_IOCTRL_GET_SCAN_ILLUMINATION_ON_OFF = 0x02C7;
    public final static int JNI_IOCTRL_SET_HIGHLIGHT_SUPPORT = 0x02C8;
    public final static int JNI_IOCTRL_SET_HIGHLIGHT_FRAMES = 0x02C9;
    public final static int JNI_IOCTRL_GET_HIGHLIGHT_FRAMES = 0x02CA;
    public final static int JNI_IOCTRL_SETUP_EXPOSURE = 0x02CB;
    public final static int JNI_IOCTRL_SETUP_GAIN = 0x02CC;
    public final static int JNI_IOCTRL_SETUP_BRIGHTNESS = 0x02CD;
    public final static int JNI_IOCTRL_SETUP_LIGHTTIME = 0x02CE;
    public final static int JNI_IOCTRL_SET_DECODEDELAYTIME = 0x02CF;

    private int FLAG_STATE = 0x00;
    private final int SIGN_INIT = 0x01;
    private final int SIGN_OPEN = 0x10;
    private static long decodeTime = 0;// Time for decode


    private static SoftEngine mInstance = new SoftEngine();

    private ICamera mCamera = null;

    private final Lock mApiLock = new ReentrantLock();

    private SoftEngine() {
        Log.d(TAG, "new SoftEngine()");
    }

    public static SoftEngine getInstance(Context context) {
        return mInstance;
    }

    public static SoftEngine getInstance() {
        return mInstance;
    }

    static {
        try {
            // 载入本地库
            System.loadLibrary("NlscanHostDecodeJni");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean initSoftEngine(Context context, String nlscanDataPath) {
        if (setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_NLSCAN_DATA_DIR, 0, nlscanDataPath)!=0) {
            return false;
        }
        return initSoftEngine(context);
    }

    /**
     * 探寻模组并初始化SDK。建议只在APP初始化时做一次即可。
     * Probe scanner and initialize SDK. We recommend invoking this function once for all during your
     * application initialization.
     *
     * @return true - 成功Success; false - 失败Fail
     */
    public boolean initSoftEngine(Context context) {
        mApiLock.lock();
        try {
            Log.d(TAG, "initSoftEngine() start");
            if (mCamera==null) {
                //mCamera = new ScanCamera2(context, ImageFormat.YUV_420_888);
    //            mCamera = new ScanCamera2(context, ImageFormat.RAW_PRIVATE);
				//mCamera = new ScanCamera2(context, ImageFormat.RAW10);
            	mCamera = new ScanCamera();
            }
            if (quickJniInit()) {
                JniScnIOCtrlEx(JNI_IOCTRL_SET_CONTEXT, 0, mCamera);
    //            int camId = Camera.getNumberOfCameras()-1;
    //            int delayTime = 0; // unit:ms
    //            if (camId>=0) {
    //                JniScnIOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_CAMERA_ID, camId, null);
    //            }
    //            JniScnIOCtrlEx(JNI_IOCTRL_SET_DECODEDELAYTIME, delayTime, null);
                Log.d(TAG, "initSoftEngine() return true");
                return true;
            }
            Log.d(TAG, "initSoftEngine() return false");
            return false;
        } finally {
            mApiLock.unlock();
        }
    }


    /**
     * IOCtrl设置，包含多种设置命令
     * Control SDK and scanner, including multiple setting commands
     *
     * @param cmd    命令标识 command
     * @param param1 int型数据项 data (int)
     * @param obj    Object型数据项 data (object)
     * @return 0 - 成功Success; <0 - 失败Fail
     */
    public int setSoftEngineIOCtrlEx(int cmd, int param1, Object obj) {
        mApiLock.lock();
        try {
            return JniScnIOCtrlEx(cmd, param1, obj);
        } finally {
            mApiLock.unlock();
        }
    }


    /**
     * 开始扫码
     * Start Scan
     *
     * @return true - 成功Success; false - 失败Fail
     */
    public boolean StartDecode() {
        mApiLock.lock();
        try {
            decodeTime = System.currentTimeMillis();
    //        ServiceTools.getInstance().setDecodeTime(decodeTime);
            Log.d(TAG, "App StartDecode()");
            return quickJniStartDecode("sendScanningResultFromNative", 0);
        } finally {
            mApiLock.unlock();
        }
    }


    /**
     * 停止扫码
     * Stop scan
     *
     * @return true - 成功Success; false - 失败Fail
     */
    public boolean StopDecode() {
        mApiLock.lock();
        try {
            Log.d(TAG, "App StopDecode() ");
            return quickJniStopDecode(0);
        } finally {
            mApiLock.unlock();
        }
    }


    /**
     * 模组上电。建议在initSoftEngine初始化完成后，调用此接口，保持模组上电，为扫描做好准备。通常，在您的设备唤醒时，
     * 也需要调用此接口重新上电。
     * Power up the scanner, make SDK ready for scanning. We recommend invoking this function
     * after initSoftEngine, keep scanner and SDK ready. Also, this function should be invoked
     * after your device wake up.
     *
     * @return true - 成功Success; false - 失败Fail
     */
    public boolean Open() {
        mApiLock.lock();
        try {
            Log.d(TAG, "SoftEngine Open() ");
            return quickJniOpen();
        } finally {
            mApiLock.unlock();
        }
    }


    /**
     * 模组下电。通常，在您的设备休眠时，需要调用此接口，控制模组下电。
     * Power down the scanner. Usually, this function should be invoked before your device sleep.
     *
     * @return true - 成功Success; false - 失败Fail
     */
    public boolean Close() {
        mApiLock.lock();
        try {
            Log.d(TAG, "SoftEngine Close() ");
            return quickJniClose();
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 设置扫码回调函数
     * Set callback of Scanning
     *
     * @param scanningCallback 回调接口
     */
    public void setScanningCallback(ScanningCallback scanningCallback) {
        mApiLock.lock();
        try {
            this.mScanningCallback = scanningCallback;
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 扫码完成后会调用该接口，返回扫描结果
     * This function will be called after scanning, return decoding result
     *
     * @param event_code 回调结果标识符;对于扫码完成后的回调，该值为1
     *                   The identifier of the callback result.If the decoding is successful,
     *                   the value is 1.
     * @param msgType    (reserve)
     * @param bMsg1      扫码结果数据 Code data.
     * @param bMsg2      (reserve)
     * @param length     The length of code data.
     * @return 1
     */
    public static int sendScanningResultFromNative(int event_code, int msgType, byte[] bMsg1,
                                                   byte[] bMsg2, int length) {
        Log.d(TAG, "sendScanningResultFromNative");
        // 后续，返回值会影响SDK库的一些工作行为。如果不需要，默认先返回1
        return mScanningCallback.onScanningCallback(event_code, msgType, bMsg1, length);
    }


    /**
     * 扫码结果回调接口
     * Scanning result callback interface
     */
    public interface ScanningCallback {
        /**
         * 扫码结果回调函数
         *
         * @param eventCode 回调结果标识符;对于扫码完成后的回调，该值为1
         *                  The identifier of the callback result.If the scan is successful, the value is 1
         * @param param1    (reserve)
         * @param param2    扫码结果数据 Code data.
         * @param length    The length of code data.
         */
        int onScanningCallback(int eventCode, int param1, byte[] param2, int length);
    }


    /**
     * 固件更新回调接口，更新过程中会调用该接口
     * Callback function for firmware upgrade progress.This function will be called during the upgrade process
     *
     * @param progressValue 当前包数 Current progress
     * @param totalValue    包总数 Total progress
     */
    public static int callBackUpdateProgress(int progressValue, int totalValue) {
//        Log.d(TAG, "callBackUpdateProgress " + progressValue + " " + totalValue);
        if (mUpgradeProgressCallback != null) {
            mUpgradeProgressCallback.onUpgradeCallback(progressValue, totalValue);
        }
        return 1;
    }

    /**
     * 固件更新回调接口
     * Callback interface for firmware upgrade progress
     */
    public interface UpgradeProgressCallback {
        /**
         * 回调函数
         *
         * @param progressValue 当前包数 Current progress
         * @param totalValue    包总数 Total progress
         */
        void onUpgradeCallback(int progressValue, int totalValue);
    }

    /**
     * 设置更新回调函数
     * Set upgradeCallback
     *
     * @param upgradeCallback 回调接口
     */
    public void setUpgradeCallback(UpgradeProgressCallback upgradeCallback) {
        mApiLock.lock();
        try {
            this.mUpgradeProgressCallback = upgradeCallback;
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 反初始化SoftEngine
     * Release the resource of SDK.
     *
     * @return true - 成功Success; false - 失败Fail
     */
    public boolean Deinit() {
        mApiLock.lock();
        try {
            Log.d(TAG, "Deinit() ");
            quickJniClose();
            return quickJniDeInit();
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 获取解码方案版本信息
     * Get SDK version
     *
     * @return 版本信息字符串 SDK version string
     */
    public String SDKVersion() {
        mApiLock.lock();
        try {
            return JniGetVersion(JNI_IOCTRL_GET_SDK_VERSION);
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 获取扫描头固件版本、硬件版本
     * Get scanner firmware version and hardware version
     *
     * @return 版本信息字符串 Version string
     */
    public String getScannerVersion() {
        mApiLock.lock();
        try {
            return JniGetVersion(JNI_IOCTRL_GET_SCANNER_VERSION);
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 获取解码算法库版本信息
     * Get algorithm library version
     *
     * @return 版本信息字符串 Version string
     */
    public String getDecodeVersion() {
        mApiLock.lock();
        try {
            return JniGetVersion(JNI_IOCTRL_GET_DECODE_VERSION);
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 码制参数设置
     * Set code parameter
     *
     * @param Id     码制（Id） Code Name
     * @param Param1 设置项     Attribute
     * @param Param2 设置值     Value
     * @return >= 0  成功Success;
     * < 0 失败Fail
     */
    public int ScanSet(String Id, String Param1, String Param2) {
        mApiLock.lock();
        try {
            return JniSetCodeAttrValue(Id, Param1, Param2);
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 获取码制参数设置值
     * get code parameter value
     *
     * @param Id     码制（Id） Code Name
     * @param Param1 设置项     Attribute
     * @return 参数值     Value
     */
    public String ScanGet(String Id, String Param1) {
        mApiLock.lock();
        try {
            return JniGetCodeAttrValue(Id, Param1);
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 设置扫码引擎使用的camera id
     * Set the camera ID used by softEngine
     *
     * @param cameraId
     */
    public void setCameraId(int cameraId) {
        setSoftEngineIOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_CAMERA_ID, cameraId, null);
    }

    /**
     * 获取最后一次解码图片,无论是否解码成功
     * Get the last decoded picture
     *
     * @return 图片数据 Image bytes
     */
    public byte[] getLastImage() {
        mApiLock.lock();
        try {
            return JniGetLastImage();
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * 获取扫描头的当前温度
     * Get current temperature of scanner
     *
     * @return 扫描头的当前摄氏温度 temperature
     */
    public int getScannerTemperature() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_GET_SCANNER_TEMP, 0, null);
    }

    /**
     * 设置单次扫描的超时时间
     * Set scanning timeout (ms)
     *
     * @param timeout 单次扫描的超时时间，单位毫秒
     */
    public void setScanTimeout(int timeout) {
        setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_SCAN_TIMEOUT, timeout, null);
    }

    /**
     * 设置补光灯使能
     * Set Illumination Enable
     *
     * @param enable 开关使能
     */
    public void setIlluminationEnable(int enable) {
        setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_SCAN_ILLUMINATION_ON_OFF, enable, null);
    }

    /**
     * 获取补光灯使能
     * Get Illumination Enable
     *
     * @param
     * @return enable  使能
     */
    public int getIlluminationEnable() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_GET_SCAN_ILLUMINATION_ON_OFF, 0, null);
    }

    /**
     * 设置高爆闪使能
     * Set Highlight Enable
     *
     * @param enable 开关使能
     */
    public int setHighlightEnable(int enable) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_HIGHLIGHT_SUPPORT, enable, null);
    }

    /**
     * 获取高爆闪使能
     * Get Highlight Enable
     *
     * @param
     * @return enable  使能
     */
    public int getHighlightEnable() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_GET_HIGHLIGHT_SUPPORT, 0, null);
    }

    /**
     * 设置高爆闪帧率
     * Set Highlight Frames
     *
     * @param frames 帧率
     */
    public int setHighlightFrames(int frames) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_HIGHLIGHT_FRAMES, frames, null);
    }

    /**
     * 获取高爆闪帧率
     * Get Highlight Frames
     *
     * @param
     * @return frames  帧率
     */
    public int getHighlightFrames() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_GET_HIGHLIGHT_FRAMES, 0, null);
    }

    /**
     * 设置期望亮度
     * Set Expect Brightness
     *
     * @param brightness 亮度
     */
    public int setExpectBrightness(int brightness) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_BRIGHTNESS, brightness, null);
    }

    /**
     * 获取期望亮度
     * Get Expect Brightness
     *
     * @return brightness  亮度
     */
    public int getExpectBrightness() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_BRIGHTNESS, -1, null);
    }

    /**
     * 设置补光灯时间
     * Set LightTime
     *
     * @param lightTime 补光时间
     */
    public int setLightTime(int lightTime) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_LIGHTTIME, lightTime, null);
    }

    /**
     * 获取补光灯时间
     * Get LightTime
     *
     * @param
     * @return LightTime  补光灯时间
     */
    public int getLightTime() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_LIGHTTIME, -1, null);
    }

    /**
     * 设置最大曝光值
     * Set MaxExposure
     *
     * @param exposure 曝光值
     */
    public int setMaxExposure(int exposure) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_EXPOSURE, exposure, "MAX");
    }
    /**
     * 设置最小曝光值
     * Set MinExposure
     *
     * @param exposure 曝光值
     */
    public int setMinExposure(int exposure) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_EXPOSURE, exposure, "MIN");
    }
    /**
     * 获取最大曝光值
     * Get MaxExposure
     *
     * @return exposure  曝光值
     */
    public int getMaxExposure() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_EXPOSURE, -1, "MAX");
    }
    /**
     * 获取最小曝光值
     * Get MinExposure
     *
     * @return exposure  曝光值
     */
    public int getMinExposure() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_EXPOSURE, -1, "MIN");
    }
    /**
     * 设置最大曝光增益
     * Set MaxGain
     *
     * @param gain 增益值
     */
    public int setMaxGain(int gain) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_GAIN, gain, "MAX");
    }
    /**
     * 设置最小曝光增益
     * Set MinGain
     *
     * @param gain 增益值
     */
    public int setMinGain(int gain) {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_GAIN, gain, "MIN");
    }
    /**
     * 获取最大曝光增益
     * Get MaxExposure
     *
     * @return gain  增益值
     */
    public int getMaxGain() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_GAIN, -1, "MAX");
    }
    /**
     * 获取最小曝光增益
     * Get MinExposure
     *
     * @return gain  增益值
     */
    public int getMinGain() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_SETUP_GAIN, -1, "MIN");
    }
    /**
     * 设置多码同图解码的分隔符
     * Set scanning timeout (ms)
     *
     * @param spec 分隔符字节数组
     */
    public void setMulticodeSeparator(byte[] spec) {
        setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_MULTICODE_SEPARATOR, spec.length, spec);
    }


    /**
     * 设置系统语言
     * Set System Language
     *
     * @param langId 0-zh 1-en
     */
    public void setNdkSystemLanguage(int langId) {
        setSoftEngineIOCtrlEx(JNI_IOCTRL_SETTING_SYSTEM_LANGUAGE, langId, null);
    }

    /**
     * 设置中心区域解码使能
     * set Focus Decode Enable
     *
     * @param enable 0-Disable 1-Enable
     */
    public void setFocusDecodeEnable(int enable) {
        setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_FOCUS_DECODE_ENABLE, enable, null);
    }

    /**
     * 获取中心区域解码使能
     * set Focus Decode Enable
     *
     * @return 1-On else-Off
     */
    public int getFocusDecodeEnable() {
        return setSoftEngineIOCtrlEx(JNI_IOCTRL_GET_FOCUS_DECODE_ENABLE, 0, null);
    }

    /**
     * 中心区域解码校准
     * setFocusDecodeCalibration
     * \
     */
    public void setFocusDecodeCalibration() {
        setSoftEngineIOCtrlEx(JNI_IOCTRL_SET_FOCUS_DECODE_CALIBRATION, 1, null);
    }

    public int setDecodeImage(byte[] img) {
        if (mApiLock.tryLock()) {
            try {
                return JniScnIOCtrlEx(SoftEngine.JNI_IOCTRL_SET_DECODE_IMG, img.length, img);
            } finally {
                mApiLock.unlock();
            }
        }
        return -1;
    }

    /**
     * 批量获取码制属性值 Get code attribute values in bulk
     * 回调函数 Callback：callBackCodeAttrProp
     *
     * @param codeName 可为"ALL" . If get all, set "ALL"
     * @param attrName 可为"ALL" . If get all, set ALL"
     */
    public int getCodeHelpDoc(String codeName, String attrName) {
        mApiLock.lock();
        try {
            return JniCodeHelpDoc(codeName, attrName);
        } finally {
            mApiLock.unlock();
        }
    }

    /**
     * Native 代码中回调该接口，返回码制属性
     * This function will be called When the attribute value is returned
     */
    public static int callBackCodeAttrProp(String codeName, String fullCodeName, String codeType,
                                           String attrName, String attrNickName, String attrType,
                                           int value, String propNote) {
        Log.d(TAG, "setCodeHelpCallback:" + codeName + " " + fullCodeName + " " + codeType + " "
                + attrName + " " + attrNickName + " " + attrType + " " + value + " " + propNote);
//        Log.v(TAG,)
        if (null != mInterfaceCodeAttrProp) {
            mInterfaceCodeAttrProp.onCodeAttrPropCallback(
                    codeName, fullCodeName, codeType, attrName, attrNickName, attrType, value, propNote);
        }
        return 0;
    }

    /**
     * 码制属性回调interface
     * Callback interface for attribute value query
     */
    public interface InterfaceCodeAttrProp {
        public void onCodeAttrPropCallback(String codeName, String fullCodeName, String codeType,
                                           String attrName, String attrNickName, String attrType,
                                           int value, String propNote);
    }

    public void setInterfaceCodeAttrProp(InterfaceCodeAttrProp newInterface) {
        this.mInterfaceCodeAttrProp = newInterface;
    }

    private boolean quickJniInit() {
        if ((FLAG_STATE & SIGN_INIT) == SIGN_INIT) {
            return true;
        }
        try {
            if (JniInit()) {
                FLAG_STATE |= SIGN_INIT;
                return true;
            } else {
                Log.d(TAG, "JNI Init Fail. ");
            }
        } catch (DLException e) {
            Log.e(TAG, e.getCode() + e.getReasonPhrase());
        }
        return false;
    }

    private boolean quickJniDeInit() {
        if ((FLAG_STATE & SIGN_INIT) != SIGN_INIT) {
            return true;
        }
        if (JniDeInit()) {
            FLAG_STATE &= ~SIGN_INIT;
            return true;
        }
        return false;
    }

    private boolean quickJniOpen() {
        if ((FLAG_STATE & SIGN_OPEN) == SIGN_OPEN) {
            return true;
        }
        if (JniOpen()) {
            FLAG_STATE |= SIGN_OPEN;
            return true;
        }
        return false;
    }

    private boolean quickJniClose() {
        if ((FLAG_STATE & SIGN_OPEN) != SIGN_OPEN) {
            return true;
        }
        if (JniClose()) {
            FLAG_STATE &= ~SIGN_OPEN;
            return true;
        }
        return false;
    }

    private boolean quickJniStartDecode(String callbackFunc, int workMode) {
        if ((FLAG_STATE & SIGN_OPEN) != SIGN_OPEN) {
            return false;
        }
        return JniStartDecode(callbackFunc, workMode);
    }

    private boolean quickJniStopDecode(int workMode) {
        if ((FLAG_STATE & SIGN_OPEN) != SIGN_OPEN) {
            return false;
        }
        return JniStopDecode(workMode);
    }

    private native boolean JniInit() throws DLException;

    private native boolean JniDeInit();

    private native boolean JniOpen();

    private native boolean JniClose();

    private native boolean JniStartDecode(String callbackFunc, int WorkMode);

    private native boolean JniStopDecode(int WorkMode);

    private native int JniScnIOCtrlEx(int cmd, int param1, Object obj);

    private native int JniSetCodeAttrValue(String codeName, String attrName, String value);

    private native String JniGetCodeAttrValue(String codeName, String attrName);

    private native String JniGetHelpDoc(String codeName);

    private native int JniCodeHelpDoc(String codeName, String attrName);

    private native byte[] JniGetLastImage();

    private native String JniGetVersion(int cmd);

}
