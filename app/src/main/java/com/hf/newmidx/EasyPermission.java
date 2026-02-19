package com.hf.newmidx;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * @author tx
 * @date 2023/5/30 9:02
 * @target 简单的封装权限请求
 */
public class EasyPermission {

    public interface IPermissionResult {
        void result(boolean allGranted);
    }

    Activity context;

    public EasyPermission(Activity context) {
        this.context = context;
    }

    public void cameraAndRecord(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void camera(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.CAMERA)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void mic(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void location(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void storage(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.READ_MEDIA_AUDIO)
                .permission(Permission.READ_MEDIA_VIDEO)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void storageAndCameraPhoneState(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.CAMERA)
                .permission(Permission.READ_PHONE_STATE)
                .permission(Permission.READ_MEDIA_AUDIO)
                .permission(Permission.READ_MEDIA_VIDEO)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void alertWindow(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public boolean manage_storage(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean manage = Environment.isExternalStorageManager();
            if (!manage) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivity(intent);
                }
                return false;
            }else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void phone(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.READ_PHONE_STATE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    public void notifications(IPermissionResult iPermissionResult) {
        XXPermissions.with(context)
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }
                });
    }

    //需要使用XXPermissions进行动态权限申请的有:
    //- BLUETOOTH:蓝牙权限,用于连接蓝牙设备。
    //- BLUETOOTH_ADMIN:蓝牙管理权限,用于发现和配对蓝牙设备。
    //- BLUETOOTH_PRIVILEGED:蓝牙特权权限,用于敏感蓝牙操作如配对管理和名称修改。这3个权限涉及到连接、配对和管理蓝牙设备,比较敏感,需要进行动态权限申请。另外3个权限:- BLUETOOTH_ADVERTISE:广播蓝牙权限,用于广播蓝牙信号。
    //- BLUETOOTH_CONNECT:连接蓝牙设备权限,这是一个过时权限,现使用BLUETOOTH权限。
    //- BLUETOOTH_SCAN:蓝牙扫描权限,用于扫描附近蓝牙设备。
    public void bluetooth(IPermissionResult iPermissionResult) {
        String[] requestPermissionArray = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionArray = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                return;
            }
            requestPermissionArray = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        XXPermissions.with(context)
                .permission(requestPermissionArray)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                        iPermissionResult.result(allGranted);
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean doNotAskAgain) {

                    }
                });
    }
}
