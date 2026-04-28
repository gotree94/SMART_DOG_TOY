package com.jieli.healthaide.ui.device.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentMyDialsBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.adapter.WatchAdapter;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.device.watch.CustomWatchBgFragment;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的表盘
 */
public class MyDialsFragment extends BaseFragment {
    private final DialShopViewModel mViewModel;
    private FragmentMyDialsBinding mBinding;
    private WatchAdapter mAdapter;

    private final ActivityResultLauncher<Intent> refreshLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            JL_Log.d(tag, "refreshLauncher", "result code : " + result.getResultCode());
            mViewModel.listWatchList();
        }
    });

    public static MyDialsFragment newInstance(DialShopViewModel viewModel) {
        return new MyDialsFragment(viewModel);
    }

    public MyDialsFragment(DialShopViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMyDialsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
        observeCallback();
        mViewModel.listWatchList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WatchDialFragment.REQUEST_CODE_DIAL_OP) {
            mViewModel.listWatchList();
        }
    }

    private void initUI() {
        mBinding.rvMyDialContainer.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        mAdapter = new WatchAdapter();
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            WatchInfo watchInfo = mAdapter.getItem(position);
            if (null == watchInfo || watchInfo.getStatus() < WatchInfo.WATCH_STATUS_EXIST) return;
            if (view.getId() == R.id.iv_item_watch_delete) {       //删除表盘
                if (mAdapter.getData().size() > 2) {
                    mViewModel.deleteWatch(watchInfo);
                } else {
                    showTips(R.string.delete_watch_tip);
                }
            } else if (view.getId() == R.id.tv_item_watch_edit) {  //编辑自定义背景
                if (watchInfo.getStatus() == WatchInfo.WATCH_STATUS_USING) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
                    ContentActivity.startContentActivityForResult(MyDialsFragment.this, CustomWatchBgFragment.class.getCanonicalName(), bundle, refreshLauncher);
                }
            } else if (view.getId() == R.id.tv_item_watch_btn) {   //使用状态
                if (watchInfo.getStatus() == WatchInfo.WATCH_STATUS_EXIST) {
                    if (watchInfo.hasUpdate()) {
                        toDialDetailFragment(watchInfo, true);
                        return;
                    }
                    mViewModel.enableCurrentWatch(watchInfo.getWatchFile().getPath());
                }
            }
        });
        mBinding.rvMyDialContainer.setAdapter(mAdapter);
        mBinding.tvDeviceDialsBtn.setOnClickListener(v -> {
            mAdapter.setEditMode(!mAdapter.isEditMode());
            updateEditState();
        });
        updateEditState();
    }

    private void observeCallback() {
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> requireActivity().runOnUiThread(() -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        }));
        mViewModel.mWatchListMLD.observe(getViewLifecycleOwner(), this::updateWatchListUI);
        mViewModel.mWatchOpDataMLD.observe(getViewLifecycleOwner(), watchOpData -> requireActivity().runOnUiThread(() -> {
            if (watchOpData.getOp() != WatchOpData.OP_DELETE_FILE) return;
            switch (watchOpData.getState()) {
                case WatchOpData.STATE_START:
                    showWaitDialog(true);
                    break;
                case WatchOpData.STATE_PROGRESS:
                    break;
                case WatchOpData.STATE_END:
                    dismissWaitDialog();
                    if (watchOpData.getResult() == FatFsErrCode.RES_OK) {
                        mViewModel.listWatchList();
                    }
                    break;
            }
        }));
    }

    private void updateEditState() {
        mBinding.tvDeviceDialsBtn.setText(mAdapter.isEditMode() ? getString(R.string.finish) : getString(R.string.manager));
    }

    private void updateWatchListUI(List<WatchInfo> list) {
        if (!isFragmentValid()) return;
        if (null == list) list = new ArrayList<>();
        mAdapter.setList(list);
    }

    private void toDialDetailFragment(WatchInfo watchInfo, boolean autoClick) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(HealthConstant.EXTRA_WATCH_INFO, watchInfo);
        bundle.putBoolean(DialDetailFragment.EXTRA_AUTO_EXECUTE, autoClick);
        ContentActivity.startContentActivityForResult(MyDialsFragment.this, DialDetailFragment.class.getCanonicalName(), bundle, WatchDialFragment.REQUEST_CODE_DIAL_OP);
    }
}