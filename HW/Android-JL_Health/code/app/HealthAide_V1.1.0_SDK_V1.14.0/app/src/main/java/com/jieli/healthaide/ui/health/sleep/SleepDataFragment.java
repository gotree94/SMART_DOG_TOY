package com.jieli.healthaide.ui.health.sleep;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.OrientationHelper;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.sleep.SleepBaseVo;
import com.jieli.healthaide.data.vo.sleep.SleepDayVo;
import com.jieli.healthaide.databinding.FragmentSleepDataBinding;
import com.jieli.healthaide.ui.dialog.CalendarDialog;
import com.jieli.healthaide.ui.health.BaseHealthDataFragment;
import com.jieli.healthaide.ui.health.sleep.adapter.AnalysisAdapter;
import com.jieli.healthaide.ui.health.sleep.adapter.NapAdapter;
import com.jieli.healthaide.ui.health.sleep.adapter.StatisticsAdapter;
import com.jieli.healthaide.ui.health.sleep.entity.AnalysisEntity;
import com.jieli.healthaide.ui.health.sleep.entity.NapEntity;
import com.jieli.healthaide.ui.health.sleep.entity.StatisticsEntity;
import com.jieli.healthaide.ui.health.sleep.viewmodel.SleepBaseViewModel;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.healthaide.util.ValueUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 10:41 AM
 * @desc :
 */
public abstract class SleepDataFragment<T extends SleepBaseVo> extends BaseHealthDataFragment {
    protected String tag = getClass().getSimpleName();

    private FragmentSleepDataBinding fragmentSleepDataBinding;
    protected int[] colors = new int[]{
            Color.parseColor("#ff60dce5"),
            Color.parseColor("#ff7badf6"),
            Color.parseColor("#ffdb79A4"),
            Color.parseColor("#ffe6be64")
    };

    private StatisticsAdapter statisticsAdapter;
    private AnalysisAdapter analysisAdapter;
    private NapAdapter napAdapter;
    protected Chart chart;
    protected long leftTime;


    protected SleepBaseViewModel<T> mViewModel;
    protected T vo;

    private boolean isFirst = true;


    protected abstract ChartData getChartData();

    protected void updateHighLight(ChartData chartData) {
    }

    protected void updateChartSetting(float max) {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentSleepDataBinding = FragmentSleepDataBinding.inflate(inflater, container, false);
        return fragmentSleepDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        initTimeView();
        initTimeSelectView();
        initStatisticView();
        initAnalysisView();
        initChartsView();
        iniNapsView();
        mViewModel.getLiveData().observe(getViewLifecycleOwner(), t -> {
            vo = t;
            if (isFirst) {
                isFirst = false;
                return;
            }
            refreshData();
        });
    }

