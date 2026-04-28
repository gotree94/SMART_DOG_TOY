package com.jieli.healthaide.ui.health.sleep.viewmodel;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.sleep.SleepYearVo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/2/21
 * @desc :
 */
public class SleepYearViewModel extends SleepBaseViewModel<SleepYearVo> {
    public SleepYearViewModel() {
        super(new HealthLiveData<>(new SleepYearVo()));
    }
}
