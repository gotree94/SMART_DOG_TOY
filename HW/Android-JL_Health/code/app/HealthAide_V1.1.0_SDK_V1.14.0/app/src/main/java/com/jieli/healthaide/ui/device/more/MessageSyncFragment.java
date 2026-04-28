package com.jieli.healthaide.ui.device.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentMessageSyncBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.util.HealthConstant;

public class MessageSyncFragment extends BaseFragment {

    private MessageSyncViewModel mViewModel;
    private FragmentMessageSyncBinding mBinding;

    public static MessageSyncFragment newInstance() {
        return new MessageSyncFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMessageSyncBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.viewMessageTopbar.tvTopbarTitle.setText(getString(R.string.alert));
        mBinding.viewMessageTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().finish());
        mBinding.sbtnMessageSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setEnableNotification(isChecked);
            handleMessageSyncUI(isChecked);
        });
        mBinding.sbtnMessageWechatSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mViewModel.addAppPackage(HealthConstant.PACKAGE_NAME_WECHAT);
            } else {
                mViewModel.removeAppPackage(HealthConstant.PACKAGE_NAME_WECHAT);
            }
        });

        mBinding.sbtnMessageQqSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mViewModel.addAppPackage(HealthConstant.PACKAGE_NAME_QQ);
            } else {
                mViewModel.removeAppPackage(HealthConstant.PACKAGE_NAME_QQ);
            }
        });

        mBinding.sbtnMessageSmsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mViewModel.addAppPackage(HealthConstant.PACKAGE_NAME_SYS_MESSAGE);
            } else {
                mViewModel.removeAppPackage(HealthConstant.PACKAGE_NAME_SYS_MESSAGE);
            }
        });

        mBinding.sbtnMessageOtherSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mViewModel.setNotFilter(isChecked));

        mViewModel = new ViewModelProvider(this).get(MessageSyncViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });

        mBinding.sbtnMessageSyncSwitch.setCheckedImmediatelyNoEvent(mViewModel.isOpenMessageSync());
        mBinding.sbtnMessageWechatSwitch.setCheckedImmediatelyNoEvent(mViewModel.isSyncWeChat());
        mBinding.sbtnMessageQqSwitch.setCheckedImmediatelyNoEvent(mViewModel.isSyncQQ());
        mBinding.sbtnMessageSmsSwitch.setCheckedImmediatelyNoEvent(mViewModel.isSyncSms());
        mBinding.sbtnMessageOtherSwitch.setCheckedImmediatelyNoEvent(mViewModel.isSyncOther());
        handleMessageSyncUI(mViewModel.isOpenMessageSync());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding = null;
        mViewModel.release();
    }

    private void handleMessageSyncUI(boolean isOpen){
        mBinding.sbtnMessageWechatSwitch.setEnabled(isOpen);
        mBinding.sbtnMessageQqSwitch.setEnabled(isOpen);
        mBinding.sbtnMessageSmsSwitch.setEnabled(isOpen);
        mBinding.sbtnMessageOtherSwitch.setEnabled(isOpen);
    }

}