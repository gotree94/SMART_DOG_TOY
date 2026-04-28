package com.jieli.healthaide.ui.device.nfc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentNFCCardDetailBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.nfc.NFCViewModel;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_rcsp.model.device.NfcMsg;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NFCCardDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NFCCardDetailFragment extends BaseFragment {
    FragmentNFCCardDetailBinding mBinding;
    private NFCViewModel mNFCViewModel;
    private NfcMsg mCurrentNfcMsg;
    private Short mDefaultCardId = null;

    public NFCCardDetailFragment() {
        // Required empty public constructor
    }

    public static NFCCardDetailFragment newInstance() {
        return new NFCCardDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_n_f_c_card_detail, container, false);
        mBinding = FragmentNFCCardDetailBinding.bind(view);
        mBinding.setFragment(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.viewTopbar.tvTopbarTitle.setText(getString(R.string.detail));
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mNFCViewModel = new ViewModelProvider(this).get(NFCViewModel.class);
        mCurrentNfcMsg = mNFCViewModel.getOperationNfcMsg();
        mNFCViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mNFCViewModel.mDefaultIdMLD.observe(getViewLifecycleOwner(), id -> {
            mDefaultCardId = id;
            refreshView();
        });
        mNFCViewModel.mControlNfcMsgStatusMLD.observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case NFCViewModel.STATUS_WORKING://删除nfc
                    showWaitDialog(true);
                    break;
                case NFCViewModel.STATUS_SUCCESS:
                    dismissWaitDialog();
                    requireActivity().onBackPressed();
                    break;
                case NFCViewModel.STATUS_FAIL:
                    dismissWaitDialog();
                    showTips(R.string.delete_card_fail);
                    break;
            }
        });
        refreshView();
    }

    public void editNFCCard() {
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putInt(NFCCardSettingFragment.NFC_CARD_SETTING_FRAGMENT_TYPE, NFCCardSettingFragment.SETTING_TYPE_EDIT);
        navController.navigate(R.id.action_NFCCardDetailFragment_to_NFCCardSettingFragment, bundle);
    }

    public void setDefaultCard() {
        mNFCViewModel.setDefaultNfcID(mCurrentNfcMsg.getId());
    }

    public void deleteCurrentCard() {
        mNFCViewModel.removeNfcMsg(mCurrentNfcMsg.getDevHandler(), mCurrentNfcMsg.getId());
    }

    private void refreshView() {
        if (mCurrentNfcMsg != null) {
            String updateTimeString = FormatUtil.formatterTime(mCurrentNfcMsg.getUpdateTime());
            mBinding.tvNfcDetailAddTime.setText(updateTimeString);
            boolean isDefaultCard = mDefaultCardId != null && mDefaultCardId == mCurrentNfcMsg.getId();
            mBinding.tvNfcDetailDefault.setTextColor((isDefaultCard) ? getResources().getColor(R.color.text_secondary_disable_color) : getResources().getColor(R.color.auxiliary_widget));
            mBinding.tvNfcDetailDefault.setEnabled(!isDefaultCard);
            mBinding.tvNfcDetailDefault.setClickable(!isDefaultCard);
            mBinding.layoutCard.tvNfcCardDefaultFlag.setVisibility(isDefaultCard ? View.VISIBLE : View.GONE);
            mBinding.layoutCard.tvNfcCardNickname.setText(mCurrentNfcMsg.getNickname());
        }
    }
}