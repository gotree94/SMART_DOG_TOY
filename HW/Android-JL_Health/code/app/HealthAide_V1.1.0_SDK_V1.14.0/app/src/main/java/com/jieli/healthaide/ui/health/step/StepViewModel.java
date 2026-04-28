package com.jieli.healthaide.ui.health.step;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.data.vo.livedatas.HealthLiveData;
import com.jieli.healthaide.data.vo.step.StepBaseVo;

import java.lang.reflect.InvocationTargetException;

/**
 * @ClassName: MovementViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/8 16:49
 */
public class StepViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    public MutableLiveData<String> timeIntervalLiveData = new MutableLiveData<>();
    public MutableLiveData<String> timeIntervalStepLiveData = new MutableLiveData<>();
    private HealthLiveData<StepBaseVo> voLiveData;

    public StepViewModel(@NonNull StepBaseVo vo) {
        voLiveData = new HealthLiveData<>(vo);
    }

    public HealthLiveData<StepBaseVo> getVo() {
        return voLiveData;
    }

    public void refresh(String uid, long startTime, long endTime) {
        voLiveData.refresh(uid, startTime, endTime);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private StepBaseVo stepBaseVo;

        public Factory(@NonNull StepBaseVo stepBaseVo) {
            this.stepBaseVo = stepBaseVo;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (ViewModel.class.isAssignableFrom(modelClass)) {
                //noinspection TryWithIdenticalCatches
                try {
                    return modelClass.getConstructor(StepBaseVo.class).newInstance(stepBaseVo);
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
