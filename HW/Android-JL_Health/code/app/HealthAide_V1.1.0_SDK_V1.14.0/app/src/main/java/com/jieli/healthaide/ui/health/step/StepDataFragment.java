package com.jieli.healthaide.ui.health.step;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.step.StepBaseVo;
import com.jieli.healthaide.databinding.FragmentStepDataBinding;
import com.jieli.healthaide.ui.health.BaseHealthDataFragment;
import com.jieli.healthaide.ui.health.step.adapter.AnalysisAdapter;
import com.jieli.healthaide.ui.health.step.entity.AnalysisEntity;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.Calendar;
import java.util.List;

public abstract class StepDataFragment<T extends StepBaseVo> extends BaseHealthDataFragment implements OnChartValueSelectedListener {
    protected String TAG = getClass().getSimpleName();
    protected FragmentStepDataBinding fragmentStepDataBinding;
    protected StepViewModel mViewModel;
    private Chart chart;
    protected T vo;
    protected long leftTime;
    protected float Y_DEFAULT_EMPTY = 0f;
    private AnalysisAdapter analysisAdapter;
    private boolean isFirstRefreshData = true;

    protected abstract T createVo();

    protected abstract Chart getChartsView();

    protected abstract ChartData getChartData();

    protected abstract void updateChartSetting(int dataLen, int max);

    protected abstract List<AnalysisEntity> getAnalysisData();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentStepDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_step_data, container, false);
        fragmentStepDataBinding.setLifecycleOwner(this);
        return fragmentStepDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        StepViewModel.Factory factory = new StepViewModel.Factory(createVo());
        mViewModel = new ViewModelProvider(this, factory).get(StepViewModel.class);
        fragmentStepDataBinding.setStepViewModel(mViewModel);
        initTimeSelectView();
        initAnalysisView();
        initChartsView();
        mViewModel.getVo().observe(getViewLifecycleOwner(), vo -> {
            this.vo = (T) vo;
            if (vo.getEntities() == null) {
                return;
            }
            refreshData();
        });
    }

    @Override
    protected void onCalendarDialogChangeDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        fragmentStepDataBinding.layoutCalenderSelector.updateTime(calendar.getTimeInMillis());
    }

    @Override
    protected Calendar getCurrentCalendar() {
        long leftTime = fragmentStepDataBinding.layoutCalenderSelector.getLeftTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(leftTime);
        return calendar;
    }

    private void initTimeSelectView() {
        fragmentStepDataBinding.layoutCalenderSelector.setListener((type, leftTime, rightTime) -> {
            this.leftTime = leftTime;
            mViewModel.refresh(getUid(), leftTime, rightTime);
        });
        fragmentStepDataBinding.layoutCalenderSelector.setType(getTimeType());
    }

    private void initAnalysisView() {
        RecyclerView recyclerView = fragmentStepDataBinding.rvSleepDataAnalysis;
        analysisAdapter = new AnalysisAdapter();
        recyclerView.setAdapter(analysisAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void initChartsView() {
        FrameLayout frameLayout = fragmentStepDataBinding.flChartsParent;
        frameLayout.removeAllViews();
        chart = getChartsView();
        frameLayout.addView(chart);
    }

    private void refreshData() {
        analysisAdapter.setList(getAnalysisData());
        ChartData chartData = getChartData();
        if (chart == null) return;
        chart.clear();
        chart.setData(chartData);
        if (null != chartData) {
            updateChartSetting(chartData.getEntryCount(), vo.max);
            updateHighLight(chartData);
        }
    }

    private void updateHighLight(ChartData chartData) {
        if (isResumed() && !isFirstRefreshData) return;
        isFirstRefreshData = false;
        if (chartData != null && chart != null) {
            chart.highlightValue(vo.highLightIndex, 0);
        }
    }

    private String getUid() {
        return HealthApplication.getAppViewModel().getUid();
    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {
        String stepValue = entry.getY() > 0f ? String.valueOf((int) entry.getY()) : "- -";
        mViewModel.timeIntervalStepLiveData.postValue(stepValue);
        mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getTimeInterval(leftTime, entry.getX(), getTimeType()));
    }

    @Override
    public void onNothingSelected() {

    }
}