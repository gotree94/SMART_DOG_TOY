package com.jieli.healthaide.demos;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jieli.healthaide.tool.esim.ESimWrapper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.model.base.BaseError;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc eSim卡功能示例
 * @since 2023/12/1
 */
public class ESimDemo {

    /**
     * 具体实现封装
     */
    private ESimWrapper eSimWrapper;
    /**
     * FIXME:示例SDK，替换成客户的SDK
     */
    private final ESimSDK eSimSDK = ESimSDK.getInstance();
    //记录数据透传回调
    private OnDataPushCallback dataPushCallback;

    /**
     * 初始化eSim的SDK
     */
    @Test
    public void initSDK(Context context) {
        //Step1. 初始化eSim卡数据封装对象
        eSimWrapper = new ESimWrapper(WatchManager.getInstance());
        //Step2. 添加eSim卡数据回调
        final ESimWrapper.ESimWrapperListener listener = new ESimWrapper.ESimWrapperListener() {
            @Override
            public void onReceiveESimData(byte[] eSimData) {
                //FIXME: 这里修改为数据透传SDK
                if (null != dataPushCallback) dataPushCallback.onReceiveData(eSimData);
            }
        };
        eSimWrapper.addESimWrapperListener(listener);
        //FIXME: 此处应该初始化eSim卡SDK，设置代理模式
        eSimSDK.init(context, 0, new DataProxy() {
            @Override
            public void sendData(byte[] data, OnSendDataCallback callback) {
                sendESimData(data, callback);
            }

            @Override
            public void setDataPushCallback(OnDataPushCallback callback) {
                dataPushCallback = callback;
            }
        });

        //若设备连接成功，开始检测
        eSimSDK.checkEnv();
    }

    /**
     * 发送eSim数据
     *
     * @param data 需要发送的数据
     */
    @Test
    public void sendESimData(byte[] data, OnSendDataCallback callback) {
        if (null == eSimWrapper) return;
        final OnDataEventCallback onDataEventCallback = new OnDataEventCallback() {
            @Override
            public void onBegin(int way) {
                // TODO: 2023/11/28 开始传输
            }

            @Override
            public void onProgress(float progress) {
                // TODO: 2023/11/28 传输进度
            }

            @Override
            public void onStop(int type, byte[] data) {
                // TODO: 2023/11/28 传输结束-结果返回数据
                if (null != callback) callback.onSuccess();
            }

            @Override
            public void onError(BaseError error) {
                // TODO: 2023/11/28 传输失败
                if (null != callback) callback.onFailed(error.getSubCode(), error.getMessage());
            }
        };
        //需要发送的数据，可以为大数据。
//        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0x00};
        //发送eSim卡数据
        eSimWrapper.sendESimData(data, onDataEventCallback);
    }

    /**
     * 释放eSim封装类
     */
    @Test
    public void release() {
        if (null == eSimWrapper) return;
        //若不需要使用，记得释放对象
        eSimWrapper.release();
        eSimWrapper = null;
        eSimSDK.destroy();
    }

    /**
     * ESim数据SDK，示例
     * <p>建议是单例模式。
     * 内部自行实现功能逻辑</p>
     */
    public static class ESimSDK {

        private static volatile ESimSDK sdk;
        /**
         * 是否初始化
         */
        private volatile boolean isInit;
        /**
         * 数据代理
         */
        private DataProxy dataProxy;

        public static ESimSDK getInstance() {
            if (null == sdk) {
                synchronized (ESimSDK.class) {
                    if (null == sdk) {
                        sdk = new ESimSDK();
                    }
                }
            }
            return sdk;
        }

        /**
         * 初始化SDK
         */
        public void init(Context context, int mode, @NonNull DataProxy proxy) {
            if (isInit) return;
            this.dataProxy = proxy;
            this.dataProxy.setDataPushCallback(mDataPushCallback);
            isInit = true;
        }

        /**
         * 检测环境
         */
        public void checkEnv() {
            if (!isInit) return;
        }

        public void destroy() {
            if (!isInit) return;
            dataProxy.setDataPushCallback(null);
            dataProxy = null;
            isInit = false;
        }

        protected DataProxy getDataProxy() {
            return dataProxy;
        }

        private final OnDataPushCallback mDataPushCallback = new OnDataPushCallback() {
            @Override
            public void onReceiveData(byte[] data) {
                //TODO: 处理接收到的数据
            }
        };
    }

    /**
     * 数据代理接口
     */
    public interface DataProxy {

        /**
         * 发送数据
         *
         * @param data     数据
         * @param callback 发送数据回调
         */
        void sendData(byte[] data, OnSendDataCallback callback);

        /**
         * 设置数据透传回调
         *
         * @param callback 数据透传回调
         */
        void setDataPushCallback(OnDataPushCallback callback);
    }

    /**
     * 发送数据结构回调
     */
    public interface OnSendDataCallback {
        /**
         * 回调发送数据成功
         */
        void onSuccess();

        /**
         * 回调发送数据失败
         *
         *
         *
         * @param code    错误码
         * @param message 描述
         */
        void onFailed(int code, String message);
    }

    /**
     * 数据透传回调
     */
    public interface OnDataPushCallback {

        /**
         * 接收到的数据
         *
         * @param data 数据
         */
        void onReceiveData(byte[] data);
    }
}
