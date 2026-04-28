package com.jieli.healthaide.tool.aiui.handle;

import android.content.Context;

import com.jieli.healthaide.tool.watch.WatchManager;

/**
 * @ClassName: BaseAIChatHandler
 * @Description: AI对话的第二个环节-AI文本交互
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/9/19 15:02
 */
public abstract class BaseAIChatHandler extends BaseAIHandler {

    public BaseAIChatHandler(WatchManager rcspOp, Context context) {
        super(rcspOp, context);
    }

    /**
     * 是否退出AI界面
     */
    boolean isExitsAIChatUI() {
        return false;
    }

    /**
     * 语音识别异常-网络异常
     */
    void onIatNetworkError() {
    }

    /**
     * 语音识别异常-识别结果为空
     */
    void onIatRecognizeEmptyError() {
    }

    /**
     * 处理语音识别文本
     */
    abstract void onIatText(String iatText);

    /**
     * 语音识别失败
     */
    void onIatFail() {
    }

    /**
     * 语音识别开始录音
     */
    void onIatStartRecord() {
    }

    /**
     * 语音识别录音中
     */
    void onIatRecording() {
    }

    /**
     * 语音识别录音结束
     */
    void onIatStopRecord() {
    }
}
