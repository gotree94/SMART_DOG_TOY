package com.jieli.healthaide.tool.upgrade;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.SystemClock;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.jieli.jl_rcsp.interfaces.bluetooth.CmdSnGenerator;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;

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
        String otaDir = HealthUtil.createFilePath(HealthApplication.getAppViewModel().getApplication(), HealthConstant.DIR_UPDATE);
        File dir = new File(otaDir);
        boolean isExistDir = dir.exists();
        if (!isExistDir) {
            isExistDir = dir.mkdir();
        }
        if (isExistDir) {
            String otaFilePath = HealthUtil.obtainUpdateFilePath(otaDir, OTA_FILE_SUFFIX);
            if (null == otaFilePath) {
                otaFilePath = otaDir + File.separator + OTA_FILE_NAME;
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
        if (null != device && mBluetoothHelper.getBluetoothOp().isConnectedGattDevice(device)) {
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
            status = HealthUtil.convertOtaConnectStatus(status);
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
                String bleAddress = record.getBleAddress();
                String edrAddress = record.getClassicAddress();
                //更新连接方式
                if (BluetoothAdapter.checkBluetoothAddress(bleAddress)) {
                    record.setConnectType(BluetoothConstant.PROTOCOL_TYPE_BLE);
                    record.setAddress(bleAddress);
                    record.setMappedAddress(edrAddress);
                    mBluetoothHelper.getBluetoothOp().updateHistoryRecord(record);
                    otaAddress = bleAddress;
                }
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
            String otaFilePath = getBluetoothOption().getFirmwareFilePath();
            if (null != otaFilePath) {
                FileUtil.deleteFile(new File(otaFilePath));
            }
        }

        @Override
        public void onCancelOTA() {
            otaAddress = null;
            if (mIUpgradeCallback != null) mIUpgradeCallback.onCancelOTA();
        }

        @Override
        public void onError(BaseError baseError) {
            if (null != baseError && baseError.getSubCode() == ErrorCode.SUB_ERR_NEED_UPDATE_RESOURCE) {
                updateHistoryRecord(null);
            }
            otaAddress = null;
            if (mIUpgradeCallback != null) mIUpgradeCallback.onError(baseError);
        }
    }
}
