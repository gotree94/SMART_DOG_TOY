package com.jieli.healthaide.util;

import java.util.ArrayList;

/**
 * @ClassName: ValueUtil
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/11/10 11:13
 */
public class ValueUtil {
    /**
     * 分析占比
     *
     * @param
     * @return
     */
    public static ArrayList<Integer> analysisPercent(Integer... data) {
        ArrayList<Integer> percent = new ArrayList<>();
        if (data.length == 0) return percent;
        //考虑算后的百分比情况13.1 , 16.5 , 50.5 ,19.9  或0，29.6，50.5，19.9
        int allValue = 0;
        for (Integer i : data) {
            allValue += i;
        }
        final int ALL_VALUE_EMPTY = 1;
        allValue = allValue != 0 ? allValue : ALL_VALUE_EMPTY;
        Remainder[] remainders = new Remainder[data.length];
        int offset = 0;
        int remainderSum = 100;
        for (Integer i : data) {
            Integer iPercent = i * 100 / allValue;
            float iRemainder = i * 100 % allValue;
            remainders[offset] = new Remainder(offset, iRemainder);
            percent.add(iPercent);
            System.out.println("remainderSum iPercent: " + iPercent);
            remainderSum -= iPercent;
            System.out.println("remainderSum1 : " + remainderSum);
            offset++;
        }
        System.out.println("remainderSum : " + remainderSum);
        bubbleSort(remainders);
        if (remainderSum != 100 && remainderSum < percent.size()) {
            offset = data.length - 1;
            for (int i = 0; i < remainderSum; i++) {
                addPercent(remainders[offset - i].position, percent);
            }
        }
        return percent;
    }


    /**
     * 给对应的百分比加一
     */
    private static void addPercent(int position, ArrayList<Integer> percents) {
        Integer percent = percents.get(position);
        percent++;
        percents.remove(position);
        percents.add(position, percent);
    }

    /**
     * 对余数冒泡排序 小到大
     */
    private static void bubbleSort(Remainder[] arr) {
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
