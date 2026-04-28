package com.jieli.healthaide.ui.health.sleep.viewmodel;

import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.sleep.SleepBaseVo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/2/21
 * @desc :
 */
public class SleepBaseViewModel<T extends SleepBaseVo> extends ViewModel {


    private HealthLiveData<T> liveData;

    public SleepBaseViewModel(HealthLiveData<T> liveData) {
        this.liveData = liveData;
    }

    public HealthLiveData<T> getLiveData() {
        return liveData;
    }

    public void refresh(String uid, long startTime, long endTime) {
        liveData.refresh(uid,startTime, endTime);
    }
}
