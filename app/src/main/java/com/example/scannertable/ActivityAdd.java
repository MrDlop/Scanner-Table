package com.example.scannertable;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class ActivityAdd extends AppCompatActivity {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    public static boolean WPhoto = false;
    public ImageView imageView = null;
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 9993:
                finish();
                break;
            case 9994:
                if (CameraService.image != null) {
                    imageView.setImageBitmap(CameraService.image);
                }break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Button btOpenCamera = findViewById(R.id.btOpenCamera);
        Button btOK = findViewById(R.id.btAddOkOne);
        imageView = findViewById(R.id.imageView);

        btOpenCamera.setOnClickListener(view -> {
            if (hasCameraPermission()) {
                enableCamera();
            } else {
                requestPermission();
            }
        }); // Ok
        btOK.setOnClickListener(view -> {
            if (WPhoto) {
                enableRecognize();
            } else {
                Toast.makeText(ActivityAdd.this, "Photo is None", Toast.LENGTH_SHORT).show();
            }
        }); // Ok

    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 9994);
    }

    private void enableRecognize() {
        int[] arr = new int[2];
        int[][] arrr = new int[1][4];
        arr[0] = 210;
        arr[1] = 297;
        arrr[0][0] = 0;
        arrr[0][1] = 0;
        arrr[0][2] = 100;
        arrr[0][3] = 100;
        String[] ars = new String[1];
        ars[0] = "P";
        MainActivity.templateJSON = new TemplateJSON(1, arr, arrr, ars);
        Intent intent = new Intent(this, RecognizeActivity.class);
        startActivityForResult(intent, 9993);

    }
}