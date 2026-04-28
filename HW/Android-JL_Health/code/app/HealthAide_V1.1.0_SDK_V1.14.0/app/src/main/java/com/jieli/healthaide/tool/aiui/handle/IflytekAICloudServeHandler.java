package com.jieli.healthaide.tool.aiui.handle;

import android.content.Context;

import com.iflytek.sparkchain.core.LLMResult;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.aiui.chat.SessionInfo;
import com.jieli.healthaide.tool.aiui.iflytek.BasicWrapper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekChatWrapper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekTtsWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AICloudServeWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AIRecordWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @ClassName: IflytekAICloudServeHandler
 * @Description: 科大讯飞AI云服务处理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/13 14:44
 */
public class IflytekAICloudServeHandler extends BaseAICloudServeHandler {
    private final IflytekChatWrapper mIflytekChatWrapper;
    private final IflytekTtsWrapper mIflytekTtsWrapper;

    public IflytekAICloudServeHandler(IflytekChatWrapper iflytekChatWrapper,
                                      IflytekTtsWrapper iflytekTtsWrapper,
                                      AICloudServeWrapper aiCloudServeWrapper,
                                      AIRecordWrapper aiRecordWrapper,
                                      WatchManager rcspOp, Context context) {
        super(aiCloudServeWrapper, aiRecordWrapper, rcspOp, context);
        mIflytekChatWrapper = iflytekChatWrapper;
        mIflytekTtsWrapper = iflytekTtsWrapper;
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    void onStopTTS() {
        JL_Log.d(TAG, "onStopTTS", "");
        mIflytekTtsWrapper.stop();
    }

    @Override
    void onStartTTS(String ttsText) {
        mIflytekTtsWrapper.execute(ttsText, result -> {
            switch (result.getState()) {
                case BasicWrapper.STATUS_WORKING: {
                    if (isExitsAIChatUI()) {
                        onStopTTS();
                    }
                    break;
                }
                case BasicWrapper.STATUS_FINISH: {
                    if (result.isSuccess()) {
                        onPlayTTSStop();
                    } else {
                        onPlayTTSFail();
                    }
                    break;
                }
            }
        });
    }

    @Override
    void onStartChat(String userText) {
        mIflytekChatWrapper.execute(userText, stateResult -> {
            switch (stateResult.getState()) {
                case BasicWrapper.STATUS_WORKING: {
                    final LLMResult result = stateResult.getResult();
                    if (result == null) return;
                    if (result.getStatus() == 0) {
                        onStartChat();
                    }
                    break;
                }
                case BasicWrapper.STATUS_FINISH: {
                    if (stateResult.isSuccess()) {
                        String content = stateResult.getMessage();
                        if (null == content || content.isEmpty()) return;
                        addNlpTTSMessage(content.replaceAll("\n", ""));
                        return;
                    }
                    addNlpTTSMessage(mContext.getString(R.string.ai_no_response));
                    break;
                }
            }
        });
    }

    @Override
    void onHandlerSessionStatus(int status) {
        switch (status) {
            case SessionInfo.STATE_FAIL://异常
            case SessionInfo.STATE_RECORD_START://开始录音
            case SessionInfo.STATE_NLP_END://语义识别结束

                break;
        }
    }
}
