package com.jieli.healthaide.demos;

import android.content.Context;

import androidx.lifecycle.Observer;

import com.jieli.healthaide.data.entity.AICloudMessageEntity;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.aiui.chat.SessionInfo;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.aicloud.AICloudMessage;

import org.junit.Test;

import java.util.List;

/**
 * @ClassName: AICloudServerDemo
 * @Description: AI云服务功能示例
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/12/28 11:23
 */
public class AICloudServerDemo {
    private AIManager mAIManager;
    private final Observer<SessionInfo> mRecordStatusObserver = sessionInfo -> {
        if (sessionInfo != null) {
            switch (sessionInfo.getStatus()) {
                case SessionInfo.STATE_RECORD_START://开始录音
                case SessionInfo.STATE_RECORDING://录音中
                case SessionInfo.STATE_IDLE://默认状态
                case SessionInfo.STATE_RECORD_END://录音结束
                case SessionInfo.STATE_IAT_END://语音识别结束
                case SessionInfo.STATE_NLP_END://语义识别结束
                case SessionInfo.STATE_FAIL://异常
                    //获取当前对话消息
                    List<AICloudMessage>  aiCloudMessageList=  sessionInfo.getSessionMessageList();
                    for (AICloudMessage aiCloudMessage:aiCloudMessageList) {
                        //获取对话的类型。0;用户，1;AI
                        int itemType =  aiCloudMessage.getItemType();
                        //获取消息的数据实体
                        AICloudMessageEntity entity =  aiCloudMessage.getEntity();
                        entity.getAiCloudState();//此消息的语音识别状态
                        entity.getDevMac();//设备mac
                        entity.getId();//消息的唯一id
                        entity.getRevId();//消息对应的回复消息的id
                        entity.getRole();//消息的角色，0;用户，1;AI
                        entity.getText();//消息的文本
                        entity.getTime();//消息的时间
                        entity.getUid();//用户的唯一id
                    }
                    break;
            }
        }
    };
    /**
     * 初始化AIManager
     */
    @Test
    public void init(Context context){
        //Step1. 初始化
        AIManager.init(context, WatchManager.getInstance());
        mAIManager = AIManager.getInstance();
        //Step2. 监听对话状态。请根据使用场景选择observeForever或observe
        mAIManager.getAICloudServe().currentSessionMessageMLD.observeForever(mRecordStatusObserver);
    }

    /**
     * 获取录音开始时间
     */
    @Test
    public long getStartRecordTime(){
        return mAIManager.getAICloudServe().getStartRecordTime();
    }


    /**
     * 停止语音合成
     */
    @Test
    public void stopTTS(){
        mAIManager.getAICloudServe().stopTTS();
    }
}
