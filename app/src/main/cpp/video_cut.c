//
// Created by Antony on 2019/12/24.
//
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "ffmpeg_jni_define.h"
#include "libavformat/avformat.h"  //封装格式
#include "libavcodec/avcodec.h"  //解码
#include "libswscale/swscale.h" //缩放
#include "libswresample/swresample.h" //重       采样
#include <android/log.h>

#define TAG "VideoPlayer"

JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_video_VideoPlayer_cutSection(JNIEnv *env, jobject instance,
                                                           jint startTime, jint endTime,
                                                           jstring src_, jstring dst_) {
    const char *src = (*env)->GetStringUTFChars(env, src_, 0);
    const char *dst = (*env)->GetStringUTFChars(env, dst_, 0);
    LOGI(TAG, "src= %s", src);
    LOGI(TAG, "dst= %s", dst);
    LOGI(TAG, "start= %d", startTime);
    LOGI(TAG, "end= %d", endTime);

    AVOutputFormat *ofmt = NULL;
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket pkt;
    av_register_all();
    //打开多媒体文件， 得到多媒体上下文AVFormatContext
    if (avformat_open_input(&ifmt_ctx, src, NULL, NULL) < 0) {
        LOGE(TAG, "avformat_open_input....error");
        goto end;
    }
    if (avformat_find_stream_info(ifmt_ctx, 0) < 0) {
        LOGE(TAG, "avformat_open_input....error");
        goto end;
    }
    av_dump_format(ifmt_ctx, 0, src, 0);
    //根据输出文件， 得到输出文件多媒体上下文
    if (avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, dst) < 0) {
        LOGE(TAG, "avformat_alloc_output_context2....error");
        goto end;
    }
    ofmt = ofmt_ctx->oformat;
    //输入多媒体文件的每一路流 拷贝到  输出文件多媒体文件的每一路流中
    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        //对于输入文件(输入的多媒体文件)的每一路流， 我们对应的输出文件 也要创建相应的输出流
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
        if (!out_stream) {
            LOGE(TAG, "out_stream is null");
            goto end;
        }
        //输入文件的codec信息 拷贝到 输出流的codec信息里
        if (avcodec_copy_context(out_stream->codec, in_stream->codec) < 0) {
            LOGE(TAG, "avcodec_copy_context....error");
            goto end;
        }
        out_stream->codec->codec_tag = 0;
        if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER) {
            out_stream->codec->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        }
    }
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        //打开 输出文件【只写】
        if (avio_open(&ofmt_ctx->pb, dst, AVIO_FLAG_WRITE) < 0) {
            LOGE(TAG, "avio_open....error");
        }
    }
    //给输出文件(剪裁后的多媒体文件)写头
    if (avformat_write_header(ofmt_ctx, NULL) < 0) {
        LOGE(TAG, "avformat_write_header....error");
        goto end;
    }
    //跳到 startTime
    if (av_seek_frame(ifmt_ctx, -1, startTime * AV_TIME_BASE, AVSEEK_FLAG_ANY) < 0) {
        LOGE(TAG, "av_seek_frame....error");
        goto end;
    }

    int64_t *dts_start_from = malloc(sizeof(int64_t) * ifmt_ctx->nb_streams);
    memset(dts_start_from, 0, sizeof(int64_t) * ifmt_ctx->nb_streams);
    int64_t *pts_start_from = malloc(sizeof(int64_t) * ifmt_ctx->nb_streams);
    memset(pts_start_from, 0, sizeof(int64_t) * ifmt_ctx->nb_streams);

    while (1) {
        AVStream *in_stream, *out_stream;
        if (av_read_frame(ifmt_ctx, &pkt) < 0) {
            break;
        }
        in_stream = ifmt_ctx->streams[pkt.stream_index];
        out_stream = ofmt_ctx->streams[pkt.stream_index];
        if (av_q2d(in_stream->time_base) * pkt.pts > endTime) {
            av_free_packet(&pkt);
            break;
        }
        if (dts_start_from[pkt.stream_index] == 0) {
            dts_start_from[pkt.stream_index] = pkt.dts;
        }
        if (pts_start_from[pkt.stream_index] == 0) {
            pts_start_from[pkt.stream_index] = pkt.pts;
        }
        /* copy packet */
        pkt.pts = av_rescale_q_rnd(pkt.pts - pts_start_from[pkt.stream_index], in_stream->time_base,
                                   out_stream->time_base, AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX);
        pkt.dts = av_rescale_q_rnd(pkt.dts - dts_start_from[pkt.stream_index], in_stream->time_base,
                                   out_stream->time_base, AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX);
        if (pkt.pts < 0) {
            pkt.pts = 0;
        }
        if (pkt.dts < 0) {
            pkt.dts = 0;
        }
        pkt.duration = (int) av_rescale_q((int64_t) pkt.duration, in_stream->time_base,
                                          out_stream->time_base);
        pkt.pos = -1;
        //把每一个包 写到输出文件中
        if (av_interleaved_write_frame(ofmt_ctx, &pkt) < 0) {
            LOGE(TAG, "av_interleaved_write_frame....error");
            break;
        }
        LOGI(TAG, "read frame...");
        av_free_packet(&pkt);
    }
    free(dts_start_from);
    free(pts_start_from);
    av_write_trailer(ofmt_ctx);

    end:
    avformat_close_input(&ifmt_ctx);
    /* close output */
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE)) {
        avio_closep(&ofmt_ctx->pb);
    }
    avformat_free_context(ofmt_ctx);
}
