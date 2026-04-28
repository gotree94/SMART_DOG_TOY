package com.jieli.healthaide.data.vo.blood_oxygen;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.BloodOxygenParseImpl;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: BloodOxygenWeekVo
 * @Description: 血氧周数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class BloodOxygenWeekVo extends BloodOxygenBaseVo {

    @Override
    protected IParserModify getParser() {
        return new BloodOxygenWeekVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> list = new ArrayList<>();
        long time = CalendarUtil.removeTime(startTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        for (int i = 0; i < 7; i++) {
            if (Math.random() * 2 > 0.5) {
                list.addAll(new BloodOxygenDayVo().createTestData(startTime, startTime));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
        }
        return list;
    }

    protected int getDayOfPosition(long time) {
        return RelativeTimeUtil.getRelativeDayOfWeek(time);
    }

    protected int getDataAllCount(long time) {
        return 7;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private BloodOxygenParseImpl parser;


        public Parser() {
            parser = new BloodOxygenParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            int dataLen = getDataAllCount(startTime);
            ParseEntity[] dataArray = new ParseEntity[dataLen];
            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            if (!entities.isEmpty()) {
                for (HealthEntity entity : entities) {
                    Result result = parseAnalysis(entity);
                    if (null != result) {
                        int index = getDayOfPosition(entity.getTime());
                        maxVal = Math.max(result.max, maxVal);
                        minVal = Math.min(result.min, minVal);
                        BloodOxygenBarCharData bloodOxygenBarCharData = new BloodOxygenBarCharData(index, result.max, result.min);
                        dataArray[index - 1] = bloodOxygenBarCharData;
                    }
                }
            }
            max = maxVal;
            min = minVal;
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < dataArray.length; i++) {
                ParseEntity parseEntity = dataArray[i];
                if (null == parseEntity) {
                    BloodOxygenBarCharData bloodOxygenBarCharData = new BloodOxygenBarCharData(i + 1, 0, 0);
                    dataArray[i] = bloodOxygenBarCharData;
                } else {
                    highLightIndex = i + 1;
                }
            }
            ArrayList<ParseEntity> data = new ArrayList<>(Arrays.asList(dataArray));
            return data;
        }

        public Result parseAnalysis(HealthEntity entity) {
            Result result = null;
            List<HealthEntity> tempArray = new ArrayList<>();
            tempArray.add(entity);
            List<ParseEntity> entities = parser.parse(tempArray);
            int max = VALUE_MIN;
            int min = VALUE_MAX;
            for (int i = 0; i < entities.size(); i++) {
                int value = (int) entities.get(i).getValue();
                if (value > 0) {
                    max = Math.max(max, value);
                    min = Math.min(min, value);
                }
            }
            if (max != VALUE_MIN && min != VALUE_MAX) {
                result = new Result();
                result.max = max;
                result.min = min;
            }
            return result;
        }

        private class Result {
            int max;
            int min;
        }
    }
}
