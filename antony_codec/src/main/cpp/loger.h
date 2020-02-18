//
// Created by Antony on 2020/2/11.
//

#ifndef ANTONYCFAV_LOGER_H
#define ANTONYCFAV_LOGER_H

#ifdef ANDROID
#include <jni.h>
#include <android/log.h>
#include <libavutil/time.h>

#define LOGI(TAG, FORMAT, ...) __android_log_print(ANDROID_LOG_INFO, TAG, FORMAT, ##__VA_ARGS__);
#define LOGE(TAG, FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);
#else
#define LOGE(format, ...)  printf(LOG_TAG format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf(LOG_TAG format "\n", ##__VA_ARGS__)
#endif

#endif //ANTONYCFAV_LOGER_H
