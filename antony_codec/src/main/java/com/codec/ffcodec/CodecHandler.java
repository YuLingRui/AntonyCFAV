package com.codec.ffcodec;

import android.os.Handler;
import android.util.Log;

public class CodecHandler {

    private final static String TAG = CodecHandler.class.getSimpleName();

    public final static int MSG_BEGIN = 9012; //开始
    public final static int MSG_END = 2019; //结束
    public final static int MSG_CONTINUE = 1118;//继续
    public final static int MSG_TOAST = 4562;//提示

    private Handler mHandler;
    private boolean isContinue = false;

    public void isContinue(boolean aContinue) {
        this.isContinue = aContinue;
    }

    public CodecHandler(Handler handler) {
        this.mHandler = handler;
    }

    //编码
    public void executeFFCmdEncode(String[] commands) {
        if (commands == null) {
            return;
        }
        //子线程中执行 FFmpeg命令行语句
        FFcodecNative.executeEncode(commands, new CodecHandleListener() {
            @Override
            public void onBegin() {
                Log.i("FFcodecNative", "handle onBegin...");
                mHandler.obtainMessage(MSG_BEGIN, null).sendToTarget();
            }

            @Override
            public void onEnd(int result) {
                if (isContinue) {
                    Log.i("FFcodecNative", "handle onContinue... " + result);
                    mHandler.obtainMessage(MSG_CONTINUE, result).sendToTarget();
                } else {
                    Log.i("FFcodecNative", "handle onEnd... " + result);
                    mHandler.obtainMessage(MSG_END, result).sendToTarget();
                }
            }
        });
    }

    //解码
    public void executeFFCmdDecode(String[] commands) {
        if (commands == null) {
            return;
        }
        //子线程中执行 FFmpeg命令行语句
        FFcodecNative.executeDecode(commands, new CodecHandleListener() {
            @Override
            public void onBegin() {
                Log.i("FFcodecNative", "handle onBegin...");
                mHandler.obtainMessage(MSG_BEGIN, null).sendToTarget();
            }

            @Override
            public void onEnd(int result) {
                if (isContinue) {
                    Log.i("FFcodecNative", "handle onContinue... " + result);
                    mHandler.obtainMessage(MSG_CONTINUE, result).sendToTarget();
                } else {
                    Log.i("FFcodecNative", "handle onEnd... " + result);
                    mHandler.obtainMessage(MSG_END, result).sendToTarget();
                }
            }
        });
    }
}
