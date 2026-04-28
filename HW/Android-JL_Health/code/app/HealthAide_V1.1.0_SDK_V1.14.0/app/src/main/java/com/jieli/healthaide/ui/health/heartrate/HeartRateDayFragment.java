package com.jieli.healthaide.ui.health.heartrate;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateBaseVo;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateDayVo;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.health.heartrate.charts.HearRateLineChartRendererModify;
import com.jieli.healthaide.ui.health.heartrate.charts.HeartRateLineChart;
import com.jieli.healthaide.ui.health.heartrate.charts.HeartRateLineDataSet;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: HeartRateDayFragmentModify
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/9 16:21
 */
public class HeartRateDayFragment extends HeartRateDataFragment<HeartRateDayVo> {
    private final float DefaultYAxisMax = 225;
    private float yAxisMax = DefaultYAxisMax;
    private float yAxisMin = 15;
    private LimitLine restingLimitLine;
    private HeartRateLineDataSet heartRateLineDataSet;
    private HeartRateLineChart chart;

    public static HeartRateDayFragment newInstance() {
        return new HeartRateDayFragment();
    }

    @Override
    protected HeartRateDayVo createVo() {
        return new HeartRateDayVo();
    }

    @Override
    protected Chart getChartsView() {
        chart = new HeartRateLineChart(requireContext());
        chart.setClickable(true);
        initChart(chart);
        chart.setRenderCallback(new HearRateLineChartRendererModify.RenderCallback() {
                                    @Override
                                    public void onCurrentPointerPositionValueX(float xValue) {
                                        //todo 只用于同步时间
                                        if (viewType == VIEW_TYPE_HEART_RATE_RANGE) {
                                            mViewModel.timeIntervalLiveData.postValue(CustomTimeFormatUtil.getMoment(xValue, CalenderSelectorView.TYPE_DAY));
                                        }
                                    }

                                    @Override
                                    public void onHighLightX(Entry entry) {
                                        Log.d(TAG, "onHighLightX: " + entry);
                                        if (viewType == VIEW_TYPE_HEART_RATE_RANGE) {
                                            String value = (entry != null && entry.getY() > 0f) ? String.valueOf((int) entry.getY()) : "- -";
                                            mViewModel.timeIntervalHeartRateValueLiveData.postValue(value);
                                        }
                                    }
                                }
        );
        return chart;
    }

