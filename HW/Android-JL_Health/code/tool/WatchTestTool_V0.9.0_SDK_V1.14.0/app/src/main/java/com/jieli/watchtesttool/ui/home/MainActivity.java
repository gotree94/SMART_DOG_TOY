package com.jieli.watchtesttool.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.component.utils.FileUtil;
import com.jieli.component.utils.SystemUtil;
import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_bt_ota.util.PreferencesHelper;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.DeviceConnectionData;
import com.jieli.watchtesttool.data.bean.WatchOpData;
import com.jieli.watchtesttool.databinding.ActivityMainBinding;
import com.jieli.watchtesttool.tool.test.TestActivity;
import com.jieli.watchtesttool.tool.test.message.SyncDeviceLogcatTask;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.ui.ContentActivity;
import com.jieli.watchtesttool.ui.device.AddDeviceFragment;
import com.jieli.watchtesttool.ui.ota.NetworkOtaFragment;
import com.jieli.watchtesttool.ui.upgrade.UpgradeFragment;
import com.jieli.watchtesttool.ui.widget.dialog.ConfigDialog;
import com.jieli.watchtesttool.ui.widget.dialog.ResultDialog;
import com.jieli.watchtesttool.ui.widget.dialog.WaitingDialog;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;

import java.io.File;
import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 主界面
 */
@RuntimePermissions
public class MainActivity extends TestActivity {

    private ActivityMainBinding mBinding;
    private MainViewModel mViewModel;

    private WaitingDialog mWaitingDialog;
    private ResultDialog mResultDialog;
    private Jl_Dialog mNotifyDialog;

    private Thread mSyncResThread;

    private SyncDeviceLogcatTask syncDevLogTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.setImmersiveStateBar(getWindow(), true);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        initUI();
        addObserver();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainActivityPermissionsDispatcher.checkAppRequestPermissionsWithPermissionCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectionStatus(mViewModel.isConnected(), mViewModel.getConnectedDevice());
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        removeObserver();
        super.onDestroy();
        mViewModel.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void syncResource(View view) {
        syncTestRes();
    }

