package com.jieli.healthaide.ui.health.weight;


import android.graphics.Color;

import androidx.core.content.ContextCompat;

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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;
import com.jieli.healthaide.data.vo.weight.WeightWeekVo;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChart;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChartRenderer;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisMultipleBaseEntity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisWeekEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;

public class WeightWeekFragment extends WeightDataFragment {
    protected final float Y_AXIS_MAX_HIGH_LIMIT = 250;
    protected final float Y_AXIS_MAX_LOWER_LIMIT = 160;
    protected float yAxisMax = Y_AXIS_MAX_LOWER_LIMIT;
    protected float yAxisMin = 5;

    public static WeightWeekFragment newInstance() {
        return new WeightWeekFragment();
    }

    @Override
    protected WeightBaseVo createVo() {
        return new WeightWeekVo();
    }

    @Override
    protected Chart getChartsView() {
        WeightLineChart weightLineChart = new WeightLineChart(requireContext());
        initChart(weightLineChart);
        return weightLineChart;
    }

    @Override
    protected void updateChartSetting(ChartData chartData) {
        super.updateChartSetting(chartData);
        if (chartData == null) return;
        WeightLineChart weightLineChart = (WeightLineChart) chart;
        YAxis axisLeft = weightLineChart.getAxisLeft();
        YAxis axisRight = weightLineChart.getAxisRight();
        yAxisMax = (int) (vo.maxVal / 40) * 40 + 40;
        yAxisMax = Math.max(yAxisMax, Y_AXIS_MAX_LOWER_LIMIT);
        yAxisMax = Math.min(yAxisMax, Y_AXIS_MAX_HIGH_LIMIT);
        axisLeft.setAxisMaximum(yAxisMax);
        axisLeft.setAxisMinimum(yAxisMin);
        axisRight.setAxisMaximum(yAxisMax);
        axisRight.setAxisMinimum(yAxisMin);
        updateLimitArray((WeightLineChart) chart);
    }

    @Override
    protected ChartData getChartData() {
        List<WeightBaseVo.WeightBarCharData> sourceDataArray = vo.getEntities();
        ArrayList<Entry> values = new ArrayList<>();
        for (WeightBaseVo.WeightBarCharData chartData : sourceDataArray) {
            //todo 这里的处理是因为space足够小，没有出现一个space里面有两条数据，否则会出现问题
            Entry entry;
            if (chartData.value != 0) {
                entry = new Entry(chartData.index, (float) chartData.value);
            } else {
                entry = new Entry(chartData.index, WeightLineChartRenderer.SKIP_Y);
            }
            values.add(entry);
        }
        LineDataSet set1;
        set1 = new LineDataSet(values, "The year 2017");
        set1.setLineWidth(1f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(4f);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setCircleHoleColor(Color.parseColor("#4852CA"));
        set1.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.yellow_E98E5F));
        set1.setDrawValues(false);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        return new LineData(dataSets);
    }

    @Override
    protected List<AnalysisMultipleBaseEntity> getAnalysisData() {
        ArrayList<AnalysisMultipleBaseEntity> data = new ArrayList<>();
        AnalysisWeekEntity entity1 = new AnalysisWeekEntity();
        AnalysisWeekEntity entity2 = new AnalysisWeekEntity();
        AnalysisWeekEntity entity3 = new AnalysisWeekEntity();
        entity1.setAnalysisDescribe(getString(R.string.weight));
        entity1.setAnalysisValue(vo.minVal != 0 || vo.maxVal != 0 ? CalendarUtil.formatString("%.1f-%.1f", formatWeightValue(vo.minVal), formatWeightValue(vo.maxVal)) : "- -");
        entity1.setAnalysisUnit(vo.averageVal != 0 ? converter.unit() : "");
        entity2.setAnalysisDescribe(getString(R.string.weekly_average));
        entity2.setAnalysisValue(vo.averageVal != 0 ? CalendarUtil.formatString("%.1f", formatWeightValue(vo.averageVal)) : "- -");
        entity2.setAnalysisUnit(vo.averageVal != 0 ? converter.unit() : "");
        entity3.setAnalysisDescribe(getString(R.string.range_of_variation));
        String rangeSign = vo.changeRange > 0 ? "+" : "";
        entity3.setAnalysisValue(vo.averageVal != 0 ? CalendarUtil.formatString("%s%.1f", rangeSign, converter.value(vo.changeRange)) : "- -");
        entity3.setAnalysisUnit(vo.averageVal != 0 ? converter.unit() : "");
        data.add(entity1);
        data.add(entity2);
        data.add(entity3);
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
            rightAxis.setLabelCount(5, true);
            rightAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return CalendarUtil.formatString("%.0f", formatWeightValue(value));
                }
            });
        }
        updateLimitArray(chart);
        Legend l = chart.getLegend();
        l.setEnabled(false);
        l.setForm(Legend.LegendForm.LINE);
    }

    private void updateLimitArray(WeightLineChart chart) {
        List<Integer> limitArray = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                limitArray.add((int) yAxisMin);
            } else if (i == 4) {
                limitArray.add((int) yAxisMax);
            } else {
                int limitVal = (int) ((yAxisMax - yAxisMin) / 4 * i + yAxisMin);
                limitArray.add(limitVal);
            }
        }
        int lineColor = Color.parseColor("#2AFFFFFF");
        int textColor = Color.parseColor("#2AFFFFFF");
        float lineWidth = 1f;
        float textSize = 10f;
        chart.getAxisLeft().removeAllLimitLines();
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
    }
}