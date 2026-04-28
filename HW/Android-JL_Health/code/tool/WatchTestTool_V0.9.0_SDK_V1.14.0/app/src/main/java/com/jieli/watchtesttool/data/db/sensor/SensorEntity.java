package com.jieli.watchtesttool.data.db.sensor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jieli.jl_rcsp.util.CHexConver;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/21
 * @desc :
 */
@Entity
public class SensorEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String devName;
    private String type;
    private String mac;
    private byte[] data;
    private long time;


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "SensorEntity{" +
                "id=" + id +
                ", devName='" + devName + '\'' +
                ", type='" + type + '\'' +
                ", mac='" + mac + '\'' +
                ", data=" + CHexConver.byte2HexStr(data) +
                ", time=" + time +
                '}';
    }
}
