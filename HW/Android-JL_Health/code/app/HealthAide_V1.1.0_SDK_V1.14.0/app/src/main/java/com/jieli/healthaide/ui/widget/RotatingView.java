package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.jieli.healthaide.R;
import com.jieli.jl_rcsp.util.JL_Log;


/**
 * 温度控制
 * Created by yangle on 2016/11/29.
 */
public class RotatingView extends View {

    private int width;
    private int height;
    private int arcRadius;

    private Paint textValuePaint;
    private Paint textUnitPaint;
    private Paint arcPaint;
    private Paint buttonPaint;
    private Paint arc2Paint;

    private int min = 0;
    private int max = 100;
    private int percent = 0;
    private int targetValue = 0;
    private int currentValue = /*12880*/0;
    private int angleRate = 1;
    private String unitTypeDescribe = "目标";
    private String unitText = "步";

    private OnValueChangeListener onValueChangeListener;

    private OnClickListener onClickListener;

    private int mContentStartColor = 0xFF805BEB;
    private int mContentEndColor = 0x7f5e41eb;
    private int mContentTextColor = 0xff575757;
    private int mUnitTextColor = 0x80ffffff;
    private int mArcBackgroundColor = 0x4DFFFFFF;
    // 当前按钮旋转的角度
    private float rotateAngle;
    // 当前的角度
    private float currentAngle;

    private int mContentLineWidth = dp2px(5);
    private int mBackgroundLineWidth = dp2px(3);
    private int mPaddingWidth = dp2px(3);
    private int mTextSize = sp2px(16);
    private int mTextUnitSize = sp2px(10);
    SweepGradient sweepGradient;
    private int startAngle = 130;
    private float angleOne = 1.0f * getMaxRotateAngle() / (max - min) / angleRate;

    public RotatingView(Context context) {
        this(context, null);
    }

