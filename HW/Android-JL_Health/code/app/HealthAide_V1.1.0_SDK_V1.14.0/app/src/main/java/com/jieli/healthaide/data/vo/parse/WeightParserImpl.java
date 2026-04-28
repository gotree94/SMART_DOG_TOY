package com.jieli.healthaide.data.vo.parse;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.jl_rcsp.util.CHexConver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.jieli.healthaide.data.entity.HealthEntity.DATA_TYPE_WEIGHT;

/**
 * @ClassName: WeightParserImpl
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/8/23 11:24
 */
public class WeightParserImpl implements IParserModify<ParseEntity> {
    @Override
    public List<ParseEntity> parse(List<HealthEntity> entities) {
        ArrayList<ParseEntity> dataArray = new ArrayList<>();
        for (HealthEntity healthEntity : entities) {
            if (healthEntity.getVersion() == 0) {
                List<ParseEntity> tempArray = new WeightParserImpl.ParserV0().parse(healthEntity);
                dataArray.addAll(tempArray);
            }
        }
        return dataArray;
    }

    private class ParserV0 implements IParser<ParseEntity> {

        @Override
        public List<ParseEntity> parse(HealthEntity entity) {
            List<ParseEntity> entities = new ArrayList<>();
            byte[] origin = entity.getData();
            if (origin != null && origin.length > 15) {
                if (origin[0] == (byte) DATA_TYPE_WEIGHT) {
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
                            for (int i = 0; i + 2 <= data.length; i += 2) {
                                int weightInteger = (0xff & data[i]);//*0-250*//
                                double weightDecimal = (0xff & data[i + 1]);//*0-99*//
                                double weight = weightInteger + (weightDecimal / 100);
                                entities.add(new ParseEntity(time, weight));
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
