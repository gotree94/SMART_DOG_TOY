package com.jieli.watchtesttool.ui.upgrade;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.model.OTAError;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.jieli.jl_bt_ota.util.FileUtil;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothViewModel;
import com.jieli.watchtesttool.tool.config.ConfigHelper;
import com.jieli.watchtesttool.tool.test.LogDialog;
import com.jieli.watchtesttool.tool.test.TestTaskQueue;
import com.jieli.watchtesttool.tool.upgrade.OTAManager;
import com.jieli.watchtesttool.tool.upgrade.auto.OnUpdateListener;
import com.jieli.watchtesttool.tool.upgrade.auto.ReConnectTask;
import com.jieli.watchtesttool.tool.upgrade.auto.UpdateParam;
import com.jieli.watchtesttool.tool.upgrade.auto.UpdateTask;
import com.jieli.watchtesttool.ui.upgrade.fileobserver.FileObserverCallback;
import com.jieli.watchtesttool.ui.upgrade.fileobserver.OtaFileObserverHelper;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc OTA逻辑实现
 * @since 2022/8/8
 */
public class UpdateViewModel extends BluetoothViewModel {
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

    private boolean isInitOTAOK;       //OTA库是否初始化OK
    private boolean isInitWatchOK;     //手表库是否初始化OK

    private boolean isSingleOTAWay;    //是否单备份OTA
    private boolean isNewReconnectWay; //设备是否处于新回连状态

    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private volatile TestTaskQueue mTaskQueue;

    public final MutableLiveData<OtaState> mOtaStateMLD = new MutableLiveData<>();
    public final MutableLiveData<ArrayList<File>> mOtaFileListMLD = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mOtaInitMLD = new MutableLiveData<>();

    public UpdateViewModel(Context context) {
        mContext = context;
        mOTAManager = new OTAManager(context);
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
        return mOTAManager.isOTA() || (mTaskQueue != null && mTaskQueue.isTesting());
    }

