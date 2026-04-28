package com.jieli.healthaide.ui.device;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.component.utils.HandlerManager;
import com.jieli.component.utils.ToastUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.tool.watch.OnDeviceConfigureListener;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.bean.DeviceConnectionData;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.device.AttrBean;
import com.jieli.jl_rcsp.model.device.DeviceInfo;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.model.device.health.IHealthSettingToAttr;
import com.jieli.jl_rcsp.model.device.health.SensorInfo;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/11
 * @desc :
 */
public class HealthSettingViewModel extends ViewModel {

    private static final String TAG = HealthSettingViewModel.class.getSimpleName();
    private final WatchManager mWatchManager = WatchManager.getInstance();
    private final HealthOpImpl mHealthOp;
    private final MutableLiveData<HealthSettingInfo> healthSettingInfoMutableLiveData = new MutableLiveData<>();
    public final MutableLiveData<DeviceConnectionData> mDeviceConnectionDataMLD = new MutableLiveData<>();
    public final MutableLiveData<BluetoothDevice> mDeviceConfigureMLD = new MutableLiveData<>();


    public HealthSettingViewModel() {
        mHealthOp = mWatchManager.getHealthOp();
        mWatchManager.addOnDeviceConfigureListener(mOnDeviceConfigureListener);
        mHealthOp.getRcspOp().registerOnRcspCallback(mOnRcspCallback);
        mHealthOp.getRcspOp().registerOnRcspEventListener(onRcspEventListener);
        if (getHealthSettingInfo() != null)
            healthSettingInfoMutableLiveData.setValue(getHealthSettingInfo());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        release();
    }

    public void release() {
        mWatchManager.removeOnDeviceConfigureListener(mOnDeviceConfigureListener);
        mHealthOp.getRcspOp().unregisterOnRcspCallback(mOnRcspCallback);
        mHealthOp.getRcspOp().unregisterOnRcspEventListener(onRcspEventListener);
    }

    public WatchConfigure getWatchConfigure() {
        return mWatchManager.getWatchConfigure(mWatchManager.getConnectedDevice());
    }

    public LiveData<HealthSettingInfo> healthSettingInfoLiveData() {
        return healthSettingInfoMutableLiveData;
    }

    public void requestHealthSettingInfo(int mask) {
        mHealthOp.readHealthSettings(mHealthOp.getConnectedDevice(), mask, null);
    }


    public void sendSettingCmd(final IHealthSettingToAttr healthSettingToAttr, final OperatCallback operatCallback) {
        JL_Log.d(TAG, "sendSettingCmd", "data : " + CHexConver.byte2HexStr(healthSettingToAttr.toAttr().getData()));
        mHealthOp.configHealthSettings(mHealthOp.getConnectedDevice(), healthSettingToAttr, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (operatCallback != null) {
                    operatCallback.onSuccess();
                }
                //固件保存数据可能延时：延时20ms
                HandlerManager.getInstance().getMainHandler().postDelayed(() -> {
                    AttrBean attrBean = healthSettingToAttr.toAttr();
                    if (attrBean == null) return;
                    requestHealthSettingInfo(0x01 << attrBean.getType());
                }, 20);
            }

            @Override
            public void onFailed(BaseError error) {
                ToastUtil.showToastShort(R.string.save_failed);
                if (operatCallback != null) {
                    operatCallback.onError(error.getSubCode());
                }
            }
        });
    }

    public void sendSettingCmd(IHealthSettingToAttr healthSettingToAttr) {
        //todo 发送设置命令
        sendSettingCmd(healthSettingToAttr, null);
    }

    public HealthSettingInfo getHealthSettingInfo() {
        DeviceInfo deviceInfo = mHealthOp.getRcspOp().getDeviceInfo();
        if (deviceInfo == null) return null;
        return deviceInfo.getHealthSettingInfo();
    }

    public SensorInfo getSensorInfo() {
        HealthSettingInfo healthSettingInfo = getHealthSettingInfo();
        if (healthSettingInfo == null) return null;
        if (healthSettingInfo.getSensorInfo() == null) return null;
        return healthSettingInfo.getSensorInfo().copy();
    }

    private final OnDeviceConfigureListener mOnDeviceConfigureListener = mDeviceConfigureMLD::postValue;

    private final OnRcspCallback mOnRcspCallback = new OnRcspCallback() {
        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            mDeviceConnectionDataMLD.setValue(new DeviceConnectionData(device, status));
        }

        @Override
        public void onSwitchConnectedDevice(BluetoothDevice device) {
            requestHealthSettingInfo(0xfff);
        }
    };

    private final OnRcspEventListener onRcspEventListener = new OnRcspEventListener() {
        @Override
        public void onHealthSettingChange(BluetoothDevice device, HealthSettingInfo healthSettingInfo) {
            super.onHealthSettingChange(device, healthSettingInfo);
            healthSettingInfoMutableLiveData.postValue(healthSettingInfo);
        }
    };
}
