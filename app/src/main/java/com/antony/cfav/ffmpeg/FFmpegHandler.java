package com.antony.cfav.ffmpeg;

import android.os.Handler;
import android.util.Log;

import com.antony.cfav.listener.OnHandleListener;

public class FFmpegHandler {

    private final static String TAG = FFmpegHandler.class.getSimpleName();

    public final static int MSG_BEGIN = 9012; //开始
    public final static int MSG_END = 2019; //结束
    public final static int MSG_CONTINUE = 1118;//继续
    public final static int MSG_TOAST = 4562;//提示

    private Handler mHandler;
    private boolean isContinue = false;

    public void isContinue(boolean aContinue) {
        this.isContinue = aContinue;
    }

    public FFmpegHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void executeFFmpegCmd(String[] commands) {
        if (commands == null) {
            return;
        }
        //子线程中执行 FFmpeg命令行语句
        FFmpegCmd.execute(commands, new OnHandleListener() {
            @Override
            public void onBegin() {
                Log.i(TAG, "handle onBegin...");
                mHandler.obtainMessage(MSG_BEGIN, null).sendToTarget();
            }

            @Override
            public void onEnd(int result) {
                if (isContinue) {
                    Log.i(TAG, "handle onContinue...");
                    mHandler.obtainMessage(MSG_CONTINUE, result).sendToTarget();
                } else {
                    Log.i(TAG, "handle onEnd...");
                    mHandler.obtainMessage(MSG_END, result).sendToTarget();
                }
            }
        });
    }
}
