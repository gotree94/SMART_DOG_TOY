package com.jieli.healthaide.ui.device.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentSensorSettingBinding;
import com.jieli.healthaide.ui.device.health.BaseHealthSettingFragment;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.model.device.health.SensorInfo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/9
 * @desc :
 */
public class SensorSettingFragment extends BaseHealthSettingFragment {

    private FragmentSensorSettingBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sensor_setting, container, false);
        binding = FragmentSensorSettingBinding.bind(root);
        binding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.viewTopbar.tvTopbarTitle.setText(R.string.sensor_setting);
        binding.swStepSensor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getStepSensor().setEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swStepSensorRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getStepSensor().setRecordEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swHeartRateSensor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getHeartRateSensor().setEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swHeartRateSensorRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getHeartRateSensor().setRecordEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swBloodOxygenSensor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getBloodOxygenSensor().setEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swBloodOxygenSensorRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getBloodOxygenSensor().setRecordEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swAltitudePressureSensor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getAltitudePressureSensor().setEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        binding.swAltitudePressureSensorRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SensorInfo sensorInfo = getSensorInfo();
            if (sensorInfo == null) return;
            sensorInfo.getAltitudePressureSensor().setRecordEnable(isChecked);
            viewModel.sendSettingCmd(sensorInfo);
        });
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.requestHealthSettingInfo(0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_SENSOR);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), healthSettingInfo -> {
            SensorInfo sensorInfo = healthSettingInfo.getSensorInfo();
            binding.rlStepSensorRecord.setAlpha(sensorInfo.getStepSensor().isEnable() ? 1.0f : DISABLE_ALPHA);
            binding.swStepSensor.setCheckedNoEvent(sensorInfo.getStepSensor().isEnable());
            binding.swStepSensorRecord.setEnabled(sensorInfo.getStepSensor().isEnable());
            binding.swStepSensorRecord.setCheckedNoEvent(sensorInfo.getStepSensor().isRecordEnable());

            binding.rlHeartRateSensorRecord.setAlpha(sensorInfo.getHeartRateSensor().isEnable() ? 1.0f : DISABLE_ALPHA);
            binding.swHeartRateSensor.setCheckedNoEvent(sensorInfo.getHeartRateSensor().isEnable());
            binding.swHeartRateSensorRecord.setEnabled(sensorInfo.getHeartRateSensor().isEnable());
            binding.swHeartRateSensorRecord.setCheckedNoEvent(sensorInfo.getHeartRateSensor().isRecordEnable());

            binding.rlBloodOxygenSensorRecord.setAlpha(sensorInfo.getBloodOxygenSensor().isEnable() ? 1.0f : DISABLE_ALPHA);
            binding.swBloodOxygenSensor.setCheckedNoEvent(sensorInfo.getBloodOxygenSensor().isEnable());
            binding.swBloodOxygenSensorRecord.setEnabled(sensorInfo.getBloodOxygenSensor().isEnable());
            binding.swBloodOxygenSensorRecord.setCheckedNoEvent(sensorInfo.getBloodOxygenSensor().isRecordEnable());

            binding.rlAltitudePressureSensorRecord.setAlpha(sensorInfo.getAltitudePressureSensor().isEnable() ? 1.0f : DISABLE_ALPHA);
            binding.swAltitudePressureSensor.setCheckedNoEvent(sensorInfo.getAltitudePressureSensor().isEnable());
            binding.swAltitudePressureSensorRecord.setEnabled(sensorInfo.getAltitudePressureSensor().isEnable());
            binding.swAltitudePressureSensorRecord.setCheckedNoEvent(sensorInfo.getAltitudePressureSensor().isRecordEnable());
        });

    }


    private SensorInfo getSensorInfo() {
        return viewModel.getSensorInfo();
    }


}
