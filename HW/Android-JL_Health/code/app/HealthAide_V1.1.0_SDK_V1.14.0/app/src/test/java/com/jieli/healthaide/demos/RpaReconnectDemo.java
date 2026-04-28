package com.jieli.healthaide.demos;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.IrkMessage;

/**
 * RpaReconnectDemo
 * @author zqjasonZhong
 * @since 2025/4/29
 * @email zhongzhuocheng@zh-jieli.com
 * @desc RPA回连处理
 */
public class RpaReconnectDemo {


    public void readIrkMessage(){
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        if(!watchManager.isWatchSystemOk()) return; //手表管理器未初始化
        //执行同步IRK信息接口
        watchManager.syncIrkMessage(new OnWatchOpCallback<IrkMessage>() {
            @Override
            public void onSuccess(IrkMessage result) {
                //回调IRK信息
                result.isPaired(); //经典蓝牙是否已配对
                result.getIrkValue(); //IRK值，需要支持RPA功能而且经典蓝牙已配对才能获取到
            }

            @Override
            public void onFailed(BaseError error) {
                //回调操作失败
            }
        });
    }
}
