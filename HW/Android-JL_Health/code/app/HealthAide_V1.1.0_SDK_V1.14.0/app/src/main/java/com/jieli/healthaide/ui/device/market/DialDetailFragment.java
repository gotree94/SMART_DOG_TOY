package com.jieli.healthaide.ui.device.market;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.util.ConnectUtil;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentDialDetailBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * 表盘详情界面
 */
public class DialDetailFragment extends BaseFragment {
    private FragmentDialDetailBinding mBinding;
    private DialShopViewModel mViewModel;
    private WatchInfo watchInfo;

    public static final String EXTRA_AUTO_EXECUTE = "auto_execute";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentDialDetailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this, new DialShopViewModel.DialShopViewModelFactory(requireContext())).get(DialShopViewModel.class);
        if (null == getArguments()) {
            requireActivity().finish();
            return;
        }
        watchInfo = getArguments().getParcelable(HealthConstant.EXTRA_WATCH_INFO);
        if (null == watchInfo) {
            requireActivity().finish();
            return;
        }
        initUI();
        observeCallback();

        if (getArguments() != null && getArguments().getBoolean(EXTRA_AUTO_EXECUTE, false)) {
            JL_Log.d(tag, "onViewCreated", "auto execute....");
            mBinding.btnDialDetailState.performClick();
        }
    }

    private void initUI() {
        ((BaseActivity) requireActivity()).setOnBackPressIntercept(this::isSkipBack);
        mBinding.viewDialDetailTopbar.tvTopbarLeft.setOnClickListener(v -> {
            if (!isSkipBack()) {
                requireActivity().finish();
            }
        });
        mBinding.btnDialDetailState.setOnClickListener(v -> {
            if (null == watchInfo) return;
            JL_Log.d(tag, "state click", "" + watchInfo);
            switch (watchInfo.getStatus()) {
                case WatchInfo.WATCH_STATUS_NOT_PAYMENT:  //跳转付费界面
                    if (watchInfo.getServerFile() == null) {
                        showTips(R.string.missing_server_information);
                        return;
                    }
                    if (watchInfo.getServerFile().getPrice() > 0) { //付费方式
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
                        ContentActivity.startContentActivity(requireContext(), DialPayFragment.class.getCanonicalName(), bundle);
                        requireActivity().finish();
                        return;
                    }
                    //免费方式
                    mViewModel.dialPayByFree(watchInfo);
                    break;
                case WatchInfo.WATCH_STATUS_NONE_EXIST: //下载
                    mViewModel.downloadDial(watchInfo);
                    break;
                case WatchInfo.WATCH_STATUS_EXIST:
                    if (watchInfo.hasUpdate()) {
                        mViewModel.updateDial(watchInfo);   //更新
                    } else {
                        mViewModel.enableCurrentWatch(watchInfo.getWatchFile().getPath()); //使能表盘
                    }
                    break;
            }
        });
        updateWatchInfoUI();
    }

    private void observeCallback() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> requireActivity().runOnUiThread(() -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        }));
        mViewModel.mWatchOpDataMLD.observe(getViewLifecycleOwner(), watchOpData -> requireActivity().runOnUiThread(() -> {
            if (watchOpData.getOp() == WatchOpData.OP_CREATE_FILE || watchOpData.getOp() == WatchOpData.OP_REPLACE_FILE) {
                String tips;
                switch (watchOpData.getState()) {
                    case WatchOpData.STATE_START:
                        mBinding.btnDialDetailState.setEnabled(false);
                        showProgress(true);
                        tips = watchOpData.getOp() == WatchOpData.OP_CREATE_FILE ? getString(R.string.download_dial) : getString(R.string.update_dial);
                        tips = CalendarUtil.formatString("%s %d%%", tips, 0);
                        updateProgress(tips, 0);
                        break;
                    case WatchOpData.STATE_PROGRESS:
                        int progress = Math.round(watchOpData.getProgress());
                        tips = watchOpData.getOp() == WatchOpData.OP_CREATE_FILE ? getString(R.string.download_dial) : getString(R.string.update_dial);
                        tips = CalendarUtil.formatString("%s %d%%", tips, progress);
                        updateProgress(tips, progress);
                        break;
                    case WatchOpData.STATE_END:
                        showProgress(false);
                        if (watchOpData.getResult() == FatFsErrCode.RES_OK) {
                            mViewModel.listWatchList();
                        } else {
                            showTips(ConnectUtil.formatString("%s %s", getString(R.string.ai_operation_failed), watchOpData.getMessage()));
                            updateWatchInfoUI();
                        }
                        break;
                }
            }
        }));
        mViewModel.mErrorMLD.observe(getViewLifecycleOwner(), error -> {
            JL_Log.e(tag, "Error", "" + error);
        });
        mViewModel.mWatchListMLD.observe(getViewLifecycleOwner(), watchInfos -> {
            if (null == watchInfos || watchInfos.isEmpty() || null == watchInfo) return;
            boolean isUpdate = false;
            for (WatchInfo info : watchInfos) {
                if ((info.getUuid() != null && info.getUuid().equals(watchInfo.getUuid()))
                        || (info.getName() != null && info.getName().equals(watchInfo.getName())
                        && info.getWatchFile() != null && info.getWatchFile().equals(watchInfo.getWatchFile()))) {
                    if (info.getStatus() != watchInfo.getStatus()) {
                        watchInfo.setStatus(info.getStatus());
                        isUpdate = true;
                    }
                    break;
                }
            }
            if (isUpdate) updateWatchInfoUI();
        });
    }

    private void updateDialPreview() {
        final WatchConfigure configure = mViewModel.getWatchConfigure(mViewModel.getConnectedDevice());
        boolean isRectangle = configure != null && configure.getDialExpandInfo() != null &&
                !configure.getDialExpandInfo().isCircular();
        final String url = watchInfo.getBitmapUri();
        if (isRectangle) { //矩形
            mBinding.ivProductImg.setImageResource(R.drawable.bg_watch_rectangle);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.ivDialImg.getLayoutParams();
            lp.width = ValueUtil.dp2px(requireContext(), 113);
            lp.height = ValueUtil.dp2px(requireContext(), 137);
            lp.verticalBias = 0.45f;
            mBinding.ivDialImg.setLayoutParams(lp);
            Glide.with(HealthApplication.getAppViewModel().getApplication())
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new RoundedCorners(ValueUtil.dp2px(requireContext(), 20)))
                    .into(mBinding.ivDialImg);
        } else { //圆形
            mBinding.ivProductImg.setImageResource(R.drawable.bg_watch_round);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.ivDialImg.getLayoutParams();
            lp.width = ValueUtil.dp2px(requireContext(), 148);
            lp.height = ValueUtil.dp2px(requireContext(), 148);
            lp.verticalBias = 0.5f;
            mBinding.ivDialImg.setLayoutParams(lp);
            Glide.with(HealthApplication.getAppViewModel().getApplication())
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mBinding.ivDialImg);
        }
    }

    private void updateWatchInfoUI() {
        if (null == watchInfo) return;
        updateDialPreview();
        mBinding.viewDialDetailTopbar.tvTopbarTitle.setText(watchInfo.getName());
        mBinding.tvDialName.setText(watchInfo.getName());
        int price = watchInfo.getServerFile() == null ? 0 : watchInfo.getServerFile().getPrice();
        String priceValue = price <= 0 ? getString(R.string.free_dial) : CalendarUtil.formatString("¥ %s", HealthUtil.getPriceFormat(price));
        mBinding.tvDialPrice.setText(priceValue);
        String content = watchInfo.getServerFile() == null ? "" : formatContent(watchInfo.getServerFile().getContent());
        mBinding.tvDialDesc.setText(content);

        mBinding.btnDialDetailState.setEnabled(true);
        mBinding.btnDialDetailState.setBackgroundResource(R.drawable.bg_purple_2_gray_selector);
        String text = null;
        switch (watchInfo.getStatus()) {
            case WatchInfo.WATCH_STATUS_NOT_PAYMENT:
                text = getString(R.string.buy_watch);
                break;
            case WatchInfo.WATCH_STATUS_NONE_EXIST:
                text = getString(R.string.download_watch);
                break;
            case WatchInfo.WATCH_STATUS_EXIST:
                if (watchInfo.hasUpdate()) {
                    text = getString(R.string.update_watch);
                } else {
                    text = getString(R.string.use_watch);
                }
                break;
            case WatchInfo.WATCH_STATUS_USING:
                text = getString(R.string.using_watch);
                mBinding.btnDialDetailState.setBackgroundResource(R.drawable.bg_btn_purple_shape);
                mBinding.btnDialDetailState.setEnabled(false);
                break;
        }
        if (!TextUtils.isEmpty(text)) {
            mBinding.btnDialDetailState.setText(text);
        }
    }

    private void showProgress(boolean isShow) {
        mBinding.btnDialDetailState.setBackgroundResource(isShow ? R.color.text_transparent : R.drawable.bg_purple_2_gray_selector);
        mBinding.pbDialDetail.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private void updateProgress(String tips, int progress) {
        if (progress >= 100) progress = 99;
        mBinding.btnDialDetailState.setText(tips);
        mBinding.pbDialDetail.setProgress(progress);
    }

    private boolean isSkipBack() {
        WatchOpData watchOpData = mViewModel.mWatchOpDataMLD.getValue();
        if (watchOpData != null && (watchOpData.getState() == WatchOpData.STATE_START ||
                watchOpData.getState() == WatchOpData.STATE_PROGRESS)) { //正在操作
            showTips(getString(R.string.handle_dial_tips));
            return true;
        }
        return false;
    }

    private String formatContent(String content) {
        if (TextUtils.isEmpty(content)) return "";
        return CalendarUtil.formatString("%s : %s", getString(R.string.dial_desc), content);
    }
}