package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.util.CommandBuilder;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备电量示例代码
 * @since 2024/3/20
 */
public class DevicePowerDemo {

    /**
     * 查询设备电量
     * <p>
     * V1.12.0, V1.13.0版本，因为接口实现有误，导致无法正常使用
     * </p>
     * <p>
     * V1.12.1_beta1版本和 V1.13.1_beta1版本已修复
     * </p>
     *
     * @param manager  手表管理器
     * @param callback 结果回调
     */
    public void requestDevicePowerWay1(@NonNull WatchManager manager, OnWatchOpCallback<Boolean> callback) {
        manager.requestDevicePower(callback);
    }

    /**
     * 查询设备电量
     * <p>
     * 全版本通用，命令方式实现
     * </p>
     * <p>
     * 正式版本修复后，不建议继续使用此接口
     * </p>
     *
     * @param manager  手表管理器
     * @param callback 结果回调
     */
    public void requestDevicePowerWay2(@NonNull WatchManager manager, OnWatchOpCallback<Boolean> callback) {
        manager.sendRcspCommand(manager.getConnectedDevice(), CommandBuilder.buildGetPublicSysInfoCmd(0x01 << AttrAndFunCode.SYS_INFO_ATTR_BATTERY),
                new RcspCommandCallback<CommandBase>() {
                    @Override
                    public void onCommandResponse(BluetoothDevice device, CommandBase cmd) {
                        final int status = cmd.getStatus();
                        if (status != StateCode.STATUS_SUCCESS) {
                            if (status == StateCode.STATUS_UNKOWN_CMD) {
                                onErrCode(device, new BaseError(RcspErrorCode.ERR_FUNC_NOT_SUPPORT)
                                        .setOpCode(cmd.getId()).setSn(cmd.getOpCodeSn()));
                                return;
                            }
                            onErrCode(device, RcspErrorCode.buildJsonError(cmd.getId(), cmd.getOpCodeSn(), RcspErrorCode.ERR_RESPONSE_BAD_STATUS,
                                    status, StateCode.printResponseStatus(status)));
                            return;
                        }
                        if (null != callback) callback.onSuccess(true);
                    }

                    @Override
                    public void onErrCode(BluetoothDevice device, BaseError error) {
                        if (callback != null)
                            callback.onFailed(new BaseError(error.getSubCode(), error.getMessage()));
                    }
                });
    }
}
