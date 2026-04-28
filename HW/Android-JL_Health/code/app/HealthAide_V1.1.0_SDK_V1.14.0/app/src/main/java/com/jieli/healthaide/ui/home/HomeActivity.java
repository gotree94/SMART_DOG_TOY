package com.jieli.healthaide.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.ActivityManager;
import com.jieli.component.utils.FileUtil;
import com.jieli.component.utils.PreferencesHelper;
import com.jieli.component.utils.SystemUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ActivityHomeBinding;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.tool.permission.PermissionsHelper;
import com.jieli.healthaide.tool.upgrade.OTAManager;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.base.DoubleClickBackExitActivity;
import com.jieli.healthaide.ui.device.DeviceFragment;
import com.jieli.healthaide.ui.device.add.AddDeviceFragment;
import com.jieli.healthaide.ui.device.add.QRCodeScanActivity;
import com.jieli.healthaide.ui.device.bean.DeviceConnectionData;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.upgrade.UpgradeFragment;
import com.jieli.healthaide.ui.dialog.PermissionDialog;
import com.jieli.healthaide.ui.dialog.WaitingDialog;
import com.jieli.healthaide.ui.health.HealthFragment;
import com.jieli.healthaide.ui.mine.MineFragment;
import com.jieli.healthaide.ui.service.HealthService;
import com.jieli.healthaide.ui.sports.ui.SportsFragment;
import com.jieli.healthaide.ui.widget.AICloudPopDialog;
import com.jieli.healthaide.ui.widget.AddDevicePopWindow;
import com.jieli.healthaide.ui.widget.ResultDialog;
import com.jieli.healthaide.util.AppUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.util.JL_Log;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

/**
 * HomeActivity
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 主页
 * @since 2025/10/24
 */
@RuntimePermissions
public class HomeActivity extends DoubleClickBackExitActivity implements NetworkStateHelper.Listener {
    public static final String HOME_ACTIVITY_RELOAD = "com.jieli.healthaide.HOME_ACTIVITY_RELOAD";
    private ActivityHomeBinding mHomeBinding;

    private HomeViewModel mViewModel;
    private int currentPos = -1;

