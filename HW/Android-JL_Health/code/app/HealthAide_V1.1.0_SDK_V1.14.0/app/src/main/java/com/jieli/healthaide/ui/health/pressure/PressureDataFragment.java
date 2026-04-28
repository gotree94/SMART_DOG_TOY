package com.jieli.healthaide.ui.health.pressure;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.pressure.PressureBaseVo;
import com.jieli.healthaide.databinding.FragmentPressureDataBinding;
import com.jieli.healthaide.ui.health.BaseHealthDataFragment;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public abstract class PressureDataFragment<T extends PressureBaseVo> extends BaseHealthDataFragment implements OnChartValueSelectedListener {
    protected String TAG = getClass().getSimpleName();
    protected FragmentPressureDataBinding fragmentPressureDataBinding;
    protected PressureViewModel mViewModel;
    protected long leftTime;
    protected T vo;
    private Chart chart;
    private Chart analysisChart;

    private boolean isFirstRefreshData = true;

    protected abstract T createVo();

    protected abstract Chart getChartsView();

    protected abstract ChartData getChartData();

    protected void updateChartSetting(int dataLen) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentPressureDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_pressure_data, container, false);
        fragmentPressureDataBinding.setLifecycleOwner(this);
        return fragmentPressureDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PressureViewModel.Factory factory = new PressureViewModel.Factory(createVo());
        mViewModel = new ViewModelProvider(this, factory).get(PressureViewModel.class);
        fragmentPressureDataBinding.setPressureViewModel(mViewModel);
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
        fragmentPressureDataBinding.layoutCalenderSelector.updateTime(calendar.getTimeInMillis());
    }

    @Override
    protected Calendar getCurrentCalendar() {
        long leftTime = fragmentPressureDataBinding.layoutCalenderSelector.getLeftTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(leftTime);
        return calendar;
    }

    private String getUid() {
        return HealthApplication.getAppViewModel().getUid();
    }

    private void initChartsView() {
        FrameLayout frameLayout = fragmentPressureDataBinding.flChartsParent;
        frameLayout.removeAllViews();
        chart = getChartsView();
        frameLayout.addView(chart);
        if (getTimeType() == CalenderSelectorView.TYPE_YEAR) {
            fragmentPressureDataBinding.tvWeightAvg.setText(getString(R.string.month_average));
        }
        if (getTimeType() == CalenderSelectorView.TYPE_DAY) {
            fragmentPressureDataBinding.tvWeightAvg.setText("");
        }
        chart.setOnChartValueSelectedListener(this);
    }

    private void initTimeSelectView() {
        fragmentPressureDataBinding.layoutCalenderSelector.setListener((type, leftTime, rightTime) -> {
            this.leftTime = leftTime;
            mViewModel.refresh(getUid(), leftTime, rightTime);
        });
        fragmentPressureDataBinding.layoutCalenderSelector.setType(getTimeType());
    }

    private void initAnalysisView() {
        //todo 分析统计结果
        analysisChart = new PieChart(requireContext());
        initAnalysisChart((PieChart) analysisChart);
        fragmentPressureDataBinding.flChartsPressureProportion.addView(analysisChart);
    }

    private void initAnalysisChart(PieChart pieChart) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(0, 0, 0, 0);
        pieChart.setCenterTextSize(10);
        pieChart.setCenterTextColor(getContext().getResources().getColor(R.color.text_secondary_color));
        pieChart.setCenterText("压力\n占比");
        pieChart.setRotationEnabled(false);//不可旋转
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);

        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(40f);

        pieChart.setDrawCenterText(true);

        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setHighlightPerTapEnabled(true);

        Legend l = pieChart.getLegend();
        l.setEnabled(false);
    }

    private void refreshData() {
        updateAnalysisView(vo.analysisDataArray);
        ChartData chartData = getChartData();
        if (chart == null) return;
        chart.clear();
        chart.setData(chartData);
        updateHighLight(chartData);
        if (chartData!=null){
            updateChartSetting(chartData.getEntryCount());
        }
    }

    private void updateHighLight(ChartData chartData) {
        if (isResumed() && !isFirstRefreshData) return;
        isFirstRefreshData = false;
        if (null != chartData ) {
            chart.highlightValue(vo.highLightIndex, 0);
        }
    }

    private void updateAnalysisView(List<Integer> data) {
        if (null == data || data.isEmpty()) return;
        String averageValue = String.valueOf(Math.round(vo.pressureAvg));
        String averageStatus = analysisStatus((int) (vo.pressureAvg));
        String valueRange;
        if (vo.max <= 0) {
            valueRange = "--";
        } else {
            valueRange = (int) vo.min + "-" + (int) vo.max;
        }
        String analysisDescribe = analysisDescribe(vo.getEntities());
        mViewModel.averagePressureValueLiveData.postValue(averageValue);
        mViewModel.averagePressureStatusLiveData.postValue(averageStatus);
        mViewModel.pressureValueRangeLiveData.postValue(valueRange);
        mViewModel.pressureAnalysisDescribeLiveData.postValue(analysisDescribe);

        fragmentPressureDataBinding.tvPressureColorTagHighValue.setText(data.get(3) + "%");
        fragmentPressureDataBinding.tvPressureColorTagMediumValue.setText(data.get(2) + "%");
        fragmentPressureDataBinding.tvPressureColorTagNormalValue.setText(data.get(1) + "%");
        fragmentPressureDataBinding.tvPressureColorTagRelaxValue.setText(data.get(0) + "%");
        analysisChart.setData(getAnalysisChartData(data));
        analysisChart.invalidate();
    }

    private ChartData getAnalysisChartData(List<Integer> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        int allData = 0;
        for (int i = 0; i < data.size(); i++) {
            entries.add(new PieEntry(data.get(i)));
            allData += data.get(i);
        }
        int emptyPercent = allData == 0 ? 100 : 0;
        entries.add(new PieEntry(emptyPercent));
        PieDataSet dataSet = new PieDataSet(entries, "Election Results");
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(0f);
        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#96C5DA"));
        colors.add(Color.parseColor("#7BD083"));
        colors.add(Color.parseColor("#F3C6A5"));
        colors.add(Color.parseColor("#D77777"));
        colors.add(Color.parseColor("#ECECEC"));
        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);
        PieData pieData = new PieData(dataSet);
        return pieData;
    }

    private String analysisDescribe(List entities) {
        return "适当的压力可促使人高效工作与生活。请继续保\n" +
                "持对压力的有效调节。";
    }

    protected String analysisStatus(int pressureValue) {
        String status;
        if (1 <= pressureValue && pressureValue < 30) {
            status = getContext().getString(R.string.relax);
        } else if (30 <= pressureValue && pressureValue < 60) {
            status = getContext().getString(R.string.normal);
        } else if (60 <= pressureValue && pressureValue < 80) {
            status = getContext().getString(R.string.medium);
        } else if (80 <= pressureValue && pressureValue <= 100) {
            status = getContext().getString(R.string.uptilted);
        } else {
            status = "";
        }
        return status;
    }

    @Override
    public void onNothingSelected() {

    }

}