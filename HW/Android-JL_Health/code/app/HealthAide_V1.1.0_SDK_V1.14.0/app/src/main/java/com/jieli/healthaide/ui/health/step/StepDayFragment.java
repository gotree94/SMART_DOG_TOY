package com.jieli.healthaide.ui.health.step;

import android.graphics.Color;
import android.graphics.DashPathEffect;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.step.StepBaseVo;
import com.jieli.healthaide.data.vo.step.StepDayVo;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KMUnitConverter;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.health.step.charts.CustomBarChart;
import com.jieli.healthaide.ui.health.step.entity.AnalysisEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 步数-天界面
 */
public class StepDayFragment extends StepDataFragment {
    protected final float DefaultYAxisMax = 111;
    protected float yAxisMax = DefaultYAxisMax;
    protected float yAxisMin = 0;
    protected CustomBarChart barChart;

    public static StepDayFragment newInstance() {
        return new StepDayFragment();
    }

    @Override
    protected StepBaseVo createVo() {
        return new StepDayVo();
    }

    @Override
    protected Chart getChartsView() {
        barChart = new CustomBarChart(requireContext());
        initChart(barChart);
        return barChart;
    }

    @Override
    protected ChartData getChartData() {
        Object fillData = new Fill[]{new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_step_chart_shape_sel)))
                , new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_step_chart_shape_nol)))};
        ArrayList<BarEntry> values = new ArrayList<>();
        List<StepBaseVo.StepChartData> sourceDataArray = vo.getEntities();
        for (StepBaseVo.StepChartData chartData : sourceDataArray) {
            BarEntry barEntry;
            if (chartData.value != 0) {
                barEntry = new BarEntry(chartData.index, chartData.value, fillData);
            } else {
                barEntry = new BarEntry(chartData.index, Y_DEFAULT_EMPTY, fillData);
            }
            values.add(barEntry);
        }
        BarDataSet set1;
        set1 = new BarDataSet(values, "step");
        set1.setDrawIcons(false);
        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setBarWidth(0.7f);
        return data;
    }

    @Override
    protected List<AnalysisEntity> getAnalysisData() {
        ArrayList<AnalysisEntity> data = new ArrayList<>();
        float totalMile = vo.getTotalDistance();
        int totalConsumption = vo.getTotalKcal();
        totalMile = totalMile / 1000;
        Converter kmUnitConverter = new KMUnitConverter().getConverter(BaseUnitConverter.getType());
        totalMile = (float) kmUnitConverter.value(totalMile);
        AnalysisEntity entity = new AnalysisEntity();
        entity.setFirstAnalysisValue(CalendarUtil.formatString("%.2f", totalMile));
        entity.setFirstAnalysisUnit(BaseUnitConverter.getType() == BaseUnitConverter.TYPE_METRIC ?getString(R.string.kilometre):getString(R.string.unit_mile));
        entity.setFirstAnalysisDescribe(getString(R.string.all_mile));
        entity.setSecondAnalysisValue(String.valueOf(totalConsumption));
        entity.setSecondAnalysisUnit(getString(R.string.kilocalorie));
        entity.setSecondAnalysisDescribe(getString(R.string.all_consumption));
        data.add(entity);
        return data;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_DAY;
    }

    @Override
    protected void updateChartSetting(int dataLen, int max) {
        YAxis leftAxis;
        YAxis rightAxis;
        {// 更新 Y-Axis的范围 :最大值最小值
            yAxisMax = max == 0 ? yAxisMax : max;
            leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMaximum(yAxisMax);
            leftAxis.setAxisMinimum(yAxisMin);
            rightAxis = barChart.getAxisRight();
            rightAxis.setAxisMaximum(yAxisMax);
            rightAxis.setAxisMinimum(yAxisMin);
            rightAxis.setDrawLabels(!(yAxisMax == DefaultYAxisMax));
        }
        if (yAxisMax == DefaultYAxisMax) return;
        {//更新LimitLine 等分线
            Integer[] limitArray = new Integer[]{(int) yAxisMax};
            int lineColor = Color.parseColor("#2AFFFFFF");
            float lineWidth = 1f;
            barChart.getAxisLeft().removeAllLimitLines();
            for (Integer limit : limitArray) {
                LimitLine limitL = new LimitLine(limit, null);
                limitL.setLineWidth(lineWidth);
                limitL.setEnabled(true);
                limitL.setLineColor(lineColor);
                limitL.enableDashedLine(10f, 5f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
                limitL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
                barChart.getAxisLeft().addLimitLine(limitL);
            }
        }
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
            chart.setViewPortOffsets(ValueUtil.dp2px(requireContext(), 16)
                    , ValueUtil.dp2px(requireContext(), 13)
                    , ValueUtil.dp2px(requireContext(), 45)
                    , ValueUtil.dp2px(requireContext(), 25));//此处是为固定给label文字留空间，否则字的多少导致边空间改变
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0.5f);//设置x轴的最小值
            xAxis.setAxisMaximum(24.5f);//设置最大值
            xAxis.setDrawLabels(true);
            xAxis.setDrawGridLines(false);
            xAxis.setAvoidFirstLastClipping(true);//避免lable被裁剪一部分
            xAxis.setTextSize(10f);
            xAxis.setTextColor(0xb3ffffff);
            xAxis.setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat mFormat = CustomTimeFormatUtil.dateFormat("HH:mm");

                @Override
                public String getFormattedValue(float value) {
                    long millis = TimeUnit.HOURS.toMillis((long) value - 8);
                    return mFormat.format(new Date(millis));
                }
            });
            xAxis.setLabelCount(5, true);
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
                    return String.valueOf((int) Math.ceil(value / 10) * 10);
                }
            });
            rightAxis.setLabelCount(2, true);
        }
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }

    protected void updateChart() {
    }
}