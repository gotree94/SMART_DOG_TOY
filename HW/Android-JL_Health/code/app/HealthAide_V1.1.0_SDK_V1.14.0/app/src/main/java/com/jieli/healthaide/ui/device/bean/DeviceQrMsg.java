package com.jieli.healthaide.ui.device.bean;

import com.google.gson.GsonBuilder;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备二维码信息
 * @since 2021/4/2
 */
public class DeviceQrMsg {
    /**
     * 设备名称
     */
    private String name;
    /**
     * BLE地址
     */
    private String bleAddr;
    /**
     * 经典蓝牙地址
     */
    private String edrAddr;
    /**
     * 厂商ID
     */
    private int vid;
    /**
     * 产品ID
     */
    private int pid;
    /**
     * 连接方式
     */
    private int connectWay = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBleAddr() {
        return bleAddr;
    }

    public void setBleAddr(String bleAddr) {
        this.bleAddr = bleAddr;
    }

    public String getEdrAddr() {
        return edrAddr;
    }

    public void setEdrAddr(String edrAddr) {
        this.edrAddr = edrAddr;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getConnectWay() {
        return connectWay;
    }

    public void setConnectWay(int connectWay) {
        this.connectWay = connectWay;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
