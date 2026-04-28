package com.jieli.healthaide.data.vo.sleep;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.SleepParseImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/27/21
 * @desc : 24小时的数据，以一个小时为间隔统计
 */
public class SleepYearVo extends SleepBaseVo {
    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        List<HealthEntity> data = new ArrayList<>();
        while (startTime < endTime) {
            calendar.add(Calendar.MONTH, 1);
            long lastMonth = calendar.getTimeInMillis();
            List<HealthEntity> list = new SleepMonthVo().createTestData(startTime, lastMonth);
            data.addAll(list);
            startTime = lastMonth;
        }
        return data;
    }

    @Override
    protected IParserModify getParser() {
        return new Parser();
    }

    private class Parser implements IParserModify<ParseEntity> {
        private SleepParseImpl sleepParse = new SleepParseImpl();

        @Override
        public List<ParseEntity> parse(List<HealthEntity> healthEntities) {
            List<ParseEntity> data = new ArrayList<>(12);
            analysis = new SleepParseImpl.Analysis();

            for (int i = 0; i < 12; i++) {
                SleepBarCharData entity = new SleepBarCharData();
                entity.index = i;
                data.add(entity);
            }
            SleepParseImpl parser = new SleepParseImpl();
            Calendar calendar = Calendar.getInstance();
            highLightIndex = -1;
            for (HealthEntity healthEntity : healthEntities) {
                calendar.setTimeInMillis(healthEntity.getTime());
                int month = calendar.get(Calendar.MONTH);
                List<ParseEntity> days = parser.parse(healthEntity);
                for (ParseEntity parseEntity : days) {
                    int type = (int) parseEntity.getValue();
                    if (type != 4) {
                        SleepBarCharData sleepBarCharData = (SleepBarCharData) data.get(month);
                        sleepBarCharData.data[type] += (parseEntity.getEndTime() - parseEntity.getStartTime());
                    }
                }
            }

            for (int i = 0; i < data.size(); i++) {
                SleepBarCharData sleepBarCharData = (SleepBarCharData) data.get(i);
                float sum = sleepBarCharData.data[0] + sleepBarCharData.data[1] + sleepBarCharData.data[2] + sleepBarCharData.data[3];
                max = Math.max(sum, max);
                if (sum != 0) {
                    highLightIndex = i + 1;
                }
            }
            if (highLightIndex == -1) {
                highLightIndex = Math.round((float) data.size() / 2);
            }
            statisticData(healthEntities);
            return data;
        }

        private void statisticData(List<HealthEntity> healthEntities) {
            long deepTime = 0;
            long shallowSleepTime = 0;
            long remTime = 0;
            long wakeTime = 0;
            long napTime = 0;
            int days = 0;
            int wakeCount = 0;
            int napDays = 0;

            int sleepGradeSum = 0;
            int deepSleepGradeSum = 0;
            int sleepGradeCount = 0;
            int deepSleepGradeCount = 0;

            for (HealthEntity healthEntity : healthEntities) {
                List<ParseEntity> entities = sleepParse.parse(healthEntity);
                SleepParseImpl.Analysis parseAnalysis = sleepParse.analysis;
                if (parseAnalysis.analysisSleepGrade != 0) {
                    sleepGradeSum += parseAnalysis.analysisSleepGrade;
                    sleepGradeCount++;
                }
                if (parseAnalysis.analysisDeepSleepGrade != 0) {
                    deepSleepGradeSum += parseAnalysis.analysisDeepSleepGrade;
                    deepSleepGradeCount++;
                }
                boolean hasDarkSleepData = false;
                boolean hasNapSleepData = false;
                for (ParseEntity entity : entities) {
                    int value = (int) entity.getValue();
                    if (value < 4) {
                        hasDarkSleepData = true;
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
                        hasNapSleepData = true;
                    }
                }
                if (hasDarkSleepData) {
                    days++;
                }
                if (hasNapSleepData) {
                    napDays++;
                }
            }

            if (sleepGradeSum != 0 && sleepGradeCount != 0) {
                analysis.analysisSleepGrade = sleepGradeSum / sleepGradeCount;
            }
            if (deepSleepGradeSum != 0 && deepSleepGradeCount != 0) {
                analysis.analysisDeepSleepGrade = deepSleepGradeSum / deepSleepGradeCount;
            }
            napList = new ArrayList<>();
            if (napDays != 0) {
                long napDuration = napTime / napDays;
                napList.add(new Nap(0, napDuration));
            }
            long total = deepTime + remTime + wakeTime + shallowSleepTime;
            if (days != 0) {
                total /= days;
                deepTime /= days;
                shallowSleepTime /= days;
                remTime /= days;
                wakeTime /= days;
                wakeCount /= days;
            }
            darkSleepTime = total;
            deepSleepTime = deepTime;
            lightSleepTime = shallowSleepTime;
            remSleepTime = remTime;
            awakeTime = wakeTime;
            napSleepTime = napTime;
            awakeNum = wakeCount;


        }

    }
}
