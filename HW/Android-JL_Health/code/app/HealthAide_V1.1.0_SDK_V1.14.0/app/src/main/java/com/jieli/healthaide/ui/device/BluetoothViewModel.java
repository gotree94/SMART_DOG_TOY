package com.jieli.healthaide.ui.device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;

/**
 * 蓝牙控制
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class BluetoothViewModel extends ViewModel {
    protected String tag = getClass().getSimpleName();
    public final BluetoothHelper mBluetoothHelper = BluetoothHelper.getInstance();

    public int getDeviceConnection(BluetoothDevice device) {
        return mBluetoothHelper.getConnectionStatus(device);
    }

    public boolean isConnectedDevice(BluetoothDevice device) {
        return mBluetoothHelper.isConnectedBtDevice(device);
    }

    public boolean isUsingDevice(BluetoothDevice device) {
        return mBluetoothHelper.isUsedBtDevice(device);
    }

    public boolean isMatchDevice(BluetoothDevice device, BluetoothDevice device1) {
        return mBluetoothHelper.getBluetoothOp().isMatchDevice(device, device1);
    }

    public boolean isConnected() {
        return mBluetoothHelper.isConnectedDevice();
    }

    public BluetoothDevice getConnectedDevice() {
        return mBluetoothHelper.getConnectedBtDevice();
    }

    public void disconnectDevice(BluetoothDevice device) {
        mBluetoothHelper.disconnectDevice(device);
    }

    public void connectHistoryRecord(HistoryRecord record, OnHistoryRecordCallback callback) {
        mBluetoothHelper.connectHistoryRecord(record, callback);
    }

    public Context getContext() {
        return HealthApplication.getAppViewModel().getApplication();
    }
}
