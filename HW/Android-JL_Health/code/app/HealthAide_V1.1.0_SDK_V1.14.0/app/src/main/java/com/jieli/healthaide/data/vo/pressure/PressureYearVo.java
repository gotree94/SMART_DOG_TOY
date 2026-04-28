package com.jieli.healthaide.data.vo.pressure;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.PressureParseImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: PressureYearVo
 * @Description: 压力年数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class PressureYearVo extends PressureBaseVo {

    @Override
    protected IParserModify getParser() {
        return new PressureYearVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> data = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        while (startTime < endTime) {
            calendar.add(Calendar.MONTH, 1);
            long lastMonth = calendar.getTimeInMillis();
            List<HealthEntity> list = new PressureMonthVo().createTestData(startTime, lastMonth);
            data.addAll(list);
            startTime = lastMonth;
        }
        return data;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private PressureParseImpl parser;

        public Parser() {
            parser = new PressureParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int dataLen = 12;
            Integer[] monthValue = new Integer[12];
            for (int i = 0; i < 12; i++) {
                monthValue[i] = 0;
            }
            Integer[] countArray = new Integer[12];
            for (int i = 0; i < 12; i++) {
                countArray[i] = 0;
            }
            Calendar calendar = Calendar.getInstance();
            for (HealthEntity healthEntity : entities) {
                calendar.setTimeInMillis(healthEntity.getTime());
                calendar.setTimeInMillis(healthEntity.getTime());
                int month = calendar.get(Calendar.MONTH);
                int last = monthValue[month];
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(healthEntity);
                List<ParseEntity> day = parser.parse(tempArray);
                int dayTotal = 0;
                int dayCount = 0;
                for (ParseEntity entity : day) {
                    dayTotal += entity.getValue();
                    dayCount = dayCount + 1;
                }
                int dayAvg = 0;
                if (dayTotal > 0) {
                    int count = countArray[month];
                    countArray[month] = count + 1;
                    dayAvg = dayTotal / dayCount;
                }
                int current = last + dayAvg;
                monthValue[month] = current;
                // 需要分析压力均值，不是压力之和
            }
            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int pressureSum = 0;
            int pressureCount = 0;
            highLightIndex = (int) Math.ceil(dataLen / 2);

            for (int i = 0; i < dataLen; i++) {
                int count = countArray[i];
                int monthSum = monthValue[i];
                int avgPressure = 0;
                if (count != 0 && monthSum != 0) {
                    avgPressure = monthSum / count;
                    maxVal = Math.max(avgPressure, maxVal);
                    minVal = Math.min(avgPressure, minVal);
                    pressureSum = pressureSum + avgPressure;
                    pressureCount = pressureCount + 1;
                    highLightIndex = i + 1;
                }
                PressureChartData pressureChartData = new PressureChartData();
                pressureChartData.index = i + 1;
                pressureChartData.value = avgPressure;
                data.add(pressureChartData);
            }
            if (pressureSum != 0 && pressureCount != 0) {
                pressureAvg = pressureSum / pressureCount;
            }
            max = maxVal;
            min = minVal;
            analysisData(data);
            return data;
        }
    }
}
