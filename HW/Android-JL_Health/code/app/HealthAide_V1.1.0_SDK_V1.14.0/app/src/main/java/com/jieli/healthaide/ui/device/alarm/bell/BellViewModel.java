package com.jieli.healthaide.ui.device.alarm.bell;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_filebrowse.interfaces.SimpleFileObserver;
import com.jieli.jl_filebrowse.util.DeviceChoseUtil;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RTCOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.AuditionParam;
import com.jieli.jl_rcsp.model.device.DefaultAlarmBell;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 9:37 AM
 * @desc :
 */
public class BellViewModel extends ViewModel {
    private final static String TAG = BellViewModel.class.getSimpleName();
    private final RTCOpImpl mRTCOp;
    MutableLiveData<List<SDCardBean>> SDCardsMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Boolean> finishLiveData = new MutableLiveData<>(false);
    public MutableLiveData<List<BellInfo>> bellsMutableLiveData = new MutableLiveData<>();


    public BellViewModel() {
        FileBrowseManager.getInstance().addFileObserver(fileObserver);
        mRTCOp = new RTCOpImpl(WatchManager.getInstance());
        mRTCOp.getRcspOp().registerOnRcspCallback(onRcspCallback);
        mRTCOp.getRcspOp().registerOnRcspEventListener(mRcspEventListener);
        List<SDCardBean> list = new ArrayList<>();
        SDCardBean bean = DeviceChoseUtil.getTargetDev();//获取设备
        list.add(bean);
        SDCardsMutableLiveData.postValue(list);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopBellAudition();
        FileBrowseManager.getInstance().removeFileObserver(fileObserver);
        mRTCOp.getRcspOp().unregisterOnRcspCallback(onRcspCallback);
        mRTCOp.getRcspOp().unregisterOnRcspEventListener(mRcspEventListener);
        mRTCOp.destroy();
    }


    public void stopBellAudition() {
        mRTCOp.stopPlayAlarmBell(mRTCOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.e(TAG, "stopBellAudition", "onFailed ---> " + error);
            }
        });
    }

    public void startBellAudition(byte type, byte dev, int cluster) {
        AuditionParam param = new AuditionParam();
        param.setType(type);
        param.setDev(dev);
        param.setCluster(cluster);
        mRTCOp.auditionAlarmBell(mRTCOp.getConnectedDevice(), param, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.e(TAG, "startBellAudition", "onFailed ---> " + error);
            }
        });
    }


    public void readAlarmBell() {
        mRTCOp.readAlarmDefaultBellList(mRTCOp.getConnectedDevice(), new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.e(TAG, "readAlarmBell", "onFailed ---> " + error);
                ToastUtil.showToastShort(R.string.alarm_bell_list_read_failed);
            }
        });
    }

    private final OnRcspCallback onRcspCallback = new OnRcspCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            if (status != StateCode.CONNECTION_OK) {
                finishLiveData.postValue(true);
            }
        }
    };

    private final OnRcspEventListener mRcspEventListener = new OnRcspEventListener() {
        @Override
        public void onAlarmDefaultBellListChange(BluetoothDevice device, List<DefaultAlarmBell> bells) {
            List<BellInfo> list = new ArrayList<>();
            for (DefaultAlarmBell b : bells) {
                BellInfo info = new BellInfo(b.getIndex(), b.getName(), false);
                list.add(info);
            }
            bellsMutableLiveData.postValue(list);
        }
    };

    private final SimpleFileObserver fileObserver = new SimpleFileObserver() {
        @Override
        public void onSdCardStatusChange(List<SDCardBean> onLineCards) {
            super.onSdCardStatusChange(onLineCards);
            SDCardsMutableLiveData.postValue(DeviceChoseUtil.getSdOfUsbDev());
        }
    };

}
