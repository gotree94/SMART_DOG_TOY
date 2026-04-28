package com.jieli.healthaide.ui.health.heartrate;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateBaseVo;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateMonthVo;
import com.jieli.healthaide.ui.health.heartrate.charts.CombinedChart;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

public class HeartRateMonthFragment extends HeartRateWeekFragment {

    public static HeartRateMonthFragment newInstance() {
        return new HeartRateMonthFragment();
    }


    @Override
    protected HeartRateBaseVo createVo() {
        return new HeartRateMonthVo();
    }

    @Override
    protected void updateChartSetting(int dataLen) {
        super.updateChartSetting(dataLen);
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setAxisMaximum(dataLen + 0.5f);//设置最大值
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_MONTH;
    }

    protected void initChart(CombinedChart chart) {
        super.initChart(chart);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(30.5f);//设置最大值 先预设30
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    value = value - 0.01f;
                    if (value < xAxis.getAxisMinimum()) {
                        value = xAxis.getAxisMinimum();
                    }
                    return CustomTimeFormatUtil.getMonthDayByLocale((int) Math.round(value));
                }
            });
            xAxis.setLabelCount(14, true);
        }
    }

}