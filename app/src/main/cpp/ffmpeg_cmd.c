
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "ffmpeg_jni_define.h"
#include "ffmpeg.h"


#define TAG "FFmpegUtil"

JNIEXPORT jint JNICALL
Java_com_antony_cfav_ffmpeg_FFmpegCmd_handle(JNIEnv *env, jclass type, jobjectArray commands) {
    //commands java层是String[]数组
    int argc = (*env)->GetArrayLength(env, commands);
    char **argv = (char **) malloc(argc * sizeof(char *)); //长度为length的字符串[在堆内存中，最后要释放]
    int i, result;
    for (i = 0; i < argc; i++) {
        jstring jstr = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        char *temp = (char *) (*env)->GetStringUTFChars(env, jstr, 0);
        LOGI(TAG, "temp %s", temp);
        argv[i] = malloc(1024);
        strcpy(argv[i], temp);
        (*env)->ReleaseStringUTFChars(env, jstr, temp);
    }
    result = run(argc, argv);
    for (i = 0; i < argc; ++i) {
        free(argv[i]);//释放内存
    }
    free(argv);
    return result;
}

