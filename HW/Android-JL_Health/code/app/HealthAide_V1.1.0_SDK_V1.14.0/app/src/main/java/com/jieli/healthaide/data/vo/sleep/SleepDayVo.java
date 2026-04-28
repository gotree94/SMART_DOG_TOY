package com.jieli.healthaide.data.vo.sleep;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.SleepParseImpl;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.util.CHexConver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/27/21
 * @desc : 24小时的数据，以一个小时为间隔统计
 */
public class SleepDayVo extends SleepBaseVo {

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        List<HealthEntity> healthEntities = new ArrayList<>();
        HealthEntity entity = new HealthEntity();

        entity.setSync(false);
        entity.setSpace((byte) 0xff);
        entity.setId(CalendarUtil.removeTime(startTime));
        entity.setTime(startTime);
        entity.setVersion((byte) 0);
        entity.setType(getType());

        byte[] data = new byte[24 * 12 + 13];
        data[0] = (byte) 0x05;
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
        data[8] = (byte) 0x0ff;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        data[9] = (byte) hour;
        data[10] = (byte) min;
        int len = 24 * 12;
        byte[] lenByte = CHexConver.int2byte2(len);
        data[11] = lenByte[0];
        data[12] = lenByte[1];
        for (int i = 13; i < data.length; i++) {
            data[i] = (byte) (Math.random() * 4);
        }

        entity.setData(data);
        healthEntities.add(entity);
        return healthEntities;
    }

    @Override
    protected IParserModify getParser() {
        return new Parser();
    }

    private class Parser implements IParserModify<ParseEntity> {
        private SleepParseImpl parser;

        Parser() {
            parser = new SleepParseImpl();
        }

        @Override
        public List<ParseEntity> parse(List<HealthEntity> entities) {
            List<ParseEntity> data = new ArrayList<>();
            analysis = new SleepParseImpl.Analysis();
//            statistics = new ArrayList<>();
            napList = new ArrayList<>();
            long deepTime = 0;
            long shallowSleepTime = 0;
            long remTime = 0;
            long wakeTime = 0;
            long napTime = 0;
            int wakeCount = 0;
            if (!entities.isEmpty()) {
                data = parser.parse(entities.get(0));
                List<ParseEntity> tempList = new ArrayList<>();
                for (ParseEntity entity : data) {
                    int value = (int) entity.getValue();
                    if (value != 4) {
                        tempList.add(entity);
                    }
                    if (value == 0) {
                        deepTime += (entity.getEndTime() - entity.getStartTime());
                    } else if (value == 1) {
                        shallowSleepTime += (entity.getEndTime() - entity.getStartTime());
                    } else if (value == 2) {
                        remTime += (entity.getEndTime() - entity.getStartTime());
                    } else if (value == 3) {
                        wakeTime += (entity.getEndTime() - entity.getStartTime());
                        wakeCount++;
                    } else if (value == 4) {
                        napTime += (entity.getEndTime() - entity.getStartTime());
                        napList.add(new Nap(entity.getStartTime(), entity.getEndTime()));
                    }
                }
                data = tempList;
                analysis = parser.analysis;
            }
            darkSleepTime = deepTime + remTime + wakeTime + shallowSleepTime;
            deepSleepTime = deepTime;
            lightSleepTime = shallowSleepTime;
            remSleepTime = remTime;
            awakeTime = wakeTime;
            napSleepTime = napTime;
            awakeNum = wakeCount;
            return data;
        }
    }
}
