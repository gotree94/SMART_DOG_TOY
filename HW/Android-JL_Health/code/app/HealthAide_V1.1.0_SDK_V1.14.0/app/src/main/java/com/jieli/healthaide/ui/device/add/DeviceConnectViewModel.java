package com.jieli.healthaide.ui.device.add;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.constant.JL_DeviceType;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.BluetoothViewModel;
import com.jieli.healthaide.ui.device.bean.DeviceConnectionData;
import com.jieli.healthaide.ui.device.bean.ScanDevice;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.util.JL_Log;

public class DeviceConnectViewModel extends BluetoothViewModel {
    private final WatchManager mWatchManager = WatchManager.getInstance();
    public final MutableLiveData<Boolean> mBtAdapterStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mScanStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<ScanDevice> mScanDeviceMLD = new MutableLiveData<>();
    public final MutableLiveData<DeviceConnectionData> mConnectionDataMLD = new MutableLiveData<>();

    public final static int SCAN_DEVICE_TIMEOUT = 30 * 1000;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());


    public DeviceConnectViewModel() {
        super();
        mBluetoothHelper.addBluetoothEventListener(mEventCallback);
        mWatchManager.registerOnWatchCallback(mWatchCallback);
        if (isScanning()) {
            mScanStatusMLD.setValue(true);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        release();
    }

    private void release() {
        mBluetoothHelper.removeBluetoothEventListener(mEventCallback);
        mWatchManager.unregisterOnWatchCallback(mWatchCallback);
        stopScan();
        uiHandler.removeCallbacksAndMessages(null);
    }

    public boolean isSkipDevice(BluetoothDevice device) {
        return mWatchManager.isBleChangeSpp() && mBluetoothHelper.isBleChangeSpp(device);
    }

    public boolean isScanning() {
        return mBluetoothHelper.getBluetoothOp().isScanning();
    }

    public boolean scanDevice() {
        boolean ret;
        if (HealthConstant.DEFAULT_CONNECT_WAY == BluetoothConstant.PROTOCOL_TYPE_SPP) {
            ret = mBluetoothHelper.getBluetoothOp().startDeviceScan(SCAN_DEVICE_TIMEOUT);
        } else {
            ret = mBluetoothHelper.getBluetoothOp().startBLEScan(SCAN_DEVICE_TIMEOUT);
        }
        JL_Log.i(tag, "scanDevice", "" + ret);
        return ret;
    }

    public boolean stopScan() {
//        isAutoScan = false;
        if (mBluetoothHelper.getBluetoothOp().getScanType() == BluetoothConstant.SCAN_TYPE_BLE) {
            return mBluetoothHelper.getBluetoothOp().stopBLEScan();
        } else {
            return mBluetoothHelper.getBluetoothOp().stopDeviceScan();
        }
    }

    public void connectDevice(BluetoothDevice device, BleScanMessage bleScanMessage) {
        mBluetoothHelper.connectDevice(device, bleScanMessage);
    }

    public void disconnectDevice(BluetoothDevice device) {
        mBluetoothHelper.disconnectDevice(device);
    }

    private boolean hasScanDevice(BluetoothDevice device) {
        if (device == null) return false;
        return mBluetoothHelper.getBluetoothOp().getDiscoveredBluetoothDevices().contains(device);
    }

    private final BluetoothEventListener mEventCallback = new BluetoothEventListener() {
        @Override
        public void onAdapterStatus(boolean bEnabled) {
            mBtAdapterStatusMLD.postValue(bEnabled);
        }

        @Override
        public void onBtDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
            if (device != null && bleScanMessage.getDeviceType() == JL_DeviceType.JL_DEVICE_TYPE_WATCH) {
                mScanDeviceMLD.setValue(new ScanDevice(device, bleScanMessage));
            }
        }

        @Override
        public void onBtDiscoveryStatus(boolean bBle, boolean bStart) {
            JL_Log.i(tag, "onBtDiscoveryStatus", "bStart = " + bStart);
            mScanStatusMLD.setValue(bStart);
            /*if (!bStart && isAutoScan) {
                scanDevice();
            }*/
        }
    };

    //回调设备初始化成功
    private final Runnable cbDeviceConnected = () -> {
        boolean isBleChangeSpp = mWatchManager.isBleChangeSpp();
        JL_Log.w(tag, "cbDeviceConnected", "isBleChangeSpp = " + isBleChangeSpp);
        if (isBleChangeSpp) return;
        mConnectionDataMLD.setValue(new DeviceConnectionData(getConnectedDevice(), StateCode.CONNECTION_OK));
    };

    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onWatchSystemInit(int code) {
            if (code == 0) {
                uiHandler.removeCallbacks(cbDeviceConnected);
                uiHandler.postDelayed(cbDeviceConnected, 500L);
            }
        }

        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            boolean isSkipCallback = mWatchManager.isBleChangeSpp(); /*&& status == StateCode.CONNECTION_DISCONNECT*/
            JL_Log.i(tag, "onConnectStateChange", "device = " + device + ", isSkipCallback = " + isSkipCallback
                    + ", status = " + status);
            if (status == StateCode.CONNECTION_DISCONNECT) {
                uiHandler.removeCallbacks(cbDeviceConnected);
            }
            if (isSkipCallback) {
                JL_Log.w(tag, "onConnectStateChange", "skip callback.");
                return;
            }
            if (status != StateCode.CONNECTION_OK) {
                mConnectionDataMLD.setValue(new DeviceConnectionData(device, status));
            }
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            mConnectionDataMLD.setValue(new DeviceConnectionData(device, StateCode.CONNECTION_OK));
        }
    };

}