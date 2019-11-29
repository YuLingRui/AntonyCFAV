
cmake_minimum_required(VERSION 3.4.1)


add_library(media-handle
        SHARED
        src/main/cpp/audio_extract.c
        src/main/cpp/audio_player.c
        src/main/cpp/audio_lame.c)

add_library(ffmpeg
        SHARED
        IMPORTED)
set_target_properties(ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libffmpeg.so)

add_library(mp3lame
        SHARED
        IMPORTED)
set_target_properties(mp3lame
        PROPERTIES IMPORTED_LOCATION
        ../../../../libs/armeabi-v7a/libmp3lame.so)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(src/main/cpp)
include_directories(src/main/cpp/include)


find_library(log-lib log)


target_link_libraries(
        media-handle
        mp3lame
        ffmpeg
        ${log-lib})