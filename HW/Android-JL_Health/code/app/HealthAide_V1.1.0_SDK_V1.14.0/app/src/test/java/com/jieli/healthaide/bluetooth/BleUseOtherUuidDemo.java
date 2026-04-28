package com.jieli.healthaide.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.impl.BluetoothBle;
import com.jieli.bluetooth_connect.impl.BluetoothCore;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback;
import com.jieli.bluetooth_connect.interfaces.listener.OnBtBleListener;
import com.jieli.bluetooth_connect.interfaces.listener.OnWriteDataCallback;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc BLE使用其他UUID示例
 * @since 2023/1/4
 */
public class BleUseOtherUuidDemo {
    private static final String TAG = BleUseOtherUuidDemo.class.getSimpleName();
    private static final UUID serverUUID = UUID.fromString("自定义的服务UUID");
    private static final UUID notifyCharacteristicsUUID = UUID.fromString("自定义的通知特征UUID");
    private static final UUID writeCharacteristicsUUID = UUID.fromString("自定义的写特征UUID");
    //BLE的通知特征的描述符UUID
    public final static UUID BLE_UUID_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private boolean isEnableNotify = false; //是否使能通知特征成功

    private final BluetoothManager bluetoothManager = BluetoothHelper.getInstance().getBluetoothOp(); //获取蓝牙管理器对象
    private final BluetoothBle bluetoothBle = BluetoothBle.getInstance(); //获取BLE管理对象

    private static final int DEFAULT_TIMEOUT = 6000;

