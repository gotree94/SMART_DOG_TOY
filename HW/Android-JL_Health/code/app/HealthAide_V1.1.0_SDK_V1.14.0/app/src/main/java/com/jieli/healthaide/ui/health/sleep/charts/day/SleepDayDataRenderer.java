package com.jieli.healthaide.ui.health.sleep.charts.day;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.TypedValue;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.renderer.LineScatterCandleRadarRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/3/21 3:28 PM
 * @desc :
 */
public class SleepDayDataRenderer extends LineScatterCandleRadarRenderer {
    private String tag = getClass().getSimpleName();
    private SleepDayDataProvider provider;

    public SleepDayDataRenderer(SleepDayDataProvider provider, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        this.provider = provider;
    }

    @Override
    public void initBuffers() {

    }

    @Override
    public void drawData(Canvas c) {
//        Log.e(tag, "drawData w = " + c.getWidth() + "\th = " + c.getHeight() + "\t" + c);
        SleepDayData dayData = provider.getSleepData();

        mRenderPaint.setStrokeWidth(2);
        mRenderPaint.setStyle(Paint.Style.FILL);
        for (ISleepDayDataSet dayDataSet : dayData.getDataSets()) {
            mXBounds.set(provider, dayDataSet);
            Transformer trans = provider.getTransformer(dayDataSet.getAxisDependency());
            for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {
                if (j >= dayDataSet.getEntryCount()) continue;
                SleepDayEntry entry = dayDataSet.getEntryForIndex(j);
                mRenderPaint.setColor(entry.color);
                float[] f = new float[]{entry.xMin(), entry.type * 1.0f, entry.xMax(), entry.type + 0.99f};//todo 这里long类型转成float，导致同样的space，算出来的positionPX不一样
                trans.pointValuesToPixel(f);
                c.drawRect(f[0], f[3], f[2], f[1], mRenderPaint);//sdk较低需要自己判读top和bottom
//                c.drawLine(c.getWidth() / 2, 0, c.getWidth() / 2, c.getHeight(), mRenderPaint);
            }

        }


    }

    @Override
    public void drawValues(Canvas c) {
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {


    }

    @Override
    public void drawExtras(Canvas c) {
        SleepDayData sleepDayData = provider.getSleepData();
        ISleepDayDataSet set = sleepDayData.getDataSetByIndex(0);
        if (set == null) return;

        SleepDayChart sleepDayChart = (SleepDayChart) provider;

        Paint p = new Paint();
        float drawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, sleepDayChart.getResources().getDisplayMetrics());

        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, sleepDayChart.getResources().getDisplayMetrics());
        p.setTextSize(textSize);
        p.setColor(Color.WHITE);
        Bitmap icNight = BitmapFactory.decodeResource(sleepDayChart.getResources(), R.mipmap.ic_night_nol);
        Bitmap icDay = BitmapFactory.decodeResource(sleepDayChart.getResources(), R.mipmap.ic_day_nol);


        long min = SleepDayEntry.fromX(sleepDayChart.getXAxis().getAxisMinimum());
        long max = SleepDayEntry.fromX((float) Math.ceil(sleepDayChart.getXAxis().getAxisMaximum()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(min);
        SimpleDateFormat simpleDateFormat = CustomTimeFormatUtil.dateFormat("HH:mm");
        String minText = simpleDateFormat.format(new Date(min));
        calendar.setTimeInMillis(max);
        String maxText = simpleDateFormat.format(new Date(max));
        MPPointD pix = provider.getTransformer(set.getAxisDependency()).getPixelForValues(provider.getLowestVisibleX(), set.getYMin());
        p.setTextAlign(Paint.Align.LEFT);
        c.drawText(minText, (float) pix.x + icNight.getWidth() + drawablePadding, c.getHeight() - p.getTextSize() / 3, p);
        c.drawBitmap(icNight, (float) pix.x, (float) c.getHeight() - icNight.getHeight(), p);


        float textWidth = p.measureText("00:00");
        p.setTextAlign(Paint.Align.RIGHT);
        pix = provider.getTransformer(set.getAxisDependency()).getPixelForValues(provider.getHighestVisibleX(), set.getYMin());
        c.drawText(maxText, (float) pix.x, c.getHeight() - p.getTextSize() / 3, p);
        c.drawBitmap(icDay, (float) pix.x - textWidth - icDay.getWidth() - drawablePadding, (float) c.getHeight() - icDay.getHeight(), p);


    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        SleepDayData sleepDayData = provider.getSleepData();

        for (Highlight high : indices) {
            ISleepDayDataSet set = sleepDayData.getDataSetByIndex(high.getDataSetIndex());
            SleepDayDataSet sleepDayDataSet = (SleepDayDataSet) set;
            sleepDayDataSet.setDrawHorizontalHighlightIndicator(false);
            if (set == null || !set.isHighlightEnabled())
                continue;

            SleepDayEntry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            float lowValue = 0;
            float highValue = 4;
            float y = (lowValue + highValue) / 2f;
            MPPointD pix = provider.getTransformer(set.getAxisDependency()).getPixelForValues(e.xMin(), y);
            high.setDraw((float) pix.x, (float) pix.y);

            mHighlightPaint.setColor(Color.RED);
            mHighlightPaint.setStyle(Paint.Style.FILL);


            MPPointD leftMp = provider.getTransformer(set.getAxisDependency()).getPixelForValues(e.xMin(), 0);
            MPPointD topAndEndMp = provider.getTransformer(set.getAxisDependency()).getPixelForValues(e.xMax(), e.type);
            float bottom = provider.getHeight();


            int[] startColors = new int[]{
                    Color.parseColor("#ff98f7fe"),
                    Color.parseColor("#ffaccbf8"),
                    Color.parseColor("#ffffa5cd"),
                    Color.parseColor("#fff0d496"),
            };

            int endColors = Color.parseColor("#0Affffff");
            LinearGradient linearGradient = new LinearGradient(0, 0, 0, bottom, startColors[e.type], endColors, Shader.TileMode.CLAMP);

            mHighlightPaint.setShader(linearGradient);
            c.drawRect((float) leftMp.x, (float) topAndEndMp.y, (float) topAndEndMp.x, bottom, mHighlightPaint);


            mHighlightPaint.setStrokeWidth(Utils.convertDpToPixel(1));

            int[] colors = new int[]{
                    Color.parseColor("#00ffffff"),
                    Color.parseColor("#7fffffff"),
                    Color.parseColor("#00ffffff"),
            };

            LinearGradient linearGradient1 = new LinearGradient(0, 0, 0, bottom, colors, null, Shader.TileMode.CLAMP);
            mHighlightPaint.setShader(linearGradient1);
            Transformer trans = provider.getTransformer(set.getAxisDependency());
            float[] f = new float[]{e.xMin(), 4.1f, e.xMax(), 4.1f};
            trans.pointValuesToPixel(f);
            float left = (f[0] + f[2]) / 2;
            float top = f[1];
            c.drawLine(left, top, left, bottom, mHighlightPaint);


//           drawHighlightLines(c, (float) pix.x, (float) pix.y, set);

        }
    }
}
