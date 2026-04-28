package com.jieli.healthaide.tool.watch;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.constant.JL_DeviceType;
import com.jieli.bluetooth_connect.data.HistoryRecordDbHelper;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.CHexConverter;
import com.jieli.bluetooth_connect.util.ParseDataUtil;
import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.tool.watch.synctask.WatchListSyncTask;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.bluetooth.CmdSnGenerator;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceConfiguration;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.IrkMessage;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.tool.DeviceStatusManager;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.util.ArrayList;

/**
 * 手表管理类
 *
 * @author zqjasonZhong
 * @since 2021/3/8
 */
public class WatchManager extends WatchOpImpl {
    private final static String TAG = WatchManager.class.getSimpleName();
    private volatile static WatchManager instance;

    private final BluetoothHelper mBluetoothHelper;
    private final DeviceStatusManager mStatusManager;
    private final HistoryRecordDbHelper mHistoryRecordDbHelper;
    private final DeviceConfigureListenerHelper mConfigureListenerHelper;
//    private final ALiIOTKit mALiIOTKit;

    private BluetoothDevice mTargetDevice;
    private GetWatchMsgTask mGetWatchMsgTask;
    private boolean isFirmwareOTA; //是否固件升级标志

    private boolean isBleChangeSpp; //是否需要BLE切换SPP

    //表盘信息列表
    public final ArrayList<WatchInfo> watchInfoList = new ArrayList<>();
    public ArrayList<FatFile> devFatFileList;

    public final static int ERR_OVER_LIMIT = 99;

    private HealthOpImpl healthOp;

    private WatchManager(int func) {
        super(func);
        mBluetoothHelper = BluetoothHelper.getInstance();
        mStatusManager = DeviceStatusManager.getInstance();
        mHistoryRecordDbHelper = HistoryRecordDbHelper.getInstance();
        mConfigureListenerHelper = new DeviceConfigureListenerHelper();
        mBluetoothHelper.addBluetoothEventListener(mEventListener);

//        mALiIOTKit = new ALiIOTKit(this);
        final BluetoothDevice connectedDev = mBluetoothHelper.getConnectedBtDevice();
        if (mBluetoothHelper.isConnectedDevice() && mBluetoothHelper.isAuthDevice(connectedDev)) {
            setTargetDevice(connectedDev);
        }

        registerOnWatchCallback(mOnWatchCallback);
        registerOnWatchCallback(new AlarmHandleTask(this));
        registerOnRcspEventListener(mOnRcspEventListener);
    }

    public static WatchManager getInstance() {
        if (null == instance) {
            synchronized (WatchManager.class) {
                if (null == instance) {
                    instance = new WatchManager(FUNC_WATCH);
                }
            }
        }
        return instance;
    }

    public void addOnDeviceConfigureListener(OnDeviceConfigureListener listener) {
        mConfigureListenerHelper.addListener(listener);
    }

    public void removeOnDeviceConfigureListener(OnDeviceConfigureListener listener) {
        mConfigureListenerHelper.removeListener(listener);
    }

    public boolean isConnected() {
        return getConnectedDevice() != null && isWatchSystemInit(getConnectedDevice());
    }

    public boolean isWatchSystemInit(BluetoothDevice device) {
        if (null == device) return false;
        return BluetoothUtil.deviceEquals(getConnectedDevice(), device) && isWatchSystemOk();
    }

    public BluetoothHelper getBluetoothHelper() {
        return mBluetoothHelper;
    }

    public CmdSnGenerator getCmdSnGenerator() {
        return mCmdSnGenerator;
    }

    public boolean isBleChangeSpp() {
        return isBleChangeSpp;
    }

    public void setBleChangeSpp(boolean bleChangeSpp) {
        isBleChangeSpp = bleChangeSpp;
    }

    public boolean isFirmwareOTA() {
        return isFirmwareOTA;
    }

    public void setFirmwareOTA(boolean firmwareOTA) {
        isFirmwareOTA = firmwareOTA;
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
//        JL_Log.w(TAG, "-getConnectedDevice- " + BluetoothUtil.printBtDeviceInfo(mTargetDevice));
        return mTargetDevice;
    }