    private static final int MSG_DISCOVERY_SERVER = 0x01;
    private static final int MSG_ENABLE_CUSTOM_UUID = 0x02;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_DISCOVERY_SERVER: {
                    //发现服务超时， 视为发现服务失败情况处理
                    if (msg.obj instanceof BluetoothGatt) {
                        BluetoothGatt gatt = (BluetoothGatt) msg.obj;
                        //TODO: 回调设备连接异常
//                    gatt.disconnect(); //可以判断异常BLE链接
                    }
                    break;
                }
                case MSG_ENABLE_CUSTOM_UUID: {
                    //使能UUID超时，按照使能UUID失败处理
                    if (msg.obj instanceof BluetoothGatt) {
                        BluetoothGatt gatt = (BluetoothGatt) msg.obj;
                        //TODO: 处理使能UUID失败的情况
                    }
                    break;
                }
            }
            return true;
        }
    });

    @Test
    public void init() {
        bluetoothManager.registerBluetoothCallback(mBluetoothEventCallback); //监听监听器
        bluetoothBle.addOnBtBleListener(mBtBleListener); //添加监听器
    }

    @Test
    public boolean sendCustomData(byte[] data) {
        if (!bluetoothBle.isConnectedGattDevice(getConnectedDevice())) { //设备未连接
            return false;
        }
        //TODO: 视情况而定，如果需要设备回复数据的，必须等待使能成功。反之，则不用
        if (!isEnableNotify) { //还没使能成功
            return false;
        }
        bluetoothBle.writeDataByBleAsync(getConnectedDevice(), serverUUID, writeCharacteristicsUUID, data, new OnWriteDataCallback() {
            @Override
            public void onBleResult(BluetoothDevice device, UUID serviceUUID, UUID characteristicUUID, boolean result, byte[] data) {
                //异步回调结果返回
            }
        });
        return true;
    }

    @Test
    public void release() {
        isEnableNotify = false;
        bluetoothManager.disconnectBtDevice(getConnectedDevice());
        bluetoothManager.unregisterBluetoothCallback(mBluetoothEventCallback);
        bluetoothBle.removeOnBtBleListener(mBtBleListener);
        mUIHandler.removeCallbacksAndMessages(null);
    }

    private BluetoothDevice getConnectedDevice() {
        return bluetoothManager.getConnectedDevice();
    }

    /**
     * 用于开启蓝牙BLE设备Notification服务
     *
     * @param gatt               Gatt对象
     * @param serviceUUID        服务UUID
     * @param characteristicUUID characteristic UUID
     * @return 结果 true 则等待系统回调BLE服务
     */
    private boolean enableBLEDeviceNotification(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        if (null == gatt) {
            JL_Log.w(TAG, "enableBLEDeviceNotification", "bluetooth gatt is null.");
            return false;
        }
        BluetoothGattService gattService = gatt.getService(serviceUUID);
        if (null == gattService) {
            JL_Log.w(TAG, "enableBLEDeviceNotification", "bluetooth gatt service is null.");
            return false;
        }
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characteristicUUID);
        if (null == characteristic) {
            JL_Log.w(TAG, "enableBLEDeviceNotification", "bluetooth characteristic is null.");
            return false;
        }
        boolean bRet = gatt.setCharacteristicNotification(characteristic, true);
        if (bRet) {
            bRet = false; //重置标识
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            if (descriptors != null) {
                JL_Log.d(TAG, "enableBLEDeviceNotification", "descriptors size = " + descriptors.size());
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    if (!BLE_UUID_NOTIFICATION_DESCRIPTOR.equals(descriptor.getUuid()))
                        continue; //跳过不相关描述符
                    bRet = tryToWriteDescriptor(gatt, descriptor, 0, false);
                    if (!bRet) {
                        JL_Log.w(TAG, "enableBLEDeviceNotification", "tryToWriteDescriptor failed....");
                    } else { //正常只有一个描述符，使能即可
                        break;
                    }
                }
            } else {
                JL_Log.w(TAG, "enableBLEDeviceNotification", "descriptors is null.");
            }
        } else {
            JL_Log.w(TAG, "enableBLEDeviceNotification", "setCharacteristicNotification is failed....");
        }
        JL_Log.w(TAG, "enableBLEDeviceNotification", "" + bRet);
        return bRet;
    }

    /**
     * 尝试使能BLE服务属性
     *
     * @param bluetoothGatt  BluetoothGatt对象
     * @param descriptor     属性
     * @param retryCount     失败次数
     * @param isSkipSetValue 是否跳过设值
     * @return 结果
     */
    private boolean tryToWriteDescriptor(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int retryCount, boolean isSkipSetValue) {
        boolean ret = isSkipSetValue;
        if (!ret) {
            ret = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            JL_Log.i(TAG, "tryToWriteDescriptor", "descriptor : setValue  ret : " + ret);
            if (!ret) {
                retryCount++;
                if (retryCount >= 3) {
                    return false;
                } else {
                    JL_Log.i(TAG, "tryToWriteDescriptor", "Failed to setValue. retryCount : " + retryCount + ", isSkipSetValue :  false");
                    SystemClock.sleep(50);
                    tryToWriteDescriptor(bluetoothGatt, descriptor, retryCount, false);
                }
            } else {
                retryCount = 0;
            }
        }
        if (ret) {
            ret = bluetoothGatt.writeDescriptor(descriptor);
            JL_Log.i(TAG, "tryToWriteDescriptor", "writeDescriptor ret : " + ret);
            if (!ret) {
                retryCount++;
                if (retryCount >= 3) {
                    return false;
                } else {
                    JL_Log.i(TAG, "tryToWriteDescriptor", "Failed to writeDescriptor. retryCount : " + retryCount + ", isSkipSetValue :  true");
                    SystemClock.sleep(50);
                    tryToWriteDescriptor(bluetoothGatt, descriptor, retryCount, true);
                }
            }
        }
        return ret;
    }

    private final BluetoothEventCallback mBluetoothEventCallback = new BluetoothEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            isEnableNotify = false;
            if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                //设备连接成功
                if (bluetoothManager.isConnectedGattDevice(device)) {//BLE连接
                    BluetoothGatt gatt = bluetoothBle.getDeviceGatt(device);
                    if (null == gatt) return;
                    List<BluetoothGattService> services = gatt.getServices();
                    if (services != null && !services.isEmpty()) {
                        mBtBleListener.onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS); //统一处理
                    } else {
                        if (!gatt.discoverServices()) { //发现BLE服务
                            //TODO: 发现服务失败, 可以考虑延时重试
                        } else {
                            mUIHandler.removeMessages(MSG_DISCOVERY_SERVER);
                            mUIHandler.sendMessageDelayed(mUIHandler.obtainMessage(MSG_DISCOVERY_SERVER, gatt), DEFAULT_TIMEOUT);
                        }
                    }
                    return;
                }
                //TODO: 可以在此处回调设备连接成功状态
            }
        }
    };

    private final OnBtBleListener mBtBleListener = new OnBtBleListener() {
        @Override
        public void onConnectionUpdatedCallback(BluetoothGatt gatt, int interval, int latency, int timeout, int status) {
            //回调连接参数变化
        }

        @Override
        public void onBleConnection(BluetoothDevice device, int status) {
            //回调BLE设备连接状态
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            mUIHandler.removeMessages(MSG_DISCOVERY_SERVER);
            //回调Ble服务发现回调
            List<BluetoothGattService> services = gatt.getServices();
            if (services == null || services.isEmpty() || isEnableNotify) return;
            for (BluetoothGattService service : services) {
                if (serverUUID.equals(service.getUuid())) { //找到自定义ServerUUID
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(notifyCharacteristicsUUID);
                    if (characteristic != null) { //找到自定义通知特征UUID
                        boolean ret = enableBLEDeviceNotification(gatt, serverUUID, notifyCharacteristicsUUID);
                        System.out.printf("开始使能[%s]的操作结果:%s\n", notifyCharacteristicsUUID, ret);
                        if (!ret) {
                            //TODO: 使能失败处理
                        } else {
                            //开启一个使能UUID回调超时任务
                            mUIHandler.removeMessages(MSG_ENABLE_CUSTOM_UUID);
                            mUIHandler.sendMessageDelayed(mUIHandler.obtainMessage(MSG_ENABLE_CUSTOM_UUID, gatt), DEFAULT_TIMEOUT);
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onBleDataNotify(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
            //回调BLE数据通知
            if (serverUUID.equals(serviceUuid) && notifyCharacteristicsUUID.equals(characteristicsUuid)) {
                //回调自定义通道的数据
                //TODO: 可以回调到上层使用
            }
        }

        @Override
        public void onBleNotificationStatus(BluetoothDevice device, UUID serviceUuid, UUID characteristicUuid, boolean bEnabled) {
            //回调BLE特征使能状态
            if (serverUUID.equals(serviceUuid) && notifyCharacteristicsUUID.equals(characteristicUuid) && mUIHandler.hasMessages(MSG_ENABLE_CUSTOM_UUID)) {
                mUIHandler.removeMessages(MSG_ENABLE_CUSTOM_UUID);
                //自定义UUID的使能结果
                isEnableNotify = bEnabled;
                //使能自定义UUID的流程完成，可以开始发数据到自定义通道
                //TODO: 可以在此处回调设备连接成功状态
            }
        }

        @Override
        public void onBleWriteStatus(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data, int status) {
            //回调BLE写数据状态
        }

        @Override
        public void onBleMtuChanged(BluetoothDevice device, int block, int status) {
            //回调BLE协商MTU改变
        }

        @Override
        public void onBleBond(BluetoothDevice device, int bondStatus) {
            //回调BLE设备配对状态
        }

        @Override
        public void onSwitchBleDevice(BluetoothDevice device) {
            //回调切换设备
        }
    };
}
