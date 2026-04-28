package com.jieli.healthaide.tool.aiui.handle;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.iflytek.sparkchain.core.LLMResult;
import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.tool.aiui.iflytek.BasicWrapper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekTextToImageWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AIDialWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.io.File;

/**
 * @ClassName: IflytekAIDialHandler
 * @Description: 科大讯飞AI手表处理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/13 15:46
 */
public class IflytekAIDialHandler extends BaseAIDialHandler {
    private final IflytekTextToImageWrapper mIflytekTextToImageWrapper;

    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            switch (status) {
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    if (RcspUtil.deviceEquals(device, WatchManager.getInstance().getTargetDevice())) {
                        mIflytekTextToImageWrapper.stop();
                    }
                    break;
            }
        }
    };

    public IflytekAIDialHandler(AIDialWrapper aiDialWrapper, WatchManager rcspOp, Context context) {
        super(aiDialWrapper, rcspOp, context);
        mIflytekTextToImageWrapper = new IflytekTextToImageWrapper(context);
        mRcspOp.registerOnWatchCallback(mWatchCallback);
    }

    @Override
    public void release() {
        super.release();
        mRcspOp.unregisterOnWatchCallback(mWatchCallback);
    }

    @Override
    void onStartAIChat(String userText) {
        String chatText = getCurrentAIDialStyle() + userText;
        mIflytekTextToImageWrapper.execute(chatText, stateResult -> {
            if (stateResult.getState() == BasicWrapper.STATUS_FINISH) {
                if (stateResult.isSuccess()) { //生产图片成功
                    final LLMResult result = stateResult.getResult();
                    final byte[] imageData = result.getImage();
                    String savePath = FileUtil.createFilePath(mContext, "aidial")
                            + File.separator + "aiSrc_cache";//CustomDialManager.getInstance().getCacheSrcImagePath();
                    File file = new File(savePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    if (FileUtil.bytesToFile(imageData, savePath)) {
                        handleAIChatImage(savePath);
                    } else {
                        JL_Log.w(TAG, "onStartAIChat", "Failed to save bitmap. " +
                                "data size : " + imageData.length + ", \nsave Path : " + savePath);
                        onTTINetworkError();
                    }
                    return;
                }
                onTTINetworkError();
            }
        });
    }
}
