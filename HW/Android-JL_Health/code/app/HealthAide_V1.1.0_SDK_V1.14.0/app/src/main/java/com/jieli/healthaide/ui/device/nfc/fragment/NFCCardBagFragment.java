package com.jieli.healthaide.ui.device.nfc.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentNFCCardBagBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.nfc.NFCViewModel;
import com.jieli.healthaide.ui.device.nfc.adapter.NfcCardBagAdapter;
import com.jieli.healthaide.ui.dialog.WaitingDialog;
import com.jieli.jl_rcsp.model.device.NfcMsg;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.List;

/**
 * 卡包
 */
public class NFCCardBagFragment extends BaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentNFCCardBagBinding mBinding;
    private NFCViewModel mNFCViewModel;
    private NfcCardBagAdapter mAdapter;
    private boolean isFirstShowFragment = true;

    public NFCCardBagFragment() {
    }

    public static NFCCardBagFragment newInstance() {
        return new NFCCardBagFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_n_f_c_card_bag, container, false);
        mBinding = FragmentNFCCardBagBinding.bind(view);
        mBinding.setFragment(this);
        mBinding.setLifecycleOwner(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.card_bag);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mAdapter = new NfcCardBagAdapter();
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            NfcMsg item = mAdapter.getItem(position);
            if (item == null) return;
            seeNFCCardInformation(item.getId());
        });
        mBinding.rvNfcDoorKey.setAdapter(mAdapter);
        mBinding.rvNfcDoorKey.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view) != mAdapter.getItemCount() - 1) {
                    outRect.bottom = ValueUtil.dp2px(requireContext(), -140);
                }
            }
        });

        mNFCViewModel = new ViewModelProvider(this).get(NFCViewModel.class);
        mNFCViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        addObserve();
        if (isFirstShowFragment) {
            mNFCViewModel.syncNfcMessage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstShowFragment) {
            isFirstShowFragment = false;
        }
    }

    public void addNewDoorKey() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_NFCCardBagFragment_to_NFCSimulationFragment);
    }

    private void addObserve() {
        mNFCViewModel.mNfcMsgMLD.observe(getViewLifecycleOwner(), this::updateNfcMsgList);
        mNFCViewModel.mDefaultIdMLD.observe(getViewLifecycleOwner(), aShort -> {
            JL_Log.d(TAG, "DefaultIdMLD", "default id : " + aShort);
            if (aShort != null && mAdapter != null) {
                mAdapter.setDefaultCart(aShort);
            }
        });
        mNFCViewModel.mSyncNfcMsgStatusMLD.observe(getViewLifecycleOwner(), integer -> {
            JL_Log.w(tag, "SyncNfcMsgStatusMLD", "status " + integer);
            if (integer == 0) {
                dismissWaitDialog();
            } else {
                showWaitDialog();
            }
        });
    }

    private void switchView(boolean isEmptyCardBag) {
        mBinding.groupCardBag.setVisibility(isEmptyCardBag ? View.GONE : View.VISIBLE);
        mBinding.groupEmpty.setVisibility(isEmptyCardBag ? View.VISIBLE : View.GONE);
    }

    private void updateNfcMsgList(List<NfcMsg> list) {
        if (isDetached() || !isAdded() || mAdapter == null) return;
        if (null == list || list.isEmpty()) {
            switchView(true);
        } else {
            switchView(false);
            mAdapter.setList(list);
        }
    }

    private void seeNFCCardInformation(short nfcId) {
        JL_Log.d(TAG, "seeNFCCardInformation", "nfcId : " + nfcId);
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
//        bundle.putShort(NFCCardDetailFragment.NFC_CARD_DETAIL_ID, nfcId);
        mNFCViewModel.setOperationNfcMsg(nfcId);
        navController.navigate(R.id.action_NFCCardBagFragment_to_NFCCardDetailFragment, bundle);
    }
}