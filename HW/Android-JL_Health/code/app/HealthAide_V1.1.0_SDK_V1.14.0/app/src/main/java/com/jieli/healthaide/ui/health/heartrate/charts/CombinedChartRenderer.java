package com.jieli.healthaide.ui.health.heartrate.charts;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.renderer.BubbleChartRenderer;
import com.github.mikephil.charting.renderer.DataRenderer;
import com.github.mikephil.charting.renderer.ScatterChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jieli.bluetooth_connect.util.JL_Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Renderer class that is responsible for rendering multiple different data-types.
 */
public class CombinedChartRenderer extends DataRenderer {
    private final String TAG = this.getClass().getSimpleName();
    /**
     * all rederers for the different kinds of data this combined-renderer can draw
     */
    protected List<DataRenderer> mRenderers = new ArrayList<DataRenderer>(5);

    protected WeakReference<Chart> mChart;

    public CombinedChartRenderer(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = new WeakReference<Chart>(chart);
        createRenderers();
    }

    /**
     * Creates the renderers needed for this combined-renderer in the required order. Also takes the DrawOrder into
     * consideration.
     */
    public void createRenderers() {
        JL_Log.e(TAG, "createRenderers", "");

        mRenderers.clear();

        CombinedChart chart = (CombinedChart) mChart.get();
        if (chart == null)
            return;

        CombinedChart.DrawOrder[] orders = chart.getDrawOrder();

        for (CombinedChart.DrawOrder order : orders) {

            switch (order) {
                case BAR:
                    if (chart.getBarData() != null)
                        mRenderers.add(new BarChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case BUBBLE:
                    if (chart.getBubbleData() != null)
                        mRenderers.add(new BubbleChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case LINE:
                    if (chart.getLineData() != null)
                        //todo 修改 :: 使用自定义折线图
                        mRenderers.add(new WeightLineChartRenderer(chart, chart, mAnimator, mViewPortHandler));
//                        mRenderers.add(new LineChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case CANDLE:
                    if (chart.getCandleData() != null)
                        //todo 修改 ：：使用自定义蜡烛图
                        mRenderers.add(new CandleStickChartRenderer(chart, chart, mAnimator, mViewPortHandler));
//                        mRenderers.add(new com.github.mikephil.charting.renderer.CandleStickChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case SCATTER:
                    if (chart.getScatterData() != null)
                        mRenderers.add(new ScatterChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
            }
        }
    }

    @Override
    public void initBuffers() {

        for (DataRenderer renderer : mRenderers)
            renderer.initBuffers();
    }

    @Override
    public void drawData(Canvas c) {

        for (DataRenderer renderer : mRenderers)
            renderer.drawData(c);

        if (isFirstRefreshData) {
            isFirstRefreshData = false;
        } else if (isDataChange) {
            isDataChange = false;
            CombinedChart chart = (CombinedChart) mChart.get();
            Highlight highlight1 = new Highlight(getHighLightXByCurrentPointerPosition(), Float.NaN, 0);
            highlight1.setDataIndex(1);//DataIndex 要设置为1，否则不行
            chart.highlightValue(highlight1, true);
        }
    }

    @Override
    public void drawValues(Canvas c) {

        for (DataRenderer renderer : mRenderers)
            renderer.drawValues(c);
    }

    @Override
    public void drawValue(Canvas canvas, String s, float v, float v1, int i) {

    }

    @Override
    public void drawExtras(Canvas c) {

        for (DataRenderer renderer : mRenderers)
            renderer.drawExtras(c);
    }

    protected List<Highlight> mHighlightBuffer = new ArrayList<Highlight>();

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        Chart chart = mChart.get();
        if (chart == null) return;

        for (DataRenderer renderer : mRenderers) {
            ChartData data = null;

//            if (renderer instanceof BarChartRenderer)
//                data = ((BarChartRenderer)renderer).mChart.getBarData();
//            else if (renderer instanceof LineChartRenderer)
//                data = ((LineChartRenderer)renderer).mChart.getLineData();
//            else if (renderer instanceof com.github.mikephil.charting.renderer.CandleStickChartRenderer)
//                data = ((CandleStickChartRenderer)renderer).mChart.getCandleData();
//            else if (renderer instanceof ScatterChartRenderer)
//                data = ((ScatterChartRenderer)renderer).mChart.getScatterData();
//            else if (renderer instanceof BubbleChartRenderer)
//                data = ((BubbleChartRenderer)renderer).mChart.getBubbleData();
            {//todo 修改：： 使用自定义chartRendere
                if (renderer instanceof WeightLineChartRenderer)
                    data = ((WeightLineChartRenderer) renderer).mChart.getLineData();
                else if (renderer instanceof CandleStickChartRenderer)
                    data = ((CandleStickChartRenderer) renderer).mChart.getCandleData();
                else {
                    return;
                }
            }
            int dataIndex = data == null ? -1
                    : ((CombinedData) chart.getData()).getAllData().indexOf(data);

            mHighlightBuffer.clear();

            for (Highlight h : indices) {
                /*if (h.getDataIndex() == dataIndex || h.getDataIndex() == -1)
                    mHighlightBuffer.add(h);*/
                {//todo 修改 ：：
                    if (dataIndex != -1 || h.getDataIndex() == -1)
                        mHighlightBuffer.add(h);
                }
            }
            renderer.drawHighlighted(c, mHighlightBuffer.toArray(new Highlight[mHighlightBuffer.size()]));
        }
    }

    /**
     * Returns the sub-renderer object at the specified index.
     *
     * @param index
     * @return
     */
    public DataRenderer getSubRenderer(int index) {
        if (index >= mRenderers.size() || index < 0)
            return null;
        else
            return mRenderers.get(index);
    }

    /**
     * Returns all sub-renderers.
     *
     * @return
     */
    public List<DataRenderer> getSubRenderers() {
        return mRenderers;
    }

    public void setSubRenderers(List<DataRenderer> renderers) {
        this.mRenderers = renderers;
    }


    public void setPointerPosition(View view, MotionEvent event) {
        for (DataRenderer renderer : mRenderers) {
            if (renderer instanceof WeightLineChartRenderer)
                ((WeightLineChartRenderer) renderer).setPointerPosition(view, event);
            else if (renderer instanceof CandleStickChartRenderer)
                ((CandleStickChartRenderer) renderer).setPointerPosition(view, event);
        }
    }

    /**
     * 修改X轴的范围后，根据游标位置获取靠近游标的高亮Entry
     * 步骤一:先修改Chart的X轴范围，然后调用chart.invalidate()刷新
     * 步骤二:调用getHighLightXByCurrentPointerPosition()获取对应的高亮值,然后chart.highValue()
     *
     * @return 高亮Entry的x值
     */
    public float getHighLightXByCurrentPointerPosition() {
        {//增加对月视图没有设置高亮时，自动选择游标就近的高亮
//            Transformer trans = this.mChart.getTransformer(YAxis.AxisDependency.LEFT);
            float highLightX = 0f;
            for (DataRenderer renderer : mRenderers) {
                if (renderer instanceof WeightLineChartRenderer)
                    highLightX = ((WeightLineChartRenderer) renderer).getHighLightXByCurrentPointerPosition();
                else if (renderer instanceof CandleStickChartRenderer)
                    highLightX = ((CandleStickChartRenderer) renderer).getHighLightXByCurrentPointerPosition();
            }
            return highLightX;
        }
    }

    private volatile boolean isDataChange = false;
    private volatile boolean isFirstRefreshData = true;

    public void notifyDataSetChanged() {
        this.isDataChange = true;
    }
}