    private void syncTestRes() {
        if (mSyncResThread == null) {
            mSyncResThread = new Thread(() -> {
                runOnUiThread(this::showWaitingDialog);
                String[] dirArray = new String[]{WatchTestConstant.DIR_WATCH,
                        WatchTestConstant.DIR_WATCH_BG,
                        WatchTestConstant.DIR_MUSIC,
                        WatchTestConstant.DIR_CONTACTS};
                //26版本以上，默认重新安装需要强制升级资源一次
                boolean isUpdate = PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean(WatchTestConstant.KEY_FORCED_UPDATE_FLAG, true);
                for (String dirName : dirArray) {
                    String dirPath = AppUtil.createFilePath(getApplicationContext(), dirName);
                    File dir = new File(dirPath);
                    if (isUpdate) {
                        if (dir.exists()) {
                            FileUtil.deleteFile(dir);//删除旧的资源文件
                            JL_Log.w(tag, String.format(Locale.getDefault(), "delete dir[%s]", dir.getPath()));
                        }
                        AppUtil.copyAssets(getApplicationContext(), dirName, dirPath);
                    } else {
                        File[] files = dir.listFiles();
                        if (files == null || files.length == 0) {
                            AppUtil.copyAssets(getApplicationContext(), dirName, dirPath);
                        }
                    }
                }
                if (isUpdate) {
                    PreferencesHelper.putBooleanValue(getApplicationContext(), WatchTestConstant.KEY_FORCED_UPDATE_FLAG, false);
                }
                runOnUiThread(() -> {
                    dismissWaitingDialog();
                    ToastUtil.showToastShort("资源已同步");
                });
                mSyncResThread = null;
            });
            mSyncResThread.start();
        }
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    public void checkAppRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MainActivityPermissionsDispatcher.requestBluetoothPermissionWithPermissionCheck(this);
        } else {
            executePermissionPass();
        }
    }

    @OnShowRationale({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    public void appPermissionsShowRationale(@NonNull PermissionRequest request) {
        request.proceed();
    }

    @OnPermissionDenied({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    public void appPermissionsDenied() {
        ToastUtil.showToastShort(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @NeedsPermission({
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @RequiresApi(Build.VERSION_CODES.S)
    public void requestBluetoothPermission() {
        executePermissionPass();
    }

    @OnShowRationale({
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @RequiresApi(Build.VERSION_CODES.S)
    public void bluetoothPermissionShowRationale(@NonNull PermissionRequest request) {
        request.proceed();
    }

    @OnPermissionDenied({
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @RequiresApi(Build.VERSION_CODES.S)
    public void bluetoothPermissionDenied() {
        ToastUtil.showToastShort(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    private void initUI() {
        mBinding.viewMainTopbar.tvTopbarTitle.setText(R.string.test_function);
        mBinding.viewMainTopbar.tvTopbarLeft.setOnClickListener(v -> toAddDeviceFragment());
        final String appVersion = SystemUtil.getVersionName(getApplicationContext());
        final int versionCode = SystemUtil.getVersion(getApplicationContext());
        JL_Log.d(tag, "APP version : " + appVersion + ", version code : " + versionCode);
        mBinding.viewMainTopbar.tvTopbarRight.setText(String.format(Locale.ENGLISH, "%s(%d)", appVersion, versionCode));
        mBinding.viewMainTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_settings_gray, 0);
        mBinding.viewMainTopbar.tvTopbarRight.setOnClickListener(v -> {
            ConfigDialog dialog = new ConfigDialog();
            dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
        });
        mBinding.llTestDelay.setVisibility(configHelper.isBanAutoTest() ? View.GONE : View.VISIBLE);
    }

    private void addObserver() {
        mViewModel.mWatchSysStatusMLD.observe(this, status -> {
            if (status == 0) {
                updateConnectionStatus(true, mViewModel.getConnectedDevice());
                final DeviceInfo deviceInfo = mViewModel.getWatchManager().getDeviceInfo();
                if (null != deviceInfo && deviceInfo.isSupportReadErrorMSg()) { //支持读取异常信息功能
                    readDeviceLog(mViewModel.getWatchManager());
                }
            }
        });

        mViewModel.mWatchRestoreSysMLD.observe(this, this::handleWatchOpData);
        mViewModel.mConnectionDataMLD.observeForever(mConnectionDataObserver);
        mViewModel.mWatchUpdateExceptionMLD.observeForever(mUpdateResourceObserver);
        mViewModel.mMandatoryUpgradeMLD.observeForever(mMandatoryUpgradeObserver);
        mViewModel.mNetworkExceptionMLD.observeForever(mNetworkExceptionObserver);
    }

    private void removeObserver() {
        mViewModel.mWatchUpdateExceptionMLD.removeObserver(mUpdateResourceObserver);
        mViewModel.mMandatoryUpgradeMLD.removeObserver(mMandatoryUpgradeObserver);
        mViewModel.mConnectionDataMLD.removeObserver(mConnectionDataObserver);
        mViewModel.mNetworkExceptionMLD.removeObserver(mNetworkExceptionObserver);
    }

    private void executePermissionPass() {
        //26版本以上，默认重新安装需要强制升级资源一次
        boolean isUpdate = PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean(WatchTestConstant.KEY_FORCED_UPDATE_FLAG, true);
        if (isUpdate) syncTestRes();
        initTestList(mViewModel.getWatchManager(), mBinding.llTest);
        updateConnectionStatus(mViewModel.isConnected(), mViewModel.getConnectedDevice());
    }

    private void toAddDeviceFragment() {
        ContentActivity.startContentActivity(this, AddDeviceFragment.class.getCanonicalName());
    }

    private void updateConnectionStatus(boolean isDevConnected, BluetoothDevice device) {
        mBinding.viewMainTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(isDevConnected ? R.drawable.ic_bluetooth_connected_blue :
                R.drawable.ic_bluetooth_disconnect_gray, 0, 0, 0);
        updateDeviceInfo(isDevConnected, device);
        if (isDevConnected) {
            showTips(getString(R.string.bt_status_connected));
            initTestList(mViewModel.getWatchManager(), mBinding.llTest);
        } else {
            dismissNotifyDialog();
        }
    }

    private void handleWatchOpData(WatchOpData data) {
        if (data.getOp() != WatchOpData.OP_RESTORE_SYS) return;
        switch (data.getState()) {
            case WatchOpData.STATE_START:
                showWaitingDialog();
                break;
            case WatchOpData.STATE_PROGRESS:
//                showRestoreSysDialog(Math.round(data.getProgress()));
                break;
            case WatchOpData.STATE_END:
                dismissWaitingDialog();
                boolean isOk = data.getResult() == 0;
                int res = isOk ? R.drawable.ic_success_green : R.drawable.ic_fail_yellow;
                String text = isOk ? getString(R.string.restore_system_success) : String.format(Locale.ENGLISH, "%s%s",
                        getString(R.string.restore_system_failure), FatUtil.getFatFsErrorCodeMsg(data.getResult()));
                if (isOk) showTips(text);
                showResultDialog(isOk, res, text);
                break;
        }
    }

    private void showWaitingDialog() {
        if (isDestroyed()) return;
        if (null == mWaitingDialog) {
            mWaitingDialog = new WaitingDialog();
        }
        if (!mWaitingDialog.isShow()) {
            mWaitingDialog.show(getSupportFragmentManager(), WaitingDialog.class.getSimpleName());
        }
    }

    private void dismissWaitingDialog() {
        if (isDestroyed()) return;
        if (null != mWaitingDialog) {
            if (mWaitingDialog.isShow()) mWaitingDialog.dismiss();
            mWaitingDialog = null;
        }
    }

    private void showResultDialog(boolean result, int res, String text) {
        if (isDestroyed() || isFinishing()) return;
        if (null == mResultDialog) {
            mResultDialog = new ResultDialog.Builder()
                    .setImgId(res)
                    .setOk(result)
                    .setResult(text)
                    .setCancel(false)
                    .setBtnText(getString(R.string.sure))
                    .create();
            mResultDialog.setOnResultListener(isOk -> {
                dismissResultDialog();
                if (!isOk) {
                    mViewModel.disconnectDevice(mViewModel.getConnectedDevice());
                }
            });
        }
        if (!mResultDialog.isShow()) {
            mResultDialog.show(getSupportFragmentManager(), ResultDialog.class.getSimpleName());
        }
    }

    private void dismissResultDialog() {
        if (isDestroyed() || isFinishing()) return;
        if (null != mResultDialog) {
            if (mResultDialog.isShow()) {
                mResultDialog.dismiss();
            }
            mResultDialog = null;
        }
    }

    private void showNotifyDialog(String content, int type) {
        if (isDestroyed() || isFinishing()) return;
        if (null == mNotifyDialog) {
            mNotifyDialog = Jl_Dialog.builder()
                    .width(0.9f)
                    .cancel(false)
                    .title(getString(R.string.tips))
                    .content(content)
                    .left(getString(R.string.cancel))
                    .leftColor(getResources().getColor(R.color.text_secondary_disable_color))
                    .leftClickListener((view, dialogFragment) -> {
                        dismissNotifyDialog();
                        mViewModel.disconnectDevice(mViewModel.getConnectedDevice());
                    })
                    .right(getString(R.string.sure))
                    .rightColor(getResources().getColor(R.color.auxiliary_error))
                    .rightClickListener((view, dialogFragment) -> {
                        dismissNotifyDialog();
                        if (type == UpgradeFragment.OTA_FLAG_NETWORK) {
                            ContentActivity.startContentActivity(MainActivity.this, NetworkOtaFragment.class.getCanonicalName());
                            return;
                        }
                        ContentActivity.startContentActivity(MainActivity.this, UpgradeFragment.class.getCanonicalName());
                    })
                    .build();
        }
        if (!mNotifyDialog.isShow()) {
            mNotifyDialog.show(getSupportFragmentManager(), "notify_update_resource");
        }
    }

    private void dismissNotifyDialog() {
        if (isDestroyed() || isFinishing()) return;
        if (null != mNotifyDialog) {
            if (mNotifyDialog.isShow()) {
                mNotifyDialog.dismiss();
            }
            mNotifyDialog = null;
        }
    }

    /**
     * 更新设备信息
     *
     * @param device 设备对象
     */
    private void updateDeviceInfo(boolean isConnected, BluetoothDevice device) {
        mBinding.tvMainDevNameValue.setText(!isConnected || device == null ? "" : AppUtil.getDeviceName(device));
        mBinding.tvMainDevAddressValue.setText(!isConnected || device == null ? "" : device.getAddress());
        mBinding.tvMainDevStatusValue.setText(isConnected ? getString(R.string.bt_status_connected) : getString(R.string.bt_status_disconnected));
    }

    private void readDeviceLog(WatchManager watchManager) {
        if (null == syncDevLogTask) {
            syncDevLogTask = new SyncDeviceLogcatTask(watchManager);
            syncDevLogTask.setINextTask(error -> {
                final String filePath = syncDevLogTask.getOutputFilePath();
                syncDevLogTask = null;
                if (error.code == WatchError.ERR_FUNC_NOT_SUPPORT) return;
                int res = error.code == 0 ? R.drawable.ic_success_green : R.drawable.ic_fail_yellow;
                if (error.code == 0) {
                    if (null == filePath || filePath.isEmpty()) {
                        //没有错误日志
                        return;
                    }
                    showResultDialog(true, res, "日志文件保存位置: " + filePath);
                    return;
                }
                showTips(String.format(Locale.ENGLISH, "读取异常信息失败.\ncode : %d, %s", error.code, error.msg));
            });
            syncDevLogTask.startTest();
        }
    }

    private final Observer<BluetoothDevice> mUpdateResourceObserver = device -> runOnUiThread(() ->
            showNotifyDialog(getString(R.string.resource_unfinished_tips), UpgradeFragment.OTA_FLAG_RESOURCE));
    private final Observer<BluetoothDevice> mMandatoryUpgradeObserver = device -> runOnUiThread(() ->
            showNotifyDialog(getString(R.string.firmware_mandatory_upgrade), UpgradeFragment.OTA_FLAG_FIRMWARE));
    private final Observer<DeviceConnectionData> mConnectionDataObserver = deviceConnectionData -> runOnUiThread(() ->
            updateConnectionStatus(deviceConnectionData.getStatus() == StateCode.CONNECTION_OK,
                    deviceConnectionData.getDevice()));
    private final Observer<NetworkInfo> mNetworkExceptionObserver = info -> runOnUiThread(() -> {
        if (null != info && info.isMandatoryOTA()) {
            showNotifyDialog(getString(R.string.network_module_ota_upgrade_tips), UpgradeFragment.OTA_FLAG_NETWORK);
        }
    });
}