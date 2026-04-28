package com.jieli.healthaide.data.vo.step;

import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.Calendar;

/**
 * @author : zhanghuanming
 * @e-mail : zhanghuanming@zh-jieli.com
 * @date : 6/16/21
 * @desc : 步数的一个月的数据，dataLen不固定，以一天为间隔统计
 */
public class StepMonthVo extends StepWeekVo {
    public StepMonthVo() {
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
