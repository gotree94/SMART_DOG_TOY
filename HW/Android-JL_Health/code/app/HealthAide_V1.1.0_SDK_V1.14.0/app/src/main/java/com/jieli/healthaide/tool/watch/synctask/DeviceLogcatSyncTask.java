package com.jieli.healthaide.tool.watch.synctask;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.logcat.LogFileInfo;
import com.jieli.jl_health_http.tool.OnResultCallback;
import com.jieli.jl_rcsp.constant.JLChipFlag;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.task.TaskListener;
import com.jieli.jl_rcsp.task.logcat.ReadLogcatTask;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.WatchFileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 同步设备日志任务
 * @since 2022/5/19
 */
public class DeviceLogcatSyncTask extends DeviceSyncTask {
    private final Context context;

    private final SimpleDateFormat dateFormat = CustomTimeFormatUtil.dateFormat("yyyyMMddHHmmss");
    private static final String PLATFORM_DEVICE = "device";
    private static final String DEVICE_BRAND = "jieli";
    private static final String KEY_CODE = "PNJYELFFFBDITNKY";

    private static final String DEFAULT_DEVICE_MAC = "11:22:33:44:55:66";

    public DeviceLogcatSyncTask(@NonNull Context context, SyncTaskFinishListener finishListener) {
        super(finishListener);
        this.context = context;
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_LOGCAT;
    }

    @Override
    public void start() {
        if (mWatchManager.isFirmwareOTA()) {
            JL_Log.w(tag, "start", "device's ota is in progress.");
            if (finishListener != null) finishListener.onFinish();
            return;
        }
        //创建任务
        final ReadLogcatTask task = new ReadLogcatTask(mWatchManager);
        //设置监听器
        task.setListener(new TaskListener() {
            @Override
            public void onBegin() {
                //回调任务开始
            }

            @Override
            public void onProgress(int progress) {
                //回调进度
            }

            @Override
            public void onFinish() {
                //回调任务完成
                JL_Log.i(tag, "onFinish", "read logcat size = " + task.getResult().length);
                uploadLogFile(task.getResult());
            }

            @Override
            public void onError(int code, String msg) {
                //回调任务异常信息
                JL_Log.w(tag, "onError", "code : " + code + ", " + msg);
                if (null != finishListener) finishListener.onFinish();
            }

            @Override
            public void onCancel(int reason) {
                //回调任务被取消, 该任务暂不支持取消操作
            }
        });
        //执行任务
        task.start();
    }

    private void uploadLogFile(byte[] data) {
        final DeviceInfo deviceInfo = mWatchManager.getDeviceInfo();
        if (deviceInfo == null) {
            JL_Log.w(tag, "uploadLogFile", "Device is disconnect.");
            if (null != finishListener) finishListener.onFinish();
            return;
        }
        String mac = deviceInfo.getEdrAddr();
        if (TextUtils.isEmpty(mac)) mac = "11:22:33:44:55:66";
        String macFlag = mac.replaceAll(":", "");
        String fileName = dateFormat.format(Calendar.getInstance().getTime()) + "_" + macFlag + ".log";
        final String logDir = HealthUtil.createFilePath(context.getApplicationContext(), HealthConstant.DIR_LOG, macFlag);
        if (data.length == 0) {
            checkCacheLog(deviceInfo, logDir);
            return;
        }
        final String filePath = logDir + "/" + fileName;
        if (!FileUtil.bytesToFile(data, filePath)) {
            checkCacheLog(deviceInfo, logDir);
            return;
        }
        uploadFile(deviceInfo, filePath, new OnResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                checkCacheLog(deviceInfo, logDir);
            }

            @Override
            public void onError(int code, String message) {
                if (null != finishListener) finishListener.onFinish();
            }
        });
    }

    private void uploadFile(DeviceInfo deviceInfo, final String filePath, OnResultCallback<Boolean> callback) {
        final String fileName = WatchFileUtil.getFileName(filePath);
        String mac = deviceInfo.getEdrAddr();
        if (TextUtils.isEmpty(mac)) mac = DEFAULT_DEVICE_MAC;
        LogFileInfo logFileInfo = new LogFileInfo();
        logFileInfo.setFileName(fileName);
        logFileInfo.setPlatform(PLATFORM_DEVICE);
        logFileInfo.setBrand(DEVICE_BRAND);
        logFileInfo.setName(getDeviceName(deviceInfo.getSdkType()));
        logFileInfo.setVersion(deviceInfo.getVersionName());
        logFileInfo.setMac(mac);
        logFileInfo.setUuid(mac);
        logFileInfo.setKeyCode(KEY_CODE);
        HttpClient.uploadLogcatFile(filePath, logFileInfo, new OnResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                JL_Log.i(tag, "uploadFile", "update logcat ok. fileName = " + fileName);
                FileUtil.deleteFile(new File(filePath)); //删除本地
                if (null != callback) callback.onResult(true);
            }

            @Override
            public void onError(int code, String message) {
                JL_Log.w(tag, "uploadFile", "onError ---> code : " + code + ", " + message);
                if (null != callback) callback.onError(code, message);
            }
        });
    }

    private void checkCacheLog(DeviceInfo deviceInfo, String dirPath) {
        String cacheLogFilePath = WatchFileUtil.obtainUpdateFilePath(dirPath, ".log");
        if (cacheLogFilePath == null) {
            JL_Log.i(tag, "checkCacheLog", "no cache log.");
            if (null != finishListener) finishListener.onFinish();
        } else {
            uploadFile(deviceInfo, cacheLogFilePath, new OnResultCallback<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    checkCacheLog(deviceInfo, WatchFileUtil.obtainUpdateFilePath(dirPath, ".log"));
                }

                @Override
                public void onError(int code, String message) {
                    JL_Log.w(tag, "checkCacheLog", "onError ---> code : " + code + ", " + message);
                    if (null != finishListener) finishListener.onFinish();
                }
            });
        }
    }

    private String getDeviceName(int sdkType) {
        String name;
        switch (sdkType) {
            case JLChipFlag.JL_CHIP_FLAG_692X_AI_SOUNDBOX:
            case JLChipFlag.JL_CHIP_FLAG_692X_ST_SOUNDBOX:
            case JLChipFlag.JL_CHIP_FLAG_696X_SOUNDBOX:
            case JLChipFlag.JL_CHIP_FLAG_696X_TWS_SOUNDBOX:
                name = "soundbox";
                break;
            case JLChipFlag.JL_CHIP_FLAG_693X_TWS_HEADSET:
            case JLChipFlag.JL_CHIP_FLAG_697X_TWS_HEADSET:
                name = "headset";
                break;
            case JLChipFlag.JL_CHIP_FLAG_695X_CHARGINGBIN:
                name = "chargingBin";
                break;
            case JLChipFlag.JL_CHIP_FLAG_695X_SOUND_CARD:
                name = "sound card";
                break;
            case JLChipFlag.JL_CHIP_FLAG_695X_WATCH:
            case JLChipFlag.JL_CHIP_FLAG_701X_WATCH:
            case JLChipFlag.JL_CHIP_FLAG_707N_WATCH:
                name = "watch";
                break;
            default:
                name = "sdk flag : " + sdkType;
                break;
        }
        return name;
    }

}
