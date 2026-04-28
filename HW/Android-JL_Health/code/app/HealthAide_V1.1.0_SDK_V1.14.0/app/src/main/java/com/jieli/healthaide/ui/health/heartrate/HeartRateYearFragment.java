package com.jieli.healthaide.ui.health.heartrate;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateBaseVo;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateYearVo;
import com.jieli.healthaide.ui.health.heartrate.charts.CombinedChart;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

public class HeartRateYearFragment extends HeartRateWeekFragment {
    public static HeartRateYearFragment newInstance() {
        return new HeartRateYearFragment();
    }
    @Override
    protected HeartRateBaseVo createVo() {
        return new HeartRateYearVo();
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_YEAR;
    }

    protected void initChart(CombinedChart chart) {
        super.initChart(chart);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(12.5f);//设置最大值
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return CustomTimeFormatUtil.getYearMonthByLocale((int) value);
                }
            });
            xAxis.setLabelCount(12, false);
        }
    }
}