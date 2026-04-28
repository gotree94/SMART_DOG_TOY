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
public class SleepMonthVo extends SleepBaseVo {

    @Override
    public List<HealthEntity> createTestData(long startTime, long endTime) {
        long time = CalendarUtil.removeTime(startTime);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        List<HealthEntity> list = new ArrayList<>();
        int max = CalendarUtil.getDaysOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        for (int i = 0; i < max; i++) {
            if (Math.random() * 2 > 0.5) {
                list.addAll(new SleepDayVo().createTestData(startTime, startTime));
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
            startTime = c.getTimeInMillis();
        }
        return list;
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
        public List<ParseEntity> parse(List<HealthEntity> healthEntities) {
            List<ParseEntity> monthSleepEntities = new ArrayList<>();
            analysis = new SleepParseImpl.Analysis();
            int sleepGradeSum = 0;
            int deepSleepGradeSum = 0;
            int sleepGradeCount = 0;
            int deepSleepGradeCount = 0;

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startTime);
            int days = CalendarUtil.getDaysOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);

            for (int i = 0; i < days; i++) {
                SleepBarCharData entity = new SleepBarCharData();
                entity.data = new float[4];
                entity.index = i;
                monthSleepEntities.add(entity);
            }
            highLightIndex = -1;
            for (HealthEntity healthEntity : healthEntities) {
                List<ParseEntity> parseEntities = parser.parse(healthEntity);
                SleepParseImpl.Analysis parseAnalysis = parser.analysis;
                if (parseAnalysis.analysisSleepGrade != 0) {
                    sleepGradeSum += parseAnalysis.analysisSleepGrade;
                    sleepGradeCount++;
                }
                if (parseAnalysis.analysisDeepSleepGrade != 0) {
                    deepSleepGradeSum += parseAnalysis.analysisDeepSleepGrade;
                    deepSleepGradeCount++;
                }
                c.setTimeInMillis(healthEntity.getTime());
                int day = c.get(Calendar.DAY_OF_MONTH) - 1;
                SleepBarCharData entity = (SleepBarCharData) monthSleepEntities.get(day);
                entity.data = new float[4];
                for (ParseEntity parseEntity : parseEntities) {
                    int type = (int) parseEntity.getValue();
                    if (type != 4) {
                        entity.data[type] += (parseEntity.getEndTime() - parseEntity.getStartTime());
                    }
                }
            }
            if (sleepGradeSum != 0 && sleepGradeCount != 0) {
                analysis.analysisSleepGrade = sleepGradeSum / sleepGradeCount;
            }
            if (deepSleepGradeSum != 0 && deepSleepGradeCount != 0) {
                analysis.analysisDeepSleepGrade = deepSleepGradeSum / deepSleepGradeCount;
            }
            for (int i = 0; i < monthSleepEntities.size(); i++) {
                SleepBarCharData sleepBarCharData = (SleepBarCharData) monthSleepEntities.get(i);
                float sum = sleepBarCharData.data[0] + sleepBarCharData.data[1] + sleepBarCharData.data[2] + sleepBarCharData.data[3];
                max = Math.max(sum, max);
                if (sum != 0) {
                    highLightIndex = i + 1;
                }
            }
            if (highLightIndex == -1) {
                highLightIndex = Math.round((float) monthSleepEntities.size() / 2);
            }
            statisticData(healthEntities);
            return monthSleepEntities;
        }

        private void statisticData(List<HealthEntity> healthEntities) {
            long deepTime = 0;
            long shallowSleepTime = 0;
            long remTime = 0;
            long wakeTime = 0;
            long napTime = 0;
            int napDays = 0;
            int days = 0;
            int wakeCount = 0;
            for (HealthEntity healthEntity : healthEntities) {
                List<ParseEntity> entities = parser.parse(healthEntity);
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

         /*   statistics = new ArrayList<>();
            List<Integer> analysisPercent = analysisPercent((int) deepSleepTime, (int) shallowSleepTime, (int) remTime, (int) wakeTime);
            statistics.add(create(deepSleepTime, analysisPercent.get(0)));//百分比算法借鉴压力的
            statistics.add(create(shallowSleepTime, analysisPercent.get(1)));
            statistics.add(create(remTime, analysisPercent.get(2)));
            statistics.add(create(wakeTime, analysisPercent.get(3)));
            score = (int) (100.0 * (deepSleepTime + shallowSleepTime) / total);
            if (days == 0) {
//                hasDarkSleep = false;
            } else {
//                hasDarkSleep = true;
            }
            napList = new ArrayList<>();
            if (napDays != 0) {
                long napDuration = napTime / napDays;
                napList.add(new Nap(0, napDuration));
            }
            analysis = new ArrayList<>();
            analysis.add((int) ((total - srcWakeTime) / 60000));//夜间睡眠的总时间/分
            analysis.add(analysisPercent.get(0));//深睡比例
            analysis.add(analysisPercent.get(1));//浅睡比例
            analysis.add(analysisPercent.get(2));//快速眼动比例
            analysis.add(wakeNum);//清醒次数*/
        }
    }
}
