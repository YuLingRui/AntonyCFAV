package com.codec.ffcodec;

public interface CodecHandleListener {

    void onBegin();

    void onEnd(int result);
}
