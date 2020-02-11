//
// Created by Antony on 2020/2/10.
//

#ifndef ANTONYCFAV_ENCODE_MP4_H
#define ANTONYCFAV_ENCODE_MP4_H


#include "encode_video.h"

extern "C" {
#endif
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#ifdef __cplusplus
};
#endif

class MP4Encoder : public VideoEncoder {
private:
    const char *mpePath;
    int width;
    int height;
    AVPacket avPacket;
    AVFormatContext *pFormatCtx = NULL;
    AVStream *pStream = NULL;
    AVCodecContext *pCodecCtx = NULL;
    AVCodec *pCodec = NULL;
    uint8_t *pFrameBuffer = NULL;
    AVFrame *pFrame = NULL;

    //AVFrame PTS
    int index = 0;

    int EncodeFrame(AVCodecContext *pCodecCtx, AVFrame *pFrame, AVPacket *pPkt);

public:
    void InitEncoder(const char *mp4Path, int width, int height);

    void EncodeStart();

    void EncodeBuffer(unsigned char *nv21Buffer);

    void EncodeStop();
};


#endif //ANTONYCFAV_ENCODE_MP4_H
