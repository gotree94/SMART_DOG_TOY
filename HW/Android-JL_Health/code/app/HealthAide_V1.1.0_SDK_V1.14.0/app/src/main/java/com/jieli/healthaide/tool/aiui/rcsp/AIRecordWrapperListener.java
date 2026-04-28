package com.jieli.healthaide.tool.aiui.rcsp;

import android.bluetooth.BluetoothDevice;

import com.jieli.jl_rcsp.model.RecordState;

/**
 * @ClassName: AIRecordWrapperListener
 * @Description: RCSP封装AI录音流程的监听
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 16:32
 */
public interface AIRecordWrapperListener {
    /**
     * 录音状态改变
     */
    void onRecordStateChange(BluetoothDevice device, RecordState recordState);

    /**
     * 解码数据
     */
    void onDecodeStream(byte[] bytes);

    /**
     * 解码开始
     */
    void onDecodeStart();

    /**
     * 解码完成
     *
     * @param code 结果码 0:正常,1:取消
     */
    void onDecodeComplete(int code, String s);

    /**
     * 解码异常
     */
    void onDecodeError(int errorCode, String errorMsg);
}
