package com.jieli.healthaide.ui.health.sleep.viewmodel;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.sleep.SleepDayVo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/2/21
 * @desc :
 */
public class SleepDayViewModel extends SleepBaseViewModel<SleepDayVo> {
    public SleepDayViewModel() {
        super(new HealthLiveData<>(new SleepDayVo()));
    }
}
