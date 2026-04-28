package com.jieli.healthaide.data.vo.blood_oxygen;

import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.Calendar;

/**
 * @ClassName: BloodOxygenMonthVo
 * @Description: 血氧月数据统计Vo
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/6/2 17:40
 */
public class BloodOxygenMonthVo extends BloodOxygenWeekVo {

    public BloodOxygenMonthVo() {
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
        return CalendarUtil.getDaysOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
    }
}
