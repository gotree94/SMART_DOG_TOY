package com.jieli.healthaide.data.entity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.jieli.healthaide.util.CalendarUtil;
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
    private static final String TAG = SportRecord.class.getSimpleName();

    public static final int TYPE_NONE = 0x00;
    public static final int TYPE_OUTDOOR = 0x01;
    public static final int TYPE_INDOOR = 0x02;
    public static final int HEART_RATE_MODE_MAX = 0x00;
    public static final int HEART_RATE_MODE_SAVE = 0x01;

    @NonNull
    private String uid;
    private byte type;//运动模式
    private long startTime;//开始时间戳
    private byte internal;//间隔
    private byte version;//版本
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] reserve; //保留位 len =10
    private int duration;//时长 unit:s
    private long stopTime;//结束时间戳
    private int distance;//距离 单位：秒
    private int kcal;//热量 单位：千卡
    private int step;//步数
    private int recoveryTime;//恢复时间
    private boolean sync; //是否已经同步到服务器

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] data; //原始数据


    @Ignore
    private byte heartRateMode;

    @Ignore
    private int[] sportsStatus;


    @Ignore
    private List<Info> dataList;

    @Ignore
    private List<Pace> paces;

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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getKcal() {
        return kcal;
    }

    public void setKcal(int kcal) {
        this.kcal = kcal;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(int recoveryTime) {
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


    public List<Info> getDataList() {
        return dataList;
    }


    public List<Pace> getPaces() {
        return paces;
    }

    public void setHeartRateMode(byte heartRateMode) {
        this.heartRateMode = heartRateMode;
    }

    public byte getHeartRateMode() {
        return heartRateMode;
    }

    public void setSportsStatus(int[] sportsStatus) {
        this.sportsStatus = sportsStatus;
    }

    public int[] getSportsStatus() {
        return sportsStatus;
    }

    /**
     * 解析运动记录数据
     *
     * @param data
     * @return
     */
    public static SportRecord from(byte[] data) {
        SportRecord sportRecord = new SportRecord();
        sportRecord.setData(data);

        int index = 0;
        //运动类型
        sportRecord.setType(data[index++]);
        //版本
        sportRecord.setVersion(data[index++]);
//        if (sportRecord.getVersion() != 0) return sportRecord;//目前只处理版本号是0的数据

        //时间间隔
        int space = data[index++];
        sportRecord.setInternal((byte) space);
        space *= 1000;
        //保留位
        byte[] reversed = new byte[10];
        System.arraycopy(data, index, reversed, 0, reversed.length);
        sportRecord.setReserve(reversed);
        index += 10;
        JL_Log.d(TAG, "from", "space = " + space + "\ttype = " + sportRecord.getType() + "\tversion = " + sportRecord.getVersion());
        List<Info> list = new ArrayList<>();


        long startTime = 0;
        for (; index < data.length; ) {
            byte flag = data[index++];
            byte len = data[index++];
            if (len < 1) {
                JL_Log.e(TAG, "from", "error len  index = " + index + "\tlen = " + len);
                break;
            }
            if (flag == 0) {//开始包
                startTime = toTime(data, index);
                if (sportRecord.getStartTime() == 0) {
                    sportRecord.setStartTime(startTime);
                }
                JL_Log.d(TAG, "from", "startTime = " + CalendarUtil.serverDateFormat().format(new Date(startTime)));
            } else if (flag == 1 && index + 4 < data.length) {//基础数据包
                Info info = new Info();
                startTime += space;
                info.time = startTime;
                info.heart = data[index] & 0xff;
                info.stepFreq = CHexConver.bytesToInt(data[index + 2], data[index + 1]);
                info.speed = CHexConver.bytesToInt(data[index + 4], data[index + 3]) * 0.01f;//km/小时
                info.pace = info.speed == 0 ? 0 : (int) (1.0f / info.speed * 3600);//s/km
                list.add(info);
                JL_Log.d(TAG, "from", "数据包 = " + info);
            } else if (flag == 2) {//暂停包
                long pauseTime = toTime(data, index);
                sportRecord.setStopTime(pauseTime);
                JL_Log.d(TAG, "from", "pauseTime = " + CalendarUtil.serverDateFormat().format(new Date(pauseTime)));
            } else if (flag == 3) {//每公里配速包
                Pace pace = new Pace();
                pace.value = CHexConver.bytesToInt(data[index + 1], data[index]);
                pace.n = CHexConver.byteToInt(data[index + 2]);
                if (sportRecord.paces == null) {
                    sportRecord.paces = new ArrayList<>();
                }
                sportRecord.paces.add(pace);
                JL_Log.d(TAG, "from", "pace = " + pace + "\tdata= " + CalendarUtil.formatString("%02x%02x%02x", data[index], data[index + 1], data[index + 2]));
            } else if (flag == (byte) 0xff) {//结束包
                long stopTime = toTime(data, index);
                sportRecord.setStopTime(stopTime);
                JL_Log.d(TAG, "from", "stopTime = " + CalendarUtil.serverDateFormat().format(new Date(stopTime)));
                index += len;//结束要加上偏移
                break;
            } else {
                JL_Log.d(TAG, "from", "错误flag = " + flag + "\tindex = " + CalendarUtil.formatString("0X%x", index));
            }
            index += len;
        }

        if (data.length < index + 14) {
            return sportRecord;
        }

        sportRecord.dataList = list;
        //运动时长
        sportRecord.setDuration(CHexConver.bytesToInt(data[index + 1], data[index]));
        index += 2;
        index += 4;//保留位
        //距离
        sportRecord.setDistance(CHexConver.bytesToInt(data[index + 1], data[index]) * 10);
        index += 2;
        //卡路里
        sportRecord.setKcal(CHexConver.bytesToInt(data[index + 1], data[index]));
        index += 2;
        //步数
        sportRecord.setStep(CHexConver.bytesLittleToInt(data, index, 4));
        index += 4;
        //恢复时间
        sportRecord.setRecoveryTime(CHexConver.bytesToInt(data[index + 1], data[index]));//
        index += 2;

        //运动区间
        if (index < data.length && index + 20 <= data.length) {
            byte[] status = new byte[data.length - index];
            System.arraycopy(data, index, status, 0, status.length);
            JL_Log.d(TAG, "from", "data = " + CHexConver.byte2HexStr(status) + "\tsize = " + status.length);
            sportRecord.setHeartRateMode(data[index++]);
            JL_Log.d(TAG, "from", "setHeartRateMode = " + sportRecord.getHeartRateMode());
            int[] tmp = new int[5];
            for (int i = 0; i < 5; i++) {
                tmp[i] = CHexConver.bytesLittleToInt(data, index + i * 4, 4);
            }
            sportRecord.sportsStatus = tmp;
            index += 20;
        }
        return sportRecord;
    }

    /**
     * 解析运动记录数据头，用于判断本地是否已经同步了该记录
     *
     * @param data
     * @return
     */
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
        JL_Log.d(TAG, "fromHeader", "space = " + space + "\ttype = " + sportRecord.getType() + "\tversion = " + sportRecord.getVersion());

        for (; index < data.length; ) {
            byte flag = data[index++];
            byte len = data[index++];
            if (len < 1) {
                JL_Log.e(TAG, "fromHeader", "error len  index = " + index + "\tlen = " + len);
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
        StringBuilder infos = new StringBuilder("");
        if (dataList != null) {
            for (Info info : dataList) {
                infos.append(info.toString())
                        .append("\t");
            }
        }

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
                ", info=" + infos.toString() +
                ", data=" + CHexConver.byte2HexStr(data) +
                '}';
    }


    public static class Info {
        public float speed;
        public int oxygen;
        public int heart;
        public long time;
        public int stepFreq;
        public int pace;

        @Override
        public String toString() {
            return "Info{" +
                    "speed=" + speed +
                    ", oxygen=" + oxygen +
                    ", heart=" + heart +
                    ", time=" + time +
                    ", stepFreq=" + stepFreq +
                    ", pace=" + pace +
                    '}';
        }
    }

    public static class Pace {
        public int n;
        public int value;

        @Override
        public String toString() {
            return "Pace{" +
                    "n=" + n +
                    ", value=" + value +
                    '}';
        }
    }

}
