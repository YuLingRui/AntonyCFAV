
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

JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_audio_AudioPlayer_extractAudio(JNIEnv *env, jobject instance,
                                                             jstring src_, jstring dst_) {
    const char *src = (*env)->GetStringUTFChars(env, src_, 0);
    const char *dst = (*env)->GetStringUTFChars(env, dst_, 0);
    LOGI(TAG, "原始文件：%s", src);
    LOGI(TAG, "输出文件：%s", dst);

    int audio_index;
    AVFormatContext *afCtx = NULL;
    AVPacket *packet;

    av_register_all();

    if (avformat_open_input(&afCtx, src, NULL, NULL) != 0) {
        LOGE(TAG, "无法打开文件!!!");
        return;
    }
    FILE *dst_fd = fopen(dst, "wd");
    if (!dst_fd) {
        LOGE(TAG, "dst_fd create faile");
        avformat_close_input(&afCtx);
        return;
    }
    audio_index = av_find_best_stream(afCtx, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
    if (audio_index < 0) {
        avformat_close_input(&afCtx);
        fclose(dst_fd);
    }
    av_init_packet(&packet);
    while (av_read_frame(afCtx, &packet) >= 0) {
        if (packet->stream_index == audio_index) {
            size_t len = fwrite(packet->data, 1, packet->size, dst_fd);
            if (len != packet->size) {
                LOGI(TAG, "len don't match packet size");
            }
        }
        av_packet_unref(&packet);
    }

    //释放资源
    if (dst_fd) {
        fclose(dst_fd);
    }
    avformat_close_input(&afCtx);
    (*env)->ReleaseStringUTFChars(env, src_, src);
}