package com.antony.cfav.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import com.antony.cfav.R;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AnRender implements GLSurfaceView.Renderer {

    private Context mContext;
    //顶点坐标系(-1, -1) (1, -1)  (-1, 1)  (1, 1)
    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f

    };

    //纹理坐标系
    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer vertexBuffer; //顶点buffer
    private FloatBuffer fragmentBuffer; //纹理buffer
    private int program; //源程序
    private int vPosition; //顶点位置
    private int fPosition; //纹理位置
    private int textureId; //纹理的ID
    private int sampler;

    public AnRender(Context context) {
        this.mContext = context;
        //为顶点坐标 分配本地内存地址
        //为什么要分配本地内存地址呢？ 因为Opengl取顶点的时候，每一次都到内存中去取值，
        // 所以这个内存在运行过程中是不允许被java虚拟机GC回收的，我们就要把它搞成本地（底层）不受虚拟机控制的这种顶点
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4) //分配内存大小（分配了32个字节长度）
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);
    }

    private void initRender() {
        String vertexSource = ShaderUtil.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtil.getRawResource(mContext, R.raw.fragment_shader);
        program = ShaderUtil.createProgram(vertexSource, fragmentSource); //创建源程序 program
        //7.得到着色器中的属性  todo：我们就从源程序中获取他的属性了
        vPosition = GLES20.glGetAttribLocation(program, "av_Position"); //顶点的向量坐标 todo:一定要跟vertex_shader.glsl中的变量对应上
        fPosition = GLES20.glGetAttribLocation(program, "af_Position"); //纹理的向量坐标
        sampler = GLES20.glGetUniformLocation(program, "sTexture"); // sampler2D

        //a.创建纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        //b.绑定纹理
        textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //c.激活纹理 (激活texture0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler, 0);
        //d.设置纹理 环绕和过滤方式
        //todo: 环绕(超出纹理坐标范围)：(s==x  t==y GL_REPEAT重复)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //todo: 过滤(纹理像素映射到坐标点)：(缩小，放大：GL_LINEAR线性)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //e.把bitmap这张图片映射到Opengl上
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.androids);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//这里 textre=0 相当于解绑了
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //TODO: OpenGL es 加载Shader
        initRender();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); //这是清屏
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);//红色清屏
        //8.使用源程序
        GLES20.glUseProgram(program);
        //9.绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //10.使顶点属性数组有效,  使纹理属性数组有效
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glEnableVertexAttribArray(fPosition);
        //11.为顶点属性赋值 todo；就是把 vertexBuffer的数据给到  vPosition
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        //为片元属性赋值    todo:  把textureBuffer的数据给到 fPosition
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);
        //todo； 到这里 vertex_shader.glsl中的  "av_Position"， "af_Position"就有数据了
        //12.绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//这里 textre=0 相当于解绑了
    }
}
