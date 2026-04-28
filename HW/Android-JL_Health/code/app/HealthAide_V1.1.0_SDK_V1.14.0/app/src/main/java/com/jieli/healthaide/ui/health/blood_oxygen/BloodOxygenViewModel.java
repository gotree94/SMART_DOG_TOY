package com.jieli.healthaide.ui.health.blood_oxygen;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.data.vo.blood_oxygen.BloodOxygenBaseVo;
import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;

import java.lang.reflect.InvocationTargetException;


/**
 * @ClassName: MovementViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 16:49
 */
public class BloodOxygenViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    public MutableLiveData<String> timeIntervalLiveData = new MutableLiveData<>();
    public MutableLiveData<String> timeIntervalBloodOxygenLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> timeIntervalBloodOxygenAvgVisibleLiveData = new MutableLiveData<>(false);
    private HealthLiveData<BloodOxygenBaseVo> voLiveData;

    public BloodOxygenViewModel(@NonNull BloodOxygenBaseVo bloodOxygenBaseVo) {
        voLiveData = new HealthLiveData<>(bloodOxygenBaseVo);
    }

    public HealthLiveData<BloodOxygenBaseVo> getVo() {
        return voLiveData;
    }

    public void refresh(String uid, long startTime, long endTime) {
        voLiveData.refresh(uid, startTime, endTime);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private BloodOxygenBaseVo bloodOxygenBaseVo;

        public Factory(@NonNull BloodOxygenBaseVo bloodOxygenBaseVo) {
            this.bloodOxygenBaseVo = bloodOxygenBaseVo;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (ViewModel.class.isAssignableFrom(modelClass)) {
                //noinspection TryWithIdenticalCatches
                try {
                    return modelClass.getConstructor(BloodOxygenBaseVo.class).newInstance(bloodOxygenBaseVo);
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
