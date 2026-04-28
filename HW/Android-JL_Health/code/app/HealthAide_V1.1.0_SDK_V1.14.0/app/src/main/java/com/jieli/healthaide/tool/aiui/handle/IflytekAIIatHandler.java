package com.jieli.healthaide.tool.aiui.handle;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.core.util.Consumer;

import com.iflytek.sparkchain.core.asr.ASR;
import com.iflytek.sparkchain.utils.constants.ErrorCode;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.aiui.iflytek.BasicWrapper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekIatWrapper;
import com.jieli.healthaide.tool.aiui.model.StateResult;
import com.jieli.healthaide.tool.aiui.rcsp.AIRecordWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @ClassName: IflytekAIIatHandler
 * @Description: 科大讯飞语音识别
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/13 11:29
 */
public class IflytekAIIatHandler extends BaseAIIatHandler {
//    private final HashMap<String, String> mIatResults = new LinkedHashMap<>();// 用HashMap存储听写结果
    private final StringBuilder mStringBuilder = new StringBuilder();
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    private final IflytekIatWrapper mIflytekIatWrapper;

    public IflytekAIIatHandler(IflytekIatWrapper iflytekIatWrapper, AIRecordWrapper aiRecordWrapper, WatchManager rcspOp, Context context) {
        super(aiRecordWrapper, rcspOp, context);
        mIflytekIatWrapper = iflytekIatWrapper;
    }

    @Override
    public void onDeviceDisconnect() {
        mIflytekIatWrapper.cancel();
    }

    @Override
    public void onRecordStart() {
//        mIatResults.clear();
        mStringBuilder.setLength(0);
        mIflytekIatWrapper.execute(null, asrCallback);
    }

    @Override
    public void onRecordData(byte[] recordData) {
        mIflytekIatWrapper.writeAudio(recordData);
    }

    @Override
    public void onRecordStop() {
        mUIHandler.postDelayed(mIflytekIatWrapper::stop, 1000);
    }

    @Override
    public void onRecordCancel() {
        mIflytekIatWrapper.cancel();
    }

    private final Consumer<StateResult<ASR.ASRResult>> asrCallback = new Consumer<StateResult<ASR.ASRResult>>() {
        @Override
        public void accept(StateResult<ASR.ASRResult> stateResult) {
            if (stateResult.getState() != BasicWrapper.STATUS_FINISH) return;
            boolean isRecording = mAIRecordWrapper.isRecording();
            setRecognizerFail(!stateResult.isSuccess());
            if (stateResult.isSuccess()) { //听写成功
                setNetworkWrong(false);
                final ASR.ASRResult result = stateResult.getResult();
                String asrText = result.getBestMatchText();
                JL_Log.d(TAG, "asrCallback", "显示听写结果: " + asrText);
                mStringBuilder.append(asrText);
                if (isRecording) {//正在录音-静音超时处理，开启新的语音识别
                    mIflytekIatWrapper.execute(null, this);
                    if (TextUtils.isEmpty(asrText)) {
                        ToastUtil.showToastLong(R.string.ai_no_speak);
                    }
                } else {
                    String iatString = mStringBuilder.toString();
                    onRecognizerResult(iatString);
                }
                return;
            }
            int errorCode = stateResult.getCode();
            if (errorCode >= ErrorCode.ERROR_NO_NETWORK && errorCode <= ErrorCode.ERROR_NET_EXCEPTION
                    || errorCode == ErrorCode.MSP_ERROR_DB_INVALID_APPID
                    || errorCode == ErrorCode.MSP_ERROR_AUTH_NO_ENOUGH_LICENSE) {
                setNetworkWrong(true);
            }
            if (errorCode == ErrorCode.MSP_ERROR_NO_DATA) {//没有录音数据,您好像没有说话哦
                onRecognizerResult("");
            }
            if (!isRecording && !mIflytekIatWrapper.isRunning()) {
                onRecognizerFail();
            }
        }
    };
}
