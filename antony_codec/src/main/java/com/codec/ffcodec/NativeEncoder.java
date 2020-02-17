package com.codec.ffcodec;

/**
 * 通过Camera v1 预览 使用FFmpeg编码
 */
public class NativeEncoder {

    static {
        System.loadLibrary("antony-lib");
    }

    public native void encodeMP4Start(String mp4Path, int width, int height);

    public native void encodeMP4Stop();

    public native void onPreviewFrame(byte[] yuvData, int width, int height);

    public native void encodeJPEG(String jpegPath, int width, int height);

}
