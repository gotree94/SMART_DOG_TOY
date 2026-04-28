package com.jieli.healthaide.data.vo.weight;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.WeightParserImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: WeightYearVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/8/23 11:20
 */
public class WeightYearVo extends WeightBaseVo {
    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> data = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        while (startTime < endTime) {
            calendar.add(Calendar.MONTH, 1);
            long lastMonth = calendar.getTimeInMillis();
            List<HealthEntity> list = new WeightMonthVo().createTestData(startTime, lastMonth);
            data.addAll(list);
            startTime = lastMonth;
        }
        return data;
    }

    @Override
    protected IParserModify getParser() {
        return new WeightYearVo.Parser();
    }

    private class Parser implements IParserModify<ParseEntity> {
        private WeightParserImpl parser;

        public Parser() {
            parser = new WeightParserImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int dataLen = 12;
            int valueSum = 0;
            int valueCount = 0;
            Double[] monthValueArray = new Double[12];
            for (int i = 0; i < 12; i++) {
                monthValueArray[i] = 0d;
            }
            Integer[] monthCountArray = new Integer[12];
            for (int i = 0; i < 12; i++) {
                monthCountArray[i] = 0;
            }
            Calendar calendar = Calendar.getInstance();
            for (HealthEntity healthEntity : entities) {
                calendar.setTimeInMillis(healthEntity.getTime());
                int month = calendar.get(Calendar.MONTH);
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(healthEntity);
                List<ParseEntity> day = parser.parse(tempArray);
                double dayTotal = 0;
                int dayCount = 0;
                for (ParseEntity entity : day) {
                    dayTotal += entity.getValue();
                    dayCount = dayCount + 1;
                }
                if (dayTotal > 0) {
                    monthCountArray[month] += dayCount;
                    monthValueArray[month] += dayTotal;
                    valueCount += dayCount;
                    valueSum += dayTotal;
                }
            }
            maxVal = VALUE_MIN;
            minVal = VALUE_MAX;
            highLightIndex = Math.round((float) dataLen / 2);
            double startValue = 0;
            double endValue = 0;
            for (int i = 0; i < dataLen; i++) {
                int count = monthCountArray[i];
                double monthSum = monthValueArray[i];
                double avgValue = 0;
                if (count != 0 && monthSum != 0) {
                    avgValue = monthSum / count;
                    maxVal = Math.max(avgValue, maxVal);
                    minVal = Math.min(avgValue, minVal);
                    highLightIndex = i + 1;
                }
                WeightBaseVo.WeightBarCharData charData = new WeightBaseVo.WeightBarCharData();
                charData.index = i + 1;
                charData.value = avgValue;
                data.add(charData);
                if (avgValue != 0) {
                    if (startValue == 0) {
                        startValue = avgValue;
                    }
                    endValue = avgValue;
                }
            }
            changeRange = endValue - startValue;
            averageVal = 0;//年平均
            if (valueCount != 0 && valueSum != 0) {
                averageVal = valueSum / valueCount;
            }
            if (entities.isEmpty()) {
                maxVal = 0;
                minVal = 0;
            }
            return data;
        }
    }
}
