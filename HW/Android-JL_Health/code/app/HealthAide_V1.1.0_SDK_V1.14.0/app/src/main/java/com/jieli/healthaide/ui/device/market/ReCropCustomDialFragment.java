package com.jieli.healthaide.ui.device.market;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentRecropCustomDialBinding;
import com.jieli.healthaide.tool.customdial.CustomDialInfo;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.CropPhotoActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.watch.CustomWatchBgFragment;
import com.jieli.healthaide.ui.device.watch.CustomWatchBgViewModel;
import com.jieli.healthaide.ui.widget.ResultDialog;
import com.jieli.healthaide.ui.widget.upgrade_dialog.UpdateResourceDialog;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_fatfs.utils.FatUtil;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.util.JL_Log;
import com.yalantis.ucrop.UCrop;

import java.io.File;

/**
 * @ClassName: ReCropCustomDialFragment
 * @Description: 重新裁剪
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/12 16:02
 */
public class ReCropCustomDialFragment extends BaseFragment {
    public static String EXTRA_CUSTOM_DIAL_INFO = "extra_custom_dial_info";
    private CustomDialInfo mCustomDialInfo;
    private CustomWatchBgViewModel mViewModel;
    private FragmentRecropCustomDialBinding mBinding;
    private UpdateResourceDialog mResourceDialog;
    private ResultDialog mResultDialog;
    private final static int REQUEST_CODE_CROP_PHOTO = 0x1460;

    public static CustomWatchBgFragment newInstance() {
        return new CustomWatchBgFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRecropCustomDialBinding.inflate(inflater, container, false);
        mBinding.clCustomWatchBgTopbar.tvTopbarTitle.setText(R.string.current_watch);
        mBinding.clCustomWatchBgTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.tvCustomWatchBgChoose.setText(R.string.re_crop);
        mBinding.tvCustomWatchBgChoose.setOnClickListener(v -> {
//            Uri imageUri = Uri.parse(mCustomDialInfo.srcImagePath);
            File file = new File(mCustomDialInfo.srcImagePath);
            Uri imageUri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
            goToCropPhoto(imageUri);
        });
//        mBinding.tvCustomWatchBgRestore.setVisibility(View.GONE);
        mViewModel = new ViewModelProvider(this).get(CustomWatchBgViewModel.class);
        if (getArguments() != null) {
            mViewModel.mWatchInfo = getArguments().getParcelable(HealthConstant.EXTRA_WATCH_INFO);
            mCustomDialInfo = getArguments().getParcelable(EXTRA_CUSTOM_DIAL_INFO);
        }
        if (mViewModel.mWatchInfo == null) {
            requireActivity().finish();
            return;
        }
        String bgName = mViewModel.getCustomBgName(mViewModel.mWatchInfo.getName());
        JL_Log.d(tag, "onActivityCreated", "bgName : " + bgName);
        mViewModel.mPhotoSavePath = new File(HealthUtil.createFilePath(requireContext(), HealthConstant.DIR_WATCH)
                + File.separator + bgName);
//        HealthUtil.updateWatchImg(requireContext(), mBinding.ivCustomWatchBgImg, mViewModel.mWatchInfo.getBitmapUri());
        updateImageView(mCustomDialInfo.cutImagePath, mBinding.ivCustomWatchBgImg, getDialExpandInfo());
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.mChangeWatchMLD.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                requireActivity().finish();
            }
        });
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
                        updateImageView(mCustomDialInfo.cutImagePath, mBinding.ivCustomWatchBgImg, getDialExpandInfo());
                    } else if (customBgStatus.getCode() == FatFsErrCode.RES_TOO_BIG) {
                        message = getString(R.string.not_enough_space);
                    }
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
                        finish();
                    }
                    break;
            }
        }));
        mViewModel.mCurrentWatchMLD.observe(getViewLifecycleOwner(), this::updateCustomBg);
        updateCustomBg(mViewModel.mWatchInfo);
    }

    @Override
    public void onDestroy() {
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
                case REQUEST_CODE_CROP_PHOTO:
                    if (isDetached() || !isAdded()) return;
                    if (data == null) return;
                    Uri uri = UCrop.getOutput(data);
                    if (uri == null) return;
                    photoPath = uri.getPath();
                    mViewModel.enableCustomBg(photoPath, mViewModel.getWatchWidth(), mViewModel.getWatchHeight(), mCustomDialInfo, null);
                    break;
            }
            JL_Log.i(tag, "onActivityResult", "photoPath = " + photoPath + ", requestCode = " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void updateCustomBg(WatchInfo info) {
        if (null == info || isDetached() || !isAdded()) return;
        String bgName = mViewModel.getCustomBgName(info.getName());
        JL_Log.d(tag, "updateCustomBg", "bgName : " + bgName + ", dial name : " + info.getName());
        mViewModel.mPhotoSavePath = new File(HealthUtil.createFilePath(requireContext(), HealthConstant.DIR_WATCH)
                + File.separator + bgName);
    }

    private DialExpandInfo getDialExpandInfo() {
        final WatchManager watchManager = WatchManager.getInstance();
        final WatchConfigure watchConfigure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        if (null == watchConfigure) return null;
        return watchConfigure.getDialExpandInfo();
    }

    private void updateImageView(String filePath, ImageView view, DialExpandInfo dialExpandInfo) {
        Glide.with(HealthApplication.getAppViewModel().getApplication())
                .load(filePath)
//                .transform(new MultiTransformation<>(new CenterCrop(),new CropTransformation(dialExpandInfo,CustomDialManager.getInstance().getWatchWidth(), CustomDialManager.getInstance().getWatchHeight())))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    private void goToCropPhoto(Uri uri) {
        Intent intent = new Intent(requireContext(), CropPhotoActivity.class);
        intent.putExtra(CropPhotoActivity.KEY_CROP_TYPE, CropPhotoActivity.CROP_TYPE_WATCH_BG);
        intent.putExtra(CropPhotoActivity.KEY_RESOURCE_URI, uri);
        intent.putExtra(CropPhotoActivity.KEY_OUTPUT_PATH, mViewModel.mPhotoSavePath.getPath());
        startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
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

}
