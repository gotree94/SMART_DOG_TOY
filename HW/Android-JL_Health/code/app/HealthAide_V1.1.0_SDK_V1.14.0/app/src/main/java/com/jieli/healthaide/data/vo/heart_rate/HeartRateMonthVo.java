package com.jieli.healthaide.data.vo.heart_rate;

import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.Calendar;

/**
 * @ClassName: BloodOxygenMonthVo
 * @Description: 心率月数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class HeartRateMonthVo extends HeartRateWeekVo {

    public HeartRateMonthVo() {
        super();
    }

    @Override
    protected int getDayOfPosition(long time) {
        return RelativeTimeUtil.getRelativeDayOfMonth(time);
    }

    @Override
    protected int getDataAllCount(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int max = CalendarUtil.getDaysOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        return max;
    }
}
