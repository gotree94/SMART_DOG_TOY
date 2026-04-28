package com.jieli.healthaide.tool.aiui.rcsp;

/**
 * @ClassName: AICloudServeWrapperListenner
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 17:16
 */
public interface AICloudServeWrapperListener {
    /**
     * tts语音合成
     */
    void onTTS(String ttsText);

    /**
     * AI云服务UI变化
     *
     * @param state 0x00:默认状态 0x01:进入AI云服务功能 0x02:退出AI云服务功能
     */
    void onDevNotifyAIDialUIChange(int state);
}
