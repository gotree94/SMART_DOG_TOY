package com.jieli.healthaide.ui.mine;

import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.step.StepBaseVo;
import com.jieli.healthaide.databinding.FragmentSportsWeeklyBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KMUnitConverter;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.health.step.charts.CustomBarChart;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/18/21 6:58 PM
 * @desc :
 */
public class SportsWeeklyFragment extends BaseFragment {
    private SportsWeeklyViewModel mViewModel;
    private FragmentSportsWeeklyBinding mBinding;


    //    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSportsWeeklyBinding.inflate(inflater, container, false);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.mine_sport_weekly);

        ImageView ivLast = mBinding.csvSportsWeekly.findViewById(R.id.ibt_calender_last);
        ivLast.setImageResource(R.drawable.ic_left_gray);
        ImageView ivNext = mBinding.csvSportsWeekly.findViewById(R.id.ibt_calender_next);
        ivNext.setImageResource(R.drawable.ic_right_arrow);
        TextView tvData = mBinding.csvSportsWeekly.findViewById(R.id.tv_time);
        tvData.setTextColor(ResourcesCompat.getColor(getResources(), R.color.text_important_color, requireActivity().getTheme()));
        mBinding.csvSportsWeekly.setType(CalenderSelectorView.TYPE_WEEK);
        mBinding.csvSportsWeekly.setListener((type, leftTime, rightTime) -> {
            JL_Log.d(tag, "csvSportsWeekly", "leftTime == " + CalendarUtil.serverDateFormat().format(leftTime) + "\trightTime = " + CalendarUtil.serverDateFormat().format(leftTime));
            mViewModel.refreshWeekData(leftTime, rightTime);
        });


        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SportsWeeklyViewModel.class);
        UserInfoViewModel userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);

        mBinding.csvSportsWeekly.setMaxTime(mViewModel.getEndTime());
        userInfoViewModel.userInfoLiveData.observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo == null) return;
            mViewModel.setUserInfo(userInfo);
            mBinding.csvSportsWeekly.setTime(mViewModel.getStartTime());
            mViewModel.refreshWeekData(mBinding.csvSportsWeekly.getLeftTime(), mBinding.csvSportsWeekly.getRightTime());

        });
        mViewModel.getLiveData().observe(getViewLifecycleOwner(), info -> {
            JL_Log.d(tag, "LiveData", "end time ----->" + info);

            Converter unitConverter = new KMUnitConverter().getConverter(BaseUnitConverter.getType());
            mBinding.layoutSportWeeklyData.tvDistance.setText(CalendarUtil.formatString("%.2f", unitConverter.value(info.totalDistance / 1000.0f)));
            mBinding.layoutSportWeeklyData.tvDistanceUnit.setText(unitConverter.unit());
            mBinding.layoutSportWeeklyData.tvStep.setText(CalendarUtil.formatString("%d", info.totalStep));
            mBinding.layoutSportWeeklyData.tvHot.setText(CalendarUtil.formatString("%d", info.totalKcal));
            mBinding.layoutSportWeeklyData.tvStepUp.setSelected(info.stepUp >= 0);
            mBinding.layoutSportWeeklyData.tvStepUp.setText(CalendarUtil.formatString("%d%s", info.stepUp, getString(R.string.step)));

            mBinding.layoutSportWeeklyData.tvDistanceUp.setSelected(info.distanceUp >= 0);
            mBinding.layoutSportWeeklyData.tvDistanceUp.setText(CalendarUtil.formatString("%.2f%s", unitConverter.value(info.distanceUp / 1000.0f), unitConverter.unit()));
            mBinding.layoutSportWeeklyData.tvHotUp.setSelected(info.kcalUp >= 0);
            mBinding.layoutSportWeeklyData.tvHotUp.setText(CalendarUtil.formatString("%d%s", info.kcalUp, getString(R.string.kcal)));


            mBinding.tvSportsWeeklyUpState.setSelected(info.targetUp >= 0);
            mBinding.tvSportsWeeklyUpState.setText(CalendarUtil.formatString("%d%s", info.targetUp, getString(R.string.day)));

            mBinding.tvSportsWeeklyTarget.setText(getString(R.string.reach_target_days, info.reachTarget));

            refreshLineChart(mBinding.lineChartSportWeekly, info.allWeekString, info.allWeekValue, info.target);
            refreshBarChart(mBinding.barChartSportWeekly, info.weekData, info.target);

        });
        userInfoViewModel.getUserInfo();
    }

    private void refreshLineChart(LineChart lineChart, String[] strings, int[] data, int targetStep) {
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);


        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.getLegend().setEnabled(false);

        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.getAxisLeft().setAxisMinimum(-200);
        float max = targetStep + 2000;
        for (int datum : data) {
            max = Math.max(max, datum);
        }
        max += max / 10;
        lineChart.getAxisLeft().setAxisMaximum(max);
        lineChart.getAxisLeft().disableGridDashedLine();
        lineChart.getAxisLeft().disableAxisLineDashedLine();
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawZeroLine(false);
        lineChart.getAxisLeft().setDrawGridLines(false);

        int padding = 14;
        lineChart.setExtraOffsets(padding, padding, padding, padding);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setTextSize(10);
        xAxis.setDrawAxisLine(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0.8f);//设置最大值
        xAxis.setAxisMaximum(4.2f);//设置最大值
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        {//画x轴边线
            int lineColor = ContextCompat.getColor(requireContext(), R.color.gray_AEAEAE);
            float lineWidth = 1f;
            xAxis.setAxisLineColor(lineColor);
            xAxis.setAxisLineWidth(lineWidth);
            xAxis.setAxisLineDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));//开启硬件加速时无效
        }
        xAxis.setLabelCount(4, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return strings[Math.round(value) - 1];
            }
        });
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            if (data[i - 1] > 0) {
                values.add(new Entry(i, data[i - 1]));
            }
        }

        LineDataSet lineDataSet = new LineDataSet(values, "line");
        lineDataSet.setDrawFilled(true);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleHoleRadius(5);
        lineDataSet.setColor(getResources().getColor(R.color.main_color));
        lineDataSet.setCircleColor(getResources().getColor(R.color.main_color));
        lineDataSet.setFillDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_sports_weekly_line_chart, requireActivity().getTheme()));

        LineData lineData = new LineData(lineDataSet);
        lineData.setValueTextSize(12);
        lineData.setDrawValues(true);
        lineData.setHighlightEnabled(false);
        lineData.setValueTextColor(getResources().getColor(R.color.text_secondary_disable_color));
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat df2 = new DecimalFormat("#,###");
                String valueString = df2.format(value);
                return valueString + getString(R.string.step);
            }
        });
        lineChart.clear();
        lineChart.setData(lineData);
    }


    //    @RequiresApi(api = Build.VERSION_CODES.M)
    private void refreshBarChart(CustomBarChart barChart, List<StepBaseVo.StepChartData> list, int targetStep) {


        float max = targetStep + 2000;
        for (StepBaseVo.StepChartData data : list) {
            max = Math.max(max, data.value);
        }

        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 1; i < 14; i += 2) {
            Fill[] fills = new Fill[]{new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_sports_weekly_chart_shape)))
                    , new Fill(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.bg_sports_weekly_chart_shape)))};
            int index = Math.max(i / 2, 0);
            float value = 0;
            if (index < list.size()) {
                value = list.get(index).value;
            }
            values.add(new BarEntry(i, value, fills));
        }


        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDrawGridBackground(false);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setTextSize(10);
        xAxis.setDrawAxisLine(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(14);


        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value % 2 == 0) return "";
                String[] weeks = requireContext().getResources().getStringArray(R.array.alarm_weeks);
                return weeks[(int) value / 2];
            }
        });


        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary_disable_color));
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        {//画x轴边线
            int lineColor = ContextCompat.getColor(requireContext(), R.color.gray_AEAEAE);
            float lineWidth = 1f;
            xAxis.setAxisLineColor(lineColor);
            xAxis.setAxisLineWidth(lineWidth);
            xAxis.setAxisLineDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));//开启硬件加速时无效
        }
        YAxis leftAxis;

        {   // // Y-Axis Style // //
            leftAxis = barChart.getAxisLeft();
            leftAxis.setDrawLabels(false);
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setSpaceBottom(0f);
            leftAxis.setSpaceTop(0f);

            leftAxis.setAxisMinimum(0);
            leftAxis.setAxisMaximum(max);
        }
        barChart.getAxisRight().setEnabled(false);

        int lineColor = ContextCompat.getColor(requireContext(), R.color.gray_AEAEAE);
        int textColor = ContextCompat.getColor(requireContext(), R.color.gray_AEAEAE);
        float lineWidth = 1f;
        float textSize = 10f;

        LimitLine limitL = new LimitLine(targetStep, null);
        limitL.setTextColor(textColor);
        limitL.setLineWidth(lineWidth);
        limitL.setEnabled(true);
        limitL.setLineColor(lineColor);
        limitL.enableDashedLine(10f, 5f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
        limitL.setTextSize(textSize);
        limitL.setLabel(getString(R.string.target) + targetStep + getString(R.string.step));
        limitL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置

        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(limitL);


        xAxis.setLabelCount(values.size() * 2);
        BarDataSet barDataSet = new BarDataSet(values, "line");
        barDataSet.setColor(getResources().getColor(R.color.main_color));
        BarData barData = new BarData(barDataSet);

        barData.setDrawValues(false);
        barData.setBarWidth(0.7f);
        barData.setHighlightEnabled(false);
        barChart.clear();
        barChart.setData(barData);
    }
}
