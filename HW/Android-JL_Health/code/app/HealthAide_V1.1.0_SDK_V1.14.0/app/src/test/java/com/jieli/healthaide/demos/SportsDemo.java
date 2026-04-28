package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.RealTimeSportsData;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.watch.SportsInfoStatusSyncCmd;
import com.jieli.jl_rcsp.model.device.health.SportsInfo;

import org.junit.Test;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 运动功能测试
 */
public class SportsDemo {

    @Test
    void syncSportsStatus() {
        //同步状态
        //使用场景：1.App连接成功  2.app接收到开始运动命令 3.app主动发送开始运动命令
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //执行读取运动信息功能并等待结果回调
        healthOp.readSportsInfo(healthOp.getConnectedDevice(), new OnOperationCallback<SportsInfo>() {
            @Override
            public void onSuccess(SportsInfo result) {
                //成功回调
//                result.getMode();//运动类型
//                result.getState(); //运动状态
//                result.getId(); //运动id
//                RcspUtil.intToTime(result.getId()); //运动开始时间
//                result.getReadRealTimeDataInterval(); //同步运动实时数据的时间间隔
//                result.getHeartRateMode();//运动的心率模式
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Test
    void startSports() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //注册RCSP事件监听器
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onSportsState(BluetoothDevice device, int state) {
                //此处将会回调运动状态变化
                if (state == StateCode.SPORT_STATE_RUNNING) { //正在运动
                    //同步设备状态
                    healthOp.readSportsInfo(device, null);
                }
            }
        });
        int mode = SportsInfoStatusSyncCmd.SPORTS_TYPE_OUTDOOR & 0xff; //户外运动模式
        //执行开始运动功能并等待结果回调
        healthOp.startSports(healthOp.getConnectedDevice(), mode, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会OnRcspEventListener#onSportsState回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Test
    void pauseSports() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //注册RCSP事件监听器
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onSportsState(BluetoothDevice device, int state) {
                //此处将会回调运动状态变化
                if (state == StateCode.SPORT_STATE_PAUSE) { //运动暂停
                    //更新运动状态
                }
            }
        });
        //执行暂停运动功能并等待结果回调
        healthOp.pauseSports(healthOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会OnRcspEventListener#onSportsState回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Test
    void resumeSports() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //注册RCSP事件监听器
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onSportsState(BluetoothDevice device, int state) {
                //此处将会回调运动状态变化
                if (state == StateCode.SPORT_STATE_RESUME) { //继续运动
                    //同步设备状态
//                    healthOp.readSportsInfo(device, null);
                }
            }
        });
        //执行继续运动功能并等待结果回调
        healthOp.resumeSports(healthOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会OnRcspEventListener#onSportsState回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Test
    void stopSports() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //注册RCSP事件监听器
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onSportsState(BluetoothDevice device, int state) {
                //此处将会回调运动状态变化
                if (state == StateCode.SPORT_STATE_NONE) { //运动结束
                    //处理运动信息
                    //1.获取运动信息，有两种方法
                    //1) 等待onSportInfoChange回调
                    //2) 获取缓存运动信息
//                    SportsInfo sportsInfo = watchManager.getDeviceInfo(device).getSportsInfo();
//                    sportsInfo.getEndTime(); //结束时间
//                    sportsInfo.getRecoveryTime(); //运动恢复时间
//                    sportsInfo.getRecordFileId(); //运动记录文件ID
//                    sportsInfo.getRecordFileSize(); //运动记录文件大小
//                    sportsInfo.getExerciseIntensityState(); //运动强度状态
                    //2.获取运动记录文件
//                    QueryFileTask.File file = new QueryFileTask.File(QueryFileTask.TYPE_SPORTS_RECORD, sportsInfo.getRecordFileId(), sportsInfo.getRecordFileSize());
//                    ReadFileTask.Param param = new ReadFileTask.Param(QueryFileTask.TYPE_SPORTS_RECORD, (short) file.id, file.size, 0);
                    //详细参考文件传输功能说明
                }
            }

            @Override
            public void onSportInfoChange(BluetoothDevice device, SportsInfo sportsInfo) {
                //此处将会回调改变的运动信息

            }
        });
        //执行停止运动功能并等待结果回调
        healthOp.stopSports(healthOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会OnRcspEventListener#onSportsState回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Test
    void realSportsData() {
        //同步运动实时数据
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化健康功能实现
        HealthOpImpl healthOp = new HealthOpImpl(watchManager);
        //执行读取运动实时数据功能并等待结果回调
        healthOp.readRealTimeSportsData(healthOp.getConnectedDevice(), new OnOperationCallback<RealTimeSportsData>() {
            @Override
            public void onSuccess(RealTimeSportsData result) {
                //成功回调
//                result.getVersion();        //版本
//                result.getStep();           //运动步数
//                result.getDistance();       //运动距离， 单位：0.01 km
//                result.getDuration();       //运动时长， 单位：秒
//                result.getSpeed();          //速度，单位：km/h
//                result.getPace();           //配速，单位：s/km
//                result.getCalorie();        //热量，单位：kcal
//                result.getStepFreq();       //步频，单位：step/min
//                result.getStride();         //步幅，单位：cm
//                result.getExerciseStatus(); //运动强度状态：最大心率模式={0非运动、1热身、2燃脂、3有氧耐力、4无氧耐力、5极限}<br/>储备心率模式={0非运动、1有氧基础、2有氧进阶、3乳酸阈值、4无氧基础、5无氧进阶}
//                result.getHeartRate();      //实时心率
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
        //监听设备的运动状态变化
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //注册RCSP事件监听器
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onSportsState(BluetoothDevice device, int state) {
                //此处回调运动状态
                switch (state) {
                    case StateCode.SPORT_STATE_NONE:     //运动结束
                        //参考结束运动功能处理
                        break;
                    case StateCode.SPORT_STATE_RUNNING:  //正在运动
                        //参考开始运动功能处理
                        break;
                    case StateCode.SPORT_STATE_PAUSE:    //运动暂停
                        break;
                    case StateCode.SPORT_STATE_RESUME:   //继续运动
                        break;
                }
            }

            @Override
            public void onSportInfoChange(BluetoothDevice device, SportsInfo sportsInfo) {
                //此处回调运动信息
            }
        });
    }

}
