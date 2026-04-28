package com.jieli.watchtesttool.tool.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;

/**
 * 蓝牙事件监听器抽象类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public abstract class BluetoothEventListener {

    /**
     * 适配器状态回调
     *
     * @param bEnabled 是否开启
     */
    public void onAdapterStatus(boolean bEnabled) {

    }

    /**
     * 扫描设备的状态回调
     *
     * @param bBle   是否BLE类型
     * @param bStart 是否开始扫描
     */
    public void onBtDiscoveryStatus(boolean bBle, boolean bStart) {

    }

    /**
     * 扫描到设备的回调
     *
     * @param device         蓝牙设备
     * @param bleScanMessage 扫描额外信息
     */
    public void onBtDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {

    }

    /**
     * 产品弹窗回调
     *
     * @param device         蓝牙设备
     * @param bleScanMessage 广播信息
     */
    public void onShowDialog(BluetoothDevice device, BleScanMessage bleScanMessage) {

    }

    /**
     * 蓝牙MTU改变回调
     *
     * @param gatt   gatt控制对象
     * @param mtu    mtu
     * @param status 状态
     */
    public void onBleMtuChange(BluetoothGatt gatt, int mtu, int status) {

    }

    /**
     * 连接状态回调
     *
     * @param device 蓝牙设备
     * @param status 连接状态
     */
    public void onConnection(BluetoothDevice device, int status) {

    }

    /**
     * 接收数据回调
     *
     * @param device 蓝牙设备
     * @param data   裸数据
     */
    public void onReceiveData(BluetoothDevice device, byte[] data) {

    }

    /**
     * 切换已连接且正在使用的设备的回调
     *
     * @param device 已连接设备
     */
    public void onSwitchConnectedDevice(BluetoothDevice device) {

    }

    /**
     * 错误回调
     *
     * @param error 错误信息
     */
    public void onError(ErrorInfo error) {

    }

}