    private WaitingDialog mRestoreSysDialog;
    private ResultDialog mResultDialog;
    private Jl_Dialog mNotifyDialog;
    private AddDevicePopWindow mAddDevicePopWindow;
    private Jl_Dialog mRingTipsDialog;
    private boolean isUserNeverAskAgain = false;
    private boolean isNeedReconnectDevice = false;
    private HomeReceiver mReceiver;
    private Thread mSyncAssertResThread = null;
    private final Fragment[] mFragments = new Fragment[]{
            HealthFragment.newInstance(),
            SportsFragment.newInstance(),
            DeviceFragment.newInstance(),
            MineFragment.newInstance()
    };
    private boolean isNeedReload = false;


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            HomeActivityPermissionsDispatcher.onPostNotificationPermissionGrantWithPermissionCheck(this);
            return;
        }
        startForegroundService();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        initServer();
        mHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(mHomeBinding.getRoot());
        setWindowStatus(R.id.cl_home_main);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        initView();
        addObserver();
        boolean isSyncAssertRes = PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean(HealthConstant.KEY_ASSERT_RES_SYNC, true);
        if (isSyncAssertRes) {
            syncAssertRes();
        }
        if (PermissionsHelper.checkAppPermissionsIsAllow(this) && !BluetoothUtil.isBluetoothEnable()) {
            BluetoothUtil.enableBluetooth(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isNeedReconnectDevice = false;
        if (AIManager.isInit()) {
            AIManager.getInstance().release();
        }
        unregisterHomeReceiver();
        dismissAddDevicePopWindow();
        dismissNotifyDialog();
        dismissResultDialog();
        dismissRestoreSysDialog();
        removeObserver();
        JL_Log.w(tag, "onDestroy", "isNeedReload : " + isNeedReload);
        if (!isNeedReload) {
            mViewModel.destroy();
            ActivityManager.getInstance().popAllActivity();
            getApplicationContext().stopService(new Intent(getApplicationContext(), HealthService.class));
        } else {
            mViewModel.release();
            isNeedReload = false;
        }
    }

    @Override
    public void onNetworkStateChange(NetWorkStateModel model) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HomeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({
            Manifest.permission.CAMERA,
    })
    public void toQrScanFragment() {
        startActivity(new Intent(this, QRCodeScanActivity.class));
    }

    @OnShowRationale({
            Manifest.permission.CAMERA,
    })
    public void showRelationForCamera(PermissionRequest request) {
        showCameraDialog(request);
        isUserNeverAskAgain = true;
    }

    @OnPermissionDenied({
            Manifest.permission.CAMERA,
    })
    public void onCameraDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @OnNeverAskAgain({
            Manifest.permission.CAMERA,
    })
    public void onCameraNeverAskAgain() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showCameraDialog(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.POST_NOTIFICATIONS})
    public void onPostNotificationPermissionGrant() {
        startForegroundService();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({Manifest.permission.POST_NOTIFICATIONS})
    public void onPostNotificationPermissionShowRationale(PermissionRequest request) {
        request.proceed();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({Manifest.permission.POST_NOTIFICATIONS})
    public void onPostNotificationPermissionNeverAsk() {
        startForegroundService();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.POST_NOTIFICATIONS})
    public void onPostNotificationPermissionDenied() {
        startForegroundService();
    }

    //初始化服务
    private void initServer() {
        //初始化人工智能
        AIManager.init(this, WatchManager.getInstance());
        Intent intent = new Intent(getApplicationContext(), AICloudPopDialog.class);
        intent.setAction(AICloudPopDialog.ACTION_AI_CLOUD);
        intent.putExtra(AICloudPopDialog.EXTRA_INIT, true);
        startService(intent);

        //初始化Bugly
        CrashReport.initCrashReport(getApplicationContext(), "44a5616453", false);
    }

    @SuppressLint("NonConstantResourceId")
    private void initView() {
        mHomeBinding.vp2HomeContainer.setOffscreenPageLimit(4);
        mHomeBinding.vp2HomeContainer.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragments[position];
            }

            @Override
            public int getItemCount() {
                return mFragments.length;
            }
        });
        mHomeBinding.vp2HomeContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    mHomeBinding.bnvHomeBottomBar.setSelectedItemId(mHomeBinding.bnvHomeBottomBar.getMenu()
                            .getItem(mHomeBinding.vp2HomeContainer.getCurrentItem()).getItemId());
                }
            }
        });
        mHomeBinding.vp2HomeContainer.setUserInputEnabled(false);
        mHomeBinding.bnvHomeBottomBar.setOnItemSelectedListener(item -> {
            int position;
            final int itemId = item.getItemId();
            if (itemId == R.id.tab_sports) {
                position = 1;
            } else if (itemId == R.id.tab_device) {
                position = 2;
            } else if (itemId == R.id.tab_mine) {
                position = 3;
            } else {
                position = 0;
            }
            setActionBar(position);
            mHomeBinding.vp2HomeContainer.setCurrentItem(position, false);
            return true;
        });
        mHomeBinding.bnvHomeBottomBar.setItemTextAppearanceActive(R.style.textMedium);
        mHomeBinding.bnvHomeBottomBar.setItemTextAppearanceInactive(R.style.textMedium);
        mHomeBinding.toolbarHome.tvToolbarTitle.setText(R.string.tab_health);
        setActionBar(0);
        mHomeBinding.bnvHomeBottomBar.setSelectedItemId(R.id.tab_health);

        initHomeReceiver();
    }

    private void addObserver() {
        mViewModel.mWatchRestoreSysMLD.observe(this, this::handleWatchOpData);
        mViewModel.mWatchUpdateExceptionMLD.observeForever(mUpdateResourceObserver);
        mViewModel.mMandatoryUpgradeMLD.observeForever(mMandatoryUpgradeObserver);
        mViewModel.mConnectionDataMLD.observeForever(mConnectionDataObserver);
        mViewModel.mRingPlayStatusMLD.observe(this, ringStatus -> {
            if (ringStatus) {
                showRingTipsDialog();
            } else {
                dismissRingTipsDialog();
            }
        });
        mViewModel.mNetworkExceptionMLD.observeForever(mNetworkExceptionObserver);
    }

    private void removeObserver() {
        mViewModel.mWatchUpdateExceptionMLD.removeObserver(mUpdateResourceObserver);
        mViewModel.mMandatoryUpgradeMLD.removeObserver(mMandatoryUpgradeObserver);
        mViewModel.mConnectionDataMLD.removeObserver(mConnectionDataObserver);
        mViewModel.mNetworkExceptionMLD.removeObserver(mNetworkExceptionObserver);
    }

    private String getFragmentTitle(int position) {
        String title = getString(R.string.tab_health);
        switch (position) {
            case 1:
                title = getString(R.string.tab_sports);
                break;
            case 2:
                title = getString(R.string.tab_device);
                break;
            case 3:
                title = getString(R.string.tab_mine);
                break;
        }
        return title;
    }

    @SuppressLint("NonConstantResourceId")
    private void setActionBar(int position) {
        if (currentPos == position) return;
        currentPos = position;
        String title = getFragmentTitle(position);
        setSupportActionBar(mHomeBinding.toolbarHome.viewToolbar);
        mHomeBinding.clHomeMain.setBackgroundColor(getResources().getColor(R.color.bg_color));
        mHomeBinding.toolbarHome.tvToolbarTitle.setText(title);
        mHomeBinding.toolbarHome.tvToolbarAddDevice.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_more_black, 0);
        mHomeBinding.tvHomeTitle.setVisibility(View.GONE);
        mHomeBinding.toolbarHome.getRoot().setVisibility(View.VISIBLE);
        mHomeBinding.tvHomeTitle.setText(title);
        mHomeBinding.clHomeMain.setBackgroundColor(getResources().getColor(position == 3 ? R.color.content_color : R.color.bg_color));
        mHomeBinding.toolbarHome.tvToolbarAddDevice.setOnClickListener(this::showAddDevicePopWindow);
        mHomeBinding.toolbarHome.tvToolbarAddDevice.setVisibility(position <= 2 ? View.VISIBLE : View.GONE);
