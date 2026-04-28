package com.jieli.watchtesttool.data.bean;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.watchtesttool.util.AppUtil;

/**
 * 发现设备信息
 *
 * @author zqjasonZhong
 * @since 2021/3/10
 */
public class ScanDevice {
    private int connectStatus = BluetoothConstant.CONNECT_STATE_DISCONNECT;
    private BluetoothDevice mDevice;
    private BleScanMessage mBleScanMessage;

    public ScanDevice(BluetoothDevice device, BleScanMessage scanMessage) {
        setDevice(device);
        setBleScanMessage(scanMessage);
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    private void setDevice(BluetoothDevice device) {
        mDevice = device;
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
                "mDevice=" + AppUtil.printBtDeviceInfo(mDevice) +
                ", connectStatus=" + connectStatus +
                ", mBleScanMessage=" + mBleScanMessage +
                '}';
    }
}
