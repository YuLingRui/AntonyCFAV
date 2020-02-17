//
// Created by Administrator on 2020/2/17.
// 模拟一个范例， 使用FFmpeg 编译Encode H2264
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "loger.h"
#include <libavcodec/avcodec.h>
#include <libavutil/opt.h>
#include <libavutil/imgutils.h>

#define TAG "FFencodeNative"

JNIEXPORT jint JNICALL
Java_com_codec_ffcodec_FFcodecNative_encode_1example(JNIEnv *env, jobject instance,
                                                      jstring enPath_, jstring codecName_) {
    const char *enPath = (*env)->GetStringUTFChars(env, enPath_, 0);
    const char *codecName = (*env)->GetStringUTFChars(env, codecName_, 0);
    AVCodecContext *avCodecCtx;
    const AVCodec *codec;
    AVFrame *frame;
    AVPacket pkt;
    FILE *file;

    codec = avcodec_find_encoder_by_name(codecName);
    if (!codec) {
        LOGE(TAG, "AVCodec  is  null");
        return -1;
    }
    avCodecCtx = avcodec_alloc_context3(codec);
    if (!avCodecCtx) {
        LOGE(TAG, "AVCodecContext is null");
        return -1;
    }

    avCodecCtx->bit_rate = 400000; //码率
    avCodecCtx->width = 480; // 视频宽度
    avCodecCtx->height = 360;// 视频高度
    avCodecCtx->time_base = {1, 25};//时间基 1秒钟25帧
    avCodecCtx->framerate = {25, 1};//帧率  越大码流越大 越流畅 越清晰
    avCodecCtx->gop_size = 10; //多少帧 产生一个关键帧【i】
    avCodecCtx->max_b_frames = 1; //b帧，前后参考帧
    avCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P; //编码的原始数据YUV格式
    if (codec->id == AV_CODEC_ID_H264) {
        av_opt_set(avCodecCtx->priv_data, "preset", "slow", 0);
    }
    if (avcodec_open2(avCodecCtx, codec, NULL) < 0) {
        LOGE(TAG, "avcodec open2 is faile");
        return -1;
    }

    file = fopen(enPath, "wb");
    if (!file) {
        LOGE(TAG, "enPath is null");
        return -1;
    }

    frame = av_frame_alloc();
    frame->format = avCodecCtx->pix_fmt;
    frame->width = avCodecCtx->width;
    frame->height = avCodecCtx->height;
    if (av_frame_get_buffer(frame, 32) < 0) {
        LOGE(TAG, "Could not allocate the video frame data");
        return -1;
    }

    int i, ret, x, y, got_output;
    uint8_t endcode[] = {0, 0, 1, 0xb7};
    /* encode 1 second of video 模拟数据帧 进行编码*/
    for (i = 0; i < 25; i++) {
        av_init_packet(&pkt);
        pkt.data = NULL;    // packet data will be allocated by the encoder
        pkt.size = 0;

        fflush(stdout);

        /* make sure the frame data is writable */
        ret = av_frame_make_writable(frame);
        if (ret < 0) {
            LOGE(TAG, "Frame data not writable");
            return -1;
        }

        /* prepare a dummy image 模拟数据*/
        /* Y */
        for (y = 0; y < avCodecCtx->height; y++) {
            for (x = 0; x < avCodecCtx->width; x++) {
                frame->data[0][y * frame->linesize[0] + x] = x + y + i * 3;
            }
        }

        /* Cb and Cr */
        for (y = 0; y < avCodecCtx->height / 2; y++) {
            for (x = 0; x < avCodecCtx->width / 2; x++) {
                frame->data[1][y * frame->linesize[1] + x] = 128 + y + i * 2;
                frame->data[2][y * frame->linesize[2] + x] = 64 + x + i * 5;
            }
        }

        frame->pts = i;

        /* encode the image */
        ret = avcodec_encode_video2(avCodecCtx, &pkt, frame, &got_output);
        if (ret < 0) {
            //fprintf(stderr, "Error encoding frame\n");
            LOGE(TAG, "Error encoding frame");
            return -1;
        }

        if (got_output) {
            //printf("Write frame %3d (size=%5d)\n", i, pkt.size);
            LOGI(TAG, "Write frame %3d (size=%5d)\n", i, pkt.size);
            fwrite(pkt.data, 1, pkt.size, file);
            av_packet_unref(&pkt);
        }
    }

    /* get the delayed frames */
    for (got_output = 1; got_output; i++) {
        fflush(stdout);

        ret = avcodec_encode_video2(avCodecCtx, &pkt, NULL, &got_output);
        if (ret < 0) {
            //fprintf(stderr, "Error encoding frame\n");
            LOGE(TAG, "Error encoding frame");
            return -1;
        }

        if (got_output) {
            printf("Write frame %3d (size=%5d)\n", i, pkt.size);
            fwrite(pkt.data, 1, pkt.size, file);
            av_packet_unref(&pkt);
        }
    }

    /* add sequence end code to have a real MPEG file */
    fwrite(endcode, 1, sizeof(endcode), file);
    fclose(file);

    avcodec_free_context(&avCodecCtx);
    av_frame_free(&frame);
    return 0;
}