    public HealthOpImpl getHealthOp() {
        if (null == healthOp) {
            healthOp = new HealthOpImpl(this);
        }
        return healthOp;
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        //自行实现蓝牙连接的客户，请打开下面打印代码
//        JL_Log.d(TAG, CalendarUtil.formatString("sendDataToDevice >> %s, data:[%s]",
//                HealthUtil.printBtDeviceInfo(device), CHexConver.byte2HexStr(data)));
        return mBluetoothHelper.sendDataToDevice(device, data);
    }

    @Override
    public void release() {
        JL_Log.e(TAG, "release", "");
        if (healthOp != null) {
            healthOp.destroy();
            healthOp = null;
        }
        super.release();
        unregisterOnWatchCallback(mOnWatchCallback);
        unregisterOnRcspEventListener(mOnRcspEventListener);
        mBluetoothHelper.removeBluetoothEventListener(mEventListener);
//        mALiIOTKit.destroy();
        watchInfoList.clear();
        mConfigureListenerHelper.release();
        if (null != mGetWatchMsgTask) {
            mGetWatchMsgTask.interrupt();
            mGetWatchMsgTask = null;
        }
        mTargetDevice = null;
        instance = null;
    }

    public void listWatchFileList(final OnWatchOpCallback<ArrayList<WatchInfo>> callback) {
        if (watchInfoList.isEmpty()) {
            JL_Log.d(TAG, "listWatchFileList", "add WatchListSyncTask");
            SyncTaskManager.getInstance().addTask(new WatchListSyncTask(callback, SyncTaskManager.getInstance()), true);
        } else {
            if (callback != null) callback.onSuccess(watchInfoList);
        }
    }

    public void updateWatchFileListByDevice(final OnWatchOpCallback<ArrayList<WatchInfo>> callback) {
        watchInfoList.clear();
        listWatchFileList(callback);
    }

