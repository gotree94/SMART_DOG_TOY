package com.jieli.watchtesttool.tool.upgrade;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.JL_Constant;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BleScanMessage;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_bt_ota.util.ParseDataUtil;
import com.jieli.jl_rcsp.interfaces.bluetooth.CmdSnGenerator;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothEventListener;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.List;

/**
 * OTA实现类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class OTAManager extends BluetoothOTAManager {
    private final BluetoothHelper mBluetoothHelper = BluetoothHelper.getInstance();

    private String otaAddress;  //需要回连的设备地址

    public final static String OTA_FILE_SUFFIX = ".ufw";
    public final static String OTA_FILE_NAME = "update.ufw";
    public final static String OTA_ZIP_SUFFIX = ".zip";

    private BluetoothLeScanner bleScanner;
    private boolean isBleScanning;
    private String reconnectMac;

    public OTAManager(Context context) {
        super(context);
        mBluetoothHelper.addBluetoothEventListener(mBluetoothEventListener);
        configureOTA();
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return mBluetoothHelper.getConnectedBtDevice();
    }

    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {
        return mBluetoothHelper.getConnectedBluetoothGatt(getConnectedDevice());
    }

    @Override
    public void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        // 添加映射的OTA地址
        updateHistoryRecord(bluetoothDevice.getAddress());
        otaAddress = bluetoothDevice.getAddress();
        mBluetoothHelper.connectDeviceWithoutRecord(bluetoothDevice);
    }

    @Override
    public void disconnectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        JL_Log.d(TAG, "disconnectBluetoothDevice", "" + bluetoothDevice);
        mBluetoothHelper.disconnectDevice(bluetoothDevice);
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {
        return mBluetoothHelper.sendDataToDevice(bluetoothDevice, bytes);
    }

    @Override
    public void release() {
        super.release();
        mBluetoothHelper.removeBluetoothEventListener(mBluetoothEventListener);
    }

    @Override
    public void startOTA(IUpgradeCallback callback) {
        final BluetoothDevice device = getTargetDevice();
        checkDevice(device);
        if (device != null) {
            otaAddress = device.getAddress();
        }
        super.startOTA(new CustomUpgradeCallback(callback));
    }

    private void configureOTA() {
        BluetoothOTAConfigure configure = BluetoothOTAConfigure.createDefault();
        int connectWay = mBluetoothHelper.getBluetoothOp().getBluetoothOption().getPriority();
        if (mBluetoothHelper.isConnectedDevice()) {
            BluetoothDevice device = mBluetoothHelper.getConnectedBtDevice();
            connectWay = mBluetoothHelper.getBluetoothOp().isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE;
        }
        configure.setPriority(connectWay)
                .setNeedChangeMtu(false)
                .setMtu(BluetoothConstant.BLE_MTU_MIN)
                .setUseAuthDevice(false)
                .setUseReconnect(false)
                .setBleScanMode(2);
        String otaDir = AppUtil.createFilePath(WatchApplication.getWatchApplication(), WatchTestConstant.DIR_UPDATE);
        File dir = new File(otaDir);
        boolean isExistDir = dir.exists();
        if (!isExistDir) {
            isExistDir = dir.mkdir();
        }
        if (isExistDir) {
            String otaFilePath = AppUtil.obtainUpdateFilePath(otaDir, OTA_FILE_SUFFIX);
            if (null == otaFilePath) {
                otaFilePath = otaDir + "/" + OTA_FILE_NAME;
            }
            configure.setFirmwareFilePath(otaFilePath);
        }
        //保证两个库的SN生成器是同一个
        final CmdSnGenerator snGenerator = WatchManager.getInstance().getCmdSnGenerator();
        configure.setSnGenerator(snGenerator::getRcspCmdSeq);
        configure(configure);

        if (mBluetoothHelper.isConnectedDevice()) {
            final BluetoothDevice connectedDev = mBluetoothHelper.getConnectedBtDevice();
            onBtDeviceConnection(connectedDev, StateCode.CONNECTION_OK);
            checkDevice(connectedDev);
        }
    }

    private void checkDevice(BluetoothDevice device) {
        if (null != device && mBluetoothHelper.getBluetoothOp().isConnectedBLEDevice(device)) {
            int mtu = mBluetoothHelper.getBluetoothOp().getBleMtu(device);
            if (mBluetoothHelper.getBluetoothOp().getDeviceGatt(device) != null) {
                onMtuChanged(mBluetoothHelper.getBluetoothOp().getDeviceGatt(device), mtu + 3, BluetoothGatt.GATT_SUCCESS);
            }
        }
    }

    private void updateHistoryRecord(String updateAddress) {
        HistoryRecord record = mBluetoothHelper.getBluetoothOp().getHistoryRecord(otaAddress);
        JL_Log.d(TAG, "updateHistoryRecord", "otaAddress : " + otaAddress
                + ", updateAddress : " + updateAddress + ",\n" + record);
        if (null != record && (!BluetoothAdapter.checkBluetoothAddress(record.getUpdateAddress())
                || !record.getUpdateAddress().equals(updateAddress))) {
            record.setUpdateAddress(updateAddress);
            mBluetoothHelper.getBluetoothOp().updateHistoryRecord(record);
        }
    }

    private boolean isSingleOTA() {
        TargetInfoResponse deviceInfo = getDeviceInfo();
        return deviceInfo != null && !deviceInfo.isSupportDoubleBackup();
    }

    @SuppressLint("MissingPermission")
    private void reconnectDevice(String mac, boolean isNewAdv) {
        if (!ConnectUtil.isHasScanPermission(context) || !ConnectUtil.isHasConnectPermission(context))
            return;
        //第一步:搜索回连设备
        bleScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        if (null == bleScanner) return;
        //开始搜索设备
        try {
            reconnectMac = mac;
            bleScanner.startScan(bleScanCb);
            isBleScanning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void stopBleScan() {
        if (!ConnectUtil.isHasScanPermission(context) || !ConnectUtil.isHasConnectPermission(context))
            return;
        if (bleScanner != null && isBleScanning) {
            isBleScanning = false;
            bleScanner.stopScan(bleScanCb);
        }
        reconnectMac = null;
    }

    private final ScanCallback bleScanCb = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            final ScanRecord record = result.getScanRecord();
            if (device == null || record == null) return;
            //第二步: 判断设备是否目标设备地址
            if (TextUtils.equals(reconnectMac, device.getAddress())) {
                //第三步: 找到回连目标设备, 连接设备
                stopBleScan();
                connectBluetoothDevice(device);
                return;
            }
            //第二步: 解析广播包数据，找到回连设备
            //由于Android 13返回的数据有问题，需要重新解析下数据
            BleScanMessage scanMessage = ParseDataUtil.parseOTAFlagFilterWithBroad(record.getBytes(), JL_Constant.OTA_IDENTIFY);
            if (scanMessage == null) return;
            if (scanMessage.isOTA() && TextUtils.equals(reconnectMac, scanMessage.getOldBleAddress())) {
                //第三步: 找到回连目标设备, 连接设备
                stopBleScan();
                connectBluetoothDevice(device);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            //搜索的设备列表
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //开启搜索失败
            stopBleScan();
        }
    };

    private final BluetoothEventListener mBluetoothEventListener = new BluetoothEventListener() {

        @Override
        public void onBleMtuChange(BluetoothGatt gatt, int mtu, int status) {
            onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            if (status == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                SystemClock.sleep(300); //等待其他部分初始化
            }
            status = AppUtil.convertOtaConnectStatus(status);
            onBtDeviceConnection(device, status);
        }

        @Override
        public void onReceiveData(BluetoothDevice device, byte[] data) {
            onReceiveDeviceData(device, data);
        }
    };

    private final class CustomUpgradeCallback implements IUpgradeCallback {
        private final IUpgradeCallback mIUpgradeCallback;

        public CustomUpgradeCallback(IUpgradeCallback callback) {
            mIUpgradeCallback = callback;
        }

        @Override
        public void onStartOTA() {
            if (mIUpgradeCallback != null) mIUpgradeCallback.onStartOTA();
        }

        @Override
        public void onNeedReconnect(String s, boolean isNewADV) {
            HistoryRecord record = mBluetoothHelper.getBluetoothOp().getHistoryRecord(s);
            if (record != null && record.getConnectType() != BluetoothConstant.PROTOCOL_TYPE_BLE) {
                String bleAddress = record.getMappedAddress();
                String edrAddress = record.getAddress();
                //更新连接方式
                if (BluetoothAdapter.checkBluetoothAddress(bleAddress)) {
                    record.setConnectType(BluetoothConstant.PROTOCOL_TYPE_BLE);
                    record.setAddress(bleAddress);
                    record.setMappedAddress(edrAddress);
                    mBluetoothHelper.getBluetoothOp().updateHistoryRecord(record);
                    otaAddress = bleAddress;
                }
            }
            if (getBluetoothOption().isUseReconnect()) {
                reconnectDevice(s, isNewADV);
            }
            if (mIUpgradeCallback != null) mIUpgradeCallback.onNeedReconnect(s, isNewADV);
        }

        @Override
        public void onProgress(int i, float v) {
            if (mIUpgradeCallback != null) mIUpgradeCallback.onProgress(i, v);
        }

        @Override
        public void onStopOTA() {
            // 移除映射的OTA地址
            updateHistoryRecord(null);
            otaAddress = null;
            if (mIUpgradeCallback != null) mIUpgradeCallback.onStopOTA();
        }

        @Override
        public void onCancelOTA() {
            otaAddress = null;
            if (mIUpgradeCallback != null) mIUpgradeCallback.onCancelOTA();
        }

        @Override
        public void onError(BaseError baseError) {
            if (getBluetoothOption().isUseReconnect()) {
                stopBleScan();
            }
            if (null != baseError && baseError.getSubCode() == ErrorCode.SUB_ERR_NEED_UPDATE_RESOURCE) {
                updateHistoryRecord(null);
            }
            otaAddress = null;
            if (mIUpgradeCallback != null) mIUpgradeCallback.onError(baseError);
        }
    }
}
