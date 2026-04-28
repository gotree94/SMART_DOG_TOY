package com.jieli.healthaide.ui.health.chart_common;

import android.graphics.Matrix;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/5/21 1:56 PM
 * @desc :
 */
public class CustomTouchListener extends BarLineChartTouchListener {
    /**
     * Constructor with initialization parameters.
     *
     * @param chart               instance of the chart
     * @param touchMatrix         the touch-matrix of the chart
     * @param dragTriggerDistance the minimum movement distance that will be interpreted as a "drag" gesture in dp (3dp equals
     */
    public CustomTouchListener(BarLineChartBase<? extends BarLineScatterCandleBubbleData<? extends IBarLineScatterCandleBubbleDataSet<? extends Entry>>> chart, Matrix touchMatrix, float dragTriggerDistance) {
        super(chart, touchMatrix, dragTriggerDistance);
    }



    @Override
    protected void performHighlight(Highlight h, MotionEvent e) {
        if (h == null || h.equalTo(mLastHighlighted)) {
            mChart.highlightValue(mLastHighlighted, true);
        } else {
            mChart.highlightValue(h, true);
            mLastHighlighted = h;
        }
    }
}
