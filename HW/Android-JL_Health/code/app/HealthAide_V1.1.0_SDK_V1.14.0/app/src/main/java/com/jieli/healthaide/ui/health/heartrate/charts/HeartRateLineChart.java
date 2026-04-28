
package com.jieli.healthaide.ui.health.heartrate.charts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.jieli.healthaide.ui.health.chart_common.CustomTouchListener;

/**
 * Chart that draws lines, surfaces, circles, ...
 *
 * @author Philipp Jahoda
 */
public class HeartRateLineChart extends BarLineChartBase<LineData> implements LineDataProvider {
    private final String TAG = this.getClass().getSimpleName();

    public HeartRateLineChart(Context context) {
        super(context);
    }

    public HeartRateLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeartRateLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new HearRateLineChartRendererModify(this,this, mAnimator, mViewPortHandler);
        mChartTouchListener = new CustomTouchListener(this, mViewPortHandler.getMatrixTouch(), 3f);
    }

    @Override
    public LineData getLineData() {
        return mData;
    }

    @Override
    protected void onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer instanceof HearRateLineChartRendererModify) {
            ((HearRateLineChartRendererModify) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isClickable()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ((HearRateLineChartRendererModify) mRenderer).setPointerPosition(this, event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN://多点按下
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    public float getHighLightXByCurrentPointerPosition() {
        return ((HearRateLineChartRendererModify) mRenderer).getHighLightXByCurrentPointerPosition();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isClickable()){
            requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setRenderCallback(HearRateLineChartRendererModify.RenderCallback renderCallback) {
        ((HearRateLineChartRendererModify) mRenderer).setRenderCallback(renderCallback);
    }
}
