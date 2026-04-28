package com.jieli.watchtesttool.tool.upgrade.auto;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.impl.BluetoothManager;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.OTAError;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.util.FileUtil;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_fatfs.utils.ZipUtil;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.WatchConstant;
import com.jieli.jl_rcsp.interfaces.watch.OnUpdateResourceCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.WatchFileUtil;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.upgrade.OTAManager;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;

import java.io.File;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 更新任务
 * @since 2022/8/2
 */
public class UpdateTask extends AbstractTestTask {
    private final WatchManager watchManager;
    private final OTAManager otaManager;
    private final UpdateParam param;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private volatile long otaStartTime;  //无线升级开始时间
    private String reconnectAddress;

    public UpdateTask(@NonNull WatchManager watchManager, @NonNull OTAManager otaManager, @NonNull UpdateParam updateParam) {
        this.watchManager = watchManager;
        this.otaManager = otaManager;
        this.param = updateParam;
    }

    @Override
    public void startTest() {
        otaStartTime = 0;
        DeviceInfo deviceInfo = watchManager.getDeviceInfo();
        if (null == deviceInfo) {
            cbError(TestError.ERR_DEVICE_NOT_CONNECT, TestError.getTestMsg(TestError.ERR_DEVICE_NOT_CONNECT));
            return;
        }
        String filePath = param.getFilePath();
        /*
         * 根据文件后缀判断升级类型
         * 若是ufw文件或者buf文件，则认为是固件升级
         * 若是zip文件,则认为资源更新
         */
        boolean isOTAFile = filePath.endsWith(".ufw") || filePath.endsWith(".UFW");
        boolean isZipFile = filePath.endsWith(".zip") || filePath.endsWith(".ZIP");
        if (deviceInfo.isMandatoryUpgrade()) { //设备处于强制升级模式
            cbLog("设备处于强制升级状态");
            if (isOTAFile) {
                otaFirmware(filePath);
                return;
            }
            if (isZipFile) {
                String dirPath = WatchFileUtil.getDirPath(filePath);
                String fileName = WatchFileUtil.getFileName(filePath);
                try {
                    String unzipName = WatchConstant.UNZIP_PREFIX + WatchFileUtil.getNameNoSuffix(fileName);
                    ZipUtil.unZipFolder(filePath, dirPath, unzipName);
                    String otaFilePath = AppUtil.obtainUpdateFilePath(dirPath, OTAManager.OTA_FILE_SUFFIX);
                    JL_Log.i(tag, "startTest", "unZipFolder : " + dirPath + ",\n"
                            + "otaFilePath : " + otaFilePath);
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
            cbError(TestError.ERR_NOT_FOUND_FILE, TestError.getTestMsg(TestError.ERR_NOT_FOUND_FILE));
            return;
        }
        final int expandMode = deviceInfo.getExpandMode();
        switch (expandMode) {
            case WatchConstant.EXPAND_MODE_RES_OTA:
                cbLog("设备处于更新资源状态");
                break;
            case WatchConstant.EXPAND_MODE_ONLY_UPDATE_RESOURCE:
                cbLog("设备处于仅更新资源状态");
                if (!isZipFile) {
                    cbError(TestError.ERR_FILE_ABNORMAL, TestError.getTestMsg(TestError.ERR_FILE_ABNORMAL));
                    return;
                }
                break;
            case WatchConstant.EXPAND_MODE_ONLY_OTA:
                cbLog("设备处于仅升级固件状态");
                if (!isOTAFile) {
                    cbError(TestError.ERR_FILE_ABNORMAL, TestError.getTestMsg(TestError.ERR_FILE_ABNORMAL));
                    return;
                }
                break;
        }
        updateFile(filePath);
    }

    private void updateFile(String filePath) {
        boolean isOTAFile = filePath.endsWith(".ufw") || filePath.endsWith(".UFW");
        boolean isZipFile = filePath.endsWith(".zip") || filePath.endsWith(".ZIP");
        if (isZipFile) {
            otaResource(filePath);
        } else if (isOTAFile) {
            otaFirmware(filePath);
        } else {
            cbError(TestError.ERR_FILE_ABNORMAL, TestError.getTestMsg(TestError.ERR_FILE_ABNORMAL));
        }
    }

    @Override
    public void stopTest() {
        if (otaManager.isOTA()) {
            otaManager.cancelOTA();
        }
    }

    @Override
    public String getName() {
        return String.format(Locale.getDefault(), "升级测试[%s]", WatchFileUtil.getFileName(param.getFilePath()));
    }

    public boolean isOTA() {
        return otaManager.isOTA();
    }

    private void otaResource(String resourcePath) {
        JL_Log.i(tag, "otaResource : " + resourcePath);
        cbLog("准备开始更新资源");
        watchManager.updateWatchResource(resourcePath, new CustomUpdateResourceCb(param.getListener()));
    }

    private void otaFirmware(String otaFilePath) {
        JL_Log.i(tag, "otaFirmware : " + otaFilePath);
        cbLog("准备开始升级固件");
        if (otaManager.getDeviceInfo() == null) {
            final InitOtaTimeout initOtaTimeout = new InitOtaTimeout(otaFilePath);
            uiHandler.postDelayed(initOtaTimeout, 8000);
            otaManager.registerBluetoothCallback(new BtEventCallback() {
                @Override
                public void onConnection(BluetoothDevice device, int status) {
                    if (status != StateCode.CONNECTION_OK && status != StateCode.CONNECTION_DISCONNECT)
                        return;
                    uiHandler.removeCallbacks(initOtaTimeout);
                    otaManager.unregisterBluetoothCallback(this);
                    if (status == StateCode.CONNECTION_OK) {
                        otaFirmware(otaFilePath);
                    } else {
                        cbError(TestError.ERR_DEVICE_NOT_CONNECT, TestError.getTestMsg(TestError.ERR_DEVICE_NOT_CONNECT));
                    }
                }
            });
            return;
        }
        otaManager.getBluetoothOption().setFirmwareFilePath(otaFilePath);
        otaManager.startOTA(new CustomUpdateCallback(otaFilePath, param.getListener()));
    }

    @NonNull
    private String formatLog(String log) {
        if (TextUtils.isEmpty(log)) return "";
        StringBuilder builder = new StringBuilder(log);
        if (param.isShowTime()) {
            int useTimeSec = 0;
            if (otaStartTime > 0) {
                useTimeSec = (int) ((getCurrentTime() - otaStartTime) / 1000);
            }
            if (useTimeSec >= 0) {
                builder.append("\n耗时时间:").append(AppUtil.formatOTATime(useTimeSec));
            }
        }
        return builder.toString();
    }

    private void cbLog(String log) {
        if (TextUtils.isEmpty(log)) return;
        onTestLog(formatLog(log));
    }

    private void cbFinish(int code, String message) {
        message = formatLog(message);
        next(new TestError(code, message));
        otaStartTime = 0;
    }

    private void cbError(int code, String message) {
        JL_Log.w(tag, "cbError", "code : " + code + ", message : " + message);
        param.getListener().onError(OnUpdateListener.OTA_TYPE_FIRMWARE, param.getFilePath(), code, message);
        cbFinish(code, message);
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void deleteUnzipFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;
        File parent = file.getParentFile();
        if (parent != null && parent.isDirectory() && parent.getName().startsWith(WatchConstant.UNZIP_PREFIX)) {
            WatchFileUtil.deleteFile(parent.getPath());
        }
    }

    private void reconnectAndUpdateResource(@NonNull OnUpdateListener listener, String filePath, String address) {
        final BluetoothManager btManager = watchManager.getBluetoothHelper().getBluetoothOp();
        HistoryRecord record = btManager.getHistoryRecord(address);
        if (record == null) {
            JL_Log.w(tag, "reconnectAndUpdateResource", "No Record. address : " + address);
            postOtaFirmwareFail(listener, filePath, OTAError.buildError(ErrorCode.SUB_ERR_RECONNECT_FAILED));
            return;
        }
        final OnWatchCallback watchCallback = new OnWatchCallback() {

            @Override
            public void onRcspInit(BluetoothDevice device, boolean isInit) {
                if (null == device || !device.getAddress().equals(reconnectAddress)) return;
                JL_Log.d(tag, "reconnectAndUpdateResource", "onRcspInit ---> " + isInit);
                if (!isInit) {
                    reconnectAddress = null;
                    watchManager.unregisterOnWatchCallback(this);
                }
            }

            @Override
            public void onWatchSystemInit(int code) {
                final BluetoothDevice device = watchManager.getConnectedDevice();
                if (null == device || !device.getAddress().equals(reconnectAddress)) return;
                JL_Log.d(tag, "reconnectAndUpdateResource", "onWatchSystemInit ---> code : " + code);
                reconnectAddress = null;
                watchManager.unregisterOnWatchCallback(this);
                if (code != 0) {
                    postOtaFirmwareFail(listener, filePath, OTAError.buildError(ErrorCode.SUB_ERR_RECONNECT_FAILED));
                } else {
                    final DeviceInfo deviceInfo = watchManager.getDeviceInfo();
                    if (deviceInfo.isSupportReuseSpaceOTA() && deviceInfo.getExpandMode() == RcspConstant.EXPAND_MODE_ONLY_UPDATE_RESOURCE) {
                        JL_Log.d(tag, "reconnectAndUpdateResource", "onResourceUpdateUnfinished.");
                        otaResource(param.getFilePath());
                    } else {
                        postOtaFirmwareSuccess(listener, filePath);
                    }
                }
            }
        };
        reconnectAddress = address;
        watchManager.registerOnWatchCallback(watchCallback);
        JL_Log.d(tag, "reconnectAndUpdateResource", "" + record);
        btManager.connectHistoryRecord(record, new OnHistoryRecordCallback() {
            @Override
            public void onSuccess(HistoryRecord record) {
                JL_Log.d(tag, "reconnectAndUpdateResource", "reconnect device successfully.");
            }

            @Override
            public void onFailed(int code, String message) {
                JL_Log.w(tag, "reconnectAndUpdateResource", "Failed to reconnect device.  code : " + code
                        + ", " + message);
                reconnectAddress = null;
                watchManager.unregisterOnWatchCallback(watchCallback);
                postOtaFirmwareFail(listener, filePath, OTAError.buildError(ErrorCode.SUB_ERR_RECONNECT_TIMEOUT));
            }
        });
    }

    private void postOtaFirmwareFail(@NonNull OnUpdateListener listener, String filePath, BaseError error) {
        int code = error.getSubCode();
        String msg = String.format(Locale.getDefault(), "更新固件失败!\n错误码:%d\n%s", code, error.getMessage());
        listener.onError(OnUpdateListener.OTA_TYPE_FIRMWARE, filePath, TestError.ERR_OTA_FIRMWARE, formatLog(msg));
        cbFinish(TestError.ERR_OTA_FIRMWARE, msg);
    }

    private void postOtaFirmwareSuccess(@NonNull OnUpdateListener listener, String filePath) {
        deleteUnzipFolder(filePath);
        listener.onStop(OnUpdateListener.OTA_TYPE_FIRMWARE, filePath);
        cbFinish(TestError.ERR_SUCCESS, TestError.getTestMsg(TestError.ERR_SUCCESS));
    }

    private final class CustomUpdateResourceCb implements OnUpdateResourceCallback {
        private final OnUpdateListener listener;
        private final int otaType = OnUpdateListener.OTA_TYPE_RESOURCE;

        public CustomUpdateResourceCb(@NonNull OnUpdateListener listener) {
            this.listener = listener;
        }

        @Override
        public void onStart(String filePath, int total) {
            otaStartTime = getCurrentTime();
            listener.onStart(otaType, filePath, total);
            cbLog(String.format(Locale.getDefault(), "准备更新资源\n文件路径:%s\n升级数量:%d", filePath, total));
        }

        @Override
        public void onProgress(int index, String filePath, float progress) {
            listener.onProgress(otaType, index, filePath, progress);
            int value = Math.round(progress);
            if (value > 100) value = 100;
            cbLog(String.format(Locale.getDefault(), "正在更新第%d文件\n文件路径:%s\n进度: %d%%", (index + 1), filePath, value));
        }

        @Override
        public void onStop(String otaFilePath) {
            JL_Log.i(tag, "CustomUpdateResourceCb", "onStop");
            listener.onStop(otaType, otaFilePath);
            if (null != otaFilePath && !otaFilePath.isEmpty()) { //需要升级固件
                otaFirmware(otaFilePath);
                return;
            }
            cbFinish(TestError.ERR_SUCCESS, TestError.getTestMsg(TestError.ERR_SUCCESS));
        }

        @Override
        public void onError(int code, String message) {
            String msg = String.format(Locale.getDefault(), "更新资源失败!\n错误码:%d\n描述:%s", code, message);
            JL_Log.w(tag, "CustomUpdateResourceCb#onError", msg);
            listener.onError(otaType, param.getFilePath(), TestError.ERR_OTA_RESOURCE, formatLog(msg));
            cbFinish(TestError.ERR_OTA_RESOURCE, msg);
        }
    }

    private final class CustomUpdateCallback implements IUpgradeCallback {
        private final String filePath;
        private final OnUpdateListener listener;

        private final int otaType = OnUpdateListener.OTA_TYPE_FIRMWARE;

        public CustomUpdateCallback(String filePath, @NonNull OnUpdateListener listener) {
            this.filePath = filePath;
            this.listener = listener;
        }

        @Override
        public void onStartOTA() {
            if (otaStartTime == 0) {
                otaStartTime = getCurrentTime();
            }
            listener.onStart(otaType, filePath, 1);
            cbLog(String.format(Locale.getDefault(), "准备升级固件\n文件路径:%s\n升级数量:%d", filePath, 1));
        }

        @Override
        public void onNeedReconnect(String addr, boolean isNewReconnectWay) {
            listener.onNeedReconnect(otaType, addr, isNewReconnectWay);
            cbLog(String.format(Locale.getDefault(), "准备回连设备\n回连地址:%s\n是否使用新回连方式:%s", addr, isNewReconnectWay));
        }

        @Override
        public void onProgress(int type, float progress) {
            listener.onProgress(otaType, type, filePath, progress);
            int value = Math.round(progress);
            if (value > 100) value = 100;
            String otaState = type == 1 ? "更新固件" : "下载文件";
            cbLog(String.format(Locale.getDefault(), "%s进度: %d%%", otaState, value));
        }

        @Override
        public void onStopOTA() {
            JL_Log.i(tag, "CustomUpdateCallback", "onStopOTA");
            postOtaFirmwareSuccess(listener, filePath);
        }

        @Override
        public void onCancelOTA() {
            deleteUnzipFolder(filePath);
            listener.onCancel(otaType, filePath);
            cbFinish(TestError.ERR_USER_STOP, TestError.getTestMsg(TestError.ERR_USER_STOP));
        }

        @Override
        public void onError(BaseError error) {
            if (null == error) return;
            JL_Log.w(tag, "CustomUpdateCallback#onError", error.toString());
            deleteUnzipFolder(filePath);
            if (error.getSubCode() == ErrorCode.SUB_ERR_NEED_UPDATE_RESOURCE) {
                uiHandler.postDelayed(() -> reconnectAndUpdateResource(listener, filePath, error.getMessage()), 3000);
                return;
            }
            postOtaFirmwareFail(listener, filePath, error);
        }
    }

    private class InitOtaTimeout implements Runnable {
        private final String filePath;

        public InitOtaTimeout(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            JL_Log.w(tag, "InitOtaTimeout", "OTAManager init timeout");
            if (otaManager.getDeviceInfo() == null) {
                cbError(TestError.ERR_DEVICE_NOT_CONNECT, TestError.getTestMsg(TestError.ERR_DEVICE_NOT_CONNECT));
            }else{
                otaFirmware(filePath);
            }
        }
    }
}
