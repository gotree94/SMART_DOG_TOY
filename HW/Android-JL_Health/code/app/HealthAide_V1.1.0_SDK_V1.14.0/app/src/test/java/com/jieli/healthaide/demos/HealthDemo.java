package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.HealthDataQuery;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.health.AirPressure;
import com.jieli.jl_rcsp.model.device.health.Altitude;
import com.jieli.jl_rcsp.model.device.health.ExerciseRecoveryTime;
import com.jieli.jl_rcsp.model.device.health.HealthData;
import com.jieli.jl_rcsp.model.device.health.HeartRate;
import com.jieli.jl_rcsp.model.device.health.MaxOxygenUptake;
import com.jieli.jl_rcsp.model.device.health.OxygenSaturation;
import com.jieli.jl_rcsp.model.device.health.PressureDetection;
import com.jieli.jl_rcsp.model.device.health.SportsSteps;
import com.jieli.jl_rcsp.model.device.health.TrainingLoad;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;

import org.junit.Test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 健康数据测试
 */
public class HealthDemo {

    @Test
    void read() {
        //主动获取实时健康数据
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //注册RCSP事件监听器
        healthOp.getRcspOp().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onHealthDataChange(BluetoothDevice device, HealthData data) {
                //此处将回调健康数据
                switch (data.type) {//根据类型不同分类
                    case AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE:         //心率
                        HeartRate heartRate = (HeartRate) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_AIR_PRESSURE:       //气压
                        AirPressure airPressure = (AirPressure) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_ALTITUDE:           //海拔高度
                        Altitude altitude = (Altitude) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_STEP:               //运动步数
                        SportsSteps sportsSteps = (SportsSteps) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_PRESSURE:           //压力检测
                        PressureDetection pressureDetection = (PressureDetection) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN:       //血氧饱和度
                        OxygenSaturation oxygenSaturation = (OxygenSaturation) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_TRAINING_LOAD:      //训练负荷
                        TrainingLoad trainingLoad = (TrainingLoad) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_MAX_OXYGEN_UPTAKE:  //最大摄氧量
                        MaxOxygenUptake maxOxygenUptake = (MaxOxygenUptake) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_SPORT_RECOVERY_TIME://运动恢复时间
                        ExerciseRecoveryTime recoveryTime = (ExerciseRecoveryTime) data;
                        break;
                }
            }
        });
        //通过掩码获取实时数据，相关掩码：AttrAndFunCode.HEALTH_DATA_TYPE_XXX
        //举例：获取实时心率，步数，距离，热量和血氧饱和度
        int mask = 0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE | (0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_STEP) | (0x01 << AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN);
        //从低位到高位排序
        byte[] subMask = new byte[]{0x01, 0x07, 0x01};
        //当前数据版本：0
        byte version = 0;
        //执行读取健康数据功能并且等待结果回调
        healthOp.readHealthData(healthOp.getConnectedDevice(), new HealthDataQuery(version, mask, subMask), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将在OnRcspEventListener#onHealthDataChange回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }


    @Test
    void listener() {
        //监听实时健康数据
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //注册RCSP事件监听器
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onHealthDataChange(BluetoothDevice device, HealthData data) {
                //此处将回调健康数据
                switch (data.type) {//根据类型不同分类
                    case AttrAndFunCode.HEALTH_DATA_TYPE_HEART_RATE:         //心率
                        HeartRate heartRate = (HeartRate) data;
//                        heartRate.getRealTimeValue(); //实时心率
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_AIR_PRESSURE:       //气压
                        AirPressure airPressure = (AirPressure) data;
//                        airPressure.getRealTimeValue(); //实时气压
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_ALTITUDE:           //海拔高度
                        Altitude altitude = (Altitude) data;
//                        altitude.getRealTimeValue();  //实时高度
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_STEP:               //运动步数
                        SportsSteps sportsSteps = (SportsSteps) data;
//                        sportsSteps.getStepNum();    //步数
//                        sportsSteps.getDistance();   //距离
//                        sportsSteps.getCalorie();    //热量
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_PRESSURE:           //压力检测
                        PressureDetection pressureDetection = (PressureDetection) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_BLOOD_OXYGEN:       //血氧饱和度
                        OxygenSaturation oxygenSaturation = (OxygenSaturation) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_TRAINING_LOAD:      //训练负荷
                        TrainingLoad trainingLoad = (TrainingLoad) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_MAX_OXYGEN_UPTAKE:  //最大摄氧量
                        MaxOxygenUptake maxOxygenUptake = (MaxOxygenUptake) data;
                        break;
                    case AttrAndFunCode.HEALTH_DATA_TYPE_SPORT_RECOVERY_TIME://运动恢复时间
                        ExerciseRecoveryTime recoveryTime = (ExerciseRecoveryTime) data;
//                        recoveryTime.getHour();  //时
//                        recoveryTime.getMin();   //分
                        break;
                }
            }
        });

    }


    @Test
    void readSleepData() {
        //读取睡眠数据
        HealthFileSyncDemo healthFileSyncDemo = new HealthFileSyncDemo(QueryFileTask.TYPE_SLEEP);
        //开始获取
        healthFileSyncDemo.start();
    }
}