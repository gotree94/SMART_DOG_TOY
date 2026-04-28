package com.jieli.healthaide.data.vo.parse;

import androidx.annotation.NonNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/31/21
 * @desc :
 */
public class ParseEntityFloat {
    private long startTime;
    private float value;
    private long endTime;

    public ParseEntityFloat(long startTime, long endTime , float value) {
        this.startTime = startTime;
        this.value = value;
        this.endTime = endTime;
    }

    public ParseEntityFloat(long startTime, float value ) {
        this.startTime = startTime;
        this.value = value;

    }

    public ParseEntityFloat() {
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "ParseEntity{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", value=" + value +
                '}';
    }
}
