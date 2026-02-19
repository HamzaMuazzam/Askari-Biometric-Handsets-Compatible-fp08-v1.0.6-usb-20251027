package com.dawn.decoderapijni;

public interface ICamera {
    void cameraOpen(int cameraId, int width, int height);
    int cameraClose();
    void cameraStart();
    void cameraStop();
}
