package com.antony.codec.encode;

/**
 * @auther antony
 * @data 2020/1/13 21 : 56
 * @function info
 */

public class NativeEncoder {
    static {
        System.loadLibrary("native-encode");
    }

    public native void encodeMP4Start(String mp4Path, int width, int height);

    public native void encodeMP4Stop();

    public native void onPreviewFrame(byte[] yuvData, int width, int height);

    public native void encodeJPEG(String jpegPath, int width, int height);

}
