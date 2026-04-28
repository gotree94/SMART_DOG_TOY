package com.jieli.healthaide.ui.device.nfc.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentNfcMsgBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.nfc.NFCViewModel;
import com.jieli.healthaide.ui.device.nfc.adapter.NfcCardBagAdapter;
import com.jieli.healthaide.ui.device.nfc.bean.NfcStatus;
import com.jieli.jl_rcsp.model.device.NfcMsg;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 卡包界面
 */
@Deprecated
public class NfcMsgFragment extends BaseFragment {
    private FragmentNfcMsgBinding mBinding;
    private NFCViewModel mViewModel;
    private NfcCardBagAdapter mAdapter;

    public static NfcMsgFragment newInstance() {
        return new NfcMsgFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentNfcMsgBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.clNfcMsgTopbar.tvTopbarTitle.setText(R.string.card_bag);
        mBinding.clNfcMsgTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.tvNfcMsgAdd.setOnClickListener(v -> mViewModel.addNfcFile());
        mBinding.rvNfcMsgList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new NfcCardBagAdapter();
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            NfcMsg item = mAdapter.getItem(position);
            if (item == null) return;
            mViewModel.setDefaultNfcID(item.getId());
        });
        mAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            NfcMsg item = mAdapter.getItem(position);
            if (item == null) return false;
            mViewModel.removeNfcMsg(item.getDevHandler(), item.getId());
            return true;
        });
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            NfcMsg item = mAdapter.getItem(position);
            if (item == null) return;
            String name = item.getNickname();
            if (name.startsWith("modify_")) {
                name = name.replace("modify_", "");
            } else {
                name = "modify_" + name;
            }
            mViewModel.modifyNfcMsg(item.getId(), Calendar.getInstance().getTimeInMillis(), name);
        });
        mBinding.rvNfcMsgList.setAdapter(mAdapter);
        mBinding.rvNfcMsgList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view) != mAdapter.getItemCount() - 1) {
                    outRect.bottom = ValueUtil.dp2px(requireContext(), -60);
                }
            }
        });

        mViewModel = new ViewModelProvider(this).get(NFCViewModel.class);
        mViewModel.mNfcMsgMLD.observe(getViewLifecycleOwner(), list -> requireActivity().runOnUiThread(() -> updateNfcMsgList(list)));
        mViewModel.mDefaultIdMLD.observe(getViewLifecycleOwner(), aShort -> {
            JL_Log.w(tag, "DefaultIdMLD", "aShort " + aShort);
            if (aShort != null && mAdapter != null) {
                mAdapter.setDefaultCart(aShort);
            }
        });
        mViewModel.mSyncNfcMsgStatusMLD.observe(getViewLifecycleOwner(), integer -> {
            JL_Log.w(tag, "SyncNfcMsgStatusMLD", "status " + integer);
            if (integer == 0) {
                dismissWaitDialog();
            } else {
                showWaitDialog(true);
            }
        });
        mViewModel.mAddNfcFileStatusMLD.observe(getViewLifecycleOwner(), this::handleNfcStatus);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });

        mViewModel.syncNfcMessage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    private void updateNfcMsgList(List<NfcMsg> list) {
        JL_Log.i(tag, "updateNfcMsgList", "start....");
        if (isDetached() || !isAdded() || mAdapter == null) return;
        if (list == null) list = new ArrayList<>();
        for (NfcMsg msg : list) {
            JL_Log.i(tag, "updateNfcMsgList", "" + msg);
        }
        mAdapter.setList(list);
    }

    private void handleNfcStatus(NfcStatus status) {
        switch (status.getStatus()) {
            case NfcStatus.NFC_STATUS_START:
                showWaitDialog(true);
                break;
            case NfcStatus.NFC_STATUS_WORKING:
                break;
            case NfcStatus.NFC_STATUS_IDLE:
            case NfcStatus.NFC_STATUS_STOP:
                dismissWaitDialog();
                break;
        }
    }
}