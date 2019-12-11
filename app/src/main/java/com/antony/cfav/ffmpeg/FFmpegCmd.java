package com.antony.cfav.ffmpeg;

import com.antony.cfav.listener.OnHandleListener;

public class FFmpegCmd {

    static {
        System.loadLibrary("media-handle");
    }

    //进入子线程调用 native方法进行音视频处理
    /**
     * @param commands ffmpeg命令行执行语句
     * @param listener 执行结果监听
     */
    public static void execute(final String[] commands, final OnHandleListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onBegin();
                }
                int result = handle(commands);
                if (listener != null) {
                    listener.onEnd(result);
                }
            }
        }).start();
    }

    /**
     * 调用 ffmpeg.c 的 run() 方法执行 “命令行语句”
     * @param commands 命令行语句
     * @return
     */
    public static native int handle(String[] commands);
}
