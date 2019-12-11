package com.antony.cfav.listener;

/**
 * 流程执行监听器
 */
public interface OnHandleListener {
    void onBegin();

    void onEnd(int result);
}
