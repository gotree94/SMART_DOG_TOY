package com.jieli.healthaide.ui.device.upgrade;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.GsonBuilder;
import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.config.ConfigHelper;
import com.jieli.healthaide.tool.upgrade.OTAManager;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.OTAError;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.util.FileUtil;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.utils.ZipUtil;
import com.jieli.jl_health_http.model.OtaFileMsg;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.impl.NetworkOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.network.OnNetworkListener;
import com.jieli.jl_rcsp.interfaces.network.OnNetworkOTACallback;
import com.jieli.jl_rcsp.interfaces.watch.OnUpdateResourceCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.CmdError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkOTAState;
import com.jieli.jl_rcsp.model.network.OTAParam;
import com.jieli.jl_rcsp.tool.datahandles.ParseHelper;
import com.jieli.jl_rcsp.util.RcspUtil;
import com.jieli.jl_rcsp.util.WatchFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpgradeViewModel extends WatchViewModel {

    private static final long RECONNECT_DELAY = 3000;

    private static final String KEY_DEVICE_ADDRESS = "address";
    private static final String KEY_OTA_FILE_PATH = "ota_file_path";

    /**
     * 回连设备的消息
     */
    private static final int MSG_RECONNECT_DEVICE = 0xF001;
    /**
     * 更新资源
     */
    private static final int MSG_UPDATE_RESOURCE = 0xF002;

    /**
     * 固件OTA实现
     */
    private final OTAManager mOTAManager;
    /**
     * 4G模块OTA实现
     */
    private final NetworkOpImpl mNetworkOp;

    public final MutableLiveData<OtaState> mOtaStateMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mOtaInitMLD = new MutableLiveData<>();
    public final MutableLiveData<NetworkInfo> mNetworkInfoMLD = new MutableLiveData<>();

    /**
     * OTA文件存放目录路径
     */
    private final String otaDirPath;
    /**
     * 网络模块OTA文件存放目录路径
     */
    private final String otaNetworkDirPath;

    /**
     * OTA状态
     */
    private final OtaState mOtaState;
    /**
     * OTA标志
     */
    private int mOtaFlag;
    /**
     * 解压文件路径
     */
    private String mUpgradeZipPath;

    /**
     * OTA库是否初始化OK
     */
    private boolean isInitOTAOK;
    /**
     * 手表库是否初始化OK
     */
    private boolean isInitWatchOK;

    /**
     * 是否跳过4G网络升级
     */
    private boolean isSkip4gOta;
    /**
     * 是否正在请求网络信息
     */
    private boolean isRequestNetworkInfo;
    /**
     * 回连设备参数
     */
    private ReconnectDeviceParam reconnectParam;

    /**
     * 是否开启本地OTA测试功能
     * Description 默认是关闭，开启后，需要在/Android/data/com.jieli.healthaide/files/upgrade/放入升级文件
     * 升级文件默认为xxx.zip的压缩包。
     */
//    public static boolean IS_LOCAL_OTA_TEST = false;  //已用isSupportOnLineOTA()替代

    /**
     * UI处理
     */
    private final Handler uiHandler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case MSG_RECONNECT_DEVICE: {
                Bundle bundle = msg.getData();
                if (null == bundle) return false;
                String address = bundle.getString(KEY_DEVICE_ADDRESS);
                String filePath = bundle.getString(KEY_OTA_FILE_PATH);
                reconnectDevice(address, filePath);
                break;
            }
            case MSG_UPDATE_RESOURCE: {
                if (!(msg.obj instanceof String)) return false;
                String filePath = (String) msg.obj;
                otaResource(filePath);
                break;
            }
        }
        return true;
    });

    public UpgradeViewModel() {
        final Context context = HealthApplication.getAppViewModel().getApplication();
        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
        mOTAManager = new OTAManager(context);
        mOTAManager.registerBluetoothCallback(mBtEventCallback);
        mNetworkOp = NetworkOpImpl.instance(mWatchManager);
        mNetworkOp.addOnNetworkListener(mOnNetworkListener);

        otaDirPath = HealthUtil.createFilePath(context, HealthConstant.DIR_UPDATE);
        otaNetworkDirPath = HealthUtil.createFilePath(context, HealthConstant.DIR_UPDATE, HealthConstant.DIR_NETWORK);
        String otaFilePath = HealthUtil.obtainUpdateFilePath(otaDirPath, OTAManager.OTA_ZIP_SUFFIX);
        if (null == otaFilePath) {
            otaFilePath = mOTAManager.getBluetoothOption().getFirmwareFilePath();
        }
        mOtaState = new OtaState().setState(OtaState.OTA_STATE_IDLE).setOtaFilePath(otaFilePath);
        final DeviceInfo deviceInfo = getDeviceInfo();
        boolean isInitOk = null != deviceInfo && (mWatchManager.isWatchSystemOk() || deviceInfo.isMandatoryUpgrade());
        setInitWatchOK(isInitOk);
    }

    public void release() {
        uiHandler.removeCallbacksAndMessages(null);
        setOtaFlag(UpgradeFragment.OTA_FLAG_NORMAL);
        setSkip4gOta(false);
        mWatchManager.setFirmwareOTA(false);
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
        mOTAManager.unregisterBluetoothCallback(mBtEventCallback);
        mOTAManager.release();
        mNetworkOp.removeOnNetworkListener(mOnNetworkListener);
        mNetworkOp.destroy();
    }

    public OtaState getOtaState() {
        return mOtaState;
    }

    public int getOtaFlag() {
        return mOtaFlag;
    }

    public void setOtaFlag(int otaFlag) {
        mOtaFlag = otaFlag;
    }

    public DeviceInfo getDeviceInfo() {
        return mWatchManager.getDeviceInfo(getConnectedDevice());
    }

    public boolean isDevOta() {
        return mOTAManager.isOTA();
    }

    public boolean isSupportOnLineOTA() {
        if (ConfigHelper.getInstance().isEnableLocalOTATest()) return false;
        return WatchServerCacheHelper.getInstance().isSupportOnlineOTA(mWatchManager);
    }

    public void setSkip4gOta(boolean isSkip4gOta) {
        this.isSkip4gOta = isSkip4gOta;
    }

    public void otaPrepare() {
        if (!isUsingDevice(getConnectedDevice())) {
            JL_Log.w(tag, "otaPrepare", "Device is disconnected.");
            return;
        }
        if (isDevOta()) {
            JL_Log.w(tag, "otaPrepare", "OTA is in progress.");
            return;
        }
        boolean isSupportOnLineOTA = isSupportOnLineOTA();
        final int state = mOtaState.getState();
        JL_Log.d(tag, "otaPrepare", "state = " + state + ", isSupportOnLineOTA = " + isSupportOnLineOTA
                + ", isSkip4gOta = " + isSkip4gOta);
        switch (state) {
            case OtaState.OTA_STATE_IDLE:
            case OtaState.OTA_STATE_STOP:
                queryNewVersionMessage(isSupportOnLineOTA, isSkip4gOta);
                break;
            case OtaState.OTA_STATE_PREPARE:
                downloadOtaFile();
                break;
            case OtaState.OTA_STATE_UPGRADE:
                startOTA();
                break;
        }
    }

    public void resetOtaState() {
        setSkip4gOta(false);
        mOtaStateMLD.setValue(mOtaState.setState(OtaState.OTA_STATE_IDLE).setStopResult(0));
    }

    /* public void cancelOTA() {
        if (isDevOta()) {
            mOTAManager.cancelOTA();
        }
    }*/

    public boolean checkNeedDisconnect(int result) {
        return result > 1024 && result != ErrorCode.SUB_ERR_OTA_IN_HANDLE && result != ErrorCode.SUB_ERR_FILE_NOT_FOUND;
    }

    private void setUpgradeUnZipPath(String unZipPath) {
        mUpgradeZipPath = unZipPath;
    }

    private String getUnzipPath(String filePath) {
        if (null == filePath || filePath.isEmpty()) return filePath;
        boolean isZipFile = filePath.endsWith(OTAManager.OTA_ZIP_SUFFIX) || filePath.endsWith(OTAManager.OTA_ZIP_SUFFIX.toUpperCase());
        if (!isZipFile) return filePath;
        String dirPath = WatchFileUtil.getDirPath(filePath);
        String fileName = WatchFileUtil.getFileName(filePath);
        String unzipName = WatchConstant.UNZIP_PREFIX + WatchFileUtil.getNameNoSuffix(fileName);
        return dirPath + File.separator + unzipName;
    }

    private String getFirmwareFilePathByZip(String zipPath) throws Exception {
        String dirPath = WatchFileUtil.getDirPath(zipPath);
        String fileName = WatchFileUtil.getFileName(zipPath);
        String unzipName = WatchConstant.UNZIP_PREFIX + WatchFileUtil.getNameNoSuffix(fileName);
        ZipUtil.unZipFolder(zipPath, dirPath, unzipName);
        setUpgradeUnZipPath(dirPath + File.separator + unzipName);
        String otaFilePath = HealthUtil.obtainUpdateFilePath(mUpgradeZipPath, OTAManager.OTA_FILE_SUFFIX);
        JL_Log.i(tag, "getResourcePathByZip", "get firmware file. unZipFolder : " + mUpgradeZipPath + ",\notaFilePath : " + otaFilePath);
        //存在资源文件夹，不需要，删除
        FileUtil.deleteFile(new File(dirPath, WatchConstant.RES_DIR_NAME));
        return otaFilePath;
    }

    /**
     * 查询新版本的信息
     */
    private void queryNewVersionMessage(boolean isSupportOnLineOTA, boolean isSkipNetwork) {
        if (!isSupportOnLineOTA) { //不支持线上升级功能
            checkLocalOtaFile(isSkipNetwork);
            return;
        }
        if (!isSkipNetwork) {
            WatchConfigure configure = mWatchManager.getWatchConfigure(getConnectedDevice());
            if (null != configure && configure.getFunctionOption().isSupportNetworkModule()) {
                tryToQuery4gOtaMsgForServer();
                return;
            }
        }
        queryOtaMsgForServer();
    }

    /**
     * 下载OTA文件
     */
    private void downloadOtaFile() {
        boolean isSupportOnLineOTA = isSupportOnLineOTA();
        if (!isSupportOnLineOTA) {
            /*if (!BuildConfig.DEBUG) { //Release版本不支持本地OTA
                onOtaFailed(ErrorCode.SUB_ERR_OTA_FAILED, "Firmware upgrade function is not supported.");
                return;
            }*/
            mOtaStateMLD.setValue(mOtaState.setState(OtaState.OTA_STATE_UPGRADE)
                    .setOtaFilePath(mOtaState.getMessage().getUrl()));

            otaPrepare();
        } else {
            String fileName = WatchFileUtil.getFileName(mOtaState.getMessage().getUrl());
            Integer id4G = mOtaState.getMessage().getId4g();
            String otaFilePath = ((id4G != null && id4G > 0) ? otaNetworkDirPath : otaDirPath) + File.separator + fileName;
            downloadFile(mOtaState.getMessage().getUrl(), otaFilePath);
        }
    }

    /**
     * 开始OTA
     */
    private void startOTA() {
        final String otaFilePath = mOtaState.getOtaFilePath();
        if (TextUtils.isEmpty(otaFilePath)) {
            onOtaFailed(ErrorCode.SUB_ERR_PARAMETER, "File path is invalid.");
            return;
        }
        if (isNetworkOtaFilePath(otaFilePath)) {
            startNetworkOta(otaFilePath);
            return;
        }
        startFirmwareOTA(otaFilePath);
    }

    private boolean isNetworkOtaFilePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;
        return filePath.startsWith(otaNetworkDirPath);
    }

    private void startFirmwareOTA(@NonNull String filePath) {
        if (isDevOta()) {
            JL_Log.w(tag, "OTA is in progress.");
            return;
        }
        final DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            onOtaFailed(ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED, "Device not Connected.");
            return;
        }
        boolean isOTAFile = filePath.endsWith(".ufw") || filePath.endsWith(".buf");
        boolean isZipFile = filePath.endsWith(OTAManager.OTA_ZIP_SUFFIX) || filePath.endsWith(OTAManager.OTA_ZIP_SUFFIX.toUpperCase());
        if (deviceInfo.isMandatoryUpgrade()) { //设备处于强制升级状态
            if (isOTAFile) {
                otaFirmware(filePath);
                return;
            }
            if (isZipFile) {
                try {
                    String otaFilePath = getFirmwareFilePathByZip(filePath);
                    if (null != otaFilePath) {
                        otaFirmware(otaFilePath);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "Not found ota file.");
            return;
        }
        boolean isResourceFile = isZipFile || filePath.endsWith(WatchConstant.RES_DIR_NAME);
        final int expandMode = deviceInfo.getExpandMode();
        switch (expandMode) {
            case WatchConstant.EXPAND_MODE_RES_OTA: {
                JL_Log.d(tag, "startFirmwareOTA", "设备处于更新资源状态");
                if (!isOTAFile && !isResourceFile) {
                    onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "Not found resource file.");
                    return;
                }
                break;
            }
            case WatchConstant.EXPAND_MODE_ONLY_UPDATE_RESOURCE: {
                JL_Log.d(tag, "startFirmwareOTA", "设备处于仅更新资源状态");
                if (!isResourceFile) {
                    onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "Not found resource file.");
                    return;
                }
                break;
            }
            case WatchConstant.EXPAND_MODE_ONLY_OTA: {
                JL_Log.d(tag, "startFirmwareOTA", "设备处于仅升级固件状态");
                if (!isOTAFile) {
                    if (isZipFile) {
                        try {
                            String otaFilePath = getFirmwareFilePathByZip(filePath);
                            if (null != otaFilePath) {
                                otaFirmware(otaFilePath);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "Not found ota file.");
                    return;
                }
                break;
            }
        }
        if (isOTAFile) {
            otaFirmware(filePath);
        } else {
            otaResource(filePath);
        }
    }

    private void otaFirmware(String filePath) {
        JL_Log.d(tag, "otaFirmware", "filePath : " + filePath);
        mOTAManager.getBluetoothOption().setFirmwareFilePath(filePath);
        mOTAManager.startOTA(new IUpgradeCallback() {
            @Override
            public void onStartOTA() {
                JL_Log.i(tag, "otaFirmware", "onStartOTA");
                mWatchManager.setFirmwareOTA(true);
                mOtaStateMLD.setValue(mOtaState.setState(OtaState.OTA_STATE_START)
                        .setOtaType(OtaState.OTA_TYPE_OTA_READY));
            }

            @Override
            public void onNeedReconnect(String s, boolean isNewAdv) {
                //TODO:允许客户自定义回连方式
            }

            @Override
            public void onProgress(int type, float progress) {
                JL_Log.d(tag, "otaFirmware", "onProgress ---> type = " + type + ", progress = " + progress);
                if (progress <= 0) return;
                mOtaStateMLD.setValue(mOtaState.setState(OtaState.OTA_STATE_WORKING)
                        .setOtaType(type == 0 ? OtaState.OTA_TYPE_OTA_READY : OtaState.OTA_TYPE_OTA_UPGRADE_FIRMWARE)
                        .setOtaProgress(progress));
            }

            @Override
            public void onStopOTA() {
                JL_Log.i(tag, "otaFirmware", "onStopOTA");
                onOtaStop();
            }

            @Override
            public void onCancelOTA() {
                JL_Log.i(tag, "otaFirmware", "onCancelOTA");
                onOtaCancel();
            }

            @Override
            public void onError(BaseError error) {
                if (null == error) return;
                JL_Log.e(tag, "otaFirmware", "onError ---> " + error);
                if (error.getSubCode() == ErrorCode.SUB_ERR_NEED_UPDATE_RESOURCE) { //需要回连更新资源
                    releaseResource();
                    uiHandler.removeMessages(MSG_RECONNECT_DEVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_DEVICE_ADDRESS, error.getMessage());
                    bundle.putString(KEY_OTA_FILE_PATH, mOtaState.getOtaFilePath());
                    Message message = uiHandler.obtainMessage(MSG_RECONNECT_DEVICE);
                    message.setData(bundle);
                    uiHandler.sendMessageDelayed(message, RECONNECT_DELAY); //延时3秒，等待设备重启
                    return;
                }
                onOtaFailed(error.getSubCode(), error.getMessage());
            }
        });
    }

    private void otaResource(String filePath) {
        setUpgradeUnZipPath(getUnzipPath(filePath));
        JL_Log.d(tag, "otaResource", "filePath : " + filePath);
        mWatchManager.updateWatchResource(filePath, new OnUpdateResourceCallback() {
            @Override
            public void onStart(String filePath, int total) {
                JL_Log.i(tag, "otaResource", "onStart ---> filePath = " + filePath + ", total = " + total);
                mOtaState.setState(OtaState.OTA_STATE_START)
                        .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                        .setOtaTotal(total);
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onProgress(int index, String filePath, float progress) {
                JL_Log.d(tag, "otaResource", "onProgress ---> index : " + index + ", progress : " + progress
                        + ",\n filePath = " + filePath);
                if (progress <= 0) return;
                mOtaState.setState(OtaState.OTA_STATE_WORKING)
                        .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                        .setOtaIndex(index + 1)
                        .setOtaFileInfo(HealthUtil.getFileNameByPath(filePath))
                        .setOtaProgress(progress);
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onStop(String otaFilePath) {
                JL_Log.i(tag, "otaResource", "onStop ---> otaFilePath = " + otaFilePath);
                if (otaFilePath == null) {
                    onOtaStop();
                } else {
                    otaFirmware(otaFilePath);
                }
            }

            @Override
            public void onError(int code, String message) {
                JL_Log.w(tag, "otaResource", RcspUtil.formatString("onError ---> code : %s, %s", RcspUtil.formatInt(code), message));
                if (code == FatFsErrCode.RES_ERR_SPACE_TO_UPDATE) {
                    message = HealthApplication.getAppViewModel().getApplication().getString(R.string.ota_err_no_space);
                } else if (code == FatFsErrCode.RES_REMOTE_NOT_CONNECT) {
                    message = HealthApplication.getAppViewModel().getApplication().getString(R.string.ota_err_device_not_connect);
                } else if (code == WatchError.ERR_RESPONSE_BAD_RESULT) {
                    CmdError error = CmdError.parseCmdError(message);
                    if (error != null && error.getCmdId() == Command.CMD_EXTERNAL_FLASH_IO_CTRL && error.getSubCode() == 1) {
                        code = ErrorCode.SUB_ERR_DEVICE_LOW_VOLTAGE;
                    }
                }
                onOtaFailed(code, message);
            }
        });
    }

    private String getLocalOTAPath(boolean isSkipNetwork) {
        if (!isLocalOTATest()) return null;
        String filePath = null;
        if (!isSkipNetwork) {
            filePath = HealthUtil.obtainUpdateFilePath(otaNetworkDirPath, OTAManager.OTA_ZIP_SUFFIX);
        }
        if (null == filePath) {
            filePath = HealthUtil.obtainUpdateFilePath(otaDirPath, OTAManager.OTA_ZIP_SUFFIX);
        }
        if (null == filePath) {
            filePath = HealthUtil.obtainUpdateFilePath(otaDirPath, OTAManager.OTA_FILE_SUFFIX);
        }
        if (null == filePath) {
            filePath = HealthUtil.obtainUpdateFilePath(otaDirPath, ".bfu");
        }
        return filePath;
    }

    private void queryOtaMsgForServer() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (null == deviceInfo) {
            onOtaFailed(ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED, "Device not Connected.");
            return;
        }
        int pid = deviceInfo.getPid();
        int uid = deviceInfo.getUid();
        mWatchServerCacheHelper.queryOtaMsg(pid, uid, new WatchServerCacheHelper.IWatchHttpCallback<OtaFileMsg>() {
            @Override
            public void onSuccess(OtaFileMsg result) {
                if (judgeDeviceNeedToOta(result)) {
                    mOtaState.setState(OtaState.OTA_STATE_PREPARE)
                            .setMessage(result);
                    if (getOtaFlag() != UpgradeFragment.OTA_FLAG_NORMAL) {
                        otaPrepare();
                    }
                } else {
                    mOtaState.setState(OtaState.OTA_STATE_IDLE)
                            .setMessage(result)
                            .setStopResult(OtaState.OTA_RES_SUCCESS);
                }
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onFailed(int code, String message) {
                onOtaFailed(code, message);
            }
        });
    }

    private void tryToQuery4gOtaMsgForServer() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (null == deviceInfo) {
            onOtaFailed(ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED, "Device not Connected.");
            return;
        }
        NetworkInfo networkInfo = deviceInfo.getNetworkInfo();
        if (null == networkInfo) {
            queryNetworkInfo();
            return;
        }
        JL_Log.d(tag, "tryToQuery4gOtaMsgForServer", "" + networkInfo);
        setSkip4gOta(true);
        mWatchServerCacheHelper.query4gOtaMessage(deviceInfo.getPid(), deviceInfo.getUid(), networkInfo.getVid(),
                new WatchServerCacheHelper.IWatchHttpCallback<OtaFileMsg>() {
                    @Override
                    public void onSuccess(OtaFileMsg result) {
                        long serverVersion = convertVersionCode(result.getId4g(), result.getVersion());
                        long networkVersion = convertVersionCode(networkInfo.getVid(), networkInfo.getVersion());
                        JL_Log.d(tag, "tryToQuery4gOtaMsgForServer", "serverVersion = " + serverVersion +
                                ", networkVersion = " + networkVersion);
                        if (networkVersion >= 0 && serverVersion > networkVersion) { //需要更新
                            mOtaState.setState(OtaState.OTA_STATE_PREPARE)
                                    .setMessage(result);
                            mOtaStateMLD.setValue(mOtaState);
                            if (getOtaFlag() == UpgradeFragment.OTA_FLAG_NETWORK) {
                                otaPrepare();
                            }
                        } else {
                            otaPrepare();
                        }
                    }

                    @Override
                    public void onFailed(int code, String message) {
                        otaPrepare();
                    }
                });
    }

    private void queryNetworkInfo() {
        if (isRequestNetworkInfo) return;
        isRequestNetworkInfo = true;
        mNetworkOp.queryNetworkInfo(getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(com.jieli.jl_rcsp.model.base.BaseError error) {
                isRequestNetworkInfo = false;
                setSkip4gOta(true);
                otaPrepare();
            }
        });
    }

    private void downloadFile(String url, String outPath) {
        JL_Log.d(tag, "downloadFile", "url : " + url + ", \noutPath : " + outPath);
        mWatchServerCacheHelper.downloadFile(url, outPath, new WatchServerCacheHelper.OnDownloadListener() {
            @Override
            public void onStart() {
                JL_Log.i(tag, "downloadFile", "onStart ---> ");
                mOtaState.setState(OtaState.OTA_STATE_DOWNLOAD)
                        .setOtaProgress(0);
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onProgress(int progress) {
                JL_Log.d(tag, "downloadFile", "onProgress ---> " + progress);
                mOtaState.setState(OtaState.OTA_STATE_DOWNLOAD)
                        .setOtaProgress(progress);
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onSuccess(String result) {
                JL_Log.i(tag, "downloadFile", "onSuccess ---> " + result);
                mOtaState.setState(OtaState.OTA_STATE_UPGRADE)
                        .setOtaFilePath(result);
                mOtaStateMLD.setValue(mOtaState);

                otaPrepare();
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.w(tag, "downloadFile", "onFailed ---> code : " + code + ", " + message);
                onOtaFailed(code, message);
            }
        });
    }

    private void releaseResource() {
        mWatchManager.setFirmwareOTA(false);
        final String otaFilePath = mOtaState.getOtaFilePath();
        final String unzipPath = mUpgradeZipPath;
        boolean isLocalOTATest = isLocalOTATest();
        JL_Log.d(tag, "releaseResource", "isLocalOTATest : " + isLocalOTATest + ", unzipPath ： " + unzipPath
                + ",\n ota file Path : " + mOtaState.getOtaFilePath());
        if (unzipPath != null) {
            FileUtil.deleteFile(new File(unzipPath));
            setUpgradeUnZipPath(null);
        }
        if (otaFilePath != null) {
            //非本地测试时才删除文件
            if (!isLocalOTATest) {
                FileUtil.deleteFile(new File(otaFilePath));
            }
        }
        if (mOtaState.getOtaType() != OtaState.OTA_TYPE_NETWORK_MODULE) {
            setSkip4gOta(false);
        }
    }

    private void onOtaCancel() {
        if (mOtaState.getState() == OtaState.OTA_STATE_STOP) return;
        JL_Log.i(tag, "onOtaCancel", "");
        releaseResource();
        mOtaState.setState(OtaState.OTA_STATE_STOP)
                .setStopResult(OtaState.OTA_RES_CANCEL)
                .setOtaProgress(0f);
        mOtaStateMLD.setValue(mOtaState);
    }

    private void onOtaStop() {
        if (mOtaState.getState() == OtaState.OTA_STATE_STOP) return;
        JL_Log.i(tag, "onOtaStop", "Upgrade is finish.");
        releaseResource();
        mOtaStateMLD.setValue(mOtaState.setState(OtaState.OTA_STATE_STOP)
                .setStopResult(OtaState.OTA_RES_SUCCESS)
                .setOtaProgress(0f));
    }

    private void onOtaFailed(int code, String msg) {
        if (mOtaState.getState() == OtaState.OTA_STATE_STOP) return;
        JL_Log.e(tag, "onOtaFailed", RcspUtil.formatString("code : %s, %s ", RcspUtil.formatInt(code), msg));
        releaseResource();
        mOtaStateMLD.setValue(mOtaState.setState(OtaState.OTA_STATE_STOP)
                .setStopResult(OtaState.OTA_RES_FAILED)
                .setError(new BaseError(code, msg))
                .setOtaProgress(0f));
    }

    private boolean judgeDeviceNeedToOta(OtaFileMsg message) {
        if (message == null) return false;
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            onOtaFailed(ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED, "device is not connected.");
            return false;
        }
        int versionCode = deviceInfo.getVersionCode();
        int serverFirmware = ParseHelper.convertVersionByString(message.getVersion());
        JL_Log.i(tag, "judgeDeviceNeedToOta", "versionCode : " + versionCode + ", sever firmware version : " + serverFirmware);
        return versionCode < serverFirmware || (serverFirmware == 0);
    }

    private void checkLocalOtaFile(boolean isSkipNetwork) {
        /*if (!BuildConfig.DEBUG) { //Release版本不支持本地OTA
            onOtaFailed(ErrorCode.SUB_ERR_OTA_FAILED, "Firmware upgrade function is not supported.");
            return;
        }*/
        String otaFilePath = getLocalOTAPath(isSkipNetwork);
        if (null != otaFilePath) {
            OtaFileMsg otaFileMsg = new OtaFileMsg();
            otaFileMsg.setContent("本地文件测试");
            otaFileMsg.setExplain("本地文件测试");
            otaFileMsg.setUrl(otaFilePath);
            otaFileMsg.setVersion("V_0.0.0.0");
            mOtaState.setState(OtaState.OTA_STATE_PREPARE)
                    .setMessage(otaFileMsg);
            mOtaStateMLD.setValue(mOtaState);
        } else {
            onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "ota file not found.");
        }
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
        mOtaInitMLD.postValue(isInitOTAOK && isInitWatchOK);
    }

    private boolean isLocalOTATest() {
        return /*BuildConfig.DEBUG &&*/ !isSupportOnLineOTA();
    }

    private long convertVersionCode(int vid, String version) {
        if (null == version || version.isEmpty()) return -1;
        Pattern pattern = Pattern.compile("\\d+"); // 定义正则表达式模式，匹配连续的数字
        Matcher matcher = pattern.matcher(version);
        StringBuilder value = new StringBuilder();
        while (matcher.find()) {
            value.append(matcher.group());
        }
        String result = value.toString();
        if (TextUtils.isDigitsOnly(result)) {
            try {
                return Long.parseLong(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private String readJsonByPath(@NonNull File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int size;
            while ((size = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, size);
            }
            String content = outputStream.toString();
            inputStream.close();
            outputStream.close();
            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startNetworkOta(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "Ota File not found." + filePath);
            return;
        }
        String otaFilePath = null;
        String fileName = file.getName();
        boolean isZipFile = fileName.endsWith(OTAManager.OTA_ZIP_SUFFIX) || fileName.endsWith(OTAManager.OTA_ZIP_SUFFIX.toUpperCase());
        if (isZipFile) {
            NetworkInfo networkInfo = getDeviceInfo().getNetworkInfo();
            try {
                String unzipName = WatchConstant.UNZIP_PREFIX + WatchFileUtil.getNameNoSuffix(fileName);
                ZipUtil.unZipFolder(filePath, otaNetworkDirPath, unzipName);
                setUpgradeUnZipPath(otaNetworkDirPath + File.separator + unzipName);
                String jsonFilePath = HealthUtil.obtainUpdateFilePath(mUpgradeZipPath, ".json");
                File json = new File(jsonFilePath);
                JL_Log.d(tag, "startNetworkOta", "jsonFilePath = " + jsonFilePath);
                if (json.exists() && json.isFile()) {
                    String content = readJsonByPath(json);
                    SdkMapInfo sdkMapInfo = new GsonBuilder().create().fromJson(content, SdkMapInfo.class);
                    JL_Log.d(tag, "startNetworkOta", "" + sdkMapInfo);
                    final List<SdkMapInfo.MapDTO> mapList = null == sdkMapInfo ? null : sdkMapInfo.getMap();
                    if (null != mapList) {
                        for (SdkMapInfo.MapDTO map : mapList) {
                            long mapVersion = convertVersionCode(networkInfo.getVid(), map.getVersion());
                            long firmwareVersion = convertVersionCode(networkInfo.getVid(), networkInfo.getVersion());
                            if (firmwareVersion >= 0 && firmwareVersion == mapVersion) {
                                otaFilePath = HealthUtil.obtainUpdateFilePath(mUpgradeZipPath, map.getPakage());
                                break;
                            }
                        }
                    }
                }
                if (!isLocalOTATest()) { //不是本地测试
                    FileUtil.deleteFile(new File(filePath));
                }
                JL_Log.d(tag, "startNetworkOta", "otaFilePath = " + otaFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            otaFilePath = file.getPath();
        }
        if (TextUtils.isEmpty(otaFilePath)) {
            onOtaFailed(ErrorCode.SUB_ERR_FILE_NOT_FOUND, "Ota File not found." + filePath);
            return;
        }
        setSkip4gOta(true);
        mNetworkOp.startNetworkOTA(getConnectedDevice(), new OTAParam(otaFilePath), new OnNetworkOTACallback() {
            @Override
            public void onStart() {
                JL_Log.i(tag, "startNetworkOta", "onStart ---> ");
                mOtaState.setState(OtaState.OTA_STATE_START)
                        .setOtaType(OtaState.OTA_TYPE_NETWORK_MODULE);
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onProgress(int progress) {
                JL_Log.d(tag, "startNetworkOta", "onProgress ---> " + progress);
                if (progress <= 0) return;
                mOtaState.setState(OtaState.OTA_STATE_WORKING)
                        .setOtaType(OtaState.OTA_TYPE_NETWORK_MODULE)
                        .setOtaProgress(progress);
                mOtaStateMLD.setValue(mOtaState);
            }

            @Override
            public void onCancel() {
                JL_Log.i(tag, "startNetworkOta", "onCancel ---> ");
                onOtaCancel();
            }

            @Override
            public void onStop() {
                JL_Log.i(tag, "startNetworkOta", "onStop ---> ");
                onOtaStop();
            }

            @Override
            public void onError(int code, String message) {
                JL_Log.w(tag, "startNetworkOta", "onError ---> code : " + RcspUtil.formatInt(code)
                        + ", " + message);
                onOtaFailed(code, message);
            }
        });
    }

    private boolean isReconnectDevice(BluetoothDevice device) {
        if (null == device || null == reconnectParam) return false;
        return TextUtils.equals(device.getAddress(), reconnectParam.getAddress());
    }

    private void reconnectDevice(String address, String filePath) {
        if (mOtaState.getState() == OtaState.OTA_STATE_RECONNECT) return; //防止重入
        final BluetoothManager btManager = mWatchManager.getBluetoothHelper().getBluetoothOp();
        final HistoryRecord historyRecord = btManager.getHistoryRecord(address);
        JL_Log.d(tag, "reconnectDevice", "address : " + address + ", filePath : " + filePath);
        if (null == historyRecord) {
            JL_Log.w(tag, "reconnectDevice", "No Record. address : " + address);
            callbackReconnectError(ErrorCode.SUB_ERR_RECONNECT_FAILED);
            return;
        }
        reconnectParam = new ReconnectDeviceParam(address, filePath);
        mOtaState.setState(OtaState.OTA_STATE_RECONNECT);
        btManager.connectHistoryRecord(historyRecord, new OnHistoryRecordCallback() {
            @Override
            public void onSuccess(HistoryRecord record) {
                JL_Log.d(tag, "reconnectDevice", "reconnect device successfully.");
            }

            @Override
            public void onFailed(int code, String message) {
                callbackReconnectError(code, message);
            }
        });
    }

    private void callbackReconnectError(int code) {
        BaseError error = OTAError.buildError(code);
        callbackReconnectError(error.getSubCode(), error.getMessage());
    }

    private void callbackReconnectError(int code, String message) {
        if (null == reconnectParam) return;
        reconnectParam = null;
        JL_Log.d(tag, "callbackReconnectError", "code : " + RcspUtil.formatInt(code) + ", " + message);
        onOtaFailed(code, message);
    }

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {

        @Override
        public void onWatchSystemInit(int code) {
            JL_Log.d(tag, "onWatchSystemInit", "code : " + code);
            setInitWatchOK(code == 0);
            final BluetoothDevice device = mWatchManager.getConnectedDevice();
            if (isReconnectDevice(device)) {
                if (code == 0) { //初始化成功， 回连设备成功
                    if (mWatchManager.isBleChangeSpp()) {
                        JL_Log.d(tag, "onWatchSystemInit", "ble change spp");
                        return;
                    }
                    ReconnectDeviceParam param = reconnectParam;
                    reconnectParam = null;
                    final DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(device);
                    if (deviceInfo.isSupportReuseSpaceOTA() && deviceInfo.getExpandMode() == RcspConstant.EXPAND_MODE_ONLY_UPDATE_RESOURCE) {
                        JL_Log.d(tag, "onWatchSystemInit", "Ready to update resource. file Path : " + param.getFilePath());
                        //延时500ms
                        uiHandler.removeMessages(MSG_UPDATE_RESOURCE);
                        uiHandler.sendMessageDelayed(uiHandler.obtainMessage(MSG_UPDATE_RESOURCE, param.getFilePath()), 500);
                        return;
                    }
                    onOtaStop();
                    return;
                }
                JL_Log.d(tag, "onWatchSystemInit", "callbackReconnectError");
                callbackReconnectError(ErrorCode.SUB_ERR_RECONNECT_FAILED);
            }
        }

        @Override
        public void onResourceUpdateUnfinished(BluetoothDevice device) {
            JL_Log.i(tag, "onResourceUpdateUnfinished", "device : " + device);
            setInitWatchOK(true);
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            JL_Log.i(tag, "onMandatoryUpgrade", "device : " + device);
            setInitWatchOK(true);
            if (isReconnectDevice(device)) {
                callbackReconnectError(ErrorCode.SUB_ERR_OTA_FAILED, "Device(" + device + ") status error");
            }
        }

        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            JL_Log.d(tag, "onConnectStateChange", "device : " + device + ", status : " + status);
            if (status != com.jieli.jl_rcsp.constant.StateCode.CONNECTION_OK) {
                setInitWatchOK(false);
                if (isReconnectDevice(device) && (status == com.jieli.jl_rcsp.constant.StateCode.CONNECTION_DISCONNECT
                        || status == com.jieli.jl_rcsp.constant.StateCode.CONNECTION_FAILED)) {
                    JL_Log.d(tag, "onConnectStateChange", "callbackReconnectError");
                    callbackReconnectError(ErrorCode.SUB_ERR_REMOTE_NOT_CONNECTED);
                }
            }
        }
    };

    private final BtEventCallback mBtEventCallback = new BtEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            if (status == StateCode.CONNECTION_OK) {
                setInitOTAOK(true);
            } else if (status == StateCode.CONNECTION_DISCONNECT && !isDevOta()) {
                setInitOTAOK(false);
//                onOtaFailed(FatFsErrCode.RES_REMOTE_NOT_CONNECT, FatUtil.getFatFsErrorCodeMsg(FatFsErrCode.RES_REMOTE_NOT_CONNECT));
            }
        }
    };

    private final OnNetworkListener mOnNetworkListener = new OnNetworkListener() {
        @Override
        public void onNetworkInfo(BluetoothDevice device, NetworkInfo info) {
            if (isRequestNetworkInfo) {
                isRequestNetworkInfo = false;
                tryToQuery4gOtaMsgForServer();
            }
            mNetworkInfoMLD.postValue(info);
        }

        @Override
        public void onNetworkOTAState(BluetoothDevice device, NetworkOTAState state) {

        }
    };
}