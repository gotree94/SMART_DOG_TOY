package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.jieli.healthaide.R;

/**
 * @ClassName: CalendarPointerView
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/20 17:45
 */
public class CalendarPointerView extends View {
    private boolean isMoveUpTrade = false;//滑动，抬手时，是否取靠近值
    private int rangeValue = 0;
    private float currentPosition = 0.0f;
    private CalendarPointerCallback calendarPointerCallback;
    private int pointerColor = 0xFF9FD37A;
    private boolean pointerCoordinateVisible = true;

    public CalendarPointerView(Context context) {
        this(context, null);
    }

    public CalendarPointerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarPointerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarPointerView);
        if (typedArray != null) {
            pointerColor = typedArray.getColor(R.styleable.CalendarPointerView_pointerColor, pointerColor);
            pointerCoordinateVisible = typedArray.getBoolean(R.styleable.CalendarPointerView_pointerCoordinateVisible, pointerCoordinateVisible);
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPointer(canvas);
        drawCoordinate(canvas);
    }

    private void drawPointer(Canvas canvas) {

    }
    private void drawCoordinate(Canvas canvas) {

    }

    public void registerCalendarPointerCallback(CalendarPointerCallback calendarPointerCallback) {
        this.calendarPointerCallback = calendarPointerCallback;
    }

    public void unregisterCalendarPointerCallback() {
        this.calendarPointerCallback = null;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
        this.calendarPointerCallback.currentPositionOnChange(position);
    }

    public int changeRange(int rangeValue) {//改变范围值，返回新的当前值
        return (int) (currentPosition * rangeValue);
    }

    public interface CalendarPointerCallback {
        public void currentPositionOnChange(int currentPosition);
    }
}
