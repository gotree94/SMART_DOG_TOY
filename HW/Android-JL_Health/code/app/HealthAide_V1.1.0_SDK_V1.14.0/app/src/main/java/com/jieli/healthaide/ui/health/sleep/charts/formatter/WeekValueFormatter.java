package com.jieli.healthaide.ui.health.sleep.charts.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/3/21 8:33 PM
 * @desc :
 */
public class WeekValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        String[] weeks = new String[]{
                "周一",
                "周二",
                "周三",
                "周四",
                "周五",
                "周六",
                "周日",

        };

        if (value >= 0 | value <= 6f) {
            return weeks[(int) value];
        } else {
            return "";
        }
    }

}
