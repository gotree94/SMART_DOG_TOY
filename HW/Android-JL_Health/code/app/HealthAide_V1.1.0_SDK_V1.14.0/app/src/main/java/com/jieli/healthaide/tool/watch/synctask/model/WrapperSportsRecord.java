package com.jieli.healthaide.tool.watch.synctask.model;

import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.CryptoUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/3
 * @desc :
 */
public class WrapperSportsRecord {
    private static final String TAG = WrapperSportsRecord.class.getSimpleName();
    private static final byte[] HEADER = new byte[]{(byte) 0xfe, (byte) 0xdc, (byte) 0xba};
    private static final byte TAIL = (byte) 0XEF;
    private static final byte PORT = (byte) 0X01; //文件上传端口0x01:android  0x02:ios
//    private byte[] header = HEADER;
//    private byte version;
//    private short crc;
//    private byte tail = TAIL;

    private SportRecord sportRecord;
    private LocationEntity locationEntity;


    private byte version;

    public WrapperSportsRecord(byte version, SportRecord sportRecord, LocationEntity locationEntity) {
        this.version = version;
        this.sportRecord = sportRecord;
        this.locationEntity = locationEntity;


    }

    public byte[] toData() {
        byte[] extrasData = locationEntity == null ? new byte[0] : locationEntity.getGpsData();
        byte[] deviceSportsRecordData = sportRecord.getData();
        if (extrasData == null) {
            extrasData = new byte[0];
        }
        if (deviceSportsRecordData == null) {
            deviceSportsRecordData = new byte[0];
        }
        if (version == 0x00) {
            ByteBuffer content = ByteBuffer.allocate(extrasData.length + deviceSportsRecordData.length + 8);
            content.putInt(deviceSportsRecordData.length)
                    .put(deviceSportsRecordData)
                    .putInt(extrasData.length)
                    .put(extrasData);
            int len = 3 + 1 + 2 + 1 + 1 + content.position();
            byte[] contentBytes = content.array();
            ByteBuffer buffer = ByteBuffer.allocate(len);
            buffer.put(HEADER);
            buffer.put(version);
            buffer.put(PORT);
            buffer.putShort(CryptoUtil.CRC16(contentBytes, (short) 0));
            buffer.put(contentBytes);
            buffer.put(TAIL);
            return buffer.array();
        }
        return null;
    }


    public static WrapperSportsRecord from(String uid, byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer = (ByteBuffer) buffer.put(data).flip();
        byte[] header = new byte[3];
        buffer.get(header, 0, 3);
        if (!Arrays.equals(header, HEADER)) {
            JL_Log.w(TAG, "from", "运动数据解析： 文件头异常" + CHexConver.byte2HexStr(header));
            return null;
        }

        byte version = buffer.get();
        byte port = buffer.get();
        short crc = buffer.getShort();
        if (version == 0x00) {

            int deviceSportsRecordDataLen = buffer.getInt();
            byte[] deviceSportsRecordData = new byte[deviceSportsRecordDataLen];
            buffer.get(deviceSportsRecordData, 0, deviceSportsRecordDataLen);

            int extrasLen = buffer.getInt();
            byte[] extras = new byte[extrasLen];
            buffer.get(extras, 0, extrasLen);
            short calcCrc = 0;
            calcCrc = CryptoUtil.CRC16(CHexConver.intToBigBytes(deviceSportsRecordDataLen), calcCrc);
            calcCrc = CryptoUtil.CRC16(deviceSportsRecordData, calcCrc);
            calcCrc = CryptoUtil.CRC16(CHexConver.intToBigBytes(extrasLen), calcCrc);
            calcCrc = CryptoUtil.CRC16(extras, calcCrc);

            if (crc != calcCrc) {
                JL_Log.w(TAG, "from", "运动数据解析：crc不相等 crc=" + crc + "\tcalcCrc =" + calcCrc + "\tport = " + port);
                return null;
            }
            byte tail = buffer.get();
            if (tail != TAIL) {
                JL_Log.w(TAG, "from", "运动数据解析：tail 不相等  " + CalendarUtil.formatString("%x", tail) + "\tport = " + port);
                return null;
            }
            SportRecord sportRecord = SportRecord.from(deviceSportsRecordData);
            sportRecord.setUid(uid);
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.setUid(uid);
            locationEntity.setGpsData(extras);
            locationEntity.setStartTime(sportRecord.getStartTime());
            return new WrapperSportsRecord(version, sportRecord, locationEntity);
        }
        return null;
    }


    public SportRecord getSportRecord() {
        return sportRecord;
    }

    public void setSportRecord(SportRecord sportRecord) {
        this.sportRecord = sportRecord;
    }

    public LocationEntity getLocationEntity() {
        return locationEntity;
    }

    public void setLocationEntity(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "WrapperSportsRecord{" +
                "sportRecord=" + sportRecord +
                ", locationEntity=" + locationEntity +
                ", version=" + version +
                '}';
    }
}
