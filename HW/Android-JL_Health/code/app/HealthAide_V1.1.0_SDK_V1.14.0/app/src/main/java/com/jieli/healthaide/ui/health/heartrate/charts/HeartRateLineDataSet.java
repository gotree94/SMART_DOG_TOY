package com.jieli.healthaide.ui.health.heartrate.charts;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * @ClassName: HeartRateLineDataSet
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/31 15:05
 */
public class HeartRateLineDataSet extends LineDataSet {
    private boolean drawSelectedCircleEnable = true;
    private int restingRate = 0;

    public HeartRateLineDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    public boolean isDrawSelectedCircleEnable() {
        return drawSelectedCircleEnable;
    }

    public void setDrawSelectedCircleEnable(boolean drawSelectedCircleEnable) {
        this.drawSelectedCircleEnable = drawSelectedCircleEnable;
    }

    public int getRestingRate() {
        return restingRate;
    }

    public void setRestingRate(int restingRate) {
        this.restingRate = restingRate;
    }
}
