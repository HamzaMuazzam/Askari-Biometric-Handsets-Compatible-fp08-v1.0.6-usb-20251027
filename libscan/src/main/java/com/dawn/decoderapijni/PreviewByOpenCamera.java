package com.dawn.decoderapijni;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
//import android.support.annotation.NonNull;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Collections;

public class PreviewByOpenCamera implements ICamera {

    private static final String TAG = "ScanJni/PreviewByOpenCamera";

    // 系统摄像头管理器对象
    private final CameraManager mCameraManager;

    private static class DeviceWrap {
        public CameraDevice device = null;
    }

    // 当前打开的摄像头对象
    private final DeviceWrap mCameraDevice = new DeviceWrap();

    private static class SessionWrap {
        public CameraCaptureSession session = null;
    }

    // 摄像头会话，用于向摄像头发送预览和拍照请求
    private final SessionWrap mCameraCaptureSession = new SessionWrap();

    private static class CameraReadyWatcher {
        public boolean ready = false;
    }

    private final CameraReadyWatcher mReadyWatcher = new CameraReadyWatcher();

    // 摄像头预览请求
    private CaptureRequest mPreviewRequest = null;

    // 照相机预览数据帧的接收者
    private ImageReader mImageReader = null;

    // 后台工作线程
    HandlerThread mBackgroundThread;

    // 可以用于向后台线程和主线程发送消息的 Handler
    private final Handler mBackgroundHandler;

    public int mPreviewFormat;

    public PreviewByOpenCamera(Context context, int previewFormat) {
        Log.d(TAG,"New instance ....");
        mCameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        mPreviewFormat = previewFormat;
        // 创建后台工作线程，初始化可以用于向后台线程发送消息的 Handler
        mBackgroundThread = new HandlerThread("PreviewByOpenCamera");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    ImageReader.OnImageAvailableListener onImage = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            synchronized (mReadyWatcher) {
                if (!mReadyWatcher.ready) {
                    mReadyWatcher.ready = true;
                    mReadyWatcher.notify();
                    Log.i(TAG, "onImageAvailable: First frame is fetched.");
                    return;
                }
            }
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();     // Y 通道，对应 planes[0]，size = width * height，pixelStride = 1
            byte[] buf = new byte[yBuffer.remaining()];
            yBuffer.get(buf);
            image.close();
            Log.i(TAG,"++++++++++++++++ onImageAvailable ++++++++++  bufsize : "+ buf.length);
            SoftEngine.getInstance()
                    .setDecodeImage(buf);

        }
    };

    CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG,"CameraStateCallback: Camera Open Success");
            synchronized (mCameraDevice) {
                // 保存当前打开的摄像头对象
                mCameraDevice.device = camera;
                mCameraDevice.notify();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i(TAG,"CameraStateCallback: Camera disconnect");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG,"CameraStateCallback: Camera state error: " + error);
        }
    };

    @SuppressLint("MissingPermission")
    public void cameraOpen(int cameraId, int width, int height) {
        Log.i(TAG, "cameraOpen " + cameraId + ", Resolution = " + width + " * " + height);
        mImageReader = ImageReader.newInstance(width, height, mPreviewFormat, 4);
        // 注册 ImageReade r的监听事件，当有图像流数据可用时会回调 onImageAvailable 方法，它的参数就是预览帧数据，可以对这帧数据进行处理。第二个参数指定回调的执行线程，我们通过后台线程执行回调
        mImageReader.setOnImageAvailableListener(onImage, mBackgroundHandler);
        try {
            // 打开指定摄像头。第一个参数表示要打开摄像机ID，第二个参数传入相机状态变化的回调接口，第三个参数用来确定相机状态变化的回调接口是在哪个线程执行，为 null 的话就在当前线程执行。
            mCameraManager.openCamera(""+cameraId, cameraStateCallback, mBackgroundHandler);
            boolean needWaitingSession = false;
            synchronized (mCameraDevice) {
                // 等待 Camera 预览准备完毕
                mCameraDevice.wait(1000);
                if (mCameraDevice.device != null) {
                    CaptureRequest.Builder builder = mCameraDevice.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    // 添加输出到 ImageReader 的 surface。然后我们就可以从 ImageReader 中获取预览数据流了。
                    builder.addTarget(mImageReader.getSurface());
                    builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                    builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
                    builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF);
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                    builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
                    builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    builder.set(CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_EFFECT_MODE_OFF);
                    mPreviewRequest = builder.build();
                    Log.i(TAG,"cameraOpen: createCaptureSession");
                    mCameraDevice.device.createCaptureSession(
                            Collections.singletonList(mImageReader.getSurface()), sessionStateCallback,
                            mBackgroundHandler);
                    needWaitingSession = true;
                }
            }
            if (needWaitingSession) {
                synchronized (mCameraCaptureSession) {
                    mCameraCaptureSession.wait(1000);
                    if (mCameraCaptureSession.session!=null) {
                        Log.i(TAG,"cameraOpen: setRepeatingRequest");
                        mCameraCaptureSession.session.setRepeatingRequest(mPreviewRequest,
                                null, mBackgroundHandler);
                    }
                }
                // 需要等待第一帧数据，以确保预览启动完毕。
                synchronized (mReadyWatcher) {
                    mReadyWatcher.ready = false;
                    mReadyWatcher.wait(1000);
                }
            }
        } catch (CameraAccessException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int cameraClose() {
        Log.i(TAG,"cameraClose ....");
        synchronized (mCameraCaptureSession) {
            if (mCameraCaptureSession.session!=null) {
                Log.i(TAG,"cameraClose: abortCaptures");
                try {
                    // 测试有几率出现关闭 camera 会卡住五六秒的现象，添加 mCameraCaptureSession.abortCaptures() 解决该问题
                    mCameraCaptureSession.session.abortCaptures();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG,"About to close camera session");
                // 也可以只用close来停止预览，效果与stopRepeating相同，会等待已开始流程的帧跑完。
                mCameraCaptureSession.session.close();
            }
            try {
                // 阻塞到session完成close，或者超时。
                mCameraCaptureSession.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (mCameraDevice) {
            if (mCameraDevice.device!=null) {
                Log.i(TAG,"Close camera");
                mCameraDevice.device.close();
                mCameraDevice.device = null;
            }
        }
        if (mImageReader != null) {
            Log.i(TAG,"Close ImageReader");
            mImageReader.close();
            mImageReader = null;
        }
        return 0;
    }

    private final CameraCaptureSession.StateCallback sessionStateCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            // 当会话配置好时，开始发送预览请求
            Log.i(TAG,"SessionStateCallback: Configured Success");
            synchronized (mCameraCaptureSession) {
                mCameraCaptureSession.session = cameraCaptureSession;
                mCameraCaptureSession.notify();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.e(TAG,"SessionStateCallback: Configure Failed");
            cameraCaptureSession.close();
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
            Log.i(TAG,"SessionStateCallback: onClosed");
            synchronized (mCameraCaptureSession) {
//                mCameraCaptureSession.session = null;
                mCameraCaptureSession.notify();
            }
        }
    };

    public void cameraStart() {

    }

    public void cameraStop() {

    }
}
