package com.jieli.healthaide.ui.health.blood_oxygen;


import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.blood_oxygen.BloodOxygenBaseVo;
import com.jieli.healthaide.data.vo.blood_oxygen.BloodOxygenWeekVo;
import com.jieli.healthaide.ui.health.blood_oxygen.chart.CandleStickChart;
import com.jieli.healthaide.ui.health.blood_oxygen.chart.CandleStickChartRenderer;
import com.jieli.healthaide.ui.health.blood_oxygen.entity.AnalysisEntity;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BloodOxygenWeekFragment extends BloodOxygenDataFragment {
    protected CandleStickChart candleStickChart;
    protected float yAxisMax = 100;
    protected float yAxisMin = 85;
    protected Integer[] limitArray = new Integer[]{(int) yAxisMin, 90, 95, (int) yAxisMax};

    public static BloodOxygenWeekFragment newInstance() {
        return new BloodOxygenWeekFragment();
    }

    @Override
    protected BloodOxygenBaseVo createVo() {
        return new BloodOxygenWeekVo();
    }

    @Override
    protected Chart getChartsView() {
        candleStickChart = new CandleStickChart(requireContext());
        initChart(candleStickChart);
        return candleStickChart;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_WEEK;
    }

    @Override
    protected List<AnalysisEntity> getAnalysisData() {
        ArrayList<AnalysisEntity> data = new ArrayList<>();
        AnalysisEntity analysisEntity = new AnalysisEntity();
        analysisEntity.setFirstAnalysisDescribe(getString(R.string.min_value));
        analysisEntity.setFirstAnalysisValue(vo.max != 0 ? CalendarUtil.formatString("%d%%", (int) vo.min) : EMPTY);
        analysisEntity.setSecondAnalysisDescribe(getString(R.string.max_value));
        analysisEntity.setSecondAnalysisValue(vo.max != 0 ? CalendarUtil.formatString("%d%%", (int) vo.max) : EMPTY);
        analysisEntity.setItemType(1);
        data.add(analysisEntity);
        return data;
    }

    @Override
    protected ChartData getChartData() {
        Fill[] fills = new Fill[]{new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_blood_oxygen_chart_shape_week_sel))),
                new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_blood_oxygen_chart_shape_week_nol)))};
        ArrayList<CandleEntry> values = new ArrayList<>();
        List<BloodOxygenBaseVo.BloodOxygenBarCharData> sourceDataArray = vo.getEntities();
        for (BloodOxygenBaseVo.BloodOxygenBarCharData barCharData : sourceDataArray) {
            CandleEntry candleEntry;
            if (barCharData.max > 0) {
                candleEntry = new CandleEntry(barCharData.index, barCharData.max, barCharData.min, barCharData.max, barCharData.min, fills);
            } else {
                candleEntry = new CandleEntry(barCharData.index, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, fills);
            }
            values.add(candleEntry);
        }
        CandleDataSet set1;
        set1 = new CandleDataSet(values, "The year 2017");
        set1.setDrawIcons(false);
        set1.setDrawValues(false);
        set1.setBarSpace(0.36f);
        return new CandleData(set1);
    }

    protected void initChart(CandleStickChart chart) {
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
            xAxis.setAxisMaximum(7 + 0.5f);//设置最大值
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
            rightAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value == yAxisMin) return "";
                    return (int) Math.ceil(value / 5) * 5 + "%";
                }
            });
            rightAxis.setLabelCount(limitArray.length, true);
        }
        int lineColor = Color.parseColor("#2AFFFFFF");
        int textColor = Color.parseColor("#2AFFFFFF");
        float lineWidth = 1f;
        float textSize = 10f;
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
    public void onValueSelected(Entry e, Highlight h) {
        CandleEntry candleEntry = (CandleEntry) e;
        String bloodOxygenValue = candleEntry.getOpen() != CandleStickChartRenderer.Y_DEFAULT_EMPTY ? CalendarUtil.formatString("%d%%-%d%%", (int) candleEntry.getClose(), (int) candleEntry.getOpen()) : "- -";
        mViewModel.timeIntervalBloodOxygenLiveData.postValue(bloodOxygenValue);
        mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getTimeInterval(leftTime, e.getX(), getTimeType()));
    }
}