
package com.jieli.healthaide.ui.health.heartrate.charts;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.renderer.LineScatterCandleRadarRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jieli.healthaide.ui.health.chart_common.Fill;

import java.lang.ref.WeakReference;
import java.util.List;

public class CandleStickChartRenderer extends LineScatterCandleRadarRenderer {

    public CandleDataProvider mChart;
    private Chart mChartView;
    private float[] mShadowBuffers = new float[8];
    private float[] mBodyBuffers = new float[4];
    private float[] mRangeBuffers = new float[4];
    private float[] mOpenBuffers = new float[4];
    private float[] mCloseBuffers = new float[4];

    public CandleStickChartRenderer(CandleDataProvider chart, Chart chartView, ChartAnimator animator,
                                    ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
        mChartView = chartView;
    }

    @Override
    public void initBuffers() {

    }

    @Override
    public void drawData(Canvas c) {
        isDrawedPointer = false;
        CandleData candleData = mChart.getCandleData();

        for (ICandleDataSet set : candleData.getDataSets()) {

            if (set.isVisible())
                drawDataSet(c, set);
        }
        if (isFirstRefreshData) {
            isFirstRefreshData = false;
        } else if (isDataChange) {
            isDataChange = false;
            mChartView.highlightValue(getHighLightXByCurrentPointerPosition(), 0);
        }
    }

    @SuppressWarnings("ResourceAsColor")
    protected void drawDataSet(Canvas c, ICandleDataSet dataSet) {

        {
            Highlight[] indices = mChartView.getHighlighted();
            CandleData candleData = mChart.getCandleData();
            if (indices != null) {
                for (Highlight high : indices) {

                    ICandleDataSet set = candleData.getDataSetByIndex(high.getDataSetIndex());

                    if (set == null || !set.isHighlightEnabled())
                        continue;

                    CandleEntry e = set.getEntryForXValue(high.getX(), high.getY());

                    if (!isInBoundsX(e, set))
                        continue;
                    {/***moveUp放手后靠近高亮*/
                        Transformer trans = this.mChart.getTransformer(set.getAxisDependency());
                        highLightX = e.getX();
                        float[] zeroLineBuffer = new float[2];
                        zeroLineBuffer[0] = highLightX;
                        zeroLineBuffer[1] = 0f;
                        trans.pointValuesToPixel(zeroLineBuffer);
                    }
                }
            }
        }

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
        {/**计算对应px值**/
            float[] zeroLineBuffer = new float[4];
            zeroLineBuffer[0] = mChart.getXChartMin();
            zeroLineBuffer[1] = 0f;
            zeroLineBuffer[2] = mChart.getXChartMax();
            zeroLineBuffer[3] = mChart.getYChartMin();
            trans.pointValuesToPixel(zeroLineBuffer);
            startX = zeroLineBuffer[0];
            endX = zeroLineBuffer[2];
            bottomY = zeroLineBuffer[3];
        }
        float phaseY = mAnimator.getPhaseY();
        float barSpace = dataSet.getBarSpace();
        boolean showCandleBar = dataSet.getShowCandleBar();

        mXBounds.set(mChart, dataSet);

        mRenderPaint.setStrokeWidth(dataSet.getShadowWidth());

        // draw the body
        for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {

            // get the entry
            CandleEntry e = dataSet.getEntryForIndex(j);

            if (e == null)
                continue;

            final float xPos = e.getX();

            float open = e.getOpen();
            final float close = e.getClose();
            final float high = e.getHigh();
            final float low = e.getLow();
            {//todo 修改：：如果open范围小于等于85，则用85.5表示
                if (open <= 85f && open != Y_DEFAULT_EMPTY) {
                    open = 85.5f;
                }
            }
            if (showCandleBar) {
                // calculate the shadow

                mShadowBuffers[0] = xPos;
                mShadowBuffers[2] = xPos;
                mShadowBuffers[4] = xPos;
                mShadowBuffers[6] = xPos;

                if (open > close) {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = open * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = close * phaseY;
                } else if (open < close) {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = close * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = open * phaseY;
                } else {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = open * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = mShadowBuffers[3];
                }

                trans.pointValuesToPixel(mShadowBuffers);

                // draw the shadows

                if (dataSet.getShadowColorSameAsCandle()) {

                    if (open > close)
                        mRenderPaint.setColor(
                                dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getDecreasingColor()
                        );

                    else if (open < close)
                        mRenderPaint.setColor(
                                dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getIncreasingColor()
                        );

                    else
                        mRenderPaint.setColor(
                                dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getNeutralColor()
                        );

                } else {
                    mRenderPaint.setColor(
                            dataSet.getShadowColor() == ColorTemplate.COLOR_NONE ?
                                    dataSet.getColor(j) :
                                    dataSet.getShadowColor()
                    );
                }

                mRenderPaint.setStyle(Paint.Style.STROKE);

                c.drawLines(mShadowBuffers, mRenderPaint);

                // calculate the body

                mBodyBuffers[0] = xPos - 0.5f + barSpace;
                mBodyBuffers[1] = close * phaseY;
                mBodyBuffers[2] = (xPos + 0.5f - barSpace);
                mBodyBuffers[3] = open * phaseY;

                trans.pointValuesToPixel(mBodyBuffers);

                // draw body differently for increasing and decreasing entry
                final boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());
                if (open > close) { // decreasing

                    if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getDecreasingColor());
                    }
                    mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());


