package com.jieli.healthaide.ui.health.entity;

/**
 * @ClassName: BloodOxygenEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:34
 */
public class BloodOxygenEntity extends HealthMultipleEntity {
    private int bloodOxygen = 0;
    long leftTime = 0;

    public BloodOxygenEntity() {
        setType(TYPE_BLOOD_OXYGEN);
    }

    public int getBloodOxygen() {
        return bloodOxygen;
    }

    public void setBloodOxygen(int bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public long getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }
}