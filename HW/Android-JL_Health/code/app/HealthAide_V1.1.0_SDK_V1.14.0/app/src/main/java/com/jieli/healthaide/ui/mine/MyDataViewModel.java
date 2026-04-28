package com.jieli.healthaide.ui.mine;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.vo.heart_rate.HeartRateMonthVo;
import com.jieli.healthaide.data.vo.parse.ParseEntity;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.mine.entries.MyData;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.HealthDataQuery;
import com.jieli.jl_rcsp.model.device.health.HealthData;
import com.jieli.jl_rcsp.model.device.health.HeartRate;
import com.jieli.jl_rcsp.model.device.health.OxygenSaturation;
import com.jieli.jl_rcsp.model.device.health.SportsSteps;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/8
 * @desc :
 */
public class MyDataViewModel extends ViewModel {
    private final WatchManager mWatchManager;
    private final HealthOpImpl mHealthOp;
    private final MutableLiveData<MyData> myDataLiveData = new MutableLiveData<>(new MyData());


    public MyDataViewModel() {
        mWatchManager = WatchManager.getInstance();
        mHealthOp = mWatchManager.getHealthOp();
        mWatchManager.registerOnRcspEventListener(listener);
    }

    public void readMyData() {
        if (mWatchManager.isConnected()) {
            readFromDevice();
        } else {
            readFromDb();
        }
    }

    public LiveData<MyData> getMyDataLiveData() {
        return myDataLiveData;
    }

    private void readFromDevice() {
        int mask = 0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE | (0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_STEP) | (0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN);
        byte[] subMask = new byte[]{0x01, 0x07, 0x01};
        byte version = 0;
        mHealthOp.readHealthData(mWatchManager.getConnectedDevice(), new HealthDataQuery(version, mask, subMask), null);
    }


    private void readFromDb() {
        MyData myData = new MyData();

        String uid = HealthApplication.getAppViewModel().getUid();
        long currentTime = CalendarUtil.removeTime(System.currentTimeMillis());

        HealthDao healthDao = HealthDataDbHelper.getInstance().getHealthDao();

        HealthEntity step = healthDao.getTodayData(uid, HealthEntity.DATA_TYPE_STEP, currentTime);
        //todo 等焕明完成解析器
        HealthEntity heartRate = healthDao.getTodayData(uid, HealthEntity.DATA_TYPE_HEART_RATE, currentTime);
        if (heartRate != null) {
            HeartRateMonthVo vo = new HeartRateMonthVo();
            List<HealthEntity> p = new ArrayList<>();
            p.add(heartRate);
            vo.setHealthEntities(p);
            List<ParseEntity> entities = vo.getEntities();
            entities.get(0);
        }

        myDataLiveData.postValue(myData);

    }

    @Override
    protected void onCleared() {
        mWatchManager.unregisterOnRcspEventListener(listener);
        super.onCleared();
    }

    private final OnRcspEventListener listener = new OnRcspEventListener() {
        @Override
        public void onHealthDataChange(BluetoothDevice device, HealthData data) {
            MyData myData = myDataLiveData.getValue();
            if (myData == null) myData = new MyData();
            switch (data.type) {
                case AttrAndFunCode.HEALTH_DATA_TYPE_STEP:
                    SportsSteps sportsSteps = (SportsSteps) data;
                    myData.setStep(sportsSteps.getStepNum());
                    myData.setDistance(sportsSteps.getDistance() * 10.0f / 1000f);
                    myData.setKcal(sportsSteps.getCalorie());
                    break;
                case AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE: //获取实时心率
                    HeartRate heartRate = (HeartRate) data;
                    myData.setHeartRate(heartRate.getRealTimeValue());
                    break;
                case AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN:
                    OxygenSaturation oxygenSaturation = (OxygenSaturation) data;
                    myData.setBloodOxygen(oxygenSaturation.getPercent());
                    break;
                default:
                    return;
            }
            myDataLiveData.postValue(myData);
        }
    };
}
