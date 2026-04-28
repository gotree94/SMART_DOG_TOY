package com.jieli.healthaide.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.amap.api.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/2
 * @desc :
 */
@Entity(primaryKeys = {"uid", "startTime"})
public class LocationEntity {

    public static final byte FLAG_LOCATION = 0x00;
    public static final byte FLAG_TIME = 0x01;
    @NonNull
    private String uid;

    private long startTime;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] gpsData;


    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public byte[] getGpsData() {
        return gpsData;
    }

    public void setGpsData(byte[] gpsData) {
        this.gpsData = gpsData;
    }


    public List<List<LatLng>> toTrackData() {

        byte[] gpsData = getGpsData();
//        if (gpsData != null) {
//            byte[] tmp = gpsData;
//            int len = tmp.length;
//            gpsData = new byte[tmp.length * 20];
//            for (int i = 0; i < gpsData.length; i += len) {
//                System.arraycopy(tmp, 0, gpsData, i, len);
//            }
//        }
//        JL_Log.d("sen", "gps-->" + gpsData.length);
//        JL_Log.e("sen","gps-->"+ CHexConver.byte2HexStr(gpsData));
        List<List<LatLng>> latLngs = new ArrayList<>();
        if (gpsData == null) return latLngs;
        //解析位置数据
        ByteBuffer buffer = ByteBuffer.allocate(gpsData.length).put(gpsData);
        int size = buffer.position();
//        JL_Log.e("sen","size-->"+ size);
        buffer.flip();
        double i = 0.0;
        while (buffer.position() < size) {
            int flag = buffer.get();
            if (flag == LocationEntity.FLAG_LOCATION) {
                double lat = buffer.getDouble();
                double lng = buffer.getDouble();
                float speed = buffer.getFloat();
//                i += 0.00001;
//                LatLng latLng = new LatLng(lat+i, lng + i);
                LatLng latLng = new LatLng(lat , lng  );
                latLngs.get(latLngs.size() - 1).add(latLng);//取最后一个位置列表
            } else if (flag == LocationEntity.FLAG_TIME) {
                long time = buffer.getLong();
//                JL_Log.d("sen", "time-->" + CalendarUtil.serverDateFormat().format(time));
                if (latLngs.size() > 0) {
                    List<LatLng> last = latLngs.get(latLngs.size() - 1);
                    if (last.size() == 0) continue; //如果上一个时间结点的gps数据是空，则复用
                }

                latLngs.add(new ArrayList<>());//遇到开始时间类型则新增一个列表
            } else {
                throw new RuntimeException("位置数据异常," + getStartTime() + "\tpos" + buffer.position() + "\tuid = " + getUid());
            }
        }


        return latLngs;
    }

    @Override
    public String toString() {
        return "LocationEntity{" +
                "uid='" + uid + '\'' +
                ", startTime=" + startTime +
                ", gpsData=" + Arrays.toString(gpsData) +
                '}';
    }
}