    @Override
    protected void onCalendarDialogChangeDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        fragmentSleepDataBinding.layoutCalenderSelector.updateTime(calendar.getTimeInMillis());
    }

    @Override
    protected Calendar getCurrentCalendar() {
        long leftTime = fragmentSleepDataBinding.layoutCalenderSelector.getLeftTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(leftTime);
        return calendar;
    }

    protected abstract Chart getChartsView();

    private List<AnalysisEntity> getAnalysisData() {
        List<AnalysisEntity> data = new ArrayList<>();
        if (vo == null) {
            return data;
        }
        List<Integer> analysisPercent = ValueUtil.analysisPercent((int) vo.deepSleepTime, (int) vo.lightSleepTime, (int) vo.remSleepTime, (int) vo.awakeTime);
        boolean average = !(vo instanceof SleepDayVo);
        int[] types = average ? new int[]{
                R.string.sleep_analysis_avg_type1, R.string.sleep_analysis_avg_type2, R.string.sleep_analysis_avg_type3, R.string.sleep_analysis_avg_type4, R.string.sleep_analysis_avg_type5, R.string.sleep_analysis_avg_type6
        } : new int[]{
                R.string.sleep_analysis_type1, R.string.sleep_analysis_type2, R.string.sleep_analysis_type3, R.string.sleep_analysis_type4, R.string.sleep_analysis_type5, R.string.sleep_analysis_type6
        };
        {//夜间正常睡眠
            AnalysisEntity entity = new AnalysisEntity();
            int darkSleepDuration = (int) (vo.darkSleepTime / 60000);
            int hour = darkSleepDuration / 60;
            int min = darkSleepDuration % 60;
            entity.title = getString(types[0], String.valueOf(hour), String.valueOf(min));
            entity.reference = getString(R.string.references) + "6-10 " + getString(R.string.hour);
            handlerAnalysisLevel(360, 600, darkSleepDuration, entity);
            data.add(entity);
        }
        {//深睡比例
            AnalysisEntity entity = new AnalysisEntity();
            entity.title = getString(types[1], String.valueOf(analysisPercent.get(0)));
            entity.reference = getString(R.string.references) + "20-60%";
            handlerAnalysisLevel(20, 60, analysisPercent.get(0), entity);
            data.add(entity);
        }
        {//浅睡比例
            AnalysisEntity entity = new AnalysisEntity();
            entity.title = getString(types[2], String.valueOf(analysisPercent.get(1)));
            entity.reference = getString(R.string.references) + "<55%";
            handlerAnalysisLevel(0, 54, analysisPercent.get(1), entity);
            data.add(entity);
        }
        {//快速眼动比例
            AnalysisEntity entity = new AnalysisEntity();
            entity.title = getString(types[3], String.valueOf(analysisPercent.get(2)));
            entity.reference = getString(R.string.references) + "10-30%";
            handlerAnalysisLevel(10, 30, analysisPercent.get(2), entity);
            data.add(entity);
        }
        {//深睡连续性
            AnalysisEntity entity = new AnalysisEntity();
            entity.title = getString(types[4], String.valueOf(vo.analysis.analysisDeepSleepGrade));
            entity.reference = getString(R.string.references) + "80-100%";
            handlerAnalysisLevel(80, 100, vo.analysis.analysisDeepSleepGrade, entity);
            data.add(entity);
        }
        {//清醒次数
            AnalysisEntity entity = new AnalysisEntity();
            entity.title = getString(types[5], String.valueOf(vo.awakeNum));
            entity.reference = getString(R.string.references) + "0-2" + getString(R.string.count_format);
            handlerAnalysisLevel(0, 2, vo.awakeNum, entity);
            data.add(entity);
        }
        return data;
    }

    private void handlerAnalysisLevel(int min, int max, int current, AnalysisEntity entity) {
        String ret;
        int color;
        if (current < min) {
            ret = getString(R.string.status_low);
            color = R.color.yellow_FFC15D;
        } else if (current > max) {
            ret = getString(R.string.status_high);
            color = R.color.red_E16F7D;
        } else {
            ret = getString(R.string.status_normal);
            color = R.color.green_7EC97D;
        }
        entity.level = ret;
        entity.levelColor = color;
    }

    protected List<StatisticsEntity> getStatisticsData() {
        List<StatisticsEntity> data = new ArrayList<>();
        if (vo == null) {
            return data;
        }
        boolean average = !(vo instanceof SleepDayVo);
        List<Integer> analysisPercent = ValueUtil.analysisPercent((int) vo.deepSleepTime, (int) vo.lightSleepTime, (int) vo.remSleepTime, (int) vo.awakeTime);
        long[] durations = new long[]{
                vo.deepSleepTime, vo.lightSleepTime, vo.remSleepTime
        };
        String[] types = average ? new String[]{
                getString(R.string.deep_sleep_avg), getString(R.string.light_sleep_avg), getString(R.string.rapid_eye_movement_avg), getString(R.string.sleep_score_avg)
        } : new String[]{
                getString(R.string.deep_sleep_full), getString(R.string.light_sleep_full), getString(R.string.rapid_eye_movement_full), getString(R.string.sleep_score)
        };

        for (int i = 0; i < 3; i++) {
            StatisticsEntity entity = new StatisticsEntity();
            entity.hour = (int) (durations[i] / (3600 * 1000));
            entity.min = (int) (durations[i] / (60 * 1000)) % 60;
            entity.type = types[i];
            entity.result = getString(R.string.sleep_ratio) + analysisPercent.get(i) + "%";
            data.add(entity);
        }

        String[] appraise = new String[]{
                "", getString(R.string.status_low), getString(R.string.status_normal), getString(R.string.status_high)
        };
        int[] color = new int[]{
                R.color.green_7EC97D, R.color.yellow_FFC15D, R.color.green_7EC97D, R.color.red_E16F7D
        };
        int entity4Status;
        if (vo.analysis.analysisSleepGrade == 0) {
            entity4Status = 0;
        } else if (vo.analysis.analysisSleepGrade < 80) {
            entity4Status = 1;
        } else if (vo.analysis.analysisSleepGrade <= 100) {
            entity4Status = 2;
        } else {
            entity4Status = 3;
        }
        StatisticsEntity entity4 = new StatisticsEntity();
        entity4.type = types[3];
        entity4.result = appraise[entity4Status];
        entity4.score = vo.analysis.analysisSleepGrade;
        entity4.itemType = StatisticsEntity.TYPE_SCORE;
        entity4.typeColor = color[entity4Status];
        data.add(entity4);
        return data;
    }

    private List<NapEntity> getNapListData() {
        List<NapEntity> resultList = new ArrayList<>();
        String type = getTimeType() == CalenderSelectorView.TYPE_DAY ? getString(R.string.sleep_nap) : getString(R.string.sleep_avg_nap);
        List<SleepBaseVo.Nap> napList = vo.getNapList();
        if (napList != null && getTimeType() != CalenderSelectorView.TYPE_YEAR) {
            for (SleepBaseVo.Nap nap : napList) {
                NapEntity napEntity = new NapEntity();
                final SimpleDateFormat mFormatDay = new SimpleDateFormat("HH:mm");
                String start = mFormatDay.format(new Date(nap.startTimeStamp));
                String end = mFormatDay.format(new Date(nap.endTimeStamp));
                if (getTimeType() == CalenderSelectorView.TYPE_DAY) {
                    napEntity.timeSlot = start + "-" + end;
                }
                napEntity.type = type;
                napEntity.drawableSrc = R.drawable.sleep_nap;
                napEntity.duration = CustomTimeFormatUtil.getFormatTime(nap.endTimeStamp - nap.startTimeStamp, getContext());
                resultList.add(napEntity);
            }
            boolean hasDarkSleep = vo.darkSleepTime != 0;
            if (getTimeType() == CalenderSelectorView.TYPE_DAY && hasDarkSleep) {
                NapEntity napEntity = new NapEntity();
                napEntity.type = getString(R.string.sleep_total);
                napEntity.duration = CustomTimeFormatUtil.getFormatTime((long) (vo.darkSleepTime + vo.napSleepTime), getContext());
                napEntity.drawableSrc = R.drawable.sleep_nap_total;
                resultList.add(napEntity);
            }
        }
        fragmentSleepDataBinding.rvSleepDataNap.setVisibility(resultList.isEmpty() ? View.GONE : View.VISIBLE);
        return resultList;
    }

    protected void refreshData() {
        chart.clear();
        ChartData chartData = getChartData();
        chart.setData(chartData);
        analysisAdapter.setList(getAnalysisData());
        statisticsAdapter.setList(getStatisticsData());
        napAdapter.setList(getNapListData());
        updateHighLight(chartData);
        updateChartSetting(vo.max);
        boolean hasDarkSleep = vo.darkSleepTime != 0;
        fragmentSleepDataBinding.rvSleepStatistics.setVisibility(hasDarkSleep ? View.VISIBLE : View.GONE);
        if (getTimeType() != CalendarDialog.CALENDAR_VIEW_TYPE_YEAR) {
            fragmentSleepDataBinding.rvSleepDataAnalysis.setVisibility(hasDarkSleep ? View.VISIBLE : View.GONE);
        }
    }

    protected void updateHighLightDateView(String date) {
        fragmentSleepDataBinding.tvDate.setText(date);
    }

    protected void updateHighLightTimeView(int time) {
        int hour = time / 60;
        int min = time % 60;
        String hourStr = String.valueOf(hour);
        String minStr = String.valueOf(min);
        if (time == 0) {
            hourStr = "-";
            minStr = "-";
        }
        fragmentSleepDataBinding.tvSleepTimeHour.setText(hourStr);
        fragmentSleepDataBinding.tvSleepTimeMin.setText(minStr);
    }

    private String getUid() {
        return HealthApplication.getAppViewModel().getUid();
    }

    private void initTimeSelectView() {
        fragmentSleepDataBinding.layoutCalenderSelector.setListener((type, leftTime, rightTime) -> {
            this.leftTime = leftTime;
            mViewModel.refresh(getUid(), leftTime, rightTime);
        });
        fragmentSleepDataBinding.layoutCalenderSelector.setType(getTimeType());
    }

    private void initStatisticView() {
        statisticsAdapter = new StatisticsAdapter();
        fragmentSleepDataBinding.rvSleepStatistics.setAdapter(statisticsAdapter);
    }

    private void initAnalysisView() {
        analysisAdapter = new AnalysisAdapter();
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        fragmentSleepDataBinding.rvSleepDataAnalysis.setAdapter(analysisAdapter);
        fragmentSleepDataBinding.rvSleepDataAnalysis.addItemDecoration(decoration);
    }

    private void initChartsView() {
        FrameLayout frameLayout = fragmentSleepDataBinding.flChartsParent;
        frameLayout.removeAllViews();
        chart = getChartsView();
        ((BarLineChartBase) chart).setScaleEnabled(false);
        frameLayout.addView(chart);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void iniNapsView() {
        napAdapter = new NapAdapter();
        fragmentSleepDataBinding.rvSleepDataNap.setAdapter(napAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), OrientationHelper.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        fragmentSleepDataBinding.rvSleepDataNap.addItemDecoration(decoration);
    }
}