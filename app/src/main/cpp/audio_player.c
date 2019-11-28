
#include <jni.h>
#include "ffmpeg_jni_define.h"
#include <stdlib.h>
#include <unistd.h>
#include "libavformat/avformat.h"  //封装格式
#include "libavcodec/avcodec.h"  //解码
#include "libswscale/swscale.h" //缩放
#include "libswresample/swresample.h" //重采样
#include <android/log.h>
/*
* 1. 使用ffmpeg 将.mp3文件 解码成 .pcm裸数据
* 2. 使用Android AudioTrack 播放pcm数据
*/
#define TAG "AudioPlayer"
#define MAX_AUDIO_FRAME_SIZE 48000 * 4

JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_audio_AudioPlayer_byAudioTrackPlay(JNIEnv *env, jobject instance,
                                                                 jstring audioPath_) {
    const char *audioPath = (*env)->GetStringUTFChars(env, audioPath_, 0);
    AVFormatContext *aFormatCtx = avformat_alloc_context();
    // TODO 注册组件
    av_register_all();
    // TODO 打开音频文件
    if (avformat_open_input(&aFormatCtx, audioPath, NULL, NULL) != 0) {
        LOGE(TAG, "无法打开音频文件!!!");
        return;
    }
    // TODO 获取输入文件信息
    if (avformat_find_stream_info(aFormatCtx, NULL) != 0) {
        LOGE(TAG, "无法获取输入文件的信息!!!");
        return;
    }
    // TODO 获取音频流索引位置
    int audio_stream_index = -1;
    for (int i = 0; i < aFormatCtx->nb_streams; ++i) {
        if (aFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_index = i;
            break;
        }
    }
    // TODO 获取音频解码器
    AVCodecContext *aCodecCtx = aFormatCtx->streams[audio_stream_index]->codec;
    AVCodec *codec = avcodec_find_decoder(aCodecCtx->codec_id);
    if (codec == NULL) {
        LOGE(TAG, "无法获取解码器!!!");
        return;
    }
    // TODO 打开解码器
    if (avcodec_open2(aCodecCtx, codec, NULL) != 0) {
        LOGE(TAG, "无法打开解码器!!!");
        return;
    }
    // TODO 压缩数据
    AVPacket *packet = av_malloc(sizeof(AVPacket));
    // TODO 解压缩数据帧
    AVFrame *frame = av_frame_alloc();
    //frame->16bit 44100 PCM 统一音频采样格式与采样率
    SwrContext *swrCtx = swr_alloc();
    enum AVSampleFormat in_sample_fmt = aCodecCtx->sample_fmt;  //输入的采样格式
    enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16; //输出的采样格式 16bit pcm
    int in_sample_rate = aCodecCtx->sample_rate; // 输入采样率
    int out_sample_rate = in_sample_rate;//输出采样率
    uint64_t in_ch_layout = aCodecCtx->channel_layout; //声道布局(2个声道，默认立体声stereo)
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;//输出的声道布局(立体声)
    swr_alloc_set_opts(swrCtx, out_ch_layout, out_sample_fmt, out_sample_rate,
                       in_ch_layout, in_sample_fmt, in_sample_rate, 0, NULL);//重采样
    swr_init(swrCtx);
    int out_channel_nb = av_get_channel_layout_nb_channels(out_ch_layout);//输出的声道个数
    // TODO 这里我们得到 AudioPlayer java类
    jclass player_class = (*env)->GetObjectClass(env, instance);
    if (!player_class) {
        LOGE(TAG, "player_class not found...");
    }
    //TODO 找到 AudioPlayer 类中的 "public AudioTrack  createAudioTrack(sampleRate 采样率, channels 声道布局){}" 方法
    jmethodID audio_track_method = (*env)->GetMethodID(env, player_class, "createAudioTrack",
                                                       "(II)Landroid/media/AudioTrack;");
    if (!audio_track_method) {
        LOGE(TAG, "audio_track_method not found...");
    }
    //TODO 创建 “AudioTrack” 完成
    jobject audio_track = (*env)->CallObjectMethod(env, instance, audio_track_method,
                                                   out_sample_rate, out_channel_nb);
    jclass audio_track_class = (*env)->GetObjectClass(env, audio_track);
    // TODO 输入声道布局
    // TODO 输入声道布局
    // TODO 输入声道布局
    avformat_close_input(&aFormatCtx);
    (*env)->ReleaseStringUTFChars(env, audioPath_, audioPath);
}

