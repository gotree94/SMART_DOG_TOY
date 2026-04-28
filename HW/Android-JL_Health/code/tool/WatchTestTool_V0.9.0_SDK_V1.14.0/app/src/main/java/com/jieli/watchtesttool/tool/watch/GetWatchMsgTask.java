package com.jieli.watchtesttool.tool.watch;

import android.os.Handler;
import android.os.Looper;

import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.listener.ThreadStateListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.watchtesttool.data.bean.WatchInfo;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
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
    public void run() {
        if (null != mStateListener) mStateListener.onStart(getId());
        if (taskList == null || taskList.isEmpty()) return;
        synchronized (mLock) {
            for (final FatFile fatFile : taskList) {
                isLock = false;
                mWatchOp.getWatchMessage(fatFile.getPath(), new OnWatchOpCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        final String watchMsg = result;
                        WLog.i(TAG, "getWatchMessage >>> -onSuccess- result = " + result + ", path = " + fatFile.getPath());
                        mWatchOp.getCustomWatchBgInfo(fatFile.getPath(), new OnWatchOpCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                WLog.w(TAG, "getCustomWatchBgInfo >>> -onSuccess- result = " + result + ", path = " + fatFile.getPath());
                                String customBgPath = result;
                                if(!"null".equals(customBgPath)){
                                    customBgPath = "/" + AppUtil.getFileNameByPath(result).toUpperCase();
                                }
                                WatchInfo watchInfo = new WatchInfo()
                                        .setName(fatFile.getName())
                                        .setFatFile(fatFile)
                                        .setVersion(watchMsg)
                                        .setSize(fatFile.getSize() * 4 * 1024)
                                        .setStatus(WatchInfo.WATCH_STATUS_EXIST)
                                        .setCustomBgFatPath(customBgPath);
                                watchList.add(watchInfo);

                                synchronized (mLock){
                                    if (isLock) {
                                        mLock.notify();
                                    }
                                }
                            }

                            @Override
                            public void onFailed(BaseError error) {
                                synchronized (mLock){
                                    if (isLock) {
                                        mLock.notify();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        synchronized (mLock){
                            if (isLock) {
                                mLock.notify();
                            }
                        }
                    }
                });
                isLock = true;
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isLock = false;
                }
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
}
