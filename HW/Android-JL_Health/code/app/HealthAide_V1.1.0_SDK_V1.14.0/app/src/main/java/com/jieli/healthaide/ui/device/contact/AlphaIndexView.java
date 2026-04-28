package com.jieli.healthaide.ui.device.contact;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/17/21 10:50 AM
 * @desc :
 */
public class AlphaIndexView extends View {
    private int currentIndex = 0;

    private String[] indexString = new String[27];

    private TextPaint textPaint;
    private OnIndexChangeListener listener;
    private final static int MAX_INDEX = 27;

    public AlphaIndexView(Context context) {
        super(context);
        init();
    }

    public AlphaIndexView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlphaIndexView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AlphaIndexView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        textPaint = new TextPaint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics()));
        textPaint.setColor(Color.parseColor("#FF858585"));

        indexString[0] = "#";
        for (int i = 0; i < 26; i++) {
            char c = (char) (65 + i);
            indexString[i + 1] = String.valueOf(c);
        }


    }

    public void setCurrentIndex(int currentIndex) {
        if (this.currentIndex == currentIndex) return;
        this.currentIndex = currentIndex;
        invalidate();
    }

    public void setListener(OnIndexChangeListener listener) {
        this.listener = listener;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float y = event.getY();
        float h = getHeight() / MAX_INDEX;
        int index = (int) Math.floor(y / h);
        if (index != currentIndex) {
            setCurrentIndex(index);
            if (listener != null) {
                char c = (char) (65 + index);
                listener.onIndexChange(index, indexString[index]);
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int itemHeight = height / MAX_INDEX;
        for (int i = 0; i < MAX_INDEX; i++) {
             int y = itemHeight * i + (itemHeight >> 1);
            Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
            float baseline = ((fontMetrics.descent - fontMetrics.ascent) >> 1) - fontMetrics.descent;
            y += baseline;
            canvas.drawText(indexString[i], width >> 1, y, textPaint);
        }
    }

    public static interface OnIndexChangeListener {
        void onIndexChange(int index, String text);
    }


}
