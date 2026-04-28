package com.jieli.healthaide.ui.device.market;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentDialListBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.adapter.WatchAdapter;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.market.bean.DialListMsg;
import com.jieli.healthaide.ui.widget.CustomLoadMoreView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * 表盘列表界面
 */
public class DialListFragment extends BaseFragment {
    private FragmentDialListBinding mBinding;
    private DialShopViewModel mViewModel;
    private WatchAdapter mAdapter;
    private int dialType;
    private PaymentBroadReceiver mReceiver;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    public static final String EXTRA_DIAL_TYPE = "dial_type";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentDialListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == getArguments()) {
            requireActivity().finish();
            return;
        }
        dialType = getArguments().getInt(EXTRA_DIAL_TYPE, -1);
        if (dialType == -1) {
            requireActivity().finish();
            return;
        }
        mViewModel = new ViewModelProvider(this, new DialShopViewModel.DialShopViewModelFactory(requireContext())).get(DialShopViewModel.class);
        if (!mViewModel.isConnected()) {
            requireActivity().finish();
            return;
        }
        initUI();
        observeCallback();
        registerCustomReceiver();
        mViewModel.listWatchList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (WatchDialFragment.REQUEST_CODE_DIAL_OP == requestCode) {
            mViewModel.listWatchList();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterCustomReceiver();
        mViewModel.release();
        mUIHandler.removeCallbacksAndMessages(null);
    }

    private void initUI() {
        mBinding.viewDialListTopbar.tvTopbarTitle.setText(getTopBarTitle());
        mBinding.viewDialListTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        if (HealthConstant.DELETE_DIAL_RECORD && dialType == HealthConstant.DIAL_TYPE_RECORD) {
            mBinding.viewDialListTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_contact_menu_delete, 0);
            mBinding.viewDialListTopbar.tvTopbarRight.setOnClickListener(v -> mViewModel.deletePaymentRecord());
        }

        mBinding.rvDialListContainer.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        mAdapter = new WatchAdapter();
        mAdapter.setBanEditCustomBg(true);
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            WatchInfo watchInfo = mAdapter.getItem(position);
            if (null == watchInfo || view.getId() != R.id.tv_item_watch_btn) return;
            switch (watchInfo.getStatus()) {
                case WatchInfo.WATCH_STATUS_NOT_PAYMENT:
                    toDialDetailFragment(watchInfo, false);
                    break;
                case WatchInfo.WATCH_STATUS_NONE_EXIST:
                    toDialDetailFragment(watchInfo, true);
                    break;
                case WatchInfo.WATCH_STATUS_EXIST:
                    if (watchInfo.hasUpdate()) {
                        toDialDetailFragment(watchInfo, true);
                    } else {
                        mViewModel.enableCurrentWatch(watchInfo.getWatchFile().getPath());
                    }
                    break;
            }
        });
        mAdapter.setOnItemClickListener(((adapter, view, position) -> {
            WatchInfo watchInfo = mAdapter.getItem(position);
            if (null == watchInfo) return;
            toDialDetailFragment(watchInfo, false);
        }));
        mBinding.rvDialListContainer.setAdapter(mAdapter);
        mAdapter.setEmptyView(R.layout.view_no_payment_record);
        mAdapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        mAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> mUIHandler.postDelayed(() -> mViewModel.loadServerDialList(dialType), 300));
        mAdapter.getLoadMoreModule().setAutoLoadMore(true);
        mAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
    }

    private void observeCallback() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> requireActivity().runOnUiThread(() -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        }));
        mViewModel.mWatchListMLD.observe(getViewLifecycleOwner(), watchInfos -> mViewModel.loadServerDialList(dialType, 1));
        mViewModel.watchListResultMLD.observe(getViewLifecycleOwner(), watchListResult -> {
            mAdapter.getLoadMoreModule().setEnableLoadMore(true);
            JL_Log.d(tag, "watchListResultMLD", "query server ===> " + watchListResult);
            if (watchListResult.isOk()) {
                mAdapter.setList(mViewModel.mergeWatchList(dialType));
                DialListMsg dialListMsg = watchListResult.getResult();
                if (dialListMsg.isLoadFinish()) {
                    mAdapter.getLoadMoreModule().loadMoreEnd(true);
                } else {
                    mAdapter.getLoadMoreModule().loadMoreComplete();
                }
            } else {
                if (mViewModel.isNetworkNotAvailable()) {
                    showTips(getString(R.string.tip_check_net));
                } else {
                    JL_Log.w(tag, "watchListResultMLD", "query server failed. " + watchListResult);
                }
                mAdapter.getLoadMoreModule().loadMoreFail();
            }
        });
        mViewModel.deleteResultMLD.observe(getViewLifecycleOwner(), booleanResult -> {
            if (booleanResult.getResult()) {
                showTips(getString(R.string.execution_succeeded));
                mViewModel.listWatchList();
            } else {
                String text = CalendarUtil.formatString("删除购买记录失败, code=%d, %s", booleanResult.getCode(), booleanResult.getMessage());
                JL_Log.w(tag, "deleteResultMLD", text);
                showTips(CalendarUtil.formatString("%s:%d", getString(R.string.failed_reason), booleanResult.getCode()));
            }
        });
    }

    private String getTopBarTitle() {
        String title = "";
        switch (dialType) {
            case HealthConstant.DIAL_TYPE_FREE:
                title = getString(R.string.free_dial);
                break;
            case HealthConstant.DIAL_TYPE_PAY:
                title = getString(R.string.pay_dial);
                break;
            case HealthConstant.DIAL_TYPE_RECORD:
                title = getString(R.string.purchase_record);
                break;
        }
        return title;
    }

    private void toDialDetailFragment(WatchInfo watchInfo, boolean autoClick) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        bundle.putBoolean(DialDetailFragment.EXTRA_AUTO_EXECUTE, autoClick);
        ContentActivity.startContentActivityForResult(DialListFragment.this, DialDetailFragment.class.getCanonicalName(),
                bundle, WatchDialFragment.REQUEST_CODE_DIAL_OP);
    }

    @SuppressLint("WrongConstant")
    private void registerCustomReceiver() {
        if (null == mReceiver) {
            IntentFilter filter = new IntentFilter(HealthConstant.ACTION_PAYMENT_SUCCESS);
            mReceiver = new PaymentBroadReceiver();
            ContextCompat.registerReceiver(requireContext(), mReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
        }
    }

    private void unregisterCustomReceiver() {
        if (null != mReceiver) {
            requireContext().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private class PaymentBroadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            JL_Log.i(tag, "onReceive", "action = " + action);
            if (HealthConstant.ACTION_PAYMENT_SUCCESS.equals(action)) {
                WatchInfo watchInfo = intent.getParcelableExtra(HealthConstant.EXTRA_WATCH_INFO);
                JL_Log.i(tag, "onReceive", "watchInfo : " + watchInfo);
                if (watchInfo != null) {
                    mViewModel.updatePayList(watchInfo);
                }
            }
        }
    }
}