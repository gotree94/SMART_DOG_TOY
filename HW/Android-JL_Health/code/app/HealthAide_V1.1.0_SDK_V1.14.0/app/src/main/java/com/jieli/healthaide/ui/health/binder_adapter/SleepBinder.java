package com.jieli.healthaide.ui.health.binder_adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chad.library.adapter.base.binder.BaseItemBinder;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.health.entity.SleepEntity;
import com.jieli.healthaide.ui.widget.CalenderSelectorView;
import com.jieli.healthaide.util.CustomTimeFormatUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @ClassName: BloodOxygenBinder
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:37
 */
public class SleepBinder extends BaseItemBinder<SleepEntity, BaseViewHolder> {
    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, SleepEntity sleepEntity) {
        baseViewHolder.setVisible(R.id.cl_health_sleep_diagram, !sleepEntity.isEmpty());
        baseViewHolder.setVisible(R.id.tv_health_empty, sleepEntity.isEmpty());
        baseViewHolder.setText(R.id.tv_hour_value, sleepEntity.isEmpty() ? "-" : String.valueOf(sleepEntity.getHour()));
        baseViewHolder.setText(R.id.tv_min_value, sleepEntity.isEmpty() ? "-" : String.valueOf(sleepEntity.getMin()));
        baseViewHolder.setText(R.id.tv_health_date, sleepEntity.isEmpty() ? getContext().getString(R.string.empty_date) : CustomTimeFormatUtil.getTimeInterval(sleepEntity.getLeftTime(), 1, CalenderSelectorView.TYPE_WEEK));
        drawSleepChart(baseViewHolder, sleepEntity);
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        View parentView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_health_sleep, viewGroup, false);
        addHadDataView(parentView);
        return new BaseViewHolder(parentView);
    }

    private void addHadDataView(View view) {
        ConstraintLayout constraintLayout = new ConstraintLayout(getContext());
        constraintLayout.setId(R.id.cl_health_sleep_diagram);
        constraintLayout.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams layoutParamsGroup = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsGroup.topMargin = ValueUtil.dp2px(getContext(), 16);
        layoutParamsGroup.topToBottom = R.id.chart_preview_sleep;
        layoutParamsGroup.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        ConstraintLayout parentLayout = (ConstraintLayout) view;
        parentLayout.addView(constraintLayout, layoutParamsGroup);
        TextView tvDiagram1 = new TextView(getContext());
        TextView tvDiagram2 = new TextView(getContext());
        TextView tvDiagram3 = new TextView(getContext());
        TextView tvDiagram4 = new TextView(getContext());
        TextView tvDiagram5 = new TextView(getContext());
        {//文字部分
            tvDiagram1.setId(R.id.tv_health_sleep_diagram_1);
            tvDiagram2.setId(R.id.tv_health_sleep_diagram_2);
            tvDiagram3.setId(R.id.tv_health_sleep_diagram_3);
            tvDiagram4.setId(R.id.tv_health_sleep_diagram_4);
            tvDiagram5.setId(R.id.tv_health_sleep_diagram_5);
            tvDiagram1.setGravity(Gravity.CENTER);
            tvDiagram2.setGravity(Gravity.CENTER);
            tvDiagram3.setGravity(Gravity.CENTER);
            tvDiagram4.setGravity(Gravity.CENTER);
            tvDiagram5.setGravity(Gravity.CENTER);
            tvDiagram1.setCompoundDrawables(getShapeDrawable(R.color.yellow_F2C45A), null, null, null);
            tvDiagram2.setCompoundDrawables(getShapeDrawable(R.color.red_F6BCA9), null, null, null);
            tvDiagram3.setCompoundDrawables(getShapeDrawable(R.color.red_F39696), null, null, null);
            tvDiagram4.setCompoundDrawables(getShapeDrawable(R.color.purple_D19DF7), null, null, null);
            tvDiagram5.setCompoundDrawables(getShapeDrawable(R.color.purple_8856F8), null, null, null);
            tvDiagram1.setCompoundDrawablePadding(3);
            tvDiagram2.setCompoundDrawablePadding(3);
            tvDiagram3.setCompoundDrawablePadding(3);
            tvDiagram4.setCompoundDrawablePadding(3);
            tvDiagram5.setCompoundDrawablePadding(3);
            tvDiagram1.setTextSize(12);
            tvDiagram2.setTextSize(12);
            tvDiagram3.setTextSize(12);
            tvDiagram4.setTextSize(12);
            tvDiagram5.setTextSize(12);
            tvDiagram1.setTextColor(getContext().getResources().getColor(R.color.gray_9E9E9E));
            tvDiagram2.setTextColor(getContext().getResources().getColor(R.color.gray_9E9E9E));
            tvDiagram3.setTextColor(getContext().getResources().getColor(R.color.gray_9E9E9E));
            tvDiagram4.setTextColor(getContext().getResources().getColor(R.color.gray_9E9E9E));
            tvDiagram5.setTextColor(getContext().getResources().getColor(R.color.gray_9E9E9E));
            tvDiagram1.setText(R.string.sober);
            tvDiagram2.setText(R.string.rapid_eye_movement);
            tvDiagram3.setText(R.string.sleep_nap);
            tvDiagram4.setText(R.string.light_sleep);
            tvDiagram5.setText(R.string.deep_sleep);
        }
        ConstraintLayout.LayoutParams layoutParams1 = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams1.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams1.endToStart = R.id.tv_health_sleep_diagram_2;
        ConstraintLayout.LayoutParams layoutParams2 = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams2.topToTop = R.id.tv_health_sleep_diagram_1;
        layoutParams2.startToEnd = R.id.tv_health_sleep_diagram_1;
        layoutParams2.endToStart = R.id.tv_health_sleep_diagram_3;
        ConstraintLayout.LayoutParams layoutParams3 = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams3.topToTop = R.id.tv_health_sleep_diagram_1;
        layoutParams3.startToEnd = R.id.tv_health_sleep_diagram_2;
        layoutParams3.endToStart = R.id.tv_health_sleep_diagram_4;
        ConstraintLayout.LayoutParams layoutParams4 = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams4.topToTop = R.id.tv_health_sleep_diagram_1;
        layoutParams4.startToEnd = R.id.tv_health_sleep_diagram_3;
        layoutParams4.endToStart = R.id.tv_health_sleep_diagram_5;
        ConstraintLayout.LayoutParams layoutParams5 = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams5.topToTop = R.id.tv_health_sleep_diagram_1;
        layoutParams5.startToEnd = R.id.tv_health_sleep_diagram_4;
        layoutParams5.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        constraintLayout.addView(tvDiagram1, layoutParams1);
        constraintLayout.addView(tvDiagram2, layoutParams2);
        constraintLayout.addView(tvDiagram3, layoutParams3);
        constraintLayout.addView(tvDiagram4, layoutParams4);
        constraintLayout.addView(tvDiagram5, layoutParams5);
    }

    /**
     * @param fillColorSrc 填充的颜色值Src
     * @return 一个Drawable
     * @description 获取一个正方形的图例（10dp*10dp）
     */
    private ShapeDrawable getShapeDrawable(int fillColorSrc) {
        int fillColor = getContext().getResources().getColor(fillColorSrc);//填充颜色
        int slideLength = ValueUtil.dp2px(getContext(), 10);//边长长度
        int roundArc = ValueUtil.dp2px(getContext(), 2);//圆角角度
        float[] outerRadii = {roundArc, roundArc, roundArc, roundArc, roundArc, roundArc, roundArc, roundArc};//外矩形        左上、右上、右下、左下的圆角半径
        RoundRectShape roundRectShape = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);//用于防止边缘的锯齿
        paint.setColor(fillColor);//设置颜色
        paint.setStyle(Paint.Style.FILL);//设置样式为空心矩形
        paint.setStrokeWidth(2.5f);//设置空心矩形边框的宽度
        shapeDrawable.setBounds(0, 0, slideLength, slideLength);
        return shapeDrawable;
    }

    /**
     * @param
     * @return
     * @description 描绘一个睡眠的简述图表
     */
    private void drawSleepChart(BaseViewHolder baseViewHolder, SleepEntity sleepEntity) {
        ImageView imageView = baseViewHolder.findView(R.id.chart_preview_sleep);
        final ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                imageView.setBackgroundDrawable(new RectDiagram(imageView.getWidth(), imageView.getHeight(), getContext()
                        , sleepEntity.getSoberRatio()
                        , sleepEntity.getRapidEyeMovementRatio()
                        , sleepEntity.getNapRatio()
                        , sleepEntity.getLightSleepRatio()
                        , sleepEntity.getDeepSleepRatio()
                       ));
            }
        });
    }

    private class RectDiagram extends Drawable {
        int width = 0;
        int height = 0;
        Paint paint = new Paint();
        int roundArc = ValueUtil.dp2px(getContext(), 2);//圆角
        int margin = ValueUtil.dp2px(getContext(), 2);//rect间距
        Context mContext;

        int partColorEmpty = getContext().getResources().getColor(R.color.purple_B9A4F0);
        boolean isEmpty = false;
        ArrayList<Integer> partRatioList = new ArrayList<>();//睡眠质量部分占比List
        ArrayList<Integer> partWidthList = new ArrayList<>();//部分宽度List
        ArrayList<Integer> partColorList = new ArrayList<>();//部分颜色List

        RectDiagram(int width, int height, Context context, int partRatio1, int partRatio2, int partRatio3, int partRatio4, int partRatio5) {
            Log.d("zHM", "RectDiagram: "+partRatio5);
            this.mContext = context;
            this.height = height;
            this.width = width;
            int partColor1 = getContext().getResources().getColor(R.color.yellow_F2C45A);
            int partColor2 = getContext().getResources().getColor(R.color.red_F6BCA9);
            int partColor3 = getContext().getResources().getColor(R.color.red_F39696);
            int partColor4 = getContext().getResources().getColor(R.color.purple_D19DF7);
            int partColor5 = getContext().getResources().getColor(R.color.purple_8856F8);
            partColorList.add(partColor1);
            partColorList.add(partColor2);
            partColorList.add(partColor3);
            partColorList.add(partColor4);
            partColorList.add(partColor5);
            partRatioList.add(partRatio1);
            partRatioList.add(partRatio2);
            partRatioList.add(partRatio3);
            partRatioList.add(partRatio4);
            partRatioList.add(partRatio5);
            paint.setAntiAlias(true);//用于防止边缘的锯齿
            paint.setStyle(Paint.Style.FILL);//设置样式为空心矩形
            paint.setStrokeWidth(2.5f);//设置空心矩形边框的宽度
            calculationPartWidth();
        }

        private void calculationPartWidth() {
            int isNoEmptyNum = 0;
            int allPartRatioNum = 0;
            for (Integer partRatio : partRatioList) {
                if (partRatio != 0) {
                    isNoEmptyNum++;
                }
                allPartRatioNum = allPartRatioNum + partRatio;
            }
            if (isNoEmptyNum == 0) {//空的sleep数据
                isEmpty = true;
            } else {
                isEmpty = false;
                int marginNum = isNoEmptyNum - 1;
                int contentWidth = width - marginNum * margin;
                for (Integer partRatio : partRatioList) {
                    int partWidth = contentWidth * partRatio / allPartRatioNum;
                    partWidthList.add(partWidth);
                }
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (isEmpty) {
                paint.setColor(partColorEmpty);
                canvas.drawRoundRect(new RectF(0, 0, width, height), roundArc, roundArc, paint);
                return;
            }
            int tranX = 0;
            for (int i = 0; i < partWidthList.size(); i++) {
                int partWidth = partWidthList.get(i);
                if (partWidth != 0) {
                    paint.setColor(partColorList.get(i));
                    canvas.drawRoundRect(new RectF(tranX, 0, partWidth + tranX, height), roundArc, roundArc, paint);
                    tranX = tranX + partWidth + margin;
                }
            }
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }
}
