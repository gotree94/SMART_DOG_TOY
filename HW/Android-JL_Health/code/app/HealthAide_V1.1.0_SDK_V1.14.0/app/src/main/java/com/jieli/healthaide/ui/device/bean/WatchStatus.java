package com.jieli.healthaide.ui.device.bean;

import android.bluetooth.BluetoothDevice;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  手表状态
 * @since 2021/3/11
 */
public class WatchStatus {
    private BluetoothDevice device;
    private int exception;

    public WatchStatus(BluetoothDevice device){
        setDevice(device);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getException() {
        return exception;
    }

    public void setException(int exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "WatchStatus{" +
                "device=" + device +
                ", exception=" + exception +
                '}';
    }
}
