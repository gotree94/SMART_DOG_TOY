package com.jieli.healthaide.ui.device.nfc.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentNFCCardSettingBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.nfc.NFCViewModel;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;
import com.jieli.jl_rcsp.model.device.NfcMsg;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NFCCardSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NFCCardSettingFragment extends BaseFragment {
    public static final String NFC_CARD_SETTING_FRAGMENT_TYPE = "nfc_card_setting_fragment_type";
    public static final int SETTING_TYPE_ADD_DOOR_KEY = 1;
    public static final int SETTING_TYPE_EDIT = 2;
    private FragmentNFCCardSettingBinding mBinding;
    private NFCViewModel mNFCViewModel;
    private NfcMsg mCurrentNfcMsg;
    private int mFragmentType;

    public NFCCardSettingFragment() {
        // Required empty public constructor
    }

    public static NFCCardSettingFragment newInstance() {
        return new NFCCardSettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentType = getArguments().getInt(NFC_CARD_SETTING_FRAGMENT_TYPE, SETTING_TYPE_EDIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_n_f_c_card_setting, container, false);
        mBinding = FragmentNFCCardSettingBinding.bind(view);
        mBinding.setFragment(this);
        mBinding.setLifecycleOwner(this);
        mBinding.etNfcSettingDoorKeyName.addTextChangedListener(nickNameTextWatcher);
        mBinding.layoutCard.tvNfcCardDefaultFlag.setVisibility(View.GONE);
        mBinding.tvNfcSettingDoorKeyNameTip.setText(getString(mFragmentType == SETTING_TYPE_ADD_DOOR_KEY ? R.string.door_key_name : R.string.modify_key_name));
        mBinding.rgCardType.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.rb_home){
                mBinding.layoutCard.ivNfcCardBg.setImageResource(R.drawable.ic_nfc_card_home);
            }else if(checkedId == R.id.rb_company){
                mBinding.layoutCard.ivNfcCardBg.setImageResource(R.drawable.ic_nfc_card_company);
            }else if(checkedId == R.id.rb_unit){
                mBinding.layoutCard.ivNfcCardBg.setImageResource(R.drawable.ic_nfc_card_unit);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.viewTopbar.tvTopbarTitle.setText(getString(mFragmentType == SETTING_TYPE_ADD_DOOR_KEY ? R.string.add_door_key : R.string.edit));
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mNFCViewModel = new ViewModelProvider(this).get(NFCViewModel.class);
        mCurrentNfcMsg = mNFCViewModel.getOperationNfcMsg();
        mNFCViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mNFCViewModel.mControlNfcMsgStatusMLD.observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case NFCViewModel.STATUS_WORKING://保存nfc
                    showWaitDialog(true);
                    break;
                case NFCViewModel.STATUS_SUCCESS:
                    dismissWaitDialog();
                    back();
                    break;
                case NFCViewModel.STATUS_FAIL:
                    dismissWaitDialog();
                    showTips(R.string.save_failed);
                    break;
            }
        });
        refreshView();
    }

    public void saveNfcMsg() {
        String nickName = mBinding.etNfcSettingDoorKeyName.getText().toString().trim();
        if ((nickName.getBytes().length > 24) || getWordCount(nickName) > 26) {
            showTips(R.string.save_failed);
        }
        if (!TextUtils.equals(nickName, mCurrentNfcMsg.getNickname())) {
            mNFCViewModel.modifyNfcMsg(mCurrentNfcMsg.getId(), 0, nickName);
        } else {
            back();
        }
    }

    private void refreshView() {
        if (null == mCurrentNfcMsg) return;
        mBinding.layoutCard.tvNfcCardNickname.setText(mCurrentNfcMsg.getNickname());
        mBinding.etNfcSettingDoorKeyName.setText(mCurrentNfcMsg.getNickname());
        int cardType = 0;
        int selectTypeSrcId;
        switch (cardType) {
            case 0:
            default:
                selectTypeSrcId = R.id.rb_home;
                break;
            case 1:
                selectTypeSrcId = R.id.rb_company;
                break;
            case 2:
                selectTypeSrcId = R.id.rb_unit;
                break;
        }
        mBinding.rgCardType.check(selectTypeSrcId);
    }

    private int getWordCount(String str) {
        int length = 0;
        for (int i = 0; i < str.length(); i++) {
            int ascii = Character.codePointAt(str, i);
            if (ascii >= 0 && ascii <= 255) {
                length++;
            } else {
                length += 2;
            }
        }
        return length;
    }

    private void showEditError(String error) {
        mBinding.etNfcSettingDoorKeyName.setError(error);
    }

    private final TextWatcher nickNameTextWatcher = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            if (null == s) return;
            String text = s.toString();
            if ((text.getBytes().length > 24) || getWordCount(text) > 26) {
                showEditError("名称最大长度为26英文字符或13中文字符");
            }
        }
    };
}