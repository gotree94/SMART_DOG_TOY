package com.jieli.healthaide.ui.health.sleep.charts.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.util.CalendarUtil;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/3/21 8:33 PM
 * @desc :
 */
public class MonthValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        if (value < 1 || value > 31) return "";
        return CalendarUtil.formatString("%d%s", (int) value, HealthApplication.getAppViewModel().getApplication().getString(R.string.calendar_type_day));
    }

}
