package com.jieli.healthaide.ui.health.pressure;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.pressure.PressureBaseVo;
import com.jieli.healthaide.data.vo.pressure.PressureDayVo;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.health.pressure.charts.CustomBarChart;
import com.jieli.healthaide.ui.health.pressure.charts.CustomBarChartRenderer;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 压力-天界面
 */
public class PressureDayFragment extends PressureDataFragment<PressureDayVo> {
    private final float DefaultYAxisMax = 100;
    private float yAxisMax = DefaultYAxisMax;
    private float yAxisMin = 0;
    private CustomBarChart barChart;
    private float Y_DEFAULT_EMPTY = 0f;

    public static PressureDayFragment newInstance() {
        return new PressureDayFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentPressureDataBinding.tvPressureAvgDescribe.setText("当日压力均值");
        return fragmentPressureDataBinding.getRoot();
    }

    @Override
    protected PressureDayVo createVo() {
        return new PressureDayVo();
    }

    @Override
    protected Chart getChartsView() {
        barChart = new CustomBarChart(requireContext());
        initChart(barChart);
        barChart.setRenderCallback(new CustomBarChartRenderer.RenderCallback() {
                                       @Override
                                       public void onCurrentPointerPositionValueX(float xValue) {
                                           //只用于同步时间
                                           mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getMoment(xValue, CalenderSelectorView.TYPE_DAY));
                                       }

                                       @Override
                                       public void onHighLightX(Entry entry) {
                                           if (entry != null) {
                                               String pressureStatus = analysisStatus((int) entry.getY());
                                               mViewModel.timeIntervalPressureStatusLiveData.postValue(pressureStatus);
                                               String pressureValue = entry.getY() > 0f ? String.valueOf((int) entry.getY()) : "- -";
                                               mViewModel.timeIntervalPressureValueLiveData.postValue(pressureValue);
                                           } else {
                                               mViewModel.timeIntervalPressureValueLiveData.postValue("- -");
                                           }
                                       }
                                   }
        );
        return barChart;
    }

    @Override
    protected ChartData getChartData() {
        Object fillData = new Fill[]{new Fill(getContext().getDrawable(R.drawable.bg_pressure_chart_shape_sel))
                , new Fill(getContext().getDrawable(R.drawable.bg_pressure_chart_shape_nol))};
        ArrayList<BarEntry> values = new ArrayList<>();
        List<PressureBaseVo.PressureChartData> sourceDataArray = vo.getEntities();
        for (PressureBaseVo.PressureChartData chartData : sourceDataArray) {
            //todo 这里的处理是因为space足够小，没有出现一个space里面有两条数据，否则会出现问题
            BarEntry barEntry;
            if (chartData.value != 0) {
                barEntry = new BarEntry(chartData.index, chartData.value, fillData);
            } else {
                barEntry = new BarEntry(chartData.index, Y_DEFAULT_EMPTY, fillData);
            }
            values.add(barEntry);
        }
        BarDataSet set1;
        set1 = new BarDataSet(values, "step week");
        set1.setDrawIcons(false);
        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setBarWidth(10f);
        return data;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_DAY;
    }

    private void initChart(CustomBarChart chart) {
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
            xAxis.setAxisMinimum(-10.5f);//设置x轴的最小值
            xAxis.setAxisMaximum(1445.5f);//设置最大值
            xAxis.setDrawGridLines(false);
            xAxis.setAvoidFirstLastClipping(true);//避免lable被裁剪一部分
            xAxis.setTextSize(10f);
            xAxis.setTextColor(0xb3ffffff);
            xAxis.setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat mFormat = CustomTimeFormatUtil.dateFormat("HH:mm");

                @Override
                public String getFormattedValue(float value) {
                    long millis = TimeUnit.HOURS.toMillis((long) (value + 10) / 60 - 8);
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
        Integer[] limitArray = new Integer[]{20, 40, 60, 80, 100};
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
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }
}
