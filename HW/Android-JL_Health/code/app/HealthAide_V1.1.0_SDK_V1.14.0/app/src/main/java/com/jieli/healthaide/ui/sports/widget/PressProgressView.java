package com.jieli.healthaide.ui.sports.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.ui.widget.SimpleAnimatorListener;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/7/21
 * @desc :
 */
public class PressProgressView extends AppCompatTextView {
    private final static int ANIMATOR_TIME = 1000;

    private Paint circlePaint;
    private Paint firstProgressPaint;
    private Paint secondProgressPaint;
    private int progress;
    private ValueAnimator addAnimator;
    private ValueAnimator subAnimator;

    private onPressProgressListener onPressProgressListener;


    public void setOnPressProgressListener(PressProgressView.onPressProgressListener onPressProgressListener) {
        this.onPressProgressListener = onPressProgressListener;
    }

    public PressProgressView(Context context) {
        super(context);
    }

    public PressProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PressProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int color = Color.parseColor((String) getTag());
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(color);

        firstProgressPaint = new Paint();
        firstProgressPaint.setStyle(Paint.Style.STROKE);
        firstProgressPaint.setStrokeWidth(ValueUtil.dp2px(getContext(), 3));
        firstProgressPaint.setAntiAlias(true);
        firstProgressPaint.setColor(Color.parseColor("#dedede"));

        secondProgressPaint = new Paint();
        secondProgressPaint.setStyle(Paint.Style.STROKE);
        secondProgressPaint.setStrokeWidth(ValueUtil.dp2px(getContext(), 3));
        secondProgressPaint.setAntiAlias(true);
        secondProgressPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float cx = getWidth() >> 1;
        float cy = getHeight() >> 1;
        float r = Math.min(cy, cx);
        canvas.save();
        canvas.drawCircle(cx, cy, r - ValueUtil.dp2px(getContext(), 5), circlePaint);

        r = Math.min(cy, cx) - firstProgressPaint.getStrokeWidth() / 2;

        RectF rectF = new RectF(cx - r, cy - r, cy + r, cy + r);
        canvas.drawArc(rectF, 0, 365, false, firstProgressPaint);

        float startAngle = -90;
        float sweepAngle = progress / 100f * 365;
        canvas.drawArc(rectF, startAngle, sweepAngle, false, secondProgressPaint);
        canvas.restore();
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (subAnimator != null && subAnimator.isRunning()) {
                    subAnimator.cancel();
                }
                progress = 0;
                startProgressAdd();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startProgressSub();
                if (addAnimator != null && addAnimator.isRunning()) {
                    addAnimator.cancel();
                }
                break;
        }
        return true;
    }

    private void startProgressAdd() {
        if (addAnimator == null) {
            addAnimator = ValueAnimator.ofInt(0, 100);
            addAnimator.addUpdateListener(animation -> {
                progress = (int) animation.getAnimatedValue();
                if (onPressProgressListener != null) {
                    onPressProgressListener.onProgress(progress,false);
                }
                invalidate();
            });
            addAnimator.setDuration(ANIMATOR_TIME);
            addAnimator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (onPressProgressListener != null) {
                        onPressProgressListener.onProgress(progress,true);
                    }
                 }
            });
        }
        addAnimator.start();
    }

    private void startProgressSub() {
        subAnimator = ValueAnimator.ofInt(progress, 0);
        subAnimator.addUpdateListener(animation -> {
            progress = (int) animation.getAnimatedValue();
            invalidate();
        });
        subAnimator.setDuration((long) (ANIMATOR_TIME * (progress / 100f)));
        subAnimator.start();
    }


    public interface onPressProgressListener {
        void onProgress(int progress,boolean end);
    }
}
