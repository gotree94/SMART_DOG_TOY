package com.jieli.healthaide.ui.health.pressure;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.pressure.PressureBaseVo;

import java.lang.reflect.InvocationTargetException;

/**
 * @ClassName: PressureViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 16:49
 */
public class PressureViewModel extends ViewModel {
    public MutableLiveData<String> timeIntervalLiveData = new MutableLiveData<>();
    public MutableLiveData<String> timeIntervalPressureValueLiveData = new MutableLiveData<>();
    public MutableLiveData<String> timeIntervalPressureStatusLiveData = new MutableLiveData<>();
    public MutableLiveData<String> averagePressureValueLiveData = new MutableLiveData<>();
    public MutableLiveData<String> averagePressureStatusLiveData = new MutableLiveData<>();
    public MutableLiveData<String> pressureAnalysisDescribeLiveData = new MutableLiveData<>();
    public MutableLiveData<String> pressureValueRangeLiveData = new MutableLiveData<>();
    private HealthLiveData<PressureBaseVo> voLiveData;

    public PressureViewModel(@NonNull PressureBaseVo vo) {
        voLiveData = new HealthLiveData<>(vo);
    }

    public HealthLiveData<PressureBaseVo> getVo() {
        return voLiveData;
    }

    public void refresh(String uid, long startTime, long endTime) {
        voLiveData.refresh(uid, startTime, endTime);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private PressureBaseVo pressureBaseVo;

        public Factory(@NonNull PressureBaseVo pressureBaseVo) {
            this.pressureBaseVo = pressureBaseVo;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (ViewModel.class.isAssignableFrom(modelClass)) {
                //noinspection TryWithIdenticalCatches
                try {
                    return modelClass.getConstructor(PressureBaseVo.class).newInstance(pressureBaseVo);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (InstantiationException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            return null;
        }
    }
}
