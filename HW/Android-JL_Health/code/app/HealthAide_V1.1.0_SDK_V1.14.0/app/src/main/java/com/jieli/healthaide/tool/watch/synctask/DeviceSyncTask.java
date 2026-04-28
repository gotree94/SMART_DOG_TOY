package com.jieli.healthaide.tool.watch.synctask;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备同步任务基类
 * @since 2021/11/25
 */
public abstract class DeviceSyncTask extends AbstractSyncTask {
    protected final WatchManager mWatchManager = WatchManager.getInstance();

    public DeviceSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);

        BluetoothEventListener eventListener = new BluetoothEventListener() {
            @Override
            public void onConnection(BluetoothDevice device, int status) {
                JL_Log.w(tag, "onConnection", "status : " + status);
                if (status != BluetoothConstant.CONNECT_STATE_CONNECTED) { //设备不处于连接状态，结束任务
                    if (finishListener != null) finishListener.onFinish();
                    mWatchManager.getBluetoothHelper().removeBluetoothEventListener(this);
                }
            }
        };
        mWatchManager.getBluetoothHelper().addBluetoothEventListener(eventListener);
    }

}
