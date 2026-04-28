package com.jieli.healthaide.tool.aiui;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.aiui.handle.BaseAICloudServeHandler;
import com.jieli.healthaide.tool.aiui.handle.BaseAIDialHandler;
import com.jieli.healthaide.tool.aiui.handle.BaseAIIatHandler;
import com.jieli.healthaide.tool.aiui.handle.IflytekAICloudServeHandler;
import com.jieli.healthaide.tool.aiui.handle.IflytekAIDialHandler;
import com.jieli.healthaide.tool.aiui.handle.IflytekAIIatHandler;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekAIDialStyleHelper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekChatWrapper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekIatWrapper;
import com.jieli.healthaide.tool.aiui.iflytek.IflytekTtsWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AICloudServeWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AICloudServeWrapperListener;
import com.jieli.healthaide.tool.aiui.rcsp.AIDialListener;
import com.jieli.healthaide.tool.aiui.rcsp.AIDialWrapper;
import com.jieli.healthaide.tool.aiui.rcsp.AIRecordWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.dialog.TransferWaitingDialog;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

/**
 * @ClassName: AIManager
 * @Description: AI管理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/9/18 15:31
 */
public class AIManager {
    private static final String TAG = "AIManager";
    private Context mContext;
    private WatchManager mRcspOp;
    private IflytekChatWrapper mIflytekChatWrapper;
    private IflytekTtsWrapper mIflytekTtsWrapper;
    private IflytekIatWrapper mIflytekIatWrapper;
    private AIRecordWrapper mAIRecordWrapper;
    private AICloudServeWrapper mAICloudServeWrapper;
    private AIDialWrapper mAIDialWrapper;
    private BaseAIIatHandler mAIIatHandler;
    private BaseAICloudServeHandler mAICloudServeHandler;
    private BaseAIDialHandler mAIDialHandler;
    private TransferWaitingDialog waitingDialog;//断开的时候要更新
    private boolean isTransferring = false;
    private boolean mIsAIDialRunning = false;
    private final AIDialListener mAIDialListener = new AIDialListener() {
        @Override
        public void onDevNotifyAIDialUIChange(int state) {
            super.onDevNotifyAIDialUIChange(state);
            if (state == 0) {//退出AI表盘界面
                JL_Log.d(TAG, "onDevNotifyAIDialUIChange", "退出AI表盘界面");
                mAIRecordWrapper.stopHandleRecord();//取消处理AI录音数据
                if (waitingDialog != null && !isTransferring) {
                    waitingDialog.dismiss();
                }
                mIsAIDialRunning = false;
            } else if (state == 1) {//进入AI表盘界面
                JL_Log.d(TAG, "onDevNotifyAIDialUIChange", "进入AI表盘界面");
                mAIRecordWrapper.startHandleRecord();//开始处理AI录音数据
                mAIIatHandler.setAIChatHandler(mAIDialHandler);
                mIsAIDialRunning = true;
                if (waitingDialog == null) {
                    waitingDialog = new TransferWaitingDialog();
//                    waitingDialog.updateText("AI表盘对话中...");
                }
                if (!waitingDialog.isShow() && !waitingDialog.isAdded()) {
                    AppCompatActivity appCompatActivity = (AppCompatActivity) ActivityManager.getInstance().getCurrentActivity();
                    FragmentManager fragmentManager = appCompatActivity.getSupportFragmentManager();
                    waitingDialog.show(fragmentManager, waitingDialog.getClass().getCanonicalName());
                }
            }
        }

        @Override
        public void onTransferThumbStart() {
            super.onTransferThumbStart();
            isTransferring = true;
            if (waitingDialog != null) {
                waitingDialog.updateText(mContext.getString(R.string.transfer_ing));
            }
        }

        @Override
        public void onTransferThumbFinish(boolean isSuccess) {
            super.onTransferThumbFinish(isSuccess);
            isTransferring = false;
            if (waitingDialog != null) {
//                waitingDialog.updateText("AI表盘对话中...");
            }
        }

        @Override
        public void onInstallDialStart() {
            super.onInstallDialStart();
            isTransferring = true;
            if (waitingDialog != null) {
                waitingDialog.updateText(mContext.getString(R.string.transfer_ing));
            }
        }

        @Override
        public void onInstallDialFinish(boolean isSuccess) {
            super.onInstallDialFinish(isSuccess);
            isTransferring = false;
            if (waitingDialog != null) {
//                waitingDialog.updateText("AI表盘对话中...");
            }
        }
    };
    private final AICloudServeWrapperListener mAICloudServeWrapperListener = new AICloudServeWrapperListener() {
        @Override
        public void onTTS(String ttsText) {

        }

        @Override
        public void onDevNotifyAIDialUIChange(int state) {
            if (state == 0x01) {//进入AI云服务界面
                JL_Log.d(TAG, "onDevNotifyAIDialUIChange", "进入AI云服务界面");
                mAIRecordWrapper.startHandleRecord();//开始处理AI录音数据
                mAIIatHandler.setAIChatHandler(mAICloudServeHandler);
            } else if (state == 0x02) {//退出AI云服务界面
                JL_Log.d(TAG, "onDevNotifyAIDialUIChange", "退出AI云服务界面");
                mAIRecordWrapper.stopHandleRecord();//取消处理AI录音数据
            }
        }
    };
    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            switch (status) {
                case StateCode.CONNECTION_DISCONNECT:
                case StateCode.CONNECTION_FAILED:
                    if (RcspUtil.deviceEquals(device, WatchManager.getInstance().getTargetDevice())) {
                        JL_Log.e(TAG, "onConnectStateChange", "蓝牙断开");
                        isTransferring = false;
                        if (waitingDialog != null) {
                            waitingDialog.dismiss();
                        }
                    }
                    break;
            }
        }
    };
    @SuppressLint("StaticFieldLeak")
    private volatile static AIManager instance;

    public static AIManager getInstance() {
        return instance;
    }

    public static boolean isInit() {
        return instance != null;
    }

    public static void init(Context context, WatchManager rcspOp) {
        synchronized (AIManager.class) {
            if (null == instance) {
                try {
                    instance = new AIManager(context, rcspOp);
                } catch (RuntimeException e) {
                    JL_Log.e(TAG, "init", e.getMessage());
                }
            }
        }
    }

    private AIManager(Context context, WatchManager rcspOp) throws RuntimeException {
        mContext = context;
        mRcspOp = rcspOp;
        //RCSP手表初始化
        mIflytekIatWrapper = new IflytekIatWrapper(context);
        mIflytekTtsWrapper = new IflytekTtsWrapper(context);
        mIflytekChatWrapper = new IflytekChatWrapper(context);
        mAIRecordWrapper = new AIRecordWrapper(rcspOp);
        mAICloudServeWrapper = new AICloudServeWrapper(rcspOp);
        mAIDialWrapper = new AIDialWrapper(rcspOp);
        mAIIatHandler = new IflytekAIIatHandler(mIflytekIatWrapper, mAIRecordWrapper, mRcspOp, mContext);
        mAICloudServeHandler = new IflytekAICloudServeHandler(mIflytekChatWrapper, mIflytekTtsWrapper, mAICloudServeWrapper, mAIRecordWrapper, mRcspOp, mContext);
        mRcspOp.registerOnWatchCallback(mWatchCallback);
        mAICloudServeWrapper.registerListener(mAICloudServeWrapperListener);
        mAIDialWrapper.registerListener(mAIDialListener);
        String paintStyle = IflytekAIDialStyleHelper.getInstance().getCurrentStyle();
        mAIDialWrapper.setPaintStyle(paintStyle);
        mAIDialHandler = new IflytekAIDialHandler(mAIDialWrapper, mRcspOp, mContext);
    }

    public void release() {
        mIflytekIatWrapper.destroy();
        mIflytekTtsWrapper.destroy();
        mIflytekChatWrapper.destroy();
        mAIRecordWrapper.release();
        mAICloudServeWrapper.release();
        mAIDialWrapper.release();
        mAIIatHandler.release();
        mAICloudServeHandler.release();
        mAIDialHandler.release();
        mRcspOp.unregisterOnWatchCallback(mWatchCallback);
        instance = null;
        mRcspOp = null;
        mContext = null;
    }

    public BaseAICloudServeHandler getAICloudServe() {
        return mAICloudServeHandler;
    }

    public BaseAIDialHandler getAIDial() {
        return mAIDialHandler;
    }

    public boolean isAIDialRunning() {
        return mIsAIDialRunning;
    }
}
