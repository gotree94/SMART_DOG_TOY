package com.jieli.healthaide.tool.esim;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.data.SendParams;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName: eSimHelper
 * @Description: eSim卡
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/11/28 16:20
 */
public class ESimWrapper {
    private final String TAG = this.getClass().getSimpleName();
    private WatchManager mRcspOp;
    private final LinkedBlockingQueue<SendTaskParam> mSendTaskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isSendData = false;
    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onReceiveBigData(BluetoothDevice device, int type, byte[] data) {
            if (null == data) return;
            JL_Log.d(TAG, "onReceiveBigData", "收到大数据 " + type + " data : " + CHexConver.byte2HexStr(data));
            if (type == RcspConstant.TYPE_ESIM_CARD) {//esim卡数据回调
                if (data.length >= 3) {
                    byte versionData = data[0];
                    int version = versionData & 0xff;
                    if (version == 0) {//版本0解析方式
                        int payloadLen = ValueUtil.bytesToInt(data[1], data[2]);
                        if (data.length < payloadLen + 3) {//数据格式不对
                            JL_Log.e(TAG, "onReceiveBigData", "Error data length is error.");
                            return;
                        }
                        byte[] payload = new byte[payloadLen];
                        System.arraycopy(data, 3, payload, 0, payloadLen);
                        // TODO: 2023/11/28  payload为设备返回的esim卡数据
                        onHandlerESimData(payload);
                    }
                }
            }
        }


        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            switch (status) {
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    if (RcspUtil.deviceEquals(device, WatchManager.getInstance().getTargetDevice())) {
                        // TODO: 2023/11/28  设备断开
                        isSendData = false;
                        mSendTaskQueue.clear();
                    }
                    break;
            }
        }
    };
    private final List<ESimWrapperListener> mESimWrapperListenerArray = new ArrayList<>();

    public ESimWrapper(WatchManager watchManager) {
        mRcspOp = watchManager;
        mRcspOp.registerOnWatchCallback(mWatchCallback);
    }

    public void release() {
        mRcspOp.unregisterOnWatchCallback(mWatchCallback);
        mRcspOp = null;
        mESimWrapperListenerArray.clear();
    }

    /**
     * 添加eSim卡数据回调
     *
     * @param listener eSim卡数据回调
     */
    public void addESimWrapperListener(ESimWrapperListener listener) {
        if (!mESimWrapperListenerArray.contains(listener)) {
            mESimWrapperListenerArray.add(listener);
        }
    }

    /**
     * 移除eSim卡数据回调
     *
     * @param listener eSim卡数据回调
     */
    public void removeESimWrapperListener(ESimWrapperListener listener) {
        mESimWrapperListenerArray.remove(listener);
    }

    /**
     * 发送eSim卡数据
     */
    public void sendESimData(byte[] eSimData, OnDataEventCallback callback) {
        if (!mRcspOp.isConnected()) return;
        byte[] data = new byte[3 + eSimData.length];
        int version = 0;//数据格式解析的版号本-目前定义是版本0
        data[0] = (byte) ((version & 0xff));
        data[1] = (byte) (eSimData.length >> 8 & 255);
        data[2] = (byte) (eSimData.length & 255);
        System.arraycopy(eSimData, 0, data, 3, eSimData.length);
        SendParams param = new SendParams(RcspConstant.TYPE_ESIM_CARD, RcspConstant.DATA_TRANSFER_VERSION,
                4 * 1024, 4 * 1024, data);
        try {
            mSendTaskQueue.put(new SendTaskParam(param, callback));
            JL_Log.d(TAG, "sendESimData", "put task in queue...");
            startSendTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动发送任务
     */
    private void startSendTask() {
        if (isSendData) {
            JL_Log.d(TAG, "startSendTask", "Task is running");
            return;
        }
        final SendTaskParam param = mSendTaskQueue.peek();
        if (null == param) {
            JL_Log.d(TAG, "startSendTask", "SendTaskParam is null");
            return;
        }
        isSendData = true;
        JL_Log.d(TAG, "startSendTask", "sendLargeData >>> " + param);
        mRcspOp.sendLargeData(param.getParam(), new OnDataEventCallback() {
            @Override
            public void onBegin(int way) {
                JL_Log.d(TAG, "onBegin", "way : " + way);
                isSendData = true;
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onBegin(way);
                }
            }

            @Override
            public void onProgress(float progress) {
                JL_Log.d(TAG, "onProgress", "progress : " + progress);
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onProgress(progress);
                }
            }

            @Override
            public void onStop(int type, byte[] data) {
                JL_Log.i(TAG, "onStop", "type :  " + type + ", isCancel: " + param.isCancel());
                isSendData = false;
                mSendTaskQueue.poll();
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onStop(type, data);
                }
                startSendTask();
            }

            @Override
            public void onError(BaseError error) {
                JL_Log.e(TAG, "onError", "" + error);
                isSendData = false;
                mSendTaskQueue.clear();
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onError(error);
                }
            }
        });
    }

    private void onHandlerESimData(byte[] payload) {
        for (ESimWrapperListener listener : mESimWrapperListenerArray) {
            listener.onReceiveESimData(payload);
        }
    }

    private static class SendTaskParam {
        private final SendParams mParam;              //发送参数
        private final OnDataEventCallback mCallback;  //结果回调
        private boolean isCancel = false;//是否取消

        public SendTaskParam(@NonNull SendParams param, OnDataEventCallback callback) {
            mParam = param;
            mCallback = callback;
        }

        @NonNull
        public SendParams getParam() {
            return mParam;
        }

        public OnDataEventCallback getCallback() {
            return mCallback;
        }


        public boolean isCancel() {
            return isCancel;
        }

        public void setCancel(boolean cancel) {
            isCancel = cancel;
        }

        @NonNull
        @Override
        public String toString() {
            return "SendTaskParam{" +
                    "mParam=" + mParam +
                    ", mCallback=" + mCallback +
                    '}';
        }
    }

    public interface ESimWrapperListener {
        void onReceiveESimData(byte[] eSimData);
    }
}
