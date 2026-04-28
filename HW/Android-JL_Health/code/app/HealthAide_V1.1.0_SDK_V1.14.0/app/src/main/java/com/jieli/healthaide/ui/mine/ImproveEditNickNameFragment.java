package com.jieli.healthaide.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentEditNicknameBinding;
import com.jieli.healthaide.ui.base.BaseFragment;

import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 2:18 PM
 * @desc :
 */
public class ImproveEditNickNameFragment extends BaseFragment {
    FragmentEditNicknameBinding binding;
    public static final String KEY_NICKNAME = "nickname";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditNicknameBinding.inflate(inflater, container, false);
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.nickname);
        binding.layoutTopbar.tvTopbarLeft.setText(R.string.cancel);
        binding.layoutTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            back();
        });
        binding.layoutTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.auxiliary_widget));

        binding.layoutTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        binding.layoutTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        binding.layoutTopbar.tvTopbarRight.setOnClickListener(v -> saveUserInfo());
        binding.layoutTopbar.tvTopbarRight.setText(R.string.save);

        if (getArguments() != null) {
            String nickname = getArguments().getString(KEY_NICKNAME);
            binding.tietNickname.setText(nickname);
        }
        return binding.getRoot();
    }

    private void saveUserInfo() {
        String nickName = Objects.requireNonNull(binding.tietNickname.getText()).toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            showTips(R.string.tip_nickname_null);
        }
        Intent intent = new Intent();
        intent.putExtra(KEY_NICKNAME, nickName);
        requireActivity().setResult(Activity.RESULT_OK, intent);
        finish();
    }


}
