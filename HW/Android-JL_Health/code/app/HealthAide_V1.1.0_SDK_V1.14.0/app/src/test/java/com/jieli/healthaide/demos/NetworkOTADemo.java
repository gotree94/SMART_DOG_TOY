package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.impl.NetworkOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.network.OnNetworkListener;
import com.jieli.jl_rcsp.interfaces.network.OnNetworkOTACallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkInfo;
import com.jieli.jl_rcsp.model.device.settings.v0.NetworkOTAState;
import com.jieli.jl_rcsp.model.network.OTAParam;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 网络模块OTA示例代码
 * @since 2024/1/8
 */
public class NetworkOTADemo {

    @Test
    public void isSupportNetwork() {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //Step1. 获取缓存的手表配置信息
        WatchConfigure configure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        if (null == configure) {
            //可以尝试向设备请求一次
            watchManager.requestDeviceConfigure(new OnWatchOpCallback<WatchConfigure>() {
                @Override
                public void onSuccess(WatchConfigure result) {
                    //获取手表配置信息成功
                }

                @Override
                public void onFailed(BaseError error) {
                    //获取手表配置信息失败
                    //error -- 错误信息
                }
            });
            return;
        }
        configure.getFunctionOption().isSupportNetworkModule(); //是否支持网络模块
    }

    @Test
    public void queryNetworkInfo() {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //Step1. 初始化网络模块升级操作对象
        NetworkOpImpl networkOp = NetworkOpImpl.instance(watchManager);
        //Step2. 注册网络事件监听器
        final OnNetworkListener listener = new OnNetworkListener() {
            @Override
            public void onNetworkInfo(BluetoothDevice device, NetworkInfo info) {
                //回调网络模块基础信息
//                info.getVid();         //4G模块厂商ID
//                info.isMandatoryOTA(); //是否需要强制升级, 升级失败后触发
//                info.getVersion();     //4G模块版本号
            }

            @Override
            public void onNetworkOTAState(BluetoothDevice device, NetworkOTAState state) {
                //回调网络模块升级状态
            }
        };
        networkOp.addOnNetworkListener(listener);
        //Step3. 执行查询网络信息接口，并等待结果回调
        networkOp.queryNetworkInfo(watchManager.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //回调操作成功结果
            }

            @Override
            public void onFailed(BaseError error) {
                //回调操作失败
                //error -- 错误描述
            }
        });
        //StepN. 不需要监听时移除监听器
//        networkOp.removeOnNetworkListener(listener);
    }

    @Test
    public void startNetWorkOTA(String filePath) {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //Step1. 初始化网络模块升级操作对象
        NetworkOpImpl networkOp = NetworkOpImpl.instance(watchManager);
        if (networkOp.isNetworkOTA()) {
            //正在OTA
            return;
        }
        //执行开始网络OTA
        //filePath --- 4G模块升级文件路径
        networkOp.startNetworkOTA(watchManager.getConnectedDevice(), new OTAParam(filePath), new OnNetworkOTACallback() {
            @Override
            public void onStart() {
                //回调OTA开始
            }

            @Override
            public void onProgress(int progress) {
                //回调OTA进度
            }

            @Override
            public void onCancel() {
                //回调OTA被取消
            }

            @Override
            public void onStop() {
                //回调OTA成功
            }

            @Override
            public void onError(int code, String message) {
                //回调OTA异常
                //code --- 错误码
                //message --- 错误描述
            }
        });
    }

    @Test
    public void listenerOTAException(){
        //Step1. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //Step2. 注册网络事件监听器
        final OnWatchCallback watchCallback = new OnWatchCallback() {
            @Override
            public void onNetworkModuleException(BluetoothDevice device, NetworkInfo info) {
                //回调升级模块升级异常，需要强制升级
                //info --- 网络模块基本信息
            }
        };
        watchManager.registerOnWatchCallback(watchCallback);

        //StepN. 不需要监听时注销监听器
//        watchManager.unregisterOnWatchCallback(watchCallback);
    }
}
