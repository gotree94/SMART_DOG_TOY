
package com.jieli.healthaide.ui.health.sleep.charts.week;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.Range;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.renderer.BarLineScatterCandleBubbleRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jieli.healthaide.ui.health.chart_common.Fill;

import java.lang.ref.WeakReference;
import java.util.List;

public class CustomBarChartRenderer extends BarLineScatterCandleBubbleRenderer {
    private final String TAG = this.getClass().getSimpleName();
    protected BarDataProvider mChart;
    protected RectF mBarRect = new RectF();
    protected BarBuffer[] mBarBuffers;
    protected Paint mShadowPaint;
    protected Paint mBarBorderPaint;
    private RectF mBarShadowRectBuffer = new RectF();
    private Chart mChartView;

    public CustomBarChartRenderer(BarDataProvider chart, Chart chartView, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        this.mChart = chart;
        this.mChartView = chartView;
        this.mHighlightPaint = new Paint(1);
        this.mHighlightPaint.setStyle(Style.FILL);
        this.mHighlightPaint.setColor(Color.rgb(0, 0, 0));
        this.mHighlightPaint.setAlpha(120);
        this.mShadowPaint = new Paint(1);
        this.mShadowPaint.setStyle(Style.FILL);
        this.mBarBorderPaint = new Paint(1);
        this.mBarBorderPaint.setStyle(Style.STROKE);
    }

    public void initBuffers() {
        BarData barData = this.mChart.getBarData();
        this.mBarBuffers = new BarBuffer[barData.getDataSetCount()];

        for (int i = 0; i < this.mBarBuffers.length; ++i) {
            IBarDataSet set = (IBarDataSet) barData.getDataSetByIndex(i);
            this.mBarBuffers[i] = new BarBuffer(set.getEntryCount() * 4 * (set.isStacked() ? set.getStackSize() : 1), barData.getDataSetCount(), set.isStacked());
        }

    }

