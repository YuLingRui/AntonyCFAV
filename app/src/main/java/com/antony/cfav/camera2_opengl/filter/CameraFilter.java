package com.antony.cfav.camera2_opengl.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.antony.cfav.R;

public class CameraFilter extends AbsFboFilter {

    protected float[] matrix;

    public CameraFilter(Context context, int mVertexShaderId, int mFragShaderId) {
        super(context, R.raw.camera_vertex, R.raw.camera_frag);
    }


    @Override
    public int onDrawFrame(int textureId) {
        //锁定绘制的区域  绘制是从左下角开始的
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        //绑定FBO，在FBO上操作
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //使用着色器
        GLES20.glUseProgram(mProgramId);

        //赋值vPosition
        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //赋值vCoord
        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //赋值vMatrix
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        //激活纹理 texture0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //SurfaceTexture 对应 GL_TEXTURE_EXTERNAL_OES 类型 【绑定纹理】
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        //赋值vTexture
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        return mFBOTextures[0];
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }
}
