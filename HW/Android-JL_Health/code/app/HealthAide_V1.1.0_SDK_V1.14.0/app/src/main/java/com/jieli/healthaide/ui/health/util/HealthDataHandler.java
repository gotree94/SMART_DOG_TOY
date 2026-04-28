package com.jieli.healthaide.ui.health.util;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateBaseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.MUnitConverter;
import com.jieli.healthaide.ui.health.chart_common.Fill;
import com.jieli.healthaide.ui.health.entity.BloodOxygenEntity;
import com.jieli.healthaide.ui.health.entity.HeartRateEntity;
import com.jieli.healthaide.ui.health.entity.PressureEntity;
import com.jieli.healthaide.ui.health.entity.SleepEntity;
import com.jieli.healthaide.ui.health.entity.StepEntity;
import com.jieli.healthaide.ui.health.heartrate.charts.HearRateLineChartRendererModify;
import com.jieli.healthaide.util.ValueUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: HealthDataHandler
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/5/10 16:25
 */
public class HealthDataHandler {
    /*   *
     * 分析当天的步数信息
     *
     * @param altitude   当天的海拔信息
     * @param step       当天的步数信息
     * @param targetStep 目标步数
     * @return 步数分析的结果*/

    public static StepEntity convertStep(int totalStep, float totalDistance, int totalKCal, float totalClimb) {
        StepEntity stepEntity = new StepEntity();
        float distance;
        Converter mUnitConverter = new MUnitConverter().getConverter(BaseUnitConverter.getType());
        distance = (float) mUnitConverter.value(totalDistance * 10);
        distance = (float) (Math.round(distance)) / 1000;
        float climb = totalClimb;
        climb = (float) mUnitConverter.value(climb);
        climb = (float) (Math.round(climb * 10)) / 10;//保留1位小数
        stepEntity.setSteps(totalStep);
        stepEntity.setDistance(distance);
        stepEntity.setHeatQuantity(totalKCal);
        stepEntity.setHeight(climb);
        return stepEntity;
    }

    /**
     * 分析最近一天的心率信息
     *
     * @param healthEntity 一天的心率信息
     * @param leftTime     起始时间戳
     * @return 心率分析的结果
     */
    public static HeartRateEntity convertHeartRate(Context context, List<HeartRateBaseVo.HeartRateCharData> healthEntity, long leftTime) {
        int lastHeartRate = 0;
        ArrayList<Entry> normalDataArray = new ArrayList();
        Fill[] fills = new Fill[]{new Fill(context.getDrawable(R.drawable.bg_blood_oxygen_chart_shape_week_sel)), new Fill(context.getDrawable(R.drawable.bg_blood_oxygen_chart_shape_week_nol))};
        for (HeartRateBaseVo.HeartRateCharData heartRateEntity : healthEntity) {
            Entry entry;
            if (heartRateEntity.max > 0) {
                entry = new Entry(heartRateEntity.index, heartRateEntity.max, fills);
                lastHeartRate = (int) heartRateEntity.max;
            } else {
                entry = new Entry(heartRateEntity.index, HearRateLineChartRendererModify.Y_DEFAULT_EMPTY, fills);
            }
            normalDataArray.add(entry);
        }
        HeartRateEntity heartRateEntity = new HeartRateEntity();
        heartRateEntity.setData(normalDataArray);
        heartRateEntity.setLastHeartBeat(lastHeartRate);
        heartRateEntity.setLeftTime(leftTime);
        return heartRateEntity;
    }

    /**
     * 分析最近一晚上的睡眠质量信息
     *
     * @param leftTime 起始时间戳
     * @return Sleep分析的结果
     */
    public static SleepEntity convertSleep(long deepSleepTime, long lightSleepTime, long remSleepTime, long awakeTime, long napSleepTime, long leftTime) {
        List<Integer> analysisPercent = ValueUtil.analysisPercent((int) deepSleepTime, (int) lightSleepTime, (int) remSleepTime, (int) awakeTime, (int) napSleepTime);
        long totalTime = deepSleepTime + lightSleepTime + remSleepTime + awakeTime + napSleepTime;
        //todo 统计睡眠质量
        SleepEntity sleepEntity = new SleepEntity();
        sleepEntity.setEmpty(false);
        sleepEntity.setDeepSleepRatio(analysisPercent.get(0));
        sleepEntity.setLightSleepRatio(analysisPercent.get(1));
        sleepEntity.setRapidEyeMovementRatio(analysisPercent.get(2));
        sleepEntity.setSoberRatio(analysisPercent.get(3));
        sleepEntity.setNapRatio(analysisPercent.get(4));
        int min = (int) (totalTime / (60 * 1000));
        int hour = min / 60;
        min = min % 60;
        sleepEntity.setHour(hour);
        sleepEntity.setMin(min);
        sleepEntity.setLeftTime(leftTime);
        return sleepEntity;
    }

    /**
     * 分析最近一天的压力信息
     *
     * @param healthEntity 一天的压力信息
     * @return 心率分析的结果
     */
    public static PressureEntity convertPressure(Context context, List<ParseEntity> healthEntity, long leftTime) {
        PressureEntity pressureEntity = new PressureEntity();
        int lastPressure;
        ParseEntity lastParseEntity = healthEntity.get(healthEntity.size() - 1);
        lastPressure = (int) lastParseEntity.getValue();
        pressureEntity.setPressure(lastPressure);
        pressureEntity.setLeftTime(leftTime);
        return pressureEntity;
    }

    /**
     * @param lastPressureValue 血氧信息
     * @return 心率分析的结果
     */
    public static BloodOxygenEntity convertBloodOxygen(Context context, float lastPressureValue, long leftTime) {
        int lastPressure = (int) lastPressureValue;
        BloodOxygenEntity bloodOxygenEntity = new BloodOxygenEntity();
        bloodOxygenEntity.setBloodOxygen(lastPressure);
        bloodOxygenEntity.setLeftTime(leftTime);
        return bloodOxygenEntity;
    }
}
