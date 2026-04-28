package com.jieli.healthaide.ui.health.weight.entity;

/**
 * @ClassName: AnalysisEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 17:07
 */
public class AnalysisDayEntity extends AnalysisMultipleBaseEntity {
    private String analysisDescribe;
    private String analysisValue = null;
    private int analysisIconSrc;

    public AnalysisDayEntity() {
        setType(TYPE_ONE);
    }

    public String getAnalysisValue() {
        return analysisValue;
    }

    public void setAnalysisValue(String analysisValue) {
        this.analysisValue = analysisValue;
    }

    public String getAnalysisDescribe() {
        return analysisDescribe;
    }

    public void setAnalysisDescribe(String analysisDescribe) {
        this.analysisDescribe = analysisDescribe;
    }

    public int getAnalysisIconSrc() {
        return analysisIconSrc;
    }

    public void setAnalysisIconSrc(int analysisIconSrc) {
        this.analysisIconSrc = analysisIconSrc;
    }
}
