package com.dawn.java.util;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    /**
     * Context.getExternalFilesDir() --> SDCard/Android/data/<application package>/files/ 目录，一般放一些长时间保存的数据
     */

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
     * 保存文件到sd
     *
     * @param filename
     * @param content
     * @return
     */
    public static boolean saveContentToSdcard(String filename, String content) {
        boolean flag = false;
        FileOutputStream fos = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            if (checkSDCard()) {
                fos = new FileOutputStream(file);
                fos.write(content.getBytes());
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
        return flag;
    }


    /**
     * 创建文件夹
     *
     * @param DirPath 文件夹路径
     */
    public static void mkdir(String DirPath) {
        File file = new File(DirPath);
        if (!(file.exists() && file.isDirectory())) {
            file.mkdirs();
        }
    }


}
