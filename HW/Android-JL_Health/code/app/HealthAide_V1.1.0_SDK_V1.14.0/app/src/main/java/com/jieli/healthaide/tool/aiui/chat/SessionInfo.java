package com.jieli.healthaide.tool.aiui.chat;

import com.jieli.healthaide.ui.device.aicloud.AICloudMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName: SessionInfo
 * @Description: 对话内容信息
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/8/9 11:18
 */
public class SessionInfo {
    public static final int STATE_FAIL = -1;//异常
    public static final int STATE_IDLE = 0;//默认状态
    public static final int STATE_RECORD_START = 1;//开始录音
    public static final int STATE_RECORDING = 2;//录音中
    public static final int STATE_RECORD_END = 3;//录音结束
    public static final int STATE_IAT_END = 4;//语音识别结束
    public static final int STATE_NLP_END = 5;//语义识别结束
    private int status = 0;
    private List<AICloudMessage> sessionMessageList = new CopyOnWriteArrayList<>();

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<AICloudMessage> getSessionMessageList() {
        return sessionMessageList;
    }

    public void setSessionMessageList(List<AICloudMessage> sessionMessageList) {
        this.sessionMessageList = sessionMessageList;
    }
}
