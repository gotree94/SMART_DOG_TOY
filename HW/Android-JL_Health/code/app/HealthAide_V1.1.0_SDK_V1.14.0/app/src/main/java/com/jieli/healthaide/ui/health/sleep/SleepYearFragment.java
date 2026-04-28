package com.jieli.healthaide.ui.health.sleep;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.ui.health.sleep.charts.week.CustomBarChart;
import com.jieli.healthaide.ui.health.sleep.viewmodel.SleepYearViewModel;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 10:41 AM
 * @desc :
 */
public class SleepYearFragment extends SleepWeekFragment {

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_YEAR;
    }

    @Override
    protected Class getViewModelClass() {
        return SleepYearViewModel.class;
    }

    public static SleepYearFragment newInstance() {
        return new SleepYearFragment();
    }

    @Override
    protected ChartData getChartData() {
        BarData data = (BarData) super.getChartData();
        data.setBarWidth(0.4f);
        return data;
    }

    protected void initChart(CustomBarChart chart) {
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