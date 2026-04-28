package com.jieli.healthaide.data.vo.pressure;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.PressureParseImpl;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: PressureWeekVo
 * @Description: 压力周数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class PressureWeekVo extends PressureBaseVo {

    @Override
    protected IParserModify getParser() {
        return new PressureWeekVo.Parser();
    }

    protected int getDayOfPosition(long time) {
        return RelativeTimeUtil.getRelativeDayOfWeek(time);
    }

    protected int getDataAllCount(long time) {
        return 7;
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> list = new ArrayList<>();
        long time = CalendarUtil.removeTime(startTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        for (int i = 0; i < 7; i++) {
            if (Math.random() * 2 > 0.5) {
                list.addAll(new PressureDayVo().createTestData(startTime, startTime));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
        }
        return list;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private PressureParseImpl parser;

        public Parser() {
            parser = new PressureParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            ArrayList<ParseEntity> data = new ArrayList<>();
            int dataLen = getDataAllCount(startTime);
            ParseEntity[] dataArray = new ParseEntity[dataLen];
            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int pressureSum = 0;
            int pressureCount = 0;
            int temptRestingAvg = 0;
            if (!entities.isEmpty()) {
                for (HealthEntity entity : entities) {
                    int value = parseAnalysis(entity);
                    if (0 != value) {
                        int index = getDayOfPosition(entity.getTime());
                        maxVal = Math.max(value, maxVal);
                        minVal = Math.min(value, minVal);
                        PressureChartData pressureChartData = new PressureChartData();
                        pressureChartData.index = index;
                        pressureChartData.value = value;
                        pressureSum = pressureSum + value;
                        pressureCount = pressureCount + 1;
                        dataArray[index - 1] = pressureChartData;
                    }
                }
            }
            if (pressureSum != 0 && pressureCount != 0) {
                temptRestingAvg = pressureSum / pressureCount;
            }
            max = maxVal;
            min = minVal;
            pressureAvg = temptRestingAvg;
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < dataArray.length; i++) {
                ParseEntity parseEntity = dataArray[i];
                if (null == parseEntity) {
                    PressureChartData pressureChartData = new PressureChartData();
                    pressureChartData.index = i + 1;
                    pressureChartData.value = 0;
                    dataArray[i] = pressureChartData;
                } else {
                    highLightIndex = i + 1;
                }
            }
            data.addAll(Arrays.asList(dataArray));
            analysisData(data);
            return data;
        }

        public int parseAnalysis(HealthEntity entity) {
            int avgPressure = 0;
            List<HealthEntity> tempArray = new ArrayList<>();
            tempArray.add(entity);
            List<ParseEntity> entities = parser.parse(tempArray);
            int total = 0;
            int count = 0;
            for (int i = 0; i < entities.size(); i++) {
                int value = (int) entities.get(i).getValue();
                if (value > 0) {
                    count++;
                    total += value;
                }
            }
            if (count != 0 && total != 0) {
                avgPressure = total / count;
                // 需要分析压力均值，不是压力之和
            }
            return avgPressure;
        }
    }
}