    public void getCurrentWatchMsg(final OnWatchOpCallback<WatchInfo> callback) {
        if (!isWatchSystemInit(getConnectedDevice())) {
            if (callback != null)
                callback.onFailed(new BaseError(FatFsErrCode.RES_INT_ERR, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_INT_ERR)));
            return;
        }
        listWatchFileList(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
            @Override
            public void onSuccess(ArrayList<WatchInfo> result) {
                if (!result.isEmpty()) {
                    getCurrentWatchInfo(new OnWatchOpCallback<FatFile>() {
                        @Override
                        public void onSuccess(FatFile result) {
                            JL_Log.d(TAG, "getCurrentWatchMsg", "getCurrentWatchInfo#Success ---> " + result);
                            WatchInfo info = getWatchInfoByFatFile(result);
                            if (info != null) {
                                if (callback != null) callback.onSuccess(info);
                            } else {
                                if (callback != null)
                                    callback.onFailed(new BaseError(FatFsErrCode.RES_ERR_PARAM, "not found watch info."));
                            }
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            if (callback != null) callback.onFailed(error);
                        }
                    });
                } else {
                    if (callback != null)
                        callback.onFailed(new BaseError(FatFsErrCode.RES_ERR_PARAM, "not found watch info."));
                }
            }

            @Override
            public void onFailed(BaseError error) {
                if (callback != null) callback.onFailed(error);
            }
        });
    }

    public void enableWatchCustomBg(final String fatFilePath, final OnWatchOpCallback<FatFile> callback) {
        enableCustomWatchBg(fatFilePath, new OnWatchOpCallback<FatFile>() {
            @Override
            public void onSuccess(FatFile result) {
                final FatFile custom = result;
                getCurrentWatchMsg(new OnWatchOpCallback<WatchInfo>() {
                    @Override
                    public void onSuccess(WatchInfo result) {
                        result.setCustomBgFatPath(fatFilePath);
                        JL_Log.d(TAG, "enableWatchCustomBg", "" + result);
                        if (callback != null) callback.onSuccess(custom);
                    }

                    @Override
                    public void onFailed(BaseError error) {
                        if (callback != null) callback.onFailed(error);
                    }
                });
            }

            @Override
            public void onFailed(BaseError error) {
                if (callback != null) callback.onFailed(error);
            }
        });
    }

    public void deleteWatch(final WatchInfo info, final OnFatFileProgressListener listener) {
        if (null == info || null == info.getWatchFile()) return;
        JL_Log.d(TAG, "deleteWatch", info.toString());
        if (info.hasCustomBgFatPath()) { //有自定义背景，先删除自定义背景，再删除表盘文件
            JL_Log.w(TAG, "deleteWatch", "custom background file path : " + info.getCustomBgFatPath());
            deleteWatchFile(info.getCustomBgFatPath(), new OnFatFileProgressListener() {
                @Override
                public void onStart(String filePath) {
                    JL_Log.i(TAG, "deleteWatch", "onStart = " + filePath);
                    if (listener != null) listener.onStart(info.getWatchFile().getPath());
                }

                @Override
                public void onProgress(float progress) {
                    float cProgress = progress * 100 / 200f;
                    JL_Log.d(TAG, "deleteWatch", "onProgress = " + cProgress);
                    if (listener != null) listener.onProgress(cProgress);
                }

                @Override
                public void onStop(int result) {
                    JL_Log.i(TAG, "deleteWatch", "onStop = " + result);
                    if (result == FatFsErrCode.RES_OK) {
                        JL_Log.d(TAG, "deleteWatch", "dial path : " + info.getWatchFile().getPath());
                        deleteWatchFile(info.getWatchFile().getPath(), new OnFatFileProgressListener() {
                            @Override
                            public void onStart(String filePath) {
                                JL_Log.i(TAG, "deleteWatch", "onStart = " + filePath);
                            }

                            @Override
                            public void onProgress(float progress) {
                                float cProgress = (progress + 100) * 100 / 200f;
                                JL_Log.d(TAG, "deleteWatch", "onProgress = " + cProgress);
                                if (listener != null) listener.onProgress(cProgress);
                            }

                            @Override
                            public void onStop(int result) {
                                JL_Log.w(TAG, "deleteWatch", "onStop = " + result);
                                final int deleteResult = result;
                                updateWatchFileListByDevice(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
                                    @Override
                                    public void onSuccess(ArrayList<WatchInfo> result) {
                                        if (listener != null) listener.onStop(deleteResult);
                                    }

                                    @Override
                                    public void onFailed(BaseError error) {
                                        if (listener != null)
                                            listener.onStop(FatFsErrCode.RES_RCSP_SEND);
                                    }
                                });
                            }
                        });
                    } else {
                        if (listener != null) listener.onStop(result);
                    }
                }
            });
            return;
        }
        JL_Log.d(TAG, "deleteWatch", "No custom background, delete dial directly. dial path : " + info.getWatchFile().getPath());
        deleteWatchFile(info.getWatchFile().getPath(), new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                JL_Log.d(TAG, "deleteWatch", "onStart = " + filePath);
                if (listener != null) listener.onStart(filePath);
            }

            @Override
            public void onProgress(float progress) {
                JL_Log.d(TAG, "deleteWatch", "onProgress = " + progress);
                if (listener != null) listener.onProgress(progress);
            }

            @Override
            public void onStop(int result) {
                JL_Log.w(TAG, "deleteWatch", "onStop = " + result);
                if (result == FatFsErrCode.RES_OK) {
                    updateWatchFileListByDevice(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
                        @Override
                        public void onSuccess(ArrayList<WatchInfo> result) {
                            if (listener != null) listener.onStop(FatFsErrCode.RES_OK);
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            if (listener != null) listener.onStop(FatFsErrCode.RES_RCSP_SEND);
                        }
                    });
                } else {
                    if (listener != null) listener.onStop(result);
                }
            }
        });
    }

    public void addFatFile(final String filePath, final boolean isNoNeedCheck, final OnFatFileProgressListener listener) {
        if (watchInfoList.size() >= HealthConstant.WATCH_MAX_COUNT) {
            if (null != listener) listener.onStop(ERR_OVER_LIMIT);
            return;
        }
        createWatchFile(filePath, isNoNeedCheck, new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                if (listener != null) listener.onStart(filePath);
            }

            @Override
            public void onProgress(float progress) {
                if (listener != null) listener.onProgress(progress);
            }

            @Override
            public void onStop(int result) {
                JL_Log.i(TAG, "addFatFile", "onStop : " + result);
                if (result == FatFsErrCode.RES_OK) {
                    if (!BuildConfig.DEBUG) {
                        FileUtil.deleteFile(new File(filePath));
                    }
                    updateWatchFileListByDevice(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
                        @Override
                        public void onSuccess(ArrayList<WatchInfo> result) {
                            if (listener != null) listener.onStop(FatFsErrCode.RES_OK);
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            if (listener != null) listener.onStop(FatFsErrCode.RES_RCSP_SEND);
                        }
                    });
                } else {
                    if (listener != null) listener.onStop(result);
                }
            }
        });
    }

    public DeviceInfo getDeviceInfo(BluetoothDevice device) {
        return mStatusManager.getDeviceInfo(device);
    }

    public ExternalFlashMsgResponse getExternalFlashMsg(BluetoothDevice device) {
        return mStatusManager.getExtFlashMsg(device);
    }

    public void updateWatchInfo(WatchInfo info) {
        if (null == info || null == info.getWatchFile()) return;
        WatchInfo cacheInfo = getWatchInfoByFatFile(info.getWatchFile());
        if (null == cacheInfo) return;
        if (cacheInfo.getStatus() != info.getStatus()) {
            cacheInfo.setStatus(info.getStatus());
        }
        if (cacheInfo.getServerFile() != info.getServerFile()) {
            cacheInfo.setServerFile(info.getServerFile());
        }
        if (cacheInfo.getUpdateUUID() == null || !cacheInfo.getUpdateUUID().equals(info.getUpdateUUID())) {
            cacheInfo.setUpdateUUID(info.getUpdateUUID());
        }
        if (cacheInfo.getUpdateFile() != info.getUpdateFile()) {
            cacheInfo.setUpdateFile(info.getUpdateFile());
        }
    }

    private void setTargetDevice(BluetoothDevice device) {
        if (!BluetoothUtil.deviceEquals(device, mTargetDevice)) {
            mTargetDevice = device;
            BluetoothDevice connectedDevice = getTargetDevice();
            JL_Log.i(TAG, "setTargetDevice", "device = " + HealthUtil.printBtDeviceInfo(device)
                    + ", connectedDevice = " + HealthUtil.printBtDeviceInfo(connectedDevice));
            if (device != null) {
                notifyBtDeviceConnection(device, StateCode.CONNECTION_OK);
            }
        }
    }

    private void updateHistoryRecordMsg(BluetoothDevice device) {
        if (!mBluetoothHelper.isConnectedBtDevice(device)) return;
        DeviceInfo deviceInfo = mStatusManager.getDeviceInfo(device);
        if (deviceInfo == null) return;
        final BluetoothManager btOp = mBluetoothHelper.getBluetoothOp();
        final HistoryRecord record = btOp.getHistoryRecord(device.getAddress());
        if (null == record) return;
        boolean isChange = false;
        final int sdkType = deviceInfo.getSdkType();
        int connectWay = btOp.isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP :
                btOp.isConnectedGattOverBrEdrDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_GATT_OVER_BR_EDR
                        : BluetoothConstant.PROTOCOL_TYPE_BLE;
        if (sdkType != record.getSdkFlag()) {
            record.setSdkFlag(sdkType);
            isChange = true;
        }
        String mappedAddress;
        if (connectWay == BluetoothConstant.PROTOCOL_TYPE_BLE) {
            mappedAddress = deviceInfo.getEdrAddr();
        } else {
            mappedAddress = deviceInfo.getBleAddr();
        }
        if (CHexConverter.checkBluetoothAddress(mappedAddress) && !TextUtils.equals(mappedAddress, record.getMappedAddress())) {
            record.setMappedAddress(mappedAddress);
            isChange = true;
        }
        if (deviceInfo.getUid() != 0 || deviceInfo.getPid() != 0) {
            if (deviceInfo.getVid() != record.getVid()) {
                record.setVid(deviceInfo.getVid());
                isChange = true;
            }
            if (deviceInfo.getUid() != record.getUid()) {
                record.setUid(deviceInfo.getUid());
                isChange = true;
            }
            if (deviceInfo.getPid() != record.getPid()) {
                record.setPid(deviceInfo.getPid());
                isChange = true;
            }
        }
        boolean isBLEChangeSPP = mBluetoothHelper.isBleToSpp(device) || deviceInfo.isBLEToSppWay();
        boolean isWatch = sdkType == JLChipFlag.JL_CHIP_FLAG_695X_WATCH || sdkType == JLChipFlag.JL_CHIP_FLAG_701X_WATCH
                || sdkType == JLChipFlag.JL_CHIP_FLAG_707N_WATCH;
        if (isBLEChangeSPP && isWatch) {
            if (record.getDevType() != JL_DeviceType.JL_DEVICE_TYPE_WATCH) { //FIXME : 后续要修改
                JL_Log.i(TAG, "updateHistoryRecordMsg", "change device type: 5");
                record.setDevType(JL_DeviceType.JL_DEVICE_TYPE_WATCH);
                isChange = true;
            }
        }
        if (isChange) {
            JL_Log.i(TAG, "updateHistoryRecordMsg", "updateHistoryRecord");
            mHistoryRecordDbHelper.updateHistoryRecord(record);
        }
    }

    public ArrayList<FatFile> getWatchList(ArrayList<FatFile> list) {
        if (null == list) return new ArrayList<>();
        ArrayList<FatFile> result = new ArrayList<>();
        for (FatFile watchFile : list) {
            if (watchFile.getName() == null) continue;
            if (watchFile.getName().startsWith("WATCH") || watchFile.getName().startsWith("watch")) {
                result.add(watchFile);
            }
        }
        return result;
    }

    public WatchInfo getWatchInfoByFatFile(FatFile watchFile) {
        if (watchFile == null || watchInfoList.isEmpty()) return null;
        WatchInfo result = null;
        for (WatchInfo watchInfo : watchInfoList) {
//            JL_Log.d(TAG, "-getWatchInfoByList- watchInfo = " + watchInfo + ", target = " + watchFile);
            if (watchFile.getPath() != null && watchFile.getPath().equals(watchInfo.getWatchFile().getPath())) {
                result = watchInfo;
                break;
            }
        }
        return result;
    }

    public WatchInfo getWatchInfoByPath(String fatFilePath) {
        if (null == fatFilePath || null == devFatFileList || devFatFileList.isEmpty()) return null;
        FatFile watchFile = null;
        for (FatFile file : devFatFileList) {
            if (fatFilePath.toUpperCase().equals(file.getPath())) {
                watchFile = file;
                break;
            }
        }
        return getWatchInfoByFatFile(watchFile);
    }

    private void requestDeviceConfigure(BluetoothDevice device) {
        DeviceConfiguration deviceConfiguration = mStatusManager.getDeviceConfigure(device);
        if (null == deviceConfiguration) {
            requestDeviceConfigure(new OnWatchOpCallback<WatchConfigure>() {
                @Override
                public void onSuccess(WatchConfigure result) {
                    mConfigureListenerHelper.onUpdate(device);
                }

                @Override
                public void onFailed(BaseError error) {

                }
            });
        } else {
            mConfigureListenerHelper.onUpdate(device);
        }
    }

    private void syncIrkMsg(BluetoothDevice device) {
        if (null == device) return;
        final DeviceInfo deviceInfo = getDeviceInfo(device);
        if (null == deviceInfo) return;
        final String bleAddress = deviceInfo.getBleAddr();
        //不是RPA，不去获取IRK信息
        if (!ParseDataUtil.isResolvablePrivateAddress(bleAddress)) return;
        final IrkMessage irkMessage = deviceInfo.getIrkMessage();
        JL_Log.d(TAG, "syncIrkMsg", "irkMessage : " + irkMessage);
        if (irkMessage == null || !irkMessage.isPaired()) {
            syncIrkMessage(new OnWatchOpCallback<IrkMessage>() {
                @Override
                public void onSuccess(IrkMessage result) {
                    JL_Log.d(TAG, "syncIrkMsg", "sync irk success. irk : " + result);
                }

                @Override
                public void onFailed(BaseError error) {
                    JL_Log.i(TAG, "syncIrkMsg", "(syncIrkMessage) --> onFailed : " + error);
                }
            });
        }
    }

    private final BluetoothEventListener mEventListener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            status = HealthUtil.convertWatchConnectStatus(status);
            switch (status) {
                case StateCode.CONNECTION_OK:
                    setTargetDevice(device);
                    break;
                case StateCode.CONNECTION_CONNECTING:
                    notifyBtDeviceConnection(device, status);
                    break;
                case StateCode.CONNECTION_FAILED:
                case StateCode.CONNECTION_DISCONNECT:
                    if (getTargetDevice() == null || BluetoothUtil.deviceEquals(getTargetDevice(), device)) {
                        setTargetDevice(null);
                        watchInfoList.clear();
                        notifyBtDeviceConnection(device, status);
                    }
                    break;
            }
        }

        @Override
        public void onReceiveData(BluetoothDevice device, byte[] data) {
            if (BluetoothUtil.deviceEquals(device, getConnectedDevice())) {
                notifyReceiveDeviceData(device, data);
            }
        }
    };

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {

        @Override
        public void onRcspInit(BluetoothDevice device, boolean isInit) {
            if (isInit) {
                updateHistoryRecordMsg(device);
                if (mBluetoothHelper.getBluetoothOp().isConnectedBLEDevice(device)) {
                    DeviceInfo deviceInfo = getDeviceInfo(device);
                    setBleChangeSpp((deviceInfo.getEdrProfile() & 0x80) > 0);
                } else {
                    setBleChangeSpp(false);
                }
            } else {
                mBluetoothHelper.disconnectDevice(device);
            }
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            updateHistoryRecordMsg(device);
        }

        @Override
        public void onWatchSystemInit(int code) {
            if (code == 0) {
                final BluetoothDevice connectedDevice = getConnectedDevice();
                final DeviceInfo deviceInfo = getDeviceInfo(connectedDevice);
                int expandMode = deviceInfo.getExpandMode();
                if (isBleChangeSpp) {
                    setBleChangeSpp(expandMode == RcspConstant.EXPAND_MODE_NONE);
                }
                if (!HealthConstant.ONLY_CONNECT_BLE) {
                    if (isBleChangeSpp) {
                        // TODO: 2022/4/19 增加检测是否需要BLE切换SPP流程
                        mBluetoothHelper.bleChangeSpp(WatchManager.this, connectedDevice);
                        return;
                    }
                    if (expandMode == RcspConstant.EXPAND_MODE_NONE || expandMode == RcspConstant.EXPAND_MODE_ONLY_UPDATE_RESOURCE) {
                        mBluetoothHelper.syncEdrConnectionStatus(connectedDevice, deviceInfo);
                    }
                }
                if (deviceInfo.isSupportDevConfigure()) {
                    requestDeviceConfigure(connectedDevice);
                }
                syncIrkMsg(connectedDevice);
            } else { //手表系统初始化失败，不设置切换标志位
                setBleChangeSpp(false);
            }
        }
    };

    private final OnRcspEventListener mOnRcspEventListener = new OnRcspEventListener() {
        @Override
        public void onIrkMessage(BluetoothDevice device, IrkMessage irkMessage) {
            if (null == device || null == irkMessage) return;
            final HistoryRecord historyRecord = mBluetoothHelper.getBluetoothOp().getHistoryRecord(device.getAddress());
            if (null == historyRecord) return;
            String irkValue = CHexConverter.byte2HexStr(irkMessage.getIrkValue());
            if (!irkValue.equals(historyRecord.getIrkValue())) {
                historyRecord.setIrkValue(irkValue);
                JL_Log.d(TAG, "onIrkMessage", "save irk value : " + irkValue);
                mBluetoothHelper.getBluetoothOp().getHistoryRecordHelper().updateHistoryRecord(historyRecord);
            }
        }
    };
}
