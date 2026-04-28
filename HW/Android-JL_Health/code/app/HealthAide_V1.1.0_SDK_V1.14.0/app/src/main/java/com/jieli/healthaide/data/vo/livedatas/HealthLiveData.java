package com.jieli.healthaide.data.vo.livedatas;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/1/21
 * @desc :
 */
public class HealthLiveData<T extends BaseVo> extends LiveData<T> {
    private String uid;
    private long startTime;
    private long endTime;
    private LifecycleOwner owner;
    private LiveData<List<HealthEntity>> healthEntitiesLiveData = new MutableLiveData<>();
    private boolean test = false;

    public HealthLiveData(@NonNull T t) {
        super(t);
    }

    //todo 只适用于一个观察者，如果每次进来都postValue会有问题 ,且没有处理observeForever的情况
//    @Override
//    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
//        super.observe(owner, observer);
//        this.owner = owner;
//    }
//
//    @Override
//    public void observeForever(@NonNull Observer<? super T> observer) {
//        super.observeForever(observer);
//    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        super.removeObserver(observer);
        if (!hasObservers()) {
            healthEntitiesLiveData.removeObserver(mHealthEntityObserver);
        }
    }

    public void refresh(String uid, long startTime, long endTime) {
        this.uid = uid;
        this.startTime = startTime;
        this.endTime = endTime;
        loadFromData();
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    private void loadFromData() {
        T t = getValue();
        assert t != null;
        t.setStartTime(startTime);
        t.setEndTime(endTime);

        healthEntitiesLiveData.removeObserver(mHealthEntityObserver);
        if (test) {
            if (Math.random() > 0) {
                healthEntitiesLiveData = new MutableLiveData<>(t.createTestData(startTime, endTime));
            } else {
                List<HealthEntity> testList = new ArrayList<>();
                healthEntitiesLiveData = new MutableLiveData<>(testList);
            }
        } else {
            healthEntitiesLiveData = HealthDataDbHelper.getInstance().getHealthDao().findHealthByDate(t.getType(), uid, startTime, endTime);
        }
        healthEntitiesLiveData.observeForever(mHealthEntityObserver);

    }

    private final Observer<List<HealthEntity>> mHealthEntityObserver = new Observer<List<HealthEntity>>() {
        @Override
        public void onChanged(List<HealthEntity> entities) {
            if (entities == null) return;
            T t = getValue();
            assert t != null;
            t.setStartTime(startTime);
            t.setEndTime(endTime);
            t.setHealthEntities(entities);
            postValue(getValue());
        }
    };


}
