package com.jieli.watchtesttool.ui.device;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.data.bean.ScanDevice;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothEventListener;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.util.WLog;

import java.util.List;

public class AddDeviceViewModel extends BluetoothViewModel {
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();
    private final BluetoothHelper mBluetoothHelper;

    public final MutableLiveData<Boolean> mBtAdapterStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mScanStatusMLD = new MutableLiveData<>();
    public final MutableLiveData<ScanDevice> mScanDeviceMLD = new MutableLiveData<>();

    private final static int SCAN_DEVICE_TIMEOUT = 30 * 1000;

    public AddDeviceViewModel(Context context) {
        mContext = context;
        mBluetoothHelper = mWatchManager.getBluetoothHelper();
        mBluetoothHelper.addBluetoothEventListener(mEventListener);
    }

    public void destroy() {
        super.destroy();
        mBluetoothHelper.removeBluetoothEventListener(mEventListener);
        stopScan();
    }

    public boolean isScanning() {
        return mBluetoothHelper.getBluetoothOp().isScanning();
    }

    public String getFilter() {
        return mConfigHelper.getAdvFilterPrefix();
    }

    public void changeFilter(String filter) {
        mConfigHelper.setAdvFilterPrefix(filter);
    }

    public void startScan() {
        if (isScanning()) {
            stopScan();
            SystemClock.sleep(100);
        }
        boolean ret = mBluetoothHelper.getBluetoothOp().startBLEScan(SCAN_DEVICE_TIMEOUT);
        JL_Log.i(tag, "startScan >> " + ret);
        if (!ret) {
            mScanStatusMLD.setValue(false);
        }
    }

    public void stopScan() {
        mBluetoothHelper.getBluetoothOp().stopBLEScan();
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

    @SuppressLint("MissingPermission")
    private void syncSystemBtDeviceList() {
        List<BluetoothDevice> mConnectedList = BluetoothUtil.getSystemConnectedBtDeviceList(mContext);
        if (null == mConnectedList || mConnectedList.isEmpty() || !ConnectUtil.isHasConnectPermission(mContext))
            return;
        String filterStr = getFilter();
        for (BluetoothDevice sysConnectDev : mConnectedList) {
            if ((TextUtils.isEmpty(filterStr) || (sysConnectDev.getName() != null && sysConnectDev.getName().contains(filterStr)))
                    && !mBluetoothHelper.isConnectedBtDevice(sysConnectDev) && !hasScanDevice(sysConnectDev)) {
                mScanDeviceMLD.setValue(new ScanDevice(sysConnectDev, new BleScanMessage(sysConnectDev.getAddress())));
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void syncSystemBleDeviceList() {
        List<BluetoothDevice> mConnectedList = BluetoothUtil.getConnectedBleDeviceList(mContext);
        if (null == mConnectedList || mConnectedList.isEmpty() || !ConnectUtil.isHasConnectPermission(mContext))
            return;
        String filterStr = getFilter();
        for (BluetoothDevice sysConnectDev : mConnectedList) {
            if ((TextUtils.isEmpty(filterStr) || (sysConnectDev.getName() != null && sysConnectDev.getName().contains(filterStr)))
                    && !mBluetoothHelper.isConnectedBtDevice(sysConnectDev) && !hasScanDevice(sysConnectDev)) {
                mScanDeviceMLD.setValue(new ScanDevice(sysConnectDev, new BleScanMessage(sysConnectDev.getAddress())));
            }
        }
    }

    private final BluetoothEventListener mEventListener = new BluetoothEventListener() {
        @Override
        public void onAdapterStatus(boolean bEnabled) {
            mBtAdapterStatusMLD.postValue(bEnabled);
        }

        @Override
        public void onBtDiscoveryStatus(boolean bBle, boolean bStart) {
            WLog.i(tag, "onBtDiscoveryStatus : " + bStart);
            mScanStatusMLD.setValue(bStart);
            if (bStart) {
                final BluetoothDevice device = getConnectedDevice();
                if (isConnected() && !hasScanDevice(device)) {
                    ScanDevice scanDevice = new ScanDevice(device, new BleScanMessage(device.getAddress()));
                    scanDevice.setConnectStatus(StateCode.CONNECTION_OK);
                    mScanDeviceMLD.setValue(scanDevice);
                }
                if (mConfigHelper.isSPPConnectWay()) {
                    syncSystemBtDeviceList();
                } else {
                    syncSystemBleDeviceList();
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onBtDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
            if (device != null) {
                WLog.d(tag, "onBtDiscovery : " + device + ",\n" + bleScanMessage);
                mScanDeviceMLD.setValue(new ScanDevice(device, bleScanMessage));
            }
        }
    };

    public static class Factory implements ViewModelProvider.Factory {
        private final Context mContext;

        public Factory(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AddDeviceViewModel(mContext);
        }
    }
}