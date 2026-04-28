package com.jieli.healthaide.data.vo.heart_rate;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.HeartRateParseImpl;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: PressureWeekVo
 * @Description: 心率周数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class HeartRateWeekVo extends HeartRateBaseVo {

    @Override
    protected IParserModify getParser() {
        return new HeartRateWeekVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> list = new ArrayList<>();
        long time = CalendarUtil.removeTime(startTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        for (int i = 0; i < 7; i++) {
            if (Math.random() * 2 > 0.5) {
                list.addAll(new HeartRateDayVo().createTestData(startTime, startTime));
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
        private HeartRateParseImpl parser;


        public Parser() {
            parser = new HeartRateParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            ArrayList<ParseEntity> data = new ArrayList<>();
            int dataLen = getDataAllCount(startTime);
            ParseEntity[] dataArray = new ParseEntity[dataLen];
            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int restingSum = 0;
            int restingCount = 0;
            int temptRestingAvg = 0;
            if (!entities.isEmpty()) {
                for (HealthEntity entity : entities) {
                    int restingHeartRate = 0;
                    byte[] reservedByte = new byte[2];
                    System.arraycopy(entity.getData(), 9, reservedByte, 0, reservedByte.length);
                    if (reservedByte[0] != (byte) 0xff) {
                        restingHeartRate = reservedByte[0] & 0xff;//currentDayEntity.get todo 应该从数据库中获取，但是现在的数据库没有这个静息心率
                        restingSum = restingSum + restingHeartRate;
                        restingCount = restingCount + 1;
                    }
                    Result result = parseAnalysis(entity);
                    if (null != result) {
                        int index = getDayOfPosition(entity.getTime());
                        maxVal = Math.max(result.max, maxVal);
                        minVal = Math.min(result.min, minVal);
                        HeartRateCharData heartRateCharData = new HeartRateCharData(index, result.max, result.min);
//                        if (restingHeartRate != 0) {
                        heartRateCharData.restingRate = restingHeartRate;
//                        }
                        dataArray[index - 1] = heartRateCharData;
                    }
                }
            }
            if (restingCount != 0 && restingSum != 0) {
                temptRestingAvg = restingSum / restingCount;
            }
            max = maxVal;
            min = minVal;
            restingAvg = temptRestingAvg;
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < dataArray.length; i++) {
                ParseEntity parseEntity = dataArray[i];
                if (null == parseEntity) {
                    HeartRateCharData heartRateCharData = new HeartRateCharData(i + 1, 0, 0);
                    dataArray[i] = heartRateCharData;
                } else {
                    highLightIndex = i + 1;
                }
            }
            data.addAll(Arrays.asList(dataArray));
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
                if (value > 0) {//因为可能会用0来代表没有数据所以需要跳过
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
