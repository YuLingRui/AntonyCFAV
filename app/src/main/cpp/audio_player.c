//
// Created by Administrator on 2019/11/26.
//

#include <jni.h>
#include "ffmpeg_jni_define.h"

JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_audio_AudioPlayer_byAudioTrackPlay(JNIEnv *env, jobject instance, jstring audioPath_) {
    const char *audioPath = (*env)->GetStringUTFChars(env, audioPath_, 0);
    /*
     * 1. 使用ffmpeg 将.mp3文件 解码成 .pcm裸数据
     * 2. 使用Android AudioTrack 播放pcm数据
     * */
    // TODO 注册组件
    // TODO 打开音频文件
    // TODO 获取输入文件信息
    // TODO 获取音频流索引位置
    // TODO 获取音频解码器
    // TODO 打开解码器
    // TODO 压缩数据
    // TODO 解压缩数据
    // TODO 输入的采样格式
    // TODO 输出的采样格式 16bit pcm
    // TODO 输入采样率
    // TODO 输出采样率
    // TODO 输入声道布局
    // TODO 输出声道布局
    // TODO 输入声道布局
    // TODO 输入声道布局
    // TODO 输入声道布局
    // TODO 输入声道布局
    (*env)->ReleaseStringUTFChars(env, audioPath_, audioPath);
}

