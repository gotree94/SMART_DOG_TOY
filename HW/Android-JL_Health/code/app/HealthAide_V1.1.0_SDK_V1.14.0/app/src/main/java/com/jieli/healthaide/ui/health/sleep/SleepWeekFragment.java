package com.jieli.healthaide.ui.health.sleep;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.sleep.SleepBaseVo;
import com.jieli.healthaide.ui.health.sleep.charts.week.CustomBarChart;
import com.jieli.healthaide.ui.health.sleep.viewmodel.SleepBaseViewModel;
import com.jieli.healthaide.ui.health.sleep.viewmodel.SleepWeekViewModel;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 10:41 AM
 * @desc :
 */
public class SleepWeekFragment extends SleepDataFragment implements OnChartValueSelectedListener {
    protected final float DefaultYAxisMax = 111;
    protected float yAxisMax = DefaultYAxisMax;
    protected float yAxisMin = 0;
    protected CustomBarChart barChart;
    protected float Y_DEFAULT_EMPTY = 0f;

    protected boolean isFirstRefreshData = true;

    public static SleepWeekFragment newInstance() {
        return new SleepWeekFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mViewModel = (SleepBaseViewModel) new ViewModelProvider(this).get(getViewModelClass());
        super.onActivityCreated(savedInstanceState);
    }

    protected Class getViewModelClass() {
        return SleepWeekViewModel.class;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_WEEK;
    }

    @Override
    protected Chart getChartsView() {
        barChart = new CustomBarChart(requireContext());
        initChart(barChart);
        return barChart;
    }

    protected void initChart(CustomBarChart chart) {
        {   // // Chart Style // //
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);// scaling can now only be done on x- and y-axis separately
            chart.setDoubleTapToZoomEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setScaleEnabled(false);//关掉放大
            chart.setAutoScaleMinMaxEnabled(false);
            chart.setOnChartValueSelectedListener(this);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(7.5f);//设置最大值
            xAxis.setDrawLabels(true);
            xAxis.setDrawGridLines(false);
//            xAxis.setAvoidFirstLastClipping(true);//避免lable被裁剪一部分
            xAxis.setTextSize(10f);
            xAxis.setTextColor(0xb3ffffff);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    String[] weeks = requireContext().getResources().getStringArray(R.array.alarm_weeks);
                    return weeks[(int) value - 1];
                }
            });
            xAxis.setLabelCount(7, false);
            {//画x轴边线
                int lineColor = Color.parseColor("#2AFFFFFF");
                float lineWidth = 1f;
                xAxis.setAxisLineColor(lineColor);
                xAxis.setAxisLineWidth(lineWidth);
                xAxis.setAxisLineDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));//开启硬件加速时无效
            }
        }

        YAxis leftAxis;
        YAxis rightAxis;
        {   // // Y-Axis Style // //
            leftAxis = chart.getAxisLeft();
            leftAxis.setDrawLabels(false);
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setSpaceBottom(0f);
            leftAxis.setSpaceTop(0f);
            // axis range
//            leftAxis.setAxisMaximum(yAxisMax);
//            leftAxis.setAxisMinimum(yAxisMin);

            rightAxis = chart.getAxisRight();
            rightAxis.setDrawLabels(false);
            rightAxis.setDrawGridLines(false);//去掉网格线
            rightAxis.setDrawAxisLine(false);//去掉边线
            rightAxis.setSpaceBottom(0f);
            rightAxis.setSpaceTop(0f);
            rightAxis.setAxisMaximum(yAxisMax);
            rightAxis.setAxisMinimum(yAxisMin);
            rightAxis.setTextSize(10f);
            rightAxis.setTextColor(0xb3ffffff);
        }
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }

    @Override
    protected ChartData getChartData() {
        ArrayList<BarEntry> values = new ArrayList<>();
        List<SleepBaseVo.SleepBarCharData> sourceDataArray = vo.getEntities();
        for (SleepBaseVo.SleepBarCharData chartData : sourceDataArray) {
            BarEntry barEntry;
            Log.d("ZHM", "getChartData: index :" + chartData.index);
            if (chartData.data.toString() != null) {
                barEntry = new BarEntry(chartData.index + 1, chartData.data);
            } else {
                barEntry = new BarEntry(chartData.index + 1, Y_DEFAULT_EMPTY);
            }
            values.add(barEntry);
        }
        BarDataSet set1;
        set1 = new BarDataSet(values, "step week");
        set1.setDrawIcons(false);
        set1.setDrawValues(false);
        set1.setColors(colors);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setBarWidth(0.25f);
        return data;
    }

    protected void updateHighLight(ChartData chartData) {
        if (isResumed() && !isFirstRefreshData) return;
        isFirstRefreshData = false;
        if (null != chartData) {
            chart.highlightValue(vo.highLightIndex, 0);//高亮值
        }
    }

    protected void updateChartSetting(float max) {
        if (max == 0) {
            max = DefaultYAxisMax;
        }
        {// 更新 Y-Axis的范围 :最大值最小值
            YAxis leftAxis;
            YAxis rightAxis;
            leftAxis = barChart.getAxisLeft();
            rightAxis = barChart.getAxisRight();
            leftAxis.setAxisMaximum((float) (1.06 * max));
            rightAxis.setAxisMaximum((float) (1.06 * max));
        }
        {//更新LimitLine 等分线
            int lineColor = Color.parseColor("#2AFFFFFF");
            float lineWidth = 1f;
            barChart.getAxisLeft().removeAllLimitLines();
            LimitLine limitL = new LimitLine(max * 0.95f, null);
            limitL.setLineWidth(lineWidth);
            limitL.setEnabled(true);
            limitL.setLineColor(lineColor);
            limitL.enableDashedLine(10f, 5f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
            limitL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
            barChart.getAxisLeft().addLimitLine(limitL);
        }
    }

    @Override
    public void onValueSelected(Entry entry, Highlight h) {
        {//当前选中的时间段对应数据 ms
            int y = (int) entry.getY();
            updateHighLightTimeView(y / 60000);
        }
        updateHighLightDateView(CustomTimeFormatUtil.getTimeInterval(leftTime, entry.getX(), getTimeType()));
    }

    @Override
    public void onNothingSelected() {

    }

}