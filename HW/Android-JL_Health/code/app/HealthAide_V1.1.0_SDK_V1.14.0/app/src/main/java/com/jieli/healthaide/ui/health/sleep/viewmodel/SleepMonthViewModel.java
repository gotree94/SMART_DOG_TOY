package com.jieli.healthaide.ui.health.sleep.viewmodel;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.sleep.SleepMonthVo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/2/21
 * @desc :
 */
public class SleepMonthViewModel extends SleepBaseViewModel<SleepMonthVo> {
    public SleepMonthViewModel() {
        super(new HealthLiveData<>(new SleepMonthVo()));
    }
}
