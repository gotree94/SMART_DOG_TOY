package com.jieli.watchtesttool.ui.device;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.ScanDevice;
import com.jieli.watchtesttool.databinding.FragmentAddDeviceBinding;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.ui.widget.CommonDecoration;
import com.jieli.watchtesttool.ui.widget.dialog.WaitingDialog;

import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 添加设备界面
 */
@RuntimePermissions
public class AddDeviceFragment extends BaseFragment {

    private AddDeviceViewModel mViewModel;
    private FragmentAddDeviceBinding mBinding;

    private ScanDeviceAdapter mScanDeviceAdapter;

    private WaitingDialog mWaitingDialog;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAddDeviceBinding.inflate(inflater, container, false);
        mBinding.srlAddDeviceRefresh.setColorSchemeColors(getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.darker_gray),
                getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.background_light));
        mBinding.srlAddDeviceRefresh.setProgressBackgroundColorSchemeColor(Color.WHITE);
        mBinding.srlAddDeviceRefresh.setSize(SwipeRefreshLayout.DEFAULT);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this, new AddDeviceViewModel.Factory(requireContext())).get(AddDeviceViewModel.class);
        initUI();
        observeCallback();
        mBinding.pbAddDeviceScanStatus.setVisibility(mViewModel.isScanning() ? View.VISIBLE : View.INVISIBLE);
        if (!ConnectUtil.isHasLocationPermission(requireContext())) {
            AddDeviceFragmentPermissionsDispatcher.checkAppRequestPermissionsWithPermissionCheck(this);
        } else {
            scanDevice(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mViewModel.isScanning()) {
            mViewModel.stopScan();
        }
    }

    @Override
    public void onDestroy() {
        dismissWaitingDialog();
        super.onDestroy();
        mViewModel.destroy();
        mBinding = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AddDeviceFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    public void checkAppRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AddDeviceFragmentPermissionsDispatcher.requestBluetoothPermissionWithPermissionCheck(this);
        } else {
            scanDevice(true);
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
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @NeedsPermission({
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @RequiresApi(Build.VERSION_CODES.S)
    public void requestBluetoothPermission() {
        scanDevice(true);
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
        mBinding.viewAddDeviceTopbar.tvTopbarTitle.setText(R.string.add_device);
        mBinding.viewAddDeviceTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.srlAddDeviceRefresh.setOnRefreshListener(() -> {
            scanDevice(true);
            mHandler.postDelayed(() -> mBinding.srlAddDeviceRefresh.setRefreshing(false), 800);
        });
        mBinding.rvAddDeviceList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvAddDeviceList.addItemDecoration(new CommonDecoration(requireContext(), RecyclerView.VERTICAL));
        mScanDeviceAdapter = new ScanDeviceAdapter();
        mBinding.rvAddDeviceList.setAdapter(mScanDeviceAdapter);
        mScanDeviceAdapter.setOnItemClickListener((adapter, view, position) -> {
            ScanDevice scanDevice = mScanDeviceAdapter.getItem(position);
            if (scanDevice == null) return;
            if (scanDevice.getConnectStatus() == StateCode.CONNECTION_DISCONNECT) {
                mViewModel.stopScan();
                mViewModel.connectDevice(scanDevice.getDevice(), scanDevice.getBleScanMessage());
            } else if (scanDevice.getConnectStatus() == StateCode.CONNECTION_OK) {
                mViewModel.disconnectDevice(scanDevice.getDevice());
            }
        });
    }

    private void observeCallback() {
        mViewModel.mBtAdapterStatusMLD.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                resetScanDeviceList();
            } else if (!mViewModel.isScanning()) {
                scanDevice(false);
            }
        });
        mViewModel.mScanStatusMLD.observe(getViewLifecycleOwner(), aBoolean -> requireActivity().runOnUiThread(() -> {
            JL_Log.i(tag, "mScanStatusMLD >> " + aBoolean);
            if (aBoolean) {
                mBinding.pbAddDeviceScanStatus.setVisibility(View.VISIBLE);
                resetScanDeviceList();
            } else {
                mBinding.pbAddDeviceScanStatus.setVisibility(View.INVISIBLE);
            }
        }));
        mViewModel.mScanDeviceMLD.observe(getViewLifecycleOwner(), this::updateScanDeviceList);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            JL_Log.e(tag, ">>>>>>>>>>>>>>>>>>>>>" + deviceConnectionData.getDevice() + ", " + deviceConnectionData.getStatus());
            updateScanDevice(deviceConnectionData.getDevice(), deviceConnectionData.getStatus());
            if (deviceConnectionData.getStatus() == StateCode.CONNECTION_CONNECTING) {
                showWaitingDialog();
            } else {
                dismissWaitingDialog();
                scanDevice(false);
            }
        });
    }

    private void scanDevice(boolean isUser) {
        if (ConnectUtil.isHasLocationPermission(requireContext())) {
            if (BluetoothUtil.isBluetoothEnable()) {
                if (!TextUtils.isEmpty(mBinding.etAddDeviceFilter.getText().toString().trim())) {
                    mViewModel.changeFilter(mBinding.etAddDeviceFilter.getText().toString().trim());
                }
                mViewModel.startScan();
            } else if (isUser) {
                BluetoothUtil.enableBluetooth(requireContext());
            }
        } else if (isUser) {
            AddDeviceFragmentPermissionsDispatcher.checkAppRequestPermissionsWithPermissionCheck(this);
        }
    }

    private void updateScanDeviceList(final ScanDevice scanDevice) {
        if (null == mScanDeviceAdapter || null == scanDevice || isDetached() || !isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            BluetoothDevice device = scanDevice.getDevice();
            JL_Log.d(tag, "updateScanDeviceList >> " + device);
            int status = mViewModel.getDeviceConnection(device);
            ScanDevice item = mScanDeviceAdapter.getItemByDevice(device);
            if (null != item) {
                item.setConnectStatus(status);
                mScanDeviceAdapter.notifyItemChanged(mScanDeviceAdapter.getItemPosition(item));
            } else {
                scanDevice.setConnectStatus(status);
                mScanDeviceAdapter.addData(scanDevice);
            }
        });
    }

    private void resetScanDeviceList() {
        if (mScanDeviceAdapter == null || isDetached() || !isAdded()) return;
        mScanDeviceAdapter.setNewInstance(new ArrayList<>());
    }

    private void updateScanDevice(BluetoothDevice device, int status) {
        if (mScanDeviceAdapter == null) return;
        mScanDeviceAdapter.updateScanDeviceConnectStatus(device, status);
    }

    private void showWaitingDialog() {
        if (isDetached() || !isAdded()) return;
        if (null == mWaitingDialog) {
            mWaitingDialog = new WaitingDialog();
        }
        if (!mWaitingDialog.isShow()) {
            mWaitingDialog.show(getChildFragmentManager(), WaitingDialog.class.getSimpleName());
        }
    }

    private void dismissWaitingDialog() {
        if (isDetached() || !isAdded()) return;
        if (null != mWaitingDialog) {
            if (mWaitingDialog.isShow()) mWaitingDialog.dismiss();
            mWaitingDialog = null;
        }
    }
}