//        mHomeBinding.toolbarHome.viewToolbar.setBackgroundColor(Color.BLUE);

    }

    private void toAddDeviceFragment() {
        ContentActivity.startContentActivity(this, AddDeviceFragment.class.getCanonicalName());
    }

    private void toUpgradeFragmentForResource() {
        String otaDir = HealthUtil.createFilePath(getApplicationContext(), HealthConstant.DIR_UPDATE);
        String otaFilePath = HealthUtil.obtainUpdateFilePath(otaDir, OTAManager.OTA_ZIP_SUFFIX);
        toUpgradeFragment(UpgradeFragment.OTA_FLAG_RESOURCE, otaFilePath);
    }

    private void toUpgradeFragmentForFirmware() {
        String otaDir = HealthUtil.createFilePath(getApplicationContext(), HealthConstant.DIR_UPDATE);
        String otaFilePath = HealthUtil.obtainUpdateFilePath(otaDir, OTAManager.OTA_FILE_SUFFIX);
        JL_Log.i(tag, "toUpgradeFragmentForFirmware", "otaDir : " + otaDir + ", otaFilePath : " + otaFilePath);
        toUpgradeFragment(UpgradeFragment.OTA_FLAG_FIRMWARE, otaFilePath);
    }

    private void toUpgradeFragment(int flag, String otaFilePath) {
        Bundle bundle = new Bundle();
        bundle.putInt(UpgradeFragment.KEY_OTA_FLAG, flag);
        bundle.putString(UpgradeFragment.KEY_OTA_FILE_PATH, otaFilePath);
        ContentActivity.startContentActivity(this, UpgradeFragment.class.getCanonicalName(), bundle);
    }

    private void handleWatchOpData(WatchOpData data) {
        if (data.getOp() != WatchOpData.OP_RESTORE_SYS) return;
        switch (data.getState()) {
            case WatchOpData.STATE_START:
                showRestoreSysDialog(0);
                break;
            case WatchOpData.STATE_PROGRESS:
                showRestoreSysDialog(Math.round(data.getProgress()));
                break;
            case WatchOpData.STATE_END:
                dismissRestoreSysDialog();
                boolean isOk = data.getResult() == 0;
                int res = isOk ? R.drawable.ic_success_green : R.drawable.ic_fail_yellow;
                String text = isOk ? getString(R.string.restore_watch_system_success) : getString(R.string.restore_watch_system_failure, FatUtil.getFatFsErrorCodeMsg(data.getResult()));
                showResultDialog(isOk, res, text);
                break;
        }
    }

    private void showRestoreSysDialog(int progress) {
        if (isDestroyed() || isFinishing()) return;
        if (mRestoreSysDialog == null) {
            mRestoreSysDialog = new WaitingDialog();
            mRestoreSysDialog.setCancelable(false);
        }
        if (!mRestoreSysDialog.isShow()) {
            mRestoreSysDialog.show(getSupportFragmentManager(), "restore_sys");
        }
    }

    private void dismissRestoreSysDialog() {
        if (isDestroyed() || isFinishing()) return;
        if (null != mRestoreSysDialog) {
            if (mRestoreSysDialog.isShow()) {
                mRestoreSysDialog.dismiss();
            }
            mRestoreSysDialog = null;
        }
    }

    private void showResultDialog(boolean result, int res, String text) {
        if (isDestroyed() || isFinishing()) return;
        if (null == mResultDialog) {
            mResultDialog = new ResultDialog.Builder()
                    .setImgId(res)
                    .setResultCode(result ? 1 : 0)
                    .setResult(text)
                    .setCancel(false)
                    .setBtnText(getString(R.string.sure))
                    .create();
            mResultDialog.setOnResultListener(isOk -> {
                dismissResultDialog();
                if (isOk == 0) {
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

    private void showNotifyDialog(final int flag, String content) {
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
                        if (flag == UpgradeFragment.OTA_FLAG_RESOURCE) {
                            toUpgradeFragmentForResource();
                        } else if (flag == UpgradeFragment.OTA_FLAG_FIRMWARE) {
                            toUpgradeFragmentForFirmware();
                        } else {
                            DeviceInfo deviceInfo = mViewModel.getDeviceInfo(mViewModel.getConnectedDevice());
                            NetworkInfo networkInfo = null == deviceInfo ? null : deviceInfo.getNetworkInfo();
                            if (null == networkInfo || (networkInfo.isMandatoryOTA() && (networkInfo.getVersion() == null
                                    || networkInfo.getVersion().isEmpty()))) {
                                showTips(getString(R.string.ota_err_file_info));
                                mViewModel.disconnectDevice(mViewModel.getConnectedDevice());
                                return;
                            }
                            toUpgradeFragment(flag, "");
                        }
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

    private void showAddDevicePopWindow(View view) {
        if (isDestroyed() || isFinishing()) return;
        if (mAddDevicePopWindow == null) {
            mAddDevicePopWindow = new AddDevicePopWindow(getApplicationContext());
            mAddDevicePopWindow.setOnAddDevicePopWindowListener(scanWay -> {
                dismissAddDevicePopWindow();
                switch (scanWay) {
                    case AddDevicePopWindow.SCAN_WAY_QR:
                        showPermissionDialog(Manifest.permission.CAMERA, null, ((permission) ->
                                HomeActivityPermissionsDispatcher.toQrScanFragmentWithPermissionCheck(this)));
                        break;
                    case AddDevicePopWindow.SCAN_WAY_DEVICE:
                        toAddDeviceFragment();
                        break;
                }
            });
        }
        if (!mAddDevicePopWindow.isShowing()) {
            mAddDevicePopWindow.showPopupWindow(view);
        }
    }

    private void dismissAddDevicePopWindow() {
        if (isDestroyed() || isFinishing()) return;
        if (mAddDevicePopWindow != null) {
            if (mAddDevicePopWindow.isShowing()) {
                mAddDevicePopWindow.dismissPopupWindow();
            }
            mAddDevicePopWindow = null;
        }
    }

    private void showCameraDialog(PermissionRequest request) {
        PermissionDialog permissionDialog = new PermissionDialog(Manifest.permission.CAMERA, request);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getSupportFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

    private void showRingTipsDialog() {
        if (mRingTipsDialog == null) {
            mRingTipsDialog = Jl_Dialog.builder()
                    .width(0.85f)
                    .content(getString(R.string.search_phone))
                    .left(getString(R.string.close_play))
                    .cancel(false)
                    .leftColor(getResources().getColor(R.color.blue_558CFF))
                    .leftClickListener((v, dialogFragment) -> {
                        dialogFragment.dismiss();
                        mRingTipsDialog = null;
                        if (mViewModel != null) {
                            mViewModel.stopRing();
                        }
                    })
                    .build();
        }
        if (!mRingTipsDialog.isShow()) {
            FragmentManager fm = getSupportFragmentManager();
            if (ActivityManager.getInstance().getTopActivity() != null && !(ActivityManager.getInstance().getTopActivity() instanceof HomeActivity)) {
                fm = ((BaseActivity) ActivityManager.getInstance().getTopActivity()).getSupportFragmentManager();
            }
            mRingTipsDialog.show(fm, "search_phone");
        }
    }

    private void dismissRingTipsDialog() {
        if (mRingTipsDialog != null) {
            if (mRingTipsDialog.isShow() && !isDestroyed()) {
                mRingTipsDialog.dismiss();
            }
            mRingTipsDialog = null;
        }
    }

    private void syncAssertRes() {
        if (mSyncAssertResThread == null) {
            mSyncAssertResThread = new Thread(() -> {
                String[] assertDirectories = new String[]{"aidial"};
                boolean isSyncAssertRes = PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean(HealthConstant.KEY_ASSERT_RES_SYNC, true);
                for (String dirName : assertDirectories) {
                    String dirPath = HealthUtil.createFilePath(getApplicationContext(), dirName);
                    File dir = new File(dirPath);
                    if (isSyncAssertRes) {
                        if (dir.exists()) {
                            FileUtil.deleteFile(dir);//删除旧的资源文件
                            JL_Log.w(tag, "syncAssertRes", ConnectUtil.formatString("delete dir[%s]", dir.getPath()));
                        }
                        AppUtil.copyAssets(getApplicationContext(), dirName, dirPath);
                    } else {
                        File[] files = dir.listFiles();
                        if (files == null || files.length == 0) {
                            AppUtil.copyAssets(getApplicationContext(), dirName, dirPath);
                        }
                    }
                }
                if (isSyncAssertRes) {
                    PreferencesHelper.putBooleanValue(getApplicationContext(), HealthConstant.KEY_ASSERT_RES_SYNC, false);
                }
                mSyncAssertResThread = null;
            });
            mSyncAssertResThread.start();
        }
    }

    @SuppressLint("UnsafeIntentLaunch")
    private void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void startForegroundService() {
        Intent intent = new Intent(getApplicationContext(), HealthService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }
    }

    private void showPermissionDialog(String permission, PermissionRequest request, PermissionDialog.OnPermissionClickListener listener) {
        if (null == permission || isDestroyed() || isFinishing()) return;
        if (PermissionUtils.hasSelfPermissions(getApplicationContext(), permission)) {
            if (null != listener) listener.onRequest(permission);
            return;
        }
        PermissionDialog permissionDialog = new PermissionDialog(permission, request, listener);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getSupportFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

    @SuppressLint("WrongConstant")
    private void initHomeReceiver() {
        if (null == mReceiver) {
            mReceiver = new HomeReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(HOME_ACTIVITY_RELOAD);
            intentFilter.addAction(HealthConstant.ACTION_RECONNECT_DEVICE);
            intentFilter.addAction(HealthService.ACTION_ACTIVITY_CLASS);
            intentFilter.addAction(HealthConstant.ACTION_UPDATE_RESOURCE_SUCCESS);
            ContextCompat.registerReceiver(this, mReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
        }
    }

    private void unregisterHomeReceiver() {
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private class HomeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent || null == intent.getAction()) return;
            switch (intent.getAction()) {
                case HOME_ACTIVITY_RELOAD: {
                    isNeedReload = true;
                    reload();
                    break;
                }
                case HealthService.ACTION_ACTIVITY_CLASS: {
                    boolean isAppInForeground = SystemUtil.isAppInForeground(getApplicationContext());
                    JL_Log.i(tag, "ACTION_ACTIVITY_CLASS", "isAppInForeground : " + isAppInForeground);
                    if (!isAppInForeground) {
                        runOnUiThread(() -> SystemUtil.moveAppToFront(getApplicationContext()));
                    }
                    break;
                }
                case HealthConstant.ACTION_RECONNECT_DEVICE: {
                    if (!mViewModel.isConnected()) {
                        mViewModel.fastConnect();
                    } else {
                        isNeedReconnectDevice = true;
                    }
                    break;
                }
                case HealthConstant.ACTION_UPDATE_RESOURCE_SUCCESS: {
                    boolean isDialogShow = mNotifyDialog != null && mNotifyDialog.isShow();
                    if (!isDialogShow) return;
                    //证明设备从强制更新资源中恢复，重新加载健康数据
                    dismissNotifyDialog();
                    SyncTaskManager.getInstance().refreshTask();
                    break;
                }
            }
        }
    }

    private final Observer<BluetoothDevice> mUpdateResourceObserver = device -> runOnUiThread(() ->
            showNotifyDialog(UpgradeFragment.OTA_FLAG_RESOURCE, getString(R.string.resource_unfinished_tips)));
    private final Observer<BluetoothDevice> mMandatoryUpgradeObserver = device -> runOnUiThread(() ->
            showNotifyDialog(UpgradeFragment.OTA_FLAG_FIRMWARE, getString(R.string.firmware_mandatory_upgrade)));

    private final Observer<NetworkInfo> mNetworkExceptionObserver = info -> runOnUiThread(() -> {
        if (null != info && info.isMandatoryOTA()) {
            showNotifyDialog(UpgradeFragment.OTA_FLAG_NETWORK, getString(R.string.network_module_ota_upgrade_tips));
        }
    });
    private final Observer<DeviceConnectionData> mConnectionDataObserver = deviceConnectionData -> {
        JL_Log.d(tag, "ConnectionDataMLD", "" + deviceConnectionData);
        if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
            dismissNotifyDialog();
            dismissAddDevicePopWindow();
            if (deviceConnectionData.getStatus() == BluetoothConstant.CONNECT_STATE_DISCONNECT && isNeedReconnectDevice) {
                isNeedReconnectDevice = false;
                mViewModel.fastConnect();
            }
        } else {
            isNeedReconnectDevice = false;
        }
    };
}