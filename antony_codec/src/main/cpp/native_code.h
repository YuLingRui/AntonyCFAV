//
// Created by Antony on 2020/2/10.
//
#include <jni.h>

#ifndef ANTONYCFAV_NATIVE_CODE_H
#define ANTONYCFAV_NATIVE_CODE_H
extern "C" {
JNIEXPORT  void JNICALL onPreviewFrame(JNIEnv *, jobject, jbyteArray, jint, jint);
JNIEXPORT void JNICALL encodeMP4Start(JNIEnv *, jobject, jstring, jint, jint);
JNIEXPORT void JNICALL encodeMP4Stop(JNIEnv *, jobject);
JNIEXPORT void JNICALL encodeJPEG(JNIEnv *, jobject, jstring, jint, jint);
};


#endif //ANTONYCFAV_NATIVE_CODE_H


