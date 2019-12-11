
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "ffmpeg_jni_define.h"
#include "libavformat/avformat.h"  //封装格式
#include "libavcodec/avcodec.h"  //解码
#include "libswscale/swscale.h" //缩放
#include "libswresample/swresample.h" //重采样
#include <android/log.h>

/**
 * 从.mp4文件中抽取 .h264数据
 */

#ifndef AV_WB32
#   define AV_WB32(p, val) do {                 \
        uint32_t d = (val);                     \
        ((uint8_t*)(p))[3] = (d);               \
        ((uint8_t*)(p))[2] = (d)>>8;            \
        ((uint8_t*)(p))[1] = (d)>>16;           \
        ((uint8_t*)(p))[0] = (d)>>24;           \
    } while(0)
#endif

#ifndef AV_RB16
#   define AV_RB16(x)                           \
    ((((const uint8_t*)(x))[0] << 8) |          \
      ((const uint8_t*)(x))[1])
#endif

#define TAG "VideoPlayer"

/**
 * 这个函数用来增加特征码Start code
 */
static int
alloc_and_copy(AVPacket *out, const uint8_t *sps_pps, uint32_t sps_pps_size, const uint8_t *in,
               uint32_t in_size) {
    uint32_t offset = out->size;
    uint8_t nal_header_size = offset ? 3 : 4; //这里做了区分 sps pps前面的特征码4个字节00000001，其他的特征码3个字节000001
    int err;

    err = av_grow_packet(out, sps_pps_size + in_size + nal_header_size);//做了扩容
    if (err < 0) {
        return err;
    }
    if (sps_pps) {
        memcpy(out->data + offset, sps_pps, sps_pps_size);
    }
    memcpy(out->data + sps_pps_size + nal_header_size + offset, in, in_size);
    if (!offset) {
        AV_WB32(out->data + sps_pps_size, 1);
    } else {
        (out->data + offset + sps_pps_size)[0] =
        (out->data + offset + sps_pps_size)[1] = 0;
        (out->data + offset + sps_pps_size)[2] = 1;
    }
    return 0;
}

/**
 * 读取 sps pps
 */
int h264_extradata_to_annexb(const uint8_t *codec_extradata, const int codec_extradata_size,
                             AVPacket *out_extradata, int padding) {
    uint16_t unit_size;
    uint64_t total_size = 0;
    uint8_t *out = NULL, unit_nb, sps_done = 0, sps_seen = 0, pps_seen = 0, sps_offset = 0, pps_offset = 0;
    const uint8_t *extradata = codec_extradata + 4;
    static const uint8_t nalu_header[4] = {0, 0, 0, 1};
    int length_size = (*extradata++ & 0x3) + 1; // retrieve length coded size, 用于指示表示编码数据长度所需字节数

    sps_offset = pps_offset = -1;

    /*检索sps和pps单元 */
    unit_nb = *extradata++ & 0x1f; /* number of sps unit(s) */
    if (!unit_nb) {
        goto pps;
    } else {
        sps_offset = 0;
        sps_seen = 1;
    }

    while (unit_nb--) {
        int err;
        unit_size = AV_RB16(extradata);
        total_size += unit_size + 4;
        if (total_size > INT_MAX - padding) {
            av_log(NULL, AV_LOG_ERROR,
                   "Too big extradata size, corrupted stream or invalid MP4/AVCC bitstream\n");
            av_free(out);
            return AVERROR(EINVAL);
        }
        if (extradata + 2 + unit_size > codec_extradata + codec_extradata_size) {
            av_log(NULL, AV_LOG_ERROR,
                   "Packet header is not contained in global extradata,""corrupted stream or invalid MP4/AVCC bitstream\n");
            av_free(out);
            return AVERROR(EINVAL);
        }
        if ((err = av_reallocp(&out, total_size + padding)) < 0) {
            return err;
        }
        memcpy(out + total_size - unit_size - 4, nalu_header, 4);
        memcpy(out + total_size - unit_size, extradata + 2, unit_size);
        extradata += 2 + unit_size;
        pps:
        if (!unit_nb && !sps_done++) {
            unit_nb = *extradata++; /*number of pps unit(s) */
            if (unit_nb) {
                pps_offset = total_size;
                pps_seen = 1;
            }
        }
    }
    if (out) {
        memset(out + total_size, 0, padding);
    }
    if (!sps_seen) {
        av_log(NULL, AV_LOG_WARNING,
               "Warning: SPS NALU missing or invalid. " "The resulting stream may not play.\n");
    }
    if (!pps_seen) {
        av_log(NULL, AV_LOG_WARNING,
               "Warning: PPS NALU missing or invalid. ""The resulting stream may not play.\n");
    }
    out_extradata->data = out;
    out_extradata->size = total_size;
    return length_size;
}

