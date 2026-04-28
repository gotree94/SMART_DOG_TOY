package com.jieli.healthaide.ui.device.health;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentEarlyWarningBinding;
import com.jieli.healthaide.ui.dialog.ChooseNumberDialog;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.model.device.health.ExerciseHeartRateReminder;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/23
 * @desc :
 */
public class EarlyWarningFragment extends BaseHealthSettingFragment {
    FragmentEarlyWarningBinding binding;

    private final static int DEFAULT_HEART_MAX = 120;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_early_warning, container, false);


        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.heart_rate_range_early_warning);

        binding = FragmentEarlyWarningBinding.bind(root);


        binding.includeSw.tvHealthItemName.setText(getString(R.string.heart_rate_limit_warn));
        binding.includeSw.tvHealthHint.setText(getString(R.string.tip_heart_rate_limit_warn));
        binding.includeSw.swHealthCommon.setVisibility(View.VISIBLE);
        binding.includeSw.ivHealthItemNext.setVisibility(View.GONE);
        binding.includeSw.ivHealthItemLeft.setVisibility(View.GONE);
        binding.includeSw.swHealthCommon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //todo 处理心率上限开关
            HealthSettingInfo healthSettingInfo = viewModel.getHealthSettingInfo();
            ExerciseHeartRateReminder exerciseHeartRateReminder = healthSettingInfo.getExerciseHeartRateReminder();
            exerciseHeartRateReminder.setEnable(isChecked);
            if (exerciseHeartRateReminder.getMax() == 0) {
                exerciseHeartRateReminder.setMax(DEFAULT_HEART_MAX);
            }
            viewModel.sendSettingCmd(exerciseHeartRateReminder);
        });


        binding.includeSelect.tvHealthItemName.setText(R.string.heart_rate_limit);
        binding.includeSelect.tvHealthHint.setVisibility(View.GONE);
        binding.includeSelect.ivHealthItemNext.setVisibility(View.VISIBLE);
        binding.includeSelect.ivHealthItemLeft.setVisibility(View.GONE);
        binding.includeSelect.tvHealthItemTail.setVisibility(View.VISIBLE);
        binding.includeSelect.tvHealthItemTail.setText(CalendarUtil.formatString("%d%s", DEFAULT_HEART_MAX, getString(R.string.times_per_minute)));
        binding.includeSelect.getRoot().setOnClickListener(v -> {
            HealthSettingInfo healthSettingInfo = viewModel.getHealthSettingInfo();
            ExerciseHeartRateReminder exerciseHeartRateReminder = healthSettingInfo.getExerciseHeartRateReminder();
            ChooseNumberDialog dialog = new ChooseNumberDialog(100, 200, getString(R.string.heart_rate_limit)
                    , getString(R.string.times_per_minute), exerciseHeartRateReminder.getMax(), index -> {
                ExerciseHeartRateReminder heartRateReminder = viewModel.getHealthSettingInfo().getExerciseHeartRateReminder();
                heartRateReminder.setMax(index);
                viewModel.sendSettingCmd(heartRateReminder);
            });
            dialog.show(getChildFragmentManager(), "EarlyWarningFragment-dialog");
        });

        binding.tvMaxHeartRatePercent.setOnClickListener(v -> {
            ExerciseHeartRateReminder heartRateReminder = viewModel.getHealthSettingInfo().getExerciseHeartRateReminder();
            heartRateReminder.setSpaceMode(ExerciseHeartRateReminder.SPACE_MODE_MAX_PERCENT);
            viewModel.sendSettingCmd(heartRateReminder);
        });
        binding.tvSaveHeartRatePercent.setOnClickListener(v -> {
            ExerciseHeartRateReminder heartRateReminder = viewModel.getHealthSettingInfo().getExerciseHeartRateReminder();
            heartRateReminder.setSpaceMode(ExerciseHeartRateReminder.SPACE_MODE_SAVE_PERCENT);
            viewModel.sendSettingCmd(heartRateReminder);
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            ExerciseHeartRateReminder exerciseHeartRateReminder = healthSettingInfo.getExerciseHeartRateReminder();
            binding.includeSw.swHealthCommon.setCheckedNoEvent(exerciseHeartRateReminder.isEnable());
            binding.includeSw.tvHealthHint.setText(getString(R.string.tip_heart_rate_limit_warn, exerciseHeartRateReminder.getMax()));
            JL_Log.d(tag, "healthSettingInfoLiveData", "data ==> " + exerciseHeartRateReminder);
            binding.includeSelect.tvHealthItemTail.setText(CalendarUtil.formatString("%d%s", exerciseHeartRateReminder.getMax(), getString(R.string.times_per_minute)));

            binding.includeSelect.getRoot().setAlpha(exerciseHeartRateReminder.isEnable() ? 1.0f : 0.4f);
            binding.includeSelect.getRoot().setClickable(exerciseHeartRateReminder.isEnable());

            int res = R.drawable.ic_choose_blue;
            binding.tvMaxHeartRatePercent.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, exerciseHeartRateReminder.getSpaceMode() == 0x00 && exerciseHeartRateReminder.isEnable() ? res : 0x00, 0);
            binding.tvSaveHeartRatePercent.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, exerciseHeartRateReminder.getSpaceMode() == 0x01 && exerciseHeartRateReminder.isEnable() ? res : 0x00, 0);

            binding.tvMaxHeartRatePercent.setClickable(exerciseHeartRateReminder.isEnable());
            binding.tvMaxHeartRatePercent.setAlpha(exerciseHeartRateReminder.isEnable() ? 1.0f : 0.4f);
            binding.tvSaveHeartRatePercent.setClickable(exerciseHeartRateReminder.isEnable());
            binding.tvSaveHeartRatePercent.setAlpha(exerciseHeartRateReminder.isEnable() ? 1.0f : 0.4f);

        });
        viewModel.requestHealthSettingInfo(0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_EXERCISE_HEART_RATE_REMINDER);
    }
}
