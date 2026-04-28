package com.jieli.healthaide.data.vo.weight;

import com.jieli.healthaide.ui.health.util.RelativeTimeUtil;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.Calendar;

/**
 * @ClassName: WeightMonthVo
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/8/23 11:20
 */
public class WeightMonthVo extends WeightWeekVo {
    public WeightMonthVo(){
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
