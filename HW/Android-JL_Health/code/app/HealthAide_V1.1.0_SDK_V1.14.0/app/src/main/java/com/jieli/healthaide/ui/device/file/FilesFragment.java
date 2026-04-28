package com.jieli.healthaide.ui.device.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentFilesBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.music.MusicManagerFragment;
import com.jieli.healthaide.ui.dialog.PermissionDialog;
import com.jieli.healthaide.ui.widget.CustomLoadMoreView;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.util.DeviceChoseUtil;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 设备文件浏览界面
 */
@RuntimePermissions
public class FilesFragment extends BaseFragment {
    FragmentFilesBinding binding;

    private SDCardBean mSdCardBean = new SDCardBean();
    private FileListAdapter mFileListAdapter;
    private final FileRouterAdapter mFileRouterAdapter = new FileRouterAdapter();

    private DeviceFileViewModel mViewModel;
    private boolean isUserNeverAskAgain = false;

    private final ActivityResultLauncher<Intent> addMusicLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (mViewModel != null && mSdCardBean != null) {
                mViewModel.cleanDownloadCache(mSdCardBean);
            }
        }
    });

    public void setSdCardBean(SDCardBean mSdCardBean) {
        this.mSdCardBean = mSdCardBean;
    }

    public static FilesFragment newInstance(SDCardBean sdCardBean) {
        Bundle args = new Bundle();
        FilesFragment fragment = new FilesFragment();
        fragment.mSdCardBean = sdCardBean;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSdCardBean = DeviceChoseUtil.getTargetDev();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilesBinding.inflate(inflater, container, false);
        //初始化文件路径导航栏
        binding.rvFilePathNav.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvFilePathNav.setAdapter(mFileRouterAdapter);
        mFileRouterAdapter.setOnItemClickListener((adapter, view12, position) -> mViewModel.back(mSdCardBean, mFileRouterAdapter.getItem(position)));

        //初始化文件列表ui
        mFileListAdapter = createFileAdapter();
        binding.rvDeviceFiles.setAdapter(mFileListAdapter);
        binding.rvDeviceFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        mFileListAdapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        mFileListAdapter.getLoadMoreModule().setOnLoadMoreListener(this::onLoadMoreRequested);

        //没有设备则返回
        if (mSdCardBean == null) {
            requireActivity().finish();
            return binding.getRoot();
        }
        createEmptyView();
        mFileListAdapter.setOnItemClickListener((adapter, view1, position) -> {
            FileStruct fileStruct = mFileListAdapter.getItem(position);
            if (fileStruct != null && fileStruct.isFile()) {
                handleFileClick(fileStruct);
            } else {
                handleFolderClick(fileStruct);
            }
        });

//        mFileListAdapter.setOnItemLongClickListener((adapter, view, position) -> {
//            FileStruct fileStruct = mFileListAdapter.getItem(position);
//            if (fileStruct.isFile()) {
//                handleLongClick(fileStruct);
//                return true;
//            }
//            return false;
//        });

        mFileListAdapter.addChildClickViewIds(R.id.btn_delete, R.id.cl_file);
        mFileListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.cl_file) {
                if (mFileListAdapter.getOnItemClickListener() != null) {
                    mFileListAdapter.getOnItemClickListener().onItemClick(adapter, view, position);
                }
            } else if (view.getId() == R.id.btn_delete) {
                handleLongClick(mFileListAdapter.getItem(position));
                View layout = mFileListAdapter.getViewByPosition(position, R.id.swml);
                if (layout != null) {
                    ((SwipeMenuLayout) layout).smoothClose();
                }
            }
        });

        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        binding.viewTopbar.tvTopbarTitle.setText(R.string.music_manager);
        binding.viewTopbar.tvTopbarRight.setText(R.string.add);
        binding.viewTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.blue_558CFF));
        binding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.viewTopbar.tvTopbarRight.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showPermissionDialog(Manifest.permission.READ_MEDIA_AUDIO, (permission ->
                        FilesFragmentPermissionsDispatcher.toMusicManagerFragmentBy33WithPermissionCheck(FilesFragment.this)));
                return;
            }
            showPermissionDialog(Manifest.permission.READ_EXTERNAL_STORAGE, (permission ->
                    FilesFragmentPermissionsDispatcher.toMusicManagerFragmentWithPermissionCheck(FilesFragment.this)));
        });
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle bundle) {
        super.onActivityCreated(bundle);
        mViewModel = new ViewModelProvider(requireActivity()).get(DeviceFileViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.currentFolderMutableLiveData.observe(getViewLifecycleOwner(), this::refreshFileRouterView);
        mViewModel.filesMutableLiveData.observe(getViewLifecycleOwner(), fileStructs -> requireActivity().runOnUiThread(() -> mFileListAdapter.setList(fileStructs)));
        mViewModel.readStateMutableLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case DeviceFileViewModel.STATE_FINISH:
                    mFileListAdapter.getLoadMoreModule().loadMoreEnd();
                    showEmptyView(true);
                    break;
                case DeviceFileViewModel.STATE_END:
                    mFileListAdapter.getLoadMoreModule().loadMoreComplete();
                    showEmptyView(true);
                    break;
                case DeviceFileViewModel.STATE_FAILED:
                    mFileListAdapter.getLoadMoreModule().loadMoreFail();
                    break;
                case DeviceFileViewModel.STATE_START:
                    showEmptyView(false);
                    break;
            }
        });
        mViewModel.musicPlayInfoLiveData.observe(getViewLifecycleOwner(), musicPlayInfo -> {
            mFileListAdapter.setDeviceMode(musicPlayInfo.getDeviceMode());
            if (musicPlayInfo.getDeviceMode() != AttrAndFunCode.SYS_INFO_FUNCTION_MUSIC) {
                mFileListAdapter.setDeviceMode(musicPlayInfo.getDeviceMode());
            }
            if (musicPlayInfo.getMusicStatusInfo() != null) {
                boolean isPlay = musicPlayInfo.getMusicStatusInfo().isPlay();
                mFileListAdapter.setSelected((byte) musicPlayInfo.getMusicStatusInfo().getCurrentDev(), mFileListAdapter.getSelectedCluster(), isPlay);
            }
            if (musicPlayInfo.getMusicNameInfo() != null) {
                mFileListAdapter.setSelected(mFileListAdapter.getDevIndex(), musicPlayInfo.getMusicNameInfo().getCluster(), mFileListAdapter.isPlay());
            }

        });
        mViewModel.getCurrentInfo(mSdCardBean);
    }


    protected FileListAdapter createFileAdapter() {
        return new FileListAdapter();
    }

    protected FileListAdapter getFileListAdapter() {
        return mFileListAdapter;
    }

    //点击文件
    protected void handleFileClick(FileStruct fileStruct) {
        mViewModel.play(mSdCardBean, fileStruct);
    }

    //点击文件夹
    protected void handleFolderClick(FileStruct fileStruct) {
        showEmptyView(false);
        mViewModel.append(mSdCardBean, fileStruct);
    }


    protected void handleLongClick(FileStruct fileStruct) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.tips)
                .setMessage(getString(R.string.whether_delete, fileStruct.getName()))
                .setCancelable(true)
                .setPositiveButton(R.string.sure, (dialog, which) -> mViewModel.delete(mSdCardBean, fileStruct))
                .setNegativeButton(R.string.cancel, (d, w) -> {

                })
                .create()
                .show();
    }

    //更新顶部导航栏
    private void refreshFileRouterView(Folder folder) {
        if (folder == null) {
            return;
        }
        List<FileStruct> list = new ArrayList<>();
        list.add(folder.getFileStruct());
        while (folder.getParent() != null) {
            folder = (Folder) folder.getParent();
            list.add(0, folder.getFileStruct());
        }
        mFileRouterAdapter.setNewInstance(list);
        binding.rvFilePathNav.scrollToPosition(mFileRouterAdapter.getData().size() - 1);
    }


    private void onLoadMoreRequested() {
        mViewModel.loadMore(mSdCardBean);
    }


    private void createEmptyView() {
        //设置空布局，可用xml文件代替
        TextView textView = new TextView(requireContext());
        textView.setText(getString(R.string.empty_folder));
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_folder_img_empty, 0, 0);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, ValueUtil.dp2px(requireContext(), 98), 0, 0);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        mFileListAdapter.setEmptyView(textView);
        mFileListAdapter.setUseEmpty(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showEmptyView(boolean show) {
        if (mFileListAdapter.isUseEmpty() != show) {
            mFileListAdapter.setUseEmpty(show);
            mFileListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FilesFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({
            Manifest.permission.READ_MEDIA_AUDIO
    })
    public void toMusicManagerFragmentBy33() {
        ContentActivity.startContentActivityForResult(FilesFragment.this, MusicManagerFragment.class.getCanonicalName(), null, addMusicLauncher);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({
            Manifest.permission.READ_MEDIA_AUDIO
    })
    public void showRelationForExternalStoragePermissionBy33(PermissionRequest request) {
        showExternalStorageDialog(Manifest.permission.READ_MEDIA_AUDIO, request);
        isUserNeverAskAgain = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({
            Manifest.permission.READ_MEDIA_AUDIO
    })
    public void onExternalStorageNeverAskAgainBy33() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showExternalStorageDialog(Manifest.permission.READ_MEDIA_AUDIO, null);
        }
    }

    @NeedsPermission({
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void toMusicManagerFragment() {
        ContentActivity.startContentActivityForResult(FilesFragment.this, MusicManagerFragment.class.getCanonicalName(), null, addMusicLauncher);
    }

    @OnShowRationale({
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void showRelationForExternalStoragePermission(PermissionRequest request) {
        showExternalStorageDialog(Manifest.permission.READ_EXTERNAL_STORAGE, request);
        isUserNeverAskAgain = true;
    }

    @OnNeverAskAgain({
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public void onExternalStorageNeverAskAgain() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showExternalStorageDialog(Manifest.permission.READ_EXTERNAL_STORAGE, null);
        }
    }

    private void showExternalStorageDialog(String permission, PermissionRequest request) {
        PermissionDialog permissionDialog = new PermissionDialog(permission, request);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getChildFragmentManager(), PermissionDialog.class.getCanonicalName());
    }
}
