
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "ffmpeg_jni_define.h"
#include "libavformat/avformat.h"  //封装格式
#include "libavcodec/avcodec.h"  //解码
#include "libswscale/swscale.h" //缩放
#include "libswresample/swresample.h" //重采样
#include <android/log.h>
/*
* 格式转换 .mp4格式 转成  .flv格式
*/
#define TAG "VideoPlayer"

JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_video_VideoPlayer_formatConversion(JNIEnv *env, jobject instance,
                                                                 jstring src_, jstring dst_) {
    const char *src = (*env)->GetStringUTFChars(env, src_, 0);
    const char *dst = (*env)->GetStringUTFChars(env, dst_, 0);
    AVFormatContext *ifmt_ctx, *ofmt_ctx;
    AVOutputFormat *ofmt;
    AVPacket pkt;
    int stream_index = 0, stream_mapping_size = 0, *stream_mapping = NULL;
    //打开多媒体文件
    if (avformat_open_input(&ifmt_ctx, src, NULL, NULL) < 0) {
        LOGE(TAG, "avformat_open_input  error.....");
        goto end;
    }
    //查找流信息
    if (avformat_find_stream_info(ifmt_ctx, NULL) < 0) {
        LOGE(TAG, "avformat_find_stream_info  error.....");
        goto end;
    }
    //dump出多媒体文件信息
    av_dump_format(ifmt_ctx, 0, src, 0);
    //创建一个输出的上下文
    if (avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, dst) < 0) {
        LOGE(TAG, "avformat_alloc_output_context2  error.....");
        goto end;
    }
    stream_mapping_size = ifmt_ctx->nb_streams;
    stream_mapping = av_malloc_array(stream_mapping_size, sizeof(*stream_mapping));
    ofmt = ofmt_ctx->oformat;

    //针对传进来的多媒体， 每一路流，我们都要创建新的流
    //这里我们只搞 视频流 音频流 字幕流
    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *out_stream;
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVCodecContext *codec_ctx = in_stream->codec; //in_stream->codec->codec_type
        if (codec_ctx->codec_type != AVMEDIA_TYPE_AUDIO &&
            codec_ctx->codec_type != AVMEDIA_TYPE_VIDEO &&
            codec_ctx->codec_type != AVMEDIA_TYPE_SUBTITLE) {
            stream_mapping[i] = -1;
            continue;
        }
        stream_mapping[i] = stream_index++;

        out_stream = avformat_new_stream(ofmt_ctx, NULL);
        if (!out_stream) {
            LOGE(TAG, "avformat_new_stream  error.....out_stream is null");
            goto end;
        }

    }

    //向输出文件中  “写头”
    if (avformat_write_header(ofmt_ctx, NULL) < 0) {
        LOGE(TAG, "avformat_write_header  error.....");
        goto end;
    }
    //向输出文件中  “写数据data”
    while (1) {
        AVStream *in_stream, *out_stream;
        if (av_read_frame(ifmt_ctx, &pkt) < 0) {
            break;
        }
        in_stream = ifmt_ctx->streams[pkt.stream_index];
        if (pkt.stream_index >= stream_mapping_size ||
            stream_mapping[pkt.stream_index] < 0) {
            av_packet_unref(&pkt);
        }

        pkt.stream_index = stream_mapping[pkt.stream_index];
        out_stream = ofmt_ctx->streams[pkt.stream_index];

        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base,
                                   AV_ROUND_INF | AV_ROUND_PASS_MINMAX);
        pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base,
                                   AV_ROUND_INF | AV_ROUND_PASS_MINMAX);
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        pkt.pos = -1;
        if (av_interleaved_write_frame(ofmt_ctx, &pkt) < 0) {
            LOGE(TAG, "av_interleaved_write_frame  error.....");
            break;
        }
        av_packet_unref(&pkt);
    }
    //向输出文件中  “写尾”
    av_write_trailer(ofmt_ctx);

    //释放资源
    end:
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE)) {
        avio_closep(ofmt_ctx);
    }
    avformat_free_context(ofmt_ctx);
    av_freep(&stream_mapping);
}