    public RotatingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RotatingView);
        if (typedArray != null) {
            mContentStartColor = typedArray.getColor(R.styleable.RotatingView_contentStartColor, mContentStartColor);
            mContentEndColor = typedArray.getColor(R.styleable.RotatingView_contentEndColor, mContentEndColor);
            mContentTextColor = typedArray.getColor(R.styleable.RotatingView_contentTextColor, mContentTextColor);
            mContentLineWidth = typedArray.getDimensionPixelSize(R.styleable.RotatingView_contentLineWidth, mContentLineWidth);
            mBackgroundLineWidth = typedArray.getDimensionPixelSize(R.styleable.RotatingView_backgroundLineWidth, mBackgroundLineWidth);
            mPaddingWidth = typedArray.getDimensionPixelSize(R.styleable.RotatingView_paddingWidth, mPaddingWidth);
            mTextSize = typedArray.getDimensionPixelSize(R.styleable.RotatingView_rTextSize, mTextSize);
            mTextUnitSize = typedArray.getDimensionPixelSize(R.styleable.RotatingView_unitTextSize, mTextUnitSize);
            String stringUnitType = typedArray.getString(R.styleable.RotatingView_unitTypeDescribe);
            if (!TextUtils.isEmpty(stringUnitType)) {
                unitTypeDescribe = stringUnitType;
            }
            String stringUnit = typedArray.getString(R.styleable.RotatingView_unitType);
            if (!TextUtils.isEmpty(stringUnit)) {
                unitText = stringUnit;
            }
            mUnitTextColor = typedArray.getColor(R.styleable.RotatingView_unitTextColor, mUnitTextColor);
            typedArray.recycle();
//            int imageResId = typedArray.getResourceId(R.styleable.RotatingView_indicatorImage, -1);
//            if (imageResId != -1) {
//                indicatorImage = BitmapFactory.decodeResource(getResources(), imageResId);
//            }
        }
        textValuePaint = new Paint();
        textValuePaint.setAntiAlias(true);
        textValuePaint.setTextSize(mTextSize);
        textValuePaint.setColor(mContentTextColor);
        textValuePaint.setTextAlign(Paint.Align.CENTER);

        textUnitPaint = new Paint();
        textUnitPaint.setAntiAlias(true);
        textUnitPaint.setTextSize(mTextUnitSize);
        textUnitPaint.setColor(mUnitTextColor);
        textUnitPaint.setTextAlign(Paint.Align.CENTER);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(mArcBackgroundColor/*Color.parseColor("#3CB7EA")*/);
        arcPaint.setStrokeWidth(mBackgroundLineWidth);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        buttonPaint = new Paint();

        arc2Paint = new Paint();
        arc2Paint.setAntiAlias(true);
        arc2Paint.setStrokeWidth(mBackgroundLineWidth);
        arc2Paint.setStyle(Paint.Style.STROKE);
        arc2Paint.setStrokeCap(Paint.Cap.ROUND);

        percent = Math.max(min, percent);
        // 计算每格的角度
        angleOne = (float) getMaxRotateAngle() / (max - min) / angleRate;
        // 计算旋转角度
        if (percent > max) {
            rotateAngle = (max - min) * angleRate * angleOne;
        } else {
            rotateAngle = (percent - min) * angleRate * angleOne;
        }

        int[] colors = {mContentStartColor, mContentStartColor, mContentEndColor, mContentStartColor};
        float[] postions = {0f, 0.05f, 0.8f, 0.99f};
        sweepGradient = new SweepGradient(0, 0, colors, postions);
        arc2Paint.setShader(sweepGradient);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 控件宽、高
        width = height = Math.min(h, w);
        // 圆弧半径
        arcRadius = width / 2 - mPaddingWidth * 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
        drawText(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //noinspection SuspiciousNameCombination
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureWidth(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = measureSize;
        switch (measureMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = measureSize;
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 绘制刻度盘下的圆弧
     *
     * @param canvas 画布
     */
    private void drawArc(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() / 2f, getHeight() / 2f);
        canvas.rotate(startAngle);
        RectF rectF = new RectF(-arcRadius, -arcRadius, arcRadius, arcRadius);
        canvas.drawArc(rectF, 0, getMaxRotateAngle(), false, arcPaint);
        JL_Log.d("RotatingView", "drawArc", "rotateAngle : " + rotateAngle);
        canvas.drawArc(rectF, 0, rotateAngle, false, arc2Paint);
        canvas.restore();
    }

    private void drawText(Canvas canvas) {
        Rect boundsUnitDescribe = new Rect();
        textUnitPaint.getTextBounds(unitTypeDescribe, 0, unitTypeDescribe.length(), boundsUnitDescribe);
        int textX2 = getWidth() / 2;
        int textY2 = getHeight() / 2 - boundsUnitDescribe.height() / 2 - dp2px(4);
        canvas.drawText(unitTypeDescribe, textX2, textY2, textUnitPaint);
        String text = targetValue != 0 ? targetValue + " " : "- -";
        Rect bounds = new Rect();
        textValuePaint.getTextBounds(text, 0, text.length(), bounds);
        Rect boundsUnit = new Rect();
        textUnitPaint.getTextBounds(unitText, 0, unitText.length(), boundsUnit);

        int rectWidth = bounds.width() + boundsUnit.width();
        int textX = getWidth() / 2 - (bounds.width() / 2 - (rectWidth / 2 - boundsUnit.width()));
        int textY = getHeight() / 2 + boundsUnit.height() + dp2px(4);
        canvas.drawText(text, textX, textY, textValuePaint);

        int textX3 = getWidth() / 2 + boundsUnit.width() / 2 + (rectWidth / 2 - boundsUnit.width());
        int textY3 = getHeight() / 2 + boundsUnit.height() + dp2px(4) /*+ boundsUnit.height()*//* + dp2px(22)*/;
        canvas.drawText(unitText, textX3, textY3, textUnitPaint);
    }

    private int getMaxRotateAngle() {
        return 360 - (startAngle - 90) * 2;
    }



    public void setValue(int targetValue, int currentValue) {
        if (this.targetValue == targetValue && this.currentValue == currentValue) {
            return;
        }
        int percent = 0;
        if (currentValue != 0 && targetValue != 0) {
            percent = (int) (((float) currentValue / targetValue) * 100);
        }
        percent = Math.max(min, percent);
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.percent = percent;
        // 计算每格的角度
        angleOne = (float) getMaxRotateAngle() / (max - min) / angleRate;
        // 计算旋转角度
        if (percent > max) {
            rotateAngle = (max - min) * angleRate * angleOne;
        } else {
            rotateAngle = (percent - min) * angleRate * angleOne;
        }
        invalidate();
    }


    /**
     * 设置值改变监听
     *
     * @param onValueChangeListener 监听接口
     */
    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    /**
     * 设置点击监听
     *
     * @param onClickListener 点击回调接口
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public int getValue() {
        return currentValue;
    }

    public int getTargetValue() {
        return targetValue;
    }


    public void setContentStartColor(int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mContentStartColor = getResources().getColor(colorResId, null);
        } else {
            mContentStartColor = getResources().getColor(colorResId);
        }
        int[] colors = {mContentStartColor, mContentStartColor, mContentEndColor, mContentStartColor};
        float[] postions = {0f, 0.05f, 0.8f, 0.99f};
        sweepGradient = new SweepGradient(0, 0, colors, postions);
        arc2Paint.setShader(sweepGradient);
    }

    public void setContentEndColor(int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mContentEndColor = getResources().getColor(colorResId, null);
        } else {
            mContentEndColor = getResources().getColor(colorResId);
        }
        int[] colors = {mContentStartColor, mContentStartColor, mContentEndColor, mContentStartColor};
        float[] postions = {0f, 0.05f, 0.8f, 0.99f};
        sweepGradient = new SweepGradient(0, 0, colors, postions);
        arc2Paint.setShader(sweepGradient);
    }

    public void setContentTextColor(int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mContentTextColor = getResources().getColor(colorResId, null);
        } else {
            mContentTextColor = getResources().getColor(colorResId);
        }
        textValuePaint.setColor(mContentTextColor);
    }

    /**
     * 值改变监听接口
     */
    public interface OnValueChangeListener {
        /**
         * 回调方法
         *
         * @param value 值
         */
        void change(RotatingView view, int value, boolean end);
    }

    /**
     * 点击回调接口
     */
    public interface OnClickListener {
        /**
         * 点击回调方法
         *
         * @param temp 温度
         */
        void onClick(int temp);
    }

    public int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
