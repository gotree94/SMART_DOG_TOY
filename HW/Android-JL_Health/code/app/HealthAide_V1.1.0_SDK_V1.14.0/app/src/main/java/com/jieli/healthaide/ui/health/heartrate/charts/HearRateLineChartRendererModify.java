package com.jieli.healthaide.ui.health.heartrate.charts;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineRadarRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HearRateLineChartRendererModify extends LineRadarRenderer {
    protected static final int PATH_TYPE_PATH = 1;
    protected static final int PATH_TYPE_CIRCLE = 2;
    protected LineDataProvider mChart;
    private Chart mChartView;

    /**
     * paint for the inner circle of the value indicators
     */
    protected Paint mCirclePaintInner;

    /**
     * Bitmap object used for drawing the paths (otherwise they are too long if
     * rendered directly on the canvas)
     */
    protected WeakReference<Bitmap> mDrawBitmap;

    /**
     * on this canvas, the paths are rendered, it is initialized with the
     * pathBitmap
     */
    protected Canvas mBitmapCanvas;

    /**
     * the bitmap configuration to be used
     */
    protected Bitmap.Config mBitmapConfig = Bitmap.Config.ARGB_8888;

    protected Path cubicPath = new Path();
    protected Path cubicFillPath = new Path();

    public HearRateLineChartRendererModify(LineDataProvider chart, Chart chartView, ChartAnimator animator,
                                           ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
        mChartView = chartView;
        mCirclePaintInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaintInner.setStyle(Paint.Style.FILL);


        mPaintCircle = new Paint();
        mPaintCircle.setStyle(Paint.Style.FILL);

        mPainSelectedCircle = new Paint();
        mPainSelectedCircle.setStyle(Paint.Style.FILL);
    }

    @Override
    public void initBuffers() {
    }

    @Override
    public void drawData(Canvas c) {
        isDrawedPointer = false;
        int width = (int) mViewPortHandler.getChartWidth();
        int height = (int) mViewPortHandler.getChartHeight();

        Bitmap drawBitmap = mDrawBitmap == null ? null : mDrawBitmap.get();

        if (drawBitmap == null
                || (drawBitmap.getWidth() != width)
                || (drawBitmap.getHeight() != height)) {

            if (width > 0 && height > 0) {
                drawBitmap = Bitmap.createBitmap(width, height, mBitmapConfig);
                mDrawBitmap = new WeakReference<>(drawBitmap);
                mBitmapCanvas = new Canvas(drawBitmap);
            } else
                return;
        }

        drawBitmap.eraseColor(Color.TRANSPARENT);

        LineData lineData = mChart.getLineData();

        for (ILineDataSet set : lineData.getDataSets()) {

            if (set.isVisible())
                drawDataSet(c, set);
        }

        c.drawBitmap(drawBitmap, 0, 0, mRenderPaint);
    }

    protected void drawDataSet(Canvas c, ILineDataSet dataSet) {
        {//修改：：动态变化颜色
            mCirclePaintInner.setColor(dataSet.getColor());
            mPaintCircle.setColor(dataSet.getColor());
            mPainSelectedCircle.setColor(dataSet.getCircleHoleColor());
        }

        if (dataSet.getEntryCount() < 1)
            return;

        mRenderPaint.setStrokeWidth(dataSet.getLineWidth());
        mRenderPaint.setPathEffect(dataSet.getDashPathEffect());
        {/**增加*/
            LineData lineData = mChart.getLineData();
            Highlight[] indices = mChartView.getHighlighted();
            if (indices != null) {
                for (Highlight high : indices) {

                    ILineDataSet set = lineData.getDataSetByIndex(high.getDataSetIndex());

                    if (set == null || !set.isHighlightEnabled())
                        continue;

                    Entry e = set.getEntryForXValue(high.getX(), high.getY());

                    if (!isInBoundsX(e, set))
                        continue;
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

        switch (dataSet.getMode()) {
            default:
            case LINEAR:
            case STEPPED:
                drawLinear(c, dataSet);
                break;

            case CUBIC_BEZIER:
                drawCubicBezier(dataSet);
                break;

            case HORIZONTAL_BEZIER:
                drawHorizontalBezier(dataSet);
                break;
        }
        drawPointer(c);
        mRenderPaint.setPathEffect(null);
    }

    protected void drawHorizontalBezier(ILineDataSet dataSet) {

        float phaseY = mAnimator.getPhaseY();

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mXBounds.set(mChart, dataSet);

        cubicPath.reset();

        if (mXBounds.range >= 1) {

            Entry prev = dataSet.getEntryForIndex(mXBounds.min);
            Entry cur = prev;

            // let the spline start
            cubicPath.moveTo(cur.getX(), cur.getY() * phaseY);

            for (int j = mXBounds.min + 1; j <= mXBounds.range + mXBounds.min; j++) {

                prev = cur;
                cur = dataSet.getEntryForIndex(j);

                final float cpx = (prev.getX())
                        + (cur.getX() - prev.getX()) / 2.0f;

                cubicPath.cubicTo(
                        cpx, prev.getY() * phaseY,
                        cpx, cur.getY() * phaseY,
                        cur.getX(), cur.getY() * phaseY);
            }
        }

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled()) {

            cubicFillPath.reset();
            cubicFillPath.addPath(cubicPath);
            // create a new path, this is bad for performance
            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds);
        }

        mRenderPaint.setColor(dataSet.getColor());

        mRenderPaint.setStyle(Paint.Style.STROKE);

        trans.pathValueToPixel(cubicPath);

        mBitmapCanvas.drawPath(cubicPath, mRenderPaint);

        mRenderPaint.setPathEffect(null);
    }

    protected void drawCubicBezier(ILineDataSet dataSet) {

        float phaseY = mAnimator.getPhaseY();

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mXBounds.set(mChart, dataSet);

        float intensity = dataSet.getCubicIntensity();

        cubicPath.reset();

        if (mXBounds.range >= 1) {

            float prevDx = 0f;
            float prevDy = 0f;
            float curDx = 0f;
            float curDy = 0f;

            // Take an extra point from the left, and an extra from the right.
            // That's because we need 4 points for a cubic bezier (cubic=4), otherwise we get lines moving and doing weird stuff on the edges of the chart.
            // So in the starting `prev` and `cur`, go -2, -1
            // And in the `lastIndex`, add +1

            final int firstIndex = mXBounds.min + 1;
            final int lastIndex = mXBounds.min + mXBounds.range;

            Entry prevPrev;
            Entry prev = dataSet.getEntryForIndex(Math.max(firstIndex - 2, 0));
            Entry cur = dataSet.getEntryForIndex(Math.max(firstIndex - 1, 0));
            Entry next = cur;
            int nextIndex = -1;

            if (cur == null) return;

            // let the spline start
            cubicPath.moveTo(cur.getX(), cur.getY() * phaseY);

            for (int j = mXBounds.min + 1; j <= mXBounds.range + mXBounds.min; j++) {

                prevPrev = prev;
                prev = cur;
                cur = nextIndex == j ? next : dataSet.getEntryForIndex(j);

                nextIndex = j + 1 < dataSet.getEntryCount() ? j + 1 : j;
                next = dataSet.getEntryForIndex(nextIndex);

                prevDx = (cur.getX() - prevPrev.getX()) * intensity;
                prevDy = (cur.getY() - prevPrev.getY()) * intensity;
                curDx = (next.getX() - prev.getX()) * intensity;
                curDy = (next.getY() - prev.getY()) * intensity;

                cubicPath.cubicTo(prev.getX() + prevDx, (prev.getY() + prevDy) * phaseY,
                        cur.getX() - curDx,
                        (cur.getY() - curDy) * phaseY, cur.getX(), cur.getY() * phaseY);
            }
        }

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled()) {

            cubicFillPath.reset();
            cubicFillPath.addPath(cubicPath);

            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds);
        }

        mRenderPaint.setColor(dataSet.getColor());

        mRenderPaint.setStyle(Paint.Style.STROKE);

        trans.pathValueToPixel(cubicPath);

        mBitmapCanvas.drawPath(cubicPath, mRenderPaint);

        mRenderPaint.setPathEffect(null);
    }

    protected void drawCubicFill(Canvas c, ILineDataSet dataSet, Path spline, Transformer trans, XBounds bounds) {

        float fillMin = dataSet.getFillFormatter()
                .getFillLinePosition(dataSet, mChart);

        spline.lineTo(dataSet.getEntryForIndex(bounds.min + bounds.range).getX(), fillMin);
        spline.lineTo(dataSet.getEntryForIndex(bounds.min).getX(), fillMin);
        spline.close();

        trans.pathValueToPixel(spline);

        final Drawable drawable = dataSet.getFillDrawable();
        if (drawable != null) {

            drawFilledPath(c, spline, drawable);
        } else {

            drawFilledPath(c, spline, dataSet.getFillColor(), dataSet.getFillAlpha());
        }
    }

    private float[] mLineBuffer = new float[4];

    /**
     * Draws a normal line.
     *
     * @param c
     * @param dataSet
     */
    protected void drawLinear(Canvas c, ILineDataSet dataSet) {

        int entryCount = dataSet.getEntryCount();

        final boolean isDrawSteppedEnabled = dataSet.isDrawSteppedEnabled();
        final int pointsPerEntryPair = isDrawSteppedEnabled ? 4 : 2;

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        float phaseY = mAnimator.getPhaseY();

        mRenderPaint.setStyle(Paint.Style.STROKE);

        Canvas canvas = null;

        // if the data-set is dashed, draw on bitmap-canvas
        if (dataSet.isDashedLineEnabled()) {
            canvas = mBitmapCanvas;
        } else {
            canvas = c;
        }

        mXBounds.set(mChart, dataSet);

        // if drawing filled is enabled
        if (dataSet.isDrawFilledEnabled() && entryCount > 0) {
//            drawLinearFill(c, dataSet, trans, mXBounds);
            drawLinearFillModify(c, dataSet, trans, mXBounds);
        }

        // more than 1 color
        if (dataSet.getColors().size() > 1) {

            int numberOfFloats = pointsPerEntryPair * 2;

            if (mLineBuffer.length <= numberOfFloats)
                mLineBuffer = new float[numberOfFloats * 2];

            int max = mXBounds.min + mXBounds.range;

            for (int j = mXBounds.min; j < max; j++) {
//                Log.d(TAG, "drawLinear: range :: " + (mXBounds.range + mXBounds.min) + " j :: " + j);
                Entry e = dataSet.getEntryForIndex(j);
                if (e == null) continue;

                mLineBuffer[0] = e.getX();
                mLineBuffer[1] = e.getY() * phaseY;

                if (j < mXBounds.max) {

                    e = dataSet.getEntryForIndex(j + 1);

                    if (e == null) break;

                    if (isDrawSteppedEnabled) {
                        mLineBuffer[2] = e.getX();
                        mLineBuffer[3] = mLineBuffer[1];
                        mLineBuffer[4] = mLineBuffer[2];
                        mLineBuffer[5] = mLineBuffer[3];
                        mLineBuffer[6] = e.getX();
                        mLineBuffer[7] = e.getY() * phaseY;
                    } else {
                        mLineBuffer[2] = e.getX();
                        mLineBuffer[3] = e.getY() * phaseY;
                    }

                } else {
                    mLineBuffer[2] = mLineBuffer[0];
                    mLineBuffer[3] = mLineBuffer[1];
                }

                // Determine the start and end coordinates of the line, and make sure they differ.
                float firstCoordinateX = mLineBuffer[0];
                float firstCoordinateY = mLineBuffer[1];
                float lastCoordinateX = mLineBuffer[numberOfFloats - 2];
                float lastCoordinateY = mLineBuffer[numberOfFloats - 1];

                if (firstCoordinateX == lastCoordinateX &&
                        firstCoordinateY == lastCoordinateY)
                    continue;

                trans.pointValuesToPixel(mLineBuffer);

                if (!mViewPortHandler.isInBoundsRight(firstCoordinateX))
                    break;

                // make sure the lines don't do shitty things outside
                // bounds
                if (!mViewPortHandler.isInBoundsLeft(lastCoordinateX) ||
                        !mViewPortHandler.isInBoundsTop(Math.max(firstCoordinateY, lastCoordinateY)) ||
                        !mViewPortHandler.isInBoundsBottom(Math.min(firstCoordinateY, lastCoordinateY)))
                    continue;

                // get the color that is set for this line-segment
                mRenderPaint.setColor(dataSet.getColor(j));

                canvas.drawLines(mLineBuffer, 0, pointsPerEntryPair * 2, mRenderPaint);
            }

        } else { // only one color per dataset

            if (mLineBuffer.length < Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 2)
                mLineBuffer = new float[Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 4];

            Entry e1, e2, e3;
            Entry highLightEntry = null;

            e1 = dataSet.getEntryForIndex(mXBounds.min);

            if (e1 != null) {
                ArrayList<PathData> pathDataArrayList = new ArrayList<>();
                ArrayList<Float> floats = null;
                int j = 0;
                ArrayList<Entry> noEmptyDataArray = new ArrayList<>();
                for (int x = mXBounds.min; x <= mXBounds.range + mXBounds.min; x++) {
//                    Log.d(TAG, "drawLinear:noEmptyDataArray x::  " + x);
                    e1 = dataSet.getEntryForIndex(x);
                    if (e1.getY() != Y_DEFAULT_EMPTY) {
                        noEmptyDataArray.add(e1);
                    }
                }
                {//修改:: 高亮判断
                    Entry highLightE1 = null;
                    Entry highLightE2 = null;
                    for (int i = 0; i < noEmptyDataArray.size(); i++) {
                        Entry e = (Entry) noEmptyDataArray.get(i);
                        if (e.getY() == 0f) continue;
                        boolean isHighLight = currentPointerX - judgeDataStopIntervalX < e.getX() && e.getX() < currentPointerX + judgeDataStopIntervalX;
                        if (!isHighLight) continue;
                        if (highLightE1 == null) {
                            highLightE1 = e;
                            continue;
                        }
                        if (highLightE2 == null) {
                            highLightE2 = e;
                        }
                    }
                    if (highLightE2 != null) {
                        float valueE1 = currentPointerX - highLightE1.getX();
                        float valueE2 = highLightE2.getX() - currentPointerX;
                        highLightEntry = Math.min(valueE1, valueE2) == valueE1 ? highLightE1 : highLightE2;
                        highLightX = highLightEntry.getX();
                    } else {
                        if (highLightE1 != null) {
                            highLightX = highLightE1.getX();
                            highLightEntry = highLightE1;
                        }
                    }
                    if (renderCallback != null) {
                        renderCallback.onHighLightX(highLightEntry);
                    }
                }
                for (int x = 0; x < noEmptyDataArray.size(); x++) {
                    Log.d(TAG, "drawLinear: range :: " + (mXBounds.range + mXBounds.min) + " j :: " + j);

                    e1 = noEmptyDataArray.get(x == 0 ? 0 : (x - 1));
                    e2 = noEmptyDataArray.get(x);
                    e3 = (x == noEmptyDataArray.size() - 1) ? null : noEmptyDataArray.get(x + 1);

                    if (e1 == null || e2 == null) continue;
                    if (x == 0 || (e2.getX() - e1.getX()) > judgeDataStopIntervalX) {//距离前一个大于12
                        if (e3 == null || (e3.getX() - e2.getX()) > judgeDataStopIntervalX) {//距离后一个也是大于21，说明是单独的一个点
                            floats = new ArrayList<>();
                            floats.add(e2.getX());
                            floats.add(e2.getY() * phaseY);
                            floats.add(e2.getX());
                            floats.add(e2.getY() * phaseY);
                            pathDataArrayList.add(new PathData(PathData.PATH_TYPE_CIRCLE, floats));
                        } else {//新起一个path
                            floats = new ArrayList<>();
                            floats.add(e2.getX());
                            floats.add(e2.getY() * phaseY);
                            floats.add(e2.getX());
                            floats.add(e2.getY() * phaseY);
                            pathDataArrayList.add(new PathData(PathData.PATH_TYPE_PATH, floats));
                        }
                    } else {//正常添加
                        floats.add(e1.getX());
                        floats.add(e1.getY() * phaseY);
                        floats.add(e2.getX());
                        floats.add(e2.getY() * phaseY);
                    }
                }

                for (PathData pathData : pathDataArrayList) {
                    ArrayList<Float> data = pathData.getFloats();
                    if (pathData.getPathType() == PATH_TYPE_PATH) {
                        mLineBuffer = new float[data.size()];
                        for (int i = 0; i < data.size(); i++) {
                            mLineBuffer[i] = data.get(i);
                        }
                        trans.pointValuesToPixel(mLineBuffer);
                        mRenderPaint.setColor(dataSet.getColor());
                        canvas.drawLines(mLineBuffer, 0, data.size(), mRenderPaint);
                    } else if (pathData.getPathType() == PATH_TYPE_CIRCLE) {
                        mLineBuffer = new float[data.size()];
                        for (int i = 0; i < data.size(); i++) {
                            mLineBuffer[i] = data.get(i);
                        }
                        trans.pointValuesToPixel(mLineBuffer);
                        mRenderPaint.setColor(dataSet.getColor());
                        canvas.drawCircle(mLineBuffer[0], mLineBuffer[1], circleRadius, mPaintCircle);
                    }
                }
                HeartRateLineDataSet heartRateLineDataSet = (HeartRateLineDataSet) dataSet;
                if (highLightEntry != null && heartRateLineDataSet.isDrawSelectedCircleEnable() && (pointerX != DEFAULT_POINTER_X)) {
                    Entry e = highLightEntry;
                    MPPointD pix = mChart.getTransformer(dataSet.getAxisDependency()).getPixelForValues(e.getX(), e.getY() * mAnimator
                            .getPhaseY());
                    /* pointerX = (float) pix.x;*/
                    c.drawCircle((float) pix.x, (float) pix.y, 9f, mPainSelectedCircle);
                }
            }
        }
        mRenderPaint.setPathEffect(null);
    }

    protected Path mGenerateFilledPathBuffer = new Path();

    /**
     * Draws a filled linear path on the canvas.
     *
     * @param c
     * @param dataSet
     * @param trans
     * @param bounds
     */
    protected void drawLinearFill(Canvas c, ILineDataSet dataSet, Transformer trans, XBounds bounds) {

        final Path filled = mGenerateFilledPathBuffer;

        final int startingIndex = bounds.min;
        final int endingIndex = bounds.range + bounds.min;
        final int indexInterval = 128;

        int currentStartIndex = 0;
        int currentEndIndex = indexInterval;
        int iterations = 0;

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
        do {
            currentStartIndex = startingIndex + (iterations * indexInterval);
            currentEndIndex = currentStartIndex + indexInterval;
            currentEndIndex = currentEndIndex > endingIndex ? endingIndex : currentEndIndex;

            if (currentStartIndex <= currentEndIndex) {
                generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled);

                trans.pathValueToPixel(filled);

                final Drawable drawable = dataSet.getFillDrawable();
                if (drawable != null) {

                    drawFilledPath(c, filled, drawable);
                } else {

                    drawFilledPath(c, filled, dataSet.getFillColor(), dataSet.getFillAlpha());
                }
            }

            iterations++;

        } while (currentStartIndex <= currentEndIndex);

    }

    /**
     * Generates a path that is used for filled drawing.
     *
     * @param dataSet    The dataset from which to read the entries.
     * @param startIndex The index from which to start reading the dataset
     * @param endIndex   The index from which to stop reading the dataset
     * @param outputPath The path object that will be assigned the chart data.
     * @return
     */
    private void generateFilledPath(final ILineDataSet dataSet, final int startIndex, final int endIndex, final Path outputPath) {

        final float fillMin = dataSet.getFillFormatter().getFillLinePosition(dataSet, mChart);
        final float phaseY = mAnimator.getPhaseY();
        final boolean isDrawSteppedEnabled = dataSet.getMode() == LineDataSet.Mode.STEPPED;

        final Path filled = outputPath;
        filled.reset();

        final Entry entry = dataSet.getEntryForIndex(startIndex);

        filled.moveTo(entry.getX(), fillMin);
        filled.lineTo(entry.getX(), entry.getY() * phaseY);

        // create a new path
        Entry currentEntry = null;
        Entry previousEntry = entry;
        for (int x = startIndex + 1; x <= endIndex; x++) {

            currentEntry = dataSet.getEntryForIndex(x);

            if (isDrawSteppedEnabled) {
                filled.lineTo(currentEntry.getX(), previousEntry.getY() * phaseY);
            }

            filled.lineTo(currentEntry.getX(), currentEntry.getY() * phaseY);

            previousEntry = currentEntry;
        }

        // close up
        if (currentEntry != null) {
            filled.lineTo(currentEntry.getX(), fillMin);
        }

        filled.close();
    }

    @Override
    public void drawValues(Canvas c) {

        if (isDrawingValuesAllowed(mChart)) {

            List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {

                ILineDataSet dataSet = dataSets.get(i);

                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                // make sure the values do not interfear with the circles
                int valOffset = (int) (dataSet.getCircleRadius() * 1.75f);

                if (!dataSet.isDrawCirclesEnabled())
                    valOffset = valOffset / 2;

                mXBounds.set(mChart, dataSet);

                float[] positions = trans.generateTransformedValuesLine(dataSet, mAnimator.getPhaseX(), mAnimator
                        .getPhaseY(), mXBounds.min, mXBounds.max);

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

                    Entry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);

                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c, dataSet.getValueFormatter(), entry.getY(), entry, i, x,
                                y - valOffset, dataSet.getValueTextColor(j / 2));
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

    public void drawValue(Canvas c, IValueFormatter formatter, float value, Entry entry, int dataSetIndex, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(formatter.getFormattedValue(value, entry, dataSetIndex, mViewPortHandler), x, y, mValuePaint);
    }

    @Override
    public void drawValue(Canvas canvas, String s, float v, float v1, int i) {

    }

    @Override
    public void drawExtras(Canvas c) {
        drawCircles(c);
        {//静息心率的ValueLable
            LineData lineData = mChart.getLineData();
            ILineDataSet dataSet = lineData.getDataSetByIndex(0);
            if (dataSet != null) {
                HeartRateLineDataSet heartRateLineDataSet = (HeartRateLineDataSet) dataSet;
                int restingRate = heartRateLineDataSet.getRestingRate();
                if (restingRate == 0) return;
                Paint p = new Paint();
                float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, mChartView.getResources().getDisplayMetrics());
                p.setTextSize(textSize);
                p.setColor(Color.parseColor("#a3d07d"));
                String restingText = String.valueOf(restingRate);
                MPPointD pix = mChart.getTransformer(dataSet.getAxisDependency()).getPixelForValues(mChart.getLowestVisibleX(),restingRate);
                float xPos;
                BarLineChartBase barLineChartBase = (BarLineChartBase) mChartView;
                float xoffset = barLineChartBase.getAxisLeft().getXOffset();
                xPos = mViewPortHandler.contentRight() + xoffset;
                float yoffset = Utils.calcTextHeight(p, "A") / 2.5f + barLineChartBase.getAxisLeft().getYOffset();
                c.drawText(restingText, xPos, (float) (pix.y + yoffset), p);
            }
        }
    }

    /**
     * cache for the circle bitmaps of all datasets
     */
    private HashMap<IDataSet, DataSetImageCache> mImageCaches = new HashMap<>();

    /**
     * buffer for drawing the circles
     */
    private float[] mCirclesBuffer = new float[2];

    protected void drawCircles(Canvas c) {

        mRenderPaint.setStyle(Paint.Style.FILL);

        float phaseY = mAnimator.getPhaseY();

        mCirclesBuffer[0] = 0;
        mCirclesBuffer[1] = 0;

        List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

        for (int i = 0; i < dataSets.size(); i++) {

            ILineDataSet dataSet = dataSets.get(i);

            if (!dataSet.isVisible() || !dataSet.isDrawCirclesEnabled() ||
                    dataSet.getEntryCount() == 0)
                continue;

            mCirclePaintInner.setColor(dataSet.getCircleHoleColor());

            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mXBounds.set(mChart, dataSet);

            float circleRadius = dataSet.getCircleRadius();
            float circleHoleRadius = dataSet.getCircleHoleRadius();
            boolean drawCircleHole = dataSet.isDrawCircleHoleEnabled() &&
                    circleHoleRadius < circleRadius &&
                    circleHoleRadius > 0.f;
            boolean drawTransparentCircleHole = drawCircleHole &&
                    dataSet.getCircleHoleColor() == ColorTemplate.COLOR_NONE;

            DataSetImageCache imageCache;

            if (mImageCaches.containsKey(dataSet)) {
                imageCache = mImageCaches.get(dataSet);
            } else {
                imageCache = new DataSetImageCache();
                mImageCaches.put(dataSet, imageCache);
            }

            boolean changeRequired = imageCache.init(dataSet);

            // only fill the cache with new bitmaps if a change is required
            if (changeRequired) {
                imageCache.fill(dataSet, drawCircleHole, drawTransparentCircleHole);
            }

            int boundsRangeCount = mXBounds.range + mXBounds.min;

            for (int j = mXBounds.min; j <= boundsRangeCount; j++) {

                Entry e = dataSet.getEntryForIndex(j);

                if (e == null) break;

                mCirclesBuffer[0] = e.getX();
                mCirclesBuffer[1] = e.getY() * phaseY;

                trans.pointValuesToPixel(mCirclesBuffer);

                if (!mViewPortHandler.isInBoundsRight(mCirclesBuffer[0]))
                    break;

                if (!mViewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) ||
                        !mViewPortHandler.isInBoundsY(mCirclesBuffer[1]))
                    continue;

                Bitmap circleBitmap = imageCache.getBitmap(j);

                if (circleBitmap != null) {
                    c.drawBitmap(circleBitmap, mCirclesBuffer[0] - circleRadius, mCirclesBuffer[1] - circleRadius, null);
                }
            }
        }
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        LineData lineData = mChart.getLineData();
        Log.d(TAG, "drawHighlighted: " + highLightX);
        Log.d(TAG, "drawHighlighted: highLight Len :: " + indices.length);
        for (Highlight high : indices) {

            ILineDataSet set = lineData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            Entry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;
            Transformer trans = this.mChart.getTransformer(set.getAxisDependency());
            {/***moveUp放手后靠近高亮*/
                highLightX = e.getX();
                float[] zeroLineBuffer = new float[2];
                zeroLineBuffer[0] = highLightX;
                zeroLineBuffer[1] = 0f;
                trans.pointValuesToPixel(zeroLineBuffer);
                finalX = zeroLineBuffer[0];
                Log.d(TAG, "drawHighlighted: finalX :: " + finalX + " highLightX :: " + highLightX);
                if (pointerX == DEFAULT_POINTER_X && pointerEvent == null) {//初始化的时候，算出高亮位置
                    pointerX = finalX;
                } else if (isMoveUp) {
                    Log.d(TAG, "drawDataSet: animator isMoveUp");
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
            MPPointD pix = mChart.getTransformer(set.getAxisDependency()).getPixelForValues(e.getX(), e.getY() * mAnimator
                    .getPhaseY());

            high.setDraw((float) pix.x, (float) pix.y);

            // draw the lines
//            drawHighlightLines(c, (float) pix.x, (float) pix.y, set);
            HeartRateLineDataSet heartRateLineDataSet = (HeartRateLineDataSet) set;
            if (e.getY() != Y_DEFAULT_EMPTY && heartRateLineDataSet.isDrawSelectedCircleEnable() && isFirstDraw) {
                c.drawCircle((float) pix.x, (float) pix.y, 9f, mPainSelectedCircle);
                if (renderCallback != null) {
                    renderCallback.onHighLightX(e);
                }
            }
        }
    }

    /**
     * Sets the Bitmap.Config to be used by this renderer.
     * Default: Bitmap.Config.ARGB_8888
     * Use Bitmap.Config.ARGB_4444 to consume less memory.
     *
     * @param config
     */
    public void setBitmapConfig(Bitmap.Config config) {
        mBitmapConfig = config;
        releaseBitmap();
    }

    /**
     * Returns the Bitmap.Config that is used by this renderer.
     *
     * @return
     */
    public Bitmap.Config getBitmapConfig() {
        return mBitmapConfig;
    }

    /**
     * Releases the drawing bitmap. This should be called when {@link LineChart#onDetachedFromWindow()}.
     */
    public void releaseBitmap() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas.setBitmap(null);
            mBitmapCanvas = null;
        }
        if (mDrawBitmap != null) {
            Bitmap drawBitmap = mDrawBitmap.get();
            if (drawBitmap != null) {
                drawBitmap.recycle();
            }
            mDrawBitmap.clear();
            mDrawBitmap = null;
        }
    }

    private class DataSetImageCache {

        private Path mCirclePathBuffer = new Path();

        private Bitmap[] circleBitmaps;

        /**
         * Sets up the cache, returns true if a change of cache was required.
         *
         * @param set
         * @return
         */
        protected boolean init(ILineDataSet set) {

            int size = set.getCircleColorCount();
            boolean changeRequired = false;

            if (circleBitmaps == null) {
                circleBitmaps = new Bitmap[size];
                changeRequired = true;
            } else if (circleBitmaps.length != size) {
                circleBitmaps = new Bitmap[size];
                changeRequired = true;
            }

            return changeRequired;
        }

        /**
         * Fills the cache with bitmaps for the given dataset.
         *
         * @param set
         * @param drawCircleHole
         * @param drawTransparentCircleHole
         */
        protected void fill(ILineDataSet set, boolean drawCircleHole, boolean drawTransparentCircleHole) {

            int colorCount = set.getCircleColorCount();
            float circleRadius = set.getCircleRadius();
            float circleHoleRadius = set.getCircleHoleRadius();

            for (int i = 0; i < colorCount; i++) {

                Bitmap.Config conf = Bitmap.Config.ARGB_4444;
                Bitmap circleBitmap = Bitmap.createBitmap((int) (circleRadius * 2.1), (int) (circleRadius * 2.1), conf);

                Canvas canvas = new Canvas(circleBitmap);
                circleBitmaps[i] = circleBitmap;
                mRenderPaint.setColor(set.getCircleColor(i));

                if (drawTransparentCircleHole) {
                    // Begin path for circle with hole
                    mCirclePathBuffer.reset();

                    mCirclePathBuffer.addCircle(
                            circleRadius,
                            circleRadius,
                            circleRadius,
                            Path.Direction.CW);

                    // Cut hole in path
                    mCirclePathBuffer.addCircle(
                            circleRadius,
                            circleRadius,
                            circleHoleRadius,
                            Path.Direction.CCW);

                    // Fill in-between
                    canvas.drawPath(mCirclePathBuffer, mRenderPaint);
                } else {

                    canvas.drawCircle(
                            circleRadius,
                            circleRadius,
                            circleRadius,
                            mRenderPaint);

                    if (drawCircleHole) {
                        canvas.drawCircle(
                                circleRadius,
                                circleRadius,
                                circleHoleRadius,
                                mCirclePaintInner);
                    }
                }
            }
        }

        /**
         * Returns the cached Bitmap at the given index.
         *
         * @param index
         * @return
         */
        protected Bitmap getBitmap(int index) {
            return circleBitmaps[index % circleBitmaps.length];
        }
    }

    private final String TAG = this.getClass().getSimpleName();
    MotionEvent pointerEvent;
    final float DEFAULT_POINTER_X = -111f;
    float pointerX = DEFAULT_POINTER_X;
    float pointerY = 0f;
    boolean isMoveUp = false;
    WeakReference<View> viewWeakReference;

    public void setPointerPosition(View view, MotionEvent event) {
        Log.d(TAG, "setPointerPosition: ");
        isFirstDraw = false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                pointerX = Math.max(event.getX(), startX);//todo 考虑超出图表范围
                pointerX = Math.min(pointerX, endX);//todo 考虑超出图表范围
                pointerY = event.getY() < 0 ? 0 : event.getY();//todo 考虑超出图表范围
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "setPointerPosition: ACTION_UP");
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
    float currentPointerX = 0f;
    public static final float DEFAULT_HIGH_LIGHT_X = 6f;
    private Paint mPaintCircle;
    private Paint mPainSelectedCircle;
    private RenderCallback renderCallback;
    private boolean isFirstDraw = true;//此处为处理多数据时的高亮问题，当点击之后就认为
    private volatile boolean isDrawedPointer = false;

    private void drawPointer(Canvas canvas) {
        if (isDrawedPointer || pointerX == DEFAULT_POINTER_X) {
            return;
        }
        isDrawedPointer = true;
        Log.d(TAG, "drawPointer: pointerX :: " + pointerX);
        LinearGradient linearGradient = new LinearGradient(0, 0, 5f, (float) (bottomY)
                , colors, position, Shader.TileMode.CLAMP);
        pointerPaint.setShader(linearGradient);
        pointerPaint.setStrokeWidth(4);
        canvas.drawLine(pointerX, 0, pointerX, (float) (bottomY) * 2, pointerPaint);
        Transformer trans = this.mChart.getTransformer(YAxis.AxisDependency.LEFT);
        MPPointD mpPointD = trans.getValuesByTouchPoint(pointerX, bottomY);
        currentPointerX = (float) mpPointD.x;
        if (renderCallback != null) {
            renderCallback.onCurrentPointerPositionValueX(currentPointerX);
        }
    }

    /**
     * !!! 这个Chart和别的Chart有一点不同，因为要做到，游标移动时在一定范围内，不改变高亮值。具体请看心率的天视图
     * !!! 所以在外部并没有调用这个getHighLightXByCurrentPointerPosition函数
     * <p>
     * 修改X轴的范围后，根据游标位置获取靠近游标的高亮Entry
     * 步骤一:先修改Chart的X轴范围，然后调用chart.invalidate()刷新
     * 步骤二:调用getHighLightXByCurrentPointerPosition()获取对应的高亮值,然后chart.highValue()
     *
     * @return 高亮Entry的x值
     */
    public float getHighLightXByCurrentPointerPosition() {
        Transformer trans = this.mChart.getTransformer(YAxis.AxisDependency.LEFT);
        MPPointD mpPointD = trans.getValuesByTouchPoint(pointerX, bottomY);
        ILineDataSet set = this.mChart.getLineData().getDataSetByIndex(0);
        if (set == null || !set.isHighlightEnabled()) return highLightX;
        Entry e = set.getEntryForXValue((float) mpPointD.x, bottomY);
        highLightX = e.getX();
        return highLightX;
    }

    private int judgeDataStopIntervalX = 12;

    private float circleRadius = 6;
    public static float Y_DEFAULT_EMPTY = 0f;

    protected void drawLinearFillModify(Canvas c, ILineDataSet dataSet, Transformer trans, XBounds mXBounds) {
        final float fillMin = dataSet.getFillFormatter().getFillLinePosition(dataSet, mChart);
        float phaseY = mAnimator.getPhaseY();
        Entry e1, e2, e3;
        Entry lastEntry = null;
        e1 = dataSet.getEntryForIndex(mXBounds.min);

        if (e1 != null) {
            ArrayList<Float> floats = null;
            ArrayList<Path> pathArrayList = new ArrayList<>();
            int j = 0;
            Path currentPath = null;


            float[] zeroLineBuffer = new float[4];

            zeroLineBuffer[0] = mChart.getXChartMin();
            zeroLineBuffer[1] = 0f;
            zeroLineBuffer[2] = mChart.getXChartMax();
            zeroLineBuffer[3] = 0f;
            trans.pointValuesToPixel(zeroLineBuffer);
            startX = zeroLineBuffer[0];
            endX = zeroLineBuffer[2];
            Log.d(TAG, "drawLinearFillModify: endX " + endX);
            bottomY = zeroLineBuffer[3];
            ArrayList<Entry> noEmptyDataArray = new ArrayList<>();
            for (int x = mXBounds.min; x <= mXBounds.range + mXBounds.min; x++) {
//                Log.d(TAG, "drawLinear:noEmptyDataArray x::  " + x);
                e1 = dataSet.getEntryForIndex(x);
                if (e1.getY() != Y_DEFAULT_EMPTY) {
                    noEmptyDataArray.add(e1);
                }
            }
            for (int x = 0; x < noEmptyDataArray.size(); x++) {
//                Log.d(TAG, "drawLinear: range :: " + (mXBounds.range + mXBounds.min) + " j :: " + j);

                e1 = noEmptyDataArray.get(x == 0 ? 0 : (x - 1));
                e2 = noEmptyDataArray.get(x);
                e3 = (x == noEmptyDataArray.size() - 1) ? null : noEmptyDataArray.get(x + 1);
                if (e1 == null || e2 == null) continue;
                if (x == 0) {//是第一个path,todo 如果第一个数据是单个点呢
                    if (e3 == null || (e3.getX() - e2.getX()) > judgeDataStopIntervalX) {//距离后一个也是大于21，说明是单独的一个点
                        floats = new ArrayList<>();
                        floats.add(e2.getX());
                        floats.add(e2.getY() * phaseY);
                        floats.add(e2.getX());
                        floats.add(0f);
                        mLineBuffer = new float[floats.size()];
                        for (int i = 0; i < floats.size(); i++) {
                            mLineBuffer[i] = floats.get(i);
                        }
                        trans.pointValuesToPixel(mLineBuffer);
                        Path circlePath = new Path();
                        circlePath.moveTo(mLineBuffer[0] - circleRadius, mLineBuffer[3]);
                        circlePath.lineTo(mLineBuffer[0] - circleRadius, mLineBuffer[1]);
                        circlePath.lineTo(mLineBuffer[0] + circleRadius, mLineBuffer[1]);
                        circlePath.lineTo(mLineBuffer[0] + circleRadius, mLineBuffer[3]);
                        circlePath.close();
                        pathArrayList.add(circlePath);
                        draw(c, dataSet, trans, mXBounds, circlePath, false);
                    } else {
                        currentPath = new Path();
                        currentPath.moveTo(e2.getX(), fillMin);
                        currentPath.lineTo(e2.getX(), e2.getY() * phaseY);
                        pathArrayList.add(currentPath);
                    }
                } else if ((e2.getX() - e1.getX()) > judgeDataStopIntervalX) {//距离前一个大于12
                    if (e3 == null || (e3.getX() - e2.getX()) > judgeDataStopIntervalX) {//距离后一个也是大于21，说明是单独的一个点
                        if (currentPath != null && lastEntry != null) {
                            currentPath.lineTo(lastEntry.getX(), fillMin);
                            currentPath.close();
                            currentPath = null;
                            lastEntry = null;
                        }
                        floats = new ArrayList<>();
                        floats.add(e2.getX());
                        floats.add(e2.getY() * phaseY);
                        floats.add(e2.getX());
                        floats.add(0f);
                        mLineBuffer = new float[floats.size()];
                        for (int i = 0; i < floats.size(); i++) {
                            mLineBuffer[i] = floats.get(i);
                        }
                        trans.pointValuesToPixel(mLineBuffer);
                        Path circlePath = new Path();
                        circlePath.moveTo(mLineBuffer[0] - circleRadius, mLineBuffer[3]);
                        circlePath.lineTo(mLineBuffer[0] - circleRadius, mLineBuffer[1]);
                        circlePath.lineTo(mLineBuffer[0] + circleRadius, mLineBuffer[1]);
                        circlePath.lineTo(mLineBuffer[0] + circleRadius, mLineBuffer[3]);
                        circlePath.close();
//                        pathArrayList.add(circlePath);
                        draw(c, dataSet, trans, mXBounds, circlePath, false);
                    } else {//新起一个path
                        if (currentPath != null && lastEntry != null) {
                            currentPath.lineTo(lastEntry.getX(), fillMin);
                            currentPath.close();
                            currentPath = null;
                            lastEntry = null;
                        }
                        currentPath = new Path();
                        currentPath.moveTo(e2.getX(), fillMin);
                        currentPath.lineTo(e2.getX(), e2.getY() * phaseY);
                        pathArrayList.add(currentPath);
                    }
                } else {//正常添加
                    if (currentPath != null) {
                        currentPath.lineTo(e2.getX(), e2.getY() * phaseY);
                        lastEntry = e2;
                    }
                }

                if (true) {
                    //todo 判断最后一个path是否画出来
                }
            }
            if (currentPath != null && lastEntry != null) {
                currentPath.lineTo(lastEntry.getX(), fillMin);
                currentPath.close();
                currentPath = null;
                lastEntry = null;
            }
            for (Path path : pathArrayList) {
                draw(c, dataSet, trans, mXBounds, path, true);
            }
        }
    }

    protected void draw(Canvas c, ILineDataSet dataSet, Transformer trans, XBounds mXBounds, Path filled, boolean isNeedTrans) {
        if (isNeedTrans) {
            trans.pathValueToPixel(filled);
        }

        final Drawable drawable = dataSet.getFillDrawable();
        if (drawable != null) {
            drawFilledPath(c, filled, drawable);
        } else {
            drawFilledPath(c, filled, dataSet.getFillColor(), dataSet.getFillAlpha());
        }
    }

    public void setRenderCallback(RenderCallback renderCallback) {
        this.renderCallback = renderCallback;
    }

    public interface RenderCallback {
        public void onCurrentPointerPositionValueX(float xValue);

        public void onHighLightX(Entry entry);
    }

    class PathData {
        PathData(int pathType, ArrayList<Float> floats) {
            this.pathType = pathType;
            this.floats = floats;
        }

        protected static final int PATH_TYPE_PATH = 1;
        protected static final int PATH_TYPE_CIRCLE = 2;
        int pathType;
        ArrayList<Float> floats;

        public int getPathType() {
            return pathType;
        }

        public void setPathType(int pathType) {
            this.pathType = pathType;
        }

        public ArrayList<Float> getFloats() {
            return floats;
        }

        public void setFloats(ArrayList<Float> floats) {
            this.floats = floats;
        }
    }
}
