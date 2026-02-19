package com.dawn.java.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.math.BigDecimal;

public class CacheUtil {
    /**
     * Context.getExternalCacheDir() --> SDCard/Android/data/<application package>/cache/目录，一般存放临时缓存数据
     * Context.getCacheDir() -->  /data/data/<application package>/cache
     */


    /**
     * 获取可以使用的缓存目录路径
     *
     * @param context
     * @return String
     */
    public static String getDiskCacheDirPath(Context context) {
        String cachePath = null;
        if (checkSDCard()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * 获取可以使用的缓存目录
     *
     * @param context
     * @return File
     */
    public static File getDiskCacheDir(Context context) {
        File cacheDirFile = new File(getDiskCacheDirPath(context));
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }

        return cacheDirFile;
    }

    /**
     * 获取当前缓存大小
     */
    public static String getTotalCacheSize(Context context) throws Exception {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return getFormatSize(cacheSize);
    }

    /**
     *  删除缓存
     */
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    /**
     *  删除文件夹
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int size = 0;
            if (children != null) {
                size = children.length;
                for (int i = 0; i < size; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        if (dir == null) {
            return true;
        } else {

            return dir.delete();
        }
    }

    /**
     * 获取文件夹大小
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 检查SD卡是否存在
     *
     * @return
     */
    public static boolean checkSDCard() {
        final String status = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(status)) {
            return true;
        }
        return false;
    }

    /**
     * 格式化单位
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}
