package com.jieli.healthaide.data.vo.heart_rate;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.HeartRateParseImpl;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: HeartRateYearVo
 * @Description: 心率年数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class HeartRateYearVo extends HeartRateBaseVo {

    @Override
    protected IParserModify getParser() {
        return new HeartRateYearVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> data = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        while (startTime < endTime) {
            calendar.add(Calendar.MONTH, 1);
            long lastMonth = calendar.getTimeInMillis();
            List<HealthEntity> list = new HeartRateDayVo().createTestData(startTime, lastMonth);
            data.addAll(list);
            startTime = lastMonth;
        }
        return data;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private HeartRateParseImpl parser;

        public Parser() {
            parser = new HeartRateParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int dataLen = 12;
            Integer[] maxValue = new Integer[12];
            for (int i = 0; i < dataLen; i++) {
                maxValue[i] = VALUE_MIN;
            }
            Integer[] minValue = new Integer[12];
            for (int i = 0; i < dataLen; i++) {
                minValue[i] = VALUE_MAX;
            }
            Integer[] restingValue = new Integer[12];
            for (int i = 0; i < dataLen; i++) {
                restingValue[i] = 0;
            }
            Integer[] restingCountValue = new Integer[12];
            for (int i = 0; i < dataLen; i++) {
                restingCountValue[i] = 0;
            }
            if (!entities.isEmpty()) {
                Calendar calendar = Calendar.getInstance();
                for (HealthEntity healthEntity : entities) {
                    calendar.setTimeInMillis(healthEntity.getTime());
                    calendar.setTimeInMillis(healthEntity.getTime());
                    int month = calendar.get(Calendar.MONTH);
                    int last = maxValue[month];
                    List<HealthEntity> tempArray = new ArrayList<>();
                    tempArray.add(healthEntity);
                    List<ParseEntity> day = parser.parse(tempArray);
                    int max = maxValue[month];
                    int min = minValue[month];
                    for (ParseEntity entity : day) {
                        int value = (int) entity.getValue();
                        if (value > 0) {//因为可能会用0来代表没有数据所以需要跳过
                            max = Math.max(max, value);
                            min = Math.min(min, value);
                        }
                    }
                    minValue[month] = min;
                    maxValue[month] = max;
                    {//静息心率
                        int restingHeartRate = 0;
                        byte[] reservedByte = new byte[2];
                        System.arraycopy(healthEntity.getData(), 9, reservedByte, 0, reservedByte.length);
                        if (reservedByte[0] != (byte) 0xff) {
                            restingHeartRate = reservedByte[0] & 0xff;//currentDayEntity.get todo 应该从数据库中获取，但是现在的数据库没有这个静息心率
                            restingValue[month] = restingValue[month] + restingHeartRate;//+healthEntity.get todo 应该从数据库中获取，暂时用假数据
                            restingCountValue[month] = restingCountValue[month] + 1;
                        }
                    }
                    // 需要分析压力均值，不是压力之和
                }
            }
            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int restingSum = 0;
            int restingCount = 0;
            int temptRestingAvg = 0;
            highLightIndex = Math.round((float) dataLen / 2);

            for (int i = 0; i < dataLen; i++) {
                int max = maxValue[i];
                int min = minValue[i];
                HeartRateCharData heartRateCharData;
                if (max != VALUE_MIN && min != VALUE_MAX) {
                    maxVal = Math.max(max, maxVal);
                    minVal = Math.min(min, minVal);
                    heartRateCharData = new HeartRateCharData(i + 1, max, min);
                    int restingRate = 0;
                    if (restingCountValue[i] != 0) {
                        restingRate = restingValue[i] / restingCountValue[i];//平均心率
                        restingSum += restingValue[i];
                        restingCount += restingCountValue[i];
                    }
                    heartRateCharData.restingRate = restingRate;
                   /* if (restingRate != 0) {
                        restingSum = restingSum + restingRate;
                        restingCount = restingCount + 1;
                    }*/
                    highLightIndex = i + 1;
                } else {
                    heartRateCharData = new HeartRateCharData(i + 1, 0, 0);
                }
                data.add(heartRateCharData);
            }
            if (restingCount != 0 && restingSum != 0) {
                temptRestingAvg = restingSum / restingCount;
            }
            max = maxVal;
            min = minVal;
            restingAvg = temptRestingAvg;
            return data;
        }
    }
}
