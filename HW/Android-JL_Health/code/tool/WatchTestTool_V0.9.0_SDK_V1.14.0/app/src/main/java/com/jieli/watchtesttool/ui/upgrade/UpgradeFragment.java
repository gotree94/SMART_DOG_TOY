package com.jieli.watchtesttool.ui.upgrade;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.FragmentUpgradeBinding;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.util.AppUtil;
import com.jieli.watchtesttool.util.FileUtil;
import com.jieli.watchtesttool.util.WatchTestConstant;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.io.File;
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
 * OTA升级界面
 */
@RuntimePermissions
public class UpgradeFragment extends BaseFragment {

    public final static int OTA_FLAG_NORMAL = 0;
    public final static int OTA_FLAG_FIRMWARE = 1;
    public final static int OTA_FLAG_RESOURCE = 2;
    public final static int OTA_FLAG_NETWORK = 3;

    private FragmentUpgradeBinding mBinding;
    //    private UpgradeViewModel mViewModel;
    private UpdateViewModel mViewModel;

    private FileSelectorAdapter mAdapter;

    /**
     * 线程池
     */
    private final ExecutorService threadTool = Executors.newSingleThreadExecutor();
    /**
     * UI处理器
     */
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> openFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK || null == result.getData()
                        || threadTool.isShutdown()) {
                    return;
                }
                final Uri uri = result.getData().getData();
                if (null == uri) return;
                JL_Log.d(tag, "openFileLauncher", "uri : " + uri);
                threadTool.submit(() -> {
                    File file = FileUtil.copyFileFromUri(requireContext(), uri,
                            AppUtil.createFilePath(requireContext(), WatchTestConstant.DIR_UPDATE),
                            ".ufw", ".zip");
                    if (null == file || !file.exists() || !file.isFile()) {
                        JL_Log.i(tag, "openFileLauncher", "Invalid file : " + file);
                        showTips(R.string.invalid_file);
                        return;
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mViewModel.readUpgradeFileList();
                        }
                    });
                });
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentUpgradeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        mViewModel = new ViewModelProvider(this, new UpgradeViewModel.UpgradeViewModelFactory(requireContext())).get(UpgradeViewModel.class);
        mViewModel = new ViewModelProvider(this, new UpdateViewModel.UpdateViewModelFactory(requireContext())).get(UpdateViewModel.class);
        initUI();
        observeCallback();

        mViewModel.readUpgradeFileList();
    }

    @Override
    public void onDestroy() {
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        threadTool.shutdownNow();
        mViewModel.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UpgradeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(value = {Manifest.permission.READ_EXTERNAL_STORAGE})
    public void readStoragePermissionAllow() {
        dismissPermissionTipsDialog();
        openFileChooser();
    }

    @OnShowRationale(value = {Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onReadStoragePermissionShowRationale(PermissionRequest request) {
        if (null != request) request.proceed();
    }

    @OnPermissionDenied(value = {Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onReadStoragePermissionDenied() {
        dismissPermissionTipsDialog();
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    private void initUI() {
        mBinding.clUpgradeTopbar.tvTopbarTitle.setText(getString(R.string.ota_upgrade));
        mBinding.clUpgradeTopbar.tvTopbarLeft.setOnClickListener(v -> {
            if (mViewModel.isDevOta()) {
                showTips(getString(R.string.ota_upgrading_tips));
                return;
            }
            requireActivity().finish();
        });
        mBinding.btnOtaUpgrade.setOnClickListener(v -> {
            List<String> otaFilePaths = mAdapter.getSelectFilePaths();
            if (otaFilePaths == null || otaFilePaths.isEmpty()) {
                showTips(getString(R.string.choose_ota_file));
                return;
            }
            int loop = mViewModel.isAutoTest() ? getAutoTestLoop() : 1;
//            mViewModel.startOTA(otaFilePaths.get(0));
            mViewModel.startAutoOTA(UpgradeFragment.this, otaFilePaths, loop);
            updateOtaBtn(false, true);
        });
        mBinding.ivAddFile.setOnClickListener(v -> tryToSelectFile());
        mAdapter = new FileSelectorAdapter(mViewModel.isAutoTest());
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            File item = mAdapter.getItem(position);
            if (null == item) return;
            mAdapter.selectFile(position, item.getPath());
        });
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            File item = mAdapter.getItem(position);
            if (null == item) return;
            if (FileUtil.deleteFile(item)) {
                mAdapter.remove(item);
            }
            View rootView = mAdapter.getViewByPosition(position, R.id.swipe_root);
            if (rootView instanceof SwipeMenuLayout) {
                ((SwipeMenuLayout) rootView).quickClose();
            }
        });
        mBinding.upgradeFileSelectorView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.upgradeFileSelectorView.setAdapter(mAdapter);
        View emptyView = LayoutInflater.from(requireContext()).inflate(R.layout.view_file_empty, null);
        TextView tvTips = emptyView.findViewById(R.id.tv_file_empty_tips);
        tvTips.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvTips.setLongClickable(false);
        tvTips.setOnClickListener(v -> tryToSelectFile());
        mAdapter.setEmptyView(emptyView);

        mBinding.clAutoTest.setVisibility(mViewModel.isAutoTest() ? View.VISIBLE : View.INVISIBLE);
        mBinding.etAutoTestLoop.setText("1");

        updateOtaBtn(mViewModel.isInitOK(), true);
    }

    private void observeCallback() {
        mViewModel.mOtaFileListMLD.observe(getViewLifecycleOwner(), files -> {
            if (isAdded() && !isDetached()) {
//                mAdapter.resetSelectedIndex();
                mAdapter.setList(files);
            }
        });
        mViewModel.mOtaInitMLD.observe(getViewLifecycleOwner(), isInit -> {
            JL_Log.d(tag, "-isInit- " + isInit);
            if (isInit && !mViewModel.isDevOta()) {
                mBinding.tvOtaMessage.setText(getString(R.string.ota_upgrade_not_started));
            }
            updateOtaBtn(isInit, true);
        });
        mViewModel.mOtaStateMLD.observe(getViewLifecycleOwner(), otaState -> requireActivity().runOnUiThread(() -> {
            JL_Log.d(tag, "-observe- " + otaState);
            switch (otaState.getState()) {
                case OtaState.OTA_STATE_PREPARE:
                    updateOtaBtn(true, true);
                    break;
                case OtaState.OTA_STATE_START: {
                    updateOtaBtn(true, false);
                    String text;
                    if (otaState.getOtaType() == OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE) {
                        text = getString(R.string.update_resource_tips, otaState.getOtaIndex(), otaState.getOtaTotal());
                    } else {
                        text = getString(R.string.ota_checking_upgrade_file);
                    }
                    mBinding.tvOtaMessage.setVisibility(View.VISIBLE);
                    mBinding.tvOtaMessage.setText(getOtaMessage(text, 0));
                    mBinding.pbOtaProgress.setProgress(0);
                    break;
                }
                case OtaState.OTA_STATE_WORKING: {
                    String text;
                    if (otaState.getOtaType() == OtaState.OTA_TYPE_OTA_UPDATE_RESOURCE) {
                        text = getString(R.string.update_resource_tips, otaState.getOtaIndex(), otaState.getOtaTotal());
                    } else if (otaState.getOtaType() == OtaState.OTA_TYPE_OTA_UPGRADE_FIRMWARE) {
                        text = getString(R.string.ota_upgrading);
                    } else {
                        text = getString(R.string.ota_check_file);
                    }
                    int value = Math.round(otaState.getOtaProgress()) >= 100 ? 100 : Math.round(otaState.getOtaProgress()) % 100;
                    mBinding.tvOtaMessage.setText(getOtaMessage(text, value));
                    mBinding.pbOtaProgress.setProgress(value);
                    break;
                }
                case OtaState.OTA_STATE_STOP: {
                    String result = getString(R.string.ota_upgrade_failed);
                    switch (otaState.getStopResult()) {
                        case OtaState.OTA_RES_SUCCESS:
                            result = getString(R.string.ota_complete);
                            break;
                        case OtaState.OTA_RES_CANCEL:
                            result = getString(R.string.ota_upgrade_cancel);
                            break;
                        case OtaState.OTA_RES_FAILED:
                            if (otaState.getError() != null) {
                                result = otaState.getError().getMessage();
                                /*if (otaState.getError().getSubCode() == ErrorCode.SUB_ERR_BLE_NOT_CONNECTED) {
                                    ToastUtil.showToastLong(result);
                                    requireActivity().finish();
                                    return;
                                }*/
                            }
                            break;
                    }
                    mBinding.tvOtaMessage.setText(getOtaMessage(result));
                    mBinding.pbOtaProgress.setProgress(0);
                    boolean isEnable = otaState.getStopResult() != OtaState.OTA_RES_SUCCESS && mViewModel.isConnected();
                    updateOtaBtn(isEnable, true);
                    break;
                }
                default:
                    updateOtaBtn(mViewModel.isInitOK(), true);
                    break;
            }
        }));
    }

    private String getOtaMessage(String message) {
        return message;
    }

    @NonNull
    private String getOtaMessage(String message, int progress) {
        return RcspUtil.formatString("%s\t\t%d%%", message, progress);
    }

    private void updateOtaBtn(boolean isEnable, boolean isShow) {
        mBinding.btnOtaUpgrade.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        mBinding.btnOtaUpgrade.setEnabled(isEnable);
        mBinding.btnOtaUpgrade.setBackgroundResource(isEnable ? R.drawable.dbg_btn_blue_selector : R.drawable.dbg_btn_gary_shape);
    }

    private int getAutoTestLoop() {
        String number = mBinding.etAutoTestLoop.getText().toString();
        if (TextUtils.isEmpty(number)) {
            return 1;
        }
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // 指定初始目录为Download文件夹（仅适用于Android 5.0及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        openFileLauncher.launch(intent);
    }

    private void tryToSelectFile() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
                && !PermissionUtils.hasSelfPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionTipsDialog(getString(R.string.select_file_permission_desc));
            UpgradeFragmentPermissionsDispatcher.readStoragePermissionAllowWithPermissionCheck(this);
            return;
        }
        openFileChooser();
    }
}