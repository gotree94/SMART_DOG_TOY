package com.jieli.healthaide.ui.health.weight;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;
import com.jieli.healthaide.data.vo.weight.WeightDayVo;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisDayEntity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisMultipleBaseEntity;
import com.jieli.healthaide.ui.mine.MyTargetFragment;
import com.jieli.healthaide.ui.mine.UserInfoViewModel;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_health_http.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 步数-天界面
 */
public class WeightDayFragment extends WeightDataFragment<WeightDayVo> {
    private float startWeightValue = 0f;//起始体重
    private float targetWeightValue = 0f;//目标体重
    private float currentWeightValue = 0.0f;//当前体重
    private float weightTaskProgress = 0.0f;//体重任务进度
    private float height = 1.71f;
    private View childView;
    private UserInfoViewModel userInfoViewModel;

    public static WeightDayFragment newInstance() {
        return new WeightDayFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        userInfoViewModel.userInfoLiveData.observe(getViewLifecycleOwner(), userInfo -> {
                    if (userInfo.getWeightTarget() > 0) {
                        startWeightValue = userInfo.getWeightStart();
                        targetWeightValue = userInfo.getWeightTarget();
                        height = (float) userInfo.getHeight() / 100;
                        BMI bmi = calculateBMI(currentWeightValue, height);
                        fragmentWeightDataBinding.tvBmi.setText(CalendarUtil.formatString("BMI %.1f  %s", bmi.getBmiValue(), bmi.getBmiDescribe()));
                        updateChartView();
                        updateChartTipTextView();
                        updateAnalysisView();
                    }
                }
        );
        analysisAdapter.setOnItemClickListener((adapter, view, position) -> {
            switch (position) {
                case 0:
                    Bundle bundle = new Bundle();
                    UserInfo userInfo = userInfoViewModel.userInfoLiveData.getValue();
                    float weight;
                    if (currentWeightValue == 0 && userInfo != null) {
                        weight = userInfo.getWeight();
                    } else {
                        weight = currentWeightValue;
                    }
                    bundle.putFloat(WeightSettingFragment.CURRENT_WEIGHT_VALUE, weight);
                    ContentActivity.startContentActivity(getContext(), WeightSettingFragment.class.getCanonicalName(), bundle);
                    break;
                case 1:
                    ContentActivity.startContentActivity(getContext(), MyTargetFragment.class.getCanonicalName());
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        userInfoViewModel.getUserInfo();
    }

    @Override
    protected WeightDayVo createVo() {
        return new WeightDayVo();
    }

    @Override
    protected Chart getChartsView() {
        return null;
    }

    @SuppressLint({"StringFormatMatches"})
    @Override
    protected View getDayChartView() {
        fragmentWeightDataBinding.tvBmi.setVisibility(View.VISIBLE);
        childView = LayoutInflater.from(getContext()).inflate(R.layout.layout_weight_day, null, false);
        updateChartTipTextView();
        return childView;
    }

    @Override
    protected ChartData getChartData() {
        List<WeightBaseVo.WeightBarCharData> sourceDataArray = vo.getEntities();
        if (vo.getHealthEntities().size() == 0) {
            currentWeightValue = 0;
        } else {
            currentWeightValue = (float) sourceDataArray.get(vo.highLightIndex - 1).value;
        }
        mViewModel.timeIntervalLiveData.postValue(currentWeightValue == 0 ? "--" : CustomTimeFormatUtil.getMoment(vo.highLightIndex, CalenderSelectorView.TYPE_DAY));
        mViewModel.timeIntervalWeightValueLiveData.postValue(currentWeightValue == 0 ? "- -" : CalendarUtil.formatString("%.1f", formatWeightValue(currentWeightValue)));
        updateChartView();
        BMI bmi = calculateBMI(currentWeightValue, height);
        fragmentWeightDataBinding.tvBmi.setText(CalendarUtil.formatString("BMI %.1f  %s", bmi.getBmiValue(), bmi.getBmiDescribe()));
        return null;
    }

    @Override
    protected List<AnalysisMultipleBaseEntity> getAnalysisData() {
        ArrayList<AnalysisMultipleBaseEntity> data = new ArrayList<>();
        AnalysisDayEntity settingEntity = new AnalysisDayEntity();
        settingEntity.setAnalysisDescribe(getString(R.string.weight_setting));
        settingEntity.setAnalysisIconSrc(R.drawable.ic_current_weight_set_up);
        data.add(settingEntity);
        AnalysisDayEntity targetEntity = new AnalysisDayEntity();
        targetEntity.setAnalysisDescribe(getString(R.string.weight_target));
        targetEntity.setAnalysisIconSrc(R.drawable.ic_target_weight_set_up);
        String analysisValueString = CalendarUtil.formatString("%.1f%s", formatWeightValue(targetWeightValue), converter.unit());
        targetEntity.setAnalysisValue(analysisValueString);
        data.add(targetEntity);
        return data;
    }

    private void updateChartTipTextView() {
        TextView tvWeightStart = childView.findViewById(R.id.tv_weight_start);
        TextView tvWeightTarget = childView.findViewById(R.id.tv_weight_target);
        tvWeightStart.setText(CalendarUtil.formatString("%s%s", getString(R.string.weight_start_data, formatWeightValue(startWeightValue)), converter.unit()));
        tvWeightTarget.setText(CalendarUtil.formatString("%s%s", getString(R.string.weight_target_data, formatWeightValue(targetWeightValue)), converter.unit()));
    }

    private void updateChartView() {
        weightTaskProgress = currentWeightValue == 0 ? 0 : calculateTaskProgress(currentWeightValue, startWeightValue, targetWeightValue);
        ProgressBar progressBar = childView.findViewById(R.id.progress_bar_weight);
        progressBar.setProgress((int) weightTaskProgress);
    }

    private void updateAnalysisView() {
        analysisAdapter.setList(getAnalysisData());
    }


    private float calculateTaskProgress(float currentVal, float startVal, float targetVal) {//借鉴华为目标和起始不能相同
        float progress;
        if (targetVal > startVal) {//增重
            progress = (currentVal - startVal) / (targetVal - startVal);
        } else if (targetVal < startVal) {//减重
            progress = (startVal - currentVal) / (startVal - targetVal);
        } else {//保持体重
            progress = currentVal == targetVal ? 1 : 0;
        }
        if (progress < 0) {
            progress = 0f;
        } else if (progress > 1) {
            progress = 1f;
        }
        return progress * 100;
    }


    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_DAY;
    }

    private BMI calculateBMI(float weightKg, float heightM) {
        final float LowBMIVal = 18.5f;
        final float NormalBMIVal = 24f;
        final float OverWeightBMIVal1 = 26f;
        final float OverWeightBMIVal2 = 28f;
        String bmiDescribe;
        float bmiVal = weightKg / (heightM * heightM);
        if (bmiVal < LowBMIVal) {
            bmiDescribe = getString(R.string.status_low);
        } else if (bmiVal < NormalBMIVal) {
            bmiDescribe = getString(R.string.status_normal);
        } else if (bmiVal < OverWeightBMIVal1) {
            bmiDescribe = getString(R.string.status_over_weight);
        } else if (bmiVal < OverWeightBMIVal2) {
            bmiDescribe = getString(R.string.status_over_weight);
        } else {
            bmiDescribe = getString(R.string.status_over_obese);
        }
        return new BMI(bmiVal, bmiDescribe);
    }

    private class BMI {
        BMI(float bmiValue, String bmiDescribe) {
            this.bmiValue = bmiValue;
            this.bmiDescribe = bmiDescribe;
        }

        float bmiValue;
        String bmiDescribe;

        public float getBmiValue() {
            return bmiValue;
        }

        public void setBmiValue(float bmiValue) {
            this.bmiValue = bmiValue;
        }

        public String getBmiDescribe() {
            return bmiDescribe;
        }

        public void setBmiDescribe(String bmiDescribe) {
            this.bmiDescribe = bmiDescribe;
        }
    }


}
