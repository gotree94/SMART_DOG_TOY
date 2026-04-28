package com.jieli.healthaide.ui.health.weight;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;

import java.lang.reflect.InvocationTargetException;

/**
 * @ClassName: 体重ViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 16:49
 */
public class WeightViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    public MutableLiveData<String> timeIntervalLiveData = new MutableLiveData<>();
    public MutableLiveData<String> timeIntervalWeightValueLiveData = new MutableLiveData<>();
    private HealthLiveData<WeightBaseVo> voLiveData;

    public WeightViewModel(@NonNull WeightBaseVo vo) {
        voLiveData = new HealthLiveData<>(vo);
    }

    public HealthLiveData<WeightBaseVo> getVo() {
        return voLiveData;
    }

    public void refresh(String uid, long startTime, long entTime) {
        voLiveData.refresh(uid, startTime, entTime);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private WeightBaseVo weightBaseVo;

        public Factory(@NonNull WeightBaseVo weightBaseVo) {
            this.weightBaseVo = weightBaseVo;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (ViewModel.class.isAssignableFrom(modelClass)) {
                //noinspection TryWithIdenticalCatches
                try {
                    return modelClass.getConstructor(WeightBaseVo.class).newInstance(weightBaseVo);
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
