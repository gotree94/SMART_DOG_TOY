package com.jieli.healthaide.ui.device.market.bean;

import android.text.TextUtils;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 头部元素
 * @since 2022/6/16
 */
public class HeadEntity {
    private final int type;
    private String title;
    private String value;
    private int valueIcon;

    public static final int HEAD_TYPE_FREE = 0;
    public static final int HEAD_TYPE_PAY = 1;

    public HeadEntity(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getValueIcon() {
        return valueIcon;
    }

    public void setValueIcon(int valueIcon) {
        this.valueIcon = valueIcon;
    }

    public boolean isHasValue() {
        return !TextUtils.isEmpty(value);
    }

    @Override
    public String toString() {
        return "HeadEntity{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", value='" + value + '\'' +
                ", valueIcon=" + valueIcon +
                '}';
    }
}
