package com.jieli.healthaide.data.vo.step;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.ParseEntityObject;
import com.jieli.healthaide.data.vo.parse.StepParserImpl;
import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.CHexConver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author : zhanghuanming
 * @e-mail : zhanghuanming@zh-jieli.com
 * @date : 6/16/21
 * @desc :步数的 24小时的数据，以一个小时为间隔统计
 */
public class StepDayVo extends StepBaseVo {

    @Override
    protected IParserModify getParser() {
        return new StepDayVo.Parser();
    }

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

        byte[] data = new byte[24 * 6];

        for (int i = 0; i < data.length; i += 6) {
            byte[] sourceData = CHexConver.int2byte2((int) (Math.random() * 1200));
            data[i] = sourceData[0];
            data[i + 1] = sourceData[1];

            byte[] distance = CHexConver.int2byte2((int) (Math.random() * 15));
            data[i + 2] = distance[0];
            data[i + 3] = distance[1];
            data[i + 1] = sourceData[1];

            byte[] kcal = CHexConver.int2byte2((int) (Math.random() * 800));
            data[i + 4] = kcal[0];
            data[i + 5] = kcal[1];
        }


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        entity.setData(ByteBuffer.allocate(data.length + 11+4)
                .put(HealthEntity.DATA_TYPE_STEP)

                //date
                .put((byte) ((calendar.get(Calendar.YEAR) >> 8) & 0xff))
                .put((byte) ((calendar.get(Calendar.YEAR)) & 0xff))
                .put((byte) ((calendar.get(Calendar.MONTH)+1) & 0xff))
                .put((byte) ((calendar.get(Calendar.DAY_OF_MONTH)) & 0xff))

                //crc
                .put((byte) (0))
                .put((byte) (0))

                .put((byte) (0))//version

                //space
                .put((byte) (5))


                //保留
                .put((byte) (0))
                .put((byte) (0))


                //time
                .put((byte) 6)
                .put((byte) 0)

                //len
                .put((byte) 0)
                .put((byte) data.length)

                .put(data)
                .array()
        );
        healthEntities.add(entity);
        return healthEntities;
    }

    private class Parser implements IParserModify<ParseEntity> {
        private StepParserImpl parser;


        public Parser() {
            parser = new StepParserImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            int space = 60;//这个space是view决定的,不是固件端决定的  todo 此处 有没有必要这样做，这样做法(APP的视图间隔不会因为小机的采样间隔而改变)
            List<ParseEntity> parseEntities = new ArrayList<>();
            if (!entities.isEmpty()) {//解析数据转换
                HealthEntity currentDayEntity = entities.get(0);
                List<HealthEntity> tempArray = new ArrayList<>();
                tempArray.add(currentDayEntity);
                parseEntities = parser.parse(tempArray);
            }

            int dataLen = 1440 / space;
            Integer[] stepArray = new Integer[dataLen];
            Arrays.fill(stepArray, 0);
            highLightIndex = Math.round((float) dataLen / 2);//高亮
            for (int i = 0; i < parseEntities.size(); i++) {//统计每个时间戳的值
                ParseEntityObject parseEntityObject = (ParseEntityObject) parseEntities.get(i);
                int step = (int) parseEntityObject.attr1;
                int distance = (int) parseEntityObject.attr2* 10;
                int cal = (int) parseEntityObject.attr3;
                int index = RelativeTimeUtil.getRelativeTimeOfDay(parseEntities.get(i).getStartTime(), space);
                if (step > 0) {
                    highLightIndex = index + 1;
                    totalStep = totalStep + step;
                    max = Math.max(step, max);
                }
                if (distance > 0) {
                    totalDistance += distance;
                }
                if (cal > 0) {
                    totalKcal += cal;
                }
                stepArray[index] += step;
            }
            int position = 1;
            for (Integer value : stepArray) {//将数据转换对应的ChartData
                StepChartData stepChartData = new StepChartData();
                stepChartData.index = position;
                stepChartData.value = value;
                data.add(stepChartData);
                position++;
            }
            return data;
        }
    }

}
