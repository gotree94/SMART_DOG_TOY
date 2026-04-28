package com.jieli.healthaide.tool.history;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc Android私有配置
 * @since 2021/7/20
 */
public class AndroidConfigData {
    @SerializedName("sdk_type")
    private int sdkType;
    @SerializedName("way")
    private int connectWay;
    private String ble;

    public int getSdkType() {
        return sdkType;
    }

    public void setSdkType(int sdkType) {
        this.sdkType = sdkType;
    }

    public int getConnectWay() {
        return connectWay;
    }

    public void setConnectWay(int connectWay) {
        this.connectWay = connectWay;
    }

    public String getBle() {
        return ble;
    }

    public void setBle(String ble) {
        this.ble = ble;
    }

    @NonNull
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
