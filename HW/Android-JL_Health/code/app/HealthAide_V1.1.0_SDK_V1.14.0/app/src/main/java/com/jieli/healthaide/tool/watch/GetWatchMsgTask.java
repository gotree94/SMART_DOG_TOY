package com.jieli.healthaide.tool.watch;

import android.os.Handler;
import android.os.Looper;

import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.listener.ThreadStateListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 获取表盘额外信息任务
 * @since 2021/3/31
 */
public class GetWatchMsgTask extends Thread {
    private final static String TAG = GetWatchMsgTask.class.getSimpleName();
    private final WatchOpImpl mWatchOp;
    private final List<FatFile> taskList;
    private final OnWatchOpCallback<ArrayList<WatchInfo>> mCallback;
    private final ThreadStateListener mStateListener;
    private final ArrayList<WatchInfo> watchList = new ArrayList<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Object mLock = new Object();
    private volatile boolean isLock;

    public GetWatchMsgTask(WatchOpImpl impl, List<FatFile> list, OnWatchOpCallback<ArrayList<WatchInfo>> callback, ThreadStateListener listener) {
        mWatchOp = impl;
        taskList = list;
        mCallback = callback;
        mStateListener = listener;
    }

    @Override
    public synchronized void start() {
//        super.start();
        ThreadManager.getInstance().postRunnable(this);
    }

    @Override
    public void run() {
        if (null != mStateListener) mStateListener.onStart(getId());
        if (taskList == null || taskList.isEmpty()) {
            if (null != mStateListener) mStateListener.onFinish(getId());
            return;
        }
        synchronized (mLock) {
            for (final FatFile watchFile : taskList) {
                final String filePath = watchFile.getPath();
                JL_Log.d(TAG, "getWatchMessage", "filePath : " + filePath);
                mWatchOp.getWatchMessage(filePath, new OnWatchOpCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        final String watchMsg = result;
                        JL_Log.d(TAG, "getWatchMessage", "(onSuccess) ---> " + result);
                        mWatchOp.getCustomWatchBgInfo(filePath, new OnWatchOpCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                JL_Log.d(TAG, "getWatchMessage#getCustomWatchBgInfo", "(onSuccess) ---> " + result);
                                String customBgPath = result;
                                if (!"null".equalsIgnoreCase(customBgPath)) {
                                    customBgPath = "/" + HealthUtil.getFileNameByPath(result).toUpperCase();
                                }
                                String version = watchMsg;
                                String uuid = "";
                                if (version != null && version.contains(",")) {
                                    String[] array = version.split(",");
                                    if (array.length > 0) {
                                        version = array[0];
                                        if (array.length > 1) {
                                            uuid = array[1];
                                        }
                                    }
                                }
                                final DialExpandInfo dialExpandInfo = getDialExpandInfo();
                                WatchInfo watchInfo = new WatchInfo()
                                        .setWatchFile(watchFile)
                                        .setVersion(version)
                                        .setUuid(uuid)
                                        .setStatus(WatchInfo.WATCH_STATUS_EXIST)
                                        .setCustomBgFatPath(customBgPath)
                                        .setCircleDial(null == dialExpandInfo || dialExpandInfo.isCircular());
                                watchList.add(watchInfo);

                                unlock();
                            }

                            @Override
                            public void onFailed(BaseError error) {
                                JL_Log.e(TAG, "getWatchMessage#getCustomWatchBgInfo", "(onFailed) ---> " + error);
                                WatchInfo watchInfo = new WatchInfo()
                                        .setWatchFile(watchFile)
                                        .setStatus(WatchInfo.WATCH_STATUS_EXIST);
                                watchList.add(watchInfo);
                                unlock();
                            }
                        });
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        JL_Log.e(TAG, "getWatchMessage", "(onFailed) ---> " + error);
                        WatchInfo watchInfo = new WatchInfo()
                                .setWatchFile(watchFile)
                                .setStatus(WatchInfo.WATCH_STATUS_EXIST);
                        watchList.add(watchInfo);
                        unlock();
                    }
                });
                lock();
            }
            if (mCallback != null) {
                mHandler.post(() -> {
                    if (watchList.isEmpty()) {
                        mCallback.onFailed(new BaseError(FatFsErrCode.RES_RCSP_SEND, "request watch message failed."));
                    } else {
                        mCallback.onSuccess(watchList);
                    }
                });
            }
        }
        if (null != mStateListener) mStateListener.onFinish(getId());
    }

    private void lock() {
        synchronized (mLock) {
            if (isLock) return;
            try {
                isLock = true;
                mLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isLock = false;
        }
    }

    private void unlock() {
        synchronized (mLock) {
            if (!isLock) return;
            mLock.notify();
        }
    }

    private DialExpandInfo getDialExpandInfo() {
        WatchConfigure configure = mWatchOp.getWatchConfigure(mWatchOp.getConnectedDevice());
        if (null == configure) return null;
        return configure.getDialExpandInfo();
    }
}
