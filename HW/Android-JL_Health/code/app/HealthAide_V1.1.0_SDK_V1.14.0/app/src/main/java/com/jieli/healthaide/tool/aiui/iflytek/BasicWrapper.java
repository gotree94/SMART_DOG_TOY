package com.jieli.healthaide.tool.aiui.iflytek;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.iflytek.sparkchain.core.LogLvl;
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.tool.aiui.model.OpResult;
import com.jieli.healthaide.tool.aiui.model.StateResult;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.io.File;

/**
 * BasicWrapper
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2025/11/6
 * note: 基础的功能封装器
 */
public abstract class BasicWrapper<I, O> {

    /**
     * 语音听写功能
     */
    public static final int FUNCTION_ASR = 0x01;
    /**
     * 语音合成功能
     */
    public static final int FUNCTION_TTS = 0x02;
    /**
     * 智能聊天功能
     */
    public static final int FUNCTION_AI_CHAT = 0x03;
    /**
     * 智能表盘功能(文生图功能)
     */
    public static final int FUNCTION_AI_DIAL = 0x04;


    /**
     * 空闲状态
     */
    public static final int STATUS_IDLE = 0;
    /**
     * 工作状态
     */
    public static final int STATUS_WORKING = 1;
    /**
     * 结束状态
     */
    public static final int STATUS_FINISH = 2;


    /**
     * 科大讯飞SDK是否初始化成功
     */
    private static boolean isSDKInit;
    /**
     * 功能封装器初始化计数
     */
    private static int initCount;

    /**
     * 类标识
     */
    protected String tag = getClass().getSimpleName();

    /**
     * 工作状态
     */
    protected int mStatus;
    /**
     * 操作标识
     */
    protected int mUserTag;

    /**
     * 操作回调
     */
    protected Consumer<StateResult<O>> mCallback;
    /**
     * UI处理
     */
    protected final Handler uiHandler = new Handler(Looper.getMainLooper());

    public BasicWrapper(Context context) throws RuntimeException {
        if (!isSDKInit) {
            initSDK(context);
            isSDKInit = true;
        }
        initCount++;
        JL_Log.i(tag, "init", "clazz : " + this + ", count : " + initCount);
        mStatus = STATUS_IDLE;
    }

    public void destroy() {
        uiHandler.removeCallbacksAndMessages(null);
        initCount--;
        JL_Log.i(tag, "destroy", "clazz : " + this + ", count : " + initCount);
        if (isSDKInit && initCount <= 0) { //SDK初始化成功，而且初始化功能类全部销毁，所以把SDK也销毁
            SparkChain.getInst().unInit();
            JL_Log.i(tag, "destroy", "SparkChain#unInit");
            isSDKInit = false;
            initCount = 0;
        }
    }

    public abstract int getType();

    public abstract boolean isRunning();

    public abstract void execute(I input, Consumer<StateResult<O>> callback);

    public abstract void stop();

    public abstract void cancel();

    protected boolean isSameTag(Object tag) {
        return (tag instanceof Integer) && mUserTag == (int) tag;
    }

    protected void setStatus(int status) {
        if (this.mStatus != status) {
            mStatus = status;
        }
    }

    protected void setCallback(Consumer<StateResult<O>> callback) {
        mCallback = callback;
    }

    protected int autoIncUserTag() {
        mUserTag++;
        return mUserTag;
    }

    protected void runInMainThread(Runnable runnable) {
        if (null == runnable) return;
        boolean isInMainThread = Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId();
        if (isInMainThread) {
            runnable.run();
            return;
        }
        uiHandler.post(runnable);
    }

    protected void callbackStart() {
        setStatus(STATUS_WORKING);
        JL_Log.d(tag, "callbackStart", "--->");
        final Consumer<StateResult<O>> callback = mCallback;
        if (null != callback) {
            runInMainThread(() -> callback.accept(new StateResult<O>()
                    .setState(STATUS_WORKING)));
        }
    }

    protected void callbackWorking(String method, float progress, O result) {
        if (!isRunning()) return;
        setStatus(STATUS_WORKING);
        JL_Log.d(tag, "callbackWorking", RcspUtil.formatString("[%s] ---> progress : %f", method, progress));
        final Consumer<StateResult<O>> callback = mCallback;
        if (null != callback) {
            runInMainThread(() -> callback.accept(new StateResult<O>()
                    .setState(STATUS_WORKING)
                    .setCode(OpResult.ERR_NONE)
                    .setProgress(progress)
                    .setResult(result)));
        }
    }

    protected void callbackFinish(String method, int code, String message, O result) {
        if (!isRunning()) return;
        setStatus(STATUS_FINISH);
        if (code == OpResult.ERR_NONE) {
            JL_Log.i(tag, "callbackFinish", RcspUtil.formatString("[%s] ---> %s", method, message));
        } else {
            JL_Log.w(tag, "callbackError", RcspUtil.formatString("[%s] ---> code : %s, %s",
                    method, code, message));
        }
        final Consumer<StateResult<O>> callback = mCallback;
        if (null != callback) {
            setCallback(null);
            runInMainThread(() -> callback.accept(new StateResult<O>()
                    .setState(STATUS_FINISH)
                    .setCode(code)
                    .setMessage(message)
                    .setResult(result)));
        }
    }

    private void initSDK(Context context) throws RuntimeException {
        SparkChainConfig config = SparkChainConfig.builder()
                .appID(BuildConfig.IFLYTEK_APP_ID)
                .apiKey(BuildConfig.IFLYTEK_API_KEY)
                .apiSecret(BuildConfig.IFLYTEK_API_SECRET);
        if (BuildConfig.DEBUG) {
            String logDirPath = JL_Log.getSaveLogPath(context);
            config.logLevel(LogLvl.VERBOSE.getValue()).logPath(logDirPath);
        } else {
            config.logLevel(LogLvl.OFF.getValue());
        }
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            config.workDir(cacheDir.getPath());
        }
        int ret = SparkChain.getInst().init(context, config);
        JL_Log.i(tag, "initSDK", "---> " + ret);
        if (ret != 0)
            throw new RuntimeException("Initialization of iFlytek SDK failed, error code: " + ret);
    }
}
