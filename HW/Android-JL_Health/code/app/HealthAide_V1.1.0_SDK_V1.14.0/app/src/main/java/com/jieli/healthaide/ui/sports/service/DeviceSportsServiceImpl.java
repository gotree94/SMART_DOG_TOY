package com.jieli.healthaide.ui.sports.service;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.annotation.NonNull;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.sports.listener.RealDataListener;
import com.jieli.healthaide.ui.sports.model.DeviceRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_dialog.BuildConfig;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.watch.SportsInfoStatusSyncCmd;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc : 固件相关运动控制服务
 */
public class DeviceSportsServiceImpl extends AbstractSportsServerImpl<DeviceRealData> implements SportsService {

    private final String tag = DeviceSportsServiceImpl.class.getSimpleName();
    private final WatchManager mWatchManager = WatchManager.getInstance();
    private final HealthOpImpl mHealthOp;
    private final DeviceSyncRealDataServiceImpl syncRealDataService;
    private final SportsInfo sportsInfo;


    public DeviceSportsServiceImpl(Context context, @NonNull SportsInfo sportsInfo) {
        // Context mContext = context.getApplicationContext();
        this.sportsInfo = sportsInfo;
        mHealthOp = mWatchManager.getHealthOp();
        syncRealDataService = new DeviceSyncRealDataServiceImpl(context);
    }

    @Override
    public void setRealDataListener(RealDataListener<DeviceRealData> realDataListener) {
        syncRealDataService.setRealDataListener(realDataListener);
    }

    @Override
    public void start() {
        addListener();
        mHealthOp.readSportsInfo(mWatchManager.getConnectedDevice(), new OnOperationCallback<com.jieli.jl_rcsp.model.device.health.SportsInfo>() {
            @Override
            public void onSuccess(com.jieli.jl_rcsp.model.device.health.SportsInfo result) {
                if (BuildConfig.DEBUG && sportsInfo.type != result.getMode()) {
                    ToastUtil.showToastLong(R.string.inconsistent_motion_state + result.getMode());
                }
                sportsInfo.type = result.getMode();//运动类型
                sportsInfo.useMap = result.getMode() == SportsInfoStatusSyncCmd.SPORTS_TYPE_OUTDOOR; //运动类型
                sportsInfo.status = result.getState(); //运动状态
                sportsInfo.id = result.getId(); //运动id
                sportsInfo.startTime = RcspUtil.intToTime(result.getId()); //运动开始时间
                sportsInfo.readRealDataInterval = result.getReadRealTimeDataInterval(); //同步运动实时数据的时间间隔
                sportsInfo.heartRateMode = result.getHeartRateMode();//运动的心率模式
                JL_Log.d(tag, "readSportsInfo", sportsInfo + ",\n" + result);
                changeStatus(sportsInfo.status);
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "readSportsInfo", "获取运动数据失败： " + error);
            }
        });
    }

    @Override
    public void pause() {
        mHealthOp.pauseSports(mWatchManager.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "pauseSports", "主动暂停失败: " + error);
            }
        });
    }

    @Override
    public void resume() {
        mHealthOp.resumeSports(mWatchManager.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "resumeSports", "主动恢复运动失败: " + error);
                changeStatus(SportsInfo.STATUS_FAILED);
            }
        });
    }

    @Override
    public void stop() {
        if (sportsInfo.status == SportsInfo.STATUS_STOP) return;
        mHealthOp.stopSports(mWatchManager.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "stopSports", "主动停止运动失败:" + error);
                changeStatus(SportsInfo.STATUS_FAILED);
            }
        });
    }

    private void addListener() {
        mWatchManager.registerOnRcspCallback(onRcspCallback);
        mWatchManager.registerOnRcspEventListener(mOnRcspEventListener);
    }

    private void removeListener() {
        mWatchManager.unregisterOnRcspCallback(onRcspCallback);
        mWatchManager.unregisterOnRcspEventListener(mOnRcspEventListener);
    }

    private final OnRcspCallback onRcspCallback = new OnRcspCallback() {

        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            super.onConnectStateChange(device, status);
            if (status != StateCode.CONNECTION_OK) {
                removeListener();
                changeStatus(SportsInfo.STATUS_FAILED);
            }
        }
    };

    private final OnRcspEventListener mOnRcspEventListener = new OnRcspEventListener() {
        @Override
        public void onSportsState(BluetoothDevice device, int state) {
            if (state == SportsInfo.STATUS_STOP) { //运动结束
                com.jieli.jl_rcsp.model.device.health.SportsInfo info = mWatchManager.getDeviceInfo(device).getSportsInfo();
                if (info == null) return;
                removeListener();
                sportsInfo.file = new QueryFileTask.File(QueryFileTask.TYPE_SPORTS_RECORD, info.getRecordFileId(), info.getRecordFileSize());
            }
            changeStatus(state);
        }
    };

    private void changeStatus(int status) {
        JL_Log.e(tag, "changeStatus", "time:" + CalendarUtil.serverDateFormat().format(RcspUtil.intToTime(this.sportsInfo.id)) + ", status = " + status);
        SportsInfo sportsInfo = this.sportsInfo;
        sportsInfo.status = status;
        sportsInfoListener.onSportsInfoChange(sportsInfo);
        syncRealDataService.setSportInfo(sportsInfo);
        if (sportsInfo.status == SportsInfo.STATUS_BEGIN || sportsInfo.status == SportsInfo.STATUS_RESUME) {
            syncRealDataService.start();
        } else if (sportsInfo.status == SportsInfo.STATUS_PAUSE) {
            syncRealDataService.stop();
        } else {
//            readSportsRecord();
            syncRealDataService.stop();
        }
    }


}
