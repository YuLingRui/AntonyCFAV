package com.antony.cfav.res_path;

import android.os.Environment;

import java.io.File;

/**
 * 音视频 资源地址管理类
 */
public class ResPath {
    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator;

    /**
     * 音频存放地址
     */
    public static final String AUDIO_PATH = PATH + "CFAV_AUDIO" + File.separator;
    /*你的答案.mp3*/
    public static final String YourAnswerMP3 = AUDIO_PATH + "YourAnswer.mp3";
    /*dj.mp3*/
    public static final String DjMP3 = AUDIO_PATH + "dj.mp3";
    public static final String ExtractAac = AUDIO_PATH + "extract.aac";//抽取
    public static final String ExtractPcm = AUDIO_PATH + "extract.pcm";//抽取pcm
    public static final String TransformWav = AUDIO_PATH + "transform.wav";//转码
    public static final String TransformAac = AUDIO_PATH + "transform.aac";//转码
    public static final String CutMP3 = AUDIO_PATH + "cut.mp3";//裁剪
    public static final String ConcatMP3 = AUDIO_PATH + "concat.mp3";//合并
    public static final String MixAac = AUDIO_PATH + "mix.aac";//混音
    public static final String MixMp3 = AUDIO_PATH + "mix2.mp3";//混音
    public static final String EncodeAac = AUDIO_PATH + "encode.aac";//编码
    /**
     * 视频存放地址
     */
    public static final String VIDEO_PATH = PATH + "CFAV_VIDEO" + File.separator;
    /*LOL CG*/
    public static final String FateCinematicMP4 = VIDEO_PATH + "FateCinematic.mp4";
    public static final String ExtractH264 = VIDEO_PATH + "extract.h264";//提取.h264数据
    public static final String ExtractH264ByCommand = VIDEO_PATH + "extract2.h264";//提取.h264数据使用命令行
    public static final String ExtractYuv = VIDEO_PATH + "extract.yuv";//提取裸数据YUV
    public static final String ConversionFlv = VIDEO_PATH + "conversion.flv";//格式转换，这里是mp4转flv
}
