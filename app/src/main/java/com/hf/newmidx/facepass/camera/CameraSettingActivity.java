package com.hf.newmidx.facepass.camera;

import android.os.Build;

import com.hf.newmidx.BaseActivity;
import com.hf.newmidx.facepass.camera.CameraManager;

public abstract class CameraSettingActivity extends BaseActivity {
    public int cameraRotation = 0;
    public boolean mirror= true ;
    public boolean cameraFront = true;
    public CameraManager manager;

    public void CameraSetting(){
        String modelName = Build.MODEL.replace("-","");
        //前置后置摄像头
        if (modelName.contains("X05")||modelName.contains("rk3568") || modelName.contains("F68V")) {
            cameraFront = false;
            if(modelName.contains("rk3568") || modelName.contains("F68V")) {
                mirror = false;
            }
        } else {
            cameraFront = true;
        }
        //cameraRotation算法角度，manager.setCameraRotate相机图像角度
        if (modelName.contains("FP08")) {
            cameraRotation = 0;
            manager.setCameraRotate(0);
        } else if (modelName.contains("X05")||modelName.contains("rk3568")) {
            cameraRotation = 180;
            manager.setCameraRotate(0);
        }else if(modelName.contains("FP07")){
            cameraRotation = 180;
            manager.setCameraRotate(180);
        }else if(modelName.contains("F68V")){
            cameraRotation = 0;
            manager.setCameraRotate(180);
        }else if(modelName.contains("FP09")){
            cameraRotation = 0;
            manager.setCameraRotate(0);
        }else if(modelName.contains("FP520")){
            cameraRotation = 0;
            manager.setCameraRotate(0);
        } else {
            cameraRotation = 0;
            manager.setCameraRotate(0);
        }
    }

    public int getSaveDegree(){
        int degrees = 0;
        String modelName = Build.MODEL.replace("-","");
        if(modelName.contains("rk3568")){
            degrees = 90;
        }
        if (modelName.contains("FP08")) {
            degrees = 0;
        } else if (modelName.contains("F68V")) {
            degrees = 0;
        } else if(modelName.contains("X05")){
            degrees = 180;
        }else if (modelName.contains("FP07")) {
            degrees = -180;
        } else if (modelName.contains("FP09")) {
            degrees = 0;
        } else if (modelName.contains("FP520")) {
            degrees = 0;
        }
        return degrees;
    }
}
