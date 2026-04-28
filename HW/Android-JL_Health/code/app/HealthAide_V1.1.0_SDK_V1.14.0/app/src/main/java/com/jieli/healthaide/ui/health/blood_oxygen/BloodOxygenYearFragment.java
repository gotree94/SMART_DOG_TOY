package com.jieli.healthaide.ui.health.blood_oxygen;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.data.vo.blood_oxygen.BloodOxygenYearVo;
import com.jieli.healthaide.ui.health.blood_oxygen.chart.CandleStickChart;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

public class BloodOxygenYearFragment extends BloodOxygenWeekFragment {

    public static BloodOxygenYearFragment newInstance() {
        return new BloodOxygenYearFragment();
    }

    @Override
    protected BloodOxygenYearVo createVo() {
        return new BloodOxygenYearVo();
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_YEAR;
    }

    protected void initChart(CandleStickChart chart) {
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