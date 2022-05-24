package com.example.scannertable;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;


public class CameraActivity extends AppCompatActivity {
    //-----------------------------Initialize values------------------------------------------------
    public static final String LOG_TAG = "Test";

    public TextureView textureView;

    public CameraService myCamera = null;
    public CameraManager mCameraManager = null;
    public HandlerThread mBackgroundThread;
    public Handler mBackgroundHandler = null;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private String cameraID;

    public static int angle;
    //----------------------------------------------------------------------------------------------


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button btConfirm = findViewById(R.id.btConfirm);
        textureView = findViewById(R.id.textureView);

        btConfirm.setOnClickListener(view -> {
            if (myCamera.isOpen()) {
                myCamera.closeCamera();
            }
            CameraService.image = textureView.getBitmap();
            ActivityAdd.WPhoto = true;
            try {
                angle = getRotationCompensation();
                Log.i(LOG_TAG, "Angle: " + angle);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            stopBackgroundThread();
            finish();
        });

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Получение списка камер с устройства

            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: " + cameraID);
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraID);
                //  Определение какая камера куда смотрит
                int faceing = cc.get(CameraCharacteristics.LENS_FACING);
                // создаем обработчик для камеры
                if (faceing == CameraCharacteristics.LENS_FACING_BACK) {
                    this.cameraID = cameraID;
                    myCamera = new CameraService(this, mCameraManager, cameraID);
                    Log.i(LOG_TAG, "Camera with: ID " + cameraID + " is BACK CAMERA  ");
                }


            }
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
        if (myCamera != null) {
            myCamera.openCamera();
        }
        startBackgroundThread();

    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    private int getRotationCompensation()
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) this.getBaseContext().getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraID)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("R", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

}
