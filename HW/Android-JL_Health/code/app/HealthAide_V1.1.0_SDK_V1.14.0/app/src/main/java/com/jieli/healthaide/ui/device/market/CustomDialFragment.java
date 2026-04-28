package com.jieli.healthaide.ui.device.market;

import static com.jieli.healthaide.ui.device.market.ReCropCustomDialFragment.EXTRA_CUSTOM_DIAL_INFO;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jieli.component.utils.FileUtil;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentCustomDialsBinding;
import com.jieli.healthaide.tool.aiui.AIManager;
import com.jieli.healthaide.tool.customdial.CustomDialInfo;
import com.jieli.healthaide.tool.customdial.CustomDialManager;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.CropPhotoActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.market.adapter.CustomDialAdapter;
import com.jieli.healthaide.ui.device.watch.CustomWatchBgViewModel;
import com.jieli.healthaide.ui.dialog.PermissionDialog;
import com.jieli.healthaide.ui.widget.ChoosePhotoDialog;
import com.jieli.healthaide.ui.widget.ResultDialog;
import com.jieli.healthaide.ui.widget.upgrade_dialog.UpdateResourceDialog;
import com.jieli.healthaide.util.AppUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.healthaide.util.UriTool;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.util.JL_Log;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * @ClassName: CustomDialFragment
 * @Description: 自定义表盘
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/11 10:59
 */
@RuntimePermissions
public class CustomDialFragment extends BaseFragment {
    private final OnEventListener mEventListener;
    private CustomWatchBgViewModel mViewModel;

