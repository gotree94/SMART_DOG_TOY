package com.jieli.healthaide.ui.device.health;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentSleepDetectionBinding;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog2;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.model.device.health.SleepDetection;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/25
 * @desc :
 */
public class SleepDetectionFragment extends BaseHealthSettingFragment implements View.OnClickListener {
    FragmentSleepDetectionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sleep_detection, container, false);
        binding = FragmentSleepDetectionBinding.bind(root);
        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.science_sleep);
        binding.tvSwStatusClose.setTag(SleepDetection.STATUS_CLOSE);
        binding.tvSwStatusOpenAllDay.setTag(SleepDetection.STATUS_ALL_DAY);
        binding.tvSwStatusSwTimeOpen.setTag(SleepDetection.STATUS_CUSTOM_TIME);

        binding.tvSwStatusSwTimeOpen.setOnClickListener(this);
        binding.tvSwStatusOpenAllDay.setOnClickListener(this);
        binding.tvSwStatusClose.setOnClickListener(this);

        binding.rlStartTime.setOnClickListener(v -> {
            SleepDetection sleepDetection = viewModel.getHealthSettingInfo().getSleepDetection();

            ChooseTimeDialog2 chooseTimeDialog = new ChooseTimeDialog2(sleepDetection.getStartHour(), sleepDetection.getStartMin(), R.string.start_time, (hour, minute) -> {
                sleepDetection.setStartHour((byte) hour);
                sleepDetection.setStartMin((byte) minute);
                if (equalsTime(sleepDetection.getStartHour(), sleepDetection.getStartMin(), sleepDetection.getEndHour(), sleepDetection.getEndMin())) {
                    sleepDetection.setEndMin((byte) minute);
                    int tmp = hour + 1;
                    tmp = tmp > 23 ? 0 : tmp;
                    sleepDetection.setEndHour((byte) tmp);
                }

                viewModel.sendSettingCmd(sleepDetection);
            });
            chooseTimeDialog.show(getChildFragmentManager(), ChooseTimeDialog.class.getSimpleName());
        });


        binding.rlEndTime.setOnClickListener(v -> {
            SleepDetection sleepDetection = viewModel.getHealthSettingInfo().getSleepDetection();

            ChooseTimeDialog2 chooseTimeDialog = new ChooseTimeDialog2(sleepDetection.getEndHour(), sleepDetection.getEndMin(), R.string.end_time, (hour, minute) -> {
                sleepDetection.setEndHour((byte) hour);
                sleepDetection.setEndMin((byte) minute);
                if (equalsTime(sleepDetection.getStartHour(), sleepDetection.getStartMin(), sleepDetection.getEndHour(), sleepDetection.getEndMin())) {
                    sleepDetection.setStartMin((byte) minute);
                    int tmp = hour - 1;
                    tmp = tmp < 0 ? 23 : tmp;
                    sleepDetection.setStartHour((byte) tmp);
                }

                JL_Log.e(tag, "rlEndTime", "sleepDetection ---> " + sleepDetection);

                viewModel.sendSettingCmd(sleepDetection);
            });
            chooseTimeDialog.show(getChildFragmentManager(), ChooseTimeDialog.class.getSimpleName());
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            SleepDetection sleepDetection = healthSettingInfo.getSleepDetection();
            binding.tvSwStatusClose.setCompoundDrawablesWithIntrinsicBounds(0, 0, sleepDetection.getStatus() == SleepDetection.STATUS_CLOSE ? R.drawable.ic_choose_blue : 0, 0);
            binding.tvSwStatusOpenAllDay.setCompoundDrawablesWithIntrinsicBounds(0, 0, sleepDetection.getStatus() == SleepDetection.STATUS_ALL_DAY ? R.drawable.ic_choose_blue : 0, 0);
            binding.tvSwStatusSwTimeOpen.setCompoundDrawablesWithIntrinsicBounds(0, 0, sleepDetection.getStatus() == SleepDetection.STATUS_CUSTOM_TIME ? R.drawable.ic_choose_blue : 0, 0);

//
//             binding.rlStartTime.setVisibility(sleepDetection.getStatus() == 0x02 ? View.VISIBLE : View.GONE);
//             binding.rlEndTime.setVisibility(sleepDetection.getStatus() == 0x02 ? View.VISIBLE : View.GONE);

            binding.rlEndTime.setAlpha(sleepDetection.getStatus() == SleepDetection.STATUS_CUSTOM_TIME ? 1.0f : 0.4f);
            binding.rlEndTime.setClickable(sleepDetection.getStatus() == SleepDetection.STATUS_CUSTOM_TIME);
            binding.rlStartTime.setAlpha(sleepDetection.getStatus() == SleepDetection.STATUS_CUSTOM_TIME ? 1.0f : 0.4f);
            binding.rlStartTime.setClickable(sleepDetection.getStatus() == SleepDetection.STATUS_CUSTOM_TIME);

            @SuppressLint("DefaultLocale") String start = CalendarUtil.formatString("%02d:%02d", sleepDetection.getStartHour(), sleepDetection.getStartMin());
            @SuppressLint("DefaultLocale") String end = CalendarUtil.formatString("%02d:%02d", sleepDetection.getEndHour(), sleepDetection.getEndMin());

            if (isSmall(sleepDetection.getEndHour(), sleepDetection.getEndMin(), sleepDetection.getStartHour(), sleepDetection.getStartMin())) {
                end = getString(R.string.next_day) + " " + end;
            }
            binding.tvSwStartTime.setText(start);
            binding.tvSwEndTime.setText(end);
        });
    }

    @Override
    public void onClick(View v) {
        byte status = (byte) v.getTag();
        SleepDetection sleepDetection = viewModel.getHealthSettingInfo().getSleepDetection();
        sleepDetection.setStatus(status);
        viewModel.sendSettingCmd(sleepDetection);
    }
}
