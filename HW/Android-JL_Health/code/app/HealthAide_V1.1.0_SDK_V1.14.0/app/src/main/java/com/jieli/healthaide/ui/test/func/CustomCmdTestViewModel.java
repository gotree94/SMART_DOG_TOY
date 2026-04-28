package com.jieli.healthaide.ui.test.func;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.device.bean.DeviceConnectionData;
import com.jieli.healthaide.ui.device.bean.OpResult;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.IHandleResult;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.custom.CustomCmd;
import com.jieli.jl_rcsp.model.parameter.CustomParam;
import com.jieli.jl_rcsp.model.response.CustomResponse;
import com.jieli.jl_rcsp.tool.CustomRcspActionCallback;
import com.jieli.jl_rcsp.util.CommandBuilder;

/**
 * CustomCmdTestViewModel
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2026/1/13
 * note: 自定义命令测试逻辑实现
 */
public class CustomCmdTestViewModel extends ViewModel {

    public static final int OP_SEND_CUSTOM_CMD = 0x00FF;
    public static final int OP_RECEIVER_CUSTOM_CMD = 0x10FF;

    private final WatchManager watchManager = WatchManager.getInstance();

    public final MutableLiveData<DeviceConnectionData> deviceConnectionMLD = new MutableLiveData<>();
    public final MutableLiveData<OpResult<byte[]>> customCmdMLD = new MutableLiveData<>();

    public CustomCmdTestViewModel() {
        watchManager.registerOnRcspCallback(onRcspCallback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        watchManager.unregisterOnRcspCallback(onRcspCallback);
    }

    public boolean isDeviceConnected() {
        return watchManager.isConnected();
    }

    public void sendCustomCmd(byte[] data, boolean isNeedReply) {
        //获取操作设备对象
        final BluetoothDevice usingDevice = watchManager.getConnectedDevice();
        if (!watchManager.isDeviceConnected(usingDevice)) {
            postFailEvent(OP_SEND_CUSTOM_CMD, RcspErrorCode.ERR_REMOTE_NOT_CONNECT);
            return;
        }
        //构造自定义命令
        CommandBase customCmd = isNeedReply ? CommandBuilder.buildCustomCmd(data) : CommandBuilder.buildCustomCmdWithoutResponse(data);
        //执行发送自定义命令操作
        watchManager.sendRcspCommand(usingDevice, customCmd, new CustomRcspActionCallback<>("sendCustomCmd", new OnOperationCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                postSuccessEvent(OP_SEND_CUSTOM_CMD, result);
            }

            @Override
            public void onFailed(BaseError error) {
                if (null == error) return;
                postFailEvent(OP_SEND_CUSTOM_CMD, error.getSubCode(), error.getMessage());
            }
        }, new IHandleResult<byte[], CustomCmd>() {
            @Override
            public int hasResult(BluetoothDevice device, CustomCmd cmd) {
                return 0;
            }

            @Override
            public byte[] handleResult(BluetoothDevice device, CustomCmd cmd) {
                if (null == cmd || cmd.getStatus() != StateCode.STATUS_SUCCESS) return null;
                CustomResponse response = cmd.getResponse();
                if (null == response) return null;
                return response.getData();
            }
        }));
    }

    private void postSuccessEvent(int op, byte[] data) {
        customCmdMLD.postValue(new OpResult<byte[]>()
                .setOp(op)
                .setCode(RcspErrorCode.ERR_NONE)
                .setResult(data));
    }

    private void postFailEvent(int op, int code) {
        postFailEvent(op, code, RcspErrorCode.getErrorDesc(code));
    }

    private void postFailEvent(int op, int code, String message) {
        customCmdMLD.postValue(new OpResult<byte[]>()
                .setOp(op)
                .setCode(code)
                .setMessage(message));
    }

    private final OnRcspCallback onRcspCallback = new OnRcspCallback() {

        @Override
        public void onConnectStateChange(BluetoothDevice device, int status) {
            if (status != StateCode.CONNECTION_OK) {
                deviceConnectionMLD.postValue(new DeviceConnectionData(device, status));
            }
        }

        @Override
        public void onRcspCommand(BluetoothDevice device, CommandBase command) {
            if (!(command instanceof CustomCmd)) return; //过滤非自定义命令
            CustomCmd customCmd = (CustomCmd) command;
            boolean isNeedReply = customCmd.getType() == CommandBase.FLAG_HAVE_PARAMETER_AND_RESPONSE
                    || command.getType() == CommandBase.FLAG_NO_PARAMETER_HAVE_RESPONSE;
            CustomParam param = customCmd.getParam();
            byte[] customData = new byte[0];
            if (null != param) {
                customData = param.getData();
            }
            if (isNeedReply) {
                //回复数据
                CustomParam responseParam = new CustomParam(new byte[0]);
                customCmd.setParam(responseParam);
                customCmd.setStatus(StateCode.STATUS_SUCCESS);
                watchManager.sendCommandResponse(device, customCmd, null);
            }
            postSuccessEvent(OP_RECEIVER_CUSTOM_CMD, customData);
        }
    };
}
