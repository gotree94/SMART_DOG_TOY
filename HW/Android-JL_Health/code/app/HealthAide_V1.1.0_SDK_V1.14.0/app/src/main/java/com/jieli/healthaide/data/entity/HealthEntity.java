package com.jieli.healthaide.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;
import com.jieli.jl_rcsp.util.CHexConver;

import java.util.Calendar;
import java.util.Date;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/27/21
 * @desc :
 */
@Entity
public class HealthEntity {

    public static final byte DATA_TYPE_HEART_RATE = QueryFileTask.TYPE_HEART_RATE;
    public static final byte DATA_TYPE_GAS_PRESSURE = (byte) 0xf1;
    public static final byte DATA_TYPE_ALTITUDE = (byte) 0xf2;
    public static final byte DATA_TYPE_STEP = QueryFileTask.TYPE_STEP;
    public static final byte DATA_TYPE_PRESSURE = (byte) 0xf4;
    public static final byte DATA_TYPE_BLOOD_OXYGEN = QueryFileTask.TYPE_BLOOD_OXYGEN;
    public static final byte DATA_TYPE_TRAINING_LOAD = (byte) 0xf6;
    public static final byte DATA_TYPE_VO2MAX = (byte) 0xf7;
    public static final byte DATA_TYPE_EXERCISE_RECOVERY_TIME = (byte) 0xF8;
    public static final byte DATA_TYPE_SPORT_INFO = (byte) 0xf9;
    public static final byte DATA_TYPE_SLEEP = QueryFileTask.TYPE_SLEEP;
    public static final byte DATA_TYPE_WEIGHT = (byte) 0xFF;


    @PrimaryKey(autoGenerate = true)
    private long id; //唯一id

    @NonNull
    private String uid;//用户唯一id

    private byte type; //数据类型

    private byte version;  //版本

    private long time; //开始时间

    private byte[] crcCode;//crc校验码

    private byte space; //间隔

    private boolean sync; //是否同步到服务器

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    @NonNull
    private byte[] data; //数据


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public byte[] getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(byte[] crcCode) {
        this.crcCode = crcCode;
    }

    public byte getSpace() {
        return space;
    }

    public void setSpace(byte space) {
        this.space = space;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public static HealthEntity from(byte[] dayData) {
        if (dayData == null || dayData.length < 7) return null;
        HealthEntity healthEntity = new HealthEntity();
        Byte type = dayData[0];//todo 考虑使用哪个type，使用文件
        if (type == null) return null;
        int year = CHexConver.bytesToInt(dayData[1], dayData[2]);
        int month = Math.max((0xff & dayData[3])-1,0);
        int day = 0xff & dayData[4];
        byte[] crcCode = new byte[2];
        System.arraycopy(dayData, 5, crcCode, 0, crcCode.length);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        long id = CalendarUtil.removeTime(calendar.getTimeInMillis()) * 1000 + type;
        healthEntity.setId(id);
        healthEntity.setType(type);
        healthEntity.setTime(calendar.getTimeInMillis());
        healthEntity.setCrcCode(crcCode);
        healthEntity.setData(dayData);
        if (dayData.length >= 9) {
            byte version = dayData[7];
            byte space = dayData[8];
            healthEntity.setVersion(version);
            healthEntity.setSpace(space);
        }
        return healthEntity;
    }

    @Override
    public String toString() {
        return "HealthEntity{" +
                "id=" + id +
                ", type=" + type +
                ", time=" + time +
                ", time=" + CalendarUtil.serverDateFormat().format(new Date(time)) +
                ", crcCode=" + CHexConver.byte2HexStr(crcCode) +
                ", space=" + space +
                ", version=" + version +
                ", uid=" + uid +
                ", data=" + CHexConver.byte2HexStr(data) +
                '}';
    }
}
