package com.jieli.healthaide.ui.device.health;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentHealthOptionBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.health.ExerciseHeartRateReminder;
import com.jieli.jl_rcsp.model.device.health.FallDetection;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.model.device.health.HeartRateMeasure;
import com.jieli.jl_rcsp.model.device.health.SedentaryReminder;
import com.jieli.jl_rcsp.model.device.health.SleepDetection;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;

/**
 * 设备的健康设置
 */
public class HealthOptionFragment extends BaseHealthSettingFragment implements OnItemClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentHealthOptionBinding mBinding;
    private HealthOptionAdapter adapter;
    private final static int TYPE_SCIENCE_SLEEP = 1;
    private final static int TYPE_SEDENTARY_REMINDER = 2;
    private final static int TYPE_MEASURE_HEART_RATE = 3;
    private final static int TYPE_EARLY_WARMING = 4;
    private final static int TYPE_PRESSURE_AUTO = 5;
    private final static int TYPE_FALL_DETECTION = 6;

    public HealthOptionFragment() {
    }

    public static HealthOptionFragment newInstance() {
        return new HealthOptionFragment();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHealthOptionBinding.inflate(inflater, container, false);
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.tab_health);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(view1 -> requireActivity().onBackPressed());
        adapter = new HealthOptionAdapter();
        mBinding.rvHealthOption.setAdapter(adapter);
        mBinding.rvHealthOption.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        mBinding.rvHealthOption.addItemDecoration(decoration);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemChildClickListener(this::onItemClick);

        mBinding.tvPersonInfo.setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), UserInfoFragment.class.getCanonicalName()));
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), this::updateHealthOptionView);
        viewModel.mDeviceConfigureMLD.observe(getViewLifecycleOwner(), device -> updateHealthOptionView(viewModel.getHealthSettingInfo()));

        //int mask = 0xFC
        int mask = 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_SLEEP_DETECTION
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_FALL_DETECTION
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_AUTOMATIC_PRESSURE_DETECTION
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_HEART_RATE_MEASURE
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_EXERCISE_HEART_RATE_REMINDER
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_SEDENTARY_REMINDER;
        viewModel.requestHealthSettingInfo(mask);
    }

    private void updateHealthOptionView(HealthSettingInfo healthSettingInfo) {
        if (null == healthSettingInfo || null == adapter) return;
        ArrayList<HealthOptionItem> dataArray = new ArrayList<>();
        String[] statusString = getResources().getStringArray(R.array.sw_status_string_array);

        WatchConfigure configure = viewModel.getWatchConfigure();
        boolean isShowPersonalInfo = configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null
                && configure.getSportHealthConfigure().getCombineFunc().isSupportPersonalInfo();
        mBinding.tvPersonInfo.setVisibility(isShowPersonalInfo ? View.VISIBLE : View.GONE);

        boolean isShowSleep = configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null
                && configure.getSportHealthConfigure().getCombineFunc().isSupportSleepDetection();
        if (isShowSleep) {
            HealthOptionItem sleepScienceItem = new HealthOptionItem();
            sleepScienceItem.setType(TYPE_SCIENCE_SLEEP);
            sleepScienceItem.setTitle(getString(R.string.science_sleep));
            sleepScienceItem.setShowNext(true);
            sleepScienceItem.setShowSw(false);
            SleepDetection sleepDetection = healthSettingInfo.getSleepDetection();
            sleepScienceItem.setTailString(statusString[sleepDetection.getStatus()]);
            dataArray.add(sleepScienceItem);
        }

        boolean isShowAlter = configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null
                && configure.getSportHealthConfigure().getCombineFunc().isSupportSedentaryReminder();
        if (isShowAlter) {
            HealthOptionItem sedentaryReminderItem = new HealthOptionItem();
            sedentaryReminderItem.setType(TYPE_SEDENTARY_REMINDER);
            sedentaryReminderItem.setTitle(getString(R.string.sedentary_reminder));
            sedentaryReminderItem.setShowNext(true);
            sedentaryReminderItem.setShowSw(false);
            SedentaryReminder sedentaryReminder = healthSettingInfo.getSedentaryReminder();
            sedentaryReminderItem.setTailString(statusString[sedentaryReminder.getStatus()]);
            sedentaryReminderItem.setTailString(getString(sedentaryReminder.getStatus() == SedentaryReminder.STATUS_OPEN ? R.string.turned_on : R.string.turned_off));
            sedentaryReminderItem.setHintText(getString(R.string.sedentary_reminder_tip));
            dataArray.add(sedentaryReminderItem);
        }

        boolean isShowContinuousTest = configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getRateFunc() != null
                && configure.getSportHealthConfigure().getRateFunc().isEnableContinuousTest();
        if (isShowContinuousTest) {
            HealthOptionItem measureHeartRateItem = new HealthOptionItem();
            measureHeartRateItem.setType(TYPE_MEASURE_HEART_RATE);
            measureHeartRateItem.setTitle(getString(R.string.continuous_measurement_heart_rate));
            measureHeartRateItem.setShowNext(true);
            HeartRateMeasure heartRateMeasure = healthSettingInfo.getHeartRateMeasure();
            measureHeartRateItem.setTailString(getString(heartRateMeasure.isEnable() ? R.string.turned_on : R.string.turned_off));
            dataArray.add(measureHeartRateItem);
        }

        boolean isShowSportRateReminder = configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null
                && configure.getSportHealthConfigure().getCombineFunc().isSupportSportRateReminder();
        if (isShowSportRateReminder) {
            HealthOptionItem earlyWarningItem = new HealthOptionItem();
            earlyWarningItem.setType(TYPE_EARLY_WARMING);
            earlyWarningItem.setTitle(getString(R.string.heart_rate_range_early_warning));
            ExerciseHeartRateReminder exerciseHeartRateReminder = healthSettingInfo.getExerciseHeartRateReminder();
            earlyWarningItem.setTailString(getString(exerciseHeartRateReminder.isEnable() ? R.string.turned_on : R.string.turned_off));
            if (exerciseHeartRateReminder.isEnable()) {
                earlyWarningItem.setHintText(getString(R.string.tip_heart_rate_limit_warn, exerciseHeartRateReminder.getMax()));
            }
            earlyWarningItem.setShowNext(true);
            dataArray.add(earlyWarningItem);
        }

//        HealthOptionItem pressureAutoItem = new HealthOptionItem();
//        pressureAutoItem.setType(TYPE_PRESSURE_AUTO);
//        pressureAutoItem.setTitle(getString(R.string.pressure_auto_testing));
//        pressureAutoItem.setShowNext(false);
//        AutomaticPressureDetection automaticPressureDetection = healthSettingInfo.getAutomaticPressureDetection();
//        pressureAutoItem.setTailString(getString(automaticPressureDetection.isEnable() ? R.string.turned_on : R.string.turned_off));
//        dataArray.add(pressureAutoItem);


        boolean isShowFallDetection = configure == null || configure.getSportHealthConfigure() != null && configure.getSportHealthConfigure().getCombineFunc() != null
                && configure.getSportHealthConfigure().getCombineFunc().isSupportFallDetection();
        if (isShowFallDetection) {
            FallDetection fallDetection = healthSettingInfo.getFallDetection();
            HealthOptionItem fallDetectionItem = new HealthOptionItem();
            fallDetectionItem.setSwChecked(fallDetection.isEnable());
            fallDetectionItem.setTitle(getString(R.string.fall_detection));
            fallDetectionItem.setShowNext(true);
            fallDetectionItem.setType(TYPE_FALL_DETECTION);
            fallDetectionItem.setTailString(getString(fallDetection.isEnable() ? R.string.turned_on : R.string.turned_off));
            fallDetectionItem.setShowSw(false);
            dataArray.add(fallDetectionItem);
        }

        adapter.setList(dataArray);
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        HealthOptionItem healthOptionItem = (HealthOptionItem) adapter.getData().get(position);
        JL_Log.d(TAG, "onItemClick", "type : " + healthOptionItem.getType());
        switch (healthOptionItem.getType()) {
            case TYPE_SCIENCE_SLEEP:
                ContentActivity.startContentActivity(requireContext(), SleepDetectionFragment.class.getCanonicalName());
                break;
            case TYPE_SEDENTARY_REMINDER:
                ContentActivity.startContentActivity(requireContext(), SedentaryReminderFragment.class.getCanonicalName());
                break;
            case TYPE_MEASURE_HEART_RATE:
                ContentActivity.startContentActivity(requireContext(), MeasureHeartRateFragment.class.getCanonicalName());
                break;
            case TYPE_EARLY_WARMING:
                ContentActivity.startContentActivity(requireContext(), EarlyWarningFragment.class.getCanonicalName());
                break;
            case TYPE_PRESSURE_AUTO:
                ContentActivity.startContentActivity(requireContext(), PressureAutoFragment.class.getCanonicalName());
                break;
            case TYPE_FALL_DETECTION:
                ContentActivity.startContentActivity(requireContext(), FallDetectionFragment.class.getCanonicalName());
                break;
        }
    }
}