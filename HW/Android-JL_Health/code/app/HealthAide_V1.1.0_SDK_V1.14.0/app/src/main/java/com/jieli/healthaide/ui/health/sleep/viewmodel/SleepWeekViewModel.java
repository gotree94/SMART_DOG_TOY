package com.jieli.healthaide.ui.health.sleep.viewmodel;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.sleep.SleepWeekVo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/2/21
 * @desc :
 */
public class SleepWeekViewModel extends SleepBaseViewModel<SleepWeekVo> {
    public SleepWeekViewModel() {
        super(new HealthLiveData<>(new SleepWeekVo()));
    }
}
