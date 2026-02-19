package com.eyecool.iris;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class IrisMainActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSIONS_CODE = 11;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        findViewById(R.id.startIrisBtn).setOnClickListener(v -> startIris());
        findViewById(R.id.loopTestBtn).setOnClickListener(v -> loopTest());
        findViewById(R.id.colorCardDetectBtn).setOnClickListener(v -> colorCardDetect());
        findViewById(R.id.startBinocularIrisBtn).setOnClickListener(v -> startBinocularIris());
        toolbar = (Toolbar)this.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IrisMainActivity.this.finish();
            }
        });
        requestPermissions();
    }

    private void startIris() {
        //SoundPlayUtils.loadAndPlay(R.raw.no_iris_please_enroll,1,0);
       startActivity(new Intent(this, IrisActivity.class));
    }

    private void loopTest() {
        //SoundPlayUtils.loadAndPlay(R.raw.verification_succeeded,1,0);
        //FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/iris_template.txt", template);
        startActivity(new Intent(this, LoopTestActivity.class));
    }

    private void colorCardDetect() {
        startActivity(new Intent(this, ColorCardDetectActivity.class));
    }
	 private void startBinocularIris() {
        startActivity(new Intent(this, IrisBinocularActivity.class));
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = null;
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions = new ArrayList<>();
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (permissions == null) {
                    permissions = new ArrayList<>();
                }
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions == null) {
            } else {
                String[] permissionArray = new String[permissions.size()];
                permissions.toArray(permissionArray);
                // Request the permission. The result will be received
                // in onRequestPermissionResult()
                requestPermissions(permissionArray, REQUEST_PERMISSIONS_CODE);
            }
        } else {
        }
    }


}