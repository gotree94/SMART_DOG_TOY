package com.jieli.healthaide.ui.health.weight;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;
import com.jieli.healthaide.data.vo.weight.WeightMonthVo;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChart;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisMultipleBaseEntity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisWeekEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.List;

/**
 * 步数-月界面
 */
public class WeightMonthFragment extends WeightWeekFragment {

    public static WeightMonthFragment newInstance() {
        return new WeightMonthFragment();
    }

    @Override
    protected WeightBaseVo createVo() {
        return new WeightMonthVo();
    }

    @Override
    protected void updateChartSetting(ChartData chartData) {
        super.updateChartSetting(chartData);
        if (chartData == null) return;
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMaximum(chartData.getEntryCount() + 0.5f);//设置最大值
    }

    @Override
    protected List<AnalysisMultipleBaseEntity> getAnalysisData() {
        List<AnalysisMultipleBaseEntity> data = super.getAnalysisData();
        AnalysisWeekEntity entity = (AnalysisWeekEntity) data.get(1);
        entity.setAnalysisDescribe(getString(R.string.month_average));
        return data;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_MONTH;
    }

    @Override
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