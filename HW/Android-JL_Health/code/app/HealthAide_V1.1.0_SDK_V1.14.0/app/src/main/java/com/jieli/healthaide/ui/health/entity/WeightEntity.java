package com.jieli.healthaide.ui.health.entity;

import com.jieli.healthaide.tool.unit.BaseUnitConverter;

/**
 * @ClassName: WeightEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:32
 */
public class WeightEntity extends HealthMultipleEntity {
    //todo 演示数据，后续清零
    private float weight = 0f;//体重/kg
    long leftTime = 0;
    private int unitType = BaseUnitConverter.TYPE_METRIC;

    public WeightEntity() {
        setType(TYPE_WEIGHT);
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    public long getLeftTime() {
        return leftTime;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }
}
