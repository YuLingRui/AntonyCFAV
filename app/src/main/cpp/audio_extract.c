
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
/**
 * 添加adts头  描述： 采样率，声道布局等【要不ffmplay 播放不了，没声音】
 */
void adts_header(char *szAdtsHeader, int dataLen) {

    int audio_object_type = 2;
    int sampling_frequency_index = 7;
    int channel_config = 2;

    int adtsLen = dataLen + 7;

    szAdtsHeader[0] = 0xff;         //syncword:0xfff                          高8bits
    szAdtsHeader[1] = 0xf0;         //syncword:0xfff                          低4bits
    szAdtsHeader[1] |= (0 << 3);    //MPEG Version:0 for MPEG-4,1 for MPEG-2  1bit
    szAdtsHeader[1] |= (0 << 1);    //Layer:0                                 2bits
    szAdtsHeader[1] |= 1;           //protection absent:1                     1bit

    szAdtsHeader[2] = (audio_object_type - 1)
            << 6;            //profile:audio_object_type - 1                      2bits
    szAdtsHeader[2] |= (sampling_frequency_index & 0x0f)
            << 2; //sampling frequency index:sampling_frequency_index  4bits
    szAdtsHeader[2] |= (0
            << 1);                             //private bit:0                                      1bit
    szAdtsHeader[2] |= (channel_config & 0x04)
            >> 2;           //channel configuration:channel_config               高1bit

    szAdtsHeader[3] =
            (channel_config & 0x03) << 6;     //channel configuration:channel_config      低2bits
    szAdtsHeader[3] |= (0
            << 5);                      //original：0                               1bit
    szAdtsHeader[3] |= (0
            << 4);                      //home：0                                   1bit
    szAdtsHeader[3] |= (0
            << 3);                      //copyright id bit：0                       1bit
    szAdtsHeader[3] |= (0
            << 2);                      //copyright id start：0                     1bit
    szAdtsHeader[3] |= ((adtsLen & 0x1800) >> 11);           //frame length：value   高2bits

    szAdtsHeader[4] = (uint8_t) ((adtsLen & 0x7f8) >> 3);     //frame length:value    中间8bits
    szAdtsHeader[5] = (uint8_t) ((adtsLen & 0x7) << 5);       //frame length:value    低3bits
    szAdtsHeader[5] |= 0x1f;                                 //buffer fullness:0x7ff 高5bits
    szAdtsHeader[6] = 0xfc;
}

JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_audio_AudioPlayer_extractAudio(JNIEnv *env, jobject instance,
                                                             jstring src_, jstring dst_) {
    const char *src = (*env)->GetStringUTFChars(env, src_, 0);
    const char *dst = (*env)->GetStringUTFChars(env, dst_, 0);
    LOGI(TAG, "原始文件：%s", src); //  /storage/emulated/0/cg.mp4
    LOGI(TAG, "输出文件：%s", dst); //  /storage/emulated/0/my.aac

    int audio_index, len;
    AVFormatContext *afCtx = NULL;
    AVPacket packet;

    av_register_all();

    if (avformat_open_input(&afCtx, src, NULL, NULL) < 0) {
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
    LOGI(TAG, "音频流index：%d", audio_index);
    if (audio_index < 0) {
        avformat_close_input(&afCtx);
        fclose(dst_fd);
    }
    av_init_packet(&packet);
    packet.data = NULL;
    packet.size = 0;
    while (av_read_frame(afCtx, &packet) >= 0) {
        if (packet.stream_index == audio_index) {
            char adts_header_buf[7];
            adts_header(adts_header_buf, packet.size);
            fwrite(adts_header_buf, 1, 7, dst_fd);
            len = fwrite(packet.data, 1, packet.size, dst_fd);
            LOGI(TAG, "写入输入：%d", len);
            if (len != packet.size) {
                LOGI(TAG, "len don't match packet size");
            }
        }
        av_packet_unref(&packet);
    }
    //释放资源
    avformat_close_input(&afCtx);
    if (dst_fd) {
        fclose(dst_fd);
    }
    (*env)->ReleaseStringUTFChars(env, src_, src);
}