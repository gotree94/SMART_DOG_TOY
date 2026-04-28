package com.jieli.healthaide.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.jieli.healthaide.R;
import com.scwang.smart.refresh.header.waterdrop.WaterDropView;
import com.scwang.smart.refresh.header.wdrawable.MaterialProgressDrawable;
import com.scwang.smart.refresh.header.wdrawable.ProgressDrawable;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.simple.SimpleComponent;
import com.scwang.smart.refresh.layout.util.SmartUtil;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;

/**
 * @ClassName: CustomRefreshHeaderView
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/25 15:17
 */
public class CustomRefreshHeaderView extends SimpleComponent implements RefreshHeader {
    protected static final float MAX_PROGRESS_ANGLE = 0.8F;
    protected RefreshState mState;
    protected ImageView mImageView;
    protected WaterDropView mWaterDropView;
    protected ProgressDrawable mProgressDrawable;
    protected MaterialProgressDrawable mProgress;

    public CustomRefreshHeaderView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CustomRefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        SpinnerStyle[] var4 = SpinnerStyle.values;
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            SpinnerStyle style = var4[var6];
            if (style.scale) {
                this.mSpinnerStyle = style;
                break;
            }
        }

        this.mWaterDropView = new WaterDropView(context);
        this.mWaterDropView.updateCompleteState(0);
        this.addView(this.mWaterDropView, -1, -1);
        this.mProgressDrawable = new ProgressDrawable();
        Drawable progressDrawable = this.mProgressDrawable;
        progressDrawable.setCallback(this);
        progressDrawable.setBounds(0, 0, SmartUtil.dp2px(20.0F), SmartUtil.dp2px(20.0F));

        this.mImageView = new ImageView(context);
        this.mProgress = new MaterialProgressDrawable(this.mImageView);
        this.mProgress.setColorSchemeColors(new int[]{-1, -16737844, -48060, -10053376, -5609780, -30720});
        this.mImageView.setImageDrawable(this.mProgress);
        this.addView(this.mImageView, SmartUtil.dp2px(30.0F), SmartUtil.dp2px(20.0F));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View imageView = this.mImageView;
        View dropView = this.mWaterDropView;
        LayoutParams lpImage = (LayoutParams) imageView.getLayoutParams();
        imageView.measure(MeasureSpec.makeMeasureSpec(lpImage.width, EXACTLY), MeasureSpec.makeMeasureSpec(lpImage.height, EXACTLY));
        dropView.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), AT_MOST), heightMeasureSpec);
        int maxWidth = Math.max(imageView.getMeasuredWidth(), dropView.getMeasuredWidth());
        int maxHeight = Math.max(imageView.getMeasuredHeight(), dropView.getMeasuredHeight());
        super.setMeasuredDimension(View.resolveSize(maxWidth, widthMeasureSpec), View.resolveSize(maxHeight, heightMeasureSpec));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View imageView = this.mImageView;
        View dropView = this.mWaterDropView;
        int measuredWidth = this.getMeasuredWidth();
        int widthWaterDrop = dropView.getMeasuredWidth();
        int heightWaterDrop = dropView.getMeasuredHeight();
        int leftWaterDrop = measuredWidth / 2 - widthWaterDrop / 2;
        dropView.layout(leftWaterDrop, 0, leftWaterDrop + widthWaterDrop, 0 + heightWaterDrop);
        int widthImage = imageView.getMeasuredWidth();
        int heightImage = imageView.getMeasuredHeight();
        int leftImage = measuredWidth / 2 - widthImage / 2;
        int topImage = widthWaterDrop / 2 - widthImage / 2;
        if (topImage + heightImage > dropView.getBottom() - (widthWaterDrop - widthImage) / 2) {
            topImage = dropView.getBottom() - (widthWaterDrop - widthImage) / 2 - heightImage;
        }

        imageView.layout(leftImage, topImage, leftImage + widthImage, topImage + heightImage);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        View dropView = this.mWaterDropView;
        Drawable progressDrawable = this.mProgressDrawable;
        if (this.mState == RefreshState.Refreshing) {
            canvas.save();
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.gray_9E9E9E));
            paint.setTextSize(SmartUtil.dp2px(14));
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float textHeight = fontMetrics.descent - fontMetrics.ascent;
            float textWidth = paint.measureText(getContext().getString(R.string.synchronizing_data));
            float contentWidth = progressDrawable.getBounds().width() + SmartUtil.dp2px(8) + textWidth;
            canvas.translate((float) this.getWidth() / 2.0F - (float) contentWidth / 2.0F
                    , (float) (this.mWaterDropView.getMaxCircleRadius() + dropView.getPaddingTop()) - (float) progressDrawable.getBounds().height() / 2.0F);
            progressDrawable.draw(canvas);
            canvas.drawText(getContext().getText(R.string.synchronizing_data).toString(),
                    (float) progressDrawable.getBounds().width() + SmartUtil.dp2px(8),
                    (float) (this.mWaterDropView.getMaxCircleRadius() + dropView.getPaddingTop()) - (float)textHeight/ 2.0F,
                    paint);
            canvas.restore();
        }

    }

    public void invalidateDrawable(@NonNull Drawable drawable) {
        this.invalidate();
    }

    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {
        if (isDragging || this.mState != RefreshState.Refreshing && this.mState != RefreshState.RefreshReleased) {
            View dropView = this.mWaterDropView;
            this.mWaterDropView.updateCompleteState(Math.max(offset, 0), height + maxDragHeight);
            dropView.postInvalidate();
        }

        if (isDragging) {
            float originalDragPercent = 1.0F * (float) offset / (float) height;
            float dragPercent = Math.min(1.0F, Math.abs(originalDragPercent));
            float adjustedPercent = (float) Math.max((double) dragPercent - 0.4D, 0.0D) * 5.0F / 3.0F;
            float extraOS = (float) (Math.abs(offset) - height);
            float tensionSlingshotPercent = Math.max(0.0F, Math.min(extraOS, (float) height * 2.0F) / (float) height);
            float tensionPercent = (float) ((double) (tensionSlingshotPercent / 4.0F) - Math.pow((double) (tensionSlingshotPercent / 4.0F), 2.0D)) * 2.0F;
            float strokeStart = adjustedPercent * 0.8F;
            float rotation = (-0.25F + 0.4F * adjustedPercent + tensionPercent * 2.0F) * 0.5F;
            this.mProgress.showArrow(true);
            this.mProgress.setStartEndTrim(0.0F, Math.min(0.8F, strokeStart));
            this.mProgress.setArrowScale(Math.min(1.0F, adjustedPercent));
            this.mProgress.setProgressRotation(rotation);
        }

    }

    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        View dropView = this.mWaterDropView;
        View imageView = this.mImageView;
        this.mState = newState;
        switch (newState) {
            case None:
                dropView.setVisibility(VISIBLE);
                imageView.setVisibility(VISIBLE);
                break;
            case PullDownToRefresh:
                dropView.setVisibility(VISIBLE);
                imageView.setVisibility(VISIBLE);
            case PullDownCanceled:
            case Refreshing:
            default:
                break;
            case ReleaseToRefresh:
                dropView.setVisibility(VISIBLE);
                imageView.setVisibility(VISIBLE);
                break;
            case RefreshFinish:
                dropView.setVisibility(GONE);
                imageView.setVisibility(GONE);
        }

    }

    public void onReleased(@NonNull RefreshLayout layout, int height, int maxDragHeight) {
        View imageView = this.mImageView;
        final View dropView = this.mWaterDropView;
        this.mProgressDrawable.start();
        imageView.setVisibility(GONE);
        this.mWaterDropView.createAnimator().start();
        dropView.animate().setDuration(150L).alpha(0.0F).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                dropView.setVisibility(GONE);
                dropView.setAlpha(1.0F);
            }
        });
    }

    public int onFinish(@NonNull RefreshLayout layout, boolean success) {
        this.mProgressDrawable.stop();
        return 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setPrimaryColors(@ColorInt int... colors) {
        if (colors.length > 0) {
            this.mWaterDropView.setIndicatorColor(colors[0]);
        }

    }
}
