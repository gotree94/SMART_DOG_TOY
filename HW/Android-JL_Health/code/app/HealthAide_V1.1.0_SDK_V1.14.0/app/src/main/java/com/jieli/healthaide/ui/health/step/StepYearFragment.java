package com.jieli.healthaide.ui.health.step;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.step.StepBaseVo;
import com.jieli.healthaide.data.vo.step.StepYearVo;
import com.jieli.healthaide.ui.health.step.charts.CustomBarChart;
import com.jieli.healthaide.ui.health.step.entity.AnalysisEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class StepYearFragment extends StepDayFragment {

    public static StepYearFragment newInstance() {
        return new StepYearFragment();
    }

    @Override
    protected StepBaseVo createVo() {
        return new StepYearVo();
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_YEAR;
    }

    @Override
    protected List<AnalysisEntity> getAnalysisData() {
        ArrayList<AnalysisEntity> data = new ArrayList<>();
        int totalStep = vo.getTotalStep();
        int avgStep = vo.getAvgStep();

        AnalysisEntity entity = new AnalysisEntity();
        entity.setFirstAnalysisValue(String.valueOf(totalStep));
        entity.setFirstAnalysisUnit(getString(R.string.step));
        entity.setFirstAnalysisDescribe(getString(R.string.all_step));
        entity.setSecondAnalysisValue(String.valueOf(avgStep));
        entity.setSecondAnalysisUnit(getString(R.string.step));
        entity.setSecondAnalysisDescribe(getString(R.string.average_step));
        data.add(entity);
        return data;
    }

    @Override
    protected void initChart(CustomBarChart chart) {
        super.initChart(chart);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.5f);//设置最大值
            xAxis.setAxisMaximum(12.5f);//设置最大值
            xAxis.setAvoidFirstLastClipping(false);//避免lable被裁剪一部分
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