package com.codec.ffcodec;

/**
 * 模拟 FFmpeg 编码 H264
 */
public class FFcodecNative {
    static {
        System.loadLibrary("antony-lib");
    }
    /**
     * 进入子线程调用 native方法进行  编解码
     *
     * @param commands ffmpeg命令行执行语句
     * @param listener 执行结果监听
     */
    public static void executeEncode(final String[] commands, final CodecHandleListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onBegin();
                }
                int result = encode_example(commands[0], commands[1]);
                if (listener != null) {
                    listener.onEnd(result);
                }
            }
        }).start();
    }

    /**
     * 进入子线程调用 native方法进行 解码
     *
     * @param commands ffmpeg命令行执行语句
     * @param listener 执行结果监听
     */
    public static void executeDecode(final String[] commands, final CodecHandleListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onBegin();
                }
                int result = decode_example(commands[0], commands[1]);
                if (listener != null) {
                    listener.onEnd(result);
                }
            }
        }).start();
    }

    /**
     * 编码示例
     *
     * @param enPath    编码输出路径
     * @param codecName 编码名称 【h264】
     * @return 0  1
     */
    public static native int encode_example(String enPath, String codecName);

    /**
     * 解码示例
     * @param src 多媒体视频地址
     * @param img 转换成图片的地址
     * @return
     */
    public static native int decode_example(String src, String img);
}
