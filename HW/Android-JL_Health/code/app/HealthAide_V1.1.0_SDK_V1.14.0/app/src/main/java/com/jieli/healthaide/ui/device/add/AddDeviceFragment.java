package com.jieli.healthaide.ui.device.add;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentAddDeviceBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.adapter.ScanDeviceAdapter;
import com.jieli.healthaide.ui.device.bean.ScanDevice;
import com.jieli.healthaide.ui.dialog.RequireGPSDialog;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 添加设备界面
 */
@RuntimePermissions
public class AddDeviceFragment extends BaseFragment {
    private DeviceConnectViewModel mViewModel;
    private FragmentAddDeviceBinding mAddDeviceBinding;

    private ScanDeviceAdapter mScanDeviceAdapter;

    private TextView tvSearchTips;
    private AnimationDrawable mSearchingAnim;
    private Jl_Dialog mTipDialog;

    private long startScanTime;
    private boolean isUserNeverAskAgain = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final int REQUEST_CODE_CONNECT_DEVICE = 0x96AB;

    private final ActivityResultLauncher<String[]> mBtPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean isGrant;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isGrant = result.get(Manifest.permission.BLUETOOTH_CONNECT);
            if (isGrant == Boolean.TRUE) {
                isGrant = result.get(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (null == isGrant) return;
            if (isGrant == Boolean.TRUE) {
                //继续开始搜索蓝牙设备
                tryToScanDevice();
            } else {
                //提示权限未被授予
                showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
            }
        }
    });

    public static AddDeviceFragment newInstance() {
        return new AddDeviceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAddDeviceBinding = FragmentAddDeviceBinding.inflate(inflater, container, false);
        return mAddDeviceBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(DeviceConnectViewModel.class);
        initUI();
        addObserver();
        tryToScanDevice();
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
        controlScanAnim(false);
        super.onDestroy();
        mViewModel.stopScan();
        mAddDeviceBinding = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONNECT_DEVICE) {
            requireActivity().finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        JL_Log.d(tag, "onRequestPermissionsResult", "requestCode : " + requestCode);
        AddDeviceFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void checkLocationPermission() {
        JL_Log.d(tag, "checkLocationPermission", "");
        if (!PermissionUtil.checkGpsProviderEnable(getContext())) {
            showOpenGPSDialog();
        } else {
            scanDevice(true);
        }
    }

    @NeedsPermission({
            Manifest.permission.CAMERA,
    })
    public void toQrScanFragment() {
        startActivity(new Intent(requireContext(), QRCodeScanActivity.class));
        finish();
    }

    @OnShowRationale({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void showRelationForLocationPermission(PermissionRequest request) {
        showRequireGPSPermissionDialog(request);
        isUserNeverAskAgain = true;
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
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showRequireGPSPermissionDialog(null);
        }
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

    private void initUI() {
        mAddDeviceBinding.clAddDeviceTopbar.tvTopbarTitle.setText(R.string.add_device);
        mAddDeviceBinding.clAddDeviceTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mAddDeviceBinding.rcAddDeviceList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mScanDeviceAdapter = new ScanDeviceAdapter();
        mScanDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            ScanDevice scanDevice = mScanDeviceAdapter.getItem(position);
            if (scanDevice == null) return;
            if (scanDevice.getConnectStatus() == BluetoothConstant.CONNECT_STATE_DISCONNECT) {
//                mViewModel.connectDevice(scanDevice.getDevice(), scanDevice.getBleScanMessage());
                tryToConnectDevice(scanDevice);
            }
        });
        mScanDeviceAdapter.setOnItemClickListener((adapter, view, position) -> {
            ScanDevice scanDevice = mScanDeviceAdapter.getItem(position);
            if (scanDevice == null) return;
            if (scanDevice.getConnectStatus() == BluetoothConstant.CONNECT_STATE_CONNECTED) {
                mViewModel.disconnectDevice(scanDevice.getDevice());
            }
        });
        View searchingView = LayoutInflater.from(requireContext()).inflate(R.layout.view_searching, null);
        tvSearchTips = searchingView.findViewById(R.id.tv_searching_tips);
        ImageView ivAnimSearching = searchingView.findViewById(R.id.iv_searching_anim);
        mSearchingAnim = (AnimationDrawable) ivAnimSearching.getDrawable();
        TextView tvNoFoundDev = searchingView.findViewById(R.id.tv_searching_no_found_device);
        String text = getString(R.string.no_found_device_tips);
        String scanQR = getString(R.string.scan_qr_code);
        SpannableString span = getSpannable(text, "####", scanQR, new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showPermissionDialog(Manifest.permission.CAMERA, (permission) ->
                        AddDeviceFragmentPermissionsDispatcher.toQrScanFragmentWithPermissionCheck(AddDeviceFragment.this));
            }
        });
        if (null != span) {
            tvNoFoundDev.append(span);
        }
        tvNoFoundDev.setMovementMethod(LinkMovementMethod.getInstance());
        tvNoFoundDev.setLongClickable(false);
        mScanDeviceAdapter.setEmptyView(searchingView);
        mAddDeviceBinding.rcAddDeviceList.setAdapter(mScanDeviceAdapter);
        String notFoundTarget = getString(R.string.not_found_target);
        String rescan = getString(R.string.rescan_device);
        SpannableString span1 = getSpannable(notFoundTarget, "####", rescan, new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                tryToScanDevice();
            }
        });
        if (null != span1) {
            mAddDeviceBinding.tvAddDeviceRescan.append(span1);
        }
        mAddDeviceBinding.tvAddDeviceRescan.setMovementMethod(LinkMovementMethod.getInstance());
        mAddDeviceBinding.tvAddDeviceRescan.setLongClickable(false);
        mAddDeviceBinding.tvAddDeviceRescan.setVisibility(View.GONE);
        tvSearchTips.setText(BluetoothUtil.isBluetoothEnable() ? "" : getString(R.string.bluetooth_is_close));
    }

    private void addObserver() {
        mViewModel.mBtAdapterStatusMLD.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                tvSearchTips.setText(getString(R.string.bluetooth_is_close));
                controlScanAnim(false);
                resetScanDeviceList();
            } else if (!mViewModel.isScanning()) {
                scanDevice(false);
            }
        });
        mViewModel.mScanStatusMLD.observe(getViewLifecycleOwner(), aBoolean -> mHandler.post(() -> {
            controlScanAnim(aBoolean);
            if (aBoolean) {
                tvSearchTips.setText(getString(R.string.device_searching));
                resetScanDeviceList();
                startScanTime = Calendar.getInstance().getTimeInMillis();
            } else {
                if (startScanTime > 0) {
                    if (mScanDeviceAdapter != null && mScanDeviceAdapter.getData().isEmpty()) {
                        long current = Calendar.getInstance().getTimeInMillis();
                        long leftTime = current - startScanTime;
                        if (leftTime > DeviceConnectViewModel.SCAN_DEVICE_TIMEOUT / 6) {
                            showTipsDialog();
                        } else {
                            scanDevice(false);
                        }
                    }
                    startScanTime = 0;
                }
            }
        }));
        mViewModel.mScanDeviceMLD.observe(getViewLifecycleOwner(), this::updateScanDeviceList);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            updateScanDevice(deviceConnectionData.getDevice(), deviceConnectionData.getStatus());
            if (deviceConnectionData.getStatus() == StateCode.CONNECTION_CONNECTING) {
                showWaitDialog();
            } else {
                dismissWaitDialog();
                scanDevice(false);
            }
        });
    }

    private void tryToScanDevice() {
        if (ConnectUtil.isHasScanPermission(requireContext())
                && ConnectUtil.isHasConnectPermission(requireContext())
                && PermissionUtil.isHasLocationPermission(requireContext())
                && PermissionUtil.checkGpsProviderEnable(requireContext())) {
            scanDevice(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (!ConnectUtil.isHasScanPermission(requireContext()) || !ConnectUtil.isHasConnectPermission(requireContext()))) {
                showPermissionDialog(Manifest.permission.BLUETOOTH_CONNECT, ((permission) ->
                        mBtPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})));
            } else {
                showPermissionDialog(Manifest.permission.ACCESS_COARSE_LOCATION, (permission) ->
                        AddDeviceFragmentPermissionsDispatcher.checkLocationPermissionWithPermissionCheck(AddDeviceFragment.this));
            }
        }
    }

    private void tryToConnectDevice(ScanDevice scanDevice) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ConnectFragment.KEY_CONNECT_DEV, scanDevice.getDevice());
        bundle.putParcelable(ConnectFragment.KEY_CONNECT_BLE_MSG, scanDevice.getBleScanMessage());
        ContentActivity.startContentActivityForResult(this, ConnectFragment.class.getCanonicalName(), bundle, REQUEST_CODE_CONNECT_DEVICE);
    }

    private void updateScanDeviceList(final ScanDevice scanDevice) {
        if (null == mScanDeviceAdapter || null == scanDevice || isDetached() || !isAdded())
            return;
        mHandler.post(() -> {
            BluetoothDevice device = scanDevice.getDevice();
            int status = mViewModel.getDeviceConnection(device);
            ScanDevice item = mScanDeviceAdapter.getItemByDevice(device);
            if (null != item) {
                item.setConnectStatus(status);
                mScanDeviceAdapter.notifyItemChanged(mScanDeviceAdapter.getItemPosition(item));
            } else {
                scanDevice.setConnectStatus(status);
                mScanDeviceAdapter.addData(scanDevice);
            }
            if (!mScanDeviceAdapter.getData().isEmpty() && mAddDeviceBinding.tvAddDeviceRescan.getVisibility() != View.VISIBLE) {
                mAddDeviceBinding.tvAddDeviceRescan.setVisibility(View.VISIBLE);
            }
        });
    }

    private void resetScanDeviceList() {
        if (mScanDeviceAdapter == null || !isFragmentValid()) return;
        mScanDeviceAdapter.setNewInstance(new ArrayList<>());
        mAddDeviceBinding.tvAddDeviceRescan.setVisibility(View.GONE);
    }

    private void updateScanDevice(BluetoothDevice device, int status) {
        if (mScanDeviceAdapter == null || !isFragmentValid()) return;
        mScanDeviceAdapter.updateScanDeviceConnectStatus(device, status);
    }

    private void scanDevice(boolean isUser) {
        if (BluetoothUtil.isBluetoothEnable()) {
            if (mViewModel.isScanning() && isUser) {
                mViewModel.stopScan();
            }
            if (!mViewModel.scanDevice()) {
                tvSearchTips.setText(getString(R.string.start_scan_failed));
                controlScanAnim(false);
                resetScanDeviceList();
            }
        } else if (isUser) {
            BluetoothUtil.enableBluetooth(requireContext());
        }
    }

    private void controlScanAnim(boolean isStart) {
        if (mSearchingAnim == null || !isFragmentValid()) return;
        if (isStart) {
            if (!mSearchingAnim.isRunning()) {
                mSearchingAnim.start();
            }
        } else {
            if (mSearchingAnim.isRunning()) {
                mSearchingAnim.stop();
                mSearchingAnim.selectDrawable(0);
            }
        }
    }

    private void showTipsDialog() {
        if (!isFragmentValid()) return;
        if (mTipDialog == null) {
            mTipDialog = Jl_Dialog.builder()
                    .width(0.8f)
                    .cancel(false)
                    .content(getString(R.string.no_found_device_tips_2))
                    .contentColor(getResources().getColor(R.color.black_242424))
                    .left(getString(R.string.retry))
                    .leftColor(getResources().getColor(R.color.blue_558CFF))
                    .leftClickListener(((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        mTipDialog = null;
                        tryToScanDevice();
                    }))
                    .right(getString(R.string.no_binding))
                    .rightColor(getResources().getColor(R.color.blue_558CFF))
                    .rightClickListener(((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        mTipDialog = null;
                        requireActivity().finish();
                    }))
                    .build();
        }
        if (!mTipDialog.isShow()) {
            mTipDialog.show(getChildFragmentManager(), "tips_dialog");
        }
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

    private void showCameraDialog(PermissionRequest request) {
        showPermissionDialog(Manifest.permission.CAMERA, request, null);
    }

    private SpannableString getSpannable(String src, String flag, String replace, ClickableSpan click) {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(flag) || TextUtils.isEmpty(replace))
            return null;
        JL_Log.d(tag, "getSpannable", "src : " + src + ", flag = " + flag + ", replace = " + replace);
        int startPos = src.indexOf(flag);
        if (startPos == -1) return null;
        int endPos = startPos + replace.length();
        src = src.replace(flag, replace);
        if (endPos > src.length()) return null;
        JL_Log.d(tag, "getSpannable", "startPos = " + startPos + ", endPos = " + endPos);
        SpannableString span = new SpannableString(src);
        span.setSpan(click, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.auxiliary_widget)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }
}