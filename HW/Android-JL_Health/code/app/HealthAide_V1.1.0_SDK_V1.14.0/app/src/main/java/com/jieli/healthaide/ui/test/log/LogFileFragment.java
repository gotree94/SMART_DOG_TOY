package com.jieli.healthaide.ui.test.log;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentLogFileBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.widget.CommonDecoration;
import com.jieli.healthaide.util.FileUtil;
import com.jieli.healthaide.util.UIHelper;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.util.JL_Log;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 打印文件界面
 * @since 2025/7/25
 */
@RuntimePermissions
public class LogFileFragment extends BaseFragment {

    private LogFileViewModel mViewModel;
    private FragmentLogFileBinding mBinding;
    private LogFileAdapter mAdapter;

    public static LogFileFragment newInstance() {
        return new LogFileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentLogFileBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LogFileViewModel.class);
        initUI();
        addObserver();
        mViewModel.readLogFiles();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogFileFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void readStoragePermissionAllow(String filePath) {
        boolean hasReadPermission = PermissionUtils.hasSelfPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean hasWritePermission = PermissionUtils.hasSelfPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        JL_Log.d(tag, "readStoragePermissionAllow", "hasReadPermission : " + hasReadPermission
                + ", hasWritePermission : " + hasWritePermission);
        disPermissionTipsDialog();
        copyFileToDownloadFolder(filePath);
    }

