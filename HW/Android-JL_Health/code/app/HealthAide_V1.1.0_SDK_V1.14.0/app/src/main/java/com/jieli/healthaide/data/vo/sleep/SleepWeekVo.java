package com.jieli.healthaide.data.vo.sleep;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.parse.IParserModify;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.data.vo.parse.SleepParseImpl;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/27/21
 * @desc : 24小时的数据，以一个小时为间隔统计
 */
public class SleepWeekVo extends SleepBaseVo {
    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {

        long time = CalendarUtil.removeTime(startTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        List<HealthEntity> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (Math.random() * 2 > 0.5) {
                list.addAll(new SleepDayVo().createTestData(startTime, startTime));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
        }

        return list;
    }

    @Override
    protected IParserModify getParser() {
        return new Parser();
    }

    private class Parser implements IParserModify<ParseEntity> {
        private SleepParseImpl sleepParse = new SleepParseImpl();

        @Override
        public List<ParseEntity> parse(List<HealthEntity> healthEntities) {
            List<ParseEntity> weekSleepEntities = new ArrayList<>(7);
            analysis = new SleepParseImpl.Analysis();
            int sleepGradeSum = 0;
            int deepSleepGradeSum = 0;
            int sleepGradeCount = 0;
            int deepSleepGradeCount = 0;

            for (int i = 0; i < 7; i++) {
                SleepBarCharData weekSleepEntity = new SleepBarCharData();
                weekSleepEntity.index = i;
                weekSleepEntities.add(weekSleepEntity);
            }
            highLightIndex = -1;
            for (HealthEntity entity : healthEntities) {
                List<ParseEntity> parseEntities = sleepParse.parse(entity);
                SleepParseImpl.Analysis parseAnalysis = sleepParse.analysis;
                if (parseAnalysis.analysisSleepGrade != 0) {
                    sleepGradeSum += parseAnalysis.analysisSleepGrade;
                    sleepGradeCount++;
                }
                if (parseAnalysis.analysisDeepSleepGrade != 0) {
                    deepSleepGradeSum += parseAnalysis.analysisDeepSleepGrade;
                    deepSleepGradeCount++;
                }

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(entity.getTime());
                int week = c.get(Calendar.DAY_OF_WEEK) - 2;
                if (week == -1) {
                    week = 6;
                }

                SleepBarCharData weekSleepEntity = (SleepBarCharData) weekSleepEntities.get(week);
                weekSleepEntity.data = new float[4];
                weekSleepEntity.index = week;
                for (ParseEntity parseEntity : parseEntities) {
                    int type = (int) parseEntity.getValue();
                    if (type != 4) {
                        weekSleepEntity.data[type] += (parseEntity.getEndTime() - parseEntity.getStartTime());
                    }
                }
            }
            if (sleepGradeSum != 0 && sleepGradeCount != 0) {
                analysis.analysisSleepGrade = sleepGradeSum / sleepGradeCount;
            }
            if (deepSleepGradeSum != 0 && deepSleepGradeCount != 0) {
                analysis.analysisDeepSleepGrade = deepSleepGradeSum / deepSleepGradeCount;
            }

            for (int i = 0; i < weekSleepEntities.size(); i++) {
                SleepBarCharData weekSleepEntity = (SleepBarCharData) weekSleepEntities.get(i);
                float sum = weekSleepEntity.data[0] + weekSleepEntity.data[1] + weekSleepEntity.data[2] + weekSleepEntity.data[3];
                max = Math.max(sum, max);
                if (sum != 0) {
                    highLightIndex = i + 1;
                }
            }
            if (highLightIndex == -1) {
                highLightIndex = Math.round((float) weekSleepEntities.size() / 2);
            }
            statisticData(healthEntities);
            return weekSleepEntities;
        }

        private void statisticData(List<HealthEntity> healthEntities) {
//            statistics = new ArrayList<>();
            long deepTime = 0;
            long shallowSleepTime = 0;
            long remTime = 0;
            long wakeTime = 0;
            long napTime = 0;
            int napDays = 0;
            int days = 0;
            int wakeCount = 0;
            for (HealthEntity healthEntity : healthEntities) {
                List<ParseEntity> entities = sleepParse.parse(healthEntity);
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
