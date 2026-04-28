package com.jieli.healthaide.tool.watch;

import android.bluetooth.BluetoothDevice;

import com.jieli.jl_rcsp.tool.callback.BaseCallbackManager;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备配置监听器辅助类
 * @since 2022/10/26
 */
public class DeviceConfigureListenerHelper extends BaseCallbackManager<OnDeviceConfigureListener> implements OnDeviceConfigureListener {

    public void addListener(OnDeviceConfigureListener listener) {
        registerCallback(listener);
    }

    public void removeListener(OnDeviceConfigureListener listener) {
        unregisterCallback(listener);
    }

    @Override
    public void onUpdate(BluetoothDevice device) {
        callbackEvent(callback -> callback.onUpdate(device));
    }
}
