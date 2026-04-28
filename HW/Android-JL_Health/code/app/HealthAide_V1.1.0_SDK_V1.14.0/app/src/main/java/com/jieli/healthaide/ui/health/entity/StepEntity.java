package com.jieli.healthaide.ui.health.entity;

import com.jieli.healthaide.tool.unit.BaseUnitConverter;

/**
 * @ClassName: StepEntity
 * @Description: 今日步数的数据Entity
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/6 15:12
 */
public class StepEntity {
    //todo 此处数据皆是演示数据，后续请清零
    private int steps = 0;//当前步数
    private int targetSteps = 10000;//目标步数
    private float distance = 0.00f;//距离/公里
    private int heatQuantity = 0;//热量/千卡
    private float height = 0f;//爬楼/米
    private int unitType = BaseUnitConverter.TYPE_METRIC;

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getTargetSteps() {
        return targetSteps;
    }

    public void setTargetSteps(int targetSteps) {
        this.targetSteps = targetSteps;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getHeatQuantity() {
        return heatQuantity;
    }

    public void setHeatQuantity(int heatQuantity) {
        this.heatQuantity = heatQuantity;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }
}
