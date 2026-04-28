package com.jieli.watchtesttool.data.bean;

import android.bluetooth.BluetoothDevice;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/3/10
 */
public class DeviceConnectionData {
    private BluetoothDevice device;
    private int status;

    public DeviceConnectionData(BluetoothDevice device, int status){
        setDevice(device);
        setStatus(status);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    private void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeviceConnectionData{" +
                "device=" + device +
                ", status=" + status +
                '}';
    }
}
