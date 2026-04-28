package com.jieli.healthaide.tool.watch.synctask;

import android.app.Activity;
import android.os.Bundle;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.sports.ui.RunningParentFragment;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.health.SportsInfo;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/4
 * @desc : 同步设备状态任务
 */
public class SyncSportsStatusTask extends DeviceSyncTask {
    private final HealthOpImpl mHealthOp;

    public SyncSportsStatusTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
        mHealthOp = mWatchManager.getHealthOp();
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_SPORT_STATUS;
    }

    @Override
    public void start() {
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        mHealthOp.readSportsInfo(mHealthOp.getConnectedDevice(), new OnOperationCallback<SportsInfo>() {
            @Override
            public void onSuccess(SportsInfo result) {
                if (result.getState() != StateCode.SPORT_STATE_NONE) {
                    toSportsUi(result.getMode());
                }
                if (finishListener != null) finishListener.onFinish();
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "onFailed", "获取运动状态失败：" + error);
                if (finishListener != null) finishListener.onFinish();
            }
        });
    }

    static void toSportsUi(int type) {
        JL_Log.d(SyncSportsStatusTask.class.getSimpleName(), "toSportsUi", "运动模式：" + type);
        if (type < 1) return;
        Activity activity = ActivityManager.getInstance().getTopActivity();
        if (activity == null) return;
        int cacheSportMode = ((HealthApplication) HealthApplication.getAppViewModel().getApplication()).getSportMode();
        JL_Log.d(SyncSportsStatusTask.class.getSimpleName(), "toSportsUi", "cacheSportMode : " + cacheSportMode);
        if (cacheSportMode == type) return;//相同运动模式，不进行处理
        Bundle bundle = new Bundle();
        bundle.putInt(RunningParentFragment.KEY_RUNNING_TYPE, type);
        ContentActivity.startContentActivity(activity, RunningParentFragment.class.getCanonicalName(), bundle);
    }
}
