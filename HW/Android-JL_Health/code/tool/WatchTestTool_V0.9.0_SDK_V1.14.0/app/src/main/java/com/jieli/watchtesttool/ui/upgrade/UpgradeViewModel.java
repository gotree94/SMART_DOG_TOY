package com.jieli.watchtesttool.ui.upgrade;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.OTAError;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.jieli.jl_bt_ota.util.FileUtil;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_fatfs.utils.ZipUtil;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.interfaces.watch.OnUpdateResourceCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.tool.upgrade.OTAManager;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.upgrade.fileobserver.FileObserverCallback;
import com.jieli.watchtesttool.ui.upgrade.fileobserver.OtaFileObserverHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.ArrayList;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc OTA逻辑实现
 * @since 2021/7/6
 */
@Deprecated
public class UpgradeViewModel extends BluetoothViewModel {
    private final WatchManager mWatchManager = WatchManager.getInstance();
    private final OTAManager mOTAManager;
    private final OtaFileObserverHelper mFileObserverHelper = OtaFileObserverHelper.getInstance();
    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();

    private static final int MSG_REFRESH_FILE = 0x01;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (MSG_REFRESH_FILE == msg.what) {
            readUpgradeFileList();
        }
        return true;
    });

    private String mUpgradeZipPath;

    private boolean isInitOTAOK;     //OTA库是否初始化OK
    private boolean isInitWatchOK;   //手表库是否初始化OK

    private boolean isSingleOTAWay;    //是否单备份OTA
    private boolean isNewReconnectWay; //设备是否处于新回连状态

    @SuppressLint("StaticFieldLeak")
    private final Context mContext;

    public final MutableLiveData<OtaState> mOtaStateMLD = new MutableLiveData<>();
    public final MutableLiveData<ArrayList<File>> mOtaFileListMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mOtaInitMLD = new MutableLiveData<>();

    public UpgradeViewModel(Context context) {
        mContext = context;
        mOTAManager = new OTAManager(mContext);
        setInitWatchOK(mWatchManager.isWatchSystemOk());
        mOtaStateMLD.setValue(new OtaState());

        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
        mOTAManager.registerBluetoothCallback(mBtEventCallback);
        mFileObserverHelper.registerFileObserverCallback(mFileObserverCallback);

        mFileObserverHelper.startObserver();
    }

    public boolean isInitOK() {
        return mOtaInitMLD.getValue() != null && mOtaInitMLD.getValue();
    }

    public boolean isDevOta() {
        return mOTAManager.isOTA();
    }

    public String getOtaFilePath() {
        return mOTAManager.getBluetoothOption().getFirmwareFilePath();
    }

    public void release() {
        mUIHandler.removeCallbacksAndMessages(null);
        mFileObserverHelper.stopObserver();
        mFileObserverHelper.unregisterFileObserverCallback(mFileObserverCallback);
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
        mOTAManager.unregisterBluetoothCallback(mBtEventCallback);
        mOTAManager.release();
    }

    public void readUpgradeFileList() {
        String dirPath = AppUtil.createFilePath(mContext, WatchTestConstant.DIR_UPDATE);
        if (FileUtil.checkFileExist(dirPath)) {
            File file = new File(dirPath);
            ArrayList<File> fileList = new ArrayList<>();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        if (file1.isFile()) {
                            fileList.add(file1);
                        }
                    }
                }
            } else {
                fileList.add(file);
            }
            mOtaFileListMLD.postValue(fileList);
        } else {
            mOtaFileListMLD.postValue(new ArrayList<>());
        }
    }


    public void startOTA(String filePath) {
        if (mOtaInitMLD.getValue() == null || !mOtaInitMLD.getValue()) {
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_PARAMETER, "Please wait for the ota lib to initialize."));
            return;
        }
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo();
        if (deviceInfo == null) {
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED));
            return;
        }
        /*
         * 根据文件后缀判断升级类型
         * 若是ufw文件或者buf文件，则认为是固件升级
         * 若是zip文件,则认为资源更新
         */
        boolean isOtaDev = filePath.endsWith(".ufw") || filePath.endsWith(".buf");
        if (deviceInfo.isMandatoryUpgrade()) {//设备处于强制升级模式
            if (isOtaDev) {
                otaFirmware(filePath);
            } else {
                if (filePath.endsWith(OTAManager.OTA_ZIP_SUFFIX)) {
                    String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
                    try {
                        ZipUtil.unZipFolder(filePath, dirPath);
                        String otaFilePath = AppUtil.obtainUpdateFilePath(dirPath, OTAManager.OTA_FILE_SUFFIX);
                        JL_Log.i(tag, "unZipFolder : " + otaFilePath);
                        File resource = new File(dirPath + "/" + WatchConstant.RES_DIR_NAME);
                        if (resource.exists()) {
                            FileUtil.deleteFile(resource);
                        }
                        if (null != otaFilePath) {
                            otaFirmware(otaFilePath);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_FILE_NOT_FOUND));
            }
            return;
        }
        if (deviceInfo.getExpandMode() == WatchConstant.EXPAND_MODE_RES_OTA) { //设备处于升级资源状态
            if (filePath.endsWith(OTAManager.OTA_ZIP_SUFFIX)) {
                otaResource(filePath);
            } else {
                postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_FILE_NOT_FOUND));
            }
            return;
        }
        if (isOtaDev) {
            otaFirmware(filePath);
        } else if (filePath.endsWith(".zip")) {
            otaResource(filePath);
        } else {
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_PARAMETER, "Ota File is error."));
        }
    }

    /**
     * 固件升级
     *
     * @param filePath 固件升级文件路径
     */
    public void otaFirmware(String filePath) {
        JL_Log.i(tag, "-otaFirmware- filePath = " + filePath);
        mOTAManager.getBluetoothOption().setFirmwareFilePath(filePath);
        mOTAManager.startOTA(new IUpgradeCallback() {
            @Override
            public void onStartOTA() {
                JL_Log.i(tag, "-otaFirmware- onStart >>>>>> ");
                TargetInfoResponse deviceInfo = mOTAManager.getDeviceInfo();
                isSingleOTAWay = !deviceInfo.isSupportDoubleBackup(); //记录是否单备份升级
                OtaState otaState = mOtaStateMLD.getValue();
                if (otaState == null) return;
                otaState.setState(OtaState.OTA_STATE_START)
                        .setOtaType(OtaState.OTA_TYPE_OTA_READY);
                mOtaStateMLD.setValue(otaState);
            }

            @Override
            public void onNeedReconnect(String s, boolean var) {
                JL_Log.i(tag, "-otaFirmware- onNeedReconnect >>>>>> address = " + s + ", isNewWay = " + var);
                //TODO:允许客户自定义回连方式
                isNewReconnectWay = var;
                JL_Log.w(tag, "-otaFirmware- :: onNeedReconnect, isSingleOTAWay = " + isSingleOTAWay
                        + ", isNewReconnectWay = " + isNewReconnectWay + ", " + mConfigHelper.isSPPConnectWay());
                if (isSingleOTAWay && isNewReconnectWay && mConfigHelper.isSPPConnectWay()) { //切换通讯方式
                    mConfigHelper.setTempConnectWay(BluetoothConstant.PROTOCOL_TYPE_SPP); //记录原通讯方式
                    mConfigHelper.setSppConnectWay(!mConfigHelper.isSPPConnectWay());   //设置BLE通讯方式
                }
            }

            @Override
            public void onProgress(int i, float v) {
                JL_Log.i(tag, "-otaFirmware- onProgress >>>>>> type = " + i + ", progress = " + v);
                if (v > 0) {
                    OtaState otaState = mOtaStateMLD.getValue();
                    if (otaState == null) return;
                    otaState.setState(OtaState.OTA_STATE_WORKING)
                            .setOtaType(i == 0 ? OtaState.OTA_TYPE_OTA_READY : OtaState.OTA_TYPE_OTA_UPGRADE_FIRMWARE)
                            .setOtaProgress(v);
                    mOtaStateMLD.setValue(otaState);
                }
            }

            @Override
            public void onStopOTA() {
                JL_Log.e(tag, "-otaFirmware- :: onStopOTA, " + isSingleOTAWay);
                if (isNewReconnectWay) isNewReconnectWay = false;
                OtaState otaState = mOtaStateMLD.getValue();
                if (otaState == null) return;
                otaState.setState(OtaState.OTA_STATE_STOP)
                        .setStopResult(OtaState.OTA_RES_SUCCESS)
                        .setOtaProgress(0f);
                mOtaStateMLD.setValue(otaState);
                if (mUpgradeZipPath != null) {
                    FileUtil.deleteFile(new File(filePath));
                    mUpgradeZipPath = null;
                }
                if (isSingleOTAWay && mConfigHelper.getTempConnectWay() != -1) { //单备份升级，临时连接方式有值
                    //进行还原操作
                    mConfigHelper.setSppConnectWay(mConfigHelper.getTempConnectWay() == BluetoothConstant.PROTOCOL_TYPE_SPP);
                    //清除临时连接方式
                    mConfigHelper.setTempConnectWay(-1);
                }
            }

            @Override
            public void onCancelOTA() {
                OtaState otaState = mOtaStateMLD.getValue();
                if (otaState == null) return;
                otaState.setState(OtaState.OTA_STATE_STOP)
                        .setStopResult(OtaState.OTA_RES_CANCEL)
                        .setOtaProgress(0f);
                mOtaStateMLD.setValue(otaState);
                if (mUpgradeZipPath != null) {
                    FileUtil.deleteFile(new File(filePath));
                    mUpgradeZipPath = null;
                }
            }

            @Override
            public void onError(BaseError baseError) {
                JL_Log.e(tag, "-otaFirmware- :: onError, baseError = " + baseError);
                postOtaFailed(OtaState.OTA_TYPE_OTA_UPGRADE_FIRMWARE, baseError);
                if (mUpgradeZipPath != null) {
                    mUpgradeZipPath = null;
                }
            }
        });
    }

    /**
     * 资源差分升级
     *
     * @param filePath 压缩包路径
     */
    public void otaResource(final String filePath) {
        mWatchManager.updateWatchResource(filePath, new OnUpdateResourceCallback() {
            @Override
            public void onStart(String filePath, int total) {
                JL_Log.i(tag, "-otaResource- onStart >>>>>> filePath = " + filePath + ", total = " + total);
                OtaState otaState = mOtaStateMLD.getValue();
                if (otaState == null) return;
                otaState.setState(OtaState.OTA_STATE_START)
                        .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                        .setOtaTotal(total);
                mOtaStateMLD.setValue(otaState);
            }

            @Override
            public void onProgress(int index, String filePath, float progress) {
                if (progress > 0) {
                    OtaState otaState = mOtaStateMLD.getValue();
                    if (otaState == null) return;
                    otaState.setState(OtaState.OTA_STATE_WORKING)
                            .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                            .setOtaIndex(index + 1)
                            .setOtaFileInfo(AppUtil.getFileNameByPath(filePath))
                            .setOtaProgress(progress);
                    mOtaStateMLD.setValue(otaState);
                }
            }

            @Override
            public void onStop(String otaFilePath) {
                JL_Log.i(tag, "-otaResource- onStop >>>>>> otaFilePath = " + otaFilePath);
                if (otaFilePath == null) {
                    OtaState otaState = mOtaStateMLD.getValue();
                    if (otaState == null) return;
                    otaState.setState(OtaState.OTA_STATE_STOP)
                            .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                            .setStopResult(OtaState.OTA_RES_SUCCESS);
                    mOtaStateMLD.setValue(otaState);
                } else {
                    mUpgradeZipPath = filePath;
                    otaFirmware(otaFilePath);
                }
            }

            @Override
            public void onError(int code, String message) {
                postOtaFailed(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE, OTAError.buildError(code, message));
            }
        });
    }

    private void postOtaFailed(BaseError error) {
        postOtaFailed(OtaState.OTA_TYPE_OTA_READY, error);
    }

    private void postOtaFailed(int otaType, BaseError error) {
        OtaState otaState = mOtaStateMLD.getValue();
        if (otaState == null) return;
        otaState.setState(OtaState.OTA_STATE_STOP)
                .setOtaType(otaType)
                .setStopResult(OtaState.OTA_RES_FAILED)
                .setOtaProgress(0f)
                .setError(error);
        mOtaStateMLD.setValue(otaState);
    }

    private void setInitOTAOK(boolean isInit) {
        if (isInitOTAOK != isInit) {
            isInitOTAOK = isInit;
            checkInitValue();
        }
    }

    private void setInitWatchOK(boolean isInit) {
        if (isInitWatchOK != isInit) {
            isInitWatchOK = isInit;
            checkInitValue();
        }
    }

    private void checkInitValue() {
        boolean isInitOTA = isInitOTAOK && isInitWatchOK;
        mOtaInitMLD.postValue(isInitOTA);
        if (isInitOTA) {  //OTA库准备完成
            //准备完毕，可以升级
            OtaState otaState = mOtaStateMLD.getValue();
            if (otaState == null || otaState.getState() == OtaState.OTA_STATE_PREPARE) return;
            otaState.setState(OtaState.OTA_STATE_PREPARE);
            mOtaStateMLD.setValue(otaState);
        }
    }

    private final BtEventCallback mBtEventCallback = new BtEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            JL_Log.e(tag, "-onConnection- " + device + "\tstatus = " + status);
            if (status == StateCode.CONNECTION_OK) {
                setInitOTAOK(true);
            } else if (status == StateCode.CONNECTION_DISCONNECT && !isDevOta()) {
                setInitWatchOK(false);
                setInitOTAOK(false);
//                postOtaFailed(new BaseError(ErrorCode.SUB_ERR_BLE_NOT_CONNECTED, "Device disconnected."));
            }
        }
    };

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {
        @Override
        public void onWatchSystemInit(int code) {
            setInitWatchOK(code == 0);
        }

        @Override
        public void onResourceUpdateUnfinished(BluetoothDevice device) {
            setInitWatchOK(true);
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            setInitWatchOK(true);
        }
    };

    private final FileObserverCallback mFileObserverCallback = (event, path) -> {
        if (event > 2) {
            mUIHandler.removeMessages(MSG_REFRESH_FILE);
            mUIHandler.sendEmptyMessageDelayed(MSG_REFRESH_FILE, 500);
        }
    };

    public static class UpgradeViewModelFactory implements ViewModelProvider.Factory {
        private final Context mContext;

        public UpgradeViewModelFactory(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) (new UpgradeViewModel(mContext));
        }
    }
}
