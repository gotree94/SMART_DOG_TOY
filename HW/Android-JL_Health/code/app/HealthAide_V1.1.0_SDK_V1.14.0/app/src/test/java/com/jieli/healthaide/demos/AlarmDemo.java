package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.impl.RTCOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.AlarmExpandCmd;
import com.jieli.jl_rcsp.model.device.AlarmBean;
import com.jieli.jl_rcsp.model.device.AlarmListInfo;
import com.jieli.jl_rcsp.model.device.AuditionParam;
import com.jieli.jl_rcsp.model.device.DefaultAlarmBell;

import org.junit.Test;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/12/1
 * @desc : 时钟功能测试
 */
public class AlarmDemo {

    @Test
    public void syncTIme() {
        //同步手机时间到设备
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        //执行同步时间功能并等待结果回调
        rtcOp.syncTime(rtcOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }

    @Test
    public void readAlarmList() {
        //读取闹钟
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        //注册Rcsp事件监听器
        rtcOp.getRcspOp().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onAlarmListChange(BluetoothDevice device, AlarmListInfo alarmListInfo) {
                //此处将会回调闹钟列表信息
            }
        });
        //执行读取闹钟列表功能并等待结果回调
        rtcOp.readAlarmList(rtcOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会在OnRcspEventListener#onAlarmListChange处回调
            }

            @Override
            public void onFailed(BaseError error) {
                 //失败回调
                 //error -- 错误信息
            }
        });
    }

    @Test
    public void updateAlarm() {
        //修改闹钟
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        //注册Rcsp事件监听器
        rtcOp.getRcspOp().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onAlarmListChange(BluetoothDevice device, AlarmListInfo alarmListInfo) {
                //此处将会回调闹钟列表信息
            }
        });
//        AlarmBean alarmBean = "通过读取闹钟得到";
        AlarmBean alarmBean = new AlarmBean();
        //执行创建或修改闹钟功能并等待结果回调
        rtcOp.addOrModifyAlarm(rtcOp.getConnectedDevice(), alarmBean, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调,成功设置后重新获取闹钟列表，更新UI
                //rtcOp.readAlarmList(rtcOp.getConnectedDevice(), null);
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }

    @Test
    public void deleteAlarm() {
        //删除闹钟
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        //注册Rcsp事件监听器
        rtcOp.getRcspOp().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onAlarmListChange(BluetoothDevice device, AlarmListInfo alarmListInfo) {
                //此处将会回调闹钟列表信息
            }
        });
//        AlarmBean alarmBean = "通过读取闹钟得到";
        AlarmBean alarmBean = new AlarmBean();
        //执行删除闹钟功能并等待结果回调
        rtcOp.deleteAlarm(rtcOp.getConnectedDevice(), alarmBean, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调,删除成功后重新获取闹钟列表，更新UI
                //rtcOp.readAlarmList(rtcOp.getConnectedDevice(), null);
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }


    @Test
    public void readDefaultBells() {
        //读取闹钟默认铃声列表，用于设置闹钟铃声
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        //注册Rcsp事件监听器
        rtcOp.getRcspOp().registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onAlarmDefaultBellListChange(BluetoothDevice device, List<DefaultAlarmBell> bells) {
                //此处将会回调闹钟默认铃声列表信息
            }
        });
        //执行读取默认闹钟铃声列表功能并等待结果回调
        rtcOp.readAlarmDefaultBellList(rtcOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //结果将会在OnRcspEventListener#onAlarmDefaultBellListChange处回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }

    @Test
    public void stopBellAudition() {
        //停止铃声试听
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        //执行停止试听铃声功能并等待结果回调
        rtcOp.stopAlarmBell(rtcOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //设备将会暂停播放铃声
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }

    @Test
    public void startBellAudition() {
        //铃声试听
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
        byte type = 0x00; //0x00:默认闹钟铃声 0x01: 目录浏览的文件
        byte dev = 1; //type == 1时有效，设备：sd，usb，flash等，通过目录浏览接口获取到
        int cluster = 1; //文件簇号，对于默认铃声，就是文件序号
        AuditionParam param = new AuditionParam();
        param.setCluster(cluster);
        param.setDev(dev);
        param.setType(type);
        //执行试听铃声功能并等待结果回调
        rtcOp.auditionAlarmBell(rtcOp.getConnectedDevice(), param, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
                //设备将会播放铃声
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }

    @Test
    public void readBellArg() {
        //获取闹铃模式设置
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        boolean enable = watchManager.getDeviceInfo().getAlarmExpandFlag() == 0x01;
        if (enable) {
            //不支持闹铃模式设置
            return;
        }
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
//      AlarmBean alarmBean = "通过读取闹钟得到";
        AlarmBean alarmBean = new AlarmBean();
        int mask = 0x01 << alarmBean.getIndex();//可以一次获取多个闹钟的闹铃模式设置状态
        //执行读取闹钟铃声参数功能并等待结果回调
        rtcOp.readAlarmBellArgs(rtcOp.getConnectedDevice(), (byte) mask, new OnOperationCallback<List<AlarmExpandCmd.BellArg>>() {
            @Override
            public void onSuccess(List<AlarmExpandCmd.BellArg> result) {
                //成功回调
                //result - 闹钟铃声参数
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
                //error -- 错误信息
            }
        });
    }

    @Test
    public void writeBellArg() {
        //设置闹铃模式
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        boolean enable = watchManager.getDeviceInfo().getAlarmExpandFlag() == 0x01;
        if (enable) {
            //不支持闹铃模式设置
            return;
        }
        //初始化时钟功能实现类
        RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
//        AlarmExpandCmd.BellArg bellArg = "通过获取闹铃模式设置得到";
        AlarmExpandCmd.BellArg bellArg = null;
        //执行设置闹钟铃声参数功能并等待结果回调
        rtcOp.setAlarmBellArg(rtcOp.getConnectedDevice(), bellArg, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //成功回调
            }

            @Override
            public void onFailed(BaseError error) {
                //失败回调
            }
        });
    }

    @Test
    public void listener(){
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        watchManager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onAlarmNotify(BluetoothDevice device, AlarmBean alarmBean) {
                super.onAlarmNotify(device, alarmBean);
                //闹钟闹铃

                //初始化时钟功能实现类
                RTCOpImpl rtcOp = new RTCOpImpl(watchManager);
                //执行停止闹钟闹铃功能并等待结果回调
                rtcOp.stopAlarmBell(device, new OnOperationCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {

                    }

                    @Override
                    public void onFailed(BaseError error) {

                    }
                });
            }

            @Override
            public void onAlarmStop(BluetoothDevice device, AlarmBean alarmBean) {
                super.onAlarmStop(device, alarmBean);
                //闹钟闹铃已停止
            }
        });
    }
}
