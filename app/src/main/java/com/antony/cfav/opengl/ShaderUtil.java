package com.antony.cfav.opengl;

import android.content.Context;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 着色器Shader工具类
 */
public class ShaderUtil {

    /**
     * 获取 raw 文件下的 .glsl
     *
     * @param context 全局环境
     * @param resId   raw id
     * @return
     */
    public static String getRawResource(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 创建并加载Shader(着色器)
     *
     * @param shaderType shader类型
     * @param source     .glsl【以字符串的形式加载进入】
     * @return 0 失败   >0 成功
     */
    public static int loadShader(int shaderType, String source) {
        // OpenGL 1.创建Shader(着色器：顶点着色器 或  片元着色器)
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            // OpenGL 2.加载shader.glsl{这里吧.glsl的文件以String字符串的方式加载}   并编译shader
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            // OpenGL 3.检查是否编译成功
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] != GLES20.GL_TRUE) {
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        } else {
            return 0;
        }
    }

    /**
     * 创建 并 链接 源程序 program
     * @param vertex 顶点
     * @param fragment 纹理
     * @return 0失败  >0成功
     */
    public static int createProgram(String vertex, String fragment) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        if (vertexShader != 0 && fragmentShader != 0) {
            // OpenGL 4.创建一个渲染程序(源程序program)
            int prgram = GLES20.glCreateProgram();
            // OpenGL 5.将着色器程序添加到渲染程序中
            GLES20.glAttachShader(prgram, vertexShader);
            GLES20.glAttachShader(prgram, fragmentShader);
            // OpenGL 6.链接源程序
            GLES20.glLinkProgram(prgram);
            return prgram;
        } else {
            return 0;
        }
    }
}
