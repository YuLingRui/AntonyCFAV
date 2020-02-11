package com.codec.ffcodec;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.codec.interfaca.ICamera;
import com.codec.view.AutoFitTextureView;

import java.io.IOException;

import io.reactivex.schedulers.Schedulers;


/**
 * Camera Version1 预览与实时编码
 */
public class CameraV1 implements ICamera, SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraV1";

    private SurfaceView mSurfaceView;

    /**
     * 相机实例
     */
    private Camera mCamera;

    /**
     * 默认使用前置摄像头
     */
    private int mCameraId;

    private NativeEncoder mNativeFrame;
    /**
     * 相机预览数据输出
     *
     * @param data
     * @param camera
     */
    Camera.Size mPreviewSize;

    public void setPreviewView(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
        this.mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        this.mNativeFrame = new NativeEncoder();
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        this.mPreviewSize = camera.getParameters().getPreviewSize();
        if (mNativeFrame != null) {
            mNativeFrame.onPreviewFrame(data, mPreviewSize.width, mPreviewSize.height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            setCameraDisplayOrientation(mCameraId, mCamera);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "surfaceChanged IOException");
        }
        Schedulers.newThread().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                mNativeFrame.onPreviewFrame(buffer, 240, 480);
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void setPreviewView(AutoFitTextureView surfaceView) {

    }

    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void encodeJPEG(String jpegPath) {
        if (mNativeFrame != null && mPreviewSize != null) {
            mNativeFrame.encodeJPEG(jpegPath, mPreviewSize.width, mPreviewSize.height);
        }
    }

    @Override
    public void encodeStart(String outputPath) {
        if (mNativeFrame != null && mPreviewSize != null) {
            mNativeFrame.encodeMP4Start(outputPath, mPreviewSize.width, mPreviewSize.height);
        }
    }

    @Override
    public void encodeStop() {
        if (mNativeFrame != null) {
            mNativeFrame.encodeMP4Stop();
        }
    }


    private Camera openCamera() {
        Camera camera;
        try {
            camera = Camera.open(mCameraId);
        } catch (Exception e) {
            camera = null;
        }
        return camera;
    }

    /**
     * 前置摄像头要转换270度
     * 后置摄像头转换90度
     *
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Activity targetActivity = (Activity) mSurfaceView.getContext();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = targetActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayDegree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }
}
