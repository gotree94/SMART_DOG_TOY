package com.jieli.healthaide.ui.sports.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.FormatUtil;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/9/21
 * @desc :
 */
public class PaceView extends ConstraintLayout {


    public PaceView(@NonNull Context context) {
        this(context, null);
    }

    public PaceView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaceView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PaceView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_pace, this, true);

    }


    public void setPaces(int[] values,int max,int average) {


        TextView tvAverage = findViewById(R.id.tv_pace_average_pace);
        TextView tvMax = findViewById(R.id.tv_pace_max_pace);
        tvAverage.setText(FormatUtil.paceFormat(average));
        tvMax.setText(FormatUtil.paceFormat(max));


        if (getContext() == null) return;
        ViewGroup viewGroup = findViewById(R.id.ll_pace);
        viewGroup.removeAllViews();
        int index = 1;

        for (int value : values) {
            PaceSubView subView = new PaceSubView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ValueUtil.dp2px(getContext(), 18), 1);
            lp.setMargins(0, 0, 0, ValueUtil.dp2px(getContext(), 12));
            subView.index = index;
            subView.setLayoutParams(lp);
            subView.progress = value / Math.max(1.0f, max);
            subView.value = value;
            viewGroup.addView(subView);
            index++;
        }

    }


    private static class PaceSubView extends View {


        private Paint bgPaint;
        private Paint progressPaint;
        private TextPaint textPaint;
        private float progress = 0.3f;
        private int index = 1;
        private int value;

        public PaceSubView(Context context) {
            super(context);

            bgPaint = new Paint();
            bgPaint.setStyle(Paint.Style.FILL);
            bgPaint.setAntiAlias(true);
            bgPaint.setColor(Color.parseColor("#ebebeb"));

            progressPaint = new Paint();
            progressPaint.setStyle(Paint.Style.FILL);
            progressPaint.setAntiAlias(true);
            progressPaint.setColor(context.getResources().getColor(R.color.main_color));

            textPaint = new TextPaint();
            textPaint.setTextSize(ValueUtil.sp2px(getContext(), 12));
            textPaint.setColor(Color.parseColor("#ffffff"));
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.LEFT);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();

            float r = h >> 1;
            canvas.drawRoundRect(0, 0, w, h, r, r, bgPaint);
            canvas.drawRoundRect(0, 0, w * progress, h, r, r, progressPaint);


            Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
            float baseline = ((fontMetrics.descent - fontMetrics.ascent) >> 1) - fontMetrics.descent + r;
            canvas.drawText(String.valueOf(index), r, baseline, textPaint);

            canvas.drawText(FormatUtil.paceFormat(value), ValueUtil.dp2px(getContext(), 56), baseline, textPaint);
        }
    }


}
