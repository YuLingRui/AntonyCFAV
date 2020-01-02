package com.antony.cfav.ffmpeg;

import android.annotation.SuppressLint;
import android.util.Log;

/**
 * 通过 FFmpeg 命令行处理音视频
 */
public class FFmpegUtil {
    private final static String TAG = "FFmpegUtil";

    /**
     * 音频 转码
     *
     * @param srcFile
     * @param targetFile
     * @return
     */
    public static String[] transformAudio(String srcFile, String targetFile) {
        Log.i(TAG, "src:" + srcFile + "    target:" + targetFile);
        String transform = "ffmpeg -i %s -f wav %s"; //这里尝试将 .mp3 转换成 .wav 格式
        //String transform = "ffmpeg -i %s %s"; //这里尝试将 .mp3 转换成 .aac 格式
        transform = String.format(transform, srcFile, targetFile);
        return transform.split(" ");
    }

    /**
     * 音频裁剪
     *
     * @param srcFile    源文件
     * @param startTime  开始时间(秒)
     * @param duration   裁剪时长(秒)
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] cutAudio(String srcFile, int startTime, int duration, String targetFile) {
        Log.i(TAG, "src:" + srcFile + "    target:" + targetFile);
        String cut = "ffmpeg -i %s -acodec copy -ss %d -t %d %s"; //音频裁剪
        cut = String.format(cut, srcFile, startTime, duration, targetFile);
        return cut.split(" ");
    }

    /**
     * 音频合并
     *
     * @param srcFile    源文件
     * @param appendFile 追加文件
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] concatAudio(String srcFile, String appendFile, String targetFile) {
        Log.i(TAG, "src:" + srcFile + "    target:" + targetFile);
        String concat = "ffmpeg -i concat:%s|%s -acodec copy %s"; //音频合并
        concat = String.format(concat, srcFile, appendFile, targetFile);
        return concat.split(" ");
    }

    /**
     * 混音
     *
     * @param srcFile    源文件
     * @param mixFile    待混合文件
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] mixAudio(String srcFile, String mixFile, String targetFile) {
        Log.i(TAG, "src:" + srcFile + "    target:" + targetFile);
        //todo: 这串命令 我实验只能混合后生成 .aac【mix.aac】才可行！！！
        String mix = "ffmpeg -i %s -i %s -filter_complex amix=inputs=2:duration=first -strict -2 %s";
        mix = String.format(mix, srcFile, mixFile, targetFile);
        return mix.split(" ");
    }

    /**
     * 抽取音频裸数据[PCM]
     *
     * @param srcFile    源文件
     * @param rate       码率
     * @param channel    声道
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] extractAudioPcm(String srcFile, int rate, int channel, String targetFile) {
        Log.i(TAG, "src:" + srcFile + "    target:" + targetFile);
        String extract = "ffmpeg -i %s -vn -ar %d -ac %d -f s16le %s";
        extract = String.format(extract, srcFile, rate, channel, targetFile);
        return extract.split(" ");
    }

    /**
     * 音频裸数据[PCM]编码
     *
     * @param srcFile    源文件
     * @param rate       码率
     * @param channel    声道
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] encodeAudio(String srcFile, int rate, int channel, String targetFile) {
        String encode = "ffmpeg -f s16le -ar %d -ac %d -i %s %s";
        encode = String.format(encode, rate, channel, srcFile, targetFile);
        return encode.split(" ");
    }

    /**
     * 抽取多媒体中 H264数据
     *
     * @param srcFile
     * @param targetFile
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] extractVideoH264(String srcFile, String targetFile) {
        String extract_h264 = "ffmpeg -i %s -an -vcodec copy %s";
        extract_h264 = String.format(extract_h264, srcFile, targetFile);
        return extract_h264.split(" ");
    }

    /**
     * 抽取多媒体中 YUV裸数据
     *
     * @param srcFile
     * @param targetFile
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] extractVideoYUV(String srcFile, String targetFile) {
        String encode = "ffmpeg -i %s -an -c:v rawvideo -pix_fmt yuv420p %s";
        encode = String.format(encode, srcFile, targetFile);
        return encode.split(" ");
    }

    /**
     * 使用ffmpeg命令进行视频剪切
     * @param srcFile 源文件
     * @param startTime 剪切的开始时间
     * @param endTime 剪切的结束时间
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] cutVideo(String srcFile, int startTime, int endTime, String targetFile) {
        String cutVideo = "ffmpeg -i %s -ss %d -t %d %s";
        cutVideo = String.format(cutVideo, srcFile,startTime, endTime, targetFile);
        return cutVideo.split(" ");
    }

}
