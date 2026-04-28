package com.jieli.healthaide.tool.iot;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.aliagentsdk.AliAgentSdk;
import com.alibaba.aliagentsdk.api.IAliAgent;
import com.alibaba.aliagentsdk.api.IJustDataTransportAliBt;
import com.alibaba.aliagentsdk.callback.IBtDataUploadCallback;
import com.alibaba.aliagentsdk.callback.IConnectCallback;
import com.alibaba.aliagentsdk.callback.IFgsStateCheckCallback;
import com.alibaba.aliagentsdk.callback.ISend2BtCallback;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.data.OnDataEventCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 阿里IOT工具
 * @since 2022/11/16
 */
public class ALiIOTKit {
    private static final String TAG = ALiIOTKit.class.getSimpleName();

    private final IAliAgent mAliAgent;                //阿里代理实现类
    private final WatchManager mWatchManager;         //手表管理类
    private final BtDataProcessor mBtDataProcessor;   //蓝牙数据处理

    private static final int TEST_MODE_SDK_NORMAL = 0;       //阿里SDK正常模式
    private static final int TEST_MODE_RCSP_TEST = 2;        //RCSP测试模式 -- 我司数据传输模拟测试
    private static final int testMode = TEST_MODE_SDK_NORMAL;  //测试模式

    private IBtDataUploadCallback mBtDataUploadCallback;    //阿里数据上传回调
    private volatile boolean isIOTServerConnect;            //IOT服务器是否连接上

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    public ALiIOTKit(@NonNull WatchManager watchManager) {
        mWatchManager = watchManager;
        //阿里代理SDK类
        AliAgentSdk aliAgentSdk = AliAgentSdk.getInstance();

        mBtDataProcessor = new BtDataProcessor(mWatchManager, (device, type, data) -> handleIOTData(data));
        mWatchManager.registerOnWatchCallback(mWatchCallback);

        if (testMode != TEST_MODE_RCSP_TEST) { //init ALi agent
            aliAgentSdk.init(HealthApplication.getAppViewModel().getApplication(), BuildConfig.DEBUG, BuildConfig.DEBUG);
            mAliAgent = aliAgentSdk.getAgent();     //阿里代理实现类
            //设置自定义数据处理实现
            mAliAgent.customBtImpl(new IJustDataTransportAliBt() {
                @Override
                public void sendData(byte[] bytes, ISend2BtCallback iSend2BtCallback) {
                    JL_Log.d(TAG, "IJustDataTransportAliBt#sendData", CHexConver.bytesToStr(bytes));
                    writeDataToDevice(bytes, iSend2BtCallback);
                }

                @Override
                public void setBtDataUploadCallback(IBtDataUploadCallback iBtDataUploadCallback) {
                    JL_Log.d(TAG, "setBtDataUploadCallback#sendData", "" + iBtDataUploadCallback);
                    mBtDataUploadCallback = iBtDataUploadCallback;
                }
            });
            //注册服务器状态回调
            mAliAgent.setLpConnectedCallback(new IConnectCallback() {
                //Iot连接断开
                @Override
                public void onIotDisconnected() {
                    JL_Log.e(TAG, "onIotDisconnected", "IOT服务器已断开");
                    isIOTServerConnect = false;
                    showTips("IOT服务器已断开");
                }

                //Iot连接成功
                @Override
                public void onIotConnected() {
                    JL_Log.i(TAG, "onIotConnected", "IOT服务器已连接");
                    isIOTServerConnect = true;
                    showTips("IOT服务器已连接");
                }

                /**
                 * Iot连接失败
                 * @param msg  失败信息
                 * @param code 错误码
                 */
                @Override
                public void onIotConnectFailure(String msg, int code) {
                    JL_Log.e(TAG, "onIotConnectFailure", "IOT服务器连接失败, code = " + code + ", " + msg);
                    isIOTServerConnect = false;
                    showTips("连接IOT服务器失败，错误码: " + code + ", " + msg);
                }
            });
        } else {
            mAliAgent = null;
        }
    }

    public void destroy() {
        disconnectIOTServer();
        mWatchManager.unregisterOnWatchCallback(mWatchCallback);
        mUIHandler.removeCallbacksAndMessages(null);
        mBtDataProcessor.destroy();
    }

