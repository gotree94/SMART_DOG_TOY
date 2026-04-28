package com.jieli.watchtesttool.tool.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;

import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.jl_bt_ota.util.CommonUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RcspAuth;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 蓝牙辅助类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class BluetoothHelper {
    private final static String TAG = BluetoothHelper.class.getSimpleName();
    private volatile static BluetoothHelper instance;
    private final BluetoothManager mBluetoothOp;
    private final ConfigHelper configHelper;
    private final RcspAuth mRcspAuth;
    private final BtEventCbManager mBtEventCbManager;
    private final Map<String, Boolean> mAuthDeviceMap = new HashMap<>();

    private ChangeBleMtuTimeoutTask mChangeBleMtuTimeoutTask;

    private static final long DELAY_WAITING_TIME = 5000L;
    private static final boolean IS_CHANGE_BLE_MTU = true;  //是否修改BLE的MTU

    private final Handler mHandler = new Handler(Looper.getMainLooper(), msg -> true);

    private BluetoothHelper() {
        mBluetoothOp = BluetoothManager.getInstance();
        configHelper = ConfigHelper.getInstance();
        mBluetoothOp.registerBluetoothCallback(mBtEventCallback);
        mRcspAuth = new RcspAuth(this::sendDataToDevice, mRcspAuthListener);
        mBtEventCbManager = new BtEventCbManager();

        mBluetoothOp.fastConnect();
    }

    public static BluetoothHelper getInstance() {
        if (null == instance) {
            synchronized (BluetoothHelper.class) {
                if (null == instance) {
                    instance = new BluetoothHelper();
                }
            }
        }
        return instance;
    }

    public void addBluetoothEventListener(BluetoothEventListener listener) {
        mBtEventCbManager.addBluetoothEventListener(listener);
    }

    public void removeBluetoothEventListener(BluetoothEventListener listener) {
        mBtEventCbManager.removeBluetoothEventListener(listener);
    }

    public void destroy() {
        mBluetoothOp.unregisterBluetoothCallback(mBtEventCallback);
        mBluetoothOp.destroy();
        mRcspAuth.removeListener(mRcspAuthListener);
        mRcspAuth.destroy();
        mAuthDeviceMap.clear();
        mHandler.removeCallbacksAndMessages(null);
        mBtEventCbManager.destroy();
        instance = null;
    }

    public BluetoothManager getBluetoothOp() {
        return mBluetoothOp;
    }

    public boolean isConnectedBtDevice(BluetoothDevice device) {
        return mBluetoothOp.isConnectedDevice(device);
    }

    public boolean isUsedBtDevice(BluetoothDevice device) {
        return mBluetoothOp.isConnectedDevice(device) && BluetoothUtil.deviceEquals(getConnectedBtDevice(), device);
    }

    public boolean isHistoryRecord(BluetoothDevice device) {
        return device != null && mBluetoothOp.getHistoryRecord(device.getAddress()) != null;
    }

    public boolean isConnectedDevice() {
        return getConnectedBtDevice() != null && isDevAuth(getConnectedBtDevice().getAddress());
    }

    public BluetoothDevice getConnectedBtDevice() {
        return mBluetoothOp.getConnectedDevice();
    }

    /**
     * 获取已连接的BLE的GATT控制对象
     *
     * @return 已连接的BLE的GATT控制对象
     */
    public BluetoothGatt getConnectedBluetoothGatt(BluetoothDevice device) {
        return mBluetoothOp.getDeviceGatt(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (null == device) return StateCode.CONNECTION_DISCONNECT;
        int status = StateCode.CONNECTION_DISCONNECT;
        if (isConnectedBtDevice(device)) {
            status = StateCode.CONNECTION_OK;
        } else if (BluetoothUtil.deviceEquals(device, getBluetoothOp().getConnectingDevice())) {
            status = StateCode.CONNECTION_CONNECTING;
        }
        return status;
    }

    public boolean connectDeviceWithoutRecord(BluetoothDevice device) {
        if (null == device) return false;
        return mBluetoothOp.connectBtDeviceWithoutRecord(device, BluetoothConstant.PROTOCOL_TYPE_BLE);
    }

    @SuppressLint("MissingPermission")
    public void connectDevice(BluetoothDevice device) {
        if (null == device || !ConnectUtil.isHasConnectPermission(WatchApplication.getWatchApplication()))
            return;
        int devType = device.getType();
        int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (devType == BluetoothDevice.DEVICE_TYPE_UNKNOWN || devType == BluetoothDevice.DEVICE_TYPE_DUAL) {
            connectWay = getCacheConnectWay(device);
            if (configHelper.isSPPConnectWay()) {
                if (connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP) {
                    String mappedAddress = mBluetoothOp.getMappedDeviceAddress(device.getAddress());
                    if (BluetoothAdapter.checkBluetoothAddress(mappedAddress)) {
                        BluetoothDevice mappedDev = AppUtil.getRemoteDevice(mappedAddress);
                        if (mappedDev != null && mappedDev.getType() != BluetoothDevice.DEVICE_TYPE_LE && mappedDev.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
                            device = mappedDev;
                        }
                    }
                }
            } else {
                connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
            }
        }
        mBluetoothOp.connectBtDevice(device, connectWay);
    }

    public void connectDevice(BluetoothDevice device, BleScanMessage scanMessage) {
        if (null == device) return;
        if (null != scanMessage) {
            JL_Log.d(TAG, "connectDevice", "device : " + device + ", " + scanMessage);
            int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
            if (configHelper.isSPPConnectWay()) {
                connectWay = BluetoothConstant.PROTOCOL_TYPE_SPP;
                BluetoothDevice edrDev = AppUtil.getRemoteDevice(scanMessage.getEdrAddr());
                if (edrDev != null) {
                    device = edrDev;
                }
            }
            mBluetoothOp.connectBtDevice(device, connectWay);
        } else {
            connectDevice(device);
        }
    }

    public void connectHistoryRecord(HistoryRecord record) {
        if (null == record) return;
        mBluetoothOp.connectHistoryRecord(record, null);
    }

    public void removeHistoryRecord(String address, OnHistoryRecordCallback callback) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) return;
        mBluetoothOp.removeHistoryRecord(address, callback);
    }

    public int getCacheConnectWay(BluetoothDevice device) {
        int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (device == null) return connectWay;
        if (mBluetoothOp.isConnectedDevice(device)) {
            if (mBluetoothOp.isConnectedSppDevice(device))
                connectWay = BluetoothConstant.PROTOCOL_TYPE_SPP;
        } else {
            HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(device.getAddress());
            if (historyRecord != null) {
                connectWay = historyRecord.getConnectType();
            }
        }
        return connectWay;
    }

    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        return mBluetoothOp.sendDataToDevice(device, data);
    }

    public void disconnectDevice(BluetoothDevice device) {
        if (null == device) return;
        if (isConnectedBtDevice(device)) {
            mBluetoothOp.disconnectBtDevice(device);
            return;
        }
        publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_DISCONNECT);
    }

    public boolean isAuthDevice(BluetoothDevice device) {
        return device != null && isDevAuth(device.getAddress());
    }

    private boolean startChangeMtu(BluetoothDevice device, int changeMtu) {
        if (mChangeBleMtuTimeoutTask != null) {
            WLog.w(TAG, "startChangeMtu", "Adjusting the MTU for BLE");
            return true;
        }
        boolean ret = mBluetoothOp.requestBleMtu(device, changeMtu);
        WLog.i(TAG, "startChangeMtu", "requestBleMtu = " + ret + ", change mtu = " + changeMtu);
        if (ret) {
            mChangeBleMtuTimeoutTask = new ChangeBleMtuTimeoutTask(device);
            mHandler.postDelayed(mChangeBleMtuTimeoutTask, DELAY_WAITING_TIME);
        }
        return ret;
    }

    private void stopChangeBleMtu() {
        WLog.i(TAG, "stopChangeBleMtu", "");
        if (mChangeBleMtuTimeoutTask != null) {
            mHandler.removeCallbacks(mChangeBleMtuTimeoutTask);
            mChangeBleMtuTimeoutTask = null;
        }
    }

    private boolean isDevAuth(String address) {
        if (!mBluetoothOp.getBluetoothOption().isUseDeviceAuth()) return true;
        Boolean b = mAuthDeviceMap.get(address);
        return b != null && b;
    }

    private void setDevAuth(BluetoothDevice device, boolean b) {
        if (null == device) return;
        mAuthDeviceMap.put(device.getAddress(), b);
    }

    private void removeDevAuth(String address) {
        mAuthDeviceMap.remove(address);
    }

    private void publishDeviceConnectionStatus(BluetoothDevice device, int status) {
        mBtEventCbManager.onConnection(device, status);
        if (BluetoothConstant.CONNECT_STATE_CONNECTED == status || BluetoothConstant.CONNECT_STATE_DISCONNECT == status) {
            if (mChangeBleMtuTimeoutTask != null && BluetoothUtil.deviceEquals(device, mChangeBleMtuTimeoutTask.getDevice())) {
                stopChangeBleMtu();
            }
            if (BluetoothConstant.CONNECT_STATE_DISCONNECT == status && device != null) {
                removeDevAuth(device.getAddress());
            }
        }
    }

    private void callbackDeviceConnected(BluetoothDevice device) {
        publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_CONNECTED);
    }

    private void handleDeviceConnectedEvent(BluetoothDevice device) {
        int connectWay = mBluetoothOp.isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE;
        WLog.d(TAG, "handleDeviceConnectedEvent", "device = " + AppUtil.printBtDeviceInfo(device) + ", connectWay = " + connectWay);
        //Step0.检测设备是否通过设备认证
        if (!isAuthDevice(device)) { //设备未认证
            mRcspAuth.stopAuth(device, false);
            boolean ret = mRcspAuth.startAuth(device);
            WLog.i(TAG, "handleDeviceConnectedEvent", "startAuth = " + ret);
            if (!ret) {
                disconnectDevice(device);
            }
            return;
        }
        //Step1.分类型处理不同的连接情况
        switch (connectWay) {
            case BluetoothConstant.PROTOCOL_TYPE_BLE:
                int mtu = mBluetoothOp.getBleMtu(device);
                int changeMtu = mBluetoothOp.getBluetoothOption().getMtu();
                if (mtu != changeMtu && IS_CHANGE_BLE_MTU) {
                    boolean ret = startChangeMtu(device, changeMtu);
                    WLog.i(TAG, "handleDeviceConnectedEvent", "startChangeMtu = " + ret + ", mtu = " + mtu + ", change mtu = " + changeMtu);
                    if (ret) {
                        return;
                    }
                }
                break;
            case BluetoothConstant.PROTOCOL_TYPE_SPP:
                break;
        }
        //Step2.回调连接成功
        callbackDeviceConnected(device);
    }

    private void handleReceiveRawData(BluetoothDevice device, byte[] rawData) {
        if (!isAuthDevice(device)) {
            mRcspAuth.handleAuthData(device, rawData);
        } else {
            mBtEventCbManager.onReceiveData(device, rawData);
        }
    }

    private final BluetoothEventCallback mBtEventCallback = new BluetoothEventCallback() {

        @Override
        public void onAdapterStatus(boolean bEnabled, boolean bHasBle) {
            mBtEventCbManager.onAdapterStatus(bEnabled);
        }

        @Override
        public void onDiscoveryStatus(boolean bBle, boolean bStart) {
            mBtEventCbManager.onBtDiscoveryStatus(bBle, bStart);
        }

        @Override
        public void onDiscovery(BluetoothDevice device, BleScanMessage bleScanMessage) {
            mBtEventCbManager.onBtDiscovery(device, bleScanMessage);
        }

        @Override
        public void onShowDialog(BluetoothDevice device, BleScanMessage bleScanMessage) {
            mBtEventCbManager.onShowDialog(device, bleScanMessage);
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            WLog.i(TAG, "onConnection", "device : " + AppUtil.printBtDeviceInfo(device) + ", status = " + status);
            if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                handleDeviceConnectedEvent(device);
            } else {
                publishDeviceConnectionStatus(device, status);
            }
        }

        @Override
        public void onSwitchConnectedDevice(BluetoothDevice device) {
            mBtEventCbManager.onSwitchConnectedDevice(device);
        }

        @Override
        public void onBleDataBlockChanged(BluetoothDevice device, int block, int status) {
            mBtEventCbManager.onBleMtuChange(getConnectedBluetoothGatt(device), block, status);
            if (status == BluetoothGatt.GATT_SUCCESS && mChangeBleMtuTimeoutTask != null && BluetoothUtil.deviceEquals(device, mChangeBleMtuTimeoutTask.getDevice())) {
                stopChangeBleMtu();
                callbackDeviceConnected(device);
            }
        }

        @Override
        public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
            if (serviceUuid.equals(mBluetoothOp.getBluetoothOption().getBleServiceUUID()) && characteristicsUuid.equals(mBluetoothOp.getBluetoothOption().getBleNotificationUUID())) {
                handleReceiveRawData(device, data);
            }
        }

        @Override
        public void onSppDataNotification(BluetoothDevice device, UUID sppUUID, byte[] data) {
            if (sppUUID.equals(mBluetoothOp.getBluetoothOption().getSppUUID())) {
                handleReceiveRawData(device, data);
            }
        }

        @Override
        public void onError(ErrorInfo error) {
            mBtEventCbManager.onError(error);
        }
    };

    private final RcspAuth.OnRcspAuthListener mRcspAuthListener = new RcspAuth.OnRcspAuthListener() {
        @Override
        public void onInitResult(boolean b) {

        }

        @Override
        public void onAuthSuccess(BluetoothDevice bluetoothDevice) {
            WLog.w(TAG, "onAuthSuccess", "device : " + AppUtil.printBtDeviceInfo(bluetoothDevice));
            setDevAuth(bluetoothDevice, true);
            handleDeviceConnectedEvent(bluetoothDevice);
        }

        @Override
        public void onAuthFailed(BluetoothDevice bluetoothDevice, int i, String s) {
            WLog.e(TAG, "onAuthFailed", "device : " + AppUtil.printBtDeviceInfo(bluetoothDevice)
                    + ", code = " + CommonUtil.formatInt(i) + ", message = " + s);
            setDevAuth(bluetoothDevice, false);
            disconnectDevice(bluetoothDevice);
        }
    };

    private class ChangeBleMtuTimeoutTask implements Runnable {
        private final BluetoothDevice mDevice;

        public ChangeBleMtuTimeoutTask(BluetoothDevice device) {
            mDevice = device;
        }

        public BluetoothDevice getDevice() {
            return mDevice;
        }

        @Override
        public void run() {
            if (mBluetoothOp.isConnectedDevice(mDevice)) {
                callbackDeviceConnected(mDevice);
            } else {
                publishDeviceConnectionStatus(mDevice, BluetoothConstant.CONNECT_STATE_DISCONNECT);
            }
        }
    }
}
