package com.jieli.watchtesttool.data.bean;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设置项
 * @since 2023/1/30
 */
public class SettingItem {
    private final int id;
    private final String name;

    public SettingItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
