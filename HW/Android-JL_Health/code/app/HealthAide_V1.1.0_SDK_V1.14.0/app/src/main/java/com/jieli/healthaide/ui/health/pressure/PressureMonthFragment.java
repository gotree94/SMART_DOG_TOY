package com.jieli.healthaide.ui.health.pressure;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.data.vo.pressure.PressureBaseVo;
import com.jieli.healthaide.data.vo.pressure.PressureMonthVo;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChart;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

/**
 * 步数-月界面
 */
public class PressureMonthFragment extends PressureWeekFragment {
    public static PressureMonthFragment newInstance() {
        return new PressureMonthFragment();
    }

    @Override
    protected PressureBaseVo createVo() {
        return new PressureMonthVo();
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_MONTH;
    }

    @Override
    protected void updateChartSetting(int dataLen) {
        XAxis xAxis = weightLineChart.getXAxis();
        xAxis.setAxisMaximum(dataLen + 0.5f);//设置最大值
    }

    protected void initChart(WeightLineChart chart) {
        super.initChart(chart);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(31.5f);//设置最大值
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