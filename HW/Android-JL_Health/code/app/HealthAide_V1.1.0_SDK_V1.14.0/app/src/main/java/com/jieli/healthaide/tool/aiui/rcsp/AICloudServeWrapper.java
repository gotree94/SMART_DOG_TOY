package com.jieli.healthaide.tool.aiui.rcsp;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.NotifyTTSPlayStateCmd;
import com.jieli.jl_rcsp.model.command.sys.UpdateSysInfoCmd;
import com.jieli.jl_rcsp.model.data.SendParams;
import com.jieli.jl_rcsp.model.device.AttrBean;
import com.jieli.jl_rcsp.model.parameter.UpdateSysInfoParam;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName: AICloudServeWrapper
 * @Description: AI云服务
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/16 16:40
 */
public class AICloudServeWrapper {
    private final String TAG = this.getClass().getSimpleName();
    private final int AISupplier = 1;//ai供应商，0:杰理,1：科大讯飞
    private boolean isEnable = false;
    private WatchManager mRcspOp;
    private final LinkedBlockingQueue<SendTaskParam> mSendTaskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isSendData = false;
    private ArrayList<AICloudServeWrapperListener> mListeners = new ArrayList<>();
    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onReceiveBigData(BluetoothDevice device, int type, byte[] data) {
            if (null == data) return;
            JL_Log.d(TAG, "onReceiveBigData", "type : " + type + ", data : " + data.length);
            if (!isEnable) return;
            switch (type) {
                case 4://TTS语音合成
                    if (data.length >= 3) {
                        byte versionData = data[0];
                        int version = versionData & 0x0f;
                        if (version == 0) {//版本0解析方式
                            int payloadLen = ValueUtil.bytesToInt(data[1], data[2]);
                            if (data.length < payloadLen + 3) {//数据格式不对
                                JL_Log.e(TAG, "onReceiveBigData", "Error data length is error.");
                                return;
                            }
                            byte[] payload = new byte[payloadLen];
                            System.arraycopy(data, 3, payload, 0, payloadLen);
                            String ttsString = new String(payload);
                            //回调tts
                            for (AICloudServeWrapperListener listener : mListeners) {
                                listener.onTTS(ttsString);
                            }
                        }
                    }
                    break;
            }
        }

        @Override
        public void onRcspCommand(BluetoothDevice device, CommandBase command) {
            super.onRcspCommand(device, command);
            if (command.getId() == Command.CMD_SYS_INFO_AUTO_UPDATE) {
//                Log.d(TAG, "onRcspCommand: 退出AI界面");
                //退出AI界面
                UpdateSysInfoCmd updateSysInfoCmd = (UpdateSysInfoCmd) command;
                UpdateSysInfoParam param = updateSysInfoCmd.getParam();
                if ((param.getFunction() & 0xff) == 0xff) {
                    for (AttrBean attrBean : param.getAttrBeanList()) {
                        if ((attrBean.getType() & 0xff) == 0x1a) {
                            byte[] attrData = attrBean.getAttrData();
                            byte version = attrData[0];
                            if ((version & 0xff) == 0x00) {//版本0
                                if (attrData.length >= 2) {
                                    byte operate = attrData[1];
                                    for (AICloudServeWrapperListener listener : mListeners) {
                                        listener.onDevNotifyAIDialUIChange((operate & 0xff));
                                    }
                                }
                            }
                        }
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
                        cancelAsyncMessage();
                    }
                    break;
            }
        }
    };


    public AICloudServeWrapper(WatchManager watchManager) {
        mRcspOp = watchManager;
        mRcspOp.registerOnWatchCallback(mWatchCallback);
    }