int h264_mp4toannexb(AVFormatContext *fmt_ctx, AVPacket *in, FILE *dst_fd) {
    AVPacket *out = NULL;
    AVPacket spspps_pkt; // sps pps 数据集包
    int len;
    uint8_t unit_type;
    int32_t nal_size;
    uint32_t cumul_size = 0;
    const uint8_t *buf;
    const uint8_t *buf_end;
    int buf_size;
    int ret = 0, i;

    buf = in->data; // 这个包的真实数据
    buf_size = in->size; //包数据大小
    buf_end = in->data + in->size;//包的末尾【data的地址，向后移了size】
    out = av_packet_alloc(); //创建packet包

    do {
        ret = AVERROR(EINVAL);
        if (buf + 4 > buf_end) {
            goto fail;
        }
        for (nal_size = 0, i = 0; i < 4; i++) {
            nal_size = (nal_size << 8) | buf[i];
        }
        buf += 4; /*s->length_size;*/
        unit_type = *buf & 0x1f;
        if (nal_size > buf_end - buf || nal_size < 0) {
            goto fail;
        }
        // NAL单元类型 == 5 表示它是一个“关键帧”
        if (unit_type == 5) {
            //对于 “关键帧” 来说  它要有  SPS  PPS 这类额外的扩展数据
            //从 “扩展数据” 中 获取 sps pps 【h264_extradata_to_annexb()函数】
            h264_extradata_to_annexb(fmt_ctx->streams[in->stream_index]->codec->extradata,
                                     fmt_ctx->streams[in->stream_index]->codec->extradata_size,
                                     &spspps_pkt,
                                     AV_INPUT_BUFFER_PADDING_SIZE);
            //增加 特征码 “Start code特征码区分帧与帧之间的间隔” 【alloc_and_copy()函数】
            if ((ret = alloc_and_copy(out, spspps_pkt.data, spspps_pkt.size, buf, nal_size)) < 0) {
                goto fail;
            }
        } else {
            if ((ret = alloc_and_copy(out, NULL, 0, buf, nal_size)) < 0) {
                goto fail;
            }
        }
        //有了 sps pps 特征码，我们把组织好的这些数据  写入到目标文件中去。
        len = fwrite(out->data, 1, out->size, dst_fd);
        if (len != out->size) {
            av_log(NULL, AV_LOG_DEBUG,
                   "warning, length of writed data isn't equal pkt.size(%d, %d)\n", len, out->size);
        }
        //将所有的数据 flush 到我们的目标文中去 [注意:我们fwrite写的时候，是写到了缓冲里，并没有真正的写到目标文件里,所以我们要flush一下]
        fflush(dst_fd);
        next_nal:
        buf += nal_size;
        cumul_size += nal_size + 4;//s->length_size;
    } while (cumul_size < buf_size);
    fail:
    av_packet_free(&out);
    return ret;
}


JNIEXPORT void JNICALL
Java_com_antony_cfav_activity_video_VideoPlayer_extractVideoH264(JNIEnv *env, jobject instance,
                                                                 jstring src_, jstring dst_) {
    const char *src = (*env)->GetStringUTFChars(env, src_, 0);
    const char *dst = (*env)->GetStringUTFChars(env, dst_, 0);
    LOGI(TAG, "src= %s", src);
    LOGI(TAG, "dst= %s", dst);
    AVFormatContext *avFormatCtx = NULL;
    AVPacket pkt;
    FILE *dst_fd = NULL;
    int video_stream_index = -1;
    av_register_all();
    //打开多媒体文件， 记得结尾要关闭
    if (avformat_open_input(&avFormatCtx, src, NULL, NULL) < 0) {
        LOGE(TAG, "avformat_open_input  error.....");
        return;
    }
    LOGI(TAG, "avformat open input success");
    //打开 “目标文件”， 结尾要关掉
    dst_fd = fopen(dst, "wd");
    if (!dst_fd) {
        LOGE(TAG, "dst_fd  is null.....");
        avformat_close_input(&avFormatCtx);
        return;
    }
    LOGI(TAG, "fopen dst success");
    //初始化数据包
    av_init_packet(&pkt);
    pkt.data = NULL;
    pkt.size = 0;
    //从打开的多媒体文件中， 找到 “视频” 流 得到这个流的index
    video_stream_index = av_find_best_stream(avFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    LOGI(TAG, "video_stream_index= %d", video_stream_index);
    if (video_stream_index < 0) {
        avformat_close_input(&avFormatCtx);
        fclose(dst_fd);
        return;
    }
    //读取多媒体文件的 每一个数据包packet
    while (av_read_frame(avFormatCtx, &pkt) >= 0) {
        //判断  数据包中流的index ==  我们多媒体文件“视频”流index
        if (video_stream_index == pkt.stream_index) {
            //设置 Start code 特征码区分帧与帧之间的间隔， 找出每一帧。
            //SPS[序列参数集] / PPS[图像参数集]   解码的一些视频参数
            int xb = h264_mp4toannexb(avFormatCtx, &pkt, dst_fd);
            LOGI(TAG, "XB= %d", xb);
        }
        //处理完一个包， 我们就把这个包的“引用” 清除掉
        av_packet_unref(&pkt);
    }
    fclose(dst_fd);
    avformat_close_input(&avFormatCtx);
}