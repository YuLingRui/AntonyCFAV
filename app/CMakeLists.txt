
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
include_directories(./src/main/cpp
        ./src/main/cpp/ffmpeg
        ./src/main/cpp/include)

add_library(media-handle
        SHARED
        src/main/cpp/ffmpeg/cmdutils.c
        src/main/cpp/ffmpeg/ffmpeg.c
        src/main/cpp/ffmpeg/ffmpeg_filter.c
        src/main/cpp/ffmpeg/ffmpeg_opt.c
        src/main/cpp/ffmpeg_cmd.c
        src/main/cpp/audio_extract.c
        src/main/cpp/audio_player.c
        src/main/cpp/audio_lame.c
        src/main/cpp/video_extract_h264.c
        src/main/cpp/video_format_conversion.c
        src/main/cpp/video_cut.c)

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


find_library(log-lib log)


target_link_libraries(
        media-handle
        mp3lame
        ffmpeg
        ${log-lib})