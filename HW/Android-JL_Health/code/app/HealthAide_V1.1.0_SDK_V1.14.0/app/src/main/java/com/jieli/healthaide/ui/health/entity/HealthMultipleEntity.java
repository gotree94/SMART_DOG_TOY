package com.jieli.healthaide.ui.health.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @ClassName: MultipleItemQuickEntity
 * @Description: 具体的数据类继承这个抽象类
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/1/28 11:11
 */
public abstract class HealthMultipleEntity implements MultiItemEntity {
    public static final int TYPE_MOVEMENT_RECORD = 1;
    public static final int TYPE_HEART_RATE = 2;
    public static final int TYPE_SLEEP = 3;
    public static final int TYPE_WEIGHT = 4;
    public static final int TYPE_PRESSURE = 5;
    public static final int TYPE_BLOOD_OXYGEN = 6;

    protected int type;

    protected void setType(int type) {
        this.type = type;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
