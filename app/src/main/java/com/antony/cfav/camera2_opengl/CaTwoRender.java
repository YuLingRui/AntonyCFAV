package com.antony.cfav.camera2_opengl;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.antony.cfav.R;
import com.antony.cfav.camera2_opengl.filter.CameraFilter;
import com.antony.cfav.camera2_opengl.filter.ScreenFilter;
import com.antony.cfav.camera2_opengl.util.Camera2Helper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CaTwoRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener,Camera2Helper.OnPreviewSizeListener, Camera2Helper.OnPreviewListener{

    private Context context;
    private CaTwoSurfaceView glSurfaceView;

    private int[] mTextures;

    private SurfaceTexture mSurfaceTexture;
    private Camera2Helper camera2Helper;

    private CameraFilter cameraFilter;
    private ScreenFilter screenFilter;

    private int mPreviewWdith;
    private int mPreviewHeight;
    private int screenSurfaceWid;
    private int screenSurfaceHeight;
    private int screenX;
    private int screenY;
    private float[] mtx = new float[16];



    public CaTwoRender(Context context, CaTwoSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        camera2Helper = new Camera2Helper((Activity) context);

        mTextures = new int[1];
        //创建一个纹理
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        //将纹理和离屏buffer绑定
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        //监听有新的图像到来
        mSurfaceTexture.setOnFrameAvailableListener(this);

        cameraFilter = new CameraFilter(context, R.raw.camera_vertex, R.raw.camera_frag);
        screenFilter = new ScreenFilter(context, R.raw.screen_vert, R.raw.screen_frag);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        camera2Helper.setPreviewSizeListener(this);
        camera2Helper.setOnPreviewListener(this);
        //打开相机
        camera2Helper.openCamera(width, height, mSurfaceTexture);

        float scaleX = (float) mPreviewHeight / (float) width;
        float scaleY = (float) mPreviewWdith / (float) height;

        float max = Math.max(scaleX, scaleY);

        screenSurfaceWid = (int) (mPreviewHeight / max);
        screenSurfaceHeight = (int) (mPreviewWdith / max);
        screenX = width - (int) (mPreviewHeight / max);
        screenY = height - (int) (mPreviewWdith / max);

        //prepare 传如 绘制到屏幕上的宽 高 起始点的X坐标 起使点的Y坐标
        cameraFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);
        screenFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        int textureId;
        // 配置屏幕
        //清理屏幕 :告诉opengl 需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //更新获取一张图
        mSurfaceTexture.updateTexImage();

        mSurfaceTexture.getTransformMatrix(mtx);
        //cameraFiler需要一个矩阵，是Surface和我们手机屏幕的一个坐标之间的关系
        cameraFilter.setMatrix(mtx);

        textureId = cameraFilter.onDrawFrame(mTextures[0]);

        int id = screenFilter.onDrawFrame(textureId);

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }

    @Override
    public void onSize(int width, int height) {
        mPreviewWdith = width;
        mPreviewHeight = height;
        Log.e("AAA", "mPreviewWdith:" + mPreviewWdith);
        Log.e("AAA", "mPreviewHeight:" + mPreviewHeight);
    }

    @Override
    public void onPreviewFrame(byte[] data, int len) {

    }
}