    @OnShowRationale(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showRelationFromStoragePermission(PermissionRequest request) {
        request.proceed();
    }

    @OnPermissionDenied(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void onDeniedFromStoragePermission() {
        disPermissionTipsDialog();
        showTips(getString(R.string.download_file_permission_denied_tips));
    }

    private void initUI() {
        mBinding.viewToolBar.tvTopbarTitle.setText(getString(R.string.log_file));
        mBinding.viewToolBar.tvTopbarLeft.setOnClickListener(v -> finish());
        mBinding.viewToolBar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_cleaning_black, 0);
        mBinding.viewToolBar.tvTopbarRight.setOnClickListener(v -> mViewModel.clearLog());

        mAdapter = new LogFileAdapter();
        mAdapter.setOnItemClickListener((adapter, view1, position) -> {
            File file = mAdapter.getItem(position);
            tryToOpenFile(file.getPath());
        });
        mAdapter.setOnItemChildClickListener((adapter, view2, position) -> {
            File file = mAdapter.getItem(position);
            final int viewId = view2.getId();
            if (viewId == R.id.btn_download) {
                if (FileUtil.isFileInDownload(requireContext(), file.getName())) return;
                tryToDownloadFile(file.getPath());
            } else if (viewId == R.id.btn_share) {
                tryToShareFile(file.getPath());
            } else if (viewId == R.id.btn_remove) {
                mViewModel.deleteFile(file.getPath());
                View itemView = mAdapter.getViewByPosition(position, R.id.main);
                if (itemView instanceof SwipeMenuLayout) {
                    ((SwipeMenuLayout) itemView).quickClose();
                }
            }
        });
        mBinding.rvLogFile.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvLogFile.setAdapter(mAdapter);
        mBinding.rvLogFile.addItemDecoration(new CommonDecoration(requireContext(), RecyclerView.VERTICAL,
                ContextCompat.getColor(requireContext(), R.color.line_color),
                ValueUtil.dp2px(requireContext(), 1)));
        UIHelper.hide(mBinding.viewToolBar.tvTopbarRight);
    }

    private void addObserver() {
        mViewModel.logFilesMLD.observe(getViewLifecycleOwner(), files -> {
            if (!isFragmentValid()) return;
            mAdapter.setList(files);
            if (files.isEmpty()) {
                UIHelper.hide(mBinding.viewToolBar.tvTopbarRight);
            } else {
                UIHelper.show(mBinding.viewToolBar.tvTopbarRight);
            }
        });

        mViewModel.opResMLD.observe(getViewLifecycleOwner(), opResult -> {
            if (!isFragmentValid() || opResult.isSuccess()) return;
            String op;
            switch (opResult.getOp()) {
                case LogFileViewModel.OP_DELETE_FILE: {
                    op = getString(R.string.op_delete_file);
                    break;
                }
                case LogFileViewModel.OP_DELETE_FOLDER: {
                    op = getString(R.string.op_delete_folder);
                    break;
                }
                default:
                    op = opResult.getOp() + "";
                    break;
            }
            showTips(ConnectUtil.formatString(
                    "%s.\n%s: %d(0x%X), %s.",
                    getString(R.string.operation_failed, op),
                    getString(R.string.error_code),
                    opResult.getCode(), opResult.getCode(),
                    opResult.getMessage()));
        });
    }

    private void tryToOpenFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            showTips(getString(R.string.file_not_found));
            return;
        }
        try {
            Uri uri = FileUtil.getUriByFile(requireContext(), file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
                showTips(getString(R.string.open_file_failed_by_other_app));
                return;
            }
            JL_Log.d(tag, "tryToOpenFile", "startActivity ");
            requireActivity().startActivity(intent);
        } catch (Exception e) {
            JL_Log.e(tag, "tryToOpenFile", "exception : " + e.getMessage());
            showTips(getString(R.string.open_file_exception));
        }
    }

    private void tryToDownloadFile(String filePath) {
        boolean hasReadPermission = PermissionUtils.hasSelfPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean hasWritePermission = PermissionUtils.hasSelfPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        JL_Log.d(tag, "tryToDownloadFile", ConnectUtil.formatString("hasReadPermission : %s, hasWritePermission : %s", hasReadPermission, hasWritePermission));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && (!hasReadPermission || !hasWritePermission)) {
            showPermissionTipsDialog(getString(R.string.download_file_permission_tips));
            LogFileFragmentPermissionsDispatcher.readStoragePermissionAllowWithPermissionCheck(this, filePath);
            return;
        }
        copyFileToDownloadFolder(filePath);
    }

    private void copyFileToDownloadFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            showTips(getString(R.string.file_not_found));
            return;
        }
        ContentValues value = new ContentValues();
        value.put(MediaStore.Downloads.DISPLAY_NAME, file.getName());
        value.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
        value.put(MediaStore.Downloads.RELATIVE_PATH,
                ConnectUtil.formatString("%s/%s", Environment.DIRECTORY_DOWNLOADS, FileUtil.DIR_HEALTH_AIDE)
        );
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, value);
        } else {
            uri = FileUtil.getUriByPath(requireContext(), FileUtil.getDownloadFilePath(file.getName()));
        }
        if (null != uri) {
            copyFile(uri, filePath);
        }
    }

    private void copyFile(Uri folderUri, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            showTips(getString(R.string.file_not_found));
            return;
        }
        try {
            final OutputStream outputStream = requireContext().getContentResolver().openOutputStream(folderUri);
            if (null == outputStream) return;
            FileInputStream input = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int readSize;
            while ((readSize = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readSize);
            }
            outputStream.close();
            input.close();
            String downloadFilePath = FileUtil.getDownloadFilePath(file.getName()).replace("/storage/emulated/0", "");
            showDownloadResultDialog(downloadFilePath);
            mAdapter.updateItemByFilePath(filePath);
        } catch (IOException e) {
            JL_Log.e(tag, "copyFile", "exception : " + e.getMessage());
            showTips(getString(R.string.download_file_exception));
        }
    }

    private void tryToShareFile(String filePath) {
        if (null == filePath) return;
        File file = new File(filePath);
        if (!file.exists()) {
            showTips(getString(R.string.file_not_found));
            return;
        }
        try {
            Uri uri = FileUtil.getUriByFile(requireContext(), file);
            if (null == uri) return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.share_log_file)));
        } catch (Exception e) {
            JL_Log.e(tag, "tryToShareFile", "exception : " + e.getMessage());
            showTips(getString(R.string.share_file_exception));
        }
    }

    private void showDownloadResultDialog(String filePath) {
        if (!isFragmentValid()) return;
        new Jl_Dialog.Builder()
                .title(getString(R.string.download_file_successful))
                .content(filePath)
                .cancel(false)
                .left(getString(R.string.sure))
                .leftColor(ContextCompat.getColor(requireContext(), R.color.blue_448eff))
                .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss())
                .build().show(getChildFragmentManager(), "Download_Result");
    }
}