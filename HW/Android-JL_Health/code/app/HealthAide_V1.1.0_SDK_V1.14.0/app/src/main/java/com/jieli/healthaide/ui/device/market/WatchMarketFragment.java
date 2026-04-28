package com.jieli.healthaide.ui.device.market;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentWatchMarketBinding;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.tool.watch.WatchServerCacheHelper;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.adapter.WatchAdapter;
import com.jieli.healthaide.ui.device.bean.WatchInfo;
import com.jieli.healthaide.ui.device.bean.WatchOpData;
import com.jieli.healthaide.ui.widget.CustomLoadMoreView;
import com.jieli.healthaide.ui.widget.upgrade_dialog.UpdateResourceDialog;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.healthaide.util.HealthUtil;
import com.jieli.jl_fatfs.FatFsErrCode;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class WatchMarketFragment extends BaseFragment {

    private WatchMarketViewModel mViewModel;
    private FragmentWatchMarketBinding mMarketBinding;
    private WatchAdapter mWatchAdapter;

    private UpdateResourceDialog mResourceDialog;

    private String showOpName;

    private final static int MSG_REQUEST_SERVER_RESOURCE = 0x1456;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_REQUEST_SERVER_RESOURCE) {
                mViewModel.loadServiceWatchList();
            }
            return true;
        }
    });

    public static WatchMarketFragment newInstance() {
        return new WatchMarketFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mMarketBinding = FragmentWatchMarketBinding.inflate(inflater, container, false);
        return mMarketBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMarketBinding.clWatchMarketTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mMarketBinding.clWatchMarketTopbar.tvTopbarTitle.setText(R.string.watch_market);
        mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setOnClickListener(v -> {
            if (mWatchAdapter != null) {
                mWatchAdapter.setEditMode(!mWatchAdapter.isEditMode());
                updateTopBarRightUI(mWatchAdapter.isEditMode());
            }
        });

        mMarketBinding.clWatchMarketList.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        mWatchAdapter = new WatchAdapter();
        mWatchAdapter.setBanEditCustomBg(true);
        mWatchAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            WatchInfo watchInfo = mWatchAdapter.getItem(position);
            if (watchInfo == null) return;
            if (view.getId() == R.id.tv_item_watch_btn) {
                switch (watchInfo.getStatus()) {
                    case WatchInfo.WATCH_STATUS_NONE_EXIST:
                        String output = HealthUtil.createFilePath(requireContext(), HealthConstant.DIR_WATCH) + "/" + watchInfo.getName().toLowerCase();
                        mViewModel.downloadWatch(watchInfo.getServerFile().getUrl(), output);
                        break;
                    case WatchInfo.WATCH_STATUS_EXIST:
                        if (watchInfo.hasUpdate()) {
                            String updateOut = HealthUtil.createFilePath(requireContext(), HealthConstant.DIR_WATCH) + "/" + watchInfo.getName().toLowerCase();
                            mViewModel.updateWatch(watchInfo.getUpdateFile().getUrl(), updateOut);
                        } else {
                            mViewModel.enableCurrentWatch(watchInfo.getWatchFile().getPath());
                        }
                        break;
                }
            } else if (view.getId() == R.id.iv_item_watch_delete) {
                mViewModel.deleteWatch(watchInfo);
            }
        });
        mMarketBinding.clWatchMarketList.setAdapter(mWatchAdapter);
        mWatchAdapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        mWatchAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> mViewModel.loadServiceWatchList());
        mWatchAdapter.getLoadMoreModule().setAutoLoadMore(true);
        mWatchAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mViewModel = new ViewModelProvider(this, new WatchMarketViewModel.WatchMarketViewModelFactory(this)).get(WatchMarketViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.mWatchListMLD.observe(getViewLifecycleOwner(), this::updateExistWatchList);
        mViewModel.mWatchMarketResultMLD.observe(getViewLifecycleOwner(), watchMarketResult -> {
            mWatchAdapter.getLoadMoreModule().setEnableLoadMore(true);
            if (watchMarketResult.isOk()) {
                updateAllWatchList(watchMarketResult.getResult().getList());
                mWatchAdapter.getLoadMoreModule().loadMoreComplete();
            } else {
                if (watchMarketResult.getCode() == WatchServerCacheHelper.ERR_LOAD_FINISH) {
                    mWatchAdapter.getLoadMoreModule().loadMoreEnd();
                } else {
                    if (NetworkStateHelper.getInstance().getNetWorkStateModel() != null && !NetworkStateHelper.getInstance().getNetWorkStateModel().isAvailable()) {
                        showTips(getString(R.string.tip_check_net));
                    } else {
                        JL_Log.e(tag, "mWatchMarketResultMLD", "request message error: " + watchMarketResult.getMessage());
                    }
                    mWatchAdapter.getLoadMoreModule().loadMoreFail();
                }
            }
        });
        mViewModel.mWatchOpDataMLD.observe(getViewLifecycleOwner(), data -> {
            if (data.getOp() == WatchOpData.OP_CREATE_FILE || data.getOp() == WatchOpData.OP_REPLACE_FILE) {
                switch (data.getState()) {
                    case WatchOpData.STATE_START:
                        if (requireActivity().getWindow() != null) {
                            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                        showOpName = HealthUtil.getFileNameByPath(data.getFilePath());
                        showUpdateResourceDialog(showOpName, 0);
                        break;
                    case WatchOpData.STATE_PROGRESS:
                        showUpdateResourceDialog(showOpName, Math.round(data.getProgress()));
                        break;
                    case WatchOpData.STATE_END:
                        dismissUpdateResourceDialog();
                        if (requireActivity().getWindow() != null) {
                            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                        if (data.getResult() == FatFsErrCode.RES_OK) {
                            mViewModel.listWatchList();
                        } else {
                            if (data.getOp() == WatchOpData.OP_CREATE_FILE) {
                                showTips(getString(R.string.create_watch_err, showOpName, data.getMessage()));
                            } else {
                                showTips(getString(R.string.update_watch_err, showOpName, data.getMessage()));
                            }
                        }
                        break;
                }
            } else if (data.getOp() == WatchOpData.OP_DELETE_FILE) {
                switch (data.getState()) {
                    case WatchOpData.STATE_START:
                        showOpName = HealthUtil.getFileNameByPath(data.getFilePath());
                        showWaitDialog();
                        break;
                    case WatchOpData.STATE_PROGRESS:
                        break;
                    case WatchOpData.STATE_END:
                        dismissWaitDialog();
                        if (data.getResult() == FatFsErrCode.RES_OK) {
                            mViewModel.listWatchList();
                        } else if (data.getResult() == FatFsErrCode.RES_OP_NOT_ALLOW) { //操作不被允许，表盘少于2个
                            showTips(R.string.delete_watch_tip);
                        } else {
                            showTips(getString(R.string.delete_watch_err, showOpName, data.getMessage()));
                        }
                        break;
                }
            }
        });
        updateTopBarRightUI(mWatchAdapter.isEditMode());

        mViewModel.listWatchList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMarketBinding = null;
        dismissUpdateResourceDialog();
        mViewModel.release();
    }

    private void updateTopBarRightUI(boolean isEditMode) {
        if (isEditMode) {
            mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setText("");
            mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sure_black, 0);
        } else {
            mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setText(R.string.manager);
            mMarketBinding.clWatchMarketTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void updateExistWatchList(ArrayList<WatchInfo> watchList) {
        if (mWatchAdapter == null || isDetached() || !isAdded() || watchList == null) return;
        mWatchAdapter.setList(watchList);
        mViewModel.resetServiceParam();
        mHandler.removeMessages(MSG_REQUEST_SERVER_RESOURCE);
        mHandler.sendEmptyMessageDelayed(MSG_REQUEST_SERVER_RESOURCE, 100);
    }

    private void updateAllWatchList(List<WatchInfo> files) {
        if (mWatchAdapter == null || isDetached() || !isAdded() || files == null) return;
        for (int i = 0; i < files.size(); i++) {
            WatchInfo info = files.get(i);
            JL_Log.d(tag, "updateAllWatchList", "index = " + i + ", " + info);
        }
        mWatchAdapter.setList(files);
    }

    private void showUpdateResourceDialog(String name, int progress) {
        if (isDetached() || !isAdded()) return;
        if (mResourceDialog == null) {
            mResourceDialog = new UpdateResourceDialog.Builder()
                    .setTitle(getString(R.string.update_resource_tips, 1, 1))
                    .setName(name)
                    .setProgress(progress)
                    .setTips(getString(R.string.transfer_file_warning))
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
}