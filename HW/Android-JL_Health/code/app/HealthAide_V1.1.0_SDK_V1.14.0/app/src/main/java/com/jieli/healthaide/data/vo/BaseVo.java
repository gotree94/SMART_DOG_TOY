package com.jieli.healthaide.data.vo;

import com.jieli.healthaide.data.entity.HealthEntity;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/31/21
 * @desc :
 */
public abstract class BaseVo {
    private List<HealthEntity> healthEntities;
    protected long startTime;
    protected long endTime;

    public List<HealthEntity> getHealthEntities() {
        return healthEntities;
    }

    public void setHealthEntities(List<HealthEntity> healthEntities) {
        this.healthEntities = healthEntities;
        parse(healthEntities);
    }

    /**
     * 在子类解析数据
     *
     * @param healthEntities
     */
    protected abstract void parse(List<HealthEntity> healthEntities);

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

    public abstract List<HealthEntity> createTestData(long startTime, long endTime);

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }


    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }
}