    /**
     * 处理设备连上的事件
     */
    private void dealWithDeviceConnectedEvent() {
        if (null == mAliAgent) return;
        JL_Log.d(TAG, "dealWithDeviceConnectedEvent", "checkFgsState >>> ");
        showTips("发起飞鸽书检查");
        //检测飞鸽书
        mAliAgent.checkFgsState(new IFgsStateCheckCallback() {
            /**
             * 飞鸽书检测结果
             */
            @Override
            public void onFgsCheckSuccess() {
                JL_Log.w(TAG, "checkFgsState", "onFgsCheckSuccess >>>>");
                showTips("飞鸽书检测成功，准备连接服务器！");
                //有三元组数据（即使没有，走完了注册的流程以后，也是算有的）
                connectIOTServer();
            }

            /**
             * 检查出错
             * @param msg  错误消息
             * @param code 错误码
             */
            @Override
            public void onFgsCheckError(String msg, int code) {
                JL_Log.e(TAG, "onFgsCheckError", "code = " + code + ", " + msg);
                showTips("飞鸽书检测失败， 错误码 ： " + code + ", " + msg);
            }
        });
    }

    /**
     * 连接阿里IOT服务器
     */
    private void connectIOTServer() {
        if (null == mAliAgent) return;
        JL_Log.d(TAG, "connectIOTServer", "isIOTServerConnect = " + isIOTServerConnect);
        if (!isIOTServerConnect) {
            mAliAgent.connectLp();
        }
    }

    /**
     * 断开阿里IOT服务器
     */
    private void disconnectIOTServer() {
        if (null == mAliAgent) return;
        JL_Log.d(TAG, "disconnectIOTServer", "isIOTServerConnect = " + isIOTServerConnect);
        if (isIOTServerConnect) {
//            isIOTServerConnect = false;
            try {
                mAliAgent.disconnectLp();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isIOTServerConnect = false;
            }
        }
    }

    /**
     * 处理设备返回的阿里IOT数据
     *
     * @param data 阿里IOT数据
     */
    private void handleIOTData(@NonNull byte[] data) {
        JL_Log.d(TAG, "handleIOTData", " data : " + CHexConver.bytesToStr(data) + ", testMode = " + testMode);
        if (testMode == TEST_MODE_RCSP_TEST) {
            byte[] changeData = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                byte value = data[i];
                value = CHexConver.intToByte((value + 1) % 256);
                changeData[i] = value;
            }
            JL_Log.d(TAG, "handleIOTData", "after ==> " + CHexConver.byte2HexStr(changeData));
            writeDataToDevice(changeData, null);
            return;
        }
        if (mBtDataUploadCallback != null) {
            JL_Log.d(TAG, "handleIOTData", "onDataUpload :: " + data.length);
            mBtDataUploadCallback.onDataUpload(data);
        }
    }

    /**
     * 发送阿里IOT数据给设备
     *
     * @param data     阿里IOT数据
     * @param callback 结果回调
     */
    private void writeDataToDevice(byte[] data, ISend2BtCallback callback) {
        mBtDataProcessor.writeAliIotData(data, new OnDataEventCallback() {
            @Override
            public void onBegin(int way) {
                JL_Log.d(TAG, "writeDataToDevice", "onBegin ---> way = " + way);
            }

            @Override
            public void onProgress(float progress) {
                JL_Log.d(TAG, "writeDataToDevice", "onProgress ---> progress = " + progress);
            }

            @Override
            public void onStop(int type, byte[] data) {
                JL_Log.w(TAG, "writeDataToDevice", "onStop ---> type = " + type + ", data = " + (null == data ? 0 : data.length));
                if (null != callback) callback.onSendSuccess();
            }

            @Override
            public void onError(BaseError error) {
                JL_Log.e(TAG, "writeDataToDevice", "onError ---> " + error);
                if (null != callback) callback.onSendFailed(error.getMessage(), error.getSubCode());
            }
        });
    }

    private void showTips(String msg) {
        if (TextUtils.isEmpty(msg) || !BuildConfig.DEBUG) return;
        mUIHandler.post(() -> Toast.makeText(HealthApplication.getAppViewModel().getApplication(), msg, Toast.LENGTH_SHORT).show());
    }

    private final OnWatchCallback mWatchCallback = new OnWatchCallback() {
        @Override
        public void onWatchSystemInit(int code) {
            if (code == 0 && !mWatchManager.isBleChangeSpp()) {
                if (testMode != TEST_MODE_RCSP_TEST) {
                    //设备连接成功，等待设备初始化完毕，进行阿里SDK的认证流程
                    mUIHandler.postDelayed(() -> dealWithDeviceConnectedEvent(), 500);
                }
            }
        }

        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            if (device != null && status != StateCode.CONNECTION_OK) {
                if (status == StateCode.CONNECTION_DISCONNECT && testMode != TEST_MODE_RCSP_TEST) {
                    //设备断开，需要断开与服务器的连接
                    disconnectIOTServer();
                }
            }
        }
    };
}
