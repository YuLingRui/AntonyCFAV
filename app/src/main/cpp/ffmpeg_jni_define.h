//
// Created by Administrator on 2019/11/27.
//

#ifndef ANTONYCFAV_FFMPEG_JNI_DEFINE_H
#define ANTONYCFAV_FFMPEG_JNI_DEFINE_H

#include <android/log.h>
#include <jni.h>
//这里定义了  info  error 日志类型的宏
#define LOGI(TAG, FORMAT, ...) _android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##_VA_ARGS_);
#define LOGE(TAG, FORMAT, ...) _android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##_VA_ARGS_);

/**
 * RETURN_TYPE: 返回值类型
 * FUNC_NAME:方法名 byAudioTrackPlay [使用AudioTrack播放音频文件]
 * ... : 传的参数
 */
#define AUDIO_PLAYER_FUNC(RETURN_TYPE, FUNC_NAME, ...)\
JNIEXPORT RETUN_TYPE  JNICALL  Java_com_antony_cfav_activity_audio_AudioPlayer_ ## FUNC_NAME(JNIEnv *env, jobject thiz, ##_VA_ARGS_);

#endif //ANTONYCFAV_FFMPEG_JNI_DEFINE_H
