package com.jieli.healthaide.data.vo.parse;


import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: BloodOxygenParseImpl
 * @Description: 解析血氧数据（单条数据的解析***基础解析***）
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class HeartRateParseImpl implements IParserModify<ParseEntity> {
    private static final String TAG = HeartRateParseImpl.class.getSimpleName();

    @Override
    public List<ParseEntity> parse(List<HealthEntity> entities) {
        ArrayList<ParseEntity> dataArray = new ArrayList<>();
        for (HealthEntity healthEntity : entities) {
            if (healthEntity.getVersion() == 0) {
                List<ParseEntity> tempArray = new HeartRateParseImpl.ParserV0().parse(healthEntity);
                dataArray.addAll(tempArray);
            }
        }
        return dataArray;
    }


    private static class ParserV0 implements IParser<ParseEntity> {
        @Override
        public List<ParseEntity> parse(HealthEntity entity) {
            List<ParseEntity> entities = new ArrayList<>();
            byte[] origin = entity.getData();
            if (origin != null && origin.length > 15) {
                if (origin[0] == (byte) 0x03) {
                    JL_Log.d(TAG, "ParserV0", "parse ---> " + CHexConver.byte2HexStr(origin));
                    Calendar calendar = Calendar.getInstance();
                    int year = CHexConver.bytesToInt(origin[1], origin[2]);
                    int month = 0xff & origin[3];
                    int day = 0xff & origin[4];
                    long space = (0xff & origin[8]) * 60 * 1000;
                    int offset = 11;
                    while (offset + 4 <= origin.length) {
                        byte[] tempArray = new byte[4];
                        System.arraycopy(origin, offset, tempArray, 0, tempArray.length);
                        int hour = 0xff & tempArray[0];
                        int minute = 0xff & tempArray[1];
                        int dataLen = CHexConver.bytesToInt(tempArray[2], tempArray[3]);
                        if (hour <= 24 && minute <= 60) {
                            if (offset + dataLen > origin.length) {
                                break;
                            }
                            calendar.set(year, month - 1, day, hour, minute);
                            byte[] data = new byte[dataLen];
                            System.arraycopy(origin, offset + 4, data, 0, dataLen);
                            long time = calendar.getTimeInMillis();
                            for (int i = 0; i < data.length; i++) {
                                int hearRate = data[i] & 0xFF;
                                hearRate = Math.max(hearRate, 0);
                                entities.add(new ParseEntity(time, hearRate));
                                time += space;
                            }
                        }
                        offset += (dataLen + 4);
                    }
                }
            }
            return entities;
        }
    }
}