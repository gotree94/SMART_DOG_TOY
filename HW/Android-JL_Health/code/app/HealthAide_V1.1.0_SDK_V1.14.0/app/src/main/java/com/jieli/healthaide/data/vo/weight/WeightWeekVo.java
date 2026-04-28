package com.jieli.healthaide.data.vo.weight;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.WeightParserImpl;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: WeightWeekVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/8/23 11:19
 */
public class WeightWeekVo extends WeightBaseVo {
    @Override
    protected IParserModify getParser() {
        return new Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> list = new ArrayList<>();
        long time = CalendarUtil.removeTime(startTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        for (int i = 0; i < getDataAllCount(time); i++) {
            if (Math.random() * 2 > 0.5) {
                list.addAll(new WeightDayVo().createTestData(startTime, startTime));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
        }
        return list;
    }

    protected int getDataAllCount(long time) {
        return 7;
    }

    protected int getDayOfPosition(long time) {
        return RelativeTimeUtil.getRelativeDayOfWeek(time);
    }

    private class Parser implements IParserModify<ParseEntity> {
        private WeightParserImpl parser;
        private double valueSum = 0;
        private int valueCount = 0;

        Parser() {
            parser = new WeightParserImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            ArrayList<ParseEntity> data = new ArrayList<>();
            if (entities == null) return data;
            int dataLen = getDataAllCount(startTime);
            ParseEntity[] dataArray = new ParseEntity[dataLen];
            maxVal = VALUE_MIN;
            minVal = VALUE_MAX;
            if (!entities.isEmpty()) {
                for (HealthEntity entity : entities) {
                    double value = parseAnalysis(entity);
                    if (0 != value) {
                        int index = getDayOfPosition(entity.getTime());
                        maxVal = Math.max(maxVal, value);
                        minVal = minVal == 0d ? value : Math.min(minVal, value);
                        WeightBaseVo.WeightBarCharData weightBarCharData = new WeightBaseVo.WeightBarCharData();
                        weightBarCharData.index = index;
                        weightBarCharData.value = value;
                        dataArray[index - 1] = weightBarCharData;
                    }
                }
                double startValue = parseAnalysis(entities.get(0));
                double endValue = parseAnalysis(entities.get(entities.size() - 1));
                changeRange = endValue - startValue;
            } else {
                maxVal = 0;
                minVal = 0;
                changeRange = 0;
            }
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < dataArray.length; i++) {
                ParseEntity parseEntity = dataArray[i];
                if (null == parseEntity) {
                    WeightBaseVo.WeightBarCharData charData = new WeightBaseVo.WeightBarCharData();
                    charData.index = i + 1;
                    charData.value = 0;
                    dataArray[i] = charData;
                } else {
                    highLightIndex = i + 1;
                }
            }
            averageVal = 0;
            if (valueSum != 0 && valueCount != 0) {
                averageVal = valueSum / valueCount;
            }
            data.addAll(Arrays.asList(dataArray));
            return data;
        }

        private double parseAnalysis(HealthEntity entity) {
            double result = 0;
            List<HealthEntity> tempArray = new ArrayList<>();
            tempArray.add(entity);
            List<ParseEntity> entities = parser.parse(tempArray);
            if (entities != null && !entities.isEmpty()) {
                double sum = 0;
                int count = 0;
                for (int i = 0; i < entities.size(); i++) {
                    double value = entities.get(i).getValue();
                    if (value > 0) {
                        sum += value;
                        count++;
                    }
                }
                if (count != 0 && sum != 0) {
                    valueSum += sum;
                    valueCount += count;
                    result = sum / count;
                }
            }
            return result;
        }
    }
}
