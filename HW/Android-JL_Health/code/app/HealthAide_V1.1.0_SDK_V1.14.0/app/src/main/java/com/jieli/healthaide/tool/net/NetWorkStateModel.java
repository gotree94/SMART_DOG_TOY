package com.jieli.healthaide.tool.net;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/22/21 10:57 AM
 * @desc :
 */
public class NetWorkStateModel {
    private int type;

    private boolean available;

    public NetWorkStateModel(int type, boolean available) {
        this.type = type;
        this.available = available;
    }

    public NetWorkStateModel() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "NetWorkStateModel{" +
                "type=" + type +
                ", available=" + available +
                '}';
    }
}
