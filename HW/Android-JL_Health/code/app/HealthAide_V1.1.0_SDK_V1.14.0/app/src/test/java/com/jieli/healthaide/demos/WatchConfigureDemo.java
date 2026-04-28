package com.jieli.healthaide.demos;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.SportHealthConfigure;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备配置信息示例代码
 * @since 2023/12/29
 */
public class WatchConfigureDemo {

    @Test
    public void getWatchConfigure() {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //Step1. 获取缓存配置信息
        WatchConfigure configure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        WatchConfigure.NecessaryFunc necessaryFunc = configure.getNecessaryFunc(); //必要功能配置
        WatchConfigure.SystemSetup systemSetup = configure.getSystemSetup();  //系统配置
        WatchConfigure.FunctionOption functionOption = configure.getFunctionOption(); //功能配置
        SportHealthConfigure sportHealthConfigure = configure.getSportHealthConfigure(); //运动健康配置
    }

    @Test
    public void requestWatchConfigure() {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //Step1. 调用请求设备信息接口，并等待结果返回
        watchManager.requestDeviceConfigure(new OnWatchOpCallback<WatchConfigure>() {
            @Override
            public void onSuccess(WatchConfigure result) {
                //回调成功
                WatchConfigure.NecessaryFunc necessaryFunc = result.getNecessaryFunc(); //必要功能配置
                WatchConfigure.SystemSetup systemSetup = result.getSystemSetup();  //系统配置
                WatchConfigure.FunctionOption functionOption = result.getFunctionOption(); //功能配置
                SportHealthConfigure sportHealthConfigure = result.getSportHealthConfigure(); //运动健康配置
            }

            @Override
            public void onFailed(BaseError error) {
                //回调失败信息
            }
        });
    }
}
