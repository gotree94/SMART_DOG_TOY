package com.jieli.healthaide.tool.aiui.handle;

import android.content.Context;
import android.text.TextUtils;

import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.aiui.rcsp.AIDialListener;
import com.jieli.healthaide.tool.aiui.rcsp.AIDialWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;

/**
 * @ClassName: BaseAIDialHandler
 * @Description: 基础AI表盘处理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/13 15:44
 */
public abstract class BaseAIDialHandler extends BaseAIChatHandler {
    private AIDialWrapper mAIDialWrapper;
    private String mIatText;//语音识别文本
    private int testSrc = 0;
    private final AIDialListener mAIDialListener = new AIDialListener() {
        @Override
        public void onGenerateDial() {
            super.onGenerateDial();
            //开始生成AI表盘
            startAIChat(mIatText);
        }

        @Override
        public void onRecordingAgain() {
            super.onRecordingAgain();
            //重新录音-暂不用处理，直接AIIatHandler那里处理就好
        }

        @Override
        public void onTransferThumbStart() {
            super.onTransferThumbStart();

        }

        @Override
        public void onTransferThumbFinish(boolean isSuccess) {
            super.onTransferThumbFinish(isSuccess);
        }

        @Override
        public void onInstallDialStart() {
            super.onInstallDialStart();
            //开始安装表盘：1.生成自定义表盘文件,2.传输表盘
        }

        @Override
        public void onInstallDialFinish(boolean isSuccess) {
            super.onInstallDialFinish(isSuccess);
        }

        @Override
        public void onReGenerateDial() {
            super.onReGenerateDial();
            //重新生成表盘
            retryAIChat();
        }
    };

    public BaseAIDialHandler(AIDialWrapper aiDialWrapper, WatchManager rcspOp, Context context) {
        super(rcspOp, context);
        mAIDialWrapper = aiDialWrapper;
        mAIDialWrapper.registerListener(mAIDialListener);
        testSrc = (int) (Math.random() * 9);
    }

    @Override
    public void release() {
        super.release();
        mAIDialWrapper.unregisterListener(mAIDialListener);
    }

    public String getCurrentAIDialStyle() {
        return mAIDialWrapper.getPainStyle();
    }

    public void setCurrentAIDialStyle(String paintStyle) {
        JL_Log.d(TAG, "setCurrentAIDialStyle", paintStyle);
        mAIDialWrapper.setPaintStyle(paintStyle);
    }

    public void onTTINetworkError() {
        String error = mContext.getString(R.string.ai_network_wrong);
        mAIDialWrapper.asyncMessageAIError(error, null);
    }

    @Override
    void onIatNetworkError() {
        String error = mContext.getString(R.string.ai_network_wrong);
        mAIDialWrapper.asyncMessageAIError(error, null);
    }

    @Override
    void onIatRecognizeEmptyError() {
        String error = mContext.getString(R.string.ai_no_speak);
        mAIDialWrapper.asyncMessageAIError(error, null);
    }

    @Override
    void onIatText(String iatText) {
        mIatText = iatText;
        transferIatText(mIatText);
    }

    @Override
    void onIatStartRecord() {
        mIatText = null;
    }

    @Override
    void onIatStopRecord() {
        super.onIatStopRecord();
    }

    protected void handleAIChatImage(String srcImagePath) {
        String dialDir = HealthUtil.createFilePath(mContext, "aidial");
        String tempThumbPath = dialDir + File.separator + mAIDialWrapper.getThumbName();//传输完成后会删除
        String tempDialPath = dialDir + File.separator + mAIDialWrapper.getCustomBgName();//传输完成后会删除
        mAIDialWrapper.handleNlpImage(srcImagePath, tempThumbPath, tempDialPath);
    }

    private void startAIChat(String userText) {
        if (HealthConstant.TEST_AI_DIAL_FUNCTION) {
            String dialDir = HealthUtil.createFilePath(mContext, "aidial");
            String srcPath = dialDir + File.separator + "src" + testSrc + ".jpg";
            JL_Log.d(TAG, "startAIChat", srcPath);
            handleAIChatImage(srcPath);
            testSrc = (testSrc + 1) % 10;
        } else {
            onStartAIChat(userText);
        }
    }

    /**
     * 传输语音识别文本
     */
    private void transferIatText(String iatText) {
        if (!TextUtils.isEmpty(iatText)) {
            mAIDialWrapper.asyncMessageIat(iatText, null);
        }
    }

    /**
     * 重新生成AI图片
     */
    private void retryAIChat() {
        startAIChat(mIatText);
    }

    /**
     * 开始对话（文生图）
     */
    abstract void onStartAIChat(String userText);
}
