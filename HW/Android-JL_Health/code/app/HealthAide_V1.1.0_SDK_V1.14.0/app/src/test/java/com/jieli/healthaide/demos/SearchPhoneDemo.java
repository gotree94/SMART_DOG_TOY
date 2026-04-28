package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.healthaide.tool.ring.RingHandler;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspConstant;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.SearchDevCmd;
import com.jieli.jl_rcsp.model.parameter.SearchDevParam;
import com.jieli.jl_rcsp.model.response.SearchDevResponse;
import com.jieli.jl_rcsp.util.CommandBuilder;
import com.jieli.jl_rcsp.util.RcspUtil;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 查找手机Demo
 * @since 2021/12/1
 */
public class SearchPhoneDemo {
    @Test
    void searchPhoneDemo() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //铃声管理器
        final RingHandler mRingHandler = RingHandler.getInstance();
        //注册手表事件回调
        watchManager.registerOnWatchCallback(new OnWatchCallback() {
            //监听设备主动推送的命令
            @Override
            public void onRcspCommand(BluetoothDevice device, CommandBase command) {
                if (command.getId() == Command.CMD_SEARCH_DEVICE) { //处理查找设备命令
                    SearchDevCmd searchDevCmd = (SearchDevCmd) command;
                    SearchDevParam param = searchDevCmd.getParam();
                    if (param == null) return;
                    if (param.getOp() == RcspConstant.RING_OP_OPEN) { //铃声打开
                        mRingHandler.playAlarmRing(param.getType(), param.getTimeoutSec() * 1000L);
                    } else { //铃声关闭
                        mRingHandler.stopAlarmRing();
                    }
                    //回复设备操作成功
                    searchDevCmd.setStatus(StateCode.STATUS_SUCCESS);
                    searchDevCmd.setParam(new SearchDevParam.SearchDevResultParam(0));
                    watchManager.sendCommandResponse(device, searchDevCmd, null);
                }
            }
        });
    }

    @Test
    void searchDeviceDemo() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //构建查找设备命令
        //op      --- 查找设备
        //timeout --- 超时时间
        //ringWay --- 全部响铃
        //player  --- 设备播放
        CommandBase searchDevCmd = CommandBuilder.buildSearchDevCmd(RcspConstant.SEARCH_TYPE_DEVICE, 60,
                RcspConstant.RING_WAY_ALL, RcspConstant.RING_PLAYER_DEVICE);
        watchManager.sendRcspCommand(watchManager.getConnectedDevice(), searchDevCmd, new RcspCommandCallback<SearchDevCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, SearchDevCmd cmd) {
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
                SearchDevResponse response = cmd.getResponse();
                if (null == response) {
                    onErrCode(device, new BaseError(RcspErrorCode.ERR_PARSE_DATA, RcspErrorCode.getErrorDesc(RcspErrorCode.ERR_PARSE_DATA)));
                    return;
                }
                if (response.getResult() == 0) {
                    //请求成功, 说明设备在响铃
                } else {
                    onErrCode(device, RcspErrorCode.buildJsonError(cmd.getId(), cmd.getOpCodeSn(), RcspErrorCode.ERR_RESPONSE_BAD_RESULT,
                            response.getResult(), RcspUtil.formatInt(response.getResult())));
                }
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                //处理错误事件
            }
        });
    }

    @Test
    void stopSearch(){
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        final WatchManager watchManager = WatchManager.getInstance();
        //构建停止查找设备命令
        watchManager.sendRcspCommand(CommandBuilder.buildSearchDevCmd(RcspConstant.RING_OP_CLOSE, 0), new RcspCommandCallback<SearchDevCmd>() {
            @Override
            public void onCommandResponse(BluetoothDevice device, SearchDevCmd cmd) {
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
                SearchDevResponse response = cmd.getResponse();
                if (null == response) {
                    onErrCode(device, new BaseError(RcspErrorCode.ERR_PARSE_DATA, RcspErrorCode.getErrorDesc(RcspErrorCode.ERR_PARSE_DATA)));
                    return;
                }
                if (response.getResult() == 0) {
                    //请求成功, 说明设备已经停止响铃
                } else {
                    onErrCode(device, RcspErrorCode.buildJsonError(cmd.getId(), cmd.getOpCodeSn(), RcspErrorCode.ERR_RESPONSE_BAD_RESULT,
                            response.getResult(), RcspUtil.formatInt(response.getResult())));
                }
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                //处理错误事件
            }
        });
    }
}
