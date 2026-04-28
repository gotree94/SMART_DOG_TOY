package com.jieli.healthaide.tool.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.bean.BluetoothOption;
import com.jieli.bluetooth_connect.bean.ErrorInfo;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.impl.BluetoothCore;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.CHexConverter;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.bluetooth_connect.util.ParseDataUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_rcsp.constant.JL_DeviceType;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.impl.RcspAuth;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.IrkMessage;
import com.jieli.jl_rcsp.tool.BooleanRcspActionCallback;
import com.jieli.jl_rcsp.util.CommandBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 蓝牙辅助类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
@SuppressLint("MissingPermission")
public class BluetoothHelper {
    private final static String TAG = BluetoothHelper.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static BluetoothHelper instance;
    private final Context mContext;
    private final BluetoothManager mBluetoothOp;
    private final RcspAuth mRcspAuth;
    private final BtEventCbManager mBtEventCbManager;
    private final Map<String, Boolean> mAuthDeviceMap = new HashMap<>();

    private ChangeBleMtuTimeoutTask mChangeBleMtuTimeoutTask;
    private ConnectSppParam connectSppParam;    //连接SPP参数
    private final List<String> bleToSppList = new ArrayList<>();
    private final Map<String, NeedUpdateDevice> needUpdateDeviceMap = new HashMap<>();  //需要更新的设备信息

    //    private static final boolean IS_NEED_DEVICE_AUTH = true; //是否需要设备认证流程
    private static final long DELAY_WAITING_TIME = 5000L;
    private final static int CHECK_DELAY = 3000;
    private static final boolean IS_CHANGE_BLE_MTU = true;  //是否修改BLE的MTU

