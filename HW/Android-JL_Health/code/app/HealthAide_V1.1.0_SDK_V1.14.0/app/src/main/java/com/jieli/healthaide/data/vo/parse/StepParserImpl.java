package com.jieli.healthaide.data.vo.parse;


import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.jieli.healthaide.data.entity.HealthEntity.DATA_TYPE_STEP;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/28/21
 * @desc :
 */
public class StepParserImpl implements IParserModify<ParseEntity> {
    private final static String TAG = StepParserImpl.class.getSimpleName();

    @Override
    public List<ParseEntity> parse(List<HealthEntity> entities) {
        ArrayList<ParseEntity> dataArray = new ArrayList<>();
        for (HealthEntity healthEntity : entities) {
            if (healthEntity.getVersion() == 0) {
                List<ParseEntity> tempArray = new StepParserImpl.ParserV0().parse(healthEntity);
                dataArray.addAll(tempArray);
            }
        }
        return dataArray;
    }

    private static class ParserV0 implements IParser<ParseEntity> {
        @Override
        public List<ParseEntity> parse(HealthEntity entity) {
            JL_Log.d(TAG, "ParserV0", "parse ---> " + entity.toString());
            List<ParseEntity> entities = new ArrayList<>();
            byte[] origin = entity.getData();
            if (origin != null && origin.length > 15) {
                if (origin[0] == DATA_TYPE_STEP) {
                    JL_Log.d(TAG, "parse", "DATA_TYPE_STEP");
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
                            JL_Log.d(TAG, "parse", "DATA_TYPE_STEP ---> dataLen: " + dataLen);
                            if (offset + dataLen > origin.length) {
                                JL_Log.d(TAG, "parse", "DATA_TYPE_STEP : " + CHexConver.byte2HexStr(origin));
                                break;
                            }
                            calendar.set(year, month - 1, day, hour, minute);
                            byte[] data = new byte[dataLen];
                            System.arraycopy(origin, offset + 4, data, 0, dataLen);
                            long time = calendar.getTimeInMillis();
                            for (int i = 0; i + 6 <= data.length; i += 6) {
                                int step = CHexConver.bytesToInt(data[i], data[i + 1]);
                                int distance = CHexConver.bytesToInt(data[i + 2], data[i + 3]);
                                int cal = CHexConver.bytesToInt(data[i + 4], data[i + 5]);
                                entities.add(new ParseEntityObject(time, step, distance, cal));
                                time += space;
                            }
                        } else {

                        }
                        offset += (dataLen + 4);
                    }
                }
            }
            return entities;

        }
    }
}
