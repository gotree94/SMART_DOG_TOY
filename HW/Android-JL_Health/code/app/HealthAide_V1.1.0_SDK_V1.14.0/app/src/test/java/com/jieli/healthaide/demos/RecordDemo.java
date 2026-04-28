package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.impl.RecordOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.record.OnRecordStateCallback;
import com.jieli.jl_rcsp.model.RecordParam;
import com.jieli.jl_rcsp.model.RecordState;
import com.jieli.jl_rcsp.model.base.BaseError;

import org.junit.Test;

import static com.jieli.jl_rcsp.model.RecordState.REASON_NORMAL;

/**
 * @ClassName: RecordDemo
 * @Description: 录音功能示例
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/12/28 15:52
 */
public class RecordDemo {
    private RecordOpImpl mRecordOp;
    private OnRecordStateCallback mOnRecordStateCallback = (bluetoothDevice, recordState) -> {
        switch (recordState.getState()) {
            case RecordState.RECORD_STATE_START:   /* 录音状态 -- 开始状态*/
                //录音参数，音频类型 采样率  断句方
                RecordParam recordParam = recordState.getRecordParam();
                recordParam.getSampleRate();//采样率
                recordParam.getVoiceType();//音频编码格式
                recordParam.getVadWay();//断句方
                break;
            case RecordState.RECORD_STATE_WORKING: //录音数据回传
                recordState.getVoiceDataBlock();//录音分包数据

                break;
            case RecordState.RECORD_STATE_IDLE: /* 录音状态 -- 空闲（结束)状态*/
                if (recordState.getReason() >= 0) {//结束原因，大于0则是正常
                    //非AI云服务功能，不需要处理以下状态
                    recordState.isPlayTTS();//是否需要播放TTS
                    recordState.isSyncIatText();//是否需要传输语音识别文本
                    recordState.isSyncNlpText();//是否需要传输语义识别文本
                }
                recordState.getVoiceData();//录音总包数据
                break;
        }
    };
    /**
     * 初始化
     */
    @Test
    public void init(){
        mRecordOp = new RecordOpImpl(WatchManager.getInstance());
        mRecordOp.addOnRecordStateCallback(mOnRecordStateCallback);
    }
    /**
     * 释放
     */
    @Test
    public void release(){
        if (mRecordOp != null) {
            mRecordOp.removeOnRecordStateCallback(mOnRecordStateCallback);
            mRecordOp.release();
            mRecordOp = null;
        }
    }

    /**
     * 获取录音状态
     */
    @Test
    public RecordState getRecordState(){
        return mRecordOp.getRecordState();
    }
    /**
     * App端开始录音
     */
    @Test
    public void startRecord(BluetoothDevice device){
        int voiceType = RecordParam.VOICE_TYPE_OPUS;//录音格式-opus
        int sampleRate = RecordParam.SAMPLE_RATE_8K;//采样率-8k
        int vadWay = RecordParam.VAD_WAY_DEVICE;//断句方-设备
        RecordParam param = new RecordParam(voiceType,sampleRate,vadWay);
        OnOperationCallback<Boolean> callback = new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result){
                    //开始成功
                }
            }
            @Override
            public void onFailed(BaseError error) {
            }
        };
         mRecordOp.startRecord(device,param,callback);
    }
    /**
     * App端结束录音
     */
    @Test
    public void stopRecord(BluetoothDevice device){
        int reason = REASON_NORMAL;//0：正常结束，1取消
        boolean isSyncIatText= true;//是否同步语音识别文本(会同步更新设置)
        boolean isSyncNlpText= true;//是否同步AI语义识别文本(会同步更新设置)
        boolean isPlayTTS= true;//是否播放AI语义识别的TTS(会同步更新设置)
        OnOperationCallback<Boolean> callback = new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result){
                    //停止成功
                }
            }
            @Override
            public void onFailed(BaseError error) {
            }
        };
        mRecordOp.stopRecord(device,reason,isSyncIatText,isSyncNlpText,isPlayTTS,callback);
    }
}
