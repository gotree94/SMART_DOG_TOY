package com.jieli.healthaide.data.vo.step;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

import java.util.List;

/**
 * @ClassName: StepBaseVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/15 11:12
 */
public abstract class StepBaseVo extends BaseParseVo {


    protected int totalStep;
    protected float totalDistance; //单位： m
    protected int totalKcal;//单位：千卡

    protected int avgStep;


    public int getAvgStep() {
        return avgStep;
    }

    public void setTotalStep(int totalStep) {
        this.totalStep = totalStep;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setTotalKcal(int totalKcal) {
        this.totalKcal = totalKcal;
    }

    public int getTotalStep() {
        return totalStep;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public int getTotalKcal() {
        return totalKcal;
    }


    public int highLightIndex;
    public int max = 0;//时间段最大值

    /**
     * 分析统计结果
     * Day：总步数（position 0） 平均步数（position 1）
     * Week Month Year： 总里程(position 0 这里的单位是m) 总消耗（position 1）
     */


    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_STEP;
    }


    public static class StepChartData extends ParseEntity {
        public int index;
        public float value;

        @Override
        public String toString() {
            return "StepChartData{" +
                    "index=" + index +
                    ", value=" + value +
                    '}';
        }
    }

    @Override
    protected void parse(List<HealthEntity> healthEntities) {
        totalDistance = 0;
        totalKcal = 0;
        totalStep = 0;
        max = 0;
        super.parse(healthEntities);

    }

    @Override
    public String toString() {
        return "StepBaseVo{" +
                "totalStep=" + totalStep +
                ", totalDistance=" + totalDistance +
                ", totalKcal=" + totalKcal +
                ", avgStep=" + avgStep +
                ", highLightIndex=" + highLightIndex +
                ", max=" + max +
                '}';
    }
}
