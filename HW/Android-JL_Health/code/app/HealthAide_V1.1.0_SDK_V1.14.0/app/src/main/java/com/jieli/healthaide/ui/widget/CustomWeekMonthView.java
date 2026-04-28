package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.RangeMonthView;

/**
 * @ClassName: CustomMonthView
 * @Description: 选择日期弹窗的月视图的布局
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/8 11:21
 */
public class CustomWeekMonthView extends RangeMonthView {

    private int mRadius;

    public CustomWeekMonthView(Context context) {
        super(context);
    }


    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2;
        mSchemePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme,
                                     boolean isSelectedPre, boolean isSelectedNext) {

        int cy = y + mItemHeight / 2;
        if (!isSelectedPre) {//第一个
            canvas.drawRect(x + mItemWidth * 1 / 3, cy - mRadius, x + mItemWidth, cy + mRadius, mSelectedPaint);
            canvas.drawCircle(x + mItemWidth * 1 / 3, cy, mRadius, mSelectedPaint);
        } else if (isSelectedPre && !isSelectedNext) {//最后一个
            canvas.drawRect(x, cy - mRadius, x + mItemWidth * 2 / 3, cy + mRadius, mSelectedPaint);
            canvas.drawCircle(x + mItemWidth * 2 / 3, cy, mRadius, mSelectedPaint);
        } else {
            canvas.drawRect(x, cy - mRadius, x + mItemWidth, cy + mRadius, mSelectedPaint);
        }
        return false;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int cy = y + mItemHeight / 2;
        canvas.drawCircle(cx, cy, mRadius, mSchemePaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        float baselineY = mTextBaseLine + y;
        int cx = x + mItemWidth / 2;
        int cy = y + mItemHeight / 2;
        int rx = 10;

        boolean isInRange = isInRange(calendar);
        boolean isEnable = !onCalendarIntercept(calendar);

        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()),
                    cx,
                    baselineY,
                    mSelectTextPaint);
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()),
                    cx,
                    baselineY,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() && isInRange && isEnable ? mSchemeTextPaint : mOtherMonthTextPaint);

        } else {
            if (calendar.isCurrentDay()) {
                Log.d("TAG", "onDrawText: " + this);
                Paint paint;
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dipToPx(getContext(), 1));
                paint.setColor(0xffEB9D5B);
                RectF rectF = new RectF(cx - mRadius, cy - mRadius, cx + mRadius, cy + mRadius);
                canvas.drawRoundRect(rectF, rx, rx, paint);
            }
            canvas.drawText(String.valueOf(calendar.getDay()), cx, baselineY,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() && isInRange && isEnable ? mCurMonthTextPaint : mOtherMonthTextPaint);
        }
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
