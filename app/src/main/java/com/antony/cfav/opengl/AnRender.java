package com.antony.cfav.opengl;

import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AnRender implements GLSurfaceView.Renderer {

    //顶点坐标系(-1, -1) (1, -1)  (-1, 1)  (1, 1)
    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f

    };

    //纹理坐标系
    private float[] textureData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    public AnRender() {
        //为顶点坐标 分配本地内存地址
        //为什么要分配本地内存地址呢？ 因为Opengl取顶点的时候，每一次都到内存中去取值，
        // 所以这个内存在运行过程中是不允许被java虚拟机GC回收的，我们就要把它搞成本地（底层）不受虚拟机控制的 这种顶点
        vertexBuffer = ByteBuffer.allocateDirect(textureData.length * 4) //分配内存大小（分配了32个字节长度）
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
