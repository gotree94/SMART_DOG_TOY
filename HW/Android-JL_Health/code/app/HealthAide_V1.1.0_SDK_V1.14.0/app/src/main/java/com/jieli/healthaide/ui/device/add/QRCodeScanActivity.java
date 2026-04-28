package com.jieli.healthaide.ui.device.add;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jieli.bluetooth_connect.bean.ble.BleScanMessage;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.BluetoothUtil;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ActivityQrcodeScanBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.device.bean.DeviceQrMsg;
import com.jieli.healthaide.ui.dialog.PermissionDialog;
import com.jieli.healthaide.ui.dialog.RequireGPSDialog;
import com.jieli.healthaide.util.PermissionUtil;
import com.jieli.jl_rcsp.constant.JL_DeviceType;
import com.jieli.jl_rcsp.util.JL_Log;
import com.king.camera.scan.AnalyzeResult;
import com.king.camera.scan.CameraScan;
import com.king.camera.scan.analyze.Analyzer;
import com.king.wechat.qrcode.WeChatQRCodeDetector;
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity;
import com.king.wechat.qrcode.scanning.analyze.WeChatScanningAnalyzer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

/**
 * QRCodeScanActivity
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 扫码界面
 * @since 2025/10/25
 */
@RuntimePermissions
public class QRCodeScanActivity extends WeChatCameraScanActivity {

    private final String tag = getClass().getSimpleName();

    private ActivityQrcodeScanBinding mBinding;

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private final Gson gson = new GsonBuilder().setLenient().create();

