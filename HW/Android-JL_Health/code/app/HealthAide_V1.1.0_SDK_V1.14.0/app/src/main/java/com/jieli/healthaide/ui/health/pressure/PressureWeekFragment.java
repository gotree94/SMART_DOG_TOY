package com.jieli.healthaide.ui.health.pressure;


import android.graphics.Color;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.pressure.PressureBaseVo;
import com.jieli.healthaide.data.vo.pressure.PressureWeekVo;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChart;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChartRenderer;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class PressureWeekFragment extends PressureDataFragment {
    private final float DefaultYAxisMax = 100;
    private float yAxisMax = DefaultYAxisMax;
    private float yAxisMin = 0;
    WeightLineChart weightLineChart;

    public static PressureWeekFragment newInstance() {
        return new PressureWeekFragment();
    }

    @Override
    protected PressureBaseVo createVo() {
        return new PressureWeekVo();
    }

    @Override
    protected Chart getChartsView() {
        weightLineChart = new WeightLineChart(requireContext());
        initChart(weightLineChart);
        return weightLineChart;
    }

    @Override
    protected ChartData getChartData() {
        ArrayList<Entry> values = new ArrayList<>();
        List<PressureBaseVo.PressureChartData> sourceDataArray = vo.getEntities();
        for (PressureBaseVo.PressureChartData chartData : sourceDataArray) {
            //todo 这里的处理是因为space足够小，没有出现一个space里面有两条数据，否则会出现问题
            Entry entry;
            if (chartData.value != 0) {
                entry = new Entry(chartData.index, chartData.value);
            } else {
                entry = new Entry(chartData.index, WeightLineChartRenderer.SKIP_Y);
            }
            values.add(entry);
        }
        LineDataSet set1;
        set1 = new LineDataSet(values, "Pressure week");
        set1.setLineWidth(1f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(4f);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setCircleHoleColor(getContext().getResources().getColor(R.color.yellow_E98E5F));
        set1.setHighLightColor(getContext().getResources().getColor(R.color.yellow_FFC15D));
        set1.setDrawValues(false);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        return data;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_WEEK;
    }

    protected void initChart(WeightLineChart chart) {
        {   // // Chart Style // //
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);// scaling can now only be done on x- and y-axis separately
            chart.setDoubleTapToZoomEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setScaleEnabled(false);//关掉放大
            chart.setAutoScaleMinMaxEnabled(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(7.5f);//设置最大值
            xAxis.setDrawLabels(true);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);//去掉边线
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
            leftAxis.setAxisMaximum(yAxisMax);
            leftAxis.setAxisMinimum(yAxisMin);

            rightAxis = chart.getAxisRight();
            rightAxis.setDrawLabels(true);
            rightAxis.setDrawGridLines(false);//去掉网格线
            rightAxis.setDrawAxisLine(false);//去掉边线
            rightAxis.setSpaceBottom(0f);
            rightAxis.setSpaceTop(0f);
            rightAxis.setAxisMaximum(yAxisMax);
            rightAxis.setAxisMinimum(yAxisMin);
            rightAxis.setTextSize(10f);
            rightAxis.setTextColor(0xb3ffffff);
            rightAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value == yAxisMin) {
                        return "0";
                    }
                    return String.valueOf((int) Math.ceil(value / 5) * 5);
                }
            });
            rightAxis.setLabelCount(6, true);
        }
        int lineColor = Color.parseColor("#2AFFFFFF");
        int textColor = Color.parseColor("#2AFFFFFF");
        float lineWidth = 1f;
        float textSize = 10f;
        Integer[] limitArray = new Integer[]{0, 20, 40, 60, 80, 100};
        for (Integer limit : limitArray) {
            LimitLine limitL = new LimitLine(limit, null);
            limitL.setTextColor(textColor);
            limitL.setLineWidth(lineWidth);
            limitL.setEnabled(true);
            limitL.setLineColor(lineColor);
            limitL.enableDashedLine(10f, 5f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
            limitL.setTextSize(textSize);
            limitL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
            chart.getAxisLeft().addLimitLine(limitL);
        }

        Legend l = chart.getLegend();
        l.setEnabled(false);
        l.setForm(Legend.LegendForm.LINE);
    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {
        if (entry != null) {
            String pressureValue = entry.getY() > 0f ? String.valueOf((int) entry.getY()) : "- -";
            mViewModel.timeIntervalPressureValueLiveData.postValue(pressureValue);
            mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getTimeInterval(leftTime, entry.getX(), getTimeType()));
        }
    }
}