package com.jieli.healthaide.ui.device.alarm;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.impl.RTCOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.command.AlarmExpandCmd;
import com.jieli.jl_rcsp.model.device.AlarmBean;
import com.jieli.jl_rcsp.model.device.AlarmListInfo;
import com.jieli.jl_rcsp.model.device.DefaultAlarmBell;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Calendar;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/15/21 9:23 PM
 * @desc : 闹钟功能逻辑处理
 */
public class AlarmViewModel extends WatchViewModel {

    public MutableLiveData<AlarmListInfo> alarmsMutableLiveData = new MutableLiveData<>();
    private final static String tag = AlarmViewModel.class.getSimpleName();
    private final RTCOpImpl mRTCOp;

    public AlarmViewModel() {
        mWatchManager.registerOnRcspEventListener(eventHandler);
        mRTCOp = new RTCOpImpl(mWatchManager);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mWatchManager.unregisterOnRcspEventListener(eventHandler);
        mRTCOp.destroy();
    }

    public void readAlarmList() {
        mRTCOp.readAlarmList(getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "readAlarmList", "onFailed ---> " + error);
                ToastUtil.showToastShort(R.string.alarm_list_read_failed);
            }
        });
    }


    public void updateAlarm(AlarmBean alarmBean, OpCallback<Boolean> callback) {
        JL_Log.i(tag, "updateAlarm", "alarmBean : " + alarmBean);
        mRTCOp.addOrModifyAlarm(getConnectedDevice(), alarmBean, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (callback != null) callback.back(true);
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "updateAlarm", "onFailed ---> " + error);
                if (callback != null) callback.back(false);
            }
        });
    }


    public void deleteAlarm(AlarmBean alarmBean) {
        deleteAlarm(alarmBean, null);
    }

    public void deleteAlarm(AlarmBean alarmBean, OperatCallback opCallback) {
        mRTCOp.deleteAlarm(getConnectedDevice(), alarmBean, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                ToastUtil.showToastShort(R.string.alarm_delete_success);
                readAlarmList();
                if (opCallback != null) opCallback.onSuccess();
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "deleteAlarm", "onFailed ---> " + error);
                ToastUtil.showToastShort(R.string.alarm_delete_failure);
                if (opCallback != null) opCallback.onError(error.getSubCode());
            }
        });
    }


    public void saveBellArgs(AlarmExpandCmd.BellArg bellArg, OpCallback<Boolean> callback) {
        if (!hasBellArgs()) {
            callback.back(true);
            return;
        }
        if (bellArg == null) {
            callback.back(false);
            return;
        }
        mRTCOp.setAlarmBellArg(getConnectedDevice(), bellArg, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (callback != null) callback.back(result);
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "saveBellArgs", "onFailed ---> " + error);
                if (callback != null) callback.back(false);
            }
        });
    }


    //新建一个闹钟
    public AlarmBean createNewAlarm() {
        AlarmBean alarmBean = new AlarmBean();
        AlarmListInfo info = alarmsMutableLiveData.getValue();

        if (info != null) {
            List<AlarmBean> list = info.getAlarmBeans();
            boolean[] has = new boolean[5];//如果有闹钟则设置为true，则空余位为false,闹钟个数最大5个
            for (AlarmBean bean : list) {
                has[bean.getIndex()] = true;
            }
            for (byte i = 0; i < has.length; i++) {
                if (!has[i]) {
                    alarmBean.setIndex(i);
                    break;
                }
            }
        }
        alarmBean.setName(HealthApplication.getAppViewModel().getApplication().getString(R.string.default_alarm_name));
        Calendar calendar = Calendar.getInstance();
        alarmBean.setHour((byte) calendar.get(Calendar.HOUR_OF_DAY));
        alarmBean.setMin((byte) calendar.get(Calendar.MINUTE));
        alarmBean.setVersion(1);
        alarmBean.setOpen(true);
        String bellName = HealthApplication.getAppViewModel().getApplication().getString(R.string.alarm_bell_1);
        alarmBean.setBellName(bellName)
                .setBellType((byte) 0)
                .setBellCluster(0);
        return alarmBean;
    }

    public void readExpandArg(AlarmBean alarmBean, OpCallback<AlarmExpandCmd.BellArg> callback) {
        if (!hasBellArgs()) return;
        mRTCOp.readAlarmBellArgs(getConnectedDevice(), (byte) (0x01 << alarmBean.getIndex()), new OnOperationCallback<List<AlarmExpandCmd.BellArg>>() {
            @Override
            public void onSuccess(List<AlarmExpandCmd.BellArg> result) {
                if (result == null || result.isEmpty()) {
                    onFailed(new BaseError(RcspErrorCode.ERR_INVALID_PARAMETER, "data is null"));
                    return;
                }
                if (callback != null) callback.back(result.get(0));
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "readExpandArg", "onFailed ---> " + error);
                if (callback != null) callback.back(null);
            }
        });
    }


    public boolean hasBellArgs() {
        if (getDeviceInfo(getConnectedDevice()) != null) {
            int flag = getDeviceInfo(getConnectedDevice()).getAlarmExpandFlag();
            return (flag & 0x01) == 0x01;
        }
        return false;
    }

    private final OnRcspEventListener eventHandler = new OnRcspEventListener() {

        @Override
        public void onAlarmListChange(BluetoothDevice device, AlarmListInfo alarmListInfo) {
            alarmsMutableLiveData.postValue(alarmListInfo);
        }

        @Override
        public void onAlarmDefaultBellListChange(BluetoothDevice device, List<DefaultAlarmBell> bells) {
            super.onAlarmDefaultBellListChange(device, bells);
        }
    };


}