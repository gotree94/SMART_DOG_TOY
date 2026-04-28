package com.jieli.healthaide.data.vo.pressure;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.PressureParseImpl;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: PressureDayVo
 * @Description: 压力的周视图Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class PressureDayVo extends PressureBaseVo {

    @Override
    protected IParserModify getParser() {
        return new PressureDayVo.Parser();
    }

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> healthEntities = new ArrayList<>();

        HealthEntity entity = new HealthEntity();

        entity.setSync(false);
        entity.setSpace((byte) 30);
        entity.setId(CalendarUtil.removeTime(startTime));
        entity.setTime(startTime);
        entity.setVersion((byte) 0);
        entity.setType(getType());

        byte[] data = new byte[24 * 2];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (Math.random() * 100);
        }

        entity.setData(data);
        healthEntities.add(entity);
        return healthEntities;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private PressureParseImpl parser;


        public Parser() {
            parser = new PressureParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int space = 1;//这个space是view决定的,不是固件端决定的
            List<ParseEntity> parseEntities = new ArrayList<>();
            if (!entities.isEmpty()) {
                HealthEntity currentDayEntity = entities.get(0);
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(currentDayEntity);
                parseEntities = parser.parse(tempArray);
            }

            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int pressureSum = 0;
            int pressureCount = 0;
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
                    pressureSum = pressureSum + value;
                    pressureCount = pressureCount + 1;
                    maxVal = Math.max(value, maxVal);
                    minVal = Math.min(value, minVal);
                }
                if (dataArray[index] == 0) {
                    dataArray[index] = value;
                } else if (value != 0) {
                    dataArray[index] = Math.min(dataArray[index], value);
                }
            }
            int position = 1;
            for (Integer value : dataArray) {
                PressureChartData pressureChartData = new PressureChartData();
                pressureChartData.index = position;
                pressureChartData.value = value;
                data.add(pressureChartData);
                position++;
            }
            pressureAvg = 0;
            if (pressureCount != 0 && pressureSum != 0) {
                pressureAvg = pressureSum / pressureCount;
            }
            analysisData(data);
            max = maxVal;
            min = minVal;
            return data;
        }
    }
}
