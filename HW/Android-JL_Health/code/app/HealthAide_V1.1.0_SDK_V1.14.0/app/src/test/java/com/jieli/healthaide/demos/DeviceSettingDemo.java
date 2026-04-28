package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.health.AutomaticPressureDetection;
import com.jieli.jl_rcsp.model.device.health.BloodOxygenMeasurementAlert;
import com.jieli.jl_rcsp.model.device.health.DisconnectReminder;
import com.jieli.jl_rcsp.model.device.health.EmergencyContact;
import com.jieli.jl_rcsp.model.device.health.ExerciseHeartRateReminder;
import com.jieli.jl_rcsp.model.device.health.FallDetection;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.model.device.health.HeartRateMeasure;
import com.jieli.jl_rcsp.model.device.health.LiftWristDetection;
import com.jieli.jl_rcsp.model.device.health.SedentaryReminder;
import com.jieli.jl_rcsp.model.device.health.SensorInfo;
import com.jieli.jl_rcsp.model.device.health.SleepDetection;
import com.jieli.jl_rcsp.model.device.health.UserInfo;

import org.junit.Test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 健康设备设置功能测试
 */
public class DeviceSettingDemo {

    @Test
    public void getHealthSetting() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //注册RCSP事件监听器
        healthOp.getRcspOp().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onHealthSettingChange(BluetoothDevice device, HealthSettingInfo healthSettingInfo) {
                //此处将会回调健康设置信息
                int funcFlag = healthSettingInfo.getFuncFlag(); //当前功能码标志
                switch (funcFlag){
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_SENSOR:
                        SensorInfo sensorInfo = healthSettingInfo.getSensorInfo(); //传感器设置开关
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_SEDENTARY_REMINDER:
                        SedentaryReminder sedentaryReminder = healthSettingInfo.getSedentaryReminder();//久坐提醒
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_HEART_RATE_MEASURE:
                        HeartRateMeasure heartRateMeasure = healthSettingInfo.getHeartRateMeasure();//心率连续测量
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_EXERCISE_HEART_RATE_REMINDER:
                        //运动心率测量
                        ExerciseHeartRateReminder exerciseHeartRateReminder = healthSettingInfo.getExerciseHeartRateReminder();
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_AUTOMATIC_PRESSURE_DETECTION:
                        //压力自动检测
                        AutomaticPressureDetection automaticPressureDetection = healthSettingInfo.getAutomaticPressureDetection();
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_SLEEP_DETECTION:
                        SleepDetection sleepDetection = healthSettingInfo.getSleepDetection();//睡眠检测
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_FALL_DETECTION:
                        FallDetection fallDetection = healthSettingInfo.getFallDetection(); //跌倒检测
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_LIFT_WRIST_DETECTION:
                        LiftWristDetection liftWristDetection = healthSettingInfo.getLiftWristDetection();//抬腕检测
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_USER_INFO:
                        UserInfo userInfo = healthSettingInfo.getUserInfo();//个人信息
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_DISCONNECT_REMINDER:
                        DisconnectReminder disconnectReminder = healthSettingInfo.getDisconnectReminder();//蓝牙断开提醒
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_BLOOD_OXYGEN_MEASUREMENT_ALERT:  //血氧测量提醒
                        BloodOxygenMeasurementAlert bloodOxygenMeasurementAlert = healthSettingInfo.getBloodOxygenMeasurementAlert();
                        break;
                    case AttrAndFunCode.HEALTH_SETTING_TYPE_EMERGENCY_CONTACT: //紧急联系人
                        EmergencyContact emergencyContact = healthSettingInfo.getEmergencyContact();
                        break;
                }
            }
        });
//        mask 设置功能对应的掩码
//        1.获取全部设置
//        int mask = 0xffffffff;

//        2.获取部分设置 类型值为：AttrAndFunCode.HEALTH_SETTING_TYPE_XXX,XXX是对应的类型,如获取传感器和睡眠检测，
        //查询传感器设置和睡眠检测设置
        //3. 不同的功能对应不同的Bit位，对应Bit置1，表示查询该功能
        int mask = 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_SENSOR
                | 0x01 << AttrAndFunCode.HEALTH_SETTING_TYPE_SLEEP_DETECTION;
        //执行读取健康设置信息并等待结果回调
        healthOp.readHealthSettings(healthOp.getConnectedDevice(), mask, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会在OnRcspEventListener#onHealthSettingChange回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });

        //获取缓存的设置状态，使用缓存状态需要先主动获取状态，避免状态不一致，sdk不会自动获取设备的设置状态
        HealthSettingInfo healthSettingInfo = healthOp.getRcspOp().getDeviceInfo().getHealthSettingInfo();

    }


    @Test
    public void setHealthSetting() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //获取缓存的设置状态，使用缓存状态需要先主动获取状态，避免状态不一致，sdk不会自动获取设备的设置状态
        HealthSettingInfo healthSettingInfo = healthOp.getRcspOp().getDeviceInfo().getHealthSettingInfo();
        //举例: 修改蓝牙断开提醒的设置状态
        final DisconnectReminder disconnectReminder = healthSettingInfo.getDisconnectReminder();
        //进行修改操作
        disconnectReminder.setEnable(false);
        //执行配置健康设置信息功能并等待结果回调
        healthOp.configHealthSettings(healthOp.getConnectedDevice(), disconnectReminder, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //修改成功后，可以重新读取健康设置信息
//                int mask = 1 << disconnectReminder.toAttr().getType();
//                healthOp.readHealthSettings(healthOp.getConnectedDevice(), mask, null);
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });

    }
}