    public void drawData(Canvas c) {
        isDrawedPointer = false;
        BarData barData = this.mChart.getBarData();
        for (int i = 0; i < barData.getDataSetCount(); ++i) {
            IBarDataSet set = (IBarDataSet) barData.getDataSetByIndex(i);
            if (set.isVisible()) {
                this.drawDataSet(c, set, i);
            }
        }
        if (isFirstRefreshData) {
            isFirstRefreshData = false;
        } else if (isDataChange) {
            isDataChange = false;
            mChartView.highlightValue(getHighLightXByCurrentPointerPosition(), 0);
        }
    }

    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        {
            BarData barData = this.mChart.getBarData();

            Highlight[] var4 = mChartView.getHighlighted();
            int var5 = var4 == null ? 0 : var4.length;
//        Log.d(TAG, "drawHighlighted: " + highLightX);
//        Log.d(TAG, "drawHighlighted: highLight Len :: " + var5);
            for (int var6 = 0; var6 < var5; ++var6) {
                Highlight high = var4[var6];
                IBarDataSet set = (IBarDataSet) barData.getDataSetByIndex(high.getDataSetIndex());
                if (set != null && set.isHighlightEnabled()) {
                    BarEntry e = (BarEntry) set.getEntryForXValue(high.getX(), high.getY());
                    if (this.isInBoundsX(e, set)) {
                        Transformer trans = this.mChart.getTransformer(set.getAxisDependency());
                        {/***moveUp放手后靠近高亮*/
                            highLightX = e.getX();
                            float[] zeroLineBuffer = new float[2];
                            zeroLineBuffer[0] = highLightX;
                            zeroLineBuffer[1] = 0f;
                            trans.pointValuesToPixel(zeroLineBuffer);

                        }
                    }
                }
            }
        }
        Transformer trans = this.mChart.getTransformer(dataSet.getAxisDependency());
        this.mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        this.mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));
        boolean drawBorder = dataSet.getBarBorderWidth() > 0.0F;
        float phaseX = this.mAnimator.getPhaseX();
        float phaseY = this.mAnimator.getPhaseY();
        if (this.mChart.isDrawBarShadowEnabled()) {
            this.mShadowPaint.setColor(dataSet.getBarShadowColor());
            BarData barData = this.mChart.getBarData();
            float barWidth = barData.getBarWidth();
            float barWidthHalf = barWidth / 2.0F;
            int i = 0;

            for (int count = Math.min((int) Math.ceil((double) ((float) dataSet.getEntryCount() * phaseX)), dataSet.getEntryCount()); i < count; ++i) {
                BarEntry e = (BarEntry) dataSet.getEntryForIndex(i);
                float x = e.getX();
                this.mBarShadowRectBuffer.left = x - barWidthHalf;
                this.mBarShadowRectBuffer.right = x + barWidthHalf;
                trans.rectValueToPixel(this.mBarShadowRectBuffer);
                if (this.mViewPortHandler.isInBoundsLeft(this.mBarShadowRectBuffer.right)) {
                    if (!this.mViewPortHandler.isInBoundsRight(this.mBarShadowRectBuffer.left)) {
                        break;
                    }

                    this.mBarShadowRectBuffer.top = this.mViewPortHandler.contentTop();
                    this.mBarShadowRectBuffer.bottom = this.mViewPortHandler.contentBottom();
                    c.drawRect(this.mBarShadowRectBuffer, this.mShadowPaint);
                }
            }
        }

        BarBuffer buffer = this.mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(this.mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(this.mChart.getBarData().getBarWidth());
        buffer.feed(dataSet);
        trans.pointValuesToPixel(buffer.buffer);
        {/**计算对应px值**/
//            Log.d(TAG, "drawValues: trans !isStacked");
            float[] zeroLineBuffer = new float[4];
            zeroLineBuffer[0] = mChart.getXChartMin();
            zeroLineBuffer[1] = mChart.getYChartMax();
            zeroLineBuffer[2] = mChart.getXChartMax();
            zeroLineBuffer[3] = mChart.getYChartMin();
            trans.pointValuesToPixel(zeroLineBuffer);
            startX = zeroLineBuffer[0];
            endX = zeroLineBuffer[2];
            bottomY = zeroLineBuffer[3];
        }
        boolean isSingleColor = dataSet.getColors().size() == 1;
        if (isSingleColor) {
            this.mRenderPaint.setColor(dataSet.getColor());
        }

        for (int j = 0; j < buffer.size(); j += 4) {
            if (this.mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                if (!this.mViewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                    break;
                }

                if (!isSingleColor) {
                    this.mRenderPaint.setColor(dataSet.getColor(j / 4));
                }

                if (dataSet.getGradientColor() != null) {
                    GradientColor gradientColor = dataSet.getGradientColor();
                    this.mRenderPaint.setShader(new LinearGradient(buffer.buffer[j], buffer.buffer[j + 3], buffer.buffer[j], buffer.buffer[j + 1], gradientColor.getStartColor(), gradientColor.getEndColor(), TileMode.MIRROR));
                }

                if (dataSet.getGradientColors() != null) {
                    this.mRenderPaint.setShader(new LinearGradient(buffer.buffer[j], buffer.buffer[j + 3], buffer.buffer[j], buffer.buffer[j + 1], dataSet.getGradientColor(j / 4).getStartColor(), dataSet.getGradientColor(j / 4).getEndColor(), TileMode.MIRROR));
                }
                if (isSingleColor) {
                    BarEntry e = (BarEntry) dataSet.getEntryForIndex(j / 4);

                    {//修改::用了Fill来实现圆形背景，
                        Fill[] fills = (Fill[]) e.getData();
                        Fill fill = null;
//                    Log.d(TAG, "drawValues: trans !isStacked highLightX  :: " + highLightX);
                        BarEntry tempHighLightX = dataSet.getEntryForXValue(highLightX, e.getY(), DataSet.Rounding.CLOSEST);
                        if (tempHighLightX == null) continue;
                        if (fills != null) {
                            fill = (e.getX() == tempHighLightX.getX()) ? fills[0] : fills[1];
                        }
                        if (fill != null) {
                            final boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());
                            fill.fillRect(
                                    c, mRenderPaint,
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    buffer.buffer[j + 2],
                                    buffer.buffer[j + 3],
                                    isInverted ? Fill.Direction.DOWN : Fill.Direction.UP);
                        }
                    }
                } else {
                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3], this.mRenderPaint);
                    if (drawBorder) {
                        c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3], this.mBarBorderPaint);
                    }
                }


            }
        }
        drawPointer(c);
    }

    protected void prepareBarHighlight(float x, float y1, float y2, float barWidthHalf, Transformer trans) {
        float left = x - barWidthHalf;
        float right = x + barWidthHalf;
        this.mBarRect.set(left, y1, right, y2);
        trans.rectToPixelPhase(this.mBarRect, this.mAnimator.getPhaseY());
    }

    public void drawValues(Canvas c) {
//        Log.d("TAG", "drawValues: ");
        if (this.isDrawingValuesAllowed(this.mChart)) {
            List<IBarDataSet> dataSets = this.mChart.getBarData().getDataSets();
            float valueOffsetPlus = Utils.convertDpToPixel(4.5F);
            float posOffset = 0.0F;
            float negOffset = 0.0F;
            boolean drawValueAboveBar = this.mChart.isDrawValueAboveBarEnabled();

            for (int i = 0; i < this.mChart.getBarData().getDataSetCount(); ++i) {
                IBarDataSet dataSet = (IBarDataSet) dataSets.get(i);
                if (this.shouldDrawValues(dataSet)) {
                    this.applyValueTextStyle(dataSet);
                    boolean isInverted = this.mChart.isInverted(dataSet.getAxisDependency());
                    float valueTextHeight = (float) Utils.calcTextHeight(this.mValuePaint, "8");
                    posOffset = drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus;
                    negOffset = drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus;
                    if (isInverted) {
                        posOffset = -posOffset - valueTextHeight;
                        negOffset = -negOffset - valueTextHeight;
                    }

                    BarBuffer buffer = this.mBarBuffers[i];
                    float phaseY = this.mAnimator.getPhaseY();
                    ValueFormatter formatter = dataSet.getValueFormatter();
                    MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                    iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                    iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);
                    float x;
                    if (!dataSet.isStacked()) {
                        for (int j = 0; (float) j < (float) buffer.buffer.length * this.mAnimator.getPhaseX(); j += 4) {
                            float x1 = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2.0F;
                            if (!this.mViewPortHandler.isInBoundsRight(x1)) {
                                break;
                            }

                            if (this.mViewPortHandler.isInBoundsY(buffer.buffer[j + 1]) && this.mViewPortHandler.isInBoundsLeft(x1)) {
                                BarEntry entry = (BarEntry) dataSet.getEntryForIndex(j / 4);
                                float val = entry.getY();
                                if (dataSet.isDrawValuesEnabled()) {
                                    this.drawValue(c, formatter.getBarLabel(entry), x1, val >= 0.0F ? buffer.buffer[j + 1] + posOffset : buffer.buffer[j + 3] + negOffset, dataSet.getValueTextColor(j / 4));
                                }

                                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                                    Drawable icon = entry.getIcon();
                                    float py = val >= 0.0F ? buffer.buffer[j + 1] + posOffset : buffer.buffer[j + 3] + negOffset;
                                    x = x1 + iconsOffset.x;
                                    py += iconsOffset.y;
                                    Utils.drawImage(c, icon, (int) x, (int) py, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                                }
                            }
                        }
                    } else {
                        Transformer trans = this.mChart.getTransformer(dataSet.getAxisDependency());
                        int bufferIndex = 0;
                        int index = 0;

                        label181:
                        while (true) {
                            float[] vals;
                            label179:
                            while (true) {
                                if ((float) index >= (float) dataSet.getEntryCount() * this.mAnimator.getPhaseX()) {
                                    break label181;
                                }

                                BarEntry entry = (BarEntry) dataSet.getEntryForIndex(index);
                                vals = entry.getYVals();
                                x = (buffer.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2.0F;
                                int color = dataSet.getValueTextColor(index);
                                float px;
                                float py;
                                if (vals == null) {
                                    if (!this.mViewPortHandler.isInBoundsRight(x)) {
                                        break label181;
                                    }

                                    if (!this.mViewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1]) || !this.mViewPortHandler.isInBoundsLeft(x)) {
                                        continue;
                                    }

                                    if (dataSet.isDrawValuesEnabled()) {
                                        this.drawValue(c, formatter.getBarLabel(entry), x, buffer.buffer[bufferIndex + 1] + (entry.getY() >= 0.0F ? posOffset : negOffset), color);
                                    }

                                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                                        Drawable icon = entry.getIcon();
                                        py = buffer.buffer[bufferIndex + 1] + (entry.getY() >= 0.0F ? posOffset : negOffset);
                                        px = x + iconsOffset.x;
                                        py += iconsOffset.y;
                                        Utils.drawImage(c, icon, (int) px, (int) py, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                                    }
                                    break;
                                }

                                float[] transformed = new float[vals.length * 2];
                                px = 0.0F;
                                py = -entry.getNegativeSum();
                                int k = 0;

                                float y;
                                for (int idx = 0; k < transformed.length; ++idx) {
                                    float value = vals[idx];
                                    if (value == 0.0F && (px == 0.0F || py == 0.0F)) {
                                        y = value;
                                    } else if (value >= 0.0F) {
                                        px += value;
                                        y = px;
                                    } else {
                                        y = py;
                                        py -= value;
                                    }

                                    transformed[k + 1] = y * phaseY;
                                    k += 2;
                                }

                                trans.pointValuesToPixel(transformed);
                                k = 0;

                                while (true) {
                                    if (k >= transformed.length) {
                                        break label179;
                                    }

                                    float val = vals[k / 2];
                                    boolean drawBelow = val == 0.0F && py == 0.0F && px > 0.0F || val < 0.0F;
                                    y = transformed[k + 1] + (drawBelow ? negOffset : posOffset);
                                    if (!this.mViewPortHandler.isInBoundsRight(x)) {
                                        break label179;
                                    }

                                    if (this.mViewPortHandler.isInBoundsY(y) && this.mViewPortHandler.isInBoundsLeft(x)) {
                                        if (dataSet.isDrawValuesEnabled()) {
                                            this.drawValue(c, formatter.getBarStackedLabel(val, entry), x, y, color);
                                        }

                                        if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                                            Drawable icon = entry.getIcon();
                                            Utils.drawImage(c, icon, (int) (x + iconsOffset.x), (int) (y + iconsOffset.y), icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                                        }
                                    }

                                    k += 2;
                                }
                            }

                            bufferIndex = vals == null ? bufferIndex + 4 : bufferIndex + 4 * vals.length;
                            ++index;
                        }
                    }
                    MPPointF.recycleInstance(iconsOffset);
                }
            }
        }
    }

    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        this.mValuePaint.setColor(color);
        c.drawText(valueText, x, y, this.mValuePaint);
    }

    public void drawHighlighted(Canvas c, Highlight[] indices) {
        BarData barData = this.mChart.getBarData();
        Highlight[] var4 = indices;
        int var5 = indices.length;
//        Log.d(TAG, "drawHighlighted: " + highLightX);
//        Log.d(TAG, "drawHighlighted: highLight Len :: " + var5);
        for (int var6 = 0; var6 < var5; ++var6) {
            Highlight high = var4[var6];
            IBarDataSet set = (IBarDataSet) barData.getDataSetByIndex(high.getDataSetIndex());
            if (set != null && set.isHighlightEnabled()) {
                BarEntry e = (BarEntry) set.getEntryForXValue(high.getX(), high.getY());
                if (this.isInBoundsX(e, set)) {
                    Transformer trans = this.mChart.getTransformer(set.getAxisDependency());
                    {/***moveUp放手后靠近高亮*/
                        highLightX = e.getX();
                        float[] zeroLineBuffer = new float[2];
                        zeroLineBuffer[0] = highLightX;
                        zeroLineBuffer[1] = 0f;
                        trans.pointValuesToPixel(zeroLineBuffer);
                        finalX = zeroLineBuffer[0];
//                        Log.d(TAG, "drawHighlighted: finalX :: " + finalX + " highLightX :: " + highLightX);
                        if (pointerX == DEFAULT_POINTER_X && pointerEvent == null) {//初始化的时候，算出高亮位置
                            pointerX = finalX;
                        } else if (isMoveUp) {
//                            Log.d(TAG, "drawDataSet: animator isMoveUp");
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
                    drawPointer(c);
                    this.mHighlightPaint.setColor(set.getHighLightColor());
                    this.mHighlightPaint.setAlpha(set.getHighLightAlpha());
                    boolean isStack = high.getStackIndex() >= 0 && e.isStacked();
                    float y1;
                    float y2;
                    if (isStack) {
                        if (this.mChart.isHighlightFullBarEnabled()) {
                            y1 = e.getPositiveSum();
                            y2 = -e.getNegativeSum();
                        } else {
                            Range range = e.getRanges()[high.getStackIndex()];
                            y1 = range.from;
                            y2 = range.to;
                        }
                    } else {
                        y1 = e.getY();
                        y2 = 0.0F;
                    }

                    this.prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2.0F, trans);
                    this.setHighlightDrawPos(high, this.mBarRect);
//                    c.drawRect(this.mBarRect, this.mHighlightPaint);
                }
            }
        }
    }

    protected void setHighlightDrawPos(Highlight high, RectF bar) {
        high.setDraw(bar.centerX(), bar.top);
    }

    public void drawExtras(Canvas c) {
    }

    MotionEvent pointerEvent;
    final float DEFAULT_POINTER_X = -111f;
    float pointerX = DEFAULT_POINTER_X;
    float pointerY = 0f;
    boolean isMoveUp = false;
    WeakReference<View> viewWeakReference;

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
    int[] colors = {Color.parseColor("#01FFFFFF"), Color.parseColor("#FFFFFF"), Color.parseColor("#01FFFFFF")};
    float[] position = {0f, 0.5f, 1.0f};
    float startX = 0f;//图表的最左y
    float endX = 0f;//图表的最右y
    float finalX = 0f;//抬手后最终停止位置
    float bottomY = 0f;//图表的最低x
    float highLightX = 0f;//当前高亮的x值
    public static final float DEFAULT_HIGH_LIGHT_X = 6f;
    private volatile boolean isDrawedPointer = false;

    private void drawPointer(Canvas canvas) {
        if (isDrawedPointer || pointerX == DEFAULT_POINTER_X) {
            return;
        }
        isDrawedPointer = true;
        Log.d("TAG", "drawPointer: pointerX :: " + pointerX);
        LinearGradient linearGradient = new LinearGradient(0, 0, 5f, (float) (bottomY)
                , colors, position, TileMode.CLAMP);
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
    public float getHighLightXByCurrentPointerPosition() {
        //增加对月视图没有设置高亮时，自动选择游标就近的高亮
        Transformer trans = this.mChart.getTransformer(YAxis.AxisDependency.LEFT);//这个tran的更新没有那么快
        MPPointD mpPointD = trans.getValuesByTouchPoint(pointerX, bottomY);
        IBarDataSet set = this.mChart.getBarData().getDataSetByIndex(0);
        if (set == null || !set.isHighlightEnabled()) return highLightX;
        Log.d(TAG, "getHighLightXByCurrentPointerPosition: entryCount :" + set.getEntryCount());

        Log.d(TAG, "getHighLightXByCurrentPointerPosition: mpPointD :" + mpPointD.x);
        BarEntry e = set.getEntryForXValue((float) mpPointD.x, bottomY);
        highLightX = e.getX();
        return highLightX;
    }

    private volatile boolean isDataChange = false;
    private volatile boolean isFirstRefreshData = true;

    public void notifyDataSetChanged() {
        this.isDataChange = true;
    }
}
