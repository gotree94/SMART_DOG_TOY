package com.jieli.healthaide.ui.device.history;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentHistoryRecordBinding;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.DeviceHistoryRecord;
import com.jieli.healthaide.ui.dialog.RequireGPSDialog;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.util.JL_Log;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 历史记录操作界面
 */
@RuntimePermissions
public class HistoryRecordFragment extends BaseFragment {

    private FragmentHistoryRecordBinding mBinding;
    private HistoryRecordViewModel mViewModel;

    private static final int ACTION_RECONNECT = 1;
    private static final int ACTION_DELETE = 2;
    public final static String KEY_HISTORY_RECORD = "history_record";

    private final Handler mUIHandler = new Handler();
    private boolean isDeleteOk = false;  //是否删除设备记录
    private int action;

    private final ActivityResultLauncher<Intent> openBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final int op = action;
                    if (BluetoothUtil.isBluetoothEnable()) {
                        checkPermissionAllow(action);
                    } else if (op == ACTION_RECONNECT) {
                        showTips(getString(R.string.history_connect_failed));
                    }
                }
            });

    public static HistoryRecordFragment newInstance() {
        return new HistoryRecordFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHistoryRecordBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null == getArguments()) {
            requireActivity().finish();
            return;
        }
        DeviceHistoryRecord record = getArguments().getParcelable(KEY_HISTORY_RECORD);
        if (null == record) {
            requireActivity().finish();
            return;
        }

        mBinding.clHistoryDeviceTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.tvHistoryDeviceReconnect.setOnClickListener(v -> {
            action = ACTION_RECONNECT;
            checkPermissionAllow(action);
        });
        mBinding.tvHistoryDeviceDelete.setOnClickListener(v -> {
            action = ACTION_DELETE;
            checkPermissionAllow(action);
        });
        mBinding.clHistoryDeviceTopbar.tvTopbarTitle.setText(record.getHistoryRecord().getName());

        mViewModel = new ViewModelProvider(this, new HistoryRecordViewModel.HistoryRecordViewModelFactory(this)).get(HistoryRecordViewModel.class);
        mViewModel.historyRecord = record;
        mViewModel.mRecordGoneMLD.observe(getViewLifecycleOwner(), aBoolean ->
                //延时执行，防止受到删除设备的影响
                mUIHandler.postDelayed(() -> {
                    if (isDeleteOk) return;
                    showTips(getString(R.string.history_is_gone));
                    requireActivity().finish();
                }, 1000));
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> requireActivity().runOnUiThread(() -> {
            BluetoothDevice cacheDevice = HealthUtil.getRemoteDevice(mViewModel.historyRecord.getHistoryRecord().getAddress());
            if (mViewModel.isMatchDevice(deviceConnectionData.getDevice(), cacheDevice)) {
                boolean isConnected = deviceConnectionData.getStatus() == BluetoothConstant.CONNECT_STATE_CONNECTED;
                updateReconnectUI(isConnected);
            }
        }));
        mViewModel.mRemoveResultMLD.observe(getViewLifecycleOwner(), historyRemoveResult -> {
            dismissWaitDialog();
            if (historyRemoveResult.isResult()) {
                isDeleteOk = true;
                showTips(getString(R.string.remove_history_success));
                requireActivity().finish();
            } else {
                showTips(getString(R.string.remove_history_failed));
            }
        });
        mViewModel.mHistoryConnectStatusMLD.observe(getViewLifecycleOwner(), historyConnectStatus -> {
            if (historyConnectStatus.getConnectStatus() == StateCode.CONNECTION_CONNECTING) {
                showWaitDialog(true);
            } else {
                dismissWaitDialog();
                if (historyConnectStatus.getConnectStatus() == StateCode.CONNECTION_OK) {
                    showTips(R.string.history_connect_ok);
                    requireActivity().finish();
                } else {
                    showTips(R.string.history_connect_failed);
                }
            }
        });
        mViewModel.mWatchProductMsgResultMLD.observe(getViewLifecycleOwner(), watchProductMsgResult -> {
            if (watchProductMsgResult.isOk()) {
                updateImageView(HealthApplication.getAppViewModel().getApplication(), mBinding.ivHistoryDeviceProduct, watchProductMsgResult.getProduct().getIcon());
            } else {
                if (!NetworkStateHelper.getInstance().getNetWorkStateModel().isAvailable()) {
                    showTips(getString(R.string.tip_check_net));
                } else {
                    JL_Log.e(tag, "WatchProductMsgResultMLD", "request message error: " + watchProductMsgResult.getMessage());
                }
            }
        });

        updateReconnectUI(mViewModel.isConnectedDevice(HealthUtil.getRemoteDevice(record.getHistoryRecord().getAddress())));
        mViewModel.getHistoryProductMsg();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HistoryRecordFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroyView() {
        mUIHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        mViewModel.release();
    }

    public void checkPermissionAllow(int action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !ConnectUtil.isHasConnectPermission(requireContext())) {
            showPermissionDialog(Manifest.permission.BLUETOOTH_CONNECT, (permission ->
                    HistoryRecordFragmentPermissionsDispatcher.requestBtPermissionWithPermissionCheck(this, action)));
            return;
        }
        if (!BluetoothUtil.isBluetoothEnable()) {
            openBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }
        if (action == ACTION_RECONNECT) {
            showPermissionDialog(Manifest.permission.ACCESS_COARSE_LOCATION, (permission ->
                    HistoryRecordFragmentPermissionsDispatcher.reconnectHistoryWithPermissionCheck(this)));
        } else if (action == ACTION_DELETE) {
            showWaitDialog(true);
            mViewModel.removeHistory();
        }
        this.action = 0;
    }

    private void updateReconnectUI(boolean isConnected) {
        mBinding.tvHistoryDeviceReconnect.setVisibility(isConnected ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateImageView(Context context, ImageView imageView, String url) {
        if (null == url) {
            imageView.setImageResource(R.drawable.ic_watch_big);
            return;
        }
        JL_Log.d(tag, "updateImageView", "url = " + url);
        boolean isGif = url.endsWith(".gif");
        if (isGif) {
            Glide.with(context).asGif().load(url)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.drawable.ic_watch_big)
                    .into(imageView);
        } else {
            Glide.with(context).asBitmap().load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.ic_watch_big)
                    .into(imageView);
        }
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void reconnectHistory() {
        if (!PermissionUtil.checkGpsProviderEnable(getContext())) {
            showOpenGPSDialog();
            return;
        }
        mViewModel.reconnectHistory(mViewModel.historyRecord);
    }

    @OnShowRationale({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void showRelationForLocationPermission(PermissionRequest request) {
        showRequireGPSPermissionDialog(request);
    }

    @OnPermissionDenied({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void onLocationDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @OnNeverAskAgain({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void onLocationNeverAskAgain() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @NeedsPermission({Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public void requestBtPermission(int action) {
        checkPermissionAllow(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnShowRationale({Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public void showRelationForBtPermission(PermissionRequest request) {
        request.proceed();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnPermissionDenied({Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public void onBtDenied() {
        showTips(CalendarUtil.formatString("%s%s%s", getString(R.string.permissions_tips_02),
                getString(R.string.permission_bluetooth), getString(R.string.permission)));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnNeverAskAgain({Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public void onBtNeverAskAgain() {
        showTips(CalendarUtil.formatString("%s%s%s", getString(R.string.permissions_tips_02),
                getString(R.string.permission_bluetooth), getString(R.string.permission)));
    }

    private void showOpenGPSDialog() {
        showGPSDialog(null, true);
    }

    private void showRequireGPSPermissionDialog(PermissionRequest request) {
        showGPSDialog(request, false);
    }

    private void showGPSDialog(PermissionRequest request, boolean isLocationService) {
        RequireGPSDialog requireGPSDialog = new RequireGPSDialog(RequireGPSDialog.VIEW_TYPE_DEVICE, request);
        requireGPSDialog.setLocationService(isLocationService);
        requireGPSDialog.setCancelable(true);
        requireGPSDialog.show(getChildFragmentManager(), RequireGPSDialog.class.getCanonicalName());
    }
}