                    Fill[] fills = (Fill[]) e.getData();
//                    Fill fill = (xPos == highLightX) ? mFills[0] : mFills[1];
                    Fill fill = (xPos == highLightX) ? fills[0] : fills[1];
                    fill.fillRect(
                            c, mRenderPaint,
                            mBodyBuffers[0], mBodyBuffers[3],
                            mBodyBuffers[2], mBodyBuffers[1],
                            isInverted ? Fill.Direction.DOWN : Fill.Direction.UP);
                    /*c.drawRect(
                            mBodyBuffers[0], mBodyBuffers[3],
                            mBodyBuffers[2], mBodyBuffers[1],
                            mRenderPaint);*/

                } else if (open < close) {

                    if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getIncreasingColor());
                    }
                    mRenderPaint.setStyle(dataSet.getIncreasingPaintStyle());
                    Fill[] fills = (Fill[]) e.getData();
//                    Fill fill = (xPos == highLightX) ? mFills[0] : mFills[1];
                    Fill fill = (xPos == highLightX) ? fills[0] : fills[1];
                    fill.fillRect(
                            c, mRenderPaint,
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            isInverted ? Fill.Direction.DOWN : Fill.Direction.UP);
                   /* c.drawRect(
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            mRenderPaint);*/
                } else { // equal values

                    if (dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getNeutralColor());
                    }
                    Fill[] fills = (Fill[]) e.getData();
