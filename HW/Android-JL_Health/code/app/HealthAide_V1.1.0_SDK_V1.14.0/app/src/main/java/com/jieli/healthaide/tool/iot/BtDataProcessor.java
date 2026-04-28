package com.jieli.healthaide.tool.iot;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.data.SendParams;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 蓝牙数据处理器
 * @since 2022/11/18
 */
public class BtDataProcessor {
    private final String tag = BtDataProcessor.class.getSimpleName();
    private final WatchManager mWatchManager;
    private final OnIOTEventListener mListener;
    private final LinkedBlockingQueue<SendTaskParam> mSendTaskQueue = new LinkedBlockingQueue<>();

    private volatile boolean isSendData;

    public BtDataProcessor(@NonNull WatchManager manager, OnIOTEventListener listener) {
        mWatchManager = manager;
        mListener = listener;

        mWatchManager.registerOnWatchCallback(mWatchCallback);
    }

    public void destroy() {
        JL_Log.i(tag, "destroy", "");
        mSendTaskQueue.clear();
        mWatchManager.unregisterOnWatchCallback(mWatchCallback);
    }

    public void writeAliIotData(byte[] data, OnDataEventCallback callback) {
        SendParams param = new SendParams(RcspConstant.TYPE_ALI_DATA, RcspConstant.DATA_TRANSFER_VERSION,
                4 * 1024, 4 * 1024, data);

        try {
            mSendTaskQueue.put(new SendTaskParam(param, callback));
            JL_Log.d(tag, "writeAliIotData", "put task in queue...");
            startSendTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startSendTask() {
        if (isSendData) {
            JL_Log.d(tag, "startSendTask", "Task is running");
            return;
        }
        final SendTaskParam param = mSendTaskQueue.peek();
        if (null == param) {
            JL_Log.d(tag, "startSendTask", "SendTaskParam is null");
            return;
        }
        JL_Log.d(tag, "startSendTask", "sendLargeData >>> " + param);
        mWatchManager.sendLargeData(param.getParam(), new OnDataEventCallback() {
            @Override
            public void onBegin(int way) {
                JL_Log.d(tag, "startSendTask", "onBegin >>>");
                isSendData = true;
                if (param.getCallback() != null) {
                    param.getCallback().onBegin(way);
                }
            }

            @Override
            public void onProgress(float progress) {
                JL_Log.d(tag, "startSendTask", "onProgress >>>" + progress);
                if (param.getCallback() != null) {
                    param.getCallback().onProgress(progress);
                }
            }

            @Override
            public void onStop(int type, byte[] data) {
                JL_Log.i(tag, "startSendTask", "onFinish >>> ");
                isSendData = false;
                mSendTaskQueue.poll();
                if (param.getCallback() != null) {
                    param.getCallback().onStop(type, data);
                }
                startSendTask();
            }

            @Override
            public void onError(BaseError error) {
                JL_Log.e(tag, "startSendTask", "onError >>> " + error);
                isSendData = false;
                mSendTaskQueue.clear();
                if (param.getCallback() != null) {
                    param.getCallback().onError(error);
                }
            }
        });
    }

    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onReceiveBigData(BluetoothDevice device, int type, byte[] data) {
            JL_Log.d(tag, "onReceiveBigData", "type : " + type + ", data : " + (null == data ? 0 : data.length));
            if (type == RcspConstant.TYPE_ALI_DATA) {
                if (mListener != null)
                    mListener.onIotData(device, type, data);
            }
        }

        @Override
        public void onBigDataError(BluetoothDevice device, BaseError error) {
            JL_Log.e(tag, "onBigDataError", "" + error);
        }
    };

    private static class SendTaskParam {
        private final SendParams mParam;              //发送参数
        private final OnDataEventCallback mCallback;  //结果回调


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

        @Override
        public String toString() {
            return "SendTaskParam{" +
                    "mParam=" + mParam +
                    ", mCallback=" + mCallback +
                    '}';
        }
    }

    public interface OnIOTEventListener {

        void onIotData(BluetoothDevice device, int type, byte[] data);
    }
}
