package com.dawn.java.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageShowActivity extends DawnActivity {
    private TopToolbar topBar;
    private ImageView imageView;
    private Bitmap bitmap;
    private byte[] bmpBytes = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_image_show;
    }

    private void initView() {
        topBar = findViewById(R.id.topBar);
        imageView = findViewById(R.id.iv_showLastImage);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {
//                saveImage(bitmap);
                saveBmp();
            }
        });
    }

    public void initData() {
        showImage();
    }
    public void showImage() {
        bmpBytes = ServiceTools.getInstance().getImageLast();
        if (bmpBytes == null) {
            return;
        }

        bitmap = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length);
        if (bitmap == null) {
            Log.d(TAG, "bitmap is null ....");
            return;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(bitmap);
    }

    private void saveBmp()
    {
        if (bmpBytes == null) {
            return;
        }

        String dirPath="/sdcard/newland/saveImages";
        isDirPathExist(dirPath);

        //文件名为时间
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String sd = sdf.format(new Date(timeStamp));
        String fileName = sd + ".bmp";

        File f = new File(dirPath, fileName);
        if (f.exists()) {
            f.delete();
        }

        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f)) ;
            out.write(bmpBytes);
            out.close();
            Toast.makeText(getApplicationContext(), R.string.common_save_success, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "save success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImage(Bitmap bitmap) {
        String dirPath="/sdcard/newland/saveImages";
        isDirPathExist(dirPath);

        //文件名为时间
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String sd = sdf.format(new Date(timeStamp));
        String fileName = sd + ".jpg";

        File f = new File(dirPath, fileName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(getApplicationContext(), R.string.common_save_success, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "save success");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void isDirPathExist(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
