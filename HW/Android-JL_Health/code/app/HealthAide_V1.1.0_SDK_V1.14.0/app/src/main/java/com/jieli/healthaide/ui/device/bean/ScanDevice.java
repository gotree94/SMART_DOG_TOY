package com.jieli.healthaide.ui.device.bean;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.healthaide.util.HealthUtil;

/**
 * 发现设备信息
 *
 * @author zqjasonZhong
 * @since 2021/3/10
 */
public class ScanDevice {
    private final BluetoothDevice mDevice;
    private int connectStatus;
    private BleScanMessage mBleScanMessage;

    public ScanDevice(@NonNull BluetoothDevice device, BleScanMessage scanMessage) {
        mDevice = device;
        setBleScanMessage(scanMessage);
    }

    @NonNull
    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public BleScanMessage getBleScanMessage() {
        return mBleScanMessage;
    }

    public void setBleScanMessage(BleScanMessage bleScanMessage) {
        mBleScanMessage = bleScanMessage;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    @Override
    public String toString() {
        return "ScanDevice{" +
                "mDevice=" + HealthUtil.printBtDeviceInfo(mDevice) +
                ", connectStatus=" + connectStatus +
                ", mBleScanMessage=" + mBleScanMessage +
                '}';
    }
}
