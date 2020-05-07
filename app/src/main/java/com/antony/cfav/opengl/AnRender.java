package com.antony.cfav.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.antony.cfav.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL 数字: 【使用Opengl的大体步骤】
 * t 数字:  【使用纹理的大体步骤】
 * v 数字: 【使用VBO的大体步骤】
 * f 数字: 【使用FBO的大体步骤】
 * m 数字: 【使用正交投影矩阵大体步骤】
 */
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
//            0f, 0f,
//            1f, 0f,
//            0f, 1f,
//            1f, 1f
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer vertexBuffer; //顶点buffer
    private FloatBuffer fragmentBuffer; //纹理buffer

    private int program; //渲染源程序
    private int vPosition; //顶点位置
    private int fPosition; //纹理位置
    private int textureId; //纹理的ID 【FBO会跟它进行绑定】
    private int sampler; //纹理采样

    private int vboId;// 保存VBO
    private int fboId;// 保存FBO

    private int uMatrix; //矩阵
    private float[] matrix = new float[16]; //正交投影使用


    private int imgTextureId; //纹理的ID，这个是我们要渲染的纹理【一张图片】
    private FboRender fboRender;

    AnRender(Context context) {
        this.mContext = context;
        fboRender = new FboRender(context);
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


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        fboRender.onFboCreate(); //离屏渲染的Render(自定义的)
        //以字符串的形式 加载出 顶点shader 和 纹理shader
        String vertexSource = ShaderUtil.getRawResource(mContext, R.raw.vertex_shader_matrix);
        String fragmentSource = ShaderUtil.getRawResource(mContext, R.raw.fragment_shader);
        //创建源程序
        program = ShaderUtil.createProgram(vertexSource, fragmentSource);

        // OpenGL 7.得到着色器中的属性  todo：我们就从源程序中获取他的属性了
        vPosition = GLES20.glGetAttribLocation(program, "av_Position"); //顶点的向量坐标 todo:一定要跟vertex_shader.glsl中的变量对应上
        fPosition = GLES20.glGetAttribLocation(program, "af_Position"); //纹理的向量坐标
        sampler = GLES20.glGetUniformLocation(program, "sTexture"); // uniform sampler2D sTexture
        uMatrix = GLES20.glGetUniformLocation(program, "u_Matrix"); // uniform mat4 u_Matrix

        //v1. 创建VBO 【Vertex Buffer Object 顶点缓存类】
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        //v2. 绑定VBO
        vboId = vbos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //v3. 分配VBO需要的缓存大小:顶点坐标数据长度 + 纹理坐标数据长度【data=null 表示只分配了空间，并没有放入数据】
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //v4. 为VBO设置坐标点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);//坐标顶点赋值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);//纹理坐标点赋值
        //v5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //TODO: >>>>>> 到这里 就把坐标系中的本地内存数据 缓存到了  显存中【GPU中】 <<<<<<<


        //t1. 创建纹理
        int[] textureIds = new int[1];
        GLES20.glGenBuffers(1, textureIds, 0);
        //t2. 绑定纹理
        textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //t3. 激活纹理 【激活纹理texture0】
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler, 0);
        //t4 .设置纹理 环绕和过滤方式
        //todo: 环绕(超出纹理坐标范围)：(s==x  t==y GL_REPEAT重复)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //todo: 过滤(纹理像素映射到坐标点)：(缩小，放大：GL_LINEAR线性)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //使用GLUtils.texImage2D 把bitmap这张图片映射到Opengl上
        //Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.androids);
        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        //bitmap.recycle();

        //f1. 创建FBO 【Frame Buffer Object 纹理缓存类】
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        //f2. 绑定FBO
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        //f3. 为FBO分配需要的内存大小 (1080 1920 是我手机的宽高，没有ActionBar)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1080, 1920, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //f4.把纹理绑定到FBO上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        //f5. 检查FBO绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("AnRender", "FBO 绑定 纹理失败！ ");
        } else {
            Log.i("AnRender", "FBO 绑定 纹理成功！ ");
        }

        //t5. 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //f6. 解绑FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //这里我们创建一个要绘制的纹理ID
        imgTextureId = createImgTexrute(R.drawable.androids);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Log.e("AnRender", "width: " + width + "     height:" + height);
        fboRender.onFboChange(width, height);
        if (width > height) { //横屏
            // m1. 计算正投影矩阵 (702 , 526 是该图片的高 宽)
            Matrix.orthoM(matrix, 0, -width / ((height / 702f * 526f)), width / ((height / 702f * 526f)), -1f, 1f, -1f, 1f);
        } else { //竖屏
            // m2. 计算正投影矩阵
            Matrix.orthoM(matrix, 0, -1, 1, -height / ((width / 526f * 702f)), height / ((width / 526f * 702f)), -1f, 1f);
        }
        //矩阵旋转(如果不旋转的话，我们的图片是倒立的)
        Matrix.rotateM(matrix, 0, 180, 1, 0, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); //清屏
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);//红色清屏【这样背景就是红色的了】
        //OpenGL 8.使用源程序
        GLES20.glUseProgram(program);
        // m3.使用正交投影
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);
        //OpenGL 9.绑定纹理， 绑定VBO, 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);//绑定FBO
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId); //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);//绑定VBO


        //OpenGL 10.使顶点属性数组有效, 使纹理属性数组有效
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glEnableVertexAttribArray(fPosition);

        // OpenGL 11.为顶点坐标属性赋值 todo；就是把 vertexBuffer的数据给到  vPosition
        //GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);//vPosition中的数据就是本地内存中的数据了
        //为片元坐标属性赋值  todo:  把textureBuffer的数据给到 fPosition
        //GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);//fPosition中的数据就是本地内存中的数据了
        //todo； 到这里 vertex_shader.glsl中的  "av_Position"， "af_Position"就有数据了
        // OpenGL[11步]. 在这一步，我们就可以使用生成好的VBO 【显存中的缓存值了】
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0); //vPosition中的数据就是显存中的数据了
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);//fPosition中的数据就是显存中的数据了

        // OpenGL 12.绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // OpenGL 13.解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//这里 textre=0 解绑纹理
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);//这里 buffer=0 解绑VBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);//这里 buffer=0 解绑FBO

        //去离屏渲染
        fboRender.onFboDraw(textureId);
    }


    private int createImgTexrute(int src) {
        int[] imgTextureIds = new int[1];
        GLES20.glGenTextures(1, imgTextureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //使用GLUtils.texImage2D 把bitmap这张图片映射到Opengl上
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), src);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return imgTextureIds[0];
    }
}
