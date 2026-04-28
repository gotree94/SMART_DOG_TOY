package com.jieli.healthaide.ui.health.sleep.charts.day;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.jieli.healthaide.ui.health.chart_common.CustomTouchListener;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/3/21 3:02 PM
 * @desc :
 */

public class SleepDayChart extends BarLineChartBase<SleepDayData> implements SleepDayDataProvider {
    public SleepDayChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SleepDayChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SleepDayChart(Context context) {
        super(context);
    }

    @Override
    public SleepDayData getSleepData() {
        return mData;
    }

    @Override
    protected void init() {
        super.init();
        mRenderer = new SleepDayDataRenderer(this, mAnimator, mViewPortHandler);
        mMarker = new SleepDayMarkerView(getContext());
        MarkerView markerView = (MarkerView) mMarker;
        markerView.setChartView(this);
        mChartTouchListener = new CustomTouchListener(this, mViewPortHandler.getMatrixTouch(), 3f);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected float[] getMarkerPosition(Highlight high) {
        SleepDayData sleepDayData = getSleepData();
        ISleepDayDataSet set = sleepDayData.getDataSetByIndex(high.getDataSetIndex());
        SleepDayEntry e = set.getEntryForXValue(high.getX(), high.getY());
        Transformer trans = getTransformer(set.getAxisDependency());
        float[] f = new float[]{e.xMin(), 4, e.xMax(), 4};
        trans.pointValuesToPixel(f);

        MPPointD pix = this.getTransformer(set.getAxisDependency()).getPixelForValues(this.getLowestVisibleX(), 5.53f);//markView的y起始位置
        return new float[]{(f[0] + f[2]) / 2, (float) pix.y};
    }
}
