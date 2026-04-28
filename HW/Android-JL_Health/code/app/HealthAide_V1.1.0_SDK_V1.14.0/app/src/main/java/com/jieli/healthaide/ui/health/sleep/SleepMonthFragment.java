package com.jieli.healthaide.ui.health.sleep;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.ui.health.sleep.charts.week.CustomBarChart;
import com.jieli.healthaide.ui.health.sleep.viewmodel.SleepMonthViewModel;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 10:41 AM
 * @desc :
 */
public class SleepMonthFragment extends SleepWeekFragment {

    public static SleepMonthFragment newInstance() {
        return new SleepMonthFragment();
    }

    @Override
    protected Class getViewModelClass() {
        return SleepMonthViewModel.class;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_MONTH;
    }

    @Override
    protected ChartData getChartData() {
        BarData data = (BarData) super.getChartData();
        data.setBarWidth(0.7f);
        return data;
    }


    protected void updateChartSetting(float max) {
        super.updateChartSetting(max);
        {
            XAxis xAxis = barChart.getXAxis();
            xAxis.setAxisMaximum(vo.getEntities().size() + 0.5f);//设置最大值
        }
    }

    protected void initChart(CustomBarChart chart) {
       super.initChart(chart);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(31.5f);//设置最大值
            xAxis.setAvoidFirstLastClipping(true);//避免lable被裁剪一部分
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