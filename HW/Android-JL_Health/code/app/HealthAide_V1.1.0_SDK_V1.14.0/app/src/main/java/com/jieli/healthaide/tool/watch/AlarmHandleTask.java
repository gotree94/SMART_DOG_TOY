package com.jieli.healthaide.tool.watch;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.R;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RTCOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.AlarmBean;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.util.JL_Log;

public class AlarmHandleTask extends OnWatchCallback {
    private static final String TAG = AlarmHandleTask.class.getSimpleName();
    private final RTCOpImpl mRTCOp;
    private final WatchManager mWatchManager;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    public AlarmHandleTask(WatchManager manager) {
        mWatchManager = manager;
        mRTCOp = new RTCOpImpl(manager);
        mUIHandler.postDelayed(() -> mWatchManager.registerOnRcspEventListener(new EventListener()), 10);
    }

    @Override
    public void onWatchSystemInit(int code) {
        JL_Log.e(TAG, "onWatchSystemInit", "code : " + code);
        if (code == 0) {
            updateSysTime();
        }
    }

    @Override
    public void onConnectStateChange(BluetoothDevice device, int status) {
        if (status != StateCode.CONNECTION_OK) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    private void updateSysTime() {
        DeviceInfo deviceInfo = mWatchManager.getDeviceInfo(mWatchManager.getConnectedDevice());
        if (deviceInfo.isRTCEnable()) {
            mRTCOp.syncTime(mWatchManager.getConnectedDevice(), new OnOperationCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    JL_Log.e(TAG, "updateSysTime", "onSuccess ---> " + result);
//                    mUIHandler.postDelayed(() -> mRTCOp.syncTime(mWatchManager.getConnectedDevice(), null), 5000);
                }

                @Override
                public void onFailed(BaseError error) {
                    JL_Log.e(TAG, "updateSysTime", "onFailed ---> " + error);
//                    onSuccess(true);
                }
            });
        }
    }

    private class EventListener extends OnRcspEventListener {
        @Override
        public void onAlarmNotify(BluetoothDevice device, AlarmBean alarmBean) {
            super.onAlarmNotify(device, alarmBean);
            FragmentActivity activity = (FragmentActivity) ActivityManager.getInstance().getCurrentActivity();
            if (activity == null) return;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag("alarm_notify");
            if (fragment != null) return;
            Jl_Dialog jl_dialog = new Jl_Dialog.Builder()
                    .title(activity.getString(R.string.tips))
                    .content(activity.getString(R.string.alarm_running))
                    .right(activity.getString(R.string.sure))
                    .width(0.8f)
                    .cancel(false)
                    .rightClickListener((v, dialogFragment) -> {
                        mRTCOp.stopAlarmBell(device, null);
                        dialogFragment.dismiss();
                    })
                    .build();
            jl_dialog.show(activity.getSupportFragmentManager(), "alarm_notify");
        }

        @Override
        public void onAlarmStop(BluetoothDevice device, AlarmBean alarmBean) {
            super.onAlarmStop(device, alarmBean);
            FragmentActivity activity = (FragmentActivity) ActivityManager.getInstance().getCurrentActivity();
            if (activity == null) return;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag("alarm_notify");
            if (fragment == null) return;
            Jl_Dialog jl_dialog = (Jl_Dialog) fragment;
            jl_dialog.dismiss();
        }
    }


}
