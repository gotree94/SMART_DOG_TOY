package com.jieli.healthaide.ui.device.bean;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.jl_rcsp.model.device.BatteryInfo;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  设备电量信息
 * @since 2021/7/9
 */
public class DevPowerMsg {
    private BluetoothDevice device;
    private BatteryInfo battery;

    public DevPowerMsg(BluetoothDevice device, BatteryInfo batteryInfo){
        setDevice(device);
        setBattery(batteryInfo);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BatteryInfo getBattery() {
        return battery;
    }

    public void setBattery(BatteryInfo battery) {
        this.battery = battery;
    }

    @NonNull
    @Override
    public String toString() {
        return "DevPowerMsg{" +
                "device=" + device +
                ", battery=" + battery +
                '}';
    }
}
