package com.jieli.healthaide.ui.health;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.data.vo.livedatas.HealthPreviewLiveData;
import com.jieli.healthaide.data.vo.preview.PreviewBloodOxygenVo;
import com.jieli.healthaide.data.vo.preview.PreviewHeartRateVo;
import com.jieli.healthaide.data.vo.preview.PreviewPressureVo;
import com.jieli.healthaide.data.vo.preview.PreviewSleepVo;
import com.jieli.healthaide.data.vo.preview.PreviewStepVo;
import com.jieli.healthaide.data.vo.preview.PreviewWeightVo;
import com.jieli.healthaide.data.vo.weight.WeightBaseVo;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.KGUnitConverter;
import com.jieli.healthaide.tool.unit.MUnitConverter;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.health.entity.BloodOxygenEntity;
import com.jieli.healthaide.ui.health.entity.HeartRateEntity;
import com.jieli.healthaide.ui.health.entity.MovementRecordEntity;
import com.jieli.healthaide.ui.health.entity.PressureEntity;
import com.jieli.healthaide.ui.health.entity.SleepEntity;
import com.jieli.healthaide.ui.health.entity.StepEntity;
import com.jieli.healthaide.ui.health.entity.WeightEntity;
import com.jieli.healthaide.ui.health.util.HealthDataHandler;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.HealthDataQuery;
import com.jieli.jl_rcsp.model.device.health.HealthData;
import com.jieli.jl_rcsp.model.device.health.SportsSteps;
import com.jieli.jl_rcsp.util.JL_Log;

import java.text.SimpleDateFormat;

/**
 * @ClassName: HealthPreviewViewModel
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/4/30 9:43
 */
public class HealthPreviewViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private final Activity mActivity;
    private final WatchManager mWatchManager;
    private final HealthOpImpl mHealthOp;
    private MUnitConverter stepMUnitConverter;
    private final MutableLiveData<PreviewStepVo> realTimeLiveData = new MutableLiveData<>();

    public HealthPreviewViewModel(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("Activity is null.");
        }
        mActivity = activity;
        mWatchManager = WatchManager.getInstance();
        mHealthOp = mWatchManager.getHealthOp();
        mWatchManager.registerOnRcspEventListener(listener);
    }

    public boolean isWatchSystemInit() {
        return mWatchManager.isWatchSystemOk() && !mWatchManager.isBleChangeSpp();
    }

    public void readMyData() {
        if (mWatchManager.isConnected()) {
            readFromDevice();
        }
    }

    private void readFromDevice() {
        int mask = 0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE | (0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_STEP) | (0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN);
        byte[] subMask = new byte[]{0x01, 0x07, 0x01};
        byte version = 0;
        mHealthOp.readHealthData(mHealthOp.getConnectedDevice(), new HealthDataQuery(version, mask, subMask), null);
    }

    /**
     * @description 获取当前天的00:00的long
     */
    private long getTodayTime() {
        long currentTime = System.currentTimeMillis();
        final long oneDayTime = 86400000;
        long daySum = currentTime / oneDayTime;
        currentTime = oneDayTime * daySum;
        return currentTime;
    }

    @SuppressLint("SimpleDateFormat")
    public LiveData<MovementRecordEntity> getMovementRecordEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        LiveData<SportRecord> liveData = HealthDataDbHelper.getInstance().getSportRecordDao()
                .getLastLiveData(uid);
        return Transformations.map(liveData, sportRecord -> {
            MovementRecordEntity lastMoveRecordEntity = new MovementRecordEntity();
            if (sportRecord != null) {
                lastMoveRecordEntity.movementType = sportRecord.getType();
                lastMoveRecordEntity.distance = sportRecord.getDistance() / 1000.f;
                lastMoveRecordEntity.dateTag = CustomTimeFormatUtil.isLocaleChinese() ? new SimpleDateFormat("MM月dd日").format(sportRecord.getStartTime())
                        : CustomTimeFormatUtil.dateFormat("MM/dd").format(sportRecord.getStartTime());
            }

            return lastMoveRecordEntity;
        });
    }


    public LiveData<StepEntity> getStepEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        long currentTime = getTodayTime();
        long startTime = CustomTimeFormatUtil.getADayStartTime(currentTime);
        long endTime = CustomTimeFormatUtil.getADayEndTime(currentTime);
        HealthPreviewLiveData<PreviewStepVo> liveData = new HealthPreviewLiveData<>(new PreviewStepVo(), HealthPreviewLiveData.TYPE_HEALTH_PREVIEW_LAST_DAY);
        MediatorLiveData<StepEntity> liveData1;
        if (!mWatchManager.isConnected()) {
            liveData1 = (MediatorLiveData<StepEntity>) Transformations.switchMap(liveData, previewStepVo -> {
                JL_Log.d(TAG, "getStepEntityMutableLiveData", "switchMap");
                MutableLiveData<StepEntity> stepEntityLiveData = new MutableLiveData<>(new StepEntity());
                int totalStep = previewStepVo.getTotalStep();
                double totalDistance = previewStepVo.getTotalDistance();
                int totalKcal = previewStepVo.getTotalKcal();
                StepEntity lastDayStepData = new StepEntity();
                if (stepMUnitConverter != null) {
                    stepMUnitConverter.release();
                }
                lastDayStepData.setSteps(totalStep);
                lastDayStepData.setHeatQuantity(totalKcal);
                stepMUnitConverter = new MUnitConverter(mActivity, totalDistance, (value, unit) -> {
                    lastDayStepData.setUnitType(BaseUnitConverter.getType());
                    float distance = (float) (Math.round(value)) / 1000;
                    lastDayStepData.setDistance(distance);
                    stepEntityLiveData.postValue(lastDayStepData);
                });
                return stepEntityLiveData;
            });
        } else {
            liveData1 = new MediatorLiveData<>();
        }
        liveData1.addSource(realTimeLiveData, new Observer<PreviewStepVo>() {
            LiveData<StepEntity> mSource;

            @Override
            public void onChanged(@Nullable PreviewStepVo previewStepVo) {
                MutableLiveData<StepEntity> stepEntityLiveData = new MutableLiveData<>(new StepEntity());
                int totalStep = previewStepVo.getTotalStep();
                double totalDistance = previewStepVo.getTotalDistance();
                int totalKcal = previewStepVo.getTotalKcal();
                StepEntity lastDayStepData = new StepEntity();
                if (stepMUnitConverter != null) {
                    stepMUnitConverter.release();
                }
                lastDayStepData.setSteps(totalStep);
                lastDayStepData.setHeatQuantity(totalKcal);
                stepMUnitConverter = new MUnitConverter(mActivity, totalDistance, (value, unit) -> {
                    lastDayStepData.setUnitType(BaseUnitConverter.getType());
                    float distance = (float) (Math.round(value)) / 1000;
                    lastDayStepData.setDistance(distance);
                    stepEntityLiveData.postValue(lastDayStepData);
                });
                if (mSource == stepEntityLiveData) {
                    return;
                }
                if (mSource != null) {
                    liveData1.removeSource(mSource);
                }
                mSource = stepEntityLiveData;
                liveData1.addSource(mSource, liveData1::setValue);
            }
        });
        liveData.refresh(uid, startTime, endTime);
        return liveData1;
    }

    public LiveData<HeartRateEntity> getHeartRateEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthPreviewLiveData<PreviewHeartRateVo> liveData = new HealthPreviewLiveData<>(new PreviewHeartRateVo(), HealthPreviewLiveData.TYPE_HEALTH_PREVIEW_LAST_DATA);
        final LiveData<HeartRateEntity> liveData1 = Transformations.map(liveData, previewHeartRateVo -> {
            HeartRateEntity lastDayHeartRate;
            if (null == previewHeartRateVo || previewHeartRateVo.getEntities() == null || previewHeartRateVo.getEntities().isEmpty()) {
                lastDayHeartRate = new HeartRateEntity();
            } else {
                lastDayHeartRate = HealthDataHandler.convertHeartRate(mActivity.getApplicationContext(), previewHeartRateVo.getEntities(),
                        previewHeartRateVo.getHealthEntities().get(0).getTime());
            }
            return lastDayHeartRate;
        });
        liveData.refresh(uid);//
        return liveData1;
    }

    public LiveData<SleepEntity> getSleepEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthPreviewLiveData<PreviewSleepVo> liveData = new HealthPreviewLiveData<>(new PreviewSleepVo(), HealthPreviewLiveData.TYPE_HEALTH_PREVIEW_LAST_DATA);
        final LiveData<SleepEntity> liveData1 = Transformations.map(liveData, previewSleepVo -> {
            SleepEntity lastDaySleepData;
            if (null == previewSleepVo || previewSleepVo.getEntities() == null || previewSleepVo.getEntities().isEmpty()) {
                lastDaySleepData = new SleepEntity();
            } else {
                lastDaySleepData = HealthDataHandler.convertSleep(previewSleepVo.deepSleepTime, previewSleepVo.lightSleepTime, previewSleepVo.remSleepTime,
                        previewSleepVo.awakeTime, previewSleepVo.napSleepTime, previewSleepVo.getHealthEntities().get(0).getTime());
            }
            return lastDaySleepData;
        });
        liveData.refresh(uid);
        return liveData1;
    }

    private KGUnitConverter weightKGUnitConverter;

    public LiveData<WeightEntity> getWeightEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthPreviewLiveData<PreviewWeightVo> liveData = new HealthPreviewLiveData<>(new PreviewWeightVo(), HealthPreviewLiveData.TYPE_HEALTH_PREVIEW_LAST_DATA);
        final LiveData<WeightEntity> liveData1 = Transformations.switchMap(liveData, previewWeightVo -> {
            MutableLiveData<WeightEntity> weightEntityLiveData = new MutableLiveData<>(new WeightEntity());
            WeightEntity lastDayWeightData = new WeightEntity();
            double weightValue = 0;
            long refreshTime = 0;
            if (null == previewWeightVo || previewWeightVo.getEntities() == null || previewWeightVo.getEntities().isEmpty()) {
            } else {
                WeightBaseVo.WeightBarCharData weightBarCharData = (WeightBaseVo.WeightBarCharData) previewWeightVo.getEntities().get(previewWeightVo.highLightIndex - 1);
                weightValue = weightBarCharData.value;
                refreshTime = previewWeightVo.getHealthEntities().get(0).getTime();
            }
            long finalRefreshTime = refreshTime;
            if (weightKGUnitConverter != null) {
                weightKGUnitConverter.release();
            }
            JL_Log.d(TAG, "WeightEntityMutableLiveData", "weightValue : " + weightValue);
            weightKGUnitConverter = new KGUnitConverter(mActivity, weightValue, (value, unit) -> {
                lastDayWeightData.setUnitType(BaseUnitConverter.getType());
                JL_Log.d(TAG, "WeightEntityMutableLiveData", "apply: previewWeightVo KGUnitConverter" + value);
                lastDayWeightData.setWeight((float) value);
                lastDayWeightData.setLeftTime(finalRefreshTime);
                weightEntityLiveData.postValue(lastDayWeightData);
            });
            return weightEntityLiveData;
        });
        liveData.refresh(uid);
        return liveData1;
    }

    public LiveData<PressureEntity> getPressureEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthPreviewLiveData<PreviewPressureVo> liveData = new HealthPreviewLiveData<>(new PreviewPressureVo(), HealthPreviewLiveData.TYPE_HEALTH_PREVIEW_LAST_DATA);
        final LiveData<PressureEntity> liveData1 = Transformations.map(liveData, previewPressureVo -> {
            PressureEntity lastDayPressureData;
            if (null == previewPressureVo || previewPressureVo.getEntities() == null || previewPressureVo.getEntities().isEmpty()) {
                lastDayPressureData = new PressureEntity();
            } else {
                lastDayPressureData = HealthDataHandler.convertPressure(mActivity.getApplication(), previewPressureVo.getEntities(),
                        previewPressureVo.getHealthEntities().get(0).getTime());
            }
            return lastDayPressureData;
        });
        liveData.refresh(uid);
        return liveData1;
    }

    public LiveData<BloodOxygenEntity> getBloodOxygenEntityMutableLiveData() {
        String uid = HealthApplication.getAppViewModel().getUid();
        HealthPreviewLiveData<PreviewBloodOxygenVo> liveData = new HealthPreviewLiveData<>(new PreviewBloodOxygenVo(), HealthPreviewLiveData.TYPE_HEALTH_PREVIEW_LAST_DATA);
        final LiveData<BloodOxygenEntity> liveData1 = Transformations.map(liveData, previewBloodOxygenVo -> {
            BloodOxygenEntity lastDayBloodOxygenData;
            if (null == previewBloodOxygenVo || previewBloodOxygenVo.getEntities() == null || previewBloodOxygenVo.getEntities().isEmpty()) {
                lastDayBloodOxygenData = new BloodOxygenEntity();
            } else {
                lastDayBloodOxygenData = HealthDataHandler.convertBloodOxygen(mActivity.getApplicationContext(), previewBloodOxygenVo.lastValue,
                        previewBloodOxygenVo.getHealthEntities().get(0).getTime());
            }
            return lastDayBloodOxygenData;
        });
        liveData.refresh(uid);
        return liveData1;
    }

    @Override
    protected void onCleared() {
        mWatchManager.unregisterOnRcspEventListener(listener);
        super.onCleared();
    }

    private final OnRcspEventListener listener = new OnRcspEventListener() {

        @Override
        public void onHealthDataChange(BluetoothDevice device, HealthData data) {
            switch (data.type) {
                case AttrAndFunCode.HEALTH_DATA_TYPE_STEP:
                    SportsSteps sportsSteps = (SportsSteps) data;
                    JL_Log.d(TAG, "onHealthDataChange", "HEALTH_DATA_TYPE_STEP --->" + sportsSteps);
                    PreviewStepVo previewStepVo = new PreviewStepVo();
                    previewStepVo.setTotalStep(sportsSteps.getStepNum());
                    previewStepVo.setTotalDistance(sportsSteps.getDistance() * 1000);
                    previewStepVo.setTotalKcal(sportsSteps.getCalorie());
                    realTimeLiveData.postValue(previewStepVo);
                    break;
                case AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE:
                    break;
                case AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN:
                    break;
                default:
                    return;
            }
        }
    };

    public static class HealthPreviewViewModelFactory implements ViewModelProvider.Factory {
        private final Activity mActivity;

        public HealthPreviewViewModelFactory(Activity activity) {
            mActivity = activity;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new HealthPreviewViewModel(mActivity);
        }
    }
}
