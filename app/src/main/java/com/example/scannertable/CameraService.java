package com.example.scannertable;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.Arrays;

public class CameraService {
    //-----------------------------Initialize values------------------------------------------------
    private final CameraActivity cameraActivity;

    private final String mCameraID;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;
    private final File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "test1.jpg");
    public static Bitmap image = null;
    //----------------------------------------------------------------------------------------------


    public CameraService(CameraActivity cameraActivity, CameraManager cameraManager, String cameraID) {
        this.cameraActivity = cameraActivity;
        cameraActivity.mCameraManager = cameraManager;
        mCameraID = cameraID;

    }

    public void makePhoto() {


        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {


                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, cameraActivity.mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();


        }


    }


    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            cameraActivity.mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));


        }

    };


    private final CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            Log.i(CameraActivity.LOG_TAG, "Open camera  with id:" + mCameraDevice.getId());

            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraDevice.close();

            Log.i(CameraActivity.LOG_TAG, "disconnect camera  with id:" + mCameraDevice.getId());
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.i(CameraActivity.LOG_TAG, "error! camera id:" + camera.getId() + " error:" + error);
        }
    };


    private void createCameraPreviewSession() {

        mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

        SurfaceTexture texture = cameraActivity.textureView.getSurfaceTexture();

        texture.setDefaultBufferSize(1920, 1080);
        Surface surface = new Surface(texture);

        try {
            final CaptureRequest.Builder builder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            builder.addTarget(surface);


            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mCaptureSession = session;
                            try {
                                mCaptureSession.setRepeatingRequest(builder.build(), null, cameraActivity.mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                        }
                    }, cameraActivity.mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    public boolean isOpen() {
        return mCameraDevice != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void openCamera() {
        try {

            if (cameraActivity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {


                cameraActivity.mCameraManager.openCamera(mCameraID, mCameraCallback, cameraActivity.mBackgroundHandler);

            }


        } catch (CameraAccessException e) {
            Log.i(CameraActivity.LOG_TAG, e.getMessage());

        }
    }

    public void closeCamera() {

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


}