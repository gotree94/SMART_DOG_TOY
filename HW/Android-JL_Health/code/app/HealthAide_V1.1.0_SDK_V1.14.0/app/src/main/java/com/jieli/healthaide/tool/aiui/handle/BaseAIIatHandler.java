package com.jieli.healthaide.tool.aiui.handle;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;

import com.jieli.healthaide.tool.aiui.rcsp.AIRecordWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AIRecordWrapperListener;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.RecordState;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

/**
 * @ClassName: BaseAIIatHandler
 * @Description: 基础语音识别处理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/13 11:28
 */
public abstract class BaseAIIatHandler extends BaseAIHandler {
    protected AIRecordWrapper mAIRecordWrapper;
    //识别异常
    private boolean isRecognizerFail = false;
    private BaseAIChatHandler mBaseAIChatHandler;
    //网络异常
    private boolean isNetworkWrong = false;
    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            switch (status) {
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    if (RcspUtil.deviceEquals(device, WatchManager.getInstance().getTargetDevice())) {
                        JL_Log.e(TAG, "onConnectStateChange", "蓝牙断开");
                        onDeviceDisconnect();
                    }
                    break;
            }
        }
    };
    private final AIRecordWrapperListener mRCSPAIUIListener = new AIRecordWrapperListener() {
        @Override
        public void onRecordStateChange(BluetoothDevice device, RecordState recordState) {
        }

        @Override
        public void onDecodeStream(byte[] bytes) {
            JL_Log.d(TAG, "AIRecordWrapperListener#onDecodeStream", "size : " + bytes.length);
            if (mBaseAIChatHandler != null) {
                mBaseAIChatHandler.onIatRecording();
            }
            onRecordData(bytes);
        }

        @Override
        public void onDecodeStart() {
            isRecognizerFail = false;
            isNetworkWrong = false;
            if (mBaseAIChatHandler != null) {
                mBaseAIChatHandler.onIatStartRecord();
            }
            onRecordStart();
        }

        @Override
        public void onDecodeComplete(int resultCode, String s) {
            JL_Log.d(TAG, "AIRecordWrapperListener#onDecodeComplete", "resultCode : " + resultCode);
            if (resultCode == 0 && !isExitsAIChatUI()) {
                onRecordStop();
                if (mBaseAIChatHandler != null) {
                    mBaseAIChatHandler.onIatStopRecord();
                }
                if (isRecognizerFail) {
                    JL_Log.d(TAG, "AIRecordWrapperListener#onDecodeComplete", "传输网络异常 " + mContext + " " + isNetworkWrong);
                    if (mContext != null && isNetworkWrong) {
                        if (mBaseAIChatHandler != null) {
                            mBaseAIChatHandler.onIatNetworkError();
                        }
                        JL_Log.d(TAG, "AIRecordWrapperListener#onDecodeComplete", "传输网络异常");
                    }
                    if (mBaseAIChatHandler != null) {
                        mBaseAIChatHandler.onIatFail();
                    }
                }
            } else {
                //取消的数据就不要
                onRecordCancel();
                if (mBaseAIChatHandler != null) {
                    mBaseAIChatHandler.onIatFail();
                }
            }
        }

        @Override
        public void onDecodeError(int errorCode, String errorMsg) {

        }
    };

    public BaseAIIatHandler(AIRecordWrapper aiRecordWrapper, WatchManager rcspOp, Context context) {
        super(rcspOp, context);
        mAIRecordWrapper = aiRecordWrapper;
        mRcspOp.registerOnWatchCallback(mWatchCallback);
        mAIRecordWrapper.registerListener(mRCSPAIUIListener);
    }

    @Override
    public void release() {
        super.release();
        mRcspOp.unregisterOnWatchCallback(mWatchCallback);
        mAIRecordWrapper.unregisterListener(mRCSPAIUIListener);
    }

    public void setAIChatHandler(BaseAIChatHandler baseAIChatHandler) {
        mBaseAIChatHandler = baseAIChatHandler;
    }

    //语音识别异常标志位
    void setRecognizerFail(boolean recognizerFail) {
        isRecognizerFail = recognizerFail;
    }

    //网络异常标志位
    void setNetworkWrong(boolean networkWrong) {
        isNetworkWrong = networkWrong;
    }

    void onRecognizerResult(String iatString) {
        if (isConnected() && !isExitsAIChatUI()) {
            if (TextUtils.isEmpty(iatString)) {//文本为空
                if (mBaseAIChatHandler != null) {
                    mBaseAIChatHandler.onIatFail();
                    mBaseAIChatHandler.onIatRecognizeEmptyError();
                }
            } else {
                String tempIatString = iatString;
                if (iatString.length() > 256) {
                    tempIatString = iatString.substring(0, 256);
                    tempIatString += "...";
                }
                if (mBaseAIChatHandler != null) {
                    mBaseAIChatHandler.onIatText(tempIatString);
                }
            }
        } else {                    //设备断开就不要显示文本了
            if (mBaseAIChatHandler != null) {
                mBaseAIChatHandler.onIatFail();
            }
        }
    }

    void onRecognizerFail() {
        if (mBaseAIChatHandler != null) {
            mBaseAIChatHandler.onIatFail();
        }
    }

    //设备断开
    abstract void onDeviceDisconnect();

    // 开始录音
    abstract void onRecordStart();

    // 收到录音数据(已解码成PCM格式)
    abstract void onRecordData(byte[] recordData);

    // 录音结束
    abstract void onRecordStop();

    // 录音取消
    abstract void onRecordCancel();


    private boolean isExitsAIChatUI() {
        boolean result = false;
        if (mBaseAIChatHandler != null) {
            result = mBaseAIChatHandler.isExitsAIChatUI();
        }
        return result;
    }
}
