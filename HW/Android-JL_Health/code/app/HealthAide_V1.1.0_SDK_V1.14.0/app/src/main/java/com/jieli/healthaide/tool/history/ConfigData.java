package com.jieli.healthaide.tool.history;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 通用配置
 * @since 2021/7/20
 */
public class ConfigData {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
