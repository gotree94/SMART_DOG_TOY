package com.jieli.healthaide.ui.health.heartrate;

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
import com.jieli.healthaide.data.vo.heart_rate.HeartRateBaseVo;
import com.jieli.healthaide.databinding.FragmentHeartRateDataBinding;
import com.jieli.healthaide.ui.health.BaseHealthDataFragment;
import com.jieli.healthaide.ui.health.heartrate.adapter.HeartDescribeAdapter;
import com.jieli.healthaide.ui.health.heartrate.entity.HeartDescribeEntity;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class HeartRateDataFragment<T extends HeartRateBaseVo> extends BaseHealthDataFragment implements OnChartValueSelectedListener {
    protected String TAG = getClass().getSimpleName();
    protected HeartRateViewModel mViewModel;
    protected final int VIEW_TYPE_HEART_RATE_RANGE = 1;
    protected final int VIEW_TYPE_RESETING_HEART_RATE = 2;
    protected int viewType = VIEW_TYPE_HEART_RATE_RANGE;
    protected Chart chart;
    protected long leftTime;
    protected T vo;
    protected boolean isFirstRefreshData = true;
    private FragmentHeartRateDataBinding fragmentHeartRateDataBinding;
    private HeartDescribeAdapter analysisAdapter;
    private final String EMPTY = "- -";

    protected abstract T createVo();

    protected abstract Chart getChartsView();

    protected abstract ChartData getChartData();

    protected abstract void changeViewType(int viewType);

    protected void updateChartSetting(int dataLen) {
    }

    protected abstract void updateHighLight(ChartData chartData);

    protected void refreshDataFinish() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentHeartRateDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_heart_rate_data, container, false);
        fragmentHeartRateDataBinding.setLifecycleOwner(this);
        return fragmentHeartRateDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HeartRateViewModel.Factory factory = new HeartRateViewModel.Factory(createVo());
        mViewModel = new ViewModelProvider(this, factory).get(HeartRateViewModel.class);
        fragmentHeartRateDataBinding.setHeartRateViewModel(mViewModel);
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
        fragmentHeartRateDataBinding.layoutCalenderSelector.updateTime(calendar.getTimeInMillis());
    }

    @Override
    protected Calendar getCurrentCalendar() {
        long leftTime = fragmentHeartRateDataBinding.layoutCalenderSelector.getLeftTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(leftTime);
        return calendar;
    }

    private void initTimeSelectView() {
        fragmentHeartRateDataBinding.layoutCalenderSelector.setListener((type, leftTime, rightTime) -> {
            this.leftTime = leftTime;
            mViewModel.refresh(getUid(), leftTime, rightTime);
        });
        fragmentHeartRateDataBinding.layoutCalenderSelector.setType(getTimeType());
    }

    private void initAnalysisView() {
        RecyclerView recyclerView = fragmentHeartRateDataBinding.rvHeartRateAnalysis;
        analysisAdapter = new HeartDescribeAdapter();
        recyclerView.setAdapter(analysisAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        analysisAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (analysisAdapter.getSelectedItemPosition() == position) return;
            analysisAdapter.setSelectedItem(position);
            changeViewType(position + 1);
            viewType = position + 1;
        });
    }

    private void initChartsView() {
        FrameLayout frameLayout = fragmentHeartRateDataBinding.flChartsParent;
        frameLayout.removeAllViews();
        chart = getChartsView();
        frameLayout.addView(chart);
        chart.setOnChartValueSelectedListener(this);
    }

    private void refreshData() {
        analysisAdapter.setList(getAnalysisData());
        ChartData chartData = getChartData();
        if (chart == null) return;
        chart.clear();
        chart.setData(chartData);
        if (null != chartData) {
            updateChartSetting(chartData.getEntryCount() / 2);
            updateHighLight(chartData);
        }
        chart.invalidate();
        refreshDataFinish();
    }

    protected List<HeartDescribeEntity> getAnalysisData() {
        ArrayList<HeartDescribeEntity> heartDescribeEntities = new ArrayList<>();
        String rangeRate;
        if (vo.max != 0) {
            rangeRate = CalendarUtil.formatString("%d-%d", (int) vo.min, (int) vo.max);
        } else {
            rangeRate = EMPTY;
        }
        String restingRate;
        if (vo.restingAvg != 0) {
            restingRate = CalendarUtil.formatString("%d", (int) vo.restingAvg);
        } else {
            restingRate = EMPTY;
        }
        heartDescribeEntities.add(new HeartDescribeEntity(R.drawable.ic_heart_rate_describe, R.string.heart_rate_range, rangeRate));
        heartDescribeEntities.add(new HeartDescribeEntity(R.drawable.ic_heart_rate_describe, R.string.resting_heart_rate, restingRate));
        return heartDescribeEntities;
    }

    private String getUid() {
        return HealthApplication.getAppViewModel().getUid();
    }

    @Override
    public void onNothingSelected() {

    }
}