package com.jieli.healthaide.tool.watch;

import android.bluetooth.BluetoothDevice;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  设备配置监听器
 * @since 2022/10/26
 */
public interface OnDeviceConfigureListener {

    void onUpdate(BluetoothDevice device);
}
