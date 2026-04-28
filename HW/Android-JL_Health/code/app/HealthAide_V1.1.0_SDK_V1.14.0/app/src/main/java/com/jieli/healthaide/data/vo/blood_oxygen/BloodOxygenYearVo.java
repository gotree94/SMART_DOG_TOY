package com.jieli.healthaide.data.vo.blood_oxygen;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.BloodOxygenParseImpl;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: BloodOxygenYearVo
 * @Description: 血氧年数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class BloodOxygenYearVo extends BloodOxygenBaseVo {
    @Override
    protected IParserModify getParser() {
        return new BloodOxygenYearVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> data = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        while (startTime < endTime) {
            calendar.add(Calendar.MONTH, 1);
            long lastMonth = calendar.getTimeInMillis();
            List<HealthEntity> list = new BloodOxygenMonthVo().createTestData(startTime, lastMonth);
            data.addAll(list);
            startTime = lastMonth;
        }
        return data;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private final BloodOxygenParseImpl parser;

        public Parser() {
            parser = new BloodOxygenParseImpl();
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
            if (!entities.isEmpty()) {
                Calendar calendar = Calendar.getInstance();
                for (HealthEntity healthEntity : entities) {
                    calendar.setTimeInMillis(healthEntity.getTime());
                    calendar.setTimeInMillis(healthEntity.getTime());
                    int month = calendar.get(Calendar.MONTH);
                    //int last = maxValue[month];
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
                    // 需要分析压力均值，不是压力之和
                }
            }
            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            highLightIndex = Math.round((float) dataLen / 2);

            for (int i = 0; i < dataLen; i++) {
                int max = maxValue[i];
                int min = minValue[i];
                BloodOxygenBarCharData bloodOxygenBarCharData;
                if (max != VALUE_MIN && min != VALUE_MAX) {
                    maxVal = Math.max(max, maxVal);
                    minVal = Math.min(min, minVal);
                    bloodOxygenBarCharData = new BloodOxygenBarCharData(i + 1, max, min);
                    highLightIndex = i + 1;
                } else {
                    bloodOxygenBarCharData = new BloodOxygenBarCharData(i + 1, 0, 0);
                }
                data.add(bloodOxygenBarCharData);
            }

            max = maxVal;
            min = minVal;
            return data;
        }
    }
}
