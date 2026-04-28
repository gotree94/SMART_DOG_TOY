package com.jieli.healthaide.data.vo.livedatas;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.BaseParseVo;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;

/**
 * @ClassName: HealthPreviewLiveData
 * @Description: 获取指定时间的LiveData数据
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/7/5 19:31
 */
public class HealthPreviewLiveData<T extends BaseParseVo> extends LiveData<T> {
    private static final String TAG = HealthPreviewLiveData.class.getSimpleName();
    public static final int TYPE_HEALTH_PREVIEW_LAST_DAY = 0;
    public static final int TYPE_HEALTH_PREVIEW_LAST_DATA = 1;
    private int type;
    private String uid;
    private long startTime;
    private long endTime;
    private boolean test = false;

    public HealthPreviewLiveData(@NonNull T t, int type) {
        super(t);
        this.type = type;
    }

    public void refresh(String uid, long startTime, long endTime) {
        this.uid = uid;
        this.startTime = startTime;
        this.endTime = endTime;
        loadFromData();
    }

    public void refresh(String uid) {
        this.uid = uid;
        loadFromData();
    }

    private void loadFromData() {
        T t = getValue();
        assert t != null;
        t.setStartTime(startTime);
        t.setEndTime(endTime);
        LiveData<HealthEntity> healthEntityLiveData;
        if (type == TYPE_HEALTH_PREVIEW_LAST_DAY) {
            healthEntityLiveData = /*test ? t.createTestData(startTime, endTime) :*/ HealthDataDbHelper.getInstance().getHealthDao().findHealthLiveDataByDate(t.getType(), uid, startTime, endTime);
        } else {
            JL_Log.d(TAG, "loadFromData", "type :" + t.getType() + " uid : " + uid);
            healthEntityLiveData = /*test ? t.createTestData(startTime, endTime) :*/ HealthDataDbHelper.getInstance().getHealthDao().findHealthLiveDataLast(t.getType(), uid);
        }
        healthEntityLiveData.observeForever(mHealthEntityObserver);
    }

    private final Observer<HealthEntity> mHealthEntityObserver = new Observer<HealthEntity>() {
        @Override
        public void onChanged(HealthEntity entity) {
            if (entity == null) return;
            T t = getValue();
            assert t != null;
            t.setStartTime(startTime);
            t.setEndTime(endTime);
            ArrayList<HealthEntity> tempList = new ArrayList<>();
            tempList.add(entity);
            t.setHealthEntities(tempList);
            postValue(getValue());
        }
    };
}
