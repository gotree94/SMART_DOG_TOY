package com.jieli.watchtesttool.ui.ota;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.FragmentNetworkOtaBinding;
import com.jieli.watchtesttool.ui.base.BaseFragment;
import com.jieli.watchtesttool.ui.upgrade.FileSelectorAdapter;
import com.jieli.watchtesttool.util.FileUtil;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

/**
 * 网络升级界面
 */
@RuntimePermissions
public class NetworkOtaFragment extends BaseFragment {
    private FragmentNetworkOtaBinding mBinding;
    private NetworkOtaViewModel mViewModel;
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
                            mViewModel.getOTADirPath(), ".zip");
                    if (null == file || !file.exists() || !file.isFile()) {
                        JL_Log.i(tag, "openFileLauncher", "Invalid file : " + file);
                        showTips(R.string.invalid_file);
                        return;
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mViewModel.listOTAFile();
                        }
                    });
                });
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentNetworkOtaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NetworkOtaViewModel.class);
        initUI();
        addObserver();
        mViewModel.queryNetworkInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.listOTAFile();
    }

    @Override
    public void onDestroyView() {
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroyView();
        uiHandler.removeCallbacksAndMessages(null);
        threadTool.shutdownNow();
        mViewModel.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        NetworkOtaFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
        mBinding.viewTopBar.tvTopbarTitle.setText(getString(R.string.func_network_update));
        mBinding.viewTopBar.tvTopbarLeft.setOnClickListener(v -> {
            if (mViewModel.isNetworkOTA()) {
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
            mViewModel.startOTA(otaFilePaths.get(0));
            updateOtaBtn(false, true);
        });
        mBinding.ivAddFile.setOnClickListener(v -> tryToSelectFile());
        mAdapter = new FileSelectorAdapter(false);
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
        tvTips.setText(getString(R.string.ota_test_file_tips, mViewModel.getOTADirPath()));
        tvTips.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvTips.setLongClickable(false);
        tvTips.setOnClickListener(v -> tryToSelectFile());
        mAdapter.setEmptyView(emptyView);

        updateOtaBtn(mViewModel.isConnected(), true);
    }

    private void addObserver() {
        mViewModel.mNetworkInfoMLD.observe(getViewLifecycleOwner(), networkInfo -> {
            if (!mViewModel.isNetworkOTA()) {
                mBinding.tvOtaMessage.setVisibility(View.VISIBLE);
                mBinding.tvOtaMessage.setText(networkInfo.toString());
            }
        });
        mViewModel.mOtaFileMLD.observe(getViewLifecycleOwner(), files -> mAdapter.setList(files));
        mViewModel.mOtaStatusMLD.observe(getViewLifecycleOwner(), this::updateOtaStatus);
    }

    private void updateOtaBtn(boolean isEnable, boolean isShow) {
        mBinding.btnOtaUpgrade.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        mBinding.btnOtaUpgrade.setEnabled(isEnable);
        mBinding.btnOtaUpgrade.setBackgroundResource(isEnable ? R.drawable.dbg_btn_blue_selector : R.drawable.dbg_btn_gary_shape);
    }

    private void updateOtaStatus(OtaStatus status) {
        if (null == status) return;
        switch (status.getState()) {
            case OtaStatus.STATE_IDLE: {
                mBinding.upgradeFileSelectorView.setEnabled(false);
                updateOtaBtn(mViewModel.isConnected(), true);
                break;
            }
            case OtaStatus.STATE_START: {
                requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mBinding.upgradeFileSelectorView.setEnabled(false);
                updateOtaBtn(true, false);
                mBinding.tvOtaMessage.setVisibility(View.VISIBLE);
                mBinding.tvOtaMessage.setText(getString(R.string.ota_prepare));
                mBinding.pbOtaProgress.setProgress(0);
                break;
            }
            case OtaStatus.STATE_WORKING: {
                updateOtaBtn(true, false);
                OtaWorking otaWorking = (OtaWorking) status;
                final int progress = otaWorking.getProgress();
                mBinding.tvOtaMessage.setVisibility(View.VISIBLE);
                mBinding.tvOtaMessage.setText(String.format(Locale.ENGLISH, "%s. %d%%", getString(R.string.ota_upgrading), progress));
                mBinding.pbOtaProgress.setProgress(progress);
                break;
            }
            case OtaStatus.STATE_STOP: {
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mBinding.upgradeFileSelectorView.setEnabled(true);
                OtaStop otaStop = (OtaStop) status;
                String text;
                if (otaStop.isSuccess()) {
                    text = getString(R.string.ota_complete);
                } else if (otaStop.isCancel()) {
                    text = getString(R.string.ota_upgrade_cancel);
                } else {
                    text = getString(R.string.ota_upgrade_failed, String.format(Locale.ENGLISH, "code: %d, %s", otaStop.getCode(), otaStop.getMessage()));
                }
                mBinding.tvOtaMessage.setVisibility(View.VISIBLE);
                mBinding.tvOtaMessage.setText(text);
                mBinding.pbOtaProgress.setProgress(0);
                boolean isEnable = !otaStop.isSuccess() && mViewModel.isConnected();
                updateOtaBtn(isEnable, true);
                break;
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        openFileLauncher.launch(intent);
    }

    private void tryToSelectFile() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
                && !PermissionUtils.hasSelfPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionTipsDialog(getString(R.string.select_file_permission_desc));
            NetworkOtaFragmentPermissionsDispatcher.readStoragePermissionAllowWithPermissionCheck(this);
            return;
        }
        openFileChooser();
    }
}