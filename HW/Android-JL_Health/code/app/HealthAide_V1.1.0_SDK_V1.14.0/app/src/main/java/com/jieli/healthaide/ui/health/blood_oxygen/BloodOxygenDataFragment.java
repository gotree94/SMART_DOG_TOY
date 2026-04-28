package com.jieli.healthaide.ui.health.blood_oxygen;

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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.blood_oxygen.BloodOxygenBaseVo;
import com.jieli.healthaide.databinding.FragmentBloodOxygenDataBinding;
import com.jieli.healthaide.ui.health.BaseHealthDataFragment;
import com.jieli.healthaide.ui.health.blood_oxygen.adapter.AnalysisAdapter;
import com.jieli.healthaide.ui.health.blood_oxygen.entity.AnalysisEntity;

import java.util.Calendar;
import java.util.List;


public abstract class BloodOxygenDataFragment<T extends BloodOxygenBaseVo> extends BaseHealthDataFragment implements OnChartValueSelectedListener {
    protected String TAG = getClass().getSimpleName();
    protected FragmentBloodOxygenDataBinding fragmentBloodOxygenDataBinding;
    protected BloodOxygenViewModel mViewModel;
    protected long leftTime;
    protected T vo;
    private Chart chart;
    private AnalysisAdapter analysisAdapter;
    protected final String EMPTY = "- -";
    private boolean isFirstRefreshData = true;


    protected abstract T createVo();

    protected abstract Chart getChartsView();

    protected abstract List<AnalysisEntity> getAnalysisData();

    protected abstract ChartData getChartData();

    protected void updateChartSetting(int dataLen) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentBloodOxygenDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_blood_oxygen_data, container, false);
        fragmentBloodOxygenDataBinding.setLifecycleOwner(this);
        return fragmentBloodOxygenDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BloodOxygenViewModel.Factory factory = new BloodOxygenViewModel.Factory(createVo());
        mViewModel = new ViewModelProvider(this, factory).get(BloodOxygenViewModel.class);
        fragmentBloodOxygenDataBinding.setBloodOxygenViewModel(mViewModel);
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
        fragmentBloodOxygenDataBinding.layoutCalenderSelector.updateTime(calendar.getTimeInMillis());
    }

    @Override
    protected Calendar getCurrentCalendar() {
        long leftTime = fragmentBloodOxygenDataBinding.layoutCalenderSelector.getLeftTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(leftTime);
        return calendar;
    }

    private void initChartsView() {
        FrameLayout frameLayout = fragmentBloodOxygenDataBinding.flChartsParent;
        frameLayout.removeAllViews();
        chart = getChartsView();
        frameLayout.addView(chart);
        chart.setOnChartValueSelectedListener(this);
    }

    private void initAnalysisView() {
        //todo 分析统计结果
        RecyclerView recyclerView = fragmentBloodOxygenDataBinding.rvBloodOxygenDataAnalysis;
        analysisAdapter = new AnalysisAdapter();
        recyclerView.setAdapter(analysisAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void initTimeSelectView() {
        fragmentBloodOxygenDataBinding.layoutCalenderSelector.setListener((type, leftTime, rightTime) -> {
            this.leftTime = leftTime;
            mViewModel.refresh(getUid(), leftTime, rightTime);
        });
        fragmentBloodOxygenDataBinding.layoutCalenderSelector.setType(getTimeType());
    }

    protected void refreshData() {
        analysisAdapter.setList(getAnalysisData());
        ChartData chartData = getChartData();
        if (chart == null) return;
        chart.clear();
        chart.setData(chartData);
        updateHighLight(chartData);
        if (null != chartData) {
            updateChartSetting(chartData.getEntryCount());
        }
    }

    private void updateHighLight(ChartData chartData) {
        if (isResumed() && !isFirstRefreshData) return;
        isFirstRefreshData = false;
        if (null != chartData) {
            chart.highlightValue(vo.highLightIndex, 0);
        }
    }

    private String getUid() {
        return HealthApplication.getAppViewModel().getUid();
    }

    @Override
    public void onNothingSelected() {

    }
}