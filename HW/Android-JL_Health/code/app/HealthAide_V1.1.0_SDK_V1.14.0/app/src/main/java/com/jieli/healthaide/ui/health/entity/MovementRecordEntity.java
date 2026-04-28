package com.jieli.healthaide.ui.health.entity;

/**
 * @ClassName: MovementRecord
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:29
 */
public class MovementRecordEntity extends HealthMultipleEntity {
    public int movementType;
    public String dateTag;
    public double distance;

    public MovementRecordEntity() {
        setType(TYPE_MOVEMENT_RECORD);
    }
}
