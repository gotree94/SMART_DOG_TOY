package com.jieli.healthaide.ui.home;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;

import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.history.HistoryRecordManager;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.tool.ring.RingHandler;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.SearchDevCmd;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.model.parameter.SearchDevParam;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 主页面逻辑操作
 * @since 2021/3/19
 */
public class HomeViewModel extends WatchViewModel {
    private final static String TAG = HomeViewModel.class.getSimpleName();
    public final MutableLiveData<WatchOpData> mWatchRestoreSysMLD = new MutableLiveData<>();
    public final MutableLiveData<BluetoothDevice> mWatchUpdateExceptionMLD = new MutableLiveData<>();
    public final MutableLiveData<BluetoothDevice> mMandatoryUpgradeMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mRingPlayStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<NetworkInfo> mNetworkExceptionMLD = new MutableLiveData<>();

    private final RingHandler mRingHandler = RingHandler.getInstance();

    public HomeViewModel() {
        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
        mRingHandler.registerOnRingStatusListener(mRingPlayStatusMLD::postValue);
    }

    public void release() {
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
    }

    public void destroy() {
        release();
        mRingHandler.destroy();
        mWatchManager.release();
        mBluetoothHelper.destroy();
        NetworkStateHelper.getInstance().destroy();
        WatchServerCacheHelper.getInstance().destroy();
        HistoryRecordManager.getInstance().release();
        NotificationHelper.getInstance().destroy();
        SyncTaskManager.getInstance().destroy();
    }

    public void fastConnect() {
        mBluetoothHelper.getBluetoothOp().fastConnect();
    }

    public void stopRing() {
        mRingHandler.stopAlarmRing();
        if (mWatchManager.isWatchSystemOk()) {
            mWatchManager.sendRcspCommand(CommandBuilder.buildSearchDevCmd(RcspConstant.RING_OP_CLOSE, 0), null);
        }
    }

    private String printDeviceInfo(BluetoothDevice device) {
        return BluetoothUtil.printBtDeviceInfo(HealthApplication.getAppViewModel().getApplication(), device);
    }

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            JL_Log.e(TAG, "onMandatoryUpgrade", "device : " + printDeviceInfo(device));
            mMandatoryUpgradeMLD.setValue(device);
        }

        @Override
        public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
            JL_Log.e(TAG, "onWatchSystemException", "device : " + printDeviceInfo(device) + ", sysStatus = " + sysStatus);
            if (sysStatus != 0 && isUsingDevice(device)) {
                mWatchManager.restoreWatchSystem(new OnFatFileProgressListener() {
                    @Override
                    public void onStart(String filePath) {
                        WatchOpData watchOpData = new WatchOpData();
                        watchOpData.setOp(WatchOpData.OP_RESTORE_SYS);
                        watchOpData.setState(WatchOpData.STATE_START);
                        watchOpData.setFilePath(filePath);
                        mWatchRestoreSysMLD.setValue(watchOpData);
                    }

                    @Override
                    public void onProgress(float progress) {
                        WatchOpData watchOpData = new WatchOpData();
                        watchOpData.setOp(WatchOpData.OP_RESTORE_SYS);
                        watchOpData.setState(WatchOpData.STATE_PROGRESS);
                        watchOpData.setProgress(progress);
                        mWatchRestoreSysMLD.setValue(watchOpData);
                    }

                    @Override
                    public void onStop(int result) {
                        WatchOpData watchOpData = new WatchOpData();
                        watchOpData.setOp(WatchOpData.OP_RESTORE_SYS);
                        watchOpData.setState(WatchOpData.STATE_END);
                        watchOpData.setResult(result);
                        mWatchRestoreSysMLD.setValue(watchOpData);
                    }
                });
            }
        }

        @Override
        public void onResourceUpdateUnfinished(BluetoothDevice device) {
            boolean isBleChangeSpp = mWatchManager.isBleChangeSpp();
            JL_Log.e(TAG, "onResourceUpdateUnfinished", "device : " + printDeviceInfo(device) + ", isBleChangeSpp = " + isBleChangeSpp);
            if (isBleChangeSpp) return;
            mWatchUpdateExceptionMLD.setValue(device);
        }

        @Override
        public void onNetworkModuleException(BluetoothDevice device, NetworkInfo info) {
            boolean isBleChangeSpp = mWatchManager.isBleChangeSpp();
            JL_Log.e(TAG, "onNetworkModuleException", "device : " + device + ", " + info + ", isBleChangeSpp = " + isBleChangeSpp);
            if (isBleChangeSpp) return;
            mNetworkExceptionMLD.setValue(info);
        }

        @Override
        public void onRcspCommand(BluetoothDevice device, CommandBase command) {
            if (command.getId() == Command.CMD_SEARCH_DEVICE) {//查找设备的处理
                SearchDevCmd searchDevCmd = (SearchDevCmd) command;
                WatchConfigure configure = mWatchManager.getWatchConfigure(device);
                boolean isAllowSearch = configure == null || (configure.getFunctionOption() != null
                        && configure.getFunctionOption().isSupportSearchDevice());
                if (isAllowSearch) {
                    SearchDevParam param = searchDevCmd.getParam();
                    if (param != null) {
                        if (param.getOp() == RcspConstant.RING_OP_OPEN) {
                            mRingHandler.playAlarmRing(param.getType(), param.getTimeoutSec() * 1000L);
                            mRingPlayStatusMLD.postValue(true);
                        } else {
                            mRingHandler.stopAlarmRing();
                            mRingPlayStatusMLD.postValue(false);
                        }
                    }
                }
                searchDevCmd.setStatus(StateCode.STATUS_SUCCESS);
                searchDevCmd.setParam(new SearchDevParam.SearchDevResultParam(0));
                mWatchManager.sendCommandResponse(device, searchDevCmd, null);
            }
        }
    };
}
