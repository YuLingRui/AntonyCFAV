package com.antony.cfav.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.antony.cfav.R;
import com.antony.cfav.dialog.ConfirmationDialog;
import com.antony.cfav.dialog.ErrorDialog;
import com.antony.cfav.view.AutoFitTextureView;

/**
 * Camera2 使用范例
 */
public class Camera2Fragment extends Fragment {

    /**
     * Conversion from screen rotation to JPEG orientation.
     * 从屏幕旋转到JPEG方向的转换。
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * 相机状态:显示相机预览。
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * 相机状态:等待对焦锁定。
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * 相机状态:等待曝光为预捕获状态。
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * 相机状态:等待曝光状态不是预捕获。
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * 相机状态:拍照。
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * 由Camera2 API保证的最大预览宽度
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Camera2 API保证的最大预览高度
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    //当前的{@link CameraDevice}的ID。
    private String mCameraId;
    //用于相机预览的{@link AutoFitTextureView}。TODO:自定义TextureView
    private AutoFitTextureView mTextureView;
    //用于相机预览的{@link CameraCaptureSession}。
    private CameraCaptureSession mCaptureSession;
    //对打开的{@link CameraDevice}的引用。
    private CameraDevice mCameraDevice;
    //{@link android.util。相机预览的尺寸}。
    private Size mPreviewSize;

    public Camera2Fragment() {
        // Required empty public constructor
    }

    public static Camera2Fragment newInstance() {
        return new Camera2Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera2, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            //configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    //打开{@link #mCameraId}指定的摄像头。
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //TODO: 过滤Camera权限
            requestCameraPermission();
            return;
        }
    }

    //过滤摄像机权限
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    //权限反馈处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    ErrorDialog.newInstance(getString(R.string.request_permission))
                            .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
