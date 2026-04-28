package com.jieli.healthaide.ui.device.market;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentDialShopBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.market.adapter.DialShopAdapter;
import com.jieli.healthaide.ui.device.market.bean.DialShopItem;
import com.jieli.healthaide.ui.device.market.bean.HeadEntity;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 表盘商城
 */
public class DialShopFragment extends BaseFragment {
    private final DialShopViewModel mViewModel;
    private FragmentDialShopBinding mBinding;
    private DialShopAdapter mAdapter;

    private PaymentBroadReceiver mReceiver;

    public static DialShopFragment newInstance(DialShopViewModel viewModel) {
        return new DialShopFragment(viewModel);
    }

    public DialShopFragment(DialShopViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentDialShopBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
        observeCallback();
        registerCustomReceiver();
        mViewModel.listWatchList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterCustomReceiver();
        mViewModel.release();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WatchDialFragment.REQUEST_CODE_DIAL_OP) {
            mViewModel.listWatchList();
        }
    }

    private void initUI() {
        mBinding.rvShopContainer.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        mAdapter = new DialShopAdapter();
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            DialShopItem item = mAdapter.getItem(position);
            if (null == item) return;
            if (view.getId() == R.id.tv_head_value) {
                if (!item.isHeader() || !(item.getObject() instanceof HeadEntity)) return;
                HeadEntity entity = (HeadEntity) item.getObject();
                if (entity.isHasValue()) {
                    switch (entity.getType()) {
                        case HeadEntity.HEAD_TYPE_FREE:    //跳转到免费表盘
                            toDialListFragment(HealthConstant.DIAL_TYPE_FREE);
                            break;
                        case HeadEntity.HEAD_TYPE_PAY:     //跳转到付费表盘
                            toDialListFragment(HealthConstant.DIAL_TYPE_PAY);
                            break;
                    }
                }
            } else {
                if (item.isHeader() || !(item.getObject() instanceof WatchInfo)) return;
                WatchInfo watchInfo = (WatchInfo) item.getObject();
                if (view.getId() == R.id.tv_item_watch_btn) {
                    if (watchInfo.getStatus() == WatchInfo.WATCH_STATUS_EXIST && !watchInfo.hasUpdate()) {
//                        mViewModel.deletePaymentRecord(watchInfo);
                        mViewModel.enableCurrentWatch(watchInfo.getWatchFile().getPath());
                    } else {
                        toDialDetailFragment(watchInfo, watchInfo.getStatus() != WatchInfo.WATCH_STATUS_NOT_PAYMENT);
                    }
                } else if (view.getId() == R.id.iv_item_watch_delete) {
                    mViewModel.deleteWatch(watchInfo);
                }
            }
        });
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            DialShopItem item = mAdapter.getItem(position);
            if (null == item || item.isHeader() || !(item.getObject() instanceof WatchInfo)) return;
            WatchInfo watchInfo = (WatchInfo) item.getObject();
            toDialDetailFragment(watchInfo, false);
        });
        mBinding.rvShopContainer.setAdapter(mAdapter);
    }

    private void observeCallback() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.mWatchListMLD.observe(getViewLifecycleOwner(), watchInfos -> handleServerList(true,
                mViewModel.mergeWatchList(HealthConstant.DIAL_TYPE_FREE), false));
        mViewModel.watchListResultMLD.observe(getViewLifecycleOwner(), watchListResult -> {
            if (watchListResult.isOk()) {
                boolean isFree = watchListResult.getDialType() == HealthConstant.DIAL_TYPE_FREE;
                JL_Log.d(tag, "watchListResultMLD", "isFree = " + isFree);
                handleServerList(isFree, mViewModel.mergeWatchList(watchListResult.getDialType()), true);
            } else {
                if (mViewModel.isNetworkNotAvailable()) {
                    showTips(getString(R.string.tip_check_net));
                } else {
                    JL_Log.w(tag, "watchListResultMLD", "query server failed. " + watchListResult);
                }
            }
        });
    }

    private void handleServerList(boolean isFree, List<WatchInfo> dials, boolean isSkip) {
        JL_Log.d(tag, "handleServerList", "isFree : " + isFree + ", dial size = " + dials.size() + ", isSkip = " + isSkip);
        for (WatchInfo info : dials) {
            JL_Log.d(tag, "handleServerList", "" + info);
        }
        if (isFree) { //免费表盘
            if (dials.isEmpty() && !isSkip) {
                mViewModel.loadServerDialList(HealthConstant.DIAL_TYPE_FREE, 1);
                return;
            }
            if (!dials.isEmpty()) {
                List<WatchInfo> watchInfos = new ArrayList<>(dials);
                List<DialShopItem> items = new ArrayList<>();
                HeadEntity entity = new HeadEntity(HeadEntity.HEAD_TYPE_FREE);
                entity.setTitle(getString(R.string.free_dial));
                boolean hasMore = watchInfos.size() >= DialShopViewModel.PAGE_NUM;
                entity.setValue(hasMore ? getString(R.string.more) : "");
                entity.setValueIcon(hasMore ? R.drawable.ic_right_arrow : 0);
                items.add(new DialShopItem(true, entity));
                for (WatchInfo info : watchInfos) {
                    if (items.size() > DialShopViewModel.PAGE_NUM) break;
                    items.add(new DialShopItem(false, info));
                }
                mAdapter.setList(items);
            }
            if (mViewModel.isPayListEmpty()) {
                mViewModel.loadServerDialList(HealthConstant.DIAL_TYPE_PAY, 1);
            } else {
                JL_Log.d(tag, "handleServerList", "request payment dials.");
                handleServerList(false, mViewModel.mergeWatchList(HealthConstant.DIAL_TYPE_PAY), isSkip);
            }
        } else { //付费表盘
            if (dials.isEmpty()) return;
            List<WatchInfo> watchInfos = new ArrayList<>(dials);
            List<WatchInfo> deleteList = new ArrayList<>();
            for (WatchInfo info : watchInfos) {
                if (mAdapter.hasItem(info)) { //判断已有数据，就添加到删除列表
                    deleteList.add(info);
                }
            }
            if (!deleteList.isEmpty()) { //过滤已有数据
                watchInfos.removeAll(deleteList);
                if (watchInfos.isEmpty()) return;
            }
            List<DialShopItem> items = new ArrayList<>();
            HeadEntity entity = new HeadEntity(HeadEntity.HEAD_TYPE_PAY);
            entity.setTitle(getString(R.string.pay_dial));
            boolean hasMore = watchInfos.size() >= DialShopViewModel.PAGE_NUM;
            entity.setValue(hasMore ? getString(R.string.more) : "");
            entity.setValueIcon(hasMore ? R.drawable.ic_right_arrow : 0);
            items.add(new DialShopItem(true, entity));
            for (WatchInfo info : watchInfos) {
                if (items.size() > DialShopViewModel.PAGE_NUM) break;
                items.add(new DialShopItem(false, info));
            }
            mAdapter.addData(items);
        }
    }

    private void toDialListFragment(int dialType) {
        Bundle bundle = new Bundle();
        bundle.putInt(DialListFragment.EXTRA_DIAL_TYPE, dialType);
        ContentActivity.startContentActivityForResult(DialShopFragment.this, DialListFragment.class.getCanonicalName(), bundle, WatchDialFragment.REQUEST_CODE_DIAL_OP);
    }

    private void toDialDetailFragment(WatchInfo watchInfo, boolean autoClick) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        bundle.putBoolean(DialDetailFragment.EXTRA_AUTO_EXECUTE, autoClick);
        ContentActivity.startContentActivityForResult(DialShopFragment.this, DialDetailFragment.class.getCanonicalName(), bundle, WatchDialFragment.REQUEST_CODE_DIAL_OP);
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
            JL_Log.d(tag, "onReceive", "action = " + action);
            if (HealthConstant.ACTION_PAYMENT_SUCCESS.equals(action)) {
                WatchInfo watchInfo = intent.getParcelableExtra(HealthConstant.EXTRA_WATCH_INFO);
                JL_Log.d(tag, "ACTION_PAYMENT_SUCCESS", "" + watchInfo);
                if (watchInfo != null) {
                    mViewModel.updatePayList(watchInfo);
                }
            }
        }
    }
}