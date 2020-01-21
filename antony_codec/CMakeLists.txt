cmake_minimum_required(VERSION 3.4.1)

find_library(log-lib log)
find_library(android-lib android)

add_library(native-lib SHARED
    src/main/cpp/nativie-lib.cpp
)

set(JNI_LIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES  IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libavutil.so)

add_library(swresample
        SHARED
        IMPORTED )
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libswresample.so )

add_library(swscale
        SHARED
        IMPORTED )
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libswscale.so )

add_library(avcodec
        SHARED
        IMPORTED )
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libavcodec.so )

add_library(avformat
        SHARED
        IMPORTED )
set_target_properties(avformat
        PROPERTIES
        IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libavformat.so )

add_library(avfilter
        SHARED
        IMPORTED )
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libavfilter.so )

add_library(avdevice
        SHARED
        IMPORTED )
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ../../../../src/main/jniLibs/armeabi-v7a/libavdevice.so )

include_directories(${JNI_LIBS_DIR}/includes)

#获取上级目录
get_filename_component(PARENT_DIR ${CMAKE_SOURCE_DIR} PATH)
#使用单个yuv库
set(LIBYUV_DIR ${PARENT_DIR}/libyuv-single)
add_library(libyuv
        SHARED
        IMPORTED)

set_target_properties(libyuv
        PROPERTIES IMPORTED_LOCATION
        ${LIBYUV_DIR}/libs/${ANDROID_ABI}/libyuv.so)

# libyuv库头文件
include_directories(${LIBYUV_DIR}/libs/${ANDROID_ABI}/include)

target_link_libraries(native-lib
        avutil swresample swscale avcodec avformat avfilter avdevice libyuv
        ${log-lib} ${android-lib} )