package com.jieli.healthaide.ui.health.binder_adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.binder.BaseItemBinder;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.entity.HeartRateEntity;
import com.jieli.healthaide.ui.health.heartrate.charts.HeartRateLineChart;
import com.jieli.healthaide.ui.health.heartrate.charts.HeartRateLineDataSet;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @ClassName: BloodOxygenBinder
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:37
 */
public class HeartRateBinder extends BaseItemBinder<HeartRateEntity, BaseViewHolder> {
    private HeartRateLineChart chart;
    private final float DefaultYAxisMax = 225;
    private float yAxisMax = DefaultYAxisMax;
    private float yAxisMin = 15;

    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, HeartRateEntity heartRateEntity) {
        if (heartRateEntity.getLastHeartBeat() != 0) {
            baseViewHolder.setText(R.id.tv_health_empty, String.valueOf(heartRateEntity.getLastHeartBeat()));
            baseViewHolder.setText(R.id.tv_health_date, CustomTimeFormatUtil.getTimeInterval(heartRateEntity.getLeftTime(), 1, CalenderSelectorView.TYPE_WEEK));
        }else {
            baseViewHolder.setText(R.id.tv_health_date, getContext().getString(R.string.empty_date));
        }
        FrameLayout frameLayout = baseViewHolder.getView(R.id.fl_charts_parent);
        initChartsView(frameLayout);
        LineData chartData = getChartData(heartRateEntity);
        chart.setData(chartData);
        chart.setClickable(false);
    }

    private void initChartsView(FrameLayout frameLayout) {
        frameLayout.removeAllViews();
        chart = getChartsView();
        frameLayout.addView(chart);
    }

    protected HeartRateLineChart getChartsView() {
        chart = new HeartRateLineChart(getContext());
        initChart(chart);
        return chart;
    }

    private void initChart(HeartRateLineChart chart) {
        {   // // Chart Style // //
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);// scaling can now only be done on x- and y-axis separately
            chart.setDoubleTapToZoomEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setScaleEnabled(false);//关掉放大
            chart.setAutoScaleMinMaxEnabled(false);
            chart.setViewPortOffsets(0, 0, 0, 0);//此处是为固定给label文字留空间，否则字的多少导致边空间改变
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0.5f);//设置x轴的最小值
            xAxis.setAxisMaximum(1440.5f);//设置最大值
            xAxis.setDrawLabels(false);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);//去掉边线
            xAxis.setAvoidFirstLastClipping(true);//避免lable被裁剪一部分
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
            rightAxis.setDrawLabels(false);
        }
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }

    protected LineData getChartData(HeartRateEntity heartRateEntity) {
        ArrayList<Entry> values = heartRateEntity.getData();
        HeartRateLineDataSet heartRateLineDataSet = new HeartRateLineDataSet(values, "DataSet 1");
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
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.shap_heart_rate_red);
            heartRateLineDataSet.setFillDrawable(drawable);
        } else {
            heartRateLineDataSet.setFillColor(Color.WHITE);
        }
        heartRateLineDataSet.setColor(0xffff0000);//线条颜色
        heartRateLineDataSet.setCircleHoleColor(0xFFA3D07D);//选中高亮
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        heartRateLineDataSet.setDrawValues(false);
        heartRateLineDataSet.setDrawCircles(false);
        heartRateLineDataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSets.add(heartRateLineDataSet); // add the data sets
        LineData data = new LineData(dataSets);
        return data;
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_health_heart_rate, viewGroup, false));
    }
}
