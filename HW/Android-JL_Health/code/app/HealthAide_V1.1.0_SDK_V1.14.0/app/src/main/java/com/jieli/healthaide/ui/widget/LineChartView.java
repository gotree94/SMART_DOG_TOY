package com.jieli.healthaide.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhpan on 2017/3/14.
 */

public class LineChartView extends View {
    private static final String TAG = LineChartView.class.getSimpleName();
    private float xOrigin; //  x轴原点坐标
    private float yOrigin;  //  y轴原点坐标
    private int mMargin10;  //  10dp的间距
    private int mWidth; //  控件宽度
    private int mHeight;  //  控件高度
    private int max = 220, min = 0;  //  最大值、最小值
    private float yInterval;  //  y轴坐标间隔
    private float xInterval;  //  x轴坐标间隔
    private final int copiesNumber = 1440; //x轴分为1440份，一分钟一份
    private final int judgeDataStopIntervalX = 12;//超过12分钟没有一次数据认为，数据中断
    private int selectedTimeStamp = 0;
    private List<ItemBean> mItems;//  折线数据

    private int[] shadeColors; //  渐变阴影颜色

    private int mLineColor;  //  折线颜色

    private Paint mPaintLine;   //  折线画笔
    private Path mPath;   //    折线路径
    private Paint mPaintShader; //  渐变色画笔
    private Path mPathShader;   //  渐变色路径
    private Paint mPainCircle; //   单个数据圆画笔
    private Paint mPainSelectedCircle; //   选中的数据画笔

    private float mSelectedCircleRadius;//选中的数据圆半径

    public int[] getShadeColors() {
        return shadeColors;
    }

    public void setShadeColors(int[] shadeColors) {
        this.shadeColors = shadeColors;
    }

    public List<ItemBean> getItems() {
        return mItems;
    }

    public void setItems(List<ItemBean> items) {
        mItems = items;
    }

    public int getSelectedTimeStamp() {
        return selectedTimeStamp;
    }

    public void setSelectedTimeStamp(int selectedTimeStamp) {
        this.selectedTimeStamp = selectedTimeStamp;
    }

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineChartView);
        mLineColor = typedArray.getColor(R.styleable.LineChartView_lcv_line_color, Color.RED);
        typedArray.recycle();
        //  初始化渐变色
        shadeColors = new int[]{
                Color.argb(100, 255, 86, 86), Color.argb(15, 255, 86, 86),
                Color.argb(0, 255, 86, 86)};
        //  初始化折线数据
        mItems = new ArrayList<>();
        mMargin10 = ValueUtil.dp2px(context, 0);
        init();
    }

    private void init() {
        //  初始化折线的画笔
        mPaintLine = new Paint();
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStrokeWidth(ValueUtil.dp2px(getContext(), 2));
        mPaintLine.setColor(mLineColor);

        //  阴影画笔
        mPaintShader = new Paint();
        mPaintShader.setAntiAlias(true);
        mPaintShader.setStrokeWidth(2f);

        mPainCircle = new Paint();
        mPainCircle.setStyle(Paint.Style.FILL);
        mPainCircle.setColor(0xFFFFFFFF);
        mPainCircle.setStrokeWidth(ValueUtil.dp2px(getContext(), 1));

        mPainSelectedCircle = new Paint();
        mPainSelectedCircle.setStyle(Paint.Style.FILL);
        mPainSelectedCircle.setColor(0xFFA3D07D);
        mPainSelectedCircle.setStrokeWidth(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            //  初始化原点坐标
            xOrigin = mMargin10;
            yOrigin = (mHeight /*- mTextSize*/ - mMargin10);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //  Y轴间距
        yInterval = (max - min) / (yOrigin);
        xInterval = (mWidth - xOrigin) / (copiesNumber/*mItems.size() - 1*/);
        //  画折线
        drawLine(canvas);
    }

    private void drawLine(Canvas canvas) {
        JL_Log.d(TAG, "drawLine", "drawSubsectionLine: ");
        //  画坐标点
        int lastTimeStamp = 0;
        float lastX = 0;
        //  渐变阴影
        Shader mShader = new LinearGradient(0, 0, 0, getHeight(), shadeColors, null, Shader.TileMode.CLAMP);
        mPaintShader.setShader(mShader);
        mPath = null;
        mPathShader = null;
        float x = 0;
        boolean isExistSelectedData = false;
        float xSelected = 0;
        float ySelected = 0;
        for (ItemBean itemBean : mItems) {
            int currentTimeStamp = (int) itemBean.getTimestamp();
            x = currentTimeStamp * xInterval + xOrigin;
            if (currentTimeStamp == selectedTimeStamp) {//todo 用近似的方法,提高使用感
                isExistSelectedData = true;
                xSelected = x;
                ySelected = yOrigin - (itemBean.getValue() - min) / yInterval;
            }
        }
        if (isExistSelectedData) {//选中的
            canvas.drawCircle(xSelected, ySelected, ValueUtil.dp2px(getContext(), 3), mPainSelectedCircle);
        }
    }

    private void drawSubsection(Canvas canvas, Path path, Path pathShader) {
        JL_Log.d(TAG, "drawSubsection", "");
        canvas.drawPath(path, mPaintLine);
        canvas.drawPath(pathShader, mPaintShader);
    }

    private boolean judgeDataContinuity(int prevTimeStamp, int currentTimeStamp, int nextTimeStamp) {
        return (Math.abs(prevTimeStamp - currentTimeStamp) < judgeDataStopIntervalX || Math.abs(currentTimeStamp - nextTimeStamp) < judgeDataStopIntervalX);
    }

    public static String timeStampToString(Long num) {
        Timestamp ts = new Timestamp(num * 1000);
        DateFormat sdf = CustomTimeFormatUtil.dateFormat("yyyy-MM-dd");
        return sdf.format(ts);
    }

    //  折线数据的实体类
    public static class ItemBean {

        private long Timestamp;
        private int value;

        public ItemBean() {
        }


        public ItemBean(long timestamp, int value) {
            super();
            Timestamp = timestamp;
            this.value = value;
        }

        public long getTimestamp() {
            return Timestamp;
        }

        public void setTimestamp(long timestamp) {
            Timestamp = timestamp;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

    }
}
