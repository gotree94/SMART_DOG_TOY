package com.jieli.healthaide.ui.health.entity;

import com.jieli.healthaide.R;

/**
 * @ClassName: PressureEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:32
 */
public class PressureEntity extends HealthMultipleEntity {
    //todo 演示数据，后续清零
    private int pressure = 0;//压力
    private int pressureStateSrc = analysisStatus(0);//压力状态
    long leftTime = 0;

    public PressureEntity() {
        setType(TYPE_PRESSURE);
    }

    public int getPressure() {
        return pressure;
    }

    public int getPressureStateSrc() {
        return pressureStateSrc;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
        pressureStateSrc = analysisStatus(pressure);
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    public long getLeftTime() {
        return leftTime;
    }

    private int analysisStatus(int pressureValue) {
        int status;
        if (pressureValue < 1) {
            status = 0;
        } else if (1 <= pressureValue && pressureValue < 30) {
            status = R.string.relax;
        } else if (30 <= pressureValue && pressureValue < 60) {
            status = R.string.normal;
        } else if (60 <= pressureValue && pressureValue < 80) {
            status = R.string.medium;
        } else {
            status = R.string.uptilted;
        }
        return status;
    }
}
