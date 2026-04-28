package com.jieli.healthaide.ui.health.weight;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;
import com.jieli.healthaide.databinding.FragmentWeightDataBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.ui.health.BaseHealthDataFragment;
import com.jieli.healthaide.ui.health.weight.adapter.AnalysisAdapter;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChartRenderer;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisMultipleBaseEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.Calendar;
import java.util.List;

import static com.jieli.healthaide.tool.unit.BaseUnitConverter.TYPE_METRIC;


public abstract class WeightDataFragment<T extends WeightBaseVo> extends BaseHealthDataFragment implements OnChartValueSelectedListener {
    protected String TAG = getClass().getSimpleName();
    protected FragmentWeightDataBinding fragmentWeightDataBinding;
    protected WeightViewModel mViewModel;
    protected Chart chart;
    protected AnalysisAdapter analysisAdapter;
    protected T vo;
    protected long leftTime;
    private boolean isFirstRefreshData = true;
    protected Converter converter = new KGUnitConverter().getConverter(BaseUnitConverter.getType());

    protected abstract T createVo();

    protected abstract Chart getChartsView();

    protected abstract ChartData getChartData();

    protected abstract List<AnalysisMultipleBaseEntity> getAnalysisData();

    protected View getDayChartView() {
        return null;
    }

    protected void updateChartSetting(ChartData chartData) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentWeightDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_weight_data, container, false);
        fragmentWeightDataBinding.setLifecycleOwner(this);
        fragmentWeightDataBinding.tvWeightUnit.setText(converter.unit());
        return fragmentWeightDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WeightViewModel.Factory factory = new WeightViewModel.Factory(createVo());
        mViewModel = new ViewModelProvider(this, factory).get(WeightViewModel.class);
        fragmentWeightDataBinding.setWeightViewModel(mViewModel);
        mViewModel.getVo().observe(getViewLifecycleOwner(), vo -> {
            this.vo = (T) vo;
            if (vo.getEntities() == null) {
                return;
            }
            refreshData();
        });
        initTimeSelectView();
        initAnalysisView();
        initChartsView();
    }

    @Override
    protected void onCalendarDialogChangeDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        fragmentWeightDataBinding.layoutCalenderSelector.updateTime(calendar.getTimeInMillis());
    }

    @Override
    protected Calendar getCurrentCalendar() {
        long leftTime = fragmentWeightDataBinding.layoutCalenderSelector.getLeftTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(leftTime);
        return calendar;
    }

    private void initTimeSelectView() {
        fragmentWeightDataBinding.layoutCalenderSelector.setListener((type, leftTime, rightTime) -> {
            this.leftTime = leftTime;
            mViewModel.refresh(getUid(), leftTime, rightTime);
        });
        fragmentWeightDataBinding.layoutCalenderSelector.setType(getTimeType());
    }

    private void initAnalysisView() {
        RecyclerView recyclerView = fragmentWeightDataBinding.rvAnalysis;
        analysisAdapter = new AnalysisAdapter();
        recyclerView.setAdapter(analysisAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void initChartsView() {
        FrameLayout frameLayout = fragmentWeightDataBinding.flChartsParent;
        frameLayout.removeAllViews();
        chart = getChartsView();
        if (chart != null) {
            frameLayout.addView(chart);
            if (getTimeType() == CalenderSelectorView.TYPE_YEAR) {
                fragmentWeightDataBinding.tvWeightAvg.setText(getString(R.string.month_average));
            }
            chart.setOnChartValueSelectedListener(this);

        } else {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) frameLayout.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            frameLayout.setLayoutParams(params);
            frameLayout.addView(getDayChartView());
        }
    }

    private void refreshData() {
        analysisAdapter.setList(getAnalysisData());
        ChartData chartData = getChartData();
        if (chart == null) return;
        chart.clear();
        chart.setData(chartData);
        updateChartSetting(chartData);
        updateHighLight(chartData);
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
    public void onValueSelected(Entry entry, Highlight h) {
        if (entry.getY() != WeightLineChartRenderer.SKIP_Y) {
            fragmentWeightDataBinding.tvWeightUnit.setVisibility(View.VISIBLE);
            fragmentWeightDataBinding.tvWeightAvg.setVisibility(View.VISIBLE);
        } else {
            fragmentWeightDataBinding.tvWeightUnit.setVisibility(View.GONE);
            fragmentWeightDataBinding.tvWeightAvg.setVisibility(View.GONE);
        }

        String pressureValue = entry.getY() > 0f ? CalendarUtil.formatString("%.1f", formatWeightValue(entry.getY())) : "- -";
        mViewModel.timeIntervalWeightValueLiveData.postValue(pressureValue);
        mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getTimeInterval(leftTime, entry.getX(), getTimeType()));
    }

    @Override
    public void onNothingSelected() {

    }

    protected double formatWeightValue(double value) {
        double ret = converter.value(value);
        ret = BaseUnitConverter.getType() == TYPE_METRIC ? formatKg(ret) : formatPound(ret);
        return ret;
    }

    protected double formatKg(double kg) {
        int minKG = 10;
        int maxKG = 250;
        kg = Math.max(minKG, kg);
        kg = Math.min(kg, maxKG);
        return kg;
    }

    protected double formatPound(double pound) {
        int minPound = 22;
        int maxPound = 551;
        pound = Math.max(minPound, pound);
        pound = Math.min(pound, maxPound);
        return pound;
    }
}