    private DeviceQrMsg deviceQrMsg;

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result != null && result.getResultCode() == Activity.RESULT_OK) {
                    parsePhoto(result.getData());
                }
            });
    private final ActivityResultLauncher<Intent> openBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result != null && result.getResultCode() == Activity.RESULT_OK && BluetoothUtil.isBluetoothEnable()) {
                    tryToConnectDevice(deviceQrMsg);
                }
            });

    @Override
    public void initUI() {
        EdgeToEdge.enable(this);
        mBinding = ActivityQrcodeScanBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
        super.initUI();
        initTopBar();
        mBinding.ivFlashLight.setOnClickListener(v -> {
            toggleTorchState();
            updateLightUI(isLightOn());
        });
        mBinding.tvSelectFromGallery.setOnClickListener(v -> tryToSelectPhoto());
        mBinding.tvManualPairDevice.setOnClickListener(v -> tryToScanDevice());

        updateLightUI(isLightOn());
    }

    @Override
    public void initCameraScan(@NonNull CameraScan<List<String>> cameraScan) {
        super.initCameraScan(cameraScan);
        cameraScan.setPlayBeep(true);
    }

    @Nullable
    @Override
    public Analyzer<List<String>> createAnalyzer() {
        // 如果需要返回结果二维码位置信息，则初始化分析器时，isOutputVertices参数传 true 即可
        return new WeChatScanningAnalyzer(true);
    }

    @Override
    public boolean isContentView() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_qrcode_scan;
    }

    @Override
    public int getFlashlightId() {
        return View.NO_ID;
    }

    @Override
    public void onScanResultCallback(@NonNull AnalyzeResult<List<String>> result) {
        //停止分析
        getCameraScan().setAnalyzeImage(false);

        String text = result.getResult().get(0);
        handleResult(text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        QRCodeScanActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    public void requestStoragePermission() {
        selectImageLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE,})
    public void showRelationForStoragePermission(PermissionRequest request) {
        if (null != request) request.proceed();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE,})
    public void onStoragePermissionDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.READ_MEDIA_IMAGES})
    public void requestStoragePermissionBy33() {
        selectImageLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({Manifest.permission.READ_MEDIA_IMAGES,})
    public void showRelationForStoragePermissionBy33(PermissionRequest request) {
        if (null != request) request.proceed();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.READ_MEDIA_IMAGES,})
    public void onStoragePermissionDeniedBy33() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void requestLocationPermission() {
        JL_Log.d(tag, "requestLocationPermission", "");
        tryToConnectDevice(deviceQrMsg);
    }

    @OnShowRationale({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void showRelationForLocationPermission(PermissionRequest request) {
        if (null != request) request.proceed();
    }

    @OnPermissionDenied({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    })
    public void onLocationPermissionDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @NeedsPermission({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public void requestBTPermission() {
        tryToConnectDevice(deviceQrMsg);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnShowRationale({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public void showRelationForBTPermission(PermissionRequest request) {
        if (null != request) request.proceed();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @OnPermissionDenied({Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public void onBTPermissionDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
        finish();
    }

    private boolean isLightOn() {
        return getCameraScan().isTorchEnabled();
    }

    private void initTopBar() {
        mBinding.viewTopBar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_back_white, 0, 0, 0);
        mBinding.viewTopBar.tvTopbarLeft.setOnClickListener(v -> finish());
        mBinding.viewTopBar.tvTopbarTitle.setText(getString(R.string.scan_qr_code_title));
        mBinding.viewTopBar.tvTopbarTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void updateLightUI(boolean isOn) {
        mBinding.ivFlashLight.setImageResource(isOn ? R.drawable.ic_scan_light_white : R.drawable.ic_scan_light_gray);
    }

    private void asyncThread(Runnable runnable) {
        if (null == runnable || threadPool.isShutdown()) return;
        threadPool.execute(runnable);
    }

    protected void showTips(int resId) {
        showTips(getString(resId));
    }

    protected void showTips(String content) {
        JL_Log.d(tag, "showTips", content);
        ToastUtil.showToastShort(content);
    }

    private void showPermissionDialog(String permission, PermissionDialog.OnPermissionClickListener listener) {
        showPermissionDialog(permission, null, listener);
    }

    private void showPermissionDialog(String permission, PermissionRequest request, PermissionDialog.OnPermissionClickListener listener) {
        if (null == permission || isDestroyed() || isFinishing()) return;
        if (PermissionUtils.hasSelfPermissions(this, permission)) {
            if (null != listener) listener.onRequest(permission);
            return;
        }
        PermissionDialog permissionDialog = new PermissionDialog(permission, request, listener);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getSupportFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

    private void tryToSelectPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            showPermissionDialog(Manifest.permission.READ_MEDIA_IMAGES, (permission ->
                    QRCodeScanActivityPermissionsDispatcher.requestStoragePermissionBy33WithPermissionCheck(this)));
            return;
        }
        showPermissionDialog(Manifest.permission.READ_EXTERNAL_STORAGE, (permission ->
                QRCodeScanActivityPermissionsDispatcher.requestStoragePermissionWithPermissionCheck(this)));
    }

    private void tryToScanDevice() {
        ContentActivity.startContentActivity(this, AddDeviceFragment.class.getCanonicalName());
        finish();
    }

    private void parsePhoto(Intent data) {
        if (null == data) return;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            if (null != bitmap) {
                asyncThread(() -> {
                    final List<String> result = WeChatQRCodeDetector.detectAndDecode(bitmap);
                    runOnUiThread(() -> {
                        String text = result.isEmpty() ? "" : result.get(0);
                        handleResult(text);
                    });
                });
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        showTips(getString(R.string.not_found_qr));
    }

    private void handleResult(String text) {
        JL_Log.d(tag, "handleResult", text);
        DeviceQrMsg message = null;
        try {
            message = gson.fromJson(text, DeviceQrMsg.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == message) {
            showTips(text);
            //继续分析
            getCameraScan().setAnalyzeImage(true);
            return;
        }
        this.deviceQrMsg = message;
        tryToConnectDevice(message);
    }

    private void tryToConnectDevice(DeviceQrMsg deviceQrMsg) {
        if (null == deviceQrMsg) return;
        if (!ConnectUtil.isHasConnectPermission(this) || !ConnectUtil.isHasScanPermission(this)) { //缺少连接蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                showPermissionDialog(Manifest.permission.BLUETOOTH_CONNECT, (permission ->
                        QRCodeScanActivityPermissionsDispatcher.requestBTPermissionWithPermissionCheck(this)));
            }
            return;
        }
        if (!PermissionUtil.isHasLocationPermission(this)) { //缺少位置权限
            showPermissionDialog(Manifest.permission.ACCESS_COARSE_LOCATION, (permission ->
                    QRCodeScanActivityPermissionsDispatcher.requestLocationPermissionWithPermissionCheck(this)));
            return;
        }
        if (!PermissionUtil.checkGpsProviderEnable(this)) { //位置服务未打开
            showGPSTipsDialog();
            return;
        }
        if (!BluetoothUtil.isBluetoothEnable()) { //蓝牙未打开
            openBtLauncher.launch(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return;
        }

        BluetoothDevice device = BluetoothUtil.getRemoteDevice(this, deviceQrMsg.getConnectWay() == BluetoothConstant.PROTOCOL_TYPE_SPP ?
                deviceQrMsg.getEdrAddr() : deviceQrMsg.getBleAddr());
        if (null == device) {
            showTips(getString(R.string.msg_read_file_err_offline));
            //继续分析
            getCameraScan().setAnalyzeImage(true);
            return;
        }
        BleScanMessage scanMessage = new BleScanMessage(device.getAddress())
                .setDeviceType(JL_DeviceType.JL_DEVICE_TYPE_WATCH)
                .setVersion(1) //目前手表都是关闭SPP的
                .setEdrAddr(deviceQrMsg.getEdrAddr())
                .setConnectWay(deviceQrMsg.getConnectWay())
                .setPid(deviceQrMsg.getPid())
                .setUid(deviceQrMsg.getVid());
        Bundle bundle = new Bundle();
        bundle.putParcelable(ConnectFragment.KEY_CONNECT_DEV, device);
        bundle.putParcelable(ConnectFragment.KEY_CONNECT_BLE_MSG, scanMessage);
        ContentActivity.startContentActivity(this, ConnectFragment.class.getCanonicalName(), bundle);
        finish();
    }

    private void showGPSTipsDialog() {
        RequireGPSDialog requireGPSDialog = new RequireGPSDialog(RequireGPSDialog.VIEW_TYPE_DEVICE, null);
        requireGPSDialog.setLocationService(true);
        requireGPSDialog.setCancelable(true);
        requireGPSDialog.show(getSupportFragmentManager(), RequireGPSDialog.class.getCanonicalName());
    }
}