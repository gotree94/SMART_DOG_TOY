package com.jieli.healthaide.data.vo.heart_rate;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.HeartRateParseImpl;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.CHexConver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName: HeartRateDayVo
 * @Description: 心率天数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/8 17:07
 */
public class HeartRateDayVo extends HeartRateBaseVo {

    @Override
    protected IParserModify getParser() {
        return new HeartRateDayVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> healthEntities = new ArrayList<>();

        HealthEntity entity = new HealthEntity();

        entity.setSync(false);
        entity.setSpace((byte) 5);
        entity.setId(CalendarUtil.removeTime(startTime));
        entity.setTime(startTime);
        entity.setVersion((byte) 0);
        entity.setType(getType());

        byte[] data = new byte[24 * 12 + 13];
        data[0] = (byte) 0x03;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        byte[] yearBytes = CHexConver.int2byte2(year);
        data[1] = yearBytes[0];
        data[2] = yearBytes[1];
        data[3] = (byte) month;
        data[4] = (byte) day;
        data[7] = (byte) 0x00;
        data[8] = (byte) 0x05;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        data[9] = (byte) hour;
        data[10] = (byte) min;
        int len = 24 * 12;
        byte[] lenByte = CHexConver.int2byte2(len);
        data[11] = lenByte[0];
        data[12] = lenByte[1];
        for (int i = 13; i < data.length; i++) {
            data[i] = (byte) (Math.random() * 160 + 40);
        }

        entity.setData(data);
        healthEntities.add(entity);
        return healthEntities;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private HeartRateParseImpl parser;


        public Parser() {
            parser = new HeartRateParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int space = 1;//这个space是view决定的,不是固件端决定的
            int restingHeartRate = 0;
            List<ParseEntity> parseEntities = new ArrayList<>();
            if (!entities.isEmpty()) {
                HealthEntity currentDayEntity = entities.get(0);
                byte[] reservedByte = new byte[2];
                System.arraycopy(currentDayEntity.getData(), 9, reservedByte, 0, reservedByte.length);
                if (reservedByte[0] != (byte) 0xff) {
                    restingHeartRate = reservedByte[0] & 0xff;//currentDayEntity.get todo 应该从数据库中获取，但是现在的数据库没有这个静息心率
                }
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(currentDayEntity);
                parseEntities = parser.parse(tempArray);
            }

            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int dataLen = 1440 / space;
            Integer[] dataArray = new Integer[dataLen];
            for (int i = 0; i < dataArray.length; i++) {
                dataArray[i] = 0;
            }
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < parseEntities.size(); i++) {
                int value = (int) parseEntities.get(i).getValue();
                int index = RelativeTimeUtil.getRelativeTimeOfDay(parseEntities.get(i).getStartTime(), space);
                if (value > 0) {
                    highLightIndex = index + 1;
                }
                if (dataArray[index] == 0) {
                    dataArray[index] = value;
                } else if (value != 0) {
                    dataArray[index] = Math.min(dataArray[index], value);
                }
                if (value > 0) {
                    maxVal = Math.max(value, maxVal);
                    minVal = Math.min(value, minVal);
                }
            }
            int position = 1;
            for (Integer value : dataArray) {
                HeartRateCharData heartRateCharData = new HeartRateCharData(position, value, value);
                data.add(heartRateCharData);
                position++;
            }
            restingAvg = restingHeartRate;
            max = maxVal;
            min = minVal;
            return data;
        }
    }

}