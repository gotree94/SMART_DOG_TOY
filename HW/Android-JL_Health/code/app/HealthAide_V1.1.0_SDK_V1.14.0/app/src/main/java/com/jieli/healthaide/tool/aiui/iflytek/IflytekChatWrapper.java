package com.jieli.healthaide.tool.aiui.iflytek;

import android.content.Context;

import androidx.core.util.Consumer;

import com.iflytek.sparkchain.core.LLM;
import com.iflytek.sparkchain.core.LLMCallbacks;
import com.iflytek.sparkchain.core.LLMConfig;
import com.iflytek.sparkchain.core.LLMError;
import com.iflytek.sparkchain.core.LLMEvent;
import com.iflytek.sparkchain.core.LLMFactory;
import com.iflytek.sparkchain.core.LLMResult;
import com.iflytek.sparkchain.core.Memory;
import com.iflytek.sparkchain.utils.constants.ErrorCode;
import com.jieli.healthaide.tool.aiui.model.OpResult;
import com.jieli.healthaide.tool.aiui.model.StateResult;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @ClassName: IflytekChatWrapper
 * @Description: 科大讯飞AI聊天功能实现
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 19:36
 */
public class IflytekChatWrapper extends BasicWrapper<String, LLMResult> {
    /**
     * AI聊天管理器
     */
    private final LLM mLLM;
    /**
     * 缓存内容
     */
    private final StringBuilder mStringBuilder;

    public IflytekChatWrapper(Context context) throws RuntimeException {
        super(context);
        LLMConfig config = LLMConfig.builder()
                .domain("lite") //"generalv3.5" //"general" 已弃用，改成 “lite” + "wss://spark-api.xf-yun.com/v1.1/chat"
                .url("wss://spark-api.xf-yun.com/v1.1/chat")
                .auditing("default")
                .maxToken(1024);
        Memory memory = Memory.tokenMemory(1024);
        mLLM = LLMFactory.textGeneration(config, memory);
        mStringBuilder = new StringBuilder();
        mLLM.registerLLMCallbacks(new LLMCallbacks() {
            @Override
            public void onLLMResult(LLMResult llmResult, Object userTag) {
                if (!isSameTag(userTag)) return;
                handleLLMResult(llmResult);
            }

            @Override
            public void onLLMEvent(LLMEvent llmEvent, Object userTag) {
                if (!isSameTag(userTag) || null == llmEvent) return;
                int eventId = llmEvent.getEventID();//获取事件ID
                String eventMsg = llmEvent.getEventMsg();//获取事件信息
                String sid = llmEvent.getSid();//本次交互的sid
                JL_Log.d(tag, "onLLMEvent", "EventID : " + eventId + ", " + eventMsg);
            }

            @Override
            public void onLLMError(LLMError llmError, Object userTag) {
                if (!isSameTag(userTag)) return;
                handleLLMError(llmError);
            }
        });
    }

    @Override
    public void destroy() {
        stop();
        mLLM.registerLLMCallbacks(null);
        super.destroy();
    }

    @Override
    public int getType() {
        return FUNCTION_AI_CHAT;
    }

    @Override
    public boolean isRunning() {
        return mStatus == STATUS_WORKING;
    }

    @Override
    public void execute(String input, Consumer<StateResult<LLMResult>> callback) {
        if (isRunning()) { //如果还在交互中，结束这次交互
            stop();
        }
        setCallback(callback);
        mStringBuilder.setLength(0); //清空缓存
        int userTag = autoIncUserTag();
        JL_Log.d(tag, "execute", "Input : " + input + ", userTag : " + userTag);
        callbackStart();
        int ret = mLLM.arun(input, userTag);
        if (ret != 0) {
            callbackFinish("execute", ret, "Operation failed. code : " + ret, null);
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) return;
        mLLM.stop();
        callbackFinish("stop", ErrorCode.MSP_ERROR_USER_CANCELLED, "User cancels operation.", null);
    }

    @Override
    public void cancel() {
        stop();
    }

    private void handleLLMResult(LLMResult result) {
        if (!isRunning() || null == result) return;
        //解析获取的交互结果，示例展示所有结果获取，开发者可根据自身需要，选择获取。
        String content = result.getContent();//获取交互结果
        int status = result.getStatus();//返回结果状态， 0：start，1：continue，2：end
        String role = result.getRole();//获取角色信息
        String sid = result.getSid();//本次交互的sid
        String rawResult = result.getRaw();//星火大模型原始输出结果。要求SDK1.1.5版本以后才能使用
        int completionTokens = result.getCompletionTokens();//获取回答的Token大小
        int promptTokens = result.getPromptTokens();//包含历史问题的总Tokens大小
        int totalTokens = result.getTotalTokens();//promptTokens和completionTokens的和，也是本次交互计费的Tokens大小

        mStringBuilder.append(content);
        if (status == 2) {
            callbackFinish("handleLLMResult", OpResult.ERR_NONE, mStringBuilder.toString(), result);
        } else {
            callbackWorking("handleLLMResult", 0f, result);
        }
    }

    private void handleLLMError(LLMError error) {
        if (!isRunning() || null == error) return;
        int errCode = error.getErrCode();//返回错误码
        String errMsg = error.getErrMsg();//获取错误信息
        String sid = error.getSid();//本次交互的sid

        callbackFinish("handleLLMError", errCode, errMsg, null);
    }

}