    @Override
    protected ChartData getChartData() {
        ArrayList<Entry> normalDataArray = new ArrayList();
        List<HeartRateBaseVo.HeartRateCharData> sourceDataArray = vo.getEntities();
        Fill[] fills = new Fill[]{new Fill(getContext().getDrawable(R.drawable.bg_blood_oxygen_chart_shape_week_sel)), new Fill(getContext().getDrawable(R.drawable.bg_blood_oxygen_chart_shape_week_nol))};
        for (HeartRateBaseVo.HeartRateCharData heartRateEntity : sourceDataArray) {
            Entry entry;
            if (heartRateEntity.max > 0) {
                entry = new Entry(heartRateEntity.index, heartRateEntity.max, fills);
            } else {
                entry = new Entry(heartRateEntity.index, HearRateLineChartRendererModify.Y_DEFAULT_EMPTY, fills);
            }
            normalDataArray.add(entry);
        }

        heartRateLineDataSet = new HeartRateLineDataSet(normalDataArray, "DataSet 1");
        heartRateLineDataSet.setDrawIcons(false);
        // line thickness and point size
        heartRateLineDataSet.setLineWidth(1f);
        heartRateLineDataSet.setCircleRadius(3f);
        heartRateLineDataSet.setDrawCircleHole(false);
        heartRateLineDataSet.setDrawFilled(true);
        heartRateLineDataSet.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return chart.getAxisLeft().getAxisMinimum();
            }
        });
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.shap_heart_rate_white);
            heartRateLineDataSet.setFillDrawable(drawable);
        } else {
            heartRateLineDataSet.setFillColor(Color.WHITE);
        }
        heartRateLineDataSet.setColor(0xffffffff);//线条颜色
        heartRateLineDataSet.setCircleHoleColor(0xFFA3D07D);//选中高亮
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        heartRateLineDataSet.setDrawValues(false);
        heartRateLineDataSet.setDrawCircles(false);
        heartRateLineDataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSets.add(heartRateLineDataSet); // add the data sets
        LineData data = new LineData(dataSets);
        return data;
    }

    @Override
    protected void updateHighLight(ChartData chartData) {
        if (isResumed() && !isFirstRefreshData) return;
        isFirstRefreshData = false;
        if (chartData != null && chart != null) {
            int highLightX = vo.highLightIndex;
            chart.highlightValue(highLightX, 0);//高亮值
        }
    }

    @Override
    protected void refreshDataFinish() {
        super.refreshDataFinish();
        if (viewType == VIEW_TYPE_RESETING_HEART_RATE) {
            float valueF = vo.restingAvg;
            showRestingLimitLine((int) valueF);
        }
    }

    @Override
    protected int getTimeType() {
        return CalenderSelectorView.TYPE_DAY;
    }

    private void initChart(HeartRateLineChart chart) {
        {   // // Chart Style // //
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);// scaling can now only be done on x- and y-axis separately
            chart.setDoubleTapToZoomEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setScaleEnabled(false);//关掉放大
            chart.setAutoScaleMinMaxEnabled(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0.5f);//设置x轴的最小值
            xAxis.setAxisMaximum(1440.5f);//设置最大值
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);//去掉边线
            xAxis.setAvoidFirstLastClipping(true);//避免lable被裁剪一部分
            xAxis.setTextSize(10f);
            xAxis.setTextColor(0xb3ffffff);
            xAxis.setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat mFormat = CustomTimeFormatUtil.dateFormat("HH:mm");

                @Override
                public String getFormattedValue(float value) {
//                    int val = Math.round((value + 10) / 60) - 8;
                    long millis = TimeUnit.HOURS.toMillis((long) value / 60 - 8);
                    return mFormat.format(new Date(millis));
                }
            });
            xAxis.setLabelCount(5, true);
        }

        YAxis leftAxis;
        YAxis rightAxis;
        {   // // Y-Axis Style // //
            leftAxis = chart.getAxisLeft();
            leftAxis.setDrawLabels(false);
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setSpaceBottom(0f);
            leftAxis.setSpaceTop(0f);
            // axis range
            leftAxis.setAxisMaximum(yAxisMax);
            leftAxis.setAxisMinimum(yAxisMin);

            rightAxis = chart.getAxisRight();
            rightAxis.setDrawLabels(true);
            rightAxis.setDrawGridLines(false);//去掉网格线
            rightAxis.setDrawAxisLine(false);//去掉边线
            rightAxis.setSpaceBottom(0f);
            rightAxis.setSpaceTop(0f);
            rightAxis.setAxisMaximum(yAxisMax);
            rightAxis.setAxisMinimum(yAxisMin);
            rightAxis.setTextSize(10f);
            rightAxis.setTextColor(0xb3ffffff);
            rightAxis.setValueFormatter(new ValueFormatter() {
                DecimalFormat mFormat = new DecimalFormat("###,###,###,##0.0");

                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            rightAxis.setLabelCount(5, false);
        }
        int lineColor = Color.parseColor("#2AFFFFFF");
        int textColor = Color.parseColor("#2AFFFFFF");
        float lineWidth = 1f;
        float textSize = 10f;
        Integer[] limitArray = new Integer[]{40, 80, 120, 160, 200};
        for (Integer limit : limitArray) {
            LimitLine limitL = new LimitLine(limit, null);
            limitL.setTextColor(textColor);
            limitL.setLineWidth(lineWidth);
            limitL.setEnabled(true);
            limitL.setLineColor(lineColor);
            limitL.enableDashedLine(10f, 5f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
            limitL.setTextSize(textSize);
            limitL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
            chart.getAxisLeft().addLimitLine(limitL);
        }
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }

    private void showRestingLimitLine(int restingRate) {
        Log.d(TAG, "showRestingLimitLine: " + restingRate);
        if (restingLimitLine != null) {
            chart.getAxisLeft().removeLimitLine(restingLimitLine);
        }
        int lineColor = Color.parseColor("#a3d07d");
        float lineWidth = 1.2f;
        restingLimitLine = new LimitLine(restingRate, null);
        restingLimitLine.setLineWidth(lineWidth);
        restingLimitLine.setLineColor(lineColor);
        restingLimitLine.setEnabled(true);
        restingLimitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
        chart.getAxisLeft().addLimitLine(restingLimitLine);
        {
            mViewModel.timeIntervalLiveData.postValue("");
            mViewModel.timeIntervalHeartRateValueLiveData.postValue(restingRate == 0 ? "- -" : String.valueOf(restingRate));
        }
        heartRateLineDataSet.setDrawSelectedCircleEnable(false);
        heartRateLineDataSet.setRestingRate(restingRate);
        chart.invalidate();
    }

    private void hideRestingLimitLine() {
        chart.getAxisLeft().removeLimitLine(restingLimitLine);
        restingLimitLine = null;
        heartRateLineDataSet.setDrawSelectedCircleEnable(true);
        heartRateLineDataSet.setRestingRate(0);
        chart.invalidate();
    }

    @Override
    protected void changeViewType(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEART_RATE_RANGE:
                hideRestingLimitLine();
                break;
            case VIEW_TYPE_RESETING_HEART_RATE:
                float valueF = vo.restingAvg;
                showRestingLimitLine((int) valueF);
                break;
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {//心率的天视图不走高亮选中更新当前选中

    }
}
