package com.jieli.healthaide.tool.aiui.rcsp;

/**
 * @ClassName: AIDialListener
 * @Description: AI表盘事件监听
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/9/27 20:31
 */
public class AIDialListener {
    /**
     * AI表盘UI变化
     *
     * @param state 0x01:进入AI表盘功能 0x00:退出AI表盘功能
     */
    public void onDevNotifyAIDialUIChange(int state) {
    }

    /**
     * 开始生成AI表盘
     */
    public void onGenerateDial() {
    }

    /**
     * 重新录音
     */
    public void onRecordingAgain() {
    }
    /**
     * 开始传输缩略图
     */
    public void onTransferThumbStart() {
    }
    /**
     * 传输缩略图结束
     */
    public void onTransferThumbFinish(boolean isSuccess) {
    }

    /**
     * 开始安装表盘
     */
    public void onInstallDialStart() {
    }
    /**
     * 安装表盘结束
     */
    public void onInstallDialFinish(boolean isSuccess) {
    }
    /**
     * 重新生成表盘
     */
    public void onReGenerateDial() {
    }
}
