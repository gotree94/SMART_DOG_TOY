package com.contrarywind.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/10
 * @desc :
 */
public class DateWheelView extends WheelView {
    private int originMaxWidth = 0;

    public DateWheelView(Context context) {
        this(context, null);
    }

    public DateWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    @Override
    protected void measuredCenterContentStart(String content) {
        Rect rect = new Rect();
        paintCenterText.getTextBounds(content, 0, content.length(), rect);
        int vW = rect.width();
        paintLabel.getTextBounds(label, 0, label.length(), rect);
        int lW = rect.width();
        int padding = (int) (getContext().getResources().getDisplayMetrics().density * 4);
        int w = vW + lW + padding;
        drawCenterContentStart = (int) ((measuredWidth - w) * 0.5);
    }

    @Override
    protected void measuredOutContentStart(String content) {
        Rect rect = new Rect();
        paintOuterText.getTextBounds(content, 0, content.length(), rect);
        int vW = rect.width();
        paintLabel.getTextBounds(label, 0, label.length(), rect);
        int lW = rect.width();
        int padding = (int) (getContext().getResources().getDisplayMetrics().density * 4);
        int w = vW + lW + padding;
        drawOutContentStart = (int) ((measuredWidth - w) * 0.5);
    }

    @Override
    protected void measureTextWidthHeight() {
        super.measureTextWidthHeight();
        originMaxWidth = maxTextWidth;

        Rect rect = new Rect();
        if (label == null) return;
        Log.d("sen", "measureTextWidthHeight label-->" + label + "\tmaxTextWidth = " + maxTextWidth);
        paintLabel.getTextBounds(label, 0, label.length(), rect);
        int lW = rect.width();
        int padding = (int) (getContext().getResources().getDisplayMetrics().density * 4);
        maxTextWidth = maxTextWidth + lW + padding;

    }

    @Override
    protected void drawLabel(Canvas canvas, int drawRightContentStart) {

        int padding = (int) (getContext().getResources().getDisplayMetrics().density * 6);
        int start = measuredWidth / 2 - maxTextWidth / 2 + originMaxWidth + padding;
        Log.d("sen", String.format(Locale.ENGLISH, "drawLabel start--> %d, measuredWidth--> %d, maxTextWidth-->%d",
                start, measuredWidth, maxTextWidth));
        super.drawLabel(canvas, start);
    }
}
