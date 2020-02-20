//
// Created by Administrator on 2020/2/18.
//模拟一个范例， 使用FFmpeg  解码H264 后 生成一些图片

#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "loger.h"
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>
#include <libavutil/imgutils.h>

#define TAG "FFcodecNative"

#define INBUF_SIZE 4096

#define WORD uint16_t
#define DWORD uint32_t
#define LONG int32_t

#pragma pack(2)
typedef struct tagBITMAPFILEHEADER {
    WORD bfType;
    DWORD bfSize;
    WORD bfReserved1;
    WORD bfReserved2;
    DWORD bfOffBits;
} BITMAPFILEHEADER, *PBITMAPFILEHEADER;


typedef struct tagBITMAPINFOHEADER {
    DWORD biSize;
    LONG biWidth;
    LONG biHeight;
    WORD biPlanes;
    WORD biBitCount;
    DWORD biCompression;
    DWORD biSizeImage;
    LONG biXPelsPerMeter;
    LONG biYPelsPerMeter;
    DWORD biClrUsed;
    DWORD biClrImportant;
} BITMAPINFOHEADER, *PBITMAPINFOHEADER;

void saveBMP(struct SwsContext *img_convert_ctx, AVFrame *frame, char *filename) {
    //1 先进行转换,  YUV420=>RGB24:
    int w = frame->width;
    int h = frame->height;


    int numBytes = avpicture_get_size(AV_PIX_FMT_BGR24, w, h);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));


    AVFrame *pFrameRGB = av_frame_alloc();
    /* 缓冲区将被写入rawvideo文件，没有对齐 */
    /*
    if (av_image_alloc(pFrameRGB->data, pFrameRGB->linesize,  w, h, AV_PIX_FMT_BGR24, pix_fmt, 1) < 0) {
        fprintf(stderr, "Could not allocate destination image\n");
        exit(1);
    }
    */
    avpicture_fill((AVPicture *) pFrameRGB, buffer, AV_PIX_FMT_BGR24, w, h);

    sws_scale(img_convert_ctx, frame->data, frame->linesize, 0, h, pFrameRGB->data,
              pFrameRGB->linesize);

    //2 构造 BITMAPINFOHEADER
    BITMAPINFOHEADER header;
    header.biSize = sizeof(BITMAPINFOHEADER);


    header.biWidth = w;
    header.biHeight = h * (-1);
    header.biBitCount = 24;
    header.biCompression = 0;
    header.biSizeImage = 0;
    header.biClrImportant = 0;
    header.biClrUsed = 0;
    header.biXPelsPerMeter = 0;
    header.biYPelsPerMeter = 0;
    header.biPlanes = 1;

    //3 构造文件头
    BITMAPFILEHEADER bmpFileHeader = {0,};
    //HANDLE hFile = NULL;
    DWORD dwTotalWriten = 0;
    DWORD dwWriten;

    bmpFileHeader.bfType = 0x4d42; //'BM';
    bmpFileHeader.bfSize = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER) + numBytes;
    bmpFileHeader.bfOffBits = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);

    FILE *pf = fopen(filename, "wb");
    fwrite(&bmpFileHeader, sizeof(BITMAPFILEHEADER), 1, pf);
    fwrite(&header, sizeof(BITMAPINFOHEADER), 1, pf);
    fwrite(pFrameRGB->data[0], 1, numBytes, pf);
    fclose(pf);
    //释放资源
    //av_free(buffer);
    av_freep(&pFrameRGB[0]);
    av_free(pFrameRGB);
}

static void pgm_save(unsigned char *buf, int wrap, int xsize, int ysize,
                     char *filename) {
    FILE *f;
    int i;

    f = fopen(filename, "w");
    fprintf(f, "P5\n%d %d\n%d\n", xsize, ysize, 255);
    for (i = 0; i < ysize; i++)
        fwrite(buf + i * wrap, 1, xsize, f);
    fclose(f);
}

static int decode_write_frame(const char *outfilename, AVCodecContext *avctx,
                              struct SwsContext *img_convert_ctx, AVFrame *frame, int *frame_count,
                              AVPacket *pkt, int last) {
    int len, got_frame;
    char buf[1024];

    len = avcodec_decode_video2(avctx, frame, &got_frame, pkt);
    if (len < 0) {
        fprintf(stderr, "Error while decoding frame %d\n", *frame_count);
        return len;
    }
    if (got_frame) {
        printf("Saving %sframe %3d\n", last ? "last " : "", *frame_count);
        fflush(stdout);

        /* the picture is allocated by the decoder, no need to free it */
        snprintf(buf, sizeof(buf), "%s-%d.bmp", outfilename, *frame_count);

        /*
        pgm_save(frame->data[0], frame->linesize[0], frame->width, frame->height, buf);
        */
        saveBMP(img_convert_ctx, frame, buf);
        (*frame_count)++;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_codec_ffcodec_FFcodecNative_decode_1example(JNIEnv *env, jclass type, jstring src_,
                                                     jstring img_) {
    const char *src = (*env)->GetStringUTFChars(env, src_, 0);
    const char *dst = (*env)->GetStringUTFChars(env, img_, 0);
    LOGI(TAG, "%s", src);
    LOGI(TAG, "%s", dst);
    AVFormatContext *avFormatCtx = NULL;
    AVCodecContext *avCodecCtx = NULL;
    AVCodec *codec;
    AVStream *vstream = NULL;
    AVFrame *frame;
    AVPacket packet;
    int stream_index;
    int frame_count = 0;//帧计数
    struct SwsContext *img_convert_ctx;
    //1.打开多媒体文件
    if (avformat_open_input(&avFormatCtx, src, NULL, NULL) < 0) {
        LOGE(TAG, "打开多媒体文件失败。。。");
        return -1;
    }
    //2.检索流信息
    if (avformat_find_stream_info(avFormatCtx, NULL) < 0) {
        LOGE(TAG, "Not find stream info....");
        return -1;
    }
    LOGI(TAG, "找到视频流信息！");
    //3.查找多媒体文件的 视频流
    stream_index = av_find_best_stream(avFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    if (stream_index < 0) {
        LOGE(TAG, "Not find best stream.....");
        return -1;
    }
    //4.赋值 多媒体文件视频流 = 我们创建的vstream
    vstream = avFormatCtx->streams[stream_index];
    //5.查找流的解码器
    codec = avcodec_find_decoder(vstream->codecpar->codec_id);
    if (!codec) {
        LOGE(TAG, "Not find decoder...");
        return -1;
    }
    //6.初始化 AVCodecContext 上下文
    avCodecCtx = avcodec_alloc_context3(NULL);
    if (!avCodecCtx) {
        LOGE(TAG, "AVCodecContext is null");
        return -1;
    }
    //7.将编解码器参数从输入流  复制到输出编解码器上下文中
    if (avcodec_parameters_to_context(avCodecCtx, vstream->codecpar) < 0) {
        LOGE(TAG, "param to context faile....");
        return -1;
    }
    //8.打开 解码器
    if (avcodec_open2(avCodecCtx, codec, NULL) < 0) {
        LOGE(TAG, "open codec faile....");
        return -1;
    }
    img_convert_ctx = sws_getContext(avCodecCtx->width, avCodecCtx->height, avCodecCtx->pix_fmt,
                                     avCodecCtx->width, avCodecCtx->height, AV_PIX_FMT_BGR24,
                                     SWS_BICUBIC, NULL, NULL, NULL);
    if (img_convert_ctx == NULL) {
        LOGE(TAG, "Cannot initialize the conversion context");
        return -1;
    }
    //9.初始化 视频帧Frame
    frame = av_frame_alloc();
    if (!frame) {
        LOGE(TAG, "Could not allocate video frame");
        return -1;
    }
    //初始化包
    av_init_packet(&packet);
    //10.读取多媒体文件 包数据
    while (av_read_frame(avFormatCtx, &packet) >= 0) {
        /**
            * 注1:一些解码器是基于流的(mpegvideo, mpegaudio)这是使用它们的唯一方法因为你不能
            * 在分析数据之前了解压缩数据的大小。但是其他一些编解码器(msmpeg4、mpeg4)是固有的帧
            * 因此，您必须使用其中一个的所有数据调用它们框架。您还必须初始化“宽度”和“高度”在初始化它们之前。*/
        /* 注2:一些解码器允许原始参数(帧大小，采样率)在任何帧都可以改变。我们处理这个你也应该照顾它*/
        if (packet.stream_index == stream_index) {
            if (decode_write_frame(dst, avCodecCtx, img_convert_ctx, frame, &frame_count, &packet, 0) < 0) {
                return -1;
            }
        }
        av_packet_unref(&packet);
    }
    /**
     * 一些编解码器，如MPEG，用a来传输i-和p -帧一帧的延迟。你必须做以下事情才能有a有机会得到视频的最后一帧。
     */
    packet.data = NULL;
    packet.size = 0;
    decode_write_frame(dst, avCodecCtx, img_convert_ctx, frame, &frame_count, &packet, 1);
    avformat_close_input(&avFormatCtx);
    sws_freeContext(img_convert_ctx);
    avcodec_free_context(&avCodecCtx);
    av_frame_free(&frame);
    return 0;
}