    public boolean isAutoTest() {
        return !mConfigHelper.isBanAutoTest();
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

    public void startAutoOTA(Fragment fragment, List<String> filePathList, int loop) {
        if (mOtaInitMLD.getValue() == null || !mOtaInitMLD.getValue()) {
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_PARAMETER, "Please wait for the ota lib to initialize."));
            return;
        }
        if (null == filePathList || filePathList.isEmpty()) {
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_PARAMETER, "file path is null."));
            return;
        }
        if (isDevOta()) {
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_OTA_IN_HANDLE, "Auto Test is progressing."));
            return;
        }
        TargetInfoResponse deviceInfo = mOTAManager.getDeviceInfo();
        if(null == deviceInfo){
            postOtaFailed(OTAError.buildError(ErrorCode.SUB_ERR_RECONNECT_TIMEOUT));
            return;
        }
        if(deviceInfo.isMandatoryUpgrade()){
            loop = 1; //强制升级模式，不允许压力测试
        }
        int temp = loop / filePathList.size();
        int loopNum = loop % filePathList.size() == 0 ? temp : temp + 1;
        int taskCount = (loop - 1) * 2 + 1;
        mTaskQueue = new TestTaskQueue(taskCount);
        int addTask = 0;
        for (int i = 0; i < loopNum; i++) {
            if (addTask >= taskCount) break;
            boolean isEnd = i == loopNum - 1;
            for (int j = 0; j < filePathList.size(); j++) {
                String filePath = filePathList.get(j);
                boolean isListEnd = j == filePathList.size() - 1;
                UpdateParam param = new UpdateParam(filePath, new CustomUpdateListener(filePath));
                param.setShowTime(true);
                mTaskQueue.add(new UpdateTask(mWatchManager, mOTAManager, param));
                addTask++;
                if (addTask >= taskCount) break;
                if (isEnd && isListEnd) continue;
                mTaskQueue.add(new ReConnectTask(mWatchManager));
                addTask++;
                if (addTask >= taskCount) break;
            }
        }
        if (loop > 1 && null != fragment) {
            final LogDialog dialog = new LogDialog(mTaskQueue, v -> mTaskQueue.stopTest());
            mTaskQueue.delayTask = 3000;
            dialog.show(fragment.getChildFragmentManager(), LogDialog.class.getSimpleName());
        }
        mTaskQueue.startTest();
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
        JL_Log.d(tag, "setInitWatchOK : isInitOTAOK = " + isInitOTAOK + ", " + isInit);
        if (isInitOTAOK != isInit) {
            isInitOTAOK = isInit;
            checkInitValue();
        }
    }

    private void setInitWatchOK(boolean isInit) {
        JL_Log.d(tag, "setInitWatchOK : isInitWatchOK = " + isInitWatchOK + ", " + isInit);
        if (isInitWatchOK != isInit) {
            isInitWatchOK = isInit;
            checkInitValue();
        }
    }

    private void checkInitValue() {
        boolean isInitOTA = isInitOTAOK && isInitWatchOK;
        JL_Log.d(tag, "checkInitValue : isInitOTAOK = " + isInitOTAOK + ", isInitWatchOK = " + isInitWatchOK);
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
            } else if (!isDevOta()) {
                setInitWatchOK(false);
                setInitOTAOK(false);
            }
        }
    };

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {

        @Override
        public void onRcspInit(BluetoothDevice device, boolean isInit) {
            if (isInit) {
                DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(device);
                if (null != deviceInfo && !deviceInfo.isSupportExternalFlashTransfer()) {
                    //如果不支持外挂FLASH功能，也认为Watch初始化OK
                    setInitWatchOK(true);
                }
            }
        }

        @Override
        public void onWatchSystemInit(int code) {
            JL_Log.i(tag, "-onWatchSystemInit- " + code);
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
        if (event > 2 && path != null) {
            mUIHandler.removeMessages(MSG_REFRESH_FILE);
            mUIHandler.sendEmptyMessageDelayed(MSG_REFRESH_FILE, 500);
        }
    };

    private final class CustomUpdateListener implements OnUpdateListener {
        private final String otaFilePath;

        public CustomUpdateListener(String otaFilePath) {
            this.otaFilePath = otaFilePath;
        }

        @Override
        public void onStart(int otaType, String filePath, int total) {
            JL_Log.i(tag, "OnUpdateListener#onStart", "otaType = " + otaType + ", total = " + total + ", filePath = " + filePath);
            OtaState otaState = mOtaStateMLD.getValue();
            if (otaState == null) return;
            otaState.setState(OtaState.OTA_STATE_START);
            if (otaType == OnUpdateListener.OTA_TYPE_RESOURCE) {
                otaState.setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                        .setOtaTotal(total);
            } else {
                TargetInfoResponse deviceInfo = mOTAManager.getDeviceInfo();
                if (null != deviceInfo) {
                    isSingleOTAWay = !deviceInfo.isSupportDoubleBackup(); //记录是否单备份升级
                }
                otaState.setOtaType(OtaState.OTA_TYPE_OTA_READY);
            }
            otaState.setError(null);
            mOtaStateMLD.setValue(otaState);
        }

        @Override
        public void onProgress(int otaType, int flag, String filePath, float progress) {
            if (progress > 0) {
                JL_Log.d(tag, "OnUpdateListener#onProgress", "flag = " + flag + ", progress = " + progress + ", otaType = " + otaType);
                OtaState otaState = mOtaStateMLD.getValue();
                if (otaState == null) return;
                if (otaType == OTA_TYPE_RESOURCE) {
                    otaState.setState(OtaState.OTA_STATE_WORKING)
                            .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                            .setOtaIndex(flag + 1)
                            .setOtaFileInfo(AppUtil.getFileNameByPath(filePath))
                            .setOtaProgress(progress);
                } else {
                    otaState.setState(OtaState.OTA_STATE_WORKING)
                            .setOtaType(flag == 0 ? OtaState.OTA_TYPE_OTA_READY : OtaState.OTA_TYPE_OTA_UPGRADE_FIRMWARE)
                            .setOtaProgress(progress);
                }
                mOtaStateMLD.setValue(otaState);
            }
        }

        @Override
        public void onNeedReconnect(int otaType, String mac, boolean isNewWay) {
            JL_Log.i(tag, "OnUpdateListener#onNeedReconnect", "address = " + mac + ", isNewWay = " + isNewWay
                    + ",\n isSingleOTAWay = " + isSingleOTAWay + ", isSPPConnectWay = " + mConfigHelper.isSPPConnectWay());
            //TODO:允许客户自定义回连方式
            isNewReconnectWay = isNewWay;
            if (isSingleOTAWay && isNewWay && mConfigHelper.isSPPConnectWay()) { //切换通讯方式
                mConfigHelper.setTempConnectWay(BluetoothConstant.PROTOCOL_TYPE_SPP); //记录原通讯方式
                mConfigHelper.setSppConnectWay(!mConfigHelper.isSPPConnectWay());   //设置BLE通讯方式
            }
        }

        @Override
        public void onStop(int otaType, String otaFilePath) {
            JL_Log.i(tag, "OnUpdateListener#onStop", "otaType = " + otaType + ", otaFilePath = " + otaFilePath
                    + ", isNewReconnectWay = " + isNewReconnectWay);
            OtaState otaState = mOtaStateMLD.getValue();
            if (otaState == null) return;
            if (otaType == OTA_TYPE_RESOURCE) {
                if (otaFilePath == null) {
                    otaState.setState(OtaState.OTA_STATE_STOP)
                            .setOtaType(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE)
                            .setStopResult(OtaState.OTA_RES_SUCCESS)
                            .setOtaProgress(0f);
                    mOtaStateMLD.setValue(otaState);
                }
            } else {
                if (isNewReconnectWay) isNewReconnectWay = false;
                otaState.setState(OtaState.OTA_STATE_STOP)
                        .setStopResult(OtaState.OTA_RES_SUCCESS)
                        .setOtaProgress(0f);
                mOtaStateMLD.setValue(otaState);
                if (otaFilePath != null && isZipFile()) {
                    FileUtil.deleteFile(new File(otaFilePath));
                }
                if (isSingleOTAWay && mConfigHelper.getTempConnectWay() != -1) { //单备份升级，临时连接方式有值
                    //进行还原操作
                    mConfigHelper.setSppConnectWay(mConfigHelper.getTempConnectWay() == BluetoothConstant.PROTOCOL_TYPE_SPP);
                    //清除临时连接方式
                    mConfigHelper.setTempConnectWay(-1);
                }
            }
        }

        @Override
        public void onCancel(int otaType, String filePath) {
            JL_Log.w(tag, "OnUpdateListener#onCancel", "otaType = " + otaType);
            if (filePath != null && isZipFile()) {
                FileUtil.deleteFile(new File(filePath));
            }
            OtaState otaState = mOtaStateMLD.getValue();
            if (otaState == null) return;
            otaState.setState(OtaState.OTA_STATE_STOP)
                    .setStopResult(OtaState.OTA_RES_CANCEL)
                    .setOtaProgress(0f);
            mOtaStateMLD.setValue(otaState);
        }

        @Override
        public void onError(int otaType, String filePath, int code, String message) {
            JL_Log.e(tag, "OnUpdateListener#onError", "otaType = " + otaType + ", code = " + code + ", " + message);
            if (otaType == OTA_TYPE_RESOURCE) {
                postOtaFailed(OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE, OTAError.buildError(code, message));
            } else {
                if (filePath != null && isZipFile()) {
                    FileUtil.deleteFile(new File(filePath));
                }
                postOtaFailed(OtaState.OTA_TYPE_OTA_UPGRADE_FIRMWARE, OTAError.buildError(code, message));
            }
        }

        private boolean isZipFile() {
            return null != otaFilePath && (otaFilePath.endsWith(".zip") || otaFilePath.endsWith(".ZIP"));
        }
    }

    public static class UpdateViewModelFactory implements ViewModelProvider.Factory {
        private final Context mContext;

        public UpdateViewModelFactory(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) (new UpdateViewModel(mContext));
        }
    }
}
