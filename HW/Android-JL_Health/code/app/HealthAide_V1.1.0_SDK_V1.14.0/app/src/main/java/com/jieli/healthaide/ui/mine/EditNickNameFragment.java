package com.jieli.healthaide.ui.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentEditNicknameBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.jl_health_http.model.UserInfo;

import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 2:18 PM
 * @desc :
 */
public class EditNickNameFragment extends BaseFragment {
    FragmentEditNicknameBinding binding;

    UserInfoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditNicknameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        initUI();
        addObserver();
        viewModel.getUserInfo();
    }

    private void initUI(){
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.nickname);
        binding.layoutTopbar.tvTopbarLeft.setText(R.string.cancel);
        binding.layoutTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> back());
        binding.layoutTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.auxiliary_widget));

        binding.layoutTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        binding.layoutTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.layoutTopbar.tvTopbarRight.setOnClickListener(v -> saveUserInfo());
        binding.layoutTopbar.tvTopbarRight.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        binding.layoutTopbar.tvTopbarRight.setText(R.string.save);
    }

    private void addObserver(){
        viewModel.userInfoLiveData.observe(getViewLifecycleOwner(), data -> binding.tietNickname.setText(data.getNickname()));
        viewModel.httpStateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case UserInfoViewModel.HTTP_STATE_UPDATING:
                    showWaitDialog();
                    break;
                case UserInfoViewModel.HTTP_STATE_UPDATED_FINISH:
                    requireActivity().finish();
                case UserInfoViewModel.HTTP_STATE_UPDATED_ERROR:
                    dismissWaitDialog();
                    break;
            }
        });
    }

    private void saveUserInfo() {
        String nickName = Objects.requireNonNull(binding.tietNickname.getText()).toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            showTips(R.string.tip_nickname_null);
            return;
        }
        UserInfo data = viewModel.copyUserInfo();
        data.setNickname(nickName);
        viewModel.updateUserInfo(data);
    }
}
