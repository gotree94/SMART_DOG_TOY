package com.jieli.watchtesttool.ui.home;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;

import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.watchtesttool.data.bean.WatchOpData;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.upgrade.fileobserver.OtaFileObserverHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 主界面
 * @since 2021/4/23
 */
public class MainViewModel extends BluetoothViewModel {
    private final static String TAG = "MainActivity";
    private final BluetoothHelper mBluetoothHelper;
    public final MutableLiveData<Integer> mWatchSysStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<WatchOpData> mWatchRestoreSysMLD = new MutableLiveData<>();
    public final MutableLiveData<BluetoothDevice> mWatchUpdateExceptionMLD = new MutableLiveData<>();
    public final MutableLiveData<BluetoothDevice> mMandatoryUpgradeMLD = new MutableLiveData<>();

    public final MutableLiveData<NetworkInfo> mNetworkExceptionMLD = new MutableLiveData<>();

    public MainViewModel() {
        mBluetoothHelper = mWatchManager.getBluetoothHelper();
        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
    }

    public WatchManager getWatchManager() {
        return mWatchManager;
    }

    public void disconnectDevice(BluetoothDevice device) {
        mBluetoothHelper.disconnectDevice(device);
    }

    public void destroy() {
        super.destroy();
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
        mWatchManager.release();
        mBluetoothHelper.destroy();
        OtaFileObserverHelper.getInstance().destroy();
    }

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {

        @Override
        public void onWatchSystemInit(int i) {
            mWatchSysStatusMLD.postValue(i);
        }

        @Override
        public void onWatchSystemException(BluetoothDevice device, int sysStatus) {
            WLog.e(TAG, "-onWatchSystemException- device = " + AppUtil.printBtDeviceInfo(device) + ", sysStatus = " + sysStatus);
            if (sysStatus != 0 && isConnectedDevice(device)) {
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
            mWatchUpdateExceptionMLD.postValue(device);
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            mMandatoryUpgradeMLD.postValue(device);
        }

        @Override
        public void onNetworkModuleException(BluetoothDevice device, NetworkInfo info) {
            mNetworkExceptionMLD.setValue(info);
        }
    };
}
