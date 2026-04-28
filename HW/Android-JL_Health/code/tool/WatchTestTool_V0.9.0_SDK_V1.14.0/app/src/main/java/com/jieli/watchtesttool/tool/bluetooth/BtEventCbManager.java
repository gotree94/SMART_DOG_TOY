package com.jieli.watchtesttool.tool.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;

import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;

import java.util.ArrayList;

/**
 * 蓝牙事件回调管理类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class BtEventCbManager {
    private final ArrayList<BluetoothEventListener> mListeners = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public BtEventCbManager() {

    }

    public void addBluetoothEventListener(BluetoothEventListener listener) {
        if (null != listener && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeBluetoothEventListener(BluetoothEventListener listener) {
        if (null != listener) {
            mListeners.remove(listener);
        }
    }

    public void destroy() {
        mListeners.clear();
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 适配器状态回调
     *
     * @param bEnabled 是否开启
     */
    public void onAdapterStatus(final boolean bEnabled) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onAdapterStatus(bEnabled);
            }
        });
    }

    /**
     * 扫描设备的状态回调
     *
     * @param bBle   是否BLE类型
     * @param bStart 是否开始扫描
     */
    public void onBtDiscoveryStatus(final boolean bBle, final boolean bStart) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onBtDiscoveryStatus(bBle, bStart);
            }
        });
    }

    /**
     * 扫描到设备的回调
     *
     * @param device         蓝牙设备
     * @param bleScanMessage 扫描额外信息
     */
    public void onBtDiscovery(final BluetoothDevice device, final BleScanMessage bleScanMessage) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onBtDiscovery(device, bleScanMessage);
            }
        });
    }

    /**
     * 产品弹窗回调
     *
     * @param device         蓝牙设备
     * @param bleScanMessage 广播信息
     */
    public void onShowDialog(final BluetoothDevice device, final BleScanMessage bleScanMessage) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onShowDialog(device, bleScanMessage);
            }
        });
    }

    /**
     * 蓝牙MTU改变回调
     *
     * @param gatt   gatt控制对象
     * @param mtu    mtu
     * @param status 状态
     */
    public void onBleMtuChange(final BluetoothGatt gatt, final int mtu, final int status) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onBleMtuChange(gatt, mtu, status);
            }
        });
    }

    /**
     * 连接状态回调
     *
     * @param device 蓝牙设备
     * @param status 连接状态
     */
    public void onConnection(final BluetoothDevice device, final int status) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onConnection(device, status);
            }
        });
    }

    /**
     * 接收数据回调
     *
     * @param device 蓝牙设备
     * @param data   裸数据
     */
    public void onReceiveData(final BluetoothDevice device, final byte[] data) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onReceiveData(device, data);
            }
        });
    }

    /**
     * 切换已连接且正在使用的设备的回调
     *
     * @param device 已连接设备
     */
    public void onSwitchConnectedDevice(final BluetoothDevice device) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onSwitchConnectedDevice(device);
            }
        });
    }

    /**
     * 错误回调
     *
     * @param error 错误信息
     */
    public void onError(final ErrorInfo error) {
        callbackEvent(new BtEventCbImpl() {
            @Override
            public void onCallback(BluetoothEventListener listener) {
                listener.onError(error);
            }
        });
    }

    private void callbackEvent(BtEventCbImpl impl) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(new BtEventCbRunnable(impl));
        } else {
            new BtEventCbRunnable(impl).run();
        }
    }

    private abstract static class BtEventCbImpl {

        public abstract void onCallback(BluetoothEventListener listener);
    }

    private class BtEventCbRunnable implements Runnable {
        private final BtEventCbImpl mBtEventCb;

        private BtEventCbRunnable(BtEventCbImpl impl) {
            mBtEventCb = impl;
        }

        @Override
        public void run() {
            if (null == mBtEventCb) return;
            if (!mListeners.isEmpty()) {
                for (BluetoothEventListener listener : new ArrayList<>(mListeners)) {
                    if (null == listener) continue;
                    mBtEventCb.onCallback(listener);
                }
            }
        }
    }
}
