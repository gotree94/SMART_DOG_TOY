package com.jieli.healthaide.ui.device.health;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.HealthSettingViewModel;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.model.device.health.HeartRateMeasure;
import com.kyleduo.switchbutton.SwitchButton;

import org.jetbrains.annotations.NotNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/22
 * @desc :
 */
public class MeasureHeartRateFragment extends BaseHealthSettingFragment {

     private SwitchButton sw;
    private ImageView ivSmart;
    private ImageView ivReal;
    ConstraintLayout clSmart;
    ConstraintLayout clReal;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_measure_heart_rate, container, false);
        sw = root.findViewById(R.id.sw_measure_heart_rate);
        ivSmart = root.findViewById(R.id.iv_heart_rate_smart);
        ivReal = root.findViewById(R.id.iv_heart_rate_real);

        ivReal.setVisibility(View.GONE);
        ivSmart.setVisibility(View.GONE);
        clSmart = root.findViewById(R.id.cl_heart_rate_smart);
        clReal = root.findViewById(R.id.cl_heart_rate_real);

        clSmart.setOnClickListener(v -> {
            HeartRateMeasure heartRateMeasure = viewModel.getHealthSettingInfo().getHeartRateMeasure();
            heartRateMeasure.setMode((byte) 0x00);
            viewModel.sendSettingCmd(heartRateMeasure);
        });

        clReal.setOnClickListener(v -> {
            HeartRateMeasure heartRateMeasure = viewModel.getHealthSettingInfo().getHeartRateMeasure();
            heartRateMeasure.setMode((byte) 0x01);
            viewModel.sendSettingCmd(heartRateMeasure);
        });
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //todo 心率连续测量
            HeartRateMeasure heartRateMeasure = viewModel.getHealthSettingInfo().getHeartRateMeasure();
            heartRateMeasure.setEnable(isChecked);
            viewModel.sendSettingCmd(heartRateMeasure);
        });

        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.continuous_measurement_heart_rate);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HealthSettingViewModel.class);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), new Observer<HealthSettingInfo>() {
            @Override
            public void onChanged(HealthSettingInfo healthSettingInfo) {
                HeartRateMeasure heartRateMeasure = healthSettingInfo.getHeartRateMeasure();
                sw.setCheckedNoEvent(heartRateMeasure.isEnable());

                clReal.setClickable(heartRateMeasure.isEnable());
                clSmart.setClickable(heartRateMeasure.isEnable());
                clReal.setAlpha(heartRateMeasure.isEnable()  ? 1.0f : 0.4f);
                clSmart.setAlpha(heartRateMeasure.isEnable()  ? 1.0f : 0.4f);

                ivReal.setVisibility(heartRateMeasure.getMode() == 0x01 && heartRateMeasure.isEnable() ? View.VISIBLE : View.GONE);
                ivSmart.setVisibility(heartRateMeasure.getMode() == 0x00 && heartRateMeasure.isEnable() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