    private FragmentCustomDialsBinding mBinding;
    private CustomDialAdapter mCustomDialAdapter;
    private ChoosePhotoDialog mChoosePhotoDialog;
    private UpdateResourceDialog mResourceDialog;
    private ResultDialog mResultDialog;
    private boolean isUserNeverAskAgain = false;
    private final static int REQUEST_CODE_TAKE_PHOTO = 0x1458;
    private final static int REQUEST_CODE_ALBUM = 0x1459;
    private final static int REQUEST_CODE_CROP_PHOTO = 0x1460;
    private Uri mSrcImageUri;
    private final ActivityResultLauncher<Intent> refreshLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            JL_Log.d(tag, "refreshLauncher", "result code : " + result.getResultCode());
            mViewModel.listWatchList();
        }
    });

    public static CustomDialFragment newInstance(OnEventListener listener) {
        return new CustomDialFragment(listener);
    }

    public CustomDialFragment(OnEventListener listener) {
        mEventListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCustomDialsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CustomWatchBgViewModel.class);
        mViewModel.mCustomBgStatusMLD.observe(getViewLifecycleOwner(), customBgStatus -> {
            switch (customBgStatus.getStatus()) {
                case CustomWatchBgViewModel.CustomBgStatus.STATUS_START:
                    showUpdateResourceDialog(customBgStatus.getFilePath(), 0);
                    break;
                case CustomWatchBgViewModel.CustomBgStatus.STATUS_PROGRESS:
                    showUpdateResourceDialog(customBgStatus.getFilePath(), Math.round(customBgStatus.getProgress()));
                    break;
                case CustomWatchBgViewModel.CustomBgStatus.STATUS_END:
                    dismissUpdateResourceDialog();
                    int resId = customBgStatus.isResult() ? R.drawable.ic_success_green : R.drawable.ic_fail_yellow;
                    String message = customBgStatus.getMessage();
                    if (customBgStatus.isResult()) {
                        message = getString(R.string.set_custom_bg_success);
                        if (null != mEventListener)
                            mEventListener.onChangeCustomBg(customBgStatus.getFilePath());
                    } else if (customBgStatus.getCode() == FatFsErrCode.RES_TOO_BIG) {
                        message = getString(R.string.not_enough_space);
                    }
//                    refreshDialList();
                    showResultDialog(customBgStatus.isResult(), resId, message);
                    break;
            }
        });
        mViewModel.mWatchOpDataMLD.observe(getViewLifecycleOwner(), data -> requireActivity().runOnUiThread(() -> {
            if (data.getOp() != WatchOpData.OP_DELETE_FILE) return;
            switch (data.getState()) {
                case WatchOpData.STATE_START:
                    showWaitDialog();
                    break;
                case WatchOpData.STATE_PROGRESS:
                    break;
                case WatchOpData.STATE_END:
                    dismissWaitDialog();
                    if (data.getResult() != FatFsErrCode.RES_OK) {
                        showTips(FatUtil.getFatFsErrorCodeMsg(data.getResult()));
//                        requireActivity().finish();
                    }
                    break;
            }
        }));
        mViewModel.mCurrentWatchMLD.observe(getViewLifecycleOwner(), this::updateCustomBg);
        CustomDialManager.getInstance().usingDialInfoIdLiveData.observe(getViewLifecycleOwner(), usingDialInfoId -> {
            mCustomDialAdapter.setUsingId(usingDialInfoId);
            mCustomDialAdapter.notifyDataSetChanged();
        });
        CustomDialManager.getInstance().dialInfoListLiveData.observe(getViewLifecycleOwner(), dialInfoList -> {
            long usingId = CustomDialManager.getInstance().getCurrentUsingId();
//            List<CustomDialInfo> list = CustomDialManager.getInstance().getDialInfoList();
            List<CustomDialAdapter.Data> dataList = new ArrayList<>();
            dataList.add(new CustomDialAdapter.Data());
            for (CustomDialInfo info : dialInfoList) {
                CustomDialAdapter.Data data = new CustomDialAdapter.Data();
                data.customDialInfo = info;
                data.type = 1;
                dataList.add(data);
            }
            mCustomDialAdapter.setUsingId(usingId);
            mCustomDialAdapter.setList(dataList);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.listWatchList();
    }

    @Override
    public void onDestroy() {
        dismissChoosePhotoDialog();
        dismissUpdateResourceDialog();
        dismissResultDialog();
        super.onDestroy();
        mBinding = null;
        if (mViewModel != null) {
            mViewModel.destroy();
            mViewModel = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String photoPath = "";
            switch (requestCode) {
                case REQUEST_CODE_TAKE_PHOTO:
                    Uri cameraUri = mViewModel.mCameraUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        photoPath = mViewModel.mPhotoSavePath.getPath();
                        cameraUri = Uri.fromFile(mViewModel.mPhotoSavePath);
                    } else {
                        photoPath = cameraUri.getEncodedPath();
                    }
                    mSrcImageUri = cameraUri;
                    goToCropPhoto(cameraUri);
                    break;
                case REQUEST_CODE_ALBUM:
                    if (data == null) return;
                    Uri uri = data.getData();
                    if (uri == null) return;
                    photoPath = UriTool.getPath(requireContext(), uri);
                    mSrcImageUri = uri;
                    goToCropPhoto(uri);
                    break;
                case REQUEST_CODE_CROP_PHOTO:
                    if (AIManager.isInit() && AIManager.getInstance().isAIDialRunning()) return;
                    if (isDetached() || !isAdded()) return;
                    if (data == null) return;
                    uri = UCrop.getOutput(data);
                    if (uri == null || mSrcImageUri == null) return;
                    photoPath = uri.getPath();
                    String fileType = (!isImageJPG(uri)) ? ".png" : ".jpg";
                    String tempSrcImagePath = FileUtil.createFilePath(getContext(), CustomDialManager.WATCH_DIRECTORY) + File.separator + CustomDialManager.IMAGE_SRC_TEMP_DEFAULT + fileType;
                    File file = new File(tempSrcImagePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    AppUtil.copyFile(getContext(), mSrcImageUri, tempSrcImagePath);
                    mViewModel.enableCustomBg(photoPath, tempSrcImagePath, mViewModel.getWatchWidth(), mViewModel.getWatchHeight());
                    break;
            }
            JL_Log.i(tag, "onActivityResult", "photoPath = " + photoPath + ", requestCode = " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CustomDialFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
//        CustomWatchBgFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private boolean isImageJPG(Uri uri) {
        String mime = requireContext().getContentResolver().getType(uri);
        if (mime != null && mime.equals("image/jpeg")) {
            return true; // JPEG 格式
        } else {
            return false;
        }
    }

    private DialExpandInfo getDialExpandInfo() {
        final WatchManager watchManager = WatchManager.getInstance();
        final WatchConfigure watchConfigure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        if (null == watchConfigure) return null;
        return watchConfigure.getDialExpandInfo();
    }

    private void initView() {
        mBinding.rvMyDialContainer.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        mCustomDialAdapter = new CustomDialAdapter();
        mCustomDialAdapter.setDialExpandInfo(getDialExpandInfo());
        mBinding.rvMyDialContainer.setAdapter(mCustomDialAdapter);
        mBinding.tvDeviceDialsBtn.setOnClickListener(v -> {
            mCustomDialAdapter.setEditMode(!mCustomDialAdapter.isEditMode());
            updateEditState();
        });
        mCustomDialAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position == 0) {
                //添加
                showChoosePhotoDialog();
                return;
            }
            CustomDialAdapter.Data data = mCustomDialAdapter.getData().get(position);
            CustomDialInfo customDialInfo = data.customDialInfo;
            goToEditDialBg(customDialInfo);
        });
        mCustomDialAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (position == 0) {
                //添加
                showChoosePhotoDialog();
                return;
            }
            CustomDialAdapter.Data data = mCustomDialAdapter.getData().get(position);
            CustomDialInfo customDialInfo = data.customDialInfo;
            if (view.getId() == R.id.iv_item_watch_delete) {       //删除表盘
                /*boolean isUsing = customDialInfo.id == CustomDialManager.getInstance().getCurrentUsingId();
                if (isUsing) {//
                    mViewModel.restoreCustomBg();
                }*/
                CustomDialManager.getInstance().deleteCustomDial(customDialInfo);
//                refreshDialList();
            } else if (view.getId() == R.id.tv_item_watch_edit) {  //编辑自定义背景
                goToEditDialBg(customDialInfo);
            } else if (view.getId() == R.id.tv_item_watch_btn) {   //使用状态
                boolean isUsing = false;/*customDialInfo.id == CustomDialManager.getInstance().getCurrentUsingId();*/
                if (!isUsing) {
                    mViewModel.enableCustomBg(customDialInfo.cutImagePath, mViewModel.getWatchWidth(), mViewModel.getWatchHeight(), customDialInfo, null);
                }
            }
        });

        updateEditState();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void updateCustomBg(WatchInfo info) {
        if (isDetached() || !isAdded() || null == info) return;
        String bgName = mViewModel.getCustomBgName(info.getName());
        JL_Log.d(tag, "updateCustomBg", "dial name : " + info.getName() + ", bgName : " + bgName);
        mViewModel.mPhotoSavePath = new File(HealthUtil.createFilePath(requireContext(), HealthConstant.DIR_WATCH)
                + File.separator + bgName);
    }

    @NeedsPermission({Manifest.permission.CAMERA,})
    public void choosePhotoByCamera() {
        JL_Log.w(tag, "choosePhotoByCamera", "");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mViewModel.mCameraUri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", mViewModel.mPhotoSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            mViewModel.mCameraUri = Uri.fromFile(mViewModel.mPhotoSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mViewModel.mCameraUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    @OnShowRationale({Manifest.permission.CAMERA,})
    public void showRelationForCamera(PermissionRequest request) {
        showCameraDialog(request);
        isUserNeverAskAgain = true;
    }

    @OnPermissionDenied({Manifest.permission.CAMERA,})
    public void onCameraDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA,})
    public void onCameraNeverAskAgain() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showCameraDialog(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.READ_MEDIA_IMAGES,})
    public void choosePhotoFromAlbumBy33() {
        JL_Log.w(tag, "choosePhotoFromAlbum", "");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")
                .addCategory(Intent.CATEGORY_OPENABLE);

        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({Manifest.permission.READ_MEDIA_IMAGES,})
    public void showRelationForExternalStoragePermissionBy33(PermissionRequest request) {
        showExternalStorageDialog(Manifest.permission.READ_MEDIA_IMAGES, request);
        isUserNeverAskAgain = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({Manifest.permission.READ_MEDIA_IMAGES,})
    public void onExternalStorageNeverAskAgainBy33() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showExternalStorageDialog(Manifest.permission.READ_MEDIA_IMAGES, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.READ_MEDIA_IMAGES,})
    public void onExternalStorageDeniedBy33() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,})
    public void choosePhotoFromAlbum() {
        JL_Log.w(tag, "choosePhotoFromAlbum", "");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")
                .addCategory(Intent.CATEGORY_OPENABLE);

        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE,})
    public void showRelationForExternalStoragePermission(PermissionRequest request) {
        showExternalStorageDialog(Manifest.permission.READ_EXTERNAL_STORAGE, request);
        isUserNeverAskAgain = true;
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE,})
    public void onExternalStorageNeverAskAgain() {
        if (isUserNeverAskAgain) {
            isUserNeverAskAgain = false;
        } else {
            showExternalStorageDialog(Manifest.permission.READ_EXTERNAL_STORAGE, null);
        }
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE,})
    public void onExternalStorageDenied() {
        showTips(getString(R.string.user_denies_permissions, getString(R.string.app_name)));
    }

    private void showExternalStorageDialog(String permission, PermissionRequest request) {
        PermissionDialog permissionDialog = new PermissionDialog(permission, request);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getChildFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

    private void refreshDialList() {
        long usingId = CustomDialManager.getInstance().getCurrentUsingId();
        List<CustomDialInfo> list = CustomDialManager.getInstance().getDialInfoList();
        List<CustomDialAdapter.Data> dataList = new ArrayList<>();
        dataList.add(new CustomDialAdapter.Data());
        for (CustomDialInfo info : list) {
            CustomDialAdapter.Data data = new CustomDialAdapter.Data();
            data.customDialInfo = info;
            data.type = 1;
            dataList.add(data);
        }
        mCustomDialAdapter.setUsingId(usingId);
        mCustomDialAdapter.setList(dataList);
    }

    private void updateEditState() {
        mBinding.tvDeviceDialsBtn.setText(mCustomDialAdapter.isEditMode() ? getString(R.string.finish) : getString(R.string.manager));
    }


    private void showChoosePhotoDialog() {
        if (isDetached() || !isAdded()) return;
        if (null == mChoosePhotoDialog) {
            mChoosePhotoDialog = new ChoosePhotoDialog();
            mChoosePhotoDialog.setOnChoosePhotoListener(new ChoosePhotoDialog.OnChoosePhotoListener() {
                @Override
                public void onTakePhoto() {
                    dismissChoosePhotoDialog();
                    showPermissionDialog(Manifest.permission.CAMERA, (permission ->
                            CustomDialFragmentPermissionsDispatcher.choosePhotoByCameraWithPermissionCheck(CustomDialFragment.this)));
//                    choosePhotoByCamera();
                }

                @Override
                public void onSelectFromAlbum() {
                    dismissChoosePhotoDialog();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        showPermissionDialog(Manifest.permission.READ_MEDIA_IMAGES, (permission ->
                                CustomDialFragmentPermissionsDispatcher.choosePhotoFromAlbumBy33WithPermissionCheck(CustomDialFragment.this)));
                        return;
                    }
                    showPermissionDialog(Manifest.permission.READ_EXTERNAL_STORAGE, (permission ->
                            CustomDialFragmentPermissionsDispatcher.choosePhotoFromAlbumWithPermissionCheck(CustomDialFragment.this)));
//                    choosePhotoFromAlbum();
                }

                @Override
                public void onCancel() {
                    dismissChoosePhotoDialog();
                }
            });
        }
        if (!mChoosePhotoDialog.isShow()) {
            mChoosePhotoDialog.show(getChildFragmentManager(), ChoosePhotoDialog.class.getSimpleName());
        }
    }

    private void dismissChoosePhotoDialog() {
        if (isDetached() || !isAdded()) return;
        if (null != mChoosePhotoDialog) {
            if (mChoosePhotoDialog.isShow()) {
                mChoosePhotoDialog.dismiss();
            }
            mChoosePhotoDialog = null;
        }
    }

    private void showCameraDialog(PermissionRequest request) {
        PermissionDialog permissionDialog = new PermissionDialog(Manifest.permission.CAMERA, request);
        permissionDialog.setCancelable(true);
        permissionDialog.show(getChildFragmentManager(), PermissionDialog.class.getCanonicalName());
    }

    private void goToCropPhoto(Uri uri) {
        Intent intent = new Intent(requireContext(), CropPhotoActivity.class);
        intent.putExtra(CropPhotoActivity.KEY_CROP_TYPE, CropPhotoActivity.CROP_TYPE_WATCH_BG);
        intent.putExtra(CropPhotoActivity.KEY_RESOURCE_URI, uri);
        intent.putExtra(CropPhotoActivity.KEY_OUTPUT_PATH, mViewModel.mPhotoSavePath.getPath());
        startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
    }

    private void goToEditDialBg(CustomDialInfo customDialInfo) {
        if (null == customDialInfo) return;
        final WatchInfo watchInfo = mViewModel.mWatchInfo;
        if (null == watchInfo) return;
        Bundle bundle = new Bundle();
        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        bundle.putParcelable(EXTRA_CUSTOM_DIAL_INFO, customDialInfo);
        ContentActivity.startContentActivityForResult(this, ReCropCustomDialFragment.class.getCanonicalName(), bundle, refreshLauncher);
    }

    private void showUpdateResourceDialog(String name, int progress) {
        if (isDetached() || !isAdded()) return;
        if (mResourceDialog == null) {
            mResourceDialog = new UpdateResourceDialog.Builder()
                    .setTitle(getString(R.string.update_resource_tips, 1, 1))
                    .setName(name)
                    .setProgress(progress)
                    .create();
        } else {
            mResourceDialog.updateView(mResourceDialog.getBuilder().setName(name).setProgress(progress));
        }
        if (!mResourceDialog.isShow()) {
            mResourceDialog.show(getChildFragmentManager(), UpdateResourceDialog.class.getSimpleName());
        }
    }

    private void dismissUpdateResourceDialog() {
        if (isDetached() || !isAdded()) return;
        if (mResourceDialog != null) {
            if (mResourceDialog.isShow()) {
                mResourceDialog.dismiss();
            }
            mResourceDialog = null;
        }
    }

    private void showResultDialog(boolean isOk, int resId, String text) {
        if (isDetached() || !isAdded()) return;
        if (mResultDialog == null) {
            mResultDialog = new ResultDialog.Builder()
                    .setCancel(false)
                    .setResultCode(isOk ? 1 : 0)
                    .setImgId(resId)
                    .setResult(text)
                    .setBtnText(getString(R.string.sure))
                    .create();
            mResultDialog.setOnResultListener(isOk1 -> {
                dismissResultDialog();
                if (isOk1 == 0) {
                    requireActivity().finish();
                }
            });
        }
        if (!mResultDialog.isShow()) {
            mResultDialog.show(getChildFragmentManager(), ResultDialog.class.getSimpleName());
        }
    }

    private void dismissResultDialog() {
        if (isDetached() || !isAdded()) return;
        if (mResultDialog != null) {
            if (mResultDialog.isShow()) {
                mResultDialog.dismiss();
            }
            mResultDialog = null;
        }
    }


    public interface OnEventListener {

        void onChangeCustomBg(String filePath);
    }
}
