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
import com.jieli.healthaide.databinding.FragmentSedentaryReminderBinding;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog2;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.model.device.health.SedentaryReminder;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/25
 * @desc :
 */
public class SedentaryReminderFragment extends BaseHealthSettingFragment {
    FragmentSedentaryReminderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sedentary_reminder, container, false);
        binding = FragmentSedentaryReminderBinding.bind(root);
        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.sedentary_reminder);


        binding.rlStartTime.setOnClickListener(v -> {
            SedentaryReminder sedentaryReminder = viewModel.getHealthSettingInfo().getSedentaryReminder();

            ChooseTimeDialog2 chooseTimeDialog = new ChooseTimeDialog2(sedentaryReminder.getStartHour(), sedentaryReminder.getStartMin(), R.string.start_time, (hour, minute) -> {
                sedentaryReminder.setStartHour((byte) hour);
                sedentaryReminder.setStartMin((byte) minute);


                if (equalsTime(sedentaryReminder.getStartHour(), sedentaryReminder.getStartMin(), sedentaryReminder.getEndHour(), sedentaryReminder.getEndMin())) {
                    sedentaryReminder.setEndMin((byte) minute);
                    int tmp = hour + 1;
                    tmp = tmp > 23 ? 0 : tmp;
                    sedentaryReminder.setEndHour((byte) tmp);
                }

                viewModel.sendSettingCmd(sedentaryReminder);
            });
            chooseTimeDialog.show(getChildFragmentManager(), ChooseTimeDialog.class.getSimpleName());
        });


        binding.rlEndTime.setOnClickListener(v -> {
            SedentaryReminder sedentaryReminder = viewModel.getHealthSettingInfo().getSedentaryReminder();

            ChooseTimeDialog2 chooseTimeDialog = new ChooseTimeDialog2(sedentaryReminder.getEndHour(), sedentaryReminder.getEndMin(), R.string.end_time, (hour, minute) -> {
                sedentaryReminder.setEndHour((byte) hour);
                sedentaryReminder.setEndMin((byte) minute);
                if (equalsTime(sedentaryReminder.getStartHour(), sedentaryReminder.getStartMin(), sedentaryReminder.getEndHour(), sedentaryReminder.getEndMin())) {
                    sedentaryReminder.setStartMin((byte) minute);
                    int tmp = hour - 1;
                    tmp = tmp < 0 ? 23 : tmp;
                    sedentaryReminder.setStartHour((byte) tmp);
                }
                viewModel.sendSettingCmd(sedentaryReminder);
            });
            chooseTimeDialog.show(getChildFragmentManager(), ChooseTimeDialog.class.getSimpleName());
        });

        binding.swSedentaryReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SedentaryReminder sedentaryReminder = viewModel.getHealthSettingInfo().getSedentaryReminder();
            sedentaryReminder.setStatus(isChecked ? SedentaryReminder.STATUS_OPEN : SedentaryReminder.STATUS_CLOSE);
            viewModel.sendSettingCmd(sedentaryReminder);
        });
        binding.swSedentaryReminderFreeLunchBreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SedentaryReminder sedentaryReminder = viewModel.getHealthSettingInfo().getSedentaryReminder();
            sedentaryReminder.setFreeLunchBreak(isChecked);
            viewModel.sendSettingCmd(sedentaryReminder);
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            SedentaryReminder sedentaryReminder = healthSettingInfo.getSedentaryReminder();

            binding.rlEndTime.setAlpha(sedentaryReminder.getStatus() == 0x01 ? 1.0f : 0.4f);
            binding.rlEndTime.setClickable(sedentaryReminder.getStatus() == 0x01);
            binding.rlStartTime.setAlpha(sedentaryReminder.getStatus() == 0x01 ? 1.0f : 0.4f);
            binding.rlStartTime.setClickable(sedentaryReminder.getStatus() == 0x01);

            binding.swSedentaryReminder.setCheckedImmediatelyNoEvent(sedentaryReminder.getStatus() == 0x01);
            binding.swSedentaryReminderFreeLunchBreak.setCheckedImmediatelyNoEvent(sedentaryReminder.isFreeLunchBreak());


            binding.swSedentaryReminderFreeLunchBreak.setEnabled(sedentaryReminder.getStatus() == 0x01);
            binding.clFreeLunchBreak.setAlpha(sedentaryReminder.getStatus() == 0x01 ? 1.0f : 0.4f);

            @SuppressLint("DefaultLocale") String start = CalendarUtil.formatString("%02d:%02d", sedentaryReminder.getStartHour(), sedentaryReminder.getStartMin());
            @SuppressLint("DefaultLocale") String end = CalendarUtil.formatString("%02d:%02d", sedentaryReminder.getEndHour(), sedentaryReminder.getEndMin());

            if (isSmall(sedentaryReminder.getEndHour(), sedentaryReminder.getEndMin(), sedentaryReminder.getStartHour(), sedentaryReminder.getStartMin())) {
                end = getString(R.string.next_day) + " " + end;
            }
            binding.tvSwStartTime.setText(start);
            binding.tvSwEndTime.setText(end);
        });
    }


}
