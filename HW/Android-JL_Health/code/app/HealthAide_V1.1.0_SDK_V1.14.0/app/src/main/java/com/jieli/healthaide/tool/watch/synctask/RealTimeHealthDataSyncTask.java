package com.jieli.healthaide.tool.watch.synctask;

import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.model.HealthDataQuery;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @ClassName: RealTimeHealthDataSyncTask
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/10 16:52
 */
public class RealTimeHealthDataSyncTask extends DeviceSyncTask {
    private final HealthOpImpl mHealthOp;

    public RealTimeHealthDataSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
        mHealthOp = mWatchManager.getHealthOp();
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_HEALTH_DATA;
    }

    @Override
    public void start() {
        if (!mWatchManager.isWatchSystemOk()) {
            JL_Log.w(tag, "start", "Device is not connect.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        WatchConfigure configure = mWatchManager.getWatchConfigure(mWatchManager.getConnectedDevice());
        boolean isStepOpen = configure == null || (configure.getSportHealthConfigure() != null
                && configure.getSportHealthConfigure().isExistGSensor() && configure.getSportHealthConfigure().getGSensorFunc().isOpen());
        boolean isRateOpen = configure == null || (configure.getSportHealthConfigure() != null
                && configure.getSportHealthConfigure().isExistRate() && configure.getSportHealthConfigure().getRateFunc().isOpen());
        boolean isBloodOxygenOpen = configure == null || (configure.getSportHealthConfigure() != null
                && configure.getSportHealthConfigure().isExistBloodOxygen() && configure.getSportHealthConfigure().getBloodOxygenFunc().isOpen());
        int mask = 0;
        byte version = 0;
        byte[] subMask = new byte[0];
        if (isStepOpen) {
            mask |= 0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE;
            subMask = new byte[]{0x01};
        }
        if (isRateOpen) {
            mask |= 0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_STEP;
            byte[] temp = new byte[subMask.length + 1];
            System.arraycopy(subMask, 0, temp, 0, subMask.length);
            temp[temp.length - 1] = 0x07;
            subMask = temp;
        }
        if (isBloodOxygenOpen) {
            mask |= 0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN;
            byte[] temp = new byte[subMask.length + 1];
            System.arraycopy(subMask, 0, temp, 0, subMask.length);
            temp[temp.length - 1] = 0x07;
            subMask = temp;
        }
        if (mask == 0) {
            JL_Log.w(tag, "start", "Not support sensor.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        JL_Log.d(tag, "start", "HealthDataQuery >> version = " + version + ", isStepOpen = " + isStepOpen + ", isRateOpen = " + isRateOpen
                + ", isBloodOxygenOpen = " + isBloodOxygenOpen + ", subMask = " + CHexConver.byte2HexStr(subMask));
        mHealthOp.readHealthData(mWatchManager.getConnectedDevice(), new HealthDataQuery(version, mask, subMask), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (finishListener != null) finishListener.onFinish();
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "readHealthData", "onFailed : " + error);
                if (finishListener != null) finishListener.onFinish();
            }
        });
    }
}
