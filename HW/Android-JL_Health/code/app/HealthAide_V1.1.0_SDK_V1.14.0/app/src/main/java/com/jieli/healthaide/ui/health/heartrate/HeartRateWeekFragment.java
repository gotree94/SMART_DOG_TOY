package com.jieli.healthaide.ui.health.heartrate;

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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateBaseVo;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateWeekVo;
import com.jieli.healthaide.ui.health.blood_oxygen.chart.CandleStickChartRenderer;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.health.heartrate.charts.CombinedChart;
import com.jieli.healthaide.ui.health.heartrate.charts.WeightLineChartRenderer;
import com.jieli.healthaide.ui.health.heartrate.entity.HeartDescribeEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeartRateWeekFragment extends HeartRateDataFragment {
    protected final float DefaultYAxisMax = 225;
    protected float yAxisMax = DefaultYAxisMax;
    protected float yAxisMin = 15;
    protected CombinedChart combinedChart;

    public static HeartRateWeekFragment newInstance() {
        return new HeartRateWeekFragment();
    }

    @Override
    protected HeartRateBaseVo createVo() {
        return new HeartRateWeekVo();
    }

    @Override
    protected Chart getChartsView() {
        combinedChart = new CombinedChart(requireContext());
        initChart(combinedChart);
        return combinedChart;
    }

    @Override
    protected ChartData getChartData() {
        return getCombinedDataByViewType(viewType);
    }

    @Override
    protected List<HeartDescribeEntity> getAnalysisData() {
        List<HeartDescribeEntity> tempList = super.getAnalysisData();
        if (tempList != null && tempList.size() >= 2) {
            tempList.get(1).setEntityTypeStringSrc(R.string.resting_heart_rate_avg);
        }
        return tempList;
    }

    @Override
    protected void updateHighLight(ChartData chartData) {
        if (isResumed() && !isFirstRefreshData) return;
        isFirstRefreshData = false;
        if (chartData != null && chart != null) {
            Highlight highlight1 = new Highlight(vo.highLightIndex, Float.NaN, 0);
            highlight1.setDataIndex(1);//DataIndex 要设置为1，否则不行
            chart.highlightValue(highlight1, true);
        }
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_WEEK;
    }

    protected void initChart(CombinedChart chart) {
        {   // // Chart Style // //
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);// scaling can now only be done on x- and y-axis separately
            chart.setDoubleTapToZoomEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setScaleEnabled(false);//关掉放大
            chart.setAutoScaleMinMaxEnabled(false);
            chart.setHighlightFullBarEnabled(false);
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
                DecimalFormat mFormat = new DecimalFormat("###,###,###,##0.0");

                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            rightAxis.setLabelCount(5, false);
        }
        int lineColor = Color.parseColor("#2AFFFFFF");
        int textColor = Color.parseColor("#2AFFFFFF");
        float lineWidth = 1f;
        float textSize = 10f;
        Integer[] limitArray = new Integer[]{40, 80, 120, 160, 200};
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
    protected void changeViewType(int viewType) {
        CombinedData combinedData = getCombinedDataByViewType(viewType);
        JL_Log.e(TAG, "changeViewType", "data : " + combinedData);
        combinedChart.setData(combinedData);
        combinedChart.invalidate();
    }

    protected CombinedData getCombinedDataByViewType(int viewType) {
        List<HeartRateBaseVo.HeartRateCharData> sourceDataArray = vo.getEntities();
        Fill[] fills = new Fill[]{new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_blood_oxygen_chart_shape_week_sel))),
                new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_blood_oxygen_chart_shape_week_nol)))};
        ArrayList<CandleEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();
        switch (viewType) {
            case VIEW_TYPE_HEART_RATE_RANGE:
                for (HeartRateBaseVo.HeartRateCharData heartRateEntity : sourceDataArray) {
                    CandleEntry candleEntry;
                    if (heartRateEntity.max > 0) {
                        candleEntry = new CandleEntry(heartRateEntity.index, heartRateEntity.max, heartRateEntity.min, heartRateEntity.max, heartRateEntity.min, fills);
                    } else {
                        candleEntry = new CandleEntry(heartRateEntity.index, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, fills);
                    }
                    barEntries.add(candleEntry);
                }
                //空的
                lineEntries = new ArrayList<>();
                for (float i = 1; i <= sourceDataArray.size(); i++) {
                    lineEntries.add(new Entry(i, WeightLineChartRenderer.SKIP_Y, null));
                }
                break;
            case VIEW_TYPE_RESETING_HEART_RATE:
                //空的
                barEntries = new ArrayList<>();
                for (float i = 1; i <= sourceDataArray.size(); i++) {
                    barEntries.add(new CandleEntry(i, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, CandleStickChartRenderer.Y_DEFAULT_EMPTY, fills));
                }
                lineEntries = new ArrayList<>();
                for (HeartRateBaseVo.HeartRateCharData heartRateEntity : sourceDataArray) {
                    Entry restingEntry;
                    if (heartRateEntity.restingRate > 0) {
                        restingEntry = new Entry(heartRateEntity.index, heartRateEntity.restingRate, fills);
                    } else {
                        restingEntry = new Entry(heartRateEntity.index, WeightLineChartRenderer.SKIP_Y, fills);
                    }
                    lineEntries.add(restingEntry);
                }
                break;
        }
        CandleDataSet candleDataSet = new CandleDataSet(barEntries, "The year 2017");
        candleDataSet.setDrawIcons(false);
        candleDataSet.setDrawValues(false);
        candleDataSet.setBarSpace(0.36f);
        CandleData candleData = new CandleData();
        candleData.addDataSet(candleDataSet);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Line DataSet");
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setCircleHoleRadius(4f);
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.red_D25454));
        lineDataSet.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.yellow_F2C45A));
        lineDataSet.setDrawValues(false);
        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);

        CombinedData data = new CombinedData();
        data.setData(candleData);
        data.setData(lineData);
        return data;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //combineChart ，上面选中喝下面选中的东西不一样
        if (null == e) return;
        JL_Log.d(TAG, "onValueSelected", "" + e);
        if (viewType != VIEW_TYPE_RESETING_HEART_RATE) {//正常心率视图
            if (!(e instanceof CandleEntry)) {
                //CombineChart的特殊之处 ，上面选中喝下面选中的东西不一样,这里强行把选中LineEntry转换成对应的CandleEntry
                int index;
                LineData lineData = (LineData) ((CombinedData) chart.getData()).getDataByIndex(0);
                ILineDataSet iLineDataSet = lineData.getDataSetByIndex(0);
                index = iLineDataSet.getEntryIndex(e);
                CandleData candleData = (CandleData) ((CombinedData) chart.getData()).getDataByIndex(1);
                ICandleDataSet iCandleDataSet = candleData.getDataSetByIndex(0);
                e = iCandleDataSet.getEntryForIndex(index);
            }
            CandleEntry candleEntry = (CandleEntry) e;
            String value = candleEntry.getOpen() != CandleStickChartRenderer.Y_DEFAULT_EMPTY ? CalendarUtil.formatString("%d-%d", (int) candleEntry.getClose(), (int) candleEntry.getOpen()) : "- -";
            mViewModel.timeIntervalHeartRateValueLiveData.postValue(value);
        } else {//静息心率视图
            if ((e instanceof CandleEntry)) {
                //CombineChart的特殊之处 ，上面选中喝下面选中的东西不一样,这里强行把选中LineEntry转换成对应的CandleEntry
                int index;
                CandleData candleData = (CandleData) ((CombinedData) chart.getData()).getDataByIndex(1);
                ICandleDataSet iCandleDataSet = candleData.getDataSetByIndex(0);
                index = iCandleDataSet.getEntryIndex((CandleEntry) e);
                LineData lineData = (LineData) ((CombinedData) chart.getData()).getDataByIndex(0);
                ILineDataSet iLineDataSet = lineData.getDataSetByIndex(0);
                e = iLineDataSet.getEntryForIndex(index);
            }
            String value = e.getY() > 0f ? String.valueOf((int) e.getY()) : "- -";
            mViewModel.timeIntervalHeartRateValueLiveData.postValue(value);
        }
        mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getTimeInterval(leftTime, e.getX(), getTimeType()));
    }
}