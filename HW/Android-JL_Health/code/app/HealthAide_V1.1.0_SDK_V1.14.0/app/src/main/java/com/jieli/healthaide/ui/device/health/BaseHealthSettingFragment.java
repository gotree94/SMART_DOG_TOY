package com.jieli.healthaide.ui.device.health;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.device.HealthSettingViewModel;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/26
 * @desc :
 */
public class BaseHealthSettingFragment extends BaseFragment {
    protected final static float DISABLE_ALPHA = 0.4f;
    protected HealthSettingViewModel viewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HealthSettingViewModel.class);
        viewModel.mDeviceConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.release();
    }


    protected boolean equalsTime(byte startHour, byte startMin, byte endHour, byte endMin) {
        int time1 = startHour * 60 + startMin;
        int time2 = endHour * 60 + endMin;
        return time1 == time2;
    }

    protected boolean isSmall(byte startHour, byte startMin, byte endHour, byte endMin) {
        int time1 = startHour * 60 + startMin;
        int time2 = endHour * 60 + endMin;
        return time1 < time2;
    }


}
