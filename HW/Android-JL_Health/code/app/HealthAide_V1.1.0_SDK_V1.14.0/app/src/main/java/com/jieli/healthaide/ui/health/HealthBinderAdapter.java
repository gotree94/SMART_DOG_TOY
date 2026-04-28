package com.jieli.healthaide.ui.health;

import com.chad.library.adapter.base.BaseBinderAdapter;
import com.jieli.healthaide.ui.health.binder_adapter.BloodOxygenBinder;
import com.jieli.healthaide.ui.health.binder_adapter.HeartRateBinder;
import com.jieli.healthaide.ui.health.binder_adapter.MovementRecordBinder;
import com.jieli.healthaide.ui.health.binder_adapter.PressureBinder;
import com.jieli.healthaide.ui.health.binder_adapter.SleepBinder;
import com.jieli.healthaide.ui.health.binder_adapter.WeightBinder;
import com.jieli.healthaide.ui.health.entity.BloodOxygenEntity;
import com.jieli.healthaide.ui.health.entity.HeartRateEntity;
import com.jieli.healthaide.ui.health.entity.MovementRecordEntity;
import com.jieli.healthaide.ui.health.entity.PressureEntity;
import com.jieli.healthaide.ui.health.entity.SleepEntity;
import com.jieli.healthaide.ui.health.entity.WeightEntity;

/**
 * @ClassName: HealthBinderAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:26
 */
public class HealthBinderAdapter extends BaseBinderAdapter {
    public HealthBinderAdapter() {
        addItemBinder(MovementRecordEntity.class, new MovementRecordBinder());
        addItemBinder(HeartRateEntity.class, new HeartRateBinder());
        addItemBinder(SleepEntity.class, new SleepBinder());
        addItemBinder(WeightEntity.class, new WeightBinder());
        addItemBinder(BloodOxygenEntity.class, new BloodOxygenBinder());
        addItemBinder(PressureEntity.class, new PressureBinder());

    }
}
