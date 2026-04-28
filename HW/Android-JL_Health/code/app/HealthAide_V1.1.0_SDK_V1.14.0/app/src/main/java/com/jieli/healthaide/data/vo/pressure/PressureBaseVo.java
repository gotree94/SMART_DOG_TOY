package com.jieli.healthaide.data.vo.pressure;

import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: PressureBaseVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/11 12:00
 */
public abstract class PressureBaseVo extends BaseParseVo {
    protected int VALUE_MAX = 100;
    protected int VALUE_MIN = 0;
    public float max;
    public float min;
    public float pressureAvg;//平均压力
    public int highLightIndex;
    public List<Integer> analysisDataArray;//分析结果

    @Override
    public byte getType() {
        return HealthEntity.DATA_TYPE_PRESSURE;
    }

    public static class PressureChartData extends ParseEntity {
        public int index;
        public float value;
    }

    /**
     * 分析压力占比
     *
     * @param
     * @return
     */
    protected void analysisData(List<ParseEntity> sourceDataArray) {
        //todo 计算当日的平均压力，最大，最小值，各阶段有多少
        int highNum = 0;
        int mediumNum = 0;
        int normalNum = 0;
        int relaxNum = 0;
        for (ParseEntity parseEntity : sourceDataArray) {
            PressureChartData temp = (PressureChartData) parseEntity;
            float pressureValue = (float) temp.value;
            if (1 <= pressureValue && pressureValue < 30) {
                relaxNum++;
            } else if (30 <= pressureValue && pressureValue < 60) {
                normalNum++;
            } else if (60 <= pressureValue && pressureValue < 80) {
                mediumNum++;
            } else if (80 <= pressureValue && pressureValue <= 100) {
                highNum++;
            }
        }
        analysisDataArray = analysisPercent(highNum, mediumNum, normalNum, relaxNum);
    }

    protected ArrayList<Integer> analysisPercent(int highNum, int mediumNum, int normalNum, int relaxNum) {//考虑算后的百分比情况13.1 , 16.5 , 50.5 ,19.9  或0，29.6，50.5，19.9
        int allValue = highNum + mediumNum + normalNum + relaxNum;
        final int ALL_VALUE_EMPTY = 1;
        allValue = allValue != 0 ? allValue : ALL_VALUE_EMPTY;
        Integer highPercent = highNum * 100 / allValue;
        Integer mediumPercent = mediumNum * 100 / allValue;
        Integer normalPercent = normalNum * 100 / allValue;
        Integer relaxPercent = relaxNum * 100 / allValue;
        float highRemainder = highNum * 100 % allValue;
        float mediumRemainder = mediumNum * 100 % allValue;
        float normalRemainder = normalNum * 100 % allValue;
        float relaxRemainder = relaxNum * 100 % allValue;
        ArrayList<Integer> percent = new ArrayList<>();
        percent.add(relaxPercent);
        percent.add(normalPercent);
        percent.add(mediumPercent);
        percent.add(highPercent);
        Remainder[] remainders = new Remainder[]{new Remainder(3, highRemainder), new Remainder(2, mediumRemainder),
                new Remainder(1, normalRemainder), new Remainder(0, relaxRemainder)};
        bubbleSort(remainders);
        int remainderSum = 100 - highPercent - mediumPercent - normalPercent - relaxPercent;
        if (remainderSum == 1) {
            addPercent(remainders[3].position, percent);
        } else if (remainderSum == 2) {
            addPercent(remainders[3].position, percent);
            addPercent(remainders[2].position, percent);
        } else if (remainderSum == 3) {
            addPercent(remainders[3].position, percent);
            addPercent(remainders[2].position, percent);
            addPercent(remainders[1].position, percent);
        }
        return percent;
    }

    /**
     * 给对应的百分比加一
     */
    private void addPercent(int position, ArrayList<Integer> percents) {
        Integer percent = percents.get(position);
        percent++;
        percents.remove(position);
        percents.add(position, percent);
    }

    /**
     * 对余数冒泡排序 小到大
     */
    private void bubbleSort(Remainder[] arr) {
        Remainder temp;//定义一个临时变量
        for (int i = 0; i < arr.length - 1; i++) {//冒泡趟数
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j + 1].remainder < arr[j].remainder) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    private static class Remainder {
        int position;
        float remainder;

        public Remainder(int position, float remainder) {
            this.position = position;
            this.remainder = remainder;
        }
    }

}
