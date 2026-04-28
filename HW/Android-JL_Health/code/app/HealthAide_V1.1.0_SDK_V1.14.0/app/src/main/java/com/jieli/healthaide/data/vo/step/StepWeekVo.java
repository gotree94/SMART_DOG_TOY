package com.jieli.healthaide.data.vo.step;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.ParseEntityObject;
import com.jieli.healthaide.data.vo.parse.StepParserImpl;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author : zhanghuanming
 * @e-mail : zhanghuanming@zh-jieli.com
 * @date : 6/16/21
 * @desc : 步数 周的数据，dataLen 固定长度 7，以一天为间隔统计
 */
public class StepWeekVo extends StepBaseVo {


    @Override
    protected IParserModify getParser() {
        return new StepWeekVo.Parser();
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
                list.addAll(new StepDayVo().createTestData(startTime, startTime));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
        }
        return list;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private StepParserImpl parser;

        public Parser() {
            parser = new StepParserImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            ArrayList<ParseEntity> data = new ArrayList<>();
            int dataLen = getDataAllCount(startTime);
            ParseEntity[] dataArray = new ParseEntity[dataLen];
            int dayCount = 0;
            for (HealthEntity entity : entities) {
                final int values[] = calcDayData(entity);
                int step = values[0];
                int distance = values[1];
                int kcal = values[2];

                //步数
                int index = getDayOfPosition(entity.getTime());
                totalStep = totalStep + step;
                dayCount = dayCount + 1;
                max = Math.max(max, step);
                StepChartData stepChartData = new StepChartData();
                stepChartData.index = index;
                stepChartData.value = step;
                stepChartData.setValue(step);
                dataArray[index - 1] = stepChartData;
                totalDistance += distance;
                totalKcal += kcal;

            }
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < dataArray.length; i++) {
                ParseEntity parseEntity = dataArray[i];
                if (null == parseEntity) {
                    StepChartData stepChartData = new StepChartData();
                    stepChartData.index = i + 1;
                    stepChartData.value = 0;
                    dataArray[i] = stepChartData;
                } else {
                    highLightIndex = i + 1;
                }
            }
            if (totalStep != 0 && dayCount != 0) {
                avgStep = totalStep / dayCount;
            }
            data.addAll(Arrays.asList(dataArray));
            return data;
        }

        public int[] calcDayData(HealthEntity entity) {
            List<HealthEntity> tempArray = new ArrayList<>();
            tempArray.add(entity);
            List<ParseEntity> entities = parser.parse(tempArray);
            int totalStep = 0;
            int totalDistance = 0;
            int totalKcal = 0;
            for (int i = 0; i < entities.size(); i++) {
                ParseEntityObject parseEntityObject = (ParseEntityObject) entities.get(i);
                int step = (int) parseEntityObject.attr1;
                int distance = (int) parseEntityObject.attr2 * 10;
                int kcal = (int) parseEntityObject.attr3;
                totalStep += Math.max(0, step);
                totalKcal += Math.max(0, kcal);
                totalDistance += Math.max(0, distance);

            }
            return new int[]{totalStep, totalDistance, totalKcal};
        }
    }


}
