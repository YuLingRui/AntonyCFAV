package com.antony.cfav.opengl;


import android.view.Surface;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class MyEglHelper {
    private EGL10 mEgl;
    private EGLDisplay mEglDisplay;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;


    private void initEgl(Surface surface, EGLContext eglContext) {
        //1.得到Egl实例
        mEgl = (EGL10) EGLContext.getEGL();

        //2.得到默认的显示设备(就是窗口)
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("My  eglGetDisplay failed...");
        }

        //3.初始化默认显示设备
        int[] version = new int[2]; //主版本号，次版本号
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("My eglInitialize failed...");
        }

        //4.设置显示设备的属性
        int[] attribes = new int[]{
                EGL10.EGL_RED_SIZE, 8,  //红
                EGL10.EGL_GREEN_SIZE, 8, //绿
                EGL10.EGL_BLUE_SIZE, 8, //蓝
                EGL10.EGL_ALPHA_SIZE, 8, //透明度
                EGL10.EGL_DEPTH_SIZE, 8, //深度 3D相关的
                EGL10.EGL_STENCIL_SIZE, 8, //模板
                EGL10.EGL_RENDERABLE_TYPE, 4, //这个是安卓规定的 用opengl2.0这个版本
                EGL10.EGL_NONE //到这个NONE 他就知道结尾了
        };
        int[] num_config = new int[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, attribes, null, 1, num_config)) {
            throw new IllegalArgumentException("My eglChooseConfig failed...");
        }
        int numConfigs = num_config[0];
        if (numConfigs <= 0) {
            throw new IllegalArgumentException("My No configs match configSpec...");
        }
        //5.从系统中获取对应属性的配置
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!mEgl.eglChooseConfig(mEglDisplay, attribes, configs, numConfigs, num_config)) {
            throw new IllegalArgumentException("My eglChooseConfig#2 failed...");
        }
        //6. 创建EglContext
        if (eglContext != null) {
            mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], eglContext, null);
        } else {
            mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, null);
        }
        //7.创建渲染的Surface
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null);
        //8.绑定EglContext 和 Surface到显示设备中
        if(!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)){
            throw new IllegalArgumentException("My eglMakeCurrent failed...");
        }
        //9.刷新数据，显示渲染场景
    }

    /**
     * 刷新数据，显示渲染场景
     * @return
     */
    public boolean swapBuffers(){
        if (mEgl != null){
            return mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
        }else {
            throw new IllegalArgumentException("My eglSwapBuffers failed...");
        }
    }

    /**
     * 释放资源
     */
    public void destroyEgl(){
        if(mEgl != null){
            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEgl.eglDestroyContext(mEglDisplay, mEglContext);
            mEglContext = null;
            mEgl.eglDestroySurface(mEglDisplay,mEglSurface);
            mEglSurface = null;
            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
            mEgl = null;
        }
    }

    public EGLContext getmEglContext() {
        return mEglContext;
    }
}
