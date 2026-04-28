package com.jieli.healthaide.data.vo.step;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.ParseEntityObject;
import com.jieli.healthaide.data.vo.parse.StepParserImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author : zhanghuanming
 * @e-mail : zhanghuanming@zh-jieli.com
 * @date : 6/16/21
 * @desc : 步数 年数据，以一个月为间隔统计
 */
public class StepYearVo extends StepBaseVo {


    @Override
    protected IParserModify getParser() {
        return new StepYearVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> data = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        while (startTime < endTime) {
            calendar.add(Calendar.MONTH, 1);
            long lastMonth = calendar.getTimeInMillis();
            List<HealthEntity> list = new StepMonthVo().createTestData(startTime, lastMonth);
            data.addAll(list);
            startTime = lastMonth;
        }
        return data;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private StepParserImpl parser;

        public Parser() {
            parser = new StepParserImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int dataLen = 12;
            Integer[] monthValue = new Integer[12];
            Integer[] monthCount = new Integer[12];
           /* for (int i = 0; i < 12; i++) {
                monthValue[i] = 0;
            }*/
            Arrays.fill(monthValue, 0);
            Arrays.fill(monthCount, 0);
            int yearDay = 0;
            Calendar calendar = Calendar.getInstance();
            for (HealthEntity healthEntity : entities) {
                yearDay++;
                calendar.setTimeInMillis(healthEntity.getTime());
                calendar.setTimeInMillis(healthEntity.getTime());
                int month = calendar.get(Calendar.MONTH);
                int last = monthValue[month];
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(healthEntity);
                List<ParseEntity> day = parser.parse(tempArray);
                int dayTotalStep = 0;
                int dayTotalDistance = 0;
                int dayTotalKcal = 0;
                for (ParseEntity entity : day) {
                    ParseEntityObject parseEntityObject = (ParseEntityObject) entity;
                    int step = (int) parseEntityObject.attr1;
                    dayTotalStep += step;
                    dayTotalDistance += parseEntityObject.attr2 * 10;
                    dayTotalKcal += parseEntityObject.attr3;
                }
                int current = last + dayTotalStep;
                monthValue[month] = current;
                monthCount[month]++;
                totalKcal += dayTotalKcal;
                totalStep += dayTotalStep;
                totalDistance += dayTotalDistance;

            }
            int totalStep = 0;//年总步数
//            int monthCount = 0;//有数据的月份
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < dataLen; i++) {
                int monthSum = monthValue[i];
                int monthVal = 0;
                if (monthSum != 0) {
                    monthVal = monthSum / monthCount[i];
                    max = Math.max(max, monthVal);
                    totalStep = totalStep + monthSum;
//                    monthCount = monthCount + 1;
                    highLightIndex = i + 1;
                }
                StepChartData stepChartData = new StepChartData();
                stepChartData.index = i + 1;
                stepChartData.value = monthVal;
                data.add(stepChartData);
            }

            avgStep = yearDay == 0 ? 0 : totalStep / yearDay;

            return data;
        }
    }
}
