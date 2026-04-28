package com.jieli.healthaide.tool.aiui.handle;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.dao.AICloudMessageDao;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.AICloudMessageEntity;
import com.jieli.healthaide.tool.aiui.chat.SessionInfo;
import com.jieli.healthaide.tool.aiui.rcsp.AICloudServeWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AICloudServeWrapperListener;
import com.jieli.healthaide.tool.aiui.rcsp.AIRecordWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.aicloud.AICloudMessage;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.util.Calendar;
import java.util.List;

import static com.jieli.healthaide.data.entity.AICloudMessageEntity.AI_STATE_IAT_END;
import static com.jieli.healthaide.data.entity.AICloudMessageEntity.AI_STATE_IAT_ING;
import static com.jieli.healthaide.data.entity.AICloudMessageEntity.AI_STATE_NLP_END;
import static com.jieli.healthaide.data.entity.AICloudMessageEntity.AI_STATE_RECORDING;
import static com.jieli.healthaide.data.entity.AICloudMessageEntity.ROLE_AI;
import static com.jieli.healthaide.data.entity.AICloudMessageEntity.ROLE_USER;

/**
 * @ClassName: BaseAICloudServeHandler
 * @Description: 基础的AI云服务处理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/13 14:40
 */
public abstract class BaseAICloudServeHandler extends BaseAIChatHandler {
    public MutableLiveData<SessionInfo> currentSessionMessageMLD = new MutableLiveData<>(null);
    private final int MSG_IDLE = 201;
    private final int MSG_RECORD_TIME_OUT = 202;
    private final int MSG_WAIT_NLP_TIME_OUT = 203;
    protected AICloudServeWrapper mAICloudServeWrapper;
    protected AIRecordWrapper mAIRecordWrapper;
    private boolean isExitsUI = false;
    private String mIatText;
    private String mNlpText;
    private SessionInfo mSessionInfo;
    private long startRecordTime = 0;
    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            switch (status) {
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    if (RcspUtil.deviceEquals(device, WatchManager.getInstance().getTargetDevice())) {
                        JL_Log.e(TAG, "onConnectStateChange", "蓝牙断开");
                        stopTTS();
                        if (mSessionInfo != null && (mSessionInfo.getStatus() == SessionInfo.STATE_RECORD_START
                                || mSessionInfo.getStatus() == SessionInfo.STATE_RECORDING
                                || mSessionInfo.getStatus() == SessionInfo.STATE_RECORD_END
                                || mSessionInfo.getStatus() == SessionInfo.STATE_IAT_END
                        )) {
                            onSessionStatus(SessionInfo.STATE_FAIL);
                        }
                    }
                    break;
            }
        }
    };
    private final AICloudServeWrapperListener mAICloudServeWrapperListener = new AICloudServeWrapperListener() {
        @Override
        public void onTTS(String ttsText) {

        }

        @Override
        public void onDevNotifyAIDialUIChange(int state) {
            if (state == 0x01) {//进入AI云服务界面
                mAICloudServeWrapper.setEnable(true);
            } else if (state == 0x02) {//退出AI云服务界面
                mAICloudServeWrapper.setEnable(false);
                stopTTS();
                isExitsUI = true;
                mAICloudServeWrapper.cancelAsyncMessage();
                if (mSessionInfo != null && (mSessionInfo.getStatus() == SessionInfo.STATE_RECORD_START
                        || mSessionInfo.getStatus() == SessionInfo.STATE_RECORDING
                        || mSessionInfo.getStatus() == SessionInfo.STATE_RECORD_END
                        || mSessionInfo.getStatus() == SessionInfo.STATE_IAT_END
                )) {
                    onSessionStatus(SessionInfo.STATE_FAIL);
                }
            }
        }
    };
    private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_IDLE:
                    onSessionStatus(SessionInfo.STATE_IDLE);//置状态到默认
                    break;
                case MSG_RECORD_TIME_OUT:
                    onSessionStatus(SessionInfo.STATE_FAIL);//置状态到默认
                    break;
                case MSG_WAIT_NLP_TIME_OUT:
                    addNlpTTSMessage(mContext.getString(R.string.ai_no_response));
                    break;
            }
        }
    };

    public BaseAICloudServeHandler(AICloudServeWrapper aiCloudServeWrapper, AIRecordWrapper aiRecordWrapper, WatchManager rcspOp, Context context) {
        super(rcspOp, context);
        mAICloudServeWrapper = aiCloudServeWrapper;
        mAIRecordWrapper = aiRecordWrapper;
        mAICloudServeWrapper.registerListener(mAICloudServeWrapperListener);
        mRcspOp.registerOnWatchCallback(mWatchCallback);
    }

    @Override
    public void release() {
        super.release();
        mAICloudServeWrapper.unregisterListener(mAICloudServeWrapperListener);
        mRcspOp.unregisterOnWatchCallback(mWatchCallback);
    }

    /**
     * 开始合成
     *
     * @param text 合成文本
     */
    public void startTTS(String text) {
        if (!isConnected()) return;
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mAICloudServeWrapper.notifyDevTTSStart();
        JL_Log.d(TAG, "startTTS", text);
        onStartTTS(text);
    }

    /**
     * 停止合成播放
     */
    public void stopTTS() {
        onStopTTS();
    }

    @Override
    boolean isExitsAIChatUI() {
        return isExitsUI;
    }

    @Override
    void onIatNetworkError() {
        String error = mContext.getString(R.string.ai_network_wrong);
//        if (mAIRecordWrapper.isNeedAsyncNlp()) {
        mAICloudServeWrapper.asyncMessageAIError(error, null);
//        }
        ToastUtil.showToastLong(error);
    }

    @Override
    void onIatRecognizeEmptyError() {
        String error = mContext.getString(R.string.ai_no_speak);
//        if (mAIRecordWrapper.isNeedAsyncNlp()) {
        mAICloudServeWrapper.asyncMessageAIError(error, null);
//        }
        ToastUtil.showToastLong(error);
    }

    @Override
    void onIatText(String iatText) {
        String finalTempIatString = iatText;
        if (mAIRecordWrapper.isNeedAsyncIat()) {
            mAICloudServeWrapper.asyncMessageIat(iatText, new OnDataEventCallback() {
                @Override
                public void onBegin(int way) {

                }

                @Override
                public void onProgress(float progress) {

                }

                @Override
                public void onStop(int type, byte[] data) {
                    if (!isExitsAIChatUI()) {
                        mIatText = finalTempIatString;
                        onSessionStatus(SessionInfo.STATE_IAT_END);
                    }
                }

                @Override
                public void onError(BaseError error) {

                }
            });
        } else {
            if (!isExitsAIChatUI()) {
                mIatText = finalTempIatString;
                onSessionStatus(SessionInfo.STATE_IAT_END);
            }
        }
        mUIHandler.removeMessages(MSG_WAIT_NLP_TIME_OUT);
        mUIHandler.sendEmptyMessageDelayed(MSG_WAIT_NLP_TIME_OUT, 10000);
//        mAIServeWrapper.startChat(iatText, mCurrentAIContext);
        onStartChat(iatText);
    }

    @Override
    void onIatFail() {
        onSessionStatus(SessionInfo.STATE_FAIL);
    }

    @Override
    void onIatStartRecord() {
        isExitsUI = false;
        stopTTS();
        onSessionStatus(SessionInfo.STATE_RECORD_START);
    }

    @Override
    void onIatRecording() {
        onSessionStatus(SessionInfo.STATE_RECORDING);
    }

    @Override
    void onIatStopRecord() {
        onSessionStatus(SessionInfo.STATE_RECORD_END);
    }

    public long getStartRecordTime() {
        return startRecordTime;
    }

    protected void addNlpTTSMessage(String nlpText) {
        if (!isConnected() || isExitsAIChatUI()) return;
        String tempNlpText = nlpText;
        if (nlpText.length() > 256) {
            tempNlpText = nlpText.substring(0, 256);
            tempNlpText += "...";
        }
        String finalTempNlpText = tempNlpText;
        if (mAIRecordWrapper.isNeedAsyncNlp()) {
            mAICloudServeWrapper.asyncMessageNlp(tempNlpText, new OnDataEventCallback() {
                @Override
                public void onBegin(int way) {

                }

                @Override
                public void onProgress(float progress) {

                }

                @Override
                public void onStop(int type, byte[] data) {
                    mNlpText = finalTempNlpText;
                    if (!isExitsAIChatUI()) {
                        onSessionStatus(SessionInfo.STATE_NLP_END);
                        if (mAIRecordWrapper.isNeedPlayTTS()) {
                            startTTS(nlpText);
                        }
                    }
                }

                @Override
                public void onError(BaseError error) {

                }
            });
        } else {
            mNlpText = finalTempNlpText;
            if (!isExitsAIChatUI()) {
                onSessionStatus(SessionInfo.STATE_NLP_END);
                if (mAIRecordWrapper.isNeedPlayTTS()) {
                    startTTS(nlpText);
                }
            }
        }
    }

    protected void onStartChat() {
        mUIHandler.removeMessages(MSG_WAIT_NLP_TIME_OUT);
    }

    protected void onPlayTTSFail() {
        onSessionStatus(SessionInfo.STATE_FAIL);
    }

    protected void onPlayTTSStop() {
        JL_Log.d(TAG, "onPlayTTSStop", "播放完成");
        mAICloudServeWrapper.notifyDevTTSStop();
    }

    private void onSessionStatus(int status) {//2023-08-11 18:35:02.425
        JL_Log.e(TAG, "onSessionStatus", "status : " + status);
        boolean isNeedSetValue = false;
        switch (status) {
            case SessionInfo.STATE_FAIL://异常
                mUIHandler.removeMessages(MSG_RECORD_TIME_OUT);
                mUIHandler.removeMessages(MSG_WAIT_NLP_TIME_OUT);
                mUIHandler.removeMessages(MSG_IDLE);
                mUIHandler.sendEmptyMessageDelayed(MSG_IDLE, 2000);
                isNeedSetValue = true;
                break;
            case SessionInfo.STATE_IDLE://默认状态//20230811094324.207
                startRecordTime = 0;
                mUIHandler.removeMessages(MSG_WAIT_NLP_TIME_OUT);
//                mSessionInfo = new SessionInfo();
                mUIHandler.removeMessages(MSG_IDLE);
                break;
            case SessionInfo.STATE_RECORD_START://开始录音
            {
                mUIHandler.removeMessages(MSG_WAIT_NLP_TIME_OUT);
                mUIHandler.removeMessages(MSG_IDLE);
                mUIHandler.removeMessages(MSG_RECORD_TIME_OUT);
                mUIHandler.sendEmptyMessageDelayed(MSG_RECORD_TIME_OUT, 35000);
                mAICloudServeWrapper.cancelAsyncMessage();
                mSessionInfo = new SessionInfo();
                isExitsUI = false;
                List<AICloudMessage> messageList = mSessionInfo.getSessionMessageList();
                AICloudMessage aiCloudMessage = new AICloudMessage();
                AICloudMessageEntity entity = new AICloudMessageEntity();
                String uid = HealthApplication.getAppViewModel().getUid();
                long time = Calendar.getInstance().getTimeInMillis();
                startRecordTime = time;
                entity.setUid(uid);
                entity.setTime(time);
                entity.setDevMac("11:22:33:44:55:66");
                entity.setRole(ROLE_USER);
                entity.setAiCloudState(AI_STATE_RECORDING);
                aiCloudMessage.setEntity(entity);
                messageList.add(aiCloudMessage);
                isNeedSetValue = true;
                break;
            }
            case SessionInfo.STATE_RECORDING://录音中
                break;
            case SessionInfo.STATE_RECORD_END://录音结束
            {
                mUIHandler.removeMessages(MSG_RECORD_TIME_OUT);
                List<AICloudMessage> messageList = mSessionInfo.getSessionMessageList();
                if (!messageList.isEmpty()) {
                    AICloudMessage aiCloudMessage = messageList.get(0);
                    AICloudMessageEntity entity = aiCloudMessage.getEntity();
                    entity.setAiCloudState(AI_STATE_IAT_ING);
                }
                break;
            }
            case SessionInfo.STATE_IAT_END://语音识别结束
            {
                List<AICloudMessage> messageList = mSessionInfo.getSessionMessageList();
                AICloudMessage aiCloudMessage = messageList.get(0);
                AICloudMessageEntity entity = aiCloudMessage.getEntity();
                entity.setAiCloudState(AI_STATE_IAT_END);
                entity.setText(mIatText);
                break;
            }
            case SessionInfo.STATE_NLP_END://语义识别结束
            {
                List<AICloudMessage> messageList = mSessionInfo.getSessionMessageList();
                AICloudMessage aiCloudMessage = new AICloudMessage();
                AICloudMessageEntity entity = new AICloudMessageEntity();
                String uid = HealthApplication.getAppViewModel().getUid();
                AICloudMessage aiCloudMessageIat = messageList.get(0);
                AICloudMessageEntity entityIat = aiCloudMessageIat.getEntity();
//                long time = Calendar.getInstance().getTimeInMillis();
                entity.setUid(uid);
                entity.setTime(entityIat.getTime() + 1);
                entity.setDevMac("11:22:33:44:55:66");
                entity.setRole(ROLE_AI);
                entity.setAiCloudState(AI_STATE_NLP_END);
                entity.setText(mNlpText);
                aiCloudMessage.setEntity(entity);
                messageList.add(aiCloudMessage);
                {//插入数据到数据库中
                    AICloudMessage aiCloudMessageUser = messageList.get(0);
                    AICloudMessageEntity entityUser = aiCloudMessageUser.getEntity();
                    long idUser = insertHistoryMessage(entityUser);
                    entityUser.setId(idUser);
                    entity.setRevId(idUser);
                    long idAI = insertHistoryMessage(entity);
                    entity.setId(idAI);
                }
                mUIHandler.removeMessages(MSG_IDLE);
                mUIHandler.sendEmptyMessageDelayed(MSG_IDLE, 2000);
                isNeedSetValue = true;
                break;
            }
            default:
                return;
        }
        if (mSessionInfo != null) {
            mSessionInfo.setStatus(status);
            if (isNeedSetValue) {
                mUIHandler.post(() -> currentSessionMessageMLD.setValue(mSessionInfo));
            } else {
                currentSessionMessageMLD.postValue(mSessionInfo);
            }
        }
        onHandlerSessionStatus(status);
    }

    private long insertHistoryMessage(AICloudMessageEntity entity) {
        AICloudMessageDao dao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).AICloudMessageDao();
        return dao.insert(entity);
    }


    /**
     * 停止TTS合成播放
     */
    abstract void onStopTTS();

    /**
     * 开始TTS合成播放
     */
    abstract void onStartTTS(String ttsText);

    /**
     * 开始对话
     *
     * @param userText 用户文本
     */
    abstract void onStartChat(String userText);

    /**
     * 处理对话状态
     */
    abstract void onHandlerSessionStatus(int status);
}
