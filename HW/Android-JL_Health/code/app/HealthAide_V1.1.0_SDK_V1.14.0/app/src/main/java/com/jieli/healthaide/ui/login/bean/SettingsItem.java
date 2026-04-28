package com.jieli.healthaide.ui.login.bean;

/**
 * 设置项
 *
 * @author zqjasonZhong
 * @since 2021/3/5
 */
public class SettingsItem {
    private String name;
    private String value;
    private boolean isHideIcon;
    private String imgPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isHideIcon() {
        return isHideIcon;
    }

    public void setHideIcon(boolean hideIcon) {
        isHideIcon = hideIcon;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    @Override
    public String toString() {
        return "SettingsItem{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", isHideIcon=" + isHideIcon +
                ", imgPath='" + imgPath + '\'' +
                '}';
    }
}
