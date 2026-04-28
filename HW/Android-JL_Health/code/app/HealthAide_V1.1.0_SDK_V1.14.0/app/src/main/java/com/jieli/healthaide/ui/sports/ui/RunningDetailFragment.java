package com.jieli.healthaide.ui.sports.ui;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.databinding.FragmentRunningDetailBinding;
import com.jieli.healthaide.databinding.ItemSportsDetailSportStatusBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KMUnitConverter;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.mine.UserInfoViewModel;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.FormatUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :
 */
public class RunningDetailFragment extends BaseFragment {
    public static final String KEY_RECORD_START_TIME = "KEY_RECORD_START_TIME";
    FragmentRunningDetailBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRunningDetailBinding.bind(inflater.inflate(R.layout.fragment_running_detail, container, false));
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.sport_indoor_running);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        long startTime = requireArguments().getLong(KEY_RECORD_START_TIME);

        ThreadManager.getInstance().postRunnable(() -> {
            String uid = HealthApplication.getAppViewModel().getUid();
            final SportRecord sportRecord = HealthDataDbHelper.getInstance().getSportRecordDao().findByStartTime(uid, startTime);
            if (sportRecord == null){
                requireActivity().finish();
                return;
            }
            mBinding.getRoot().post(() -> {
                SportRecord decode = SportRecord.from(sportRecord.getData());
                initView(decode);
            });
        });
