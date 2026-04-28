package com.jieli.watchtesttool.tool.watch;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.data.HistoryRecordDbHelper;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.util.CommonUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.interfaces.OnFatFileProgressListener;
import com.jieli.jl_fatfs.model.FatFile;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.WatchOpImpl;
import com.jieli.jl_rcsp.interfaces.bluetooth.CmdSnGenerator;
import com.jieli.jl_rcsp.interfaces.listener.ThreadStateListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.data.bean.WatchInfo;
import com.jieli.watchtesttool.data.db.sensor.SensorDataListener;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothEventListener;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WLog;

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

    private final BluetoothHelper mBluetoothHelper = BluetoothHelper.getInstance();

    private BluetoothDevice mTargetDevice;
    private GetWatchMsgTask mGetWatchMsgTask;

    private final ArrayList<WatchInfo> watchInfoList = new ArrayList<>();
    private ArrayList<FatFile> devFatFileList;

    private WatchManager(int func) {
        super(func);
        mBluetoothHelper.addBluetoothEventListener(mEventListener);

        if (mBluetoothHelper.isConnectedDevice()) {
            final BluetoothDevice connectedDev = mBluetoothHelper.getConnectedBtDevice();
            setTargetDevice(connectedDev);
        }
        registerOnRcspEventListener(new SensorDataListener());
        registerOnWatchCallback(new AlarmHandleTask(this));
        registerOnWatchCallback(mOnWatchCallback);
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

    public BluetoothHelper getBluetoothHelper() {
        return mBluetoothHelper;
    }

    public CmdSnGenerator getCmdSnGenerator() {
        return mCmdSnGenerator;
    }

    public boolean isWatchSystemInit(BluetoothDevice device) {
        if (null == device) return false;
        return BluetoothUtil.deviceEquals(getConnectedDevice(), device) && isWatchSystemOk();
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return mTargetDevice;
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
//        JL_Log.v("sendDataToDevice", CHexConver.byte2HexStr(data));
        return mBluetoothHelper.sendDataToDevice(device, data);
    }

    @Override
    public void release() {
        super.release();
        if (null != mGetWatchMsgTask) {
            mGetWatchMsgTask.interrupt();
            mGetWatchMsgTask = null;
        }
        unregisterOnWatchCallback(mOnWatchCallback);
        mBluetoothHelper.removeBluetoothEventListener(mEventListener);
        instance = null;
    }

    public void listWatchFileList(final OnWatchOpCallback<ArrayList<WatchInfo>> callback) {
        if (watchInfoList.isEmpty()) {
            listWatchList(new OnWatchOpCallback<ArrayList<FatFile>>() {
                @Override
                public void onSuccess(ArrayList<FatFile> result) {
                    devFatFileList = result;
                    if (devFatFileList == null) devFatFileList = new ArrayList<>();
                    ArrayList<FatFile> list = getWatchList(result);
                    if (!list.isEmpty()) {
                        if (null == mGetWatchMsgTask) {
                            mGetWatchMsgTask = new GetWatchMsgTask(WatchManager.this, list, new CustomListWatchFileListCallback(callback), new ThreadStateListener() {
                                @Override
                                public void onStart(long threadId) {

                                }

                                @Override
                                public void onFinish(long threadId) {
                                    if (mGetWatchMsgTask != null && mGetWatchMsgTask.getId() == threadId) {
                                        mGetWatchMsgTask = null;
                                    }
                                }
                            });
                            mGetWatchMsgTask.start();
                        }
                    } else {
                        watchInfoList.clear();
                        if (callback != null) callback.onSuccess(new ArrayList<>());
                    }
                }

                @Override
                public void onFailed(BaseError error) {
                    watchInfoList.clear();
                    if (callback != null) callback.onFailed(error);
                }
            });
        } else {
            if (callback != null) callback.onSuccess(watchInfoList);
        }
    }

    public void updateWatchFileListByDevice(final OnWatchOpCallback<ArrayList<WatchInfo>> callback) {
        watchInfoList.clear();
        listWatchFileList(callback);
    }

    public void getCurrentWatchMsg(final OnWatchOpCallback<WatchInfo> callback) {
        listWatchFileList(new OnWatchOpCallback<ArrayList<WatchInfo>>() {
            @Override
            public void onSuccess(ArrayList<WatchInfo> result) {
                if (!result.isEmpty()) {
                    getCurrentWatchInfo(new OnWatchOpCallback<FatFile>() {
                        @Override
                        public void onSuccess(FatFile result) {
                            WLog.d(TAG, "getCurrentWatchMsg", "" + result);
                            WatchInfo info = getWatchInfoByFatFile(result);
                            if (info != null) {
                                if (callback != null) callback.onSuccess(info);
                            } else {
                                if (callback != null)
                                    callback.onFailed(new BaseError(ErrorCode.SUB_ERR_PARAMETER, "not found watch info."));
                            }
                        }

                        @Override
                        public void onFailed(BaseError error) {
                            if (callback != null) callback.onFailed(error);
                        }
                    });
                } else {
                    if (callback != null)
                        callback.onFailed(new BaseError(ErrorCode.SUB_ERR_PARAMETER, "not found watch info."));
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
                        WLog.d(TAG, "enableWatchCustomBg", "" + result);
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
        if (null == info || null == info.getFatFile()) return;
        WLog.d(TAG, "deleteWatch", "" + info);
        if (info.hasCustomBgFatPath()) { //有自定义背景，先删除自定义背景，再删除表盘文件
            final String filePath = info.getCustomBgFatPath();
            WLog.i(TAG, "deleteWatch", "CustomBgFatPath = " + filePath);
            deleteWatchFile(filePath, new OnFatFileProgressListener() {
                @Override
                public void onStart(String filePath) {
                    WLog.i(TAG, "deleteWatch", "onStart ---> " + filePath);
                    if (listener != null) listener.onStart(info.getFatFile().getPath());
                }

                @Override
                public void onProgress(float progress) {
                    float cProgress = progress * 100 / 200f;
                    WLog.d(TAG, "deleteWatch", "onProgress = " + cProgress);
                    if (listener != null) listener.onProgress(cProgress);
                }

                @Override
                public void onStop(int result) {
                    WLog.i(TAG, "deleteWatch", "onStop = " + result);
                    if (result == FatFsErrCode.RES_OK) {
                        tryToDeleteWatch(info.getFatFile(), listener);
                    } else {
                        if (listener != null) listener.onStop(result);
                    }
                }
            });
            return;
        }
        tryToDeleteWatch(info.getFatFile(), listener);

    }

    private void tryToDeleteWatch(FatFile fatFile, OnFatFileProgressListener listener) {
        WLog.d(TAG, "tryToDeleteWatch", "filePath : " + fatFile.getPath());
        deleteWatchFile(fatFile.getPath(), new OnFatFileProgressListener() {
            @Override
            public void onStart(String filePath) {
                WLog.i(TAG, "tryToDeleteWatch", "onStart ---> " + filePath);
                if (listener != null) listener.onStart(filePath);
            }

            @Override
            public void onProgress(float progress) {
                WLog.d(TAG, "tryToDeleteWatch", "onProgress = " + progress);
                if (listener != null) listener.onProgress(progress);
            }

            @Override
            public void onStop(int result) {
                WLog.d(TAG, "tryToDeleteWatch", "onStop = " + result);
                final String fileName = fatFile.getName();
                if (result == FatFsErrCode.RES_OK && (fileName.startsWith("WATCH") ||
                        fileName.startsWith("BGP"))) {
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

    public boolean isExistSDCard() {
        DeviceInfo deviceInfo = getDeviceInfo(getConnectedDevice());
        if (deviceInfo == null) return false;
        boolean ret = deviceInfo.isSupportSd1();
        WLog.d(RcspConstant.DEBUG_LOG_TAG, "-isExistSDCard- ret = " + ret);
        return ret;
    }

    public ExternalFlashMsgResponse getExternalFlashMsg(BluetoothDevice device) {
        return mStatusManager.getExtFlashMsg(device);
    }

    private void setTargetDevice(BluetoothDevice device) {
        if (!BluetoothUtil.deviceEquals(device, mTargetDevice)) {
            mTargetDevice = device;
            BluetoothDevice connectedDevice = getTargetDevice();
            WLog.i(RcspConstant.DEBUG_LOG_TAG, "-setTargetDevice- device = " + AppUtil.printBtDeviceInfo(device)
                    + ", connectedDevice = " + AppUtil.printBtDeviceInfo(connectedDevice));
            if (device != null) {
                notifyBtDeviceConnection(device, StateCode.CONNECTION_OK);
            }
        }
    }

    private void updateHistoryRecordMsg(BluetoothDevice device) {
        if (!mBluetoothHelper.isConnectedBtDevice(device)) return;
        DeviceInfo deviceInfo = mStatusManager.getDeviceInfo(device);
        if (deviceInfo == null) return;
        int connectWay = mBluetoothHelper.getBluetoothOp().isConnectedSppDevice(device) ? BluetoothConstant.PROTOCOL_TYPE_SPP : BluetoothConstant.PROTOCOL_TYPE_BLE;
        String mappedAddress;
        if (connectWay == BluetoothConstant.PROTOCOL_TYPE_SPP) {
            mappedAddress = deviceInfo.getBleAddr();
        } else {
            mappedAddress = deviceInfo.getEdrAddr();
        }
        if (CHexConver.checkBluetoothAddress(mappedAddress)) {
            HistoryRecordDbHelper.updateDeviceInfo(mBluetoothHelper.getBluetoothOp(), device, deviceInfo.getSdkType(), mappedAddress);
        }
        if (deviceInfo.getVid() != 0 || deviceInfo.getPid() != 0) {
            HistoryRecordDbHelper.updateDeviceIDs(mBluetoothHelper.getBluetoothOp(), device, deviceInfo.getVid(), deviceInfo.getVid(), deviceInfo.getPid());
        }
    }

    private ArrayList<FatFile> getWatchList(ArrayList<FatFile> list) {
        if (null == list) return new ArrayList<>();
        ArrayList<FatFile> result = new ArrayList<>();
        for (FatFile fatFile : list) {
            if (fatFile.getName().startsWith("WATCH") || fatFile.getName().startsWith("watch")) {
                result.add(fatFile);
            }
        }
        return result;
    }

    public WatchInfo getWatchInfoByFatFile(FatFile fatFile) {
        if (fatFile == null || watchInfoList.isEmpty()) return null;
        WatchInfo result = null;
        for (WatchInfo watchInfo : watchInfoList) {
            WLog.d(TAG, "-getWatchInfoByList- watchInfo = " + watchInfo + ", target = " + fatFile);
            if (fatFile.getPath() != null && fatFile.getPath().equals(watchInfo.getFatFile().getPath())) {
                result = watchInfo;
                break;
            }
        }
        return result;
    }

    public WatchInfo getWatchInfoByPath(String fatFilePath) {
        if (null == fatFilePath || null == devFatFileList || devFatFileList.isEmpty()) return null;
        FatFile fatFile = null;
        for (FatFile file : devFatFileList) {
            if (fatFilePath.toUpperCase().equals(file.getPath())) {
                fatFile = file;
                break;
            }
        }
        return getWatchInfoByFatFile(fatFile);
    }

    private final BluetoothEventListener mEventListener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            status = AppUtil.convertWatchConnectStatus(status);
            if (status == StateCode.CONNECTION_OK) {
                updateHistoryRecordMsg(device);
                if (null == mTargetDevice) {
                    setTargetDevice(device);
                }
            } else if (BluetoothUtil.deviceEquals(mTargetDevice, device)) {
                notifyBtDeviceConnection(device, status);
                if (status == StateCode.CONNECTION_FAILED || status == StateCode.CONNECTION_DISCONNECT) {
                    setTargetDevice(null);
                }
            }
        }

        @Override
        public void onReceiveData(BluetoothDevice device, byte[] data) {
//            WLog.d(TAG, "-onReceiveData- device = " + AppUtil.printBtDeviceInfo(device));
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
                final DeviceInfo deviceInfo = getDeviceInfo(device);
                if (!deviceInfo.isMandatoryUpgrade() && !deviceInfo.isSupportExternalFlashTransfer()) {
                    JL_Log.w(TAG, "onRcspInit", "设备不支持手表操作");
                    mBluetoothHelper.disconnectDevice(device);
                    return;
                }
                return;
            }
            JL_Log.w(TAG, "onRcspInit", "Failed to init rcsp sdk.");
            mBluetoothHelper.disconnectDevice(device);
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice bluetoothDevice) {
            updateHistoryRecordMsg(bluetoothDevice);
        }

        @Override
        public void onWatchSystemInit(int code) {
            if (code != ErrorCode.ERR_NONE) {
                JL_Log.w(TAG, "onWatchSystemInit", "Failed to init watch system. code : " + CommonUtil.formatInt(code));
                mBluetoothHelper.disconnectDevice(getConnectedDevice());
            }
        }
    };

    private final class CustomListWatchFileListCallback implements OnWatchOpCallback<ArrayList<WatchInfo>> {
        private final OnWatchOpCallback<ArrayList<WatchInfo>> mCallback;

        public CustomListWatchFileListCallback(OnWatchOpCallback<ArrayList<WatchInfo>> callback) {
            mCallback = callback;
        }

        @Override
        public void onSuccess(ArrayList<WatchInfo> result) {
            watchInfoList.clear();
            watchInfoList.addAll(result);
            if (mCallback != null) mCallback.onSuccess(result);
        }

        @Override
        public void onFailed(BaseError error) {
            watchInfoList.clear();
            if (mCallback != null) mCallback.onFailed(error);
        }
    }
}
