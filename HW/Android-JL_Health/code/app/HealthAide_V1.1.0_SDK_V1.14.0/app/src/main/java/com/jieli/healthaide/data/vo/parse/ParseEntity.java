package com.jieli.healthaide.data.vo.parse;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/31/21
 * @desc :
 */
public class ParseEntity {
    private long startTime;
    private double value;
    private long endTime;

    public ParseEntity(long startTime,  long endTime ,double value) {
        this.startTime = startTime;
        this.value = value;
        this.endTime = endTime;
    }

    public ParseEntity(long startTime, double value ) {
        this.startTime = startTime;
        this.value = value;

    }

    public ParseEntity() {
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ParseEntity{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", value=" + value +
                '}';
    }
}
