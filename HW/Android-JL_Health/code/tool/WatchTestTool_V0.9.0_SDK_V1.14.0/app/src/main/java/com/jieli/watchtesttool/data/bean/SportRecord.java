package com.jieli.watchtesttool.data.bean;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;


import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/8/21
 * @desc :
 */
@Entity(primaryKeys = {"uid", "startTime"})
public class SportRecord {

    public static final int TYPE_NONE = 0x00;
    public static final int TYPE_OUTDOOR = 0x01;
    public static final int TYPE_INDOOR = 0x02;
    @NonNull
    private String uid;
    private byte type;//运动模式
    private long startTime;//开始时间戳
    private byte internal;//间隔
    private byte version;//版本
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] reserve; //保留位 len =10
    private short duration;//时长 unit:s
    private long stopTime;//结束时间戳
    private short distance;//距离
    private short kcal;//热量
    private int step;//步数
    private short recoveryTime;//恢复时间
    private boolean sync; //是否已经同步到服务器

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] data; //原始数据


    @Ignore
    private List<Info> dataList;

    public SportRecord() {
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public byte getInternal() {
        return internal;
    }

    public void setInternal(byte internal) {
        this.internal = internal;
    }

    public byte[] getReserve() {
        return reserve;
    }

    public void setReserve(byte[] reserve) {
        this.reserve = reserve;
    }

    public short getDuration() {
        return duration;
    }

    public void setDuration(short duration) {
        this.duration = duration;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public short getDistance() {
        return distance;
    }

    public void setDistance(short distance) {
        this.distance = distance;
    }

    public short getKcal() {
        return kcal;
    }

    public void setKcal(short kcal) {
        this.kcal = kcal;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public short getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(short recoveryTime) {
        this.recoveryTime = recoveryTime;
    }


    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getVersion() {
        return version;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public boolean isSync() {
        return sync;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public static SportRecord from(byte[] data) {
        SportRecord sportRecord = new SportRecord();
        sportRecord.setData(data);


        int index = 0;
        //运动类型
        sportRecord.setType(data[index++]);
        //版本
        sportRecord.setVersion(data[index++]);
        //时间间隔
        int space = data[index++];
        sportRecord.setInternal((byte) space);
        space *= 1000;
        //保留位
        byte[] reversed = new byte[10];
        System.arraycopy(data, index, reversed, 0, reversed.length);
        sportRecord.setReserve(reversed);
        index += 10;
        System.out.println("SportRecord" + "\tspace = " + space + "\ttype = " + sportRecord.getType() + "\tversion = " + sportRecord.getVersion());
        List<Info> list = new ArrayList<>();

        long startTime = 0;
        for (; index < data.length; ) {
            byte flag = data[index++];
            byte len = data[index++];

            if (len < 1) {
                JL_Log.e("SportRecord", "error len  index = " + index + "\tlen = " + len);
                break;
            }
            if (flag == 0) {//开始包
                startTime = toTime(data, index);
                if (sportRecord.getStartTime() == 0) {
                    sportRecord.setStartTime(startTime);
                }
                System.out.println("startTime = " + CalendarUtil.serverDateFormat().format(new Date(startTime)));
            } else if (flag == 1 && index + 4 < data.length) {//基础数据包
                Info info = new Info();
                startTime += space;
                info.time = startTime;
                info.heart = data[index] & 0xff;
                info.stride = CHexConver.bytesToInt(data[index + 2], data[index + 1]);
                info.speed = CHexConver.bytesToInt(data[index + 4], data[index + 3]) * 0.01f;//km/小时
                //                info.speed = CHexConver.bytesToShort(data[index + 3], data[index + 2]);
                list.add(info);
                System.out.println("数据包 = " + info.toString());

            } else if (flag == 2) {//暂停包
                long pauseTime = toTime(data, index);
                sportRecord.setStopTime(pauseTime);
                System.out.println("pauseTime = " + CalendarUtil.serverDateFormat().format(new Date(pauseTime)));
            } else if (flag == (byte) 0xff) {//结束包
                long stopTime = toTime(data, index);
                sportRecord.setStopTime(stopTime);
                System.out.println("stopTime = " + CalendarUtil.serverDateFormat().format(new Date(stopTime)));
                index += len;//结束要加上偏移
                break;
            } else {
                System.out.println("SportRecord" + "\t错误flag = " + flag + "\tindex = " + String.format("0X%x", index));
            }
            index += len;
        }

        if (data.length < index + 14) {
            return sportRecord;
        }

        sportRecord.dataList = list;
        //运动时长
        sportRecord.setDuration(CHexConver.bytesToShort(data[index + 1], data[index]));
        index += 2;
        index += 4;//保留位
        //距离
        sportRecord.setDistance(CHexConver.bytesToShort(data[index + 1], data[index]));
        index += 2;
        //卡路里
        sportRecord.setKcal(CHexConver.bytesToShort(data[index + 1], data[index]));
        index += 2;
        //步数
        sportRecord.setStep(CHexConver.bytesLittleToInt(data, index, 4));
        index += 4;
        //恢复时间
        sportRecord.setRecoveryTime(CHexConver.bytesToShort(data[index + 1], data[index]));//
        return sportRecord;
    }

    public static SportRecord fromHeader(byte[] data) {
        SportRecord sportRecord = new SportRecord();
        sportRecord.setData(data);
        int index = 0;
        //运动类型
        sportRecord.setType(data[index++]);
        //版本
        sportRecord.setVersion(data[index++]);
        //时间间隔
        int space = data[index++];
        sportRecord.setInternal((byte) space);
        space *= 1000;
        //保留位
        byte[] reversed = new byte[10];
        System.arraycopy(data, index, reversed, 0, reversed.length);
        sportRecord.setReserve(reversed);
        index += 10;
        System.out.println("SportRecord" + "\tspace = " + space + "\ttype = " + sportRecord.getType() + "\tversion = " + sportRecord.getVersion());
        List<Info> list = new ArrayList<>();


        for (; index < data.length; ) {
            byte flag = data[index++];
            byte len = data[index++];
            if (len < 1) {
                JL_Log.e("SportRecord", "error len  index = " + index + "\tlen = " + len);
                break;
            }
            if (flag == 0) {//开始包
                long startTime = toTime(data, index);
                sportRecord.setStartTime(startTime);
                return sportRecord;
            }
            index += len;
        }

        return sportRecord;
    }


    private static long toTime(byte[] data, int start) {
        //小端数据转大段数据
        int tmp = CHexConver.bytesLittleToInt(data, start, 4);
        return RcspUtil.intToTime(tmp);
    }


    public static SportRecord test(Context context) throws IOException {
        InputStream is = context.getAssets().open("5678.SPT");
//        String outPath =context.getCacheDir().getPath() + File.separator + "record_temp_file";
//        InputStream is =new  FileInputStream(outPath);
        byte[] data = new byte[is.available()];
        is.read(data);
        SportRecord sportRecord = SportRecord.from(data);
        System.out.println(sportRecord.toString());
        return sportRecord;
//        for (BaseData baseData : sportRecord1.dataList) {
//            System.out.println(baseData.toString());
//        }
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = CalendarUtil.serverDateFormat();
        return "SportRecord{" +
                ", type=" + type +
                ", startTime=" + simpleDateFormat.format(new Date(startTime)) +
                ", internal=" + internal +
                ", reserve=" + CHexConver.byte2HexStr(reserve) +
                ", duration=" + duration +
                ", stopTime=" + simpleDateFormat.format(new Date(stopTime)) +
                ", distance=" + distance +
                ", kcal=" + kcal +
                ", step=" + step +
                ", recoveryTime=" + recoveryTime +
                ", sync=" + sync +
                ", uid=" + uid +

                ", data=" + CHexConver.byte2HexStr(data) +
                '}';
    }


    static class Info {
        public float speed;
        public int stride;
        public int heart;
        public long time;

        @Override
        public String toString() {
            return "BaseData{" +
                    "speed=" + speed +
                    ", stride=" + stride +
                    ", heart=" + heart +
                    ", time=" + time +
                    '}';
        }
    }
}
