package com.jieli.healthaide.data.vo.blood_oxygen;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.BloodOxygenParseImpl;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: BloodOxygenDayVo
 * @Description: 血氧天数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class BloodOxygenDayVo extends BloodOxygenBaseVo {
    public float lastValue;//最新的血氧数据
    public long lastTime;

    @Override
    protected IParserModify getParser() {
        return new BloodOxygenDayVo.Parser();
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

        byte[] data = new byte[24];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (Math.random() * 15 + 85);
        }

        entity.setData(data);
        healthEntities.add(entity);
        return healthEntities;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private final BloodOxygenParseImpl parser;


        public Parser() {
            parser = new BloodOxygenParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int space = 30;//这个space是view决定的,不是固件端决定的
            List<ParseEntity> parseEntities = new ArrayList<>();
            if (!entities.isEmpty()) {
                HealthEntity currentDayEntity = entities.get(0);
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(currentDayEntity);
                parseEntities = parser.parse(tempArray);
            }

            int maxVal = VALUE_MIN;
            int minVal = VALUE_MAX;
            int dataLen = 1440 / space;
            Integer[] dataArray = new Integer[dataLen];
            Integer[] numArray = new Integer[dataLen];
            Arrays.fill(dataArray, 0);
            Arrays.fill(numArray, 0);
            lastValue = 0;
            highLightIndex = Math.round((float) dataLen / 2);
            for (int i = 0; i < parseEntities.size(); i++) {
                int value = (int) parseEntities.get(i).getValue();
                if (value <= 0) continue;
                int index = RelativeTimeUtil.getRelativeTimeOfDay(parseEntities.get(i).getStartTime(), space);
                highLightIndex = index + 1;
                lastValue = value;
                lastTime = parseEntities.get(i).getStartTime();
                dataArray[index] += value;
                numArray[index]++;
                maxVal = Math.max(value, maxVal);
                minVal = Math.min(value, minVal);
            }
            for (int i = 0; i < dataArray.length; i++) {
                int value = 0;
                if (numArray[i] != 0 && dataArray[i] != 0) {
                    value = dataArray[i] / numArray[i];
                }
                BloodOxygenBarCharData bloodOxygenBarCharData = new BloodOxygenBarCharData(i + 1, value, value);
                data.add(bloodOxygenBarCharData);
            }
            max = maxVal;
            min = minVal;
            return data;
        }
    }

}
