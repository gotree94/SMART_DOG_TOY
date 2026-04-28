package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.data.SendParams;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 大数据传输示例代码
 * @since 2023/12/29
 */
public class BigDataTransferDemo {

    @Test
    public void testReceiveBigData() {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager mWatchManager = WatchManager.getInstance();
        final OnWatchCallback onWatchCallback = new OnWatchCallback() {
            /**
             * 回调接收到的大数据
             * @param device 设备对象
             * @param type   数据类型
             * @param data   大数据
             */
            @Override
            public void onReceiveBigData(BluetoothDevice device, int type, byte[] data) {
                super.onReceiveBigData(device, type, data);
            }

            /**
             * 接收大数据发生异常
             *
             * @param device 设备对象
             * @param error  错误事件
             */
            @Override
            public void onBigDataError(BluetoothDevice device, BaseError error) {
                super.onBigDataError(device, error);
            }
        };
        //Step1. 注册事件监听器
        mWatchManager.registerOnWatchCallback(onWatchCallback);

        //StepN. 不需要监听大数据时,记得移除监听器
        mWatchManager.unregisterOnWatchCallback(onWatchCallback);
    }

    @Test
    public void sendBigData() {
        //Step0. WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager mWatchManager = WatchManager.getInstance();
        //Step1. 构建发送参数
        int type = RcspConstant.TYPE_RAW_DATA; //裸数据
        int version = 0; //数据协议
        int sendLimit = 4 * 1024; //每次发送数据的最大值
        int recvLimit = 4 * 1024; //每次接收数据的最大值
        byte[] data = new byte[1024]; //发送的大数据
        SendParams param = new SendParams(type, version, sendLimit, recvLimit, data);
        //Step2. 调用发送大数据接口，并等待回调。
        //注意: 大数据接口不能多次调用，需要等待回调结束，才能继续调用
        mWatchManager.sendLargeData(param, new OnDataEventCallback() {
            /**
             * 回调操作开始
             * @param way 传输方式
             *            <p>{@link RcspConstant#WAY_SEND_DATA} -- 发送数据
             *            {@link RcspConstant#WAY_READ_DATA} -- 读取数据</p>
             */
            @Override
            public void onBegin(int way) {

            }

            /**
             * 回调进度
             * @param progress 进度
             */
            @Override
            public void onProgress(float progress) {

            }

            /**
             * 回调操作结束
             * @param type 数据类型
             * @param data 数据
             */
            @Override
            public void onStop(int type, byte[] data) {

            }

            /**
             * 回调发生异常
             * @param error 错误信息
             */
            @Override
            public void onError(BaseError error) {

            }
        });
    }
}
