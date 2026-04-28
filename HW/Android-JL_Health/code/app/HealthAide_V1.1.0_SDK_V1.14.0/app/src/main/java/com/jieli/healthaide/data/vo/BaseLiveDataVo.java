package com.jieli.healthaide.data.vo;

import androidx.lifecycle.LiveData;

import com.jieli.healthaide.data.entity.HealthEntity;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/31/21
 * @desc :
 */
public abstract class BaseLiveDataVo {
    private HealthEntity healthEntity;
    protected long startTime;
    protected long endTime;

    public HealthEntity getHealthEntity() {
        return healthEntity;
    }

    public void setHealthEntities(HealthEntity healthEntity) {
        this.healthEntity = healthEntity;
        parse(healthEntity);
    }

    /**
     * 在子类解析数据
     *
     * @param healthEntity
     */
    protected abstract void parse(HealthEntity healthEntity);

    /**
     * 数据类型
     *
     * @return
     */
    public abstract byte getType();

    /**
     * 生成模拟数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */

    public abstract LiveData<HealthEntity> createTestData(long startTime, long endTime);

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
