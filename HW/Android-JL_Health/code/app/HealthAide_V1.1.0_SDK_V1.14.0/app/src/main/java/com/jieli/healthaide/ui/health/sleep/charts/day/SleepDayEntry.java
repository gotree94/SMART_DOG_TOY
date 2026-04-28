package com.jieli.healthaide.ui.health.sleep.charts.day;

import android.annotation.SuppressLint;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/3/21 3:07 PM
 * @desc :
 */
public class SleepDayEntry extends Entry {

    public long startTime;
    public long endTime;
    public static long minTime;
    public static long maxTime;

    public int type;
    public int color;

    public SleepDayEntry(long startTime, long endTime, int type, int color) {
        super(toX((endTime - startTime) / 2 + startTime), type);
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setType(int type) {
        this.type = type;
        setY(type);
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;

    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        setX(startTime);
    }


    public float xMin() {
        return toX(startTime);
    }

    public float xMax() {
        return toX(endTime);
    }

    /**
     * 时间戳转换为x轴坐标
     *
     * @param time
     * @return
     */
    public static float toX(long time) {
        long div = time - minTime;
        div /= 1000;//s
        return div;
    }

    /**
     * x轴坐标转换为时间戳
     * @param x
     * @return
     */
    public static long fromX(float x) {
        long time = (long) x;
        time *= 1000;//ms
        time += minTime;
        return time;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public String toString() {
        return "SleepDayEntry{" +
                "startTime=" + new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss").format(new Date(startTime)) +
                ", endTime=" + new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss").format(new Date(endTime)) +
                ", type=" + type +
                ", color=" + color +
                '}';
    }
}
