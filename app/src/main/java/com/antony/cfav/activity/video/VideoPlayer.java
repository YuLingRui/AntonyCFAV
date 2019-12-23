package com.antony.cfav.activity.video;

public class VideoPlayer {

    static {
        System.loadLibrary("media-handle");
    }

    //使用FFmpeg抽取mp4中的视频h264数据
    public native void extractVideoH264(String src, String dst);
    //使用FFmpeg将 .mp4格式 转成  .flv格式
    public native void formatConversion(String src, String dst);
}
