package com.jieli.healthaide.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jieli.healthaide.databinding.FragmentPhoneRegionChoseBinding;
import com.jieli.healthaide.util.phone.PhoneUtil;

/**
 * @ClassName: PhoneRegionFragment9
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/2 15:11
 */
public class PhoneRegionChoseFragment extends Fragment {
    private FragmentPhoneRegionChoseBinding fragmentPhoneRegionChoseBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentPhoneRegionChoseBinding = FragmentPhoneRegionChoseBinding.inflate(inflater, container, false);
        PhoneUtil.getAllRegionIso();
        return fragmentPhoneRegionChoseBinding.getRoot();
    }
}
