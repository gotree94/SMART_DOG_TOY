package com.jieli.healthaide.ui.health.weight;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;
import com.jieli.healthaide.data.vo.weight.WeightYearVo;
import com.jieli.healthaide.ui.health.weight.charts.WeightLineChart;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisMultipleBaseEntity;
import com.jieli.healthaide.ui.health.weight.entity.AnalysisWeekEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.List;

public class WeightYearFragment extends WeightWeekFragment {

    public static WeightYearFragment newInstance() {
        return new WeightYearFragment();
    }

    @Override
    protected WeightBaseVo createVo() {
        return new WeightYearVo();
    }

    @Override
    protected List<AnalysisMultipleBaseEntity> getAnalysisData() {
        List<AnalysisMultipleBaseEntity> data = super.getAnalysisData();
        AnalysisWeekEntity entity = (AnalysisWeekEntity) data.get(1);
        entity.setAnalysisDescribe(getString(R.string.year_average));
        return data;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_YEAR;
    }

    @Override
    protected void initChart(WeightLineChart chart) {
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