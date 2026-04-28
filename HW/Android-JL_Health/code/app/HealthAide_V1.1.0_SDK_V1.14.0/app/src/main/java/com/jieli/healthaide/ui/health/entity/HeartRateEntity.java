package com.jieli.healthaide.ui.health.entity;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * @ClassName: HeartRateEntity
 * @Description: 心率预览信息
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:31
 */
public class HeartRateEntity extends HealthMultipleEntity {
    public int count = 1440;//一天24*60 Min
    long leftTime = 0;//时间段的起始时间
    long rightTime = 0;//时间段的结束时间
    Object fillData = null;//填充的折线图的内容(类型 Fill)
    int lastHeartBeat = 0;//最新一次心率
    ArrayList<Entry> dataArray = new ArrayList<>();//折线的点数据

    public HeartRateEntity() {
        setType(TYPE_HEART_RATE);
    }

    public ArrayList<Entry> getData() {
        return dataArray;
    }

    public void setData(ArrayList<Entry> dataArray) {
        this.dataArray = dataArray;
    }
    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    public void setRightTime(long rightTime) {
        this.rightTime = rightTime;
    }

    public void setFillData(Object fillData) {
        this.fillData = fillData;
    }

    public int getLastHeartBeat() {
        return lastHeartBeat;
    }

    public void setLastHeartBeat(int lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    public long getLeftTime() {
        return leftTime;
    }

}
