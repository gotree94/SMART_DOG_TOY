package com.jieli.healthaide.ui.device.alarm;

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
import com.jieli.healthaide.databinding.FragmentEditAlarmNameBinding;
import com.jieli.healthaide.ui.base.BaseFragment;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/10/21 2:18 PM
 * @desc : 编辑闹钟名称界面
 */
public class EditAlarmNameFragment extends BaseFragment {
    FragmentEditAlarmNameBinding binding;
    public static final String KEY_AlARM_NAME = "alarm";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditAlarmNameBinding.inflate(inflater, container, false);
        binding.layoutTopbar.tvTopbarTitle.setText(R.string.named);
        binding.layoutTopbar.tvTopbarLeft.setText(R.string.cancel);
        binding.layoutTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            requireActivity().onBackPressed();
        });
        binding.layoutTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.auxiliary_widget));

        binding.layoutTopbar.tvTopbarRight.setVisibility(View.GONE);

        binding.layoutTopbar.tvTopbarSecondRight.setVisibility(View.VISIBLE);
        binding.layoutTopbar.tvTopbarSecondRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        binding.layoutTopbar.tvTopbarSecondRight.setOnClickListener(v -> saveAlarmName());
        binding.layoutTopbar.tvTopbarSecondRight.setText(R.string.save);

        if (getArguments() != null) {
            String nickname = getArguments().getString(KEY_AlARM_NAME);
            binding.tietAlarmName.setText(nickname);
        }
        return binding.getRoot();
    }

    private void saveAlarmName() {
        if (!isFragmentValid() || null == binding.tietAlarmName.getText()) return;
        String alarmName = binding.tietAlarmName.getText().toString().trim();
        if (checkName(alarmName)) {
            Intent intent = new Intent();
            intent.putExtra(KEY_AlARM_NAME, alarmName);
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        }
    }


    private boolean checkName(String name) {
        if (TextUtils.isEmpty(name)) {
            showTips(R.string.alarm_name_can_not_be_empty);
            return false;
        }

        byte[] data = name.getBytes();
        if (data.length > 20) { //限制闹钟名小于20 bytes
            showTips(R.string.alarm_name_length_too_long);
            return false;
        }
        return true;
    }


}
