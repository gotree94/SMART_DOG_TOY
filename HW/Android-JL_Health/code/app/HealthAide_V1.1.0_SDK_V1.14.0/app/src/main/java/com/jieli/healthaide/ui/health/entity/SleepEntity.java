package com.jieli.healthaide.ui.health.entity;

import java.util.ArrayList;

/**
 * @ClassName: SleepEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:31
 */
public class SleepEntity extends HealthMultipleEntity {
    private boolean isEmpty = true;
    ArrayList<Object> dataArray = new ArrayList<>();//睡眠的数据
    private int deepSleepRatio = 0;
    private int lightSleepRatio = 0;
    private int rapidEyeMovementRatio = 0;
    private int soberRatio = 0;
    private int napRatio = 0;
    private int hour = 0;
    private int min = 0;
    private long leftTime = 0;
    private long rightTime = 0;

    public SleepEntity() {
        setType(TYPE_SLEEP);
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public int getDeepSleepRatio() {
        return deepSleepRatio;
    }

    public int getLightSleepRatio() {
        return lightSleepRatio;
    }

    public int getRapidEyeMovementRatio() {
        return rapidEyeMovementRatio;
    }

    public int getSoberRatio() {
        return soberRatio;
    }

    public int getNapRatio() {
        return napRatio;
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public long getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    public void setRightTime(long rightTime) {
        this.rightTime = rightTime;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public void setDataArray(ArrayList<Object> dataArray) {
        this.dataArray = dataArray;
    }

    public void setDeepSleepRatio(int deepSleepRatio) {
        this.deepSleepRatio = deepSleepRatio;
    }

    public void setLightSleepRatio(int lightSleepRatio) {
        this.lightSleepRatio = lightSleepRatio;
    }

    public void setRapidEyeMovementRatio(int rapidEyeMovementRatio) {
        this.rapidEyeMovementRatio = rapidEyeMovementRatio;
    }

    public void setNapRatio(int napRatio) {
        this.napRatio = napRatio;
    }

    public void setSoberRatio(int soberRatio) {
        this.soberRatio = soberRatio;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMin(int min) {
        this.min = min;
    }
}