//        String uid = HealthApplication.getAppViewModel().getUid();
//        final SportRecord sportRecord = HealthDataDbHelper.getInstance().getSportRecordDao().findByStartTime(uid, startTime);
//        if (sportRecord == null) requireActivity().finish();
//        SportRecord decode = SportRecord.from(sportRecord.getData());
//        initView(decode);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UserInfoViewModel userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        userInfoViewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), userInfo -> mBinding.tvUserName.setText(userInfo.getNickname()));
        userInfoViewModel.getUserInfo();
    }


    private void initView(SportRecord sportRecord) {

        initHeaderView(sportRecord);
        initPaceView(sportRecord);


        Info stepFreqInfo = new Info();
        stepFreqInfo.title = R.string.step_freq_info_title;
        stepFreqInfo.icon = R.drawable.run_icon_step_sel;
        stepFreqInfo.maxTitle = R.string.max_step_freq;
        stepFreqInfo.averageTitle = R.string.average_step_freq;
        stepFreqInfo.lineColor = R.color.sports_detail_step_freq_line;
        stepFreqInfo.bgDrawable = R.drawable.bg_sports_detail_step_freq_line_chart;


        Info heartRateInfo = new Info();
        heartRateInfo.title = R.string.average_heart_rate_and_unit;
        heartRateInfo.icon = R.drawable.run_icon_heart_sel;
        heartRateInfo.maxTitle = R.string.max_heart_rate;
        heartRateInfo.averageTitle = R.string.average_heart_rate;
        heartRateInfo.lineColor = R.color.red_FFEB5B5B;
        heartRateInfo.bgDrawable = R.drawable.bg_sports_detail_heart_rate_line_chart;


        List<SportRecord.Info> list = sportRecord.getDataList();
        ArrayList<Entry> stepFreqs = new ArrayList<>();
        ArrayList<Entry> heartRates = new ArrayList<>();


        int index = 0, labelCount;
        int maxStepFreq = 0, averageStepFreq, totalStepFreq = 0;
        int maxHeartRate = 0, averageHeartRate, totalHeartRate = 0;


        int internal = sportRecord.getInternal();
//        for (int i = 0; i < 50; i++)
        for (SportRecord.Info info : list) {
            maxStepFreq = Math.max(maxStepFreq, info.stepFreq);
            totalStepFreq += info.stepFreq;

            stepFreqs.add(new Entry(index / 60.0f, info.stepFreq));

            maxHeartRate = Math.max(maxHeartRate, info.heart);
            totalHeartRate += info.heart;
            heartRates.add(new Entry(index / 60.0f, info.heart));

            index += internal;
        }

        averageStepFreq = list.size() > 0 ? totalStepFreq / list.size() : 0;
        averageHeartRate = list.size() > 0 ? totalHeartRate / list.size() : 0;

        labelCount = Math.min(stepFreqs.size() / 2, 10);

        stepFreqInfo.max = maxStepFreq;
        stepFreqInfo.maxX = Math.max(120, index);
        stepFreqInfo.average = averageStepFreq;
        stepFreqInfo.values = stepFreqs;

        heartRateInfo.max = maxHeartRate;
        heartRateInfo.maxX = Math.max(120, index);
        heartRateInfo.average = averageHeartRate;
        heartRateInfo.values = heartRates;

        stepFreqInfo.labelCount = labelCount;
        heartRateInfo.labelCount = labelCount;

        initChartView(mBinding.layoutStepFreq, stepFreqInfo);
        initChartView(mBinding.layoutHeartRate, heartRateInfo);

        mBinding.includeSportsDetail.layoutSportsDetailInfoHeartRate.tvSportsDetailValue.setText(String.valueOf(heartRateInfo.average));
        mBinding.includeSportsDetail.layoutSportsDetailInfoHeartRate.tvSportsDetailTitle.setText(heartRateInfo.title);
        initSportsStatus(sportRecord);

    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    private void initHeaderView(SportRecord sportRecord) {
//        double averagePace = sportRecord.getDistance() <= 0 ? 0 : (1.0 * sportRecord.getDuration() / (sportRecord.getDistance() / 1000.0));
        long duration = sportRecord.getDuration();
        final Converter unitConverter = new KMUnitConverter().getConverter(BaseUnitConverter.getType());

        float distance = sportRecord.getDistance() / 1000.0f;
        mBinding.tvDate.setText(new SimpleDateFormat("yyyy/MM/dd\t\tHH:mm", Locale.ENGLISH).format(sportRecord.getStartTime()));
        mBinding.tvDistance.setText(CalendarUtil.formatString("%.2f", unitConverter.value(distance)));
        mBinding.tvDistanceUint.setText(unitConverter.unit());
        mBinding.includeSportsDetail.layoutSportsDetailInfoPace.tvSportsDetailTitle.setText(R.string.average_pace);


        mBinding.includeSportsDetail.layoutSportsDetailInfoDuration.tvSportsDetailTitle.setText(R.string.total_time);
        mBinding.includeSportsDetail.layoutSportsDetailInfoDuration.tvSportsDetailValue.setText(CalendarUtil.formatSeconds(duration));

        mBinding.includeSportsDetail.layoutSportsDetailInfoKcal.tvSportsDetailTitle.setText(R.string.kcal);
        mBinding.includeSportsDetail.layoutSportsDetailInfoKcal.tvSportsDetailValue.setText("" + sportRecord.getKcal());

        mBinding.includeSportsDetail.layoutSportsDetailInfoStep.tvSportsDetailTitle.setText(R.string.step_number_and_unit);
        mBinding.includeSportsDetail.layoutSportsDetailInfoStep.tvSportsDetailValue.setText("" + sportRecord.getStep());


//        mBinding.tvSpeed.setText(FormatUtil.paceFormat((long) averagePace));
//        mBinding.tvTime.setText(CalendarUtil.formatSeconds(duration));
//        mBinding.tvKcal.setText("" + kcal);

    }


    private void initPaceView(SportRecord sportRecord) {
        List<SportRecord.Pace> paces = sportRecord.getPaces();
        if (paces == null || paces.size() < 1) {
            paces = new ArrayList<>();
        }
        int[] values = new int[paces.size()];

        for (int i = 0; i < values.length; i++) {
            SportRecord.Pace pace = paces.get(i);
            values[i] = pace.value;
        }
        int max = 0, average;
        for (int value : values) {
            max = Math.max(max, value);
        }

        average = sportRecord.getDistance() <= 0 ? 0 : (int) (1.0 * sportRecord.getDuration() / (sportRecord.getDistance() / 1000.0));

        mBinding.includeSportsDetail.layoutSportsDetailInfoPace.tvSportsDetailValue.setText(FormatUtil.paceFormat(average));
        mBinding.paceviewInfo.setPaces(values, max, average);
    }


    private void initSportsStatus(SportRecord sportRecord) {
        int[] values = sportRecord.getSportsStatus();
        int max = 0;
        if (values == null || values.length < 1) {
            values = new int[5];
        } else {
            for (int value : values) {
                max = Math.max(max, value);
            }
        }
        int[] maxModes = new int[]{
//                R.string.real_sports_status_0,
                R.string.real_sports_status_1,
                R.string.real_sports_status_2,
                R.string.real_sports_status_3,
                R.string.real_sports_status_4, R.string.real_sports_status_5
        };

        int[] saveModes = new int[]{
//                R.string.save_sports_status_0,
                R.string.save_sports_status_1,
                R.string.save_sports_status_2,
                R.string.save_sports_status_3,
                R.string.save_sports_status_4, R.string.save_sports_status_5
        };

        int[] pbRes = new int[]{
//                R.drawable.pb_sports_status_0,
                R.drawable.pb_sports_status_1,
                R.drawable.pb_sports_status_2,
                R.drawable.pb_sports_status_3,
                R.drawable.pb_sports_status_4,
                R.drawable.pb_sports_status_5
        };
        int[] titleRes = sportRecord.getHeartRateMode() == SportRecord.HEART_RATE_MODE_MAX ? maxModes : saveModes;

        ItemSportsDetailSportStatusBinding[] items = new ItemSportsDetailSportStatusBinding[]{
                mBinding.sportsDetailSportStatus1,
                mBinding.sportsDetailSportStatus2,
                mBinding.sportsDetailSportStatus3,
                mBinding.sportsDetailSportStatus4,
                mBinding.sportsDetailSportStatus5,
        };
        for (int i = 0; i < values.length; i++) {
            initPbSportsStatus(items[i], pbRes[i], titleRes[i], values[i], max);
        }

    }


    //todo 暂时用的是假数据
    private void initPbSportsStatus(ItemSportsDetailSportStatusBinding item, int pbBg, int title, int value, int max) {
        item.tvSportsStatusValue.setText(CalendarUtil.formatSeconds(value));
        item.tvSportsStatusTitle.setText(title);
        item.pbSportsStatusValue.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), pbBg, requireActivity().getTheme()));
        item.pbSportsStatusValue.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            item.pbSportsStatusValue.setProgress(value, true);
        } else {
            item.pbSportsStatusValue.setProgress(value);
        }
    }


    private void initChartView(com.jieli.healthaide.databinding.ItemSportsDetailLineChartBinding item, Info info) {
        item.tvSportsDetailsChartTitle.setCompoundDrawablesWithIntrinsicBounds(info.icon, 0, 0, 0);
        item.tvSportsDetailsChartTitle.setText(info.title);

        item.tvSportsDetailsChartAverageTitle.setText(info.averageTitle);
        item.tvSportsDetailsChartAverage.setText(String.valueOf(info.average));

        item.tvSportsDetailsChartMaxTitle.setText(info.maxTitle);
        item.tvSportsDetailsChartMax.setText(String.valueOf(info.max));

        LineChart lineChart = item.lineChartSportsDetailsChart;
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setSpaceBottom(0);
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawAxisLine(false);
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(Math.max(120, info.max + 20));

        lineChart.getAxisRight().setEnabled(false);

        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);

        lineChart.getLegend().setEnabled(false);


        float max = Math.max(info.maxX / 60.0f, 10.0f);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMaximum(max);
        xAxis.setEnabled(true);
        xAxis.setTextSize(10);
        xAxis.setDrawAxisLine(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);

        xAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.line_color, requireActivity().getTheme()));
        xAxis.setValueFormatter(new ValueFormatter() {
            private int last;

            @SuppressLint("DefaultLocale")
            @Override
            public String getFormattedValue(float value) {
                int space = (int) (value - last);
                last = (int) value;
                return last + space > max ? CalendarUtil.formatString("%dmin", last) : String.valueOf((int) value);
            }
        });

        LineDataSet lineDataSet = new LineDataSet(info.values, "min");
        lineDataSet.setColor(ResourcesCompat.getColor(getResources(), info.lineColor, requireActivity().getTheme()));
        lineDataSet.setFillDrawable(ResourcesCompat.getDrawable(getResources(), info.bgDrawable, requireActivity().getTheme()));
        lineDataSet.setDrawFilled(true);
        lineDataSet.setDrawCircles(false);
        LineData lineData = new LineData(lineDataSet);
        lineData.setDrawValues(false);
        lineData.setHighlightEnabled(false);
        lineChart.clear();
        lineChart.setData(lineData);

    }


    private static class Info {

        int title;
        int maxTitle;
        int averageTitle;
        int icon;

        int average;
        int max;

        int maxX;

        ArrayList<Entry> values;

        int lineColor;

        int bgDrawable;

        int labelCount;

        @NonNull
        @Override
        public String toString() {
            return "Info{" +
                    "title=" + title +
                    ", maxTitle=" + maxTitle +
                    ", averageTitle=" + averageTitle +
                    ", icon=" + icon +
                    ", average=" + average +
                    ", max=" + max +
                    ", maxX=" + maxX +
                    ", values=" + values +
                    ", lineColor=" + lineColor +
                    ", bgDrawable=" + bgDrawable +
                    ", labelCount=" + labelCount +
                    '}';
        }
    }
}
