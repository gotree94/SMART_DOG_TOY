package com.jieli.healthaide.ui.device.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentLiftWristDetectionBinding;
import com.jieli.healthaide.ui.device.health.BaseHealthSettingFragment;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog;
import com.jieli.healthaide.ui.dialog.ChooseTimeDialog2;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.model.device.health.LiftWristDetection;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/25
 * @desc :
 */
public class LiftWristDetectionFragment extends BaseHealthSettingFragment implements View.OnClickListener {
    FragmentLiftWristDetectionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lift_wrist_detection, container, false);
        binding = FragmentLiftWristDetectionBinding.bind(root);
        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.bright_screen);
        binding.tvSwStatusClose.setTag(LiftWristDetection.STATUS_CLOSE);
        binding.tvSwStatusOpenAllDay.setTag(LiftWristDetection.STATUS_ALL_DAY);
        binding.tvSwStatusSwTimeOpen.setTag(LiftWristDetection.STATUS_CUSTOM_TIME);

        binding.tvSwStatusSwTimeOpen.setOnClickListener(this);
        binding.tvSwStatusOpenAllDay.setOnClickListener(this);
        binding.tvSwStatusClose.setOnClickListener(this);

        binding.rlStartTime.setOnClickListener(v -> {
            LiftWristDetection liftWristDetection = viewModel.getHealthSettingInfo().getLiftWristDetection();
            ChooseTimeDialog2 chooseTimeDialog = new ChooseTimeDialog2(liftWristDetection.getStartHour(), liftWristDetection.getStartMin(), R.string.start_time, (hour, minute) -> {
                liftWristDetection.setStartHour((byte) hour)
                        .setStartMin((byte) minute);
                if (equalsTime(liftWristDetection.getStartHour(), liftWristDetection.getStartMin(), liftWristDetection.getEndHour(), liftWristDetection.getEndMin())) {
                    int tmp = hour + 1;
                    tmp = tmp > 23 ? 0 : tmp;
                    liftWristDetection.setEndMin((byte) minute)
                            .setEndHour((byte) tmp);
                }
                viewModel.sendSettingCmd(liftWristDetection);
            });
            chooseTimeDialog.show(getChildFragmentManager(), ChooseTimeDialog.class.getSimpleName());
        });


        binding.rlEndTime.setOnClickListener(v -> {
            LiftWristDetection liftWristDetection = viewModel.getHealthSettingInfo().getLiftWristDetection();
            ChooseTimeDialog2 chooseTimeDialog = new ChooseTimeDialog2(liftWristDetection.getStartHour(), liftWristDetection.getStartMin(), R.string.end_time, (hour, minute) -> {
                liftWristDetection.setEndHour((byte) hour);
                liftWristDetection.setEndMin((byte) minute);
                if (equalsTime(liftWristDetection.getStartHour(), liftWristDetection.getStartMin(), liftWristDetection.getEndHour(), liftWristDetection.getEndMin())) {
                    liftWristDetection.setStartMin((byte) minute);
                    int tmp = hour - 1;
                    tmp = tmp < 0 ? 23 : tmp;
                    liftWristDetection.setStartHour((byte) tmp);
                }
                viewModel.sendSettingCmd(liftWristDetection);
            });
            chooseTimeDialog.show(getChildFragmentManager(), ChooseTimeDialog.class.getSimpleName());
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            LiftWristDetection liftWristDetection = healthSettingInfo.getLiftWristDetection();
            binding.tvSwStatusClose.setCompoundDrawablesWithIntrinsicBounds(0, 0, liftWristDetection.getStatus() == LiftWristDetection.STATUS_CLOSE ? R.drawable.ic_choose_blue : 0, 0);
            binding.tvSwStatusOpenAllDay.setCompoundDrawablesWithIntrinsicBounds(0, 0, liftWristDetection.getStatus() == LiftWristDetection.STATUS_ALL_DAY ? R.drawable.ic_choose_blue : 0, 0);
            binding.tvSwStatusSwTimeOpen.setCompoundDrawablesWithIntrinsicBounds(0, 0, liftWristDetection.getStatus() == LiftWristDetection.STATUS_CUSTOM_TIME ? R.drawable.ic_choose_blue : 0, 0);


            binding.rlEndTime.setAlpha(liftWristDetection.getStatus() == LiftWristDetection.STATUS_CUSTOM_TIME ? 1.0f : 0.4f);
            binding.rlEndTime.setClickable(liftWristDetection.getStatus() == LiftWristDetection.STATUS_CUSTOM_TIME);
            binding.rlStartTime.setAlpha(liftWristDetection.getStatus() == LiftWristDetection.STATUS_CUSTOM_TIME ? 1.0f : 0.4f);
            binding.rlStartTime.setClickable(liftWristDetection.getStatus() == LiftWristDetection.STATUS_CUSTOM_TIME);

            String start = CalendarUtil.formatString("%02d:%02d", liftWristDetection.getStartHour(), liftWristDetection.getStartMin());
            String end = CalendarUtil.formatString("%02d:%02d", liftWristDetection.getEndHour(), liftWristDetection.getEndMin());

            if (isSmall(liftWristDetection.getEndHour(), liftWristDetection.getEndMin(), liftWristDetection.getStartHour(), liftWristDetection.getStartMin())) {
                end = getString(R.string.next_day) + " " + end;
            }
            binding.tvSwStartTime.setText(start);
            binding.tvSwEndTime.setText(end);
        });
    }

    @Override
    public void onClick(View v) {
        byte status = (byte) v.getTag();
        HealthSettingInfo healthSettingInfo = viewModel.getHealthSettingInfo();
        if(null == healthSettingInfo) return;
        LiftWristDetection liftWristDetection = healthSettingInfo.getLiftWristDetection();
        liftWristDetection.setStatus(status);
        viewModel.sendSettingCmd(liftWristDetection);
    }
}
