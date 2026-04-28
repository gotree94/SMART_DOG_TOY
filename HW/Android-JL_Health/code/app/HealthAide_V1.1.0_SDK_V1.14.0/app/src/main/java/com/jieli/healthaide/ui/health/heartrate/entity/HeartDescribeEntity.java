package com.jieli.healthaide.ui.health.heartrate.entity;

/**
 * @ClassName: HeartDescribeEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 8:49
 */
public class HeartDescribeEntity {
    private int iconSrc;
    private int entityTypeStringSrc;
    private String valueString;

    public HeartDescribeEntity(int iconSrc, int entityTypeStringSrc, String valueString) {
        this.iconSrc = iconSrc;
        this.entityTypeStringSrc = entityTypeStringSrc;
        this.valueString = valueString;
    }

    public int getIconSrc() {
        return iconSrc;
    }

    public void setIconSrc(int iconSrc) {
        this.iconSrc = iconSrc;
    }

    public int getEntityTypeStringSrc() {
        return entityTypeStringSrc;
    }

    public void setEntityTypeStringSrc(int entityTypeStringSrc) {
        this.entityTypeStringSrc = entityTypeStringSrc;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }
}
