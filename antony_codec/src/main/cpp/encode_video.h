//
// Created by Antony on 2020/2/10.
//

#ifndef ANTONYCFAV_ENCODE_VIDEO_H
#define ANTONYCFAV_ENCODE_VIDEO_H


class VideoEncoder {
protected:
    bool transform = false;
public:
    virtual void InitEncoder(const char *mp4Path, int width, int height) = 0;

    virtual void EncodeStart() = 0;

    virtual void EncodeBuffer(unsigned char *nv21Buffer) = 0;

    virtual void EncodeStop() = 0;

    bool isTransform();

};


#endif //ANTONYCFAV_ENCODE_VIDEO_H
