package com.jieli.healthaide.tool.watch.synctask;

import android.text.TextUtils;

import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/12
 * @desc : app闪退重启后会导致uid丢失，在执行部分任务的时候要重新获取uid
 */
public class RequestUidSyncTask extends AbstractSyncTask {
    public RequestUidSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
    }

    @Override
    public int getType() {
        return TASK_TYPE_REQUEST_UID;
    }

    @Override
    public void start() {
        String uid = HealthApplication.getAppViewModel().getUid();
        if (!TextUtils.isEmpty(uid)) {
            finishListener.onFinish();
            return;
        }
        ThreadManager.getInstance().postRunnable(() -> HealthApplication.getAppViewModel().requestProfile(new OperatCallback() {
            @Override
            public void onSuccess() {
                finishListener.onFinish();
            }

            @Override
            public void onError(int code) {
                finishListener.onFinish();
                JL_Log.d(tag, "onError", "获取uid失败 ---> " + code);
            }
        }));
    }
}