    public void release() {
        if (mRcspOp != null) {
            mRcspOp.unregisterOnWatchCallback(mWatchCallback);
            mRcspOp = null;
        }
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public void registerListener(AICloudServeWrapperListener listener) {
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void unregisterListener(AICloudServeWrapperListener listener) {
        if (this.mListeners.contains(listener)) {
            this.mListeners.remove(listener);
        }
    }

    /**
     * 消息同步-语音识别
     *
     * @param iatText 识别文本
     */
    public void asyncMessageIat(String iatText, OnDataEventCallback callback) {
        JL_Log.d(TAG, "asyncMessageIat", iatText);
//        if (this.isNeedAsyncIat()) {
        this.sendTextData(0, iatText, callback);
//        }
    }

    /**
     * 消息同步-语义结果
     *
     * @param nlpText 语义结果文本
     */
    public void asyncMessageNlp(String nlpText, OnDataEventCallback callback) {
        JL_Log.d(TAG, "asyncMessageNlp", nlpText);
//        if (this.isNeedAsyncNlp()) {
        this.sendTextData(1, nlpText, callback);
//        }
    }

    /**
     * 消息同步-AI错误提示
     *
     * @param aiErrorText AI错误提示
     */
    public void asyncMessageAIError(String aiErrorText, OnDataEventCallback callback) {
        JL_Log.e(TAG, "asyncMessageAIError", aiErrorText);
//        if (this.isNeedAsyncNlp()) {
        this.sendTextData(2, aiErrorText, callback);
//        }
    }

    /**
     * 取消之前的消息同步
     */
    public void cancelAsyncMessage() {
        for (Object param : mSendTaskQueue.toArray()) {
            SendTaskParam sendTaskParam = (SendTaskParam) param;
            sendTaskParam.setCancel(true);
        }
        mSendTaskQueue.clear();
    }

    /**
     * 通知设备TTS播放-开始
     */
    public void notifyDevTTSStart() {
        JL_Log.e(TAG, "notifyDevTTSStart", "");
        NotifyTTSPlayStateCmd.Param param = new NotifyTTSPlayStateCmd.Param(0);
        NotifyTTSPlayStateCmd cmd = new NotifyTTSPlayStateCmd(param);
        sendCmdToDev(cmd, null);
    }

    /**
     * 通知设备TTS播放-结束
     */
    public void notifyDevTTSStop() {
        JL_Log.e(TAG, "notifyDevTTSStop", "");
        NotifyTTSPlayStateCmd.Param param = new NotifyTTSPlayStateCmd.Param(1);
        NotifyTTSPlayStateCmd cmd = new NotifyTTSPlayStateCmd(param);
        sendCmdToDev(cmd, null);
    }

    /**
     * @param textType //文本类型，0:语音识别，1：Ai应答文本
     */
    private void sendTextData(int textType, String text, OnDataEventCallback callback) {
        if (!mRcspOp.isConnected()) return;
        byte[] textData = text.getBytes();
        byte[] data = new byte[4 + textData.length];
        int version = 0;//数据格式解析的版号本
        data[0] = (byte) ((version & 0x0f) + ((textType & 0x0f) << 4));
        data[1] = (byte) this.AISupplier;
        data[2] = (byte) (textData.length >> 8 & 255);
        data[3] = (byte) (textData.length & 255);
        System.arraycopy(textData, 0, data, 4, textData.length);
        SendParams param = new SendParams(RcspConstant.TYPE_AI_CLOUD_DATA, RcspConstant.DATA_TRANSFER_VERSION,
                4 * 1024, 4 * 1024, data);
        try {
            mSendTaskQueue.put(new SendTaskParam(param, callback, text));
            JL_Log.d(TAG, "sendTextData", "put task in queue...");
            startSendTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendCmdToDev(CommandBase commandBase, RcspCommandCallback commandCallback) {
        if (mRcspOp != null) {
            BluetoothDevice connectedDevice = mRcspOp.getConnectedDevice();
            if (connectedDevice != null) {
                mRcspOp.sendRcspCommand(connectedDevice, commandBase, commandCallback);
            } else {
                JL_Log.e(TAG, "sendCmdToDev", "no connected device");
            }
        } else {
            JL_Log.e(TAG, "sendCmdToDev", "RcspOp is release");
        }
    }

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
        JL_Log.d(TAG, "startSendTask", "sendLargeData ---> " + param);
        WatchManager.getInstance().sendLargeData(param.getParam(), new OnDataEventCallback() {
            @Override
            public void onBegin(int way) {
                JL_Log.d(TAG, "startSendTask", "onBegin ---> way : " + way);
                isSendData = true;
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onBegin(way);
                }
            }

            @Override
            public void onProgress(float progress) {
                JL_Log.d(TAG, "startSendTask", "onProgress ---> " + progress);
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onProgress(progress);
                }
            }

            @Override
            public void onStop(int type, byte[] data) {
                JL_Log.i(TAG, "startSendTask", "onFinish ---> " + param.getText() + ", isCancel: " + param.isCancel());
                isSendData = false;
                mSendTaskQueue.poll();
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onStop(type, data);
                }
                startSendTask();
            }

            @Override
            public void onError(BaseError error) {
                JL_Log.e(TAG, "startSendTask", "onError ---> " + error);
                isSendData = false;
                mSendTaskQueue.clear();
                if (param.getCallback() != null && !param.isCancel()) {
                    param.getCallback().onError(error);
                }
            }
        });
    }

    private static class SendTaskParam {
        private final SendParams mParam;              //发送参数
        private final OnDataEventCallback mCallback;  //结果回调
        private boolean isCancel = false;//是否取消
        private String text;

        public SendTaskParam(@NonNull SendParams param, OnDataEventCallback callback, String text) {
            mParam = param;
            mCallback = callback;
            this.text = text;
        }

        @NonNull
        public SendParams getParam() {
            return mParam;
        }

        public OnDataEventCallback getCallback() {
            return mCallback;
        }

        public String getText() {
            return text;
        }

        public boolean isCancel() {
            return isCancel;
        }

        public void setCancel(boolean cancel) {
            isCancel = cancel;
        }

        @Override
        public String toString() {
            return "SendTaskParam{" +
                    "mParam=" + mParam +
                    ", mCallback=" + mCallback +
                    '}';
        }
    }
}
