package com.codec.res_path;

import android.os.Environment;

import java.io.File;

public class ResPath {
    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator;
    /**
     * 音频存放地址
     */
    private static final String AUDIO_PATH = PATH + "AUDIO_PATH" + File.separator;
    //录音 pcm数据
    public static final String RECORD_PCM = AUDIO_PATH + "audiopcm.pcm";
    //pcm 搞成可以播放的 .wav媒体文件
    public static final String RECORD_WAV = AUDIO_PATH + "audiowav.wav";


    /*
     *  视频存放地址
     */
    public static final String VIDEO_PATH = PATH + "CFAV_VIDEO" + File.separator;
    //FFmpeg 编码H264 范例
    public static final String ENCODE_H264_EXAMPLE = VIDEO_PATH + "enexample.h264";

    public static final String DECODE_H264_RES = VIDEO_PATH + "cut2.mp4";//要解码的多媒体文件

    public static final String Extractor_MP4_RES = VIDEO_PATH + "Cinematic.mp4";//要分解的多媒体文件
    public static final String Extractor_MP4_DST = VIDEO_PATH + "extractor.mp4";//要分解的多媒体文件
    public static final String Extractor_MP3_DST = VIDEO_PATH + "extractor.mp3";//要分解的多媒体文件
}