    private final static int MSG_CHECK_BLE_DISCONNECT = 0x0111;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (MSG_CHECK_BLE_DISCONNECT == msg.what) {
            if (connectSppParam != null) {
                BluetoothDevice bleDev = getRemoteDevice(connectSppParam.getBleAddress());
                if (isConnectedBtDevice(bleDev)) {
                    JL_Log.i(TAG, "MSG_CHECK_BLE_DISCONNECT", "Ble is connected. so disconnect ble...");
                    disconnectDevice(bleDev);
                }
            }
        }
        return true;
    });

    private BluetoothHelper() {
        BluetoothOption bluetoothOption = BluetoothOption.createDefaultOption()
                .setPriority(HealthConstant.DEFAULT_CONNECT_WAY)
                .setScanFilterData("")
                .setNeedChangeBleMtu(false)
                .setMtu(BluetoothConstant.BLE_MTU_MAX)
                .setUseMultiDevice(false);
        /*.setSkipNoneNameDevice(false); *///Android 13 搜索uboot设备可能会出现没有名字的情况
        mContext = HealthApplication.getAppViewModel().getApplication();
        if (!BluetoothCore.isInit()) {
            BluetoothCore.init(mContext, bluetoothOption);
        }
        mBluetoothOp = BluetoothManager.getInstance();
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
        needUpdateDeviceMap.clear();
        mHandler.removeCallbacksAndMessages(null);
        mBtEventCbManager.destroy();
        bleToSppList.clear();
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

    public boolean isHistoryRecord(String devAddress) {
        return mBluetoothOp.getHistoryRecord(devAddress) != null;
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
        if (null == device) return BluetoothConstant.CONNECT_STATE_DISCONNECT;
        int status = BluetoothConstant.CONNECT_STATE_DISCONNECT;
        if (isConnectedBtDevice(device)) {
            status = BluetoothConstant.CONNECT_STATE_CONNECTED;
        } else if (BluetoothUtil.deviceEquals(device, getBluetoothOp().getConnectingDevice())) {
            status = BluetoothConstant.CONNECT_STATE_CONNECTING;
        }
        return status;
    }

    public boolean connectDeviceWithoutRecord(BluetoothDevice device) {
        if (null == device) return false;
        return mBluetoothOp.connectBtDeviceWithoutRecord(device, BluetoothConstant.PROTOCOL_TYPE_BLE);
    }

    @SuppressLint("MissingPermission")
    public boolean connectDevice(BluetoothDevice device) {
        if (null == device || !ConnectUtil.isHasConnectPermission(mContext)) return false;
        int devType = device.getType();
        int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (!HealthConstant.ONLY_CONNECT_BLE) {
            if (devType == BluetoothDevice.DEVICE_TYPE_UNKNOWN || devType == BluetoothDevice.DEVICE_TYPE_DUAL) {
                connectWay = getCacheConnectWay(device);
                if (connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP) {
                    String mappedAddress = mBluetoothOp.getMappedDeviceAddress(device.getAddress());
                    if (BluetoothAdapter.checkBluetoothAddress(mappedAddress)) {
                        BluetoothDevice mappedDev = getRemoteDevice(mappedAddress);
                        if (mappedDev != null && mappedDev.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
                            device = mappedDev;
                        }
                    }
                }
            }
        }
        return mBluetoothOp.connectBtDevice(device, connectWay);
    }

    @SuppressLint("MissingPermission")
    public boolean connectDevice(BluetoothDevice device, BleScanMessage scanMessage) {
        if (null == device || !ConnectUtil.isHasConnectPermission(mContext)) return false;
        if (null != scanMessage) {
            JL_Log.d(TAG, "connectDevice", "device : " + printDeviceInfo(device) + ", " + scanMessage);
            int connectWay = scanMessage.getConnectWay();
            if (connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP) {
                //固件修改了Bit(1)决定是否进行先连接BLE再连接SPP， 兼容BR28手表广播包version == 1
                if (scanMessage.getDeviceType() == JL_DeviceType.JL_DEVICE_TYPE_WATCH && (scanMessage.getVersion() == 1 ||
                        CHexConverter.checkBitValue(CHexConverter.intToByte(scanMessage.getVersion()), 1))) { //特殊连接方式
                    connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
                } else {
                    if (!HealthConstant.ONLY_CONNECT_BLE) {
                        BluetoothDevice edrDev = getRemoteDevice(scanMessage.getEdrAddr());
                        if (edrDev != null) device = edrDev;
                    } else {
                        connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
                    }
                }
            } else if (scanMessage.isOTA()) {//升级设备
                HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(scanMessage.getOtaBleAddress());
                if (historyRecord != null) {//存在历史记录设备
                    historyRecord.setUpdateAddress(device.getAddress());
                    mBluetoothOp.getHistoryRecordHelper().updateHistoryRecord(historyRecord);
                } else {//新连接一个强升设备（新回连设备）
                    needUpdateDeviceMap.put(device.getAddress(), new NeedUpdateDevice(device.getAddress(), scanMessage.getOtaBleAddress(),
                            scanMessage.getDeviceType(), scanMessage.getVid(), scanMessage.getUid(), scanMessage.getPid()));
                }
            }
            return mBluetoothOp.connectBtDevice(device, connectWay);
        } else {
            return connectDevice(device);
        }
    }

    public void connectHistoryRecord(HistoryRecord record, OnHistoryRecordCallback callback) {
        if (null == record) return;
        mBluetoothOp.connectHistoryRecord(record, callback);
    }

    public void removeHistoryRecord(String address, OnHistoryRecordCallback callback) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) return;
        mBluetoothOp.removeHistoryRecord(address, callback);
    }

    @SuppressLint("MissingPermission")
    public int getCacheConnectWay(BluetoothDevice device) {
        if (null == device || !ConnectUtil.isHasConnectPermission(mContext))
            return BluetoothConstant.SCAN_TYPE_BLE;
        int connectWay = BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (mBluetoothOp.isConnectedDevice(device)) {
            if (mBluetoothOp.isConnectedSppDevice(device))
                connectWay = BluetoothConstant.PROTOCOL_TYPE_SPP;
        } else {
            HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(device.getAddress());
            if (historyRecord != null) {
                if (!device.getAddress().equals(historyRecord.getUpdateAddress())) {
                    connectWay = historyRecord.getConnectType();
                }
            }
        }
        return connectWay;
    }

    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        return mBluetoothOp.sendDataToDevice(device, data);
    }

    @SuppressLint("MissingPermission")
    public void disconnectDevice(BluetoothDevice device) {
        if (null == device || !ConnectUtil.isHasConnectPermission(mContext)) return;
        if (isConnectedBtDevice(device)) {
            mBluetoothOp.disconnectBtDevice(device);
        } else {
            publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_DISCONNECT);
        }
    }

    public boolean isAuthDevice(BluetoothDevice device) {
        return device != null && isDevAuth(device.getAddress());
    }

    @SuppressLint("MissingPermission")
    public void syncEdrConnectionStatus(final BluetoothDevice device, final DeviceInfo deviceInfo) {
        if (null == deviceInfo || !ConnectUtil.isHasConnectPermission(mContext) || deviceInfo.isBleOnly())
            return;
        String edrAddress = deviceInfo.getEdrAddr();
        BluetoothDevice mEdrDevice = getRemoteDevice(edrAddress);
        if (null == mEdrDevice) return;
        if (deviceInfo.getEdrStatus() == RcspConstant.STATUS_CLASSIC_BLUETOOTH_CONNECTED) { //设备经典蓝牙已连接
            int phoneEdrStatus = mBluetoothOp.isConnectedByProfile(mEdrDevice);
            if (phoneEdrStatus == BluetoothProfile.STATE_CONNECTED) { //设备被手机连接上
                tryToChangeActivityDevice(device, mEdrDevice, deviceInfo);
            } else { //设备被其他手机连接上
                JL_Log.w(TAG, "syncEdrConnectionStatus", "Device is connected by another phone.");
                boolean ret = mBluetoothOp.startConnectByBreProfiles(mEdrDevice);
                JL_Log.w(TAG, "syncEdrConnectionStatus", "Try to connect, result : "
                        + (ret ? "Device starts connecting" : "Device connection failed"));
            }
        } else { //设备经典蓝牙未连接
            boolean ret = mBluetoothOp.startConnectByBreProfiles(mEdrDevice);
            JL_Log.w(TAG, "syncEdrConnectionStatus", (ret ? "Device starts connecting" : "Device connection failed"));
        }
    }

    public boolean isBleChangeSpp(BluetoothDevice device) {
        return device != null && connectSppParam != null && device.getAddress().equals(connectSppParam.getBleAddress());
    }

    public boolean isBleToSpp(BluetoothDevice device) {
        return device != null && bleToSppList.contains(device.getAddress());
    }

    public boolean isNeedUpdateDevice(HistoryRecord record) {
        if (null == record) return false;
        NeedUpdateDevice needUpdateDevice = null;
        if (BluetoothAdapter.checkBluetoothAddress(record.getAddress())) {
            needUpdateDevice = needUpdateDeviceMap.get(record.getAddress());
        }
        if (null == needUpdateDevice) {
            if (BluetoothAdapter.checkBluetoothAddress(record.getMappedAddress())) {
                needUpdateDevice = needUpdateDeviceMap.get(record.getMappedAddress());
            }
        }
        if (null == needUpdateDevice) {
            if (BluetoothAdapter.checkBluetoothAddress(record.getUpdateAddress())) {
                needUpdateDevice = needUpdateDeviceMap.get(record.getUpdateAddress());
            }
        }
        return needUpdateDevice != null;
    }

    public void bleChangeSpp(@NonNull RcspOpImpl rcspOp, @NonNull BluetoothDevice device) {
        DeviceInfo deviceInfo = rcspOp.getDeviceInfo(device);
        //设置回连信息
        connectSppParam = new ConnectSppParam(device.getAddress(), deviceInfo.getEdrAddr());
        JL_Log.d(TAG, "bleChangeSpp", connectSppParam.toString());
        rcspOp.sendRcspCommand(device, CommandBuilder.buildNotifyCommunicationWayCmd(BluetoothConstant.PROTOCOL_TYPE_SPP, 0),
                new BooleanRcspActionCallback("bleChangeSpp", new OnOperationCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        //开始准备切换SPP, 启动等待超时
                        JL_Log.i(TAG, "bleChangeSpp", "Waiting for ble disconnect...");
                        mHandler.removeMessages(MSG_CHECK_BLE_DISCONNECT);
                        mHandler.sendEmptyMessageDelayed(MSG_CHECK_BLE_DISCONNECT, CHECK_DELAY);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        JL_Log.w(TAG, "bleChangeSpp", "onErrCode ---> " + error);
                        if (mHandler.hasMessages(MSG_CHECK_BLE_DISCONNECT)) {//如果还有超时任务，立即触发
                            mHandler.removeMessages(MSG_CHECK_BLE_DISCONNECT);
                            mHandler.sendEmptyMessage(MSG_CHECK_BLE_DISCONNECT);
                        }
                    }
                }));
    }

    private boolean startChangeMtu(BluetoothDevice device, int changeMtu) {
        if (mChangeBleMtuTimeoutTask != null) {
            JL_Log.w(TAG, "startChangeMtu", "Adjusting the MTU for BLE");
            return true;
        }
        boolean ret = mBluetoothOp.requestBleMtu(device, changeMtu);
        JL_Log.i(TAG, "startChangeMtu", "requestBleMtu = " + ret + ", change mtu = " + changeMtu);
        if (ret) {
            mChangeBleMtuTimeoutTask = new ChangeBleMtuTimeoutTask(device);
            mHandler.postDelayed(mChangeBleMtuTimeoutTask, DELAY_WAITING_TIME);
        }
        return ret;
    }

    private void stopChangeBleMtu() {
        JL_Log.i(TAG, "stopChangeBleMtu", "");
        if (mChangeBleMtuTimeoutTask != null) {
            mHandler.removeCallbacks(mChangeBleMtuTimeoutTask);
            mChangeBleMtuTimeoutTask = null;
        }
    }

    /**
     * 是否需要设备认证流程
     *
     * @return boolean 结果
     */
    private boolean isEnableDeviceAuth() {
//        return IS_NEED_DEVICE_AUTH;
        return ConfigHelper.getInstance().isEnableDeviceAuth();
    }

    private boolean isDevAuth(String address) {
        if (!isEnableDeviceAuth()) return true;
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
        JL_Log.i(TAG, "publishDeviceConnectionStatus", CalendarUtil.formatString("device : %s, status: %d",
                printDeviceInfo(device), status));
        if (BluetoothConstant.CONNECT_STATE_CONNECTED == status || BluetoothConstant.CONNECT_STATE_DISCONNECT == status) {
            if (mChangeBleMtuTimeoutTask != null && BluetoothUtil.deviceEquals(device, mChangeBleMtuTimeoutTask.getDevice())) {
                stopChangeBleMtu();
            }
            if (BluetoothConstant.CONNECT_STATE_DISCONNECT == status && device != null) {
                removeDevAuth(device.getAddress());
                if (handleSppReconnectEvent(device, connectSppParam)) {
                    //处理SPP回连事件
                    JL_Log.i(TAG, "publishDeviceConnectionStatus", "ready to connect spp. " + device);
                }
            }
        }
        mBtEventCbManager.onConnection(device, status);
    }

    private void callbackDeviceConnected(BluetoothDevice device) {
        JL_Log.i(TAG, "callbackDeviceConnected", "device = " + printDeviceInfo(device));
        NeedUpdateDevice needUpdateDevice = needUpdateDeviceMap.get(device.getAddress());
        if (needUpdateDevice != null) {//没有历史记录的新回连设备
            HistoryRecord historyRecord = getBluetoothOp().getHistoryRecord(device.getAddress());
            JL_Log.i(TAG, "callbackDeviceConnected", "obtain historyRecord,  " + historyRecord);
            if (historyRecord != null) {
                historyRecord.setAddress(needUpdateDevice.getOriginalBleAddress());
                historyRecord.setUpdateAddress(device.getAddress());
                historyRecord.setDevType(needUpdateDevice.getDeviceType());
                historyRecord.setConnectType(needUpdateDevice.getConnectWay());
                historyRecord.setVid(needUpdateDevice.getVid());
                historyRecord.setUid(needUpdateDevice.getUid());
                historyRecord.setPid(needUpdateDevice.getPid());
                mBluetoothOp.getHistoryRecordHelper().updateHistoryRecord(historyRecord);
                JL_Log.i(TAG, "callbackDeviceConnected", "change historyRecord before, " + historyRecord);
            }
            needUpdateDeviceMap.remove(device.getAddress());
        }
        publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_CONNECTED);
    }

    private void handleDeviceConnectedEvent(BluetoothDevice device) {
        int connectWay = mBluetoothOp.isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE;
        JL_Log.d(TAG, "handleDeviceConnectedEvent", "device = " + printDeviceInfo(device) + ", connectWay = " + connectWay);
        //Step0.检测设备是否通过设备认证
        if (!isAuthDevice(device)) { //设备未认证
            mRcspAuth.stopAuth(device, false);
            boolean ret = mRcspAuth.startAuth(device);
            JL_Log.i(TAG, "handleDeviceConnectedEvent", "startAuth = " + ret);
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
                    JL_Log.i(TAG, "handleDeviceConnectedEvent", "startChangeMtu = " + ret + ", mtu = " + mtu + ", change mtu = " + changeMtu);
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
        boolean isAuthDevice = isAuthDevice(device);
        JL_Log.d(TAG, "handleReceiveRawData", "device = " + device + ", isAuthDevice : " + isAuthDevice
                + ", rawData = " + CHexConverter.byte2HexStr(rawData));
        if (!isAuthDevice) {
            mRcspAuth.handleAuthData(device, rawData);
        } else {
            mBtEventCbManager.onReceiveData(device, rawData);
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice getCacheEdrDevice(BluetoothDevice device, DeviceInfo deviceInfo) {
        if (device == null || !ConnectUtil.isHasConnectPermission(mContext)) return null;
        BluetoothDevice mTargetDevice = null;
        if (deviceInfo != null) {
            BluetoothDevice edrDev = getRemoteDevice(deviceInfo.getEdrAddr());
            if (edrDev != null) {
                mTargetDevice = edrDev;
            }
        }
        if (mTargetDevice == null) {
            HistoryRecord historyRecord = mBluetoothOp.getHistoryRecord(device.getAddress());
            if (null != historyRecord) {
                String edrAddr;
                if (historyRecord.getConnectType() == BluetoothConstant.PROTOCOL_TYPE_BLE) {
                    edrAddr = historyRecord.getMappedAddress();
                } else {
                    edrAddr = historyRecord.getAddress();
                }
                BluetoothDevice temp = getRemoteDevice(edrAddr);
                JL_Log.d(TAG, "getCacheEdrDevice", "edrAddr :" + edrAddr + ", device : " + printDeviceInfo(temp));
                if (temp != null && (temp.getType() != BluetoothDevice.DEVICE_TYPE_LE &&
                        temp.getType() != BluetoothDevice.DEVICE_TYPE_DUAL)) {
                    mTargetDevice = temp;
                }
            }
        }
        if (mTargetDevice == null) {
            mTargetDevice = device;
        }
        return mTargetDevice;
    }

    private void tryToChangeActivityDevice(BluetoothDevice connectedDevice, BluetoothDevice mEdrDevice, DeviceInfo deviceInfo) {
        if (/*!BluetoothConstant.IS_CHANGE_ACTIVITY_DEVICE || */connectedDevice == null || mEdrDevice == null)
            return;
        BluetoothDevice currentDev = mBluetoothOp.getActivityBluetoothDevice();
        BluetoothDevice useDevice = mBluetoothOp.getConnectedDevice();
        if (BluetoothUtil.deviceEquals(connectedDevice, useDevice)) {
            if (!BluetoothUtil.deviceEquals(currentDev, mEdrDevice)) {
                boolean setRet = mBluetoothOp.setActivityBluetoothDevice(mEdrDevice);
                JL_Log.i(TAG, "tryToChangeActivityDevice", "setActivityBluetoothDevice >> " + setRet +
                        ", mEdrDevice : " + printDeviceInfo(mEdrDevice));
            }
        } else {
            BluetoothDevice mTargetEdrDev = getCacheEdrDevice(useDevice, deviceInfo);
            if (!BluetoothUtil.deviceEquals(currentDev, mTargetEdrDev)) {
                boolean setRet = mBluetoothOp.setActivityBluetoothDevice(mTargetEdrDev);
                JL_Log.i(TAG, "tryToChangeActivityDevice", "setActivityBluetoothDevice >> " + setRet +
                        ", mTargetEdrDev : " + printDeviceInfo(mTargetEdrDev));
            }
        }
    }

    private void resetConnectSppParam() {
        connectSppParam = null;
        WatchManager.getInstance().setBleChangeSpp(false);
    }

    private boolean handleSppReconnectEvent(BluetoothDevice device, ConnectSppParam connectSppParam) {
        JL_Log.i(TAG, "handleSppReconnectEvent", "device : " + device + ", " + connectSppParam);
        if (connectSppParam != null && device.getAddress().equals(connectSppParam.bleAddress)) {
            mHandler.removeMessages(MSG_CHECK_BLE_DISCONNECT); //移除超时任务
            final String address = connectSppParam.getSppAddress();
            //开始回连设备
            final BluetoothDevice sppDevice = getRemoteDevice(address);
            if (null == sppDevice) {
                JL_Log.w(TAG, "handleSppReconnectEvent", "not found device. " + address);
                resetConnectSppParam();
                return false;
            }
            JL_Log.w(TAG, "handleSppReconnectEvent", CalendarUtil.formatString("ble[%s] is disconnected. ready to connect spp[%s].", device.getAddress(), address));
            //等待500ms, 目的是等待EDR开启，加快SPP连接
            mHandler.postDelayed(() -> {
                resetConnectSppParam();
                bleToSppList.add(sppDevice.getAddress());
                JL_Log.i(TAG, "handleSppReconnectEvent", "start connect spp. " + printDeviceInfo(sppDevice));
                if (!mBluetoothOp.connectSPPDevice(sppDevice)) {
                    publishDeviceConnectionStatus(device, BluetoothConstant.CONNECT_STATE_DISCONNECT);
                }
            }, 500);
            return true;
        }
        return false;
    }

    private String printDeviceInfo(BluetoothDevice device) {
        return HealthUtil.printBtDeviceInfo(device);
    }

    private BluetoothDevice getRemoteDevice(String address) {
        return HealthUtil.getRemoteDevice(address);
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
        public void onBondStatus(BluetoothDevice device, int status) {
            super.onBondStatus(device, status);
        }

        @Override
        public void onBtDeviceConnectStatus(BluetoothDevice device, int status) {
            super.onBtDeviceConnectStatus(device, status);
            if (null != device && status == BluetoothConstant.CONNECT_STATE_CONNECTED) { //经典蓝牙已连接
                final WatchManager watchManager = WatchManager.getInstance();
                final HistoryRecord history = mBluetoothOp.getHistoryRecord(device.getAddress());
                //不是RPA，不去获取IRK信息
                if (!ParseDataUtil.isResolvablePrivateAddress(history.getBleAddress())) return;
                final BluetoothDevice targetDev = BluetoothUtil.getRemoteDevice(mContext, history.getAddress());
                if (null != targetDev && watchManager.isWatchSystemInit(targetDev)) {
                    watchManager.syncIrkMessage(new OnWatchOpCallback<IrkMessage>() {
                        @Override
                        public void onSuccess(IrkMessage result) {
                            JL_Log.d(TAG, "onBtDeviceConnectStatus", "sync irk success. irk : " + result);
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            JL_Log.i(TAG, "onBtDeviceConnectStatus", "(syncIrkMessage) --> onFailed : " + error);
                        }
                    });
                }
            }
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            JL_Log.i(TAG, "onConnection", "device : " + device + ", status = " + status);
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
        public void onHistoryRecordChange(int op, HistoryRecord record) {
            mBtEventCbManager.onHistoryRecord(op, record);
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
            JL_Log.w(TAG, "onInitResult", "" + b);
        }

        @Override
        public void onAuthSuccess(BluetoothDevice bluetoothDevice) {
            JL_Log.w(TAG, "onAuthSuccess", "device : " + printDeviceInfo(bluetoothDevice));
            setDevAuth(bluetoothDevice, true);
            handleDeviceConnectedEvent(bluetoothDevice);
        }

        @Override
        public void onAuthFailed(BluetoothDevice bluetoothDevice, int i, String s) {
            JL_Log.e(TAG, "onAuthFailed", "device : " + printDeviceInfo(bluetoothDevice) + ", code = " + i + ", message = " + s);
            if (i == RcspAuth.ERR_AUTH_DEVICE_TIMEOUT) {
                // 设备认证超时
                // 可能是设备端的设备认证标识与APP设置的不一致导致。
                // 尝试传入「设备已连接」状态进行SDK初始化流程
                onAuthSuccess(bluetoothDevice);
                return;
            }
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

    private static class ConnectSppParam {
        private final String bleAddress;
        private final String sppAddress;

        public ConnectSppParam(String bleAddress, String sppAddress) {
            this.bleAddress = bleAddress;
            this.sppAddress = sppAddress;
        }

        public String getBleAddress() {
            return bleAddress;
        }

        public String getSppAddress() {
            return sppAddress;
        }

        @NonNull
        @Override
        public String toString() {
            return "ConnectSppParam{" +
                    "bleAddress='" + bleAddress + '\'' +
                    ", sppAddress='" + sppAddress + '\'' +
                    '}';
        }
    }
}
