
#ifndef ANTONYCFAV_FFMPEG_JNI_DEFINE_H
#define ANTONYCFAV_FFMPEG_JNI_DEFINE_H

#include <android/log.h>
#include <jni.h>
//这里定义了  info  error 日志类型的宏
#define LOGI(TAG, FORMAT, ...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##__VA_ARGS__);
#define LOGE(TAG, FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);
#endif //ANTONYCFAV_FFMPEG_JNI_DEFINE_H
