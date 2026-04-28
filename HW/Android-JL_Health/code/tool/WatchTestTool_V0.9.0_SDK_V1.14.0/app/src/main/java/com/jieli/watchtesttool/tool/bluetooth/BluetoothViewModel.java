package com.jieli.watchtesttool.tool.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.data.bean.DeviceConnectionData;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;

/**
 * 蓝牙控制
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class BluetoothViewModel extends ViewModel {
    protected String tag = getClass().getSimpleName();
    protected final BluetoothHelper mBluetoothHelper = BluetoothHelper.getInstance();
    protected final WatchManager mWatchManager = WatchManager.getInstance();
    public final MutableLiveData<DeviceConnectionData> mConnectionDataMLD = new MutableLiveData<>();

    public BluetoothViewModel() {
        mBluetoothHelper.addBluetoothEventListener(mBluetoothEventListener);
        mWatchManager.registerOnWatchCallback(mWatchCallback);

        if (isConnected()) {
            mConnectionDataMLD.setValue(new DeviceConnectionData(getConnectedDevice(), StateCode.CONNECTION_OK));
        }
    }

    public void destroy() {
        mBluetoothHelper.removeBluetoothEventListener(mBluetoothEventListener);
        mWatchManager.unregisterOnWatchCallback(mWatchCallback);
    }

    public int getDeviceConnection(BluetoothDevice device) {
        return mWatchManager.getBluetoothHelper().getConnectionStatus(device);
    }

    public boolean isConnectedDevice(BluetoothDevice device) {
        return mWatchManager.isDeviceConnected(device);
    }

    public boolean isConnected() {
        return isConnectedDevice(getConnectedDevice());
    }

    public BluetoothDevice getConnectedDevice() {
        return mWatchManager.getConnectedDevice();
    }

    public Context getContext() {
        return WatchApplication.getWatchApplication().getApplicationContext();
    }

    private final BluetoothEventListener mBluetoothEventListener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            int newState = AppUtil.convertWatchConnectStatus(status);
            if (newState != StateCode.CONNECTION_OK) {
                mConnectionDataMLD.setValue(new DeviceConnectionData(device, newState));
            }
        }
    };

    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            mConnectionDataMLD.setValue(new DeviceConnectionData(getConnectedDevice(), StateCode.CONNECTION_OK));
        }

        @Override
        public void onWatchSystemInit(int code) {
            if (code == WatchError.ERR_NONE) {
                mConnectionDataMLD.setValue(new DeviceConnectionData(getConnectedDevice(), StateCode.CONNECTION_OK));
            }
        }
    };
}
