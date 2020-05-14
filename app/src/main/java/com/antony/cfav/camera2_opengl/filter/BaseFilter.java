package com.antony.cfav.camera2_opengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.antony.cfav.camera2_opengl.util.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class BaseFilter {

    private Context mContext;

    private int mVertexShaderId;
    private int mFragShaderId;
    private String mVertexShader;
    private String mFragShader;

    protected int mProgramId;
    protected int vTexture;    //属性：uniform samplerExternalOES vTexture
    protected int vMatrix;     //属性：uniform mat4 vMatrix
    protected int vPosition;   //属性：attribute vec4 vPosition
    protected int vCoord;      //属性：attribute vec4 vCoord

    protected int mOutputHeight;
    protected int mOutputWidth;
    protected int y;
    protected int x;

    protected final FloatBuffer mGlVertexBuffer;
    protected final FloatBuffer mGlTextureBuffer;


    public BaseFilter(Context context, int mVertexShaderId, int mFragShaderId) {
        this.mContext = context;
        this.mVertexShaderId = mVertexShaderId;
        this.mFragShaderId = mFragShaderId;

        //顶点坐标
        float[] VERTEXT = {
                -1.0f, 1.0f,
                1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f
        };
        mGlVertexBuffer = ByteBuffer.allocateDirect(VERTEXT.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEXT);
        mGlVertexBuffer.position(0);

        //纹理坐标
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };
        mGlTextureBuffer = ByteBuffer.allocateDirect(TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE);
        mGlTextureBuffer.position(0);
        initBase();
    }

    private void initBase() {
        mVertexShader = OpenGlUtils.readRawShaderFile(mContext, mVertexShaderId);
        mFragShader = OpenGlUtils.readRawShaderFile(mContext, mFragShaderId);
        //创建着色器程序
        mProgramId = OpenGlUtils.loadProgram(mVertexShader, mFragShader);
        //获取着色器变量，需要赋值
        vPosition = GLES20.glGetAttribLocation(mProgramId, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgramId, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgramId, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(mProgramId, "vTexture");
    }

    public void prepare(int width, int height, int x, int y) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
        this.x = x;
        this.y = y;
    }


    public int onDrawFrame(int textureid) {
        GLES20.glViewport(x, y, mOutputWidth, mOutputHeight);

        GLES20.glUseProgram(mProgramId);

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 8, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return textureid;
    }

    public void release() {
        GLES20.glDeleteProgram(mProgramId);
    }

    protected void resetCoordinate() {

    }
}
