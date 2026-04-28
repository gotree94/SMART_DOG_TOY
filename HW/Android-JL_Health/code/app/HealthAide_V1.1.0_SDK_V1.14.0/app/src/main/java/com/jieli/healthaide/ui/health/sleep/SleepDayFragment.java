package com.jieli.healthaide.ui.health.sleep;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.sleep.SleepDayVo;
import com.jieli.healthaide.ui.health.sleep.charts.day.SleepDayChart;
import com.jieli.healthaide.ui.health.sleep.charts.day.SleepDayData;
import com.jieli.healthaide.ui.health.sleep.charts.day.SleepDayDataSet;
import com.jieli.healthaide.ui.health.sleep.charts.day.SleepDayEntry;
import com.jieli.healthaide.ui.health.sleep.charts.formatter.DayValueFormatter;
import com.jieli.healthaide.ui.health.sleep.viewmodel.SleepDayViewModel;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 10:41 AM
 * @desc :
 */
public class SleepDayFragment extends SleepDataFragment<SleepDayVo> {
    public static SleepDayFragment newInstance() {
        return new SleepDayFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(SleepDayViewModel.class);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected Chart getChartsView() {

        SleepDayChart sleepDayChart = new SleepDayChart(requireContext());
        sleepDayChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sleepDayChart.setPinchZoom(false);
        sleepDayChart.setDoubleTapToZoomEnabled(false);

        float max = 6.7f;
        sleepDayChart.getAxisLeft().setAxisMaximum(max);
        sleepDayChart.getAxisLeft().setAxisMinimum(-0.2f);
        sleepDayChart.getAxisLeft().setDrawLabels(false);
        sleepDayChart.getAxisLeft().setDrawGridLines(false);
        sleepDayChart.getAxisLeft().setDrawAxisLine(false);
        sleepDayChart.getAxisLeft().setSpaceBottom(0f);
        sleepDayChart.getAxisLeft().setSpaceTop(0f);
//        sleepDayChart.getAxisLeft().setEnabled(false);
        {
            int lineColor = Color.parseColor("#2AFFFFFF");
            float lineWidth = 1f;
            LimitLine limitL = new LimitLine(6, null);
            limitL.setLineWidth(lineWidth);
            limitL.setEnabled(true);
            limitL.setLineColor(lineColor);
            limitL.enableDashedLine(10f, 5f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
            limitL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
            sleepDayChart.getAxisLeft().addLimitLine(limitL);
        }

        sleepDayChart.getAxisRight().setAxisMaximum(max);
        sleepDayChart.getAxisRight().setAxisMinimum(-0.8f);
        sleepDayChart.getAxisRight().setEnabled(false);

        sleepDayChart.getXAxis().setDrawGridLines(false);

        sleepDayChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        sleepDayChart.getXAxis().setAxisLineColor(Color.argb(25, 255, 255, 255));
        sleepDayChart.getXAxis().setAxisLineWidth(1);
        sleepDayChart.getXAxis().setAxisLineDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));
        sleepDayChart.getXAxis().setYOffset(10);

//        sleepDayChart.setHighlightPerTapEnabled(false);
        sleepDayChart.getXAxis().setValueFormatter(new DayValueFormatter());

        sleepDayChart.invalidate();
        sleepDayChart.setDescription(null);

        Legend l = sleepDayChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setEnabled(false);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);


        sleepDayChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                SleepDayEntry entry = (SleepDayEntry) e;
                JL_Log.e(tag, "onValueSelected", entry.toString());
            }

            @Override
            public void onNothingSelected() {

            }
        });

//        sleepDayChart.post(() -> {
//            final long downTime = SystemClock.uptimeMillis();
//            int[] pos = new int[2];
//            sleepDayChart.getLocationOnScreen(pos);
//            float x = pos[0] + 12;
//            float y = pos[1] + 10;
//            final MotionEvent downEvent = MotionEvent.obtain(
//                    downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0);
//            final MotionEvent upEvent = MotionEvent.obtain(
//                    downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
//            sleepDayChart.onTouchEvent(downEvent);
//            sleepDayChart.onTouchEvent(upEvent);
//            downEvent.recycle();
//            upEvent.recycle();
//        });
        return sleepDayChart;
    }

    @Override
    protected ChartData getChartData() {
        List<SleepDayEntry> list = new ArrayList<>();
        if (vo != null && vo.getEntities() != null && vo.getEntities().size() > 0) {
            List<ParseEntity> parseEntities = vo.getEntities();
            long minTime = parseEntities.get(0).getStartTime();
            long maxTime = parseEntities.get(vo.getEntities().size() - 1).getEndTime();
            //通过静态变量设置最大最小值做X轴变换，目的：为了修复long-->float精度损失
            SleepDayEntry.minTime = minTime;
            SleepDayEntry.maxTime = maxTime;
            for (ParseEntity entity : parseEntities) {
                list.add(new SleepDayEntry(entity.getStartTime(), entity.getEndTime(), (int) entity.getValue(), colors[(int) entity.getValue()]));
            }
            chart.getXAxis().setAxisMinimum(SleepDayEntry.toX(minTime));
            chart.getXAxis().setAxisMaximum(SleepDayEntry.toX(maxTime));
        } else {
            long startTime = CalendarUtil.removeTime(getCurrentCalendar().getTimeInMillis());
            long minTime = startTime;
            long maxTime = startTime + 8 * 60 * 60 * 1000;
            SleepDayEntry.minTime = minTime;
            SleepDayEntry.maxTime = maxTime;
            chart.getXAxis().setAxisMinimum(SleepDayEntry.toX(minTime));
            chart.getXAxis().setAxisMaximum(SleepDayEntry.toX(maxTime));
        }
        SleepDayDataSet dayDataSet = new SleepDayDataSet(list, "sleep");
        SleepDayData sleepDayData = new SleepDayData();
        sleepDayData.addDataSet(dayDataSet);

        updateHighLightTimeView(getAverageTime());//更新总时长
        return sleepDayData;
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_DAY;
    }

    private int getAverageTime() {
        long space = 0;//分钟
        if (vo != null) {
            List<ParseEntity> parseEntities = vo.getEntities();
            for (ParseEntity entity : parseEntities) {
                long tmp = entity.getEndTime() / 1000 / 60 - entity.getStartTime() / 1000 / 60;
                space += tmp;
            }
        }
        return (int) space;
    }

}