//                    Fill fill = (xPos == highLightX) ? mFills[0] : mFills[1];
                    Fill fill = (xPos == highLightX) ? fills[0] : fills[1];
                    fill.fillRect(
                            c, mRenderPaint,
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            isInverted ? Fill.Direction.DOWN : Fill.Direction.UP);
                   /* c.drawLine(
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            mRenderPaint);*/
                }
            } else {
                mRangeBuffers[0] = xPos;
                mRangeBuffers[1] = high * phaseY;
                mRangeBuffers[2] = xPos;
                mRangeBuffers[3] = low * phaseY;

                mOpenBuffers[0] = xPos - 0.5f + barSpace;
                mOpenBuffers[1] = open * phaseY;
                mOpenBuffers[2] = xPos;
                mOpenBuffers[3] = open * phaseY;

                mCloseBuffers[0] = xPos + 0.5f - barSpace;
                mCloseBuffers[1] = close * phaseY;
                mCloseBuffers[2] = xPos;
                mCloseBuffers[3] = close * phaseY;

                trans.pointValuesToPixel(mRangeBuffers);
                trans.pointValuesToPixel(mOpenBuffers);
                trans.pointValuesToPixel(mCloseBuffers);

                // draw the ranges
                int barColor;

                if (open > close)
                    barColor = dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getDecreasingColor();
                else if (open < close)
                    barColor = dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getIncreasingColor();
                else
                    barColor = dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getNeutralColor();

                mRenderPaint.setColor(barColor);
                c.drawLine(
                        mRangeBuffers[0], mRangeBuffers[1],
                        mRangeBuffers[2], mRangeBuffers[3],
                        mRenderPaint);
                c.drawLine(
                        mOpenBuffers[0], mOpenBuffers[1],
                        mOpenBuffers[2], mOpenBuffers[3],
                        mRenderPaint);
                c.drawLine(
                        mCloseBuffers[0], mCloseBuffers[1],
                        mCloseBuffers[2], mCloseBuffers[3],
                        mRenderPaint);
            }
        }
        drawPointer(c);
    }

    @Override
    public void drawValues(Canvas c) {

        // if values are drawn
        if (isDrawingValuesAllowed(mChart)) {

            List<ICandleDataSet> dataSets = mChart.getCandleData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {

                ICandleDataSet dataSet = dataSets.get(i);

                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                mXBounds.set(mChart, dataSet);

                float[] positions = trans.generateTransformedValuesCandle(
                        dataSet, mAnimator.getPhaseX(), mAnimator.getPhaseY(), mXBounds.min, mXBounds.max);

                float yOffset = Utils.convertDpToPixel(5f);

                MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                for (int j = 0; j < positions.length; j += 2) {

                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;

                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    CandleEntry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);

                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                dataSet.getValueFormatter(),
                                entry.getHigh(),
                                entry,
                                i,
                                x,
                                y - yOffset,
                                dataSet
                                        .getValueTextColor(j / 2));
                    }

                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                        Drawable icon = entry.getIcon();

                        Utils.drawImage(
                                c,
                                icon,
                                (int) (x + iconsOffset.x),
                                (int) (y + iconsOffset.y),
                                icon.getIntrinsicWidth(),
                                icon.getIntrinsicHeight());
                    }
                }
                MPPointF.recycleInstance(iconsOffset);
            }
        }
    }

    /**
     * Draws the value of the given entry by using the provided IValueFormatter.
     *
     * @param c            canvas
     * @param formatter    formatter for custom value-formatting
     * @param value        the value to be drawn
     * @param entry        the entry the value belongs to
     * @param dataSetIndex the index of the DataSet the drawn Entry belongs to
     * @param x            position
     * @param y            position
     * @param color
     */
    public void drawValue(Canvas c, IValueFormatter formatter, float value, Entry entry, int dataSetIndex, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(formatter.getFormattedValue(value, entry, dataSetIndex, mViewPortHandler), x, y, mValuePaint);
    }

    @Override
    public void drawValue(Canvas canvas, String s, float v, float v1, int i) {

    }

    @Override
    public void drawExtras(Canvas c) {
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        CandleData candleData = mChart.getCandleData();

        for (Highlight high : indices) {

            ICandleDataSet set = candleData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            CandleEntry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;
            {/***moveUp放手后靠近高亮*/
                Transformer trans = this.mChart.getTransformer(set.getAxisDependency());
                highLightX = e.getX();
                float[] zeroLineBuffer = new float[2];
                zeroLineBuffer[0] = highLightX;
                zeroLineBuffer[1] = 0f;
                trans.pointValuesToPixel(zeroLineBuffer);
                finalX = zeroLineBuffer[0];
                if (pointerX == DEFAULT_POINTER_X && pointerEvent == null) {//初始化的时候，算出高亮位置
                    pointerX = finalX;
                } else if (isMoveUp) {
//                    Log.d(TAG, "drawDataSet: animator isMoveUp");
                    isMoveUp = false;
                    View view = viewWeakReference.get();
                    ValueAnimator animator = ValueAnimator.ofFloat(pointerX, finalX);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            pointerX = (float) valueAnimator.getAnimatedValue();
                            view.invalidate();
                        }
                    });
                    animator.setDuration(100);
                    animator.setRepeatCount(0);
                    animator.start();
                }
            }
            float lowValue = e.getLow() * mAnimator.getPhaseY();
            float highValue = e.getHigh() * mAnimator.getPhaseY();
            float y = (lowValue + highValue) / 2f;
            drawPointer(c);//todo 不清楚直接这样加会不会有bug，后面再看看

            MPPointD pix = mChart.getTransformer(set.getAxisDependency()).getPixelForValues(e.getX(), y);

            high.setDraw((float) pix.x, (float) pix.y);
            /* *//**修改:: 增加选中高亮*//*
            mHighlightPaint.setColor(set.getHighLightColor());
            final boolean isInverted = mChart.isInverted(set.getAxisDependency());
            mFill.fillRect(
                    c, mHighlightPaint,
                    mBodyBuffers[0], mBodyBuffers[3],
                    mBodyBuffers[2], mBodyBuffers[1],
                    isInverted ? Fill.Direction.DOWN : Fill.Direction.UP);*/
            // draw the lines
//            drawHighlightLines(c, (float) pix.x, (float) pix.y, set);
        }
    }


    MotionEvent pointerEvent;
    final float DEFAULT_POINTER_X = -111f;
    float pointerX = DEFAULT_POINTER_X;
    float pointerY = 0f;
    boolean isMoveUp = false;
    WeakReference<View> viewWeakReference;
    private final String TAG = this.getClass().getSimpleName();

    public void setPointerPosition(View view, MotionEvent event) {
//        Log.d(TAG, "setPointerPosition: ");
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                pointerX = Math.max(event.getX(), startX);//todo 考虑超出图表范围
                pointerX = Math.min(pointerX, endX);//todo 考虑超出图表范围
                pointerY = event.getY() < 0 ? 0 : event.getY();//todo 考虑超出图表范围
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                Log.d(TAG, "setPointerPosition: ACTION_UP");
                pointerX = Math.max(event.getX(), startX);//todo 考虑超出图表范围
                pointerX = Math.min(pointerX, endX);//todo 考虑超出图表范围
                pointerY = event.getY() < 0 ? 0 : event.getY();//todo 考虑超出图表范围
                if (!isMoveUp) {
                    isMoveUp = true;
                    viewWeakReference = new WeakReference<View>(view);
                }
                break;
        }
        pointerEvent = event;
    }

    Paint pointerPaint = new Paint();
    //两个坐标形成变量，规定了渐变的方向和间距大小，着色器为镜像
    int[] colors = {Color.parseColor("#01FFFFFF"), Color.parseColor("#9FD37A"), Color.parseColor("#01FFFFFF")};
    float[] position = {0f, 0.5f, 1.0f};
    float startX = 0f;//图表的最左y
    float endX = 0f;//图表的最右y
    float finalX = 0f;//抬手后最终停止位置
    float bottomY = 0f;//图表的最低x
    float highLightX = 0f;//当前高亮的x值
    public static float Y_DEFAULT_EMPTY = 55.555555f;//CandleStickChart不能设置超过y轴最小值很多的值，此处低于50就会高亮无效
    private volatile boolean isDrawedPointer = false;

    private void drawPointer(Canvas canvas) {
        if (isDrawedPointer || pointerX == DEFAULT_POINTER_X) {
            return;
        }
        isDrawedPointer = true;
        Log.d("TAG", "drawPointer: pointerX :: " + pointerX);
        LinearGradient linearGradient = new LinearGradient(0, 0, 5f, (float) (bottomY)
                , colors, position, Shader.TileMode.CLAMP);
        pointerPaint.setShader(linearGradient);
        pointerPaint.setStrokeWidth(4);
        canvas.drawLine(pointerX, 0, pointerX, (float) (bottomY) * 2, pointerPaint);
    }

    /**
     * 修改X轴的范围后，根据游标位置获取靠近游标的高亮Entry
     * 步骤一:先修改Chart的X轴范围，然后调用chart.invalidate()刷新
     * 步骤二:调用getHighLightXByCurrentPointerPosition()获取对应的高亮值,然后chart.highValue()
     *
     * @return 高亮Entry的x值
     */
    public float getHighLightXByCurrentPointerPosition() {//在这之前需要
        Transformer trans = this.mChart.getTransformer(YAxis.AxisDependency.LEFT);
        MPPointD mpPointD = trans.getValuesByTouchPoint(pointerX, bottomY);
        ICandleDataSet set = this.mChart.getCandleData().getDataSetByIndex(0);
        if (set == null || !set.isHighlightEnabled()) return highLightX;
        CandleEntry e = set.getEntryForXValue((float) mpPointD.x, bottomY);
        highLightX = e.getX();
        return highLightX;
    }

    private volatile boolean isDataChange = false;
    private volatile boolean isFirstRefreshData = true;

    public void notifyDataSetChanged() {
        this.isDataChange = true;
    }
}
