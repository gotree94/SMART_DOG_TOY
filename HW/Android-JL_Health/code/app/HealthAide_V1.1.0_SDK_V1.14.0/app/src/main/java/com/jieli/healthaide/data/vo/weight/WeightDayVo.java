package com.jieli.healthaide.data.vo.weight;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.WeightParserImpl;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.CHexConver;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: WeightDayVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/8/23 11:18
 */
public class WeightDayVo extends WeightBaseVo {

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> healthEntities = new ArrayList<>();
        HealthEntity entity = new HealthEntity();
        entity.setSync(false);
        entity.setSpace((byte) 60);
        entity.setId(CalendarUtil.removeTime(startTime));
        entity.setTime(startTime);
        entity.setVersion((byte) 0);
        entity.setType(getType());

        byte[] data = new byte[2];
        data[0] = CHexConver.intToByte((int) (Math.random() * 250));
        data[1] = CHexConver.intToByte((int) (Math.random() * 99));
        entity.setData(data);
        healthEntities.add(entity);
        return healthEntities;
    }

    @Override
    protected IParserModify getParser() {
        return new Parser();
    }

    private class Parser implements IParserModify<ParseEntity> {
        private WeightParserImpl parser;

        Parser() {
            parser = new WeightParserImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            if (entities == null) return data;
            List<ParseEntity> parseEntities = new ArrayList<>();
            if (!entities.isEmpty()) {//byte原始数据 转换成 double数据
                HealthEntity currentDayEntity = entities.get(0);
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(currentDayEntity);
                parseEntities = parser.parse(tempArray);
            }
            //将设备space间隔存储的数据，转存至view 的space间隔的数组
            int space = 1;
            int dataLen = 1440 / space;
            Double[] dataArray = new Double[dataLen];
            for (int i = 0; i < dataArray.length; i++) {//初始化对应时间戳默认数据(0)的数组
                dataArray[i] = 0d;
            }
            highLightIndex = -1;//高亮
            double valueSum = 0;
            double hasDataCount = 0;
            for (int i = 0; i < parseEntities.size(); i++) {
                double value = parseEntities.get(i).getValue();
                long startTime = parseEntities.get(i).getStartTime();
                int index = RelativeTimeUtil.getRelativeTimeOfDay(startTime, space);
                if (value > 0) {
                    highLightIndex = Math.max(highLightIndex, index + 1);
                    maxVal = Math.max(value, maxVal);
                    minVal = minVal == 0d ? value : Math.min(minVal, value);
                    dataArray[index] = value;
                    valueSum += value;
                    hasDataCount++;
                }
            }
            if (highLightIndex == -1) {
                highLightIndex = Math.round((float) dataLen / 2);
            }
            for (int i = 0; i < dataArray.length; i++) {
                double value = dataArray[i];
                WeightBarCharData weightBarCharData = new WeightBarCharData();
                weightBarCharData.index = i;
                weightBarCharData.value = value;
                data.add(weightBarCharData);
            }
            if (!entities.isEmpty()) {//分析数据
                changeRange = parseEntities.get(0).getValue() - parseEntities.get(parseEntities.size() - 1).getValue();
                averageVal = hasDataCount == 0 ? 0 : valueSum / hasDataCount;
            } else {
                maxVal = 0;
                minVal = 0;
            }
            return data;
        }
    }

}
