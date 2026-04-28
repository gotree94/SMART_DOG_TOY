package com.jieli.healthaide.tool.watch.synctask;

import android.content.Intent;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.watch.GetWatchMsgTask;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_health_http.model.WatchFileMsg;
import com.jieli.jl_rcsp.interfaces.listener.ThreadStateListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘列表同步任务
 * @since 2021/11/2
 */
public class WatchListSyncTask extends DeviceSyncTask {
    public static String INTENT_ACTION_WATCH_LIST = "com.jieli.healthaide.action.watch_list";
    private final static String TAG = WatchListSyncTask.class.getSimpleName();
    private final OnWatchOpCallback<ArrayList<WatchInfo>> callback;

    public WatchListSyncTask(OnWatchOpCallback<ArrayList<WatchInfo>> callback, SyncTaskFinishListener finishListener) {
        super(finishListener);
        this.callback = callback;
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DIAL_LIST_INFO;
    }

    @Override
    public void start() {
        if (!mWatchManager.isWatchSystemOk()) {
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        mWatchManager.listWatchList(new OnWatchOpCallback<ArrayList<FatFile>>() {
            @Override
            public void onSuccess(ArrayList<FatFile> result) {
                if (result == null) result = new ArrayList<>();
                mWatchManager.devFatFileList = result;
                ArrayList<FatFile> list = mWatchManager.getWatchList(result);
                if (!list.isEmpty()) {
                    new GetWatchMsgTask(mWatchManager, list, new CustomListWatchFileListCallback(callback), new ThreadStateListener() {
                        @Override
                        public void onStart(long threadId) {
                            JL_Log.w(TAG, "GetWatchMsgTask", "onStart");
                        }

                        @Override
                        public void onFinish(long threadId) {
                            JL_Log.w(TAG, "GetWatchMsgTask", "onFinish");
                            if (finishListener != null) finishListener.onFinish();
                        }
                    }).start();
                } else {
                    mWatchManager.watchInfoList.clear();
                    if (callback != null) callback.onSuccess(new ArrayList<>());
                    if (finishListener != null) finishListener.onFinish();
                }
            }

            @Override
            public void onFailed(BaseError error) {
                mWatchManager.watchInfoList.clear();
                if (callback != null) callback.onFailed(error);
                if (finishListener != null) finishListener.onFinish();
            }
        });
    }


    private final class CustomListWatchFileListCallback implements OnWatchOpCallback<ArrayList<WatchInfo>> {
        private final OnWatchOpCallback<ArrayList<WatchInfo>> mCallback;

        public CustomListWatchFileListCallback(OnWatchOpCallback<ArrayList<WatchInfo>> callback) {
            mCallback = callback;
        }

        @Override
        public void onSuccess(ArrayList<WatchInfo> result) {
            mWatchManager.watchInfoList.clear();
            mWatchManager.watchInfoList.addAll(result);
            for (WatchInfo info : mWatchManager.watchInfoList) {
                WatchFileMsg fileMsg = WatchServerCacheHelper.getInstance().getCacheWatchServerMsg(mWatchManager, info.getUuid());
                if (null == fileMsg) continue;
                info.setServerFile(fileMsg);
            }
            Intent intent = new Intent(INTENT_ACTION_WATCH_LIST);
            HealthApplication.getAppViewModel().getApplication().sendBroadcast(intent);
            if (mCallback != null) mCallback.onSuccess(mWatchManager.watchInfoList);
        }

        @Override
        public void onFailed(BaseError error) {
            mWatchManager.watchInfoList.clear();
            if (mCallback != null) mCallback.onFailed(error);
        }
    }
}
