package com.antony.cfav.camera2_opengl.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.antony.cfav.camera2_opengl.util.OpenGlUtils;

public class AbsFboFilter extends BaseFilter {

    protected int[] mFrameBuffers;
    protected int[] mFBOTextures;

    public AbsFboFilter(Context context, int mVertexShaderId, int mFragShaderId) {
        super(context, mVertexShaderId, mFragShaderId);
    }

    @Override
    public void prepare(int width, int height, int x, int y) {
        super.prepare(width, height, x, y);
        loadFbo();
    }


    @Override
    protected void resetCoordinate() {
        super.resetCoordinate();
        mGlTextureBuffer.clear();
        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        mGlTextureBuffer.put(TEXTURE);
    }

    public void loadFbo() {
        if(mGlTextureBuffer != null){
            destroyFrameBuffers();
        }

        //创建一个FBO 的纹理
        mFBOTextures = new int[1];
        OpenGlUtils.glGenTextures(mFBOTextures);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextures[0]);

        //创建FrameBuffer
        mFrameBuffers = new int[1];
        GLES20.glGenFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //指定FBO纹理的输出图像的格式 RGBA
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mOutputWidth, mOutputHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //将FBO 绑定到 2D的纹理上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFBOTextures[0], 0);
        //检查是否绑定成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("AbsFboFilter", "fbo wrong");
        } else {
            Log.e("AbsFboFilter", "fbo success");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }


    public void destroyFrameBuffers() {
        //删除fbo的纹理
        if (mFBOTextures != null) {
            GLES20.glDeleteTextures(1, mFBOTextures, 0);
            mFBOTextures = null;
        }
        //删除fbo
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }
}
