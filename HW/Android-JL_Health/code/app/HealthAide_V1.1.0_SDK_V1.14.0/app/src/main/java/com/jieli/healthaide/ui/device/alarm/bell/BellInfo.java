package com.jieli.healthaide.ui.device.alarm.bell;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2020/9/2 9:57 AM
 * @desc :
 */
public class BellInfo {

    private boolean selected;

    private byte type;
    private byte dev;
    private int cluster;
    private String name;


    public BellInfo() {
    }

    public BellInfo(int cluster, String name, boolean selected) {
        this.name = name;
        this.cluster = cluster;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public void setType(byte type) {
        this.type = type;
    }

    public void setDev(byte dev) {
        this.dev = dev;
    }

    public byte getType() {
        return type;
    }

    public byte getDev() {
        return dev;
    }

    @Override
    public String toString() {
        return "DefaultAlarmBell{" +
                "name='" + name + '\'' +
                ", index=" + cluster +
                ", selected=" + selected +
                '}';
    }
}
