package com.jieli.healthaide.ui.health.blood_oxygen;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.data.vo.blood_oxygen.BloodOxygenMonthVo;
import com.jieli.healthaide.ui.health.blood_oxygen.chart.CandleStickChart;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

/**
 * 血氧-月界面
 */
public class BloodOxygenMonthFragment extends BloodOxygenWeekFragment {

    public static BloodOxygenMonthFragment newInstance() {
        return new BloodOxygenMonthFragment();
    }

    @Override
    protected BloodOxygenMonthVo createVo() {
        return new BloodOxygenMonthVo();
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_MONTH;
    }

    @Override
    protected ChartData getChartData() {
        CandleData data = (CandleData) super.getChartData();
        CandleDataSet set= (CandleDataSet) data.getDataSetByIndex(0);
        set.setBarSpace(0.25f);
        return data;
    }

    protected void updateChartSetting(int dataLen) {
        XAxis xAxis = candleStickChart.getXAxis();
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
        xAxis.setAxisMaximum(dataLen + 0.5f);//设置最大值
        candleStickChart.invalidate();
    }

    protected void initChart(CandleStickChart chart) {
        super.initChart(chart);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(31 + 0.5f);//设置最大值
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