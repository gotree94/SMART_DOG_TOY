package com.jieli.healthaide.ui.health.sleep.charts.day;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 3:13 PM
 * @desc :
 */
class SleepDayMarkerView extends MarkerView {
    private final String TAG = this.getClass().getSimpleName();
    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     */
    private TextView tvMin;
    private TextView tvTime;

    private Context context;

    public SleepDayMarkerView(Context context) {
        super(context, R.layout.item_sleep_day_marker_view);
        this.context = context;
        tvMin = findViewById(R.id.tv_sleep_marker_view_min);
        tvTime = findViewById(R.id.tv_sleep_marker_view_time);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        SleepDayEntry entry = (SleepDayEntry) e;
        long startTime = entry.startTime;
        long endTime = entry.endTime;
        long space = endTime - startTime;
        space = space / 1000 / 60;
        int[] typesRes = {R.string.deep_sleep, R.string.light_sleep, R.string.rapid_eye_movement, R.string.sober};

        String typeStr = context.getString(typesRes[entry.type]);
        Date start = new Date(startTime);
        Date end = new Date(endTime);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = CustomTimeFormatUtil.dateFormat("HH:mm");
        String sleepTimeStr = null;
        String hourUnit = context.getString(R.string.hour);
        String minUnit = context.getString(R.string.min);
        if (space == 0) {//时间间隔为0
        } else if (space % 60 == 0) {//间隔为整小时
            sleepTimeStr = CalendarUtil.formatString("%d%s", space / 60, hourUnit);
        } else if ((space / 60) > 0) {//间隔大于一小时
            sleepTimeStr = CalendarUtil.formatString("%d%s%d%s", space / 60, hourUnit, space % 60, minUnit);
        } else {//
            sleepTimeStr = CalendarUtil.formatString("%d%s", space % 60, minUnit);
        }
        tvMin.setText(CalendarUtil.formatString("%s%s", typeStr, sleepTimeStr));
        tvTime.setText(simpleDateFormat.format(start) + "-" + simpleDateFormat.format(end));
        super.refreshContent(e, highlight);
    }


    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        super.draw(canvas, posX, posY);
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        MPPointF offset = getOffset();
        MPPointF mOffset2 = new MPPointF();
        mOffset2.x = offset.x;
        mOffset2.y = offset.y;

        Chart chart = getChartView();

        float width = getWidth();
        float height = getHeight();
        if (posX + mOffset2.x < 0) {
            mOffset2.x = -posX;
        } else if (chart != null) {
            mOffset2.x = -width / 2;
            if (posX + width / 2 > chart.getWidth()) {
                mOffset2.x = chart.getWidth() - posX - width;
            } else if (posX - width / 2 < 0) {
                mOffset2.x = -posX;
            }
        }

        if (posY + mOffset2.y < 0) {
            mOffset2.y = -posY;
        } else if (chart != null && posY + height + mOffset2.y > chart.getHeight()) {
            mOffset2.y = chart.getHeight() - posY - height;
        }
        return mOffset2;
    }
}
