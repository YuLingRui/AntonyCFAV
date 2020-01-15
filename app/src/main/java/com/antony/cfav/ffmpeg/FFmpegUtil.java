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
     *
     * @param srcFile    源文件
     * @param startTime  剪切的开始时间
     * @param endTime    剪切的结束时间
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] cutVideo(String srcFile, int startTime, int endTime, String targetFile) {
        String cutVideo = "ffmpeg -i %s -ss %d -t %d %s";
        cutVideo = String.format(cutVideo, srcFile, startTime, endTime, targetFile);
        return cutVideo.split(" ");
    }

    /**
     * 视频拼接
     *
     * @param srcFile    第一个视频
     * @param conFile    第二个视频
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] concatVideo(String srcFile, String conFile, String targetFile) {
        String concat = "ffmpeg -i concat: %s|%s -c copy %s";
        concat = String.format(concat, srcFile, conFile, targetFile);
        return concat.split(" ");
    }

    /**
     * 视频转码
     *
     * @param srcFile    源文件
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] transformVideo(String srcFile, String targetFile) {
        String transform = "ffmpeg -i %s -vcodec copy -acodec copy %s";
        transform = String.format(transform, srcFile, targetFile);
        return transform.split(" ");
    }

    /**
     * 视频截图
     *
     * @param srcFile    源文件
     * @param screenTime 截图的时间点
     * @param size       截图大小
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] screenShotVideo(String srcFile, int screenTime, String size, String targetFile) {
        String screenShot = "ffmpeg -i %s -f image2 -ss %d -s %s %s";
        screenShot = String.format(screenShot, srcFile, screenTime, size, targetFile);
        return screenShot.split(" ");
    }

    /**
     * 使用ffmpeg命令行给视频添加水印
     *
     * @param srcFile    源文件
     * @param waterMark  水印文件路径
     * @param targetFile 目标文件
     * @return 添加水印后的文件
     */
    public static String[] addWaterMark(String srcFile, String waterMark, String resolution, int bitRate, String targetFile) {
        String mBitRate = String.valueOf(bitRate) + "k";
        String waterMarkCmd = "ffmpeg -i %s -i %s -s %s -b:v %s -filter_complex overlay=0:0 %s";
        waterMarkCmd = String.format(waterMarkCmd, srcFile, waterMark, resolution, mBitRate, targetFile);
        return waterMarkCmd.split(" ");
    }
    //ffmpeg -y -t 60 -i input.mp4 -i logo1.png -i logo2.png -filter_complex "overlay=x=if(lt(mod(t\,20)\,10)\,10\,NAN ):y=10,overlay=x=if(gt(mod(t\,20)\,10)\,W-w-10\,NAN ) :y=10" output.mp4

    /**
     * 视频画中画
     * @param srcFile 第一个视频
     * @param inFile 第二个视频
     * @param x 坐标
     * @param y 坐标
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] pinInPicVideo(String srcFile, String inFile, int x, int y, String targetFile) {
        String pinIn = "ffmpeg -i %s -i %s -filter_complex overlay=%d:%d %s";
        pinIn = String.format(pinIn, srcFile, inFile, x, y, targetFile);
        return pinIn.split(" ");
    }

    /**
     * 使用ffmpeg命令进行视频截图
     * @param srcFile 源文件
     * @param time 截图开始时间
     * @param size 图片尺寸大小
     * @param targetFile 目标文件
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String[] screenShot(String srcFile, int time, String size, String targetFile) {
        String screen = "ffmpeg -i %s -f image2 -ss %d -s %s %s";
        screen = String.format(screen, srcFile, time, size, targetFile);
        return screen.split(" ");
    }


    /**
     * 使用ffmpeg命令行进行视频转成Gif动图
     *
     * @param srcFile    源文件
     * @param startTime  开始时间
     * @param duration   截取时长
     * @param resolution 分辨率
     * @param frameRate  帧率
     * @param targetFile 目标文件
     * @return Gif文件
     */
    @SuppressLint("DefaultLocale")
    public static String[] generateGif(String srcFile, int startTime, int duration, String resolution, int frameRate, String targetFile) {
        String generateGifCmd = "ffmpeg -i %s -ss %d -t %d -s %s -r %d -f gif %s";
        generateGifCmd = String.format(generateGifCmd, srcFile, startTime, duration,
                resolution, frameRate, targetFile);
        return generateGifCmd.split(" ");
    }





































}
