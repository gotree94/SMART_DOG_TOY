package com.jieli.healthaide.ui.health.weight.entity;

/**
 * @ClassName: AnalysisEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 17:07
 */
public class AnalysisWeekEntity extends AnalysisMultipleBaseEntity {
    private String analysisDescribe;
    private String analysisValue = null;
    private String analysisUnit;

    public AnalysisWeekEntity() {
        setType(TYPE_TWO);
    }

    public String getAnalysisDescribe() {
        return analysisDescribe;
    }

    public void setAnalysisDescribe(String analysisDescribe) {
        this.analysisDescribe = analysisDescribe;
    }

    public String getAnalysisValue() {
        return analysisValue;
    }

    public void setAnalysisValue(String analysisValue) {
        this.analysisValue = analysisValue;
    }

    public String getAnalysisUnit() {
        return analysisUnit;
    }

    public void setAnalysisUnit(String analysisUnit) {
        this.analysisUnit = analysisUnit;
    }
}
