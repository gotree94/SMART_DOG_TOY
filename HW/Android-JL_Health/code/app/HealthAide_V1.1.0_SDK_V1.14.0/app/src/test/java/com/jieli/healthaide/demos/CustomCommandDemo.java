package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.watch.WatchManager;
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
import com.jieli.jl_rcsp.tool.DeviceStatusManager;
import com.jieli.jl_rcsp.util.CommandBuilder;

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
        //build the custom command
        CommandBase customCmd = CommandBuilder.buildCustomCmd(data); //carries custom data
        final boolean isNoResponse = false; //Set whether the command does not require reply
        if (isNoResponse) {
            customCmd = CommandBuilder.buildCustomCmdWithoutResponse(data);//Setting does not require reply
        }
        //Send custom command and waiting for the result callback
        manager.sendRcspCommand(manager.getTargetDevice(), customCmd, new RcspCommandCallback<CustomCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, CustomCmd cmd) {
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
                boolean hasResponse = cmd.getType() == CommandBase.FLAG_HAVE_PARAMETER_AND_RESPONSE
                        || cmd.getType() == CommandBase.FLAG_NO_PARAMETER_HAVE_RESPONSE;
                if (hasResponse) { //有回复
                    CustomResponse response = cmd.getResponse();
                    if (null == response) {
                        onErrCode(device, new BaseError(RcspErrorCode.ERR_PARSE_DATA, RcspErrorCode.getErrorDesc(RcspErrorCode.ERR_PARSE_DATA)));
                        return;
                    }
                    byte[] data = response.getData();
                    //parse data
                }
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
                if (command.getId() != Command.CMD_EXTRA_CUSTOM) return; //filter other command
                CustomCmd customCmd = (CustomCmd) command;
                //Determine whether to reply the command
                boolean hasResponse = customCmd.getType() == CommandBase.FLAG_HAVE_PARAMETER_AND_RESPONSE
                        || customCmd.getType() == CommandBase.FLAG_NO_PARAMETER_HAVE_RESPONSE;
                CustomParam param = customCmd.getParam();
                if (param == null) {
                    if (hasResponse) {
//                   //you can set up the custom response data if desired
//                    byte[] responseData = new byte[0]; //custom response data
//                    param.setData(responseData);
//                    customCmd.setParam(param);
                        customCmd.setParam(null);
                        customCmd.setStatus(StateCode.STATUS_SUCCESS);
                        manager.sendCommandResponse(device, customCmd, null);
                    }
                    return;
                }
                byte[] data = param.getData(); //the custom data from device
                //parse data
                if (hasResponse) { //需要回复
                    //doing some thing and reply a success result.
                    customCmd.setStatus(StateCode.STATUS_SUCCESS);
//                   //you can set up the custom response data if desired
//                    byte[] responseData = new byte[0]; //custom response data
//                    param.setData(responseData);
//                    customCmd.setParam(param);
                    customCmd.setParam(null);
                    manager.sendCommandResponse(device, customCmd, null);
                }
            }
        });
    }


    @Test
    public void checkRCSPProtocolMTU(BluetoothDevice device) {
        //最大发送MTU
        int protocolMtu = DeviceStatusManager.getInstance().getMaxReceiveMtu(device);
        //自定义命令的大小 = 最大发送MTU - 协议包长度
        int customDataLimit = protocolMtu - 23;
        //建议与固件协商好，不建议发最大数据
    }
}
