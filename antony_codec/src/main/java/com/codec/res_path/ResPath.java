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
}
