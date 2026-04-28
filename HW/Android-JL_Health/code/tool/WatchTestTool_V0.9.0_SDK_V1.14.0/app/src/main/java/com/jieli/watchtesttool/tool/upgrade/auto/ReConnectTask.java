package com.jieli.watchtesttool.tool.upgrade.auto;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.jieli.bluetooth_connect.bean.history.HistoryRecord;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.bluetooth_connect.interfaces.callback.OnHistoryRecordCallback;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_rcsp.constant.WatchError;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothEventListener;
import com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;
import com.jieli.watchtesttool.util.AppUtil;

import java.util.List;
import java.util.Locale;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 回连设备任务
 * @since 2022/8/8
 */
public class ReConnectTask extends AbstractTestTask {
    private final WatchManager mWatchManager;
    private final BluetoothHelper mBluetoothHelper;

    private volatile boolean isTest;
    private volatile int retryCount;

    private static final int TIMEOUT = 80 * 1000;

    private static final int TEST_COUNT_LIMIT = 3;
    private static final int MSG_RECONNECT_DEVICE_TIMEOUT = 0x1365;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_RECONNECT_DEVICE_TIMEOUT) {
                final int temp = retryCount;
                retryCount = temp + 1;
                reconnectDevice();
            }
            return true;
        }
    });

    public ReConnectTask(@NonNull WatchManager watchManager) {
        mWatchManager = watchManager;
        mBluetoothHelper = watchManager.getBluetoothHelper();
    }

    @Override
    public void startTest() {
        cbTestStart();
        if (mBluetoothHelper.isConnectedDevice()) {
            BluetoothDevice device = getConnectedDevice();
            DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(device);
            if (deviceInfo != null && (deviceInfo.isMandatoryUpgrade() || mWatchManager.isWatchSystemOk())) {
                deviceConnected(device);
            } else {
                deviceConnecting(device);
            }
        } else if (mBluetoothHelper.getBluetoothOp().isConnecting()) {
            deviceConnecting(mBluetoothHelper.getBluetoothOp().getConnectingDevice());
        } else {
            //未连接
            onTestLog("正在回连设备!!!");
            reconnectDevice();
        }
    }

    @Override
    public void stopTest() {
        if (!isTest) return;
        cbTestFinish(TestError.ERR_USER_STOP);
    }

    @Override
    public String getName() {
        return "回连设备测试";
    }

    public boolean isTest() {
        return isTest;
    }

    private BluetoothDevice getConnectedDevice() {
        return mBluetoothHelper.getConnectedBtDevice();
    }

    private void deviceConnecting(BluetoothDevice device) {
        //正在连接, 需要等待连接结果
        String text = String.format(Locale.getDefault(), "设备正在连接: [%s]",
                AppUtil.printBtDeviceInfo(device));
        onTestLog(text);
        mUIHandler.removeMessages(MSG_RECONNECT_DEVICE_TIMEOUT);
        mUIHandler.sendEmptyMessageDelayed(MSG_RECONNECT_DEVICE_TIMEOUT, TIMEOUT);
    }

    private void deviceConnected(BluetoothDevice device) {
        //已连接
        String text = String.format(Locale.getDefault(), "设备已连接: [%s]",
                AppUtil.printBtDeviceInfo(device));
        onTestLog(text);
        cbTestFinish(TestError.ERR_SUCCESS);
    }

    private void reconnectDevice() {
        List<HistoryRecord> list = mBluetoothHelper.getBluetoothOp().getHistoryRecordList();
        if (null == list || list.isEmpty()) {
            cbTestFinish(TestError.ERR_NOT_FOUND_DEVICE);
            return;
        }
        HistoryRecord history = list.get(0);
        mBluetoothHelper.getBluetoothOp().connectHistoryRecord(history, new OnHistoryRecordCallback() {
            @Override
            public void onSuccess(HistoryRecord record) {
                if (!isTest) return;
                String text = String.format(Locale.getDefault(), "回连设备成功: [%s]", record.getAddress());
                onTestLog(text);
                cbTestFinish(TestError.ERR_SUCCESS);
            }

            @Override
            public void onFailed(int code, String message) {
                if (!isTest) return;
                String text = String.format(Locale.getDefault(), "回连设备失败, 错误码: %d, \n%s", code, message);
                onTestLog(text);
                JL_Log.e(tag, "connectHistoryRecord :: onFailed >> " + retryCount + ", " + TEST_COUNT_LIMIT);
                if (retryCount < TEST_COUNT_LIMIT) {
                    final int temp = retryCount;
                    retryCount = temp + 1;
                    reconnectDevice();
                } else {
                    cbTestFinish(TestError.ERR_RECONNECT_OVER_TIME);
                }
            }
        });
    }

    private void cbTestStart() {
        isTest = true;
        retryCount = 0;
        mBluetoothHelper.addBluetoothEventListener(mEventListener);
        mWatchManager.registerOnWatchCallback(mOnWatchCallback);
    }

    private void cbTestFinish(int code) {
        isTest = false;
        retryCount = 0;
        mUIHandler.removeCallbacksAndMessages(null);
        mBluetoothHelper.removeBluetoothEventListener(mEventListener);
        mWatchManager.unregisterOnWatchCallback(mOnWatchCallback);
        next(new TestError(code));
    }

    private final BluetoothEventListener mEventListener = new BluetoothEventListener() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            if (!isTest) return;
            if (status == BluetoothConstant.CONNECT_STATE_DISCONNECT) {
                String text = String.format(Locale.getDefault(), "设备[%s]未连接~", AppUtil.printBtDeviceInfo(device));
                onTestLog(text);
                mUIHandler.removeMessages(MSG_RECONNECT_DEVICE_TIMEOUT);
                final int temp = retryCount;
                retryCount = temp + 1;
                reconnectDevice();
            } else if (status == BluetoothConstant.CONNECT_STATE_CONNECTING) {
                deviceConnecting(device);
            }
        }
    };

    private final OnWatchCallback mOnWatchCallback = new OnWatchCallback() {
        @Override
        public void onWatchSystemInit(int i) {
            if (i == WatchError.ERR_NONE) {
                deviceConnected(getConnectedDevice());
//                return;
            }
            // mBluetoothHelper.disconnectDevice(getConnectedDevice());
        }

        @Override
        public void onMandatoryUpgrade(BluetoothDevice device) {
            deviceConnected(device);
        }
    };
}
