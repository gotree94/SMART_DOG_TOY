package com.jieli.healthaide.ui.health.blood_oxygen.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @ClassName: AnalysisEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 17:07
 */
public class AnalysisEntity implements MultiItemEntity {
    private int itemType = 0;
    private String firstAnalysisValue;
    private String firstAnalysisDescribe;
    private String secondAnalysisValue;
    private String secondAnalysisDescribe;

    public String getFirstAnalysisValue() {
        return firstAnalysisValue;
    }

    public void setFirstAnalysisValue(String firstAnalysisValue) {
        this.firstAnalysisValue = firstAnalysisValue;
    }

    public String getFirstAnalysisDescribe() {
        return firstAnalysisDescribe;
    }

    public void setFirstAnalysisDescribe(String firstAnalysisDescribe) {
        this.firstAnalysisDescribe = firstAnalysisDescribe;
    }

    public String getSecondAnalysisValue() {
        return secondAnalysisValue;
    }

    public void setSecondAnalysisValue(String secondAnalysisValue) {
        this.secondAnalysisValue = secondAnalysisValue;
    }

    public String getSecondAnalysisDescribe() {
        return secondAnalysisDescribe;
    }

    public void setSecondAnalysisDescribe(String secondAnalysisDescribe) {
        this.secondAnalysisDescribe = secondAnalysisDescribe;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
