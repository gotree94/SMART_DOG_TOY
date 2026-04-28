package com.jieli.healthaide.ui.sports.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentHomeIndoorRunningBinding;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.base.BaseFragment;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/2/21
 * @desc :
 */
public class HomeIndoorRunningFragment extends BaseFragment {

    public static HomeIndoorRunningFragment newInstance() {
        return new HomeIndoorRunningFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentHomeIndoorRunningBinding mBinding = FragmentHomeIndoorRunningBinding.inflate(inflater, container, false);
        mBinding.btnStartIndoorSport.setOnClickListener(v -> {
            if (WatchManager.getInstance().getConnectedDevice() == null) {
                showTips(R.string.bt_disconnect_tips);
                return;
            }
            SportsCountdownFragment.startByIndoorRunning(requireContext());
        });
        return mBinding.getRoot();
    }

}
