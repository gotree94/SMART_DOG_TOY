package com.jieli.watchtesttool;

import android.bluetooth.BluetoothDevice;

import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.custom.CustomCmd;
import com.jieli.jl_rcsp.model.parameter.CustomParam;
import com.jieli.jl_rcsp.model.response.CustomResponse;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.watchtesttool.tool.watch.WatchManager;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2022/3/15
 */
public class CustomCommandDemo {

    /**
     * 发送自定义命令
     *
     * @param data 自定义数据
     */
    @Test
    public void sendCustomCommand(byte[] data) {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager manager = WatchManager.getInstance();
        //Send custom command and waiting for the result callback
        manager.sendRcspCommand(manager.getTargetDevice(), CommandBuilder.buildCustomCmd(data), new RcspCommandCallback<CustomCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, CustomCmd cmd) {
                if (cmd.getStatus() != StateCode.STATUS_SUCCESS) {
                    onErrCode(device, new BaseError(RcspErrorCode.ERR_RESPONSE_BAD_RESULT, "Device Reply an bad state: " + cmd.getStatus()));
                    return;
                }
                CustomResponse response = cmd.getResponse();
                if (null == response) {
                    onErrCode(device, new BaseError(RcspErrorCode.ERR_PARSE_DATA, "Response data is error."));
                    return;
                }
                byte[] data = response.getData();
                //parse data

            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                //callback error event
            }
        });
    }

    /**
     * 接收自定义命令
     */
    @Test
    public void receiveCustomCmd() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager manager = WatchManager.getInstance();
        //add Rcsp event callback
        manager.registerOnRcspCallback(new OnRcspCallback() {
            @Override
            public void onRcspCommand(BluetoothDevice device, CommandBase command) {
                //receive rcsp command
                if(command.getId() != Command.CMD_CUSTOM) return; //filter other command
                CustomCmd customCmd = (CustomCmd) command;
                CustomParam param = customCmd.getParam();
                if(param == null){
                    customCmd.setStatus(StateCode.STATUS_FAIL); //bad data, reply an bad status.
                    manager.sendCommandResponse(device, customCmd, null);
                    return;
                }
                byte[] data = param.getData();
                //parse data
                //doing some thing and reply a success result.
                customCmd.setStatus(StateCode.STATUS_SUCCESS);
                manager.sendCommandResponse(device, customCmd, null);
            }
        });
    }
}
