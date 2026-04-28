package com.jieli.healthaide.tool.aiui.rcsp;

import static com.jieli.jl_rcsp.model.RecordState.RECORD_STATE_IDLE;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_audio_decode.callback.OnDecodeStreamCallback;
import com.jieli.jl_audio_decode.exceptions.OpusException;
import com.jieli.jl_audio_decode.opus.OpusManager;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RecordOpImpl;
import com.jieli.jl_rcsp.interfaces.record.OnRecordStateCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.RecordParam;
import com.jieli.jl_rcsp.model.RecordState;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.util.ArrayList;

/**
 * @ClassName: AIRecordWrapper
 * @Description: AI录音实现
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 16:15
 */
public class AIRecordWrapper {
    // TODO: 2023/10/16 要优化成，可以停止 处理录音数据
    private final String TAG = this.getClass().getSimpleName();
    private final int MSG_RECORD_TIME_OUT = 2;
    private boolean isNeedAsyncIat = false;//是否需要同步语音识别
    private boolean isNeedAsyncNlp = false;//是否需要同步语义结果
    private boolean isNeedPlayTTS = false;//是否需要播放TTS
    //录音实现
    private RecordOpImpl mRecordOp;
    private WatchManager mRcspOp;
    private OpusManager mOpusManager;
    private RecordParam mRecordParam;//录音参数
    private final ArrayList<AIRecordWrapperListener> mListeners = new ArrayList<>();

    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            switch (status) {
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    if (RcspUtil.deviceEquals(device, WatchManager.getInstance().getTargetDevice())) {
                        isNeedPlayTTS = false;
                        isNeedAsyncIat = false;
                        isNeedAsyncNlp = false;
                        stopDecodeStream();
                        if (mRecordOp != null) {
                            mRecordOp.stopRecord(device, 1, false, false, false, null);
                        }
                    }
                    break;
            }
        }

    };
    private final OnRecordStateCallback mOnRecordStateCallback = (bluetoothDevice, recordState) -> {
        switch (recordState.getState()) {
            case RecordState.RECORD_STATE_START:   /* 录音状态 -- 开始状态*/
                mRecordParam = recordState.getRecordParam();
            {
                startRecordTimeOut();
                JL_Log.e(TAG, "OnRecordStateCallback", "VoiceType = " + getVoiceType());
                if (getVoiceType() == RecordParam.VOICE_TYPE_OPUS) {//opus
                    if (mOpusManager != null) {
                        mOpusManager.startDecodeStream(new OnDecodeStreamCallback() {
                            @Override
                            public void onDecodeStream(byte[] bytes) {
                                JL_Log.d(TAG, "onDecodeStream", CalendarUtil.formatString("解码数据长度: %d", bytes.length));
                                for (AIRecordWrapperListener listener : mListeners) {
                                    listener.onDecodeStream(bytes);
                                }
                            }

                            @Override
                            public void onStart() {
                                JL_Log.d(TAG, "onStart", "开始编码--------->>>");
                                for (AIRecordWrapperListener listener : mListeners) {
                                    listener.onDecodeStart();
                                }
                            }

                            @Override
                            public void onComplete(String s) {
                                JL_Log.d(TAG, "onComplete", "编码完成, output : " + s);
                                //TODO: 考虑延时停下来
                                if (mRecordOp != null) {
                                    for (AIRecordWrapperListener listener : mListeners) {
                                        listener.onDecodeComplete(mRecordOp.getRecordState().getReason(), s);
                                    }
                                }
                            }

                            @Override
                            public void onError(int i, String s) {
                                JL_Log.d(TAG, "onError", "编码异常: " + i + ", " + s);
                                for (AIRecordWrapperListener listener : mListeners) {
                                    listener.onDecodeError(i, s);
                                }
                            }
                        });
                    }
                } else if (getVoiceType() == RecordParam.VOICE_TYPE_SPEEX) {
                } else if (getVoiceType() == RecordParam.VOICE_TYPE_PCM) {//pcm
                    for (AIRecordWrapperListener listener : mListeners) {
                        listener.onDecodeStart();
                    }
                }
            }
            break;
            case RecordState.RECORD_STATE_WORKING: //录音数据回传
            {
                startRecordTimeOut();
                JL_Log.d(TAG, "RECORD_STATE_WORKING", "录音数据回传: " + getVoiceType());
                if (getVoiceType() == RecordParam.VOICE_TYPE_OPUS) {//opus
                    if (mOpusManager != null && mOpusManager.isDecodeStream()) {
                        JL_Log.d(TAG, "RECORD_STATE_WORKING", "writeAudioStream");
                        mOpusManager.writeAudioStream(recordState.getVoiceDataBlock());
                    }
                } else if (getVoiceType() == RecordParam.VOICE_TYPE_SPEEX) {//speex
                } else if (getVoiceType() == RecordParam.VOICE_TYPE_PCM) {//pcm
                    for (AIRecordWrapperListener listener : mListeners) {
                        listener.onDecodeStream(recordState.getVoiceDataBlock());
                    }
                }
            }
            break;
            case RECORD_STATE_IDLE:
                if (recordState.getReason() >= 0) {
                    isNeedPlayTTS = recordState.isPlayTTS();
                    isNeedAsyncIat = recordState.isSyncIatText();
                    isNeedAsyncNlp = recordState.isSyncNlpText();
                }

//                writeDataTOFile(recordState.getVoiceData());
                stopRecordTimeOut();
                if (mOpusManager != null && mOpusManager.isDecodeStream()) {
                    mOpusManager.stopDecodeStream();
                }
                if (getVoiceType() == RecordParam.VOICE_TYPE_PCM) {//pcm
                    for (AIRecordWrapperListener listener : mListeners) {
                        listener.onDecodeComplete(mRecordOp.getRecordState().getReason(), "pcm decode complete");
                    }
                }
                break;
        }
        for (AIRecordWrapperListener listener : mListeners) {
            listener.onRecordStateChange(bluetoothDevice, recordState);
        }
    };

    private final Handler mHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (msg.what == MSG_RECORD_TIME_OUT) {
            JL_Log.e(TAG, "MSG_RECORD_TIME_OUT", "等待录音数据超时: ");
            isNeedPlayTTS = false;
            isNeedAsyncIat = false;
            isNeedAsyncNlp = false;
            stopDecodeStream();
            if (mRecordOp != null) {
                mRecordOp.stopRecord(mRecordOp.getConnectedDevice(), 1, false, false, false, null);
            }
        }
        return true;
    });

    public AIRecordWrapper(WatchManager watchManager) {
        mRcspOp = watchManager;
//        mRecordOp = new RecordOpImpl(watchManager);
        mRcspOp.registerOnWatchCallback(mWatchCallback);
        try {
            mOpusManager = new OpusManager();
        } catch (OpusException e) {
            e.printStackTrace();
            JL_Log.w(TAG, "init", "OpusManager create failed.  message : " + e.getMessage());
        }
    }

    public void release() {
        mHandler.removeMessages(MSG_RECORD_TIME_OUT);
        if (mRcspOp != null) {
            mRcspOp.unregisterOnWatchCallback(mWatchCallback);
            mRcspOp = null;
        }
        if (mRecordOp != null) {
            mRecordOp.removeOnRecordStateCallback(mOnRecordStateCallback);
            mRecordOp.release();
            mRecordOp = null;
        }
    }

    public void registerListener(AIRecordWrapperListener listener) {
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void unregisterListener(AIRecordWrapperListener listener) {
        if (this.mListeners.contains(listener)) {
            this.mListeners.remove(listener);
        }
    }

    /**
     * 开始处理录音
     */
    public void startHandleRecord() {
        stopHandleRecord();
        mRecordOp = new RecordOpImpl(mRcspOp);
        mRecordOp.addOnRecordStateCallback(mOnRecordStateCallback);
    }

    /**
     * 停止处理录音
     */
    public void stopHandleRecord() {
        if (mRecordOp != null) {
            mRecordOp.removeOnRecordStateCallback(mOnRecordStateCallback);
            mRecordOp.release();
            mRecordOp = null;
        }
    }

    public boolean isNeedAsyncIat() {
        return isNeedAsyncIat;
    }

    public boolean isNeedAsyncNlp() {
        return isNeedAsyncNlp;
    }

    public boolean isNeedPlayTTS() {
        return isNeedPlayTTS;
    }

    public boolean isRecording() {
        if (mRecordOp == null) {
            return false;
        }
        if (!mRcspOp.isDeviceConnected(mRecordOp.getConnectedDevice())) return false;
        return (mRecordOp.getRecordState().getState() != RECORD_STATE_IDLE) || (mOpusManager.isDecodeStream());
    }

    /**
     * 开始录音
     *
     * @param device   操作设备
     * @param param    录音参数
     * @param callback 结果回调
     */
//    public void startRecord(BluetoothDevice device, RecordParam param, OnOperationCallback<Boolean> callback) {
//        mRecordOp.startRecord(device, param, callback);
//    }

    /**
     * 结束录音
     *
     * @param device    操作设备
     * @param reason    结束原因   0:正常，1:取消
     * @param isIatText 是否同步语音识别文本(会同步更新设置)
     * @param isNlpText 是否同步AI语义识别文本(会同步更新设置)
     * @param isTTS     是否播放AI语义识别的TTS(会同步更新设置)
     * @param callback  结果回调
     */
//    public void stopRecord(BluetoothDevice device, int reason, boolean isIatText, boolean isNlpText, boolean isTTS, OnOperationCallback<Boolean> callback) {
//        mRecordOp.stopRecord(device, reason, isIatText, isNlpText, isTTS, callback);
//    }
    private void stopDecodeStream() {
        if (mOpusManager != null && mOpusManager.isDecodeStream()) {
            mOpusManager.stopDecodeStream();
        }
    }

    private int getVoiceType() {
        return mRecordParam.getVoiceType();
    }

    private void startRecordTimeOut() {
        mHandler.removeMessages(MSG_RECORD_TIME_OUT);
        mHandler.sendEmptyMessageDelayed(MSG_RECORD_TIME_OUT, 4000);
    }

    private void stopRecordTimeOut() {
        mHandler.removeMessages(MSG_RECORD_TIME_OUT);
    }

}
