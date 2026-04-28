package com.jieli.healthaide.demos;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.tool.bluetooth.BluetoothEventListener;
import com.jieli.healthaide.tool.bluetooth.BluetoothHelper;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.constant.StateCode;
import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspEventListener;
import com.jieli.jl_rcsp.interfaces.rcsp.RcspCommandCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.base.CommonResponse;
import com.jieli.jl_rcsp.model.command.custom.CustomCmd;
import com.jieli.jl_rcsp.model.command.data.DataCmd;
import com.jieli.jl_rcsp.model.command.data.DataHasResponseCmd;
import com.jieli.jl_rcsp.model.command.status.GetTargetInfoCmd;
import com.jieli.jl_rcsp.model.device.AlarmBean;
import com.jieli.jl_rcsp.model.device.AlarmListInfo;
import com.jieli.jl_rcsp.model.device.BatteryInfo;
import com.jieli.jl_rcsp.model.device.ChannelInfo;
import com.jieli.jl_rcsp.model.device.DefaultAlarmBell;
import com.jieli.jl_rcsp.model.device.DevStorageInfo;
import com.jieli.jl_rcsp.model.device.DynamicLimiterParam;
import com.jieli.jl_rcsp.model.device.EqInfo;
import com.jieli.jl_rcsp.model.device.EqPresetInfo;
import com.jieli.jl_rcsp.model.device.FmStatusInfo;
import com.jieli.jl_rcsp.model.device.ID3MusicInfo;
import com.jieli.jl_rcsp.model.device.LightControlInfo;
import com.jieli.jl_rcsp.model.device.MusicNameInfo;
import com.jieli.jl_rcsp.model.device.MusicStatusInfo;
import com.jieli.jl_rcsp.model.device.PlayModeInfo;
import com.jieli.jl_rcsp.model.device.ReverberationParam;
import com.jieli.jl_rcsp.model.device.VoiceMode;
import com.jieli.jl_rcsp.model.device.VolumeInfo;
import com.jieli.jl_rcsp.model.device.health.HealthData;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.jieli.jl_rcsp.model.device.health.SportsInfo;
import com.jieli.jl_rcsp.model.parameter.CustomParam;
import com.jieli.jl_rcsp.model.parameter.DataParam;
import com.jieli.jl_rcsp.model.response.CustomResponse;
import com.jieli.jl_rcsp.model.response.TargetInfoResponse;
import com.jieli.jl_rcsp.util.CommandBuilder;

import org.junit.Test;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc RCSP测试Demo
 * @since 2022/3/24
 */
public class RcspDemo {

    /**
     * RCSP简单实现
     */
    public static class RcspManager extends RcspOpImpl {
        //蓝牙连接封装类，可以替换成用户的连接库实现
        //注意：请与固件工程师协商，是否需要设备认证流程。如果需要，请连接成功后先进行设备认证流程，认证成功后才算连接成功。
        private final BluetoothHelper mBTHelper = BluetoothHelper.getInstance();

        private static volatile RcspManager instance;  //为了方便使用，推荐单例方式使用

        private RcspManager() {
            mBTHelper.addBluetoothEventListener(mBTEventListener);
        }

        public static RcspManager getInstance() {
            if (null == instance) {
                synchronized (RcspManager.class) {
                    if (null == instance) {
                        instance = new RcspManager();
                    }
                }
            }
            return instance;
        }

        @Override
        public BluetoothDevice getConnectedDevice() {
            return mBTHelper.getConnectedBtDevice();
        }

        @Override
        public boolean isDeviceConnected(BluetoothDevice device) {
            return mBTHelper.isConnectedBtDevice(device);
        }

        @Override
        public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
            return mBTHelper.sendDataToDevice(device, data);
        }

        @Override
        public void release() {
            mBTHelper.removeBluetoothEventListener(mBTEventListener);
            super.release();
            instance = null;
        }

        /**
         * 转换成RCSP的连接状态
         *
         * @param status 系统连接状态
         * @return 手表连接状态
         */
        private int convertRCSPConnectStatus(int status) {
            int newStatus;
            switch (status) {
                case BluetoothConstant.CONNECT_STATE_CONNECTING:
                    newStatus = StateCode.CONNECTION_CONNECTING;
                    break;
                case BluetoothConstant.CONNECT_STATE_CONNECTED:
                    newStatus = StateCode.CONNECTION_OK;
                    break;
                default:
                    newStatus = StateCode.CONNECTION_DISCONNECT;
                    break;
            }
            return newStatus;
        }

        private final BluetoothEventListener mBTEventListener = new BluetoothEventListener() {
            @Override
            public void onConnection(BluetoothDevice device, int status) {
                int newStatus = convertRCSPConnectStatus(status); //转换连接状态。很重要！！！
                //传递连接状态到RCSP库处理
                //RCSP库初始化流程需要等设备连接成功才开始。
                notifyBtDeviceConnection(device, newStatus);
            }

            @Override
            public void onReceiveData(BluetoothDevice device, byte[] data) {
                notifyReceiveDeviceData(device, data); //传递接收到的数据到RCSP库处理
            }
        };
    }

    /**
     * 测试发送RCSP命令
     */
    @Test
    public void sendRCSPCommand() {
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 构建命令数据, 比如：查询设备信息命令*/
        CommandBase getDeviceInfoCmd = CommandBuilder.buildGetDeviceInfoCmd(0xffffffff);
        /*3. 发送命令并等待命令结果回调*/
        /*发送命令的参数对应：目标设备对象， 命令数据， 命令超时时间， 结果回调*/
        manager.sendRcspCommand(manager.getConnectedDevice(), getDeviceInfoCmd,  new RcspCommandCallback<GetTargetInfoCmd>() {
            /**
             * 回调命令的回复数据
             *
             * @param device 已连接设备
             * @param cmd    回复命令
             *               <p>若是无回复的命令，则返回命令原型；
             */
            @Override
            public void onCommandResponse(BluetoothDevice device, GetTargetInfoCmd cmd) {
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
                TargetInfoResponse response = cmd.getResponse();
                if (null == response) {
                    onErrCode(device, new BaseError(RcspErrorCode.ERR_PARSE_DATA, RcspErrorCode.getErrorDesc(RcspErrorCode.ERR_PARSE_DATA)));
                    return;
                }
                // TODO: handle device response
            }

            /**
             * 回调异常情况
             *
             * @param device 已连接设备
             * @param error  错误信息
             */
            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                // TODO: handle error event
            }
        });
    }

    /**
     * 测试发送RCSP数据包
     *
     * @param responseCmdId  响应命令的ID
     * @param data           数据
     * @param isNeedResponse 是否需要回复
     */
    @Test
    public void sendData(int responseCmdId, byte[] data, boolean isNeedResponse) {
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 构建数据命令*/
        CommandBase dataCmd;
        DataParam param = new DataParam(data); //设置需要回复的数据
        param.setXmOpCode(responseCmdId);      //设置响应的命令序号
        //组装成数据命令
        /*数据命令可分为需要回复和不需要回复的。可以根据需要进行组装。*/
        /*因为数据命令一般是传输大量数据的，推荐是不需要回复，加快传输过程。小数据传输，请使用自定义命令*/
        if (isNeedResponse) { //需要回复
            dataCmd = new DataHasResponseCmd(param);  //组装成需要回复的数据命令
        } else { //不需要回复
            dataCmd = new DataCmd(param);    //组装成不需要回复的数据命令
        }
        /*3. 发送命令并等待命令结果回调*/
        manager.sendRcspCommand(manager.getConnectedDevice(), dataCmd, new RcspCommandCallback() {
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
                if (!isNeedResponse) { //不需要回复
                    //此处回调的是数据命令原型。
                    //有此回调，说明命令发送成功
                    return;
                }
                DataHasResponseCmd dataHasResponseCmd = (DataHasResponseCmd) cmd;
                CommonResponse response = dataHasResponseCmd.getResponse();
                byte[] result = response.getRawData();  //自定义回复结果
                if (null != result) { //自行解析回复结果
                    //parse result
                }
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                // handle error event
            }
        });
    }

    /**
     * 发送自定义命令
     *
     * @param data 自定义数据
     */
    @Test
    public void sendCustomCmd(byte[] data) {
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 构建自定义命令*/
        CommandBase customCmd = CommandBuilder.buildCustomCmd(data);
        /*3. 发送命令并等待命令结果回调*/
        manager.sendRcspCommand(manager.getConnectedDevice(), customCmd, new RcspCommandCallback<CustomCmd>() {
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
                CustomResponse response = cmd.getResponse();
                if (null == response) {
                    onErrCode(device, new BaseError(RcspErrorCode.ERR_PARSE_DATA, RcspErrorCode.getErrorDesc(RcspErrorCode.ERR_PARSE_DATA)));
                    return;
                }
                byte[] data = response.getData(); //设备回复的数据
                //parse data
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                // handle error event
            }
        });
    }

    /**
     * 测试接收自定义命令
     */
    @Test
    public void receivedCustomCmd() {
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 注册RCSP监听器*/
        manager.registerOnRcspCallback(new OnRcspCallback() {
            /**
             * 回调接收到的RCSP命令
             *
             * @param device  已连接设备
             * @param command RCSP命令
             */
            @Override
            public void onRcspCommand(BluetoothDevice device, CommandBase command) {
                if (command.getId() != Command.CMD_EXTRA_CUSTOM) return;//过滤不相关的命令
                /*3. 接收到命令，并解析处理*/
                CustomCmd customCmd = (CustomCmd) command;
                CustomParam param = customCmd.getParam(); //获取自定义命令的参数
                if (null == param) return;
                byte[] data = param.getData();
                //解析自定义数据
                //parse data
                //处理完成后，需要回复结果
                //注意，耗时处理，请先回复结果再用子线程处理
                customCmd.setStatus(StateCode.STATUS_SUCCESS);  //回复命令处理成功结果
                /*4. 回复命令处理结果*/
                manager.sendCommandResponse(device, customCmd, new RcspCommandCallback() {
                    @Override
                    public void onCommandResponse(BluetoothDevice device, CommandBase cmd) {
                        //回复命令发送成功
                    }

                    @Override
                    public void onErrCode(BluetoothDevice device, BaseError error) {
                        //处理命令发送异常情况
                    }
                });
            }
        });

        /*5. 不再需要监听回调时。记得移除监听器*/
        // manager.unregisterOnRcspCallback(listener);
    }

    /**
     * 发送回复命令
     *
     * @param command 需要回复的命令
     */
    @Test
    public void sendResponse(CommandBase command){
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 处理命令数据并设置回复结果*/
        CustomCmd customCmd = (CustomCmd) command;
        CustomParam param = customCmd.getParam();
        byte[] data = param.getData(); //自定义数据
        //parse data and handle it
        //设置回复结果状态
        customCmd.setStatus(StateCode.STATUS_SUCCESS); //回复成功，也可以回复其他状态，根据处理结果决定
        /*3. 回复命令处理结果*/
        manager.sendCommandResponse(manager.getConnectedDevice(), customCmd, new RcspCommandCallback() {
            @Override
            public void onCommandResponse(BluetoothDevice device, CommandBase cmd) {
                //回复命令发送成功
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                //处理命令发送异常情况
            }
        });
    }

    /**
     * 监听RCSP事件
     */
    @Test
    public void listenerRCSPEvent() {
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 注册RCSP事件监听器*/
        manager.registerOnRcspEventListener(new OnRcspEventListener() {
            @Override
            public void onDeviceModeChange(BluetoothDevice device, int mode) {
                //回调设备模式
            }

            @Override
            public void onVolumeChange(BluetoothDevice device, VolumeInfo volume) {
                //回调音量变化
            }

            @Override
            public void onEqChange(BluetoothDevice device, EqInfo eqInfo) {
                //回调EQ调节信息
            }

            @Override
            public void onEqPresetChange(BluetoothDevice device, EqPresetInfo eqPresetInfo) {
                //回调EQ预设值信息
            }

            @Override
            public void onDevStorageInfoChange(BluetoothDevice device, DevStorageInfo storageInfo) {
                //回调设备存储器信息
            }

            @Override
            public void onFileFormatChange(BluetoothDevice device, String fileFormat) {
                //回调设备支持文件格式
            }

            @Override
            public void onMusicNameChange(BluetoothDevice device, MusicNameInfo nameInfo) {
                //回调音乐名称
            }

            @Override
            public void onMusicStatusChange(BluetoothDevice device, MusicStatusInfo statusInfo) {
                //回调音乐播放状态
            }

            @Override
            public void onPlayModeChange(BluetoothDevice device, PlayModeInfo playModeInfo) {
                //回调音乐播放模式
            }

            @Override
            public void onBatteryChange(BluetoothDevice device, BatteryInfo batteryInfo) {
                //回调电量变化
            }

            @Override
            public void onAuxStatusChange(BluetoothDevice device, boolean isPlay) {
                //回调外接设备的播放状态
            }

            @Override
            public void onFmChannelsChange(BluetoothDevice device, List<ChannelInfo> channels) {
                //回调FM频道列表
            }

            @Override
            public void onFmStatusChange(BluetoothDevice device, FmStatusInfo fmStatusInfo) {
                //回调FM状态
            }

            @Override
            public void onAlarmListChange(BluetoothDevice device, AlarmListInfo alarmListInfo) {
                //回调闹钟列表
            }

            @Override
            public void onAlarmDefaultBellListChange(BluetoothDevice device, List<DefaultAlarmBell> bells) {
                //回调闹钟默认铃声列表
            }

            @Override
            public void onAlarmNotify(BluetoothDevice device, AlarmBean alarmBean) {
                //回调提醒闹钟信息
            }

            @Override
            public void onAlarmStop(BluetoothDevice device, AlarmBean alarmBean) {
                //回调提醒闹钟响铃结束
            }

            @Override
            public void onID3MusicInfo(BluetoothDevice device, ID3MusicInfo id3MusicInfo) {
                //回调ID3播放信息
            }

            @Override
            public void onHighAndBassChange(BluetoothDevice device, int high, int bass) {
                //回调高低音信息
            }

            @Override
            public void onExpandFunction(BluetoothDevice device, int type, byte[] data) {
                //回调拓展功能信息
            }

            @Override
            public void onReverberation(BluetoothDevice device, ReverberationParam param) {
                //回调混淆信息
            }

            @Override
            public void onDynamicLimiter(BluetoothDevice device, DynamicLimiterParam param) {
                //回调动态限幅信息
            }

            @Override
            public void onPhoneCallStatusChange(BluetoothDevice device, int status) {
                //回调电话状态
            }


            @Override
            public void onLightControlInfo(BluetoothDevice device, LightControlInfo lightControlInfo) {
                //回调灯光控制信息
            }

            @Override
            public void onSoundCardEqChange(BluetoothDevice device, EqInfo eqInfo) {
                //回调声卡EQ效果信息
            }

            @Override
            public void onSoundCardStatusChange(BluetoothDevice device, long mask, byte[] values) {
                //回调声卡状态信息
            }

            @Override
            public void onCurrentVoiceMode(BluetoothDevice device, VoiceMode voiceMode) {
                //回调当前噪声处理模式信息
            }

            @Override
            public void onVoiceModeList(BluetoothDevice device, List<VoiceMode> voiceModes) {
                //回调所有噪声处理模式
            }

            @Override
            public void onHealthDataChange(BluetoothDevice device, HealthData data) {
                //回调健康数据
            }

            @Override
            public void onHealthSettingChange(BluetoothDevice device, HealthSettingInfo healthSettingInfo) {
                //回调健康设置信息
            }

            @Override
            public void onSensorLogDataChange(BluetoothDevice device, int type, byte[] data) {
                //回调传感器信息
            }

            @Override
            public void onSportsState(BluetoothDevice device, int state) {
                //回调运动状态
            }

            @Override
            public void onSportInfoChange(BluetoothDevice device, SportsInfo sportsInfo) {
                //回调运动信息
            }
        });
    }


    @Test
    public void registerRCSPCallback() {
        /*1. 初始化RCSP控制器， 实现RcspOpImpl流程参考上节1.4*/
        RcspManager manager = RcspManager.getInstance();
        /*2. 注册RCSP状态回调*/
        manager.registerOnRcspCallback(new OnRcspCallback() {
            @Override
            public void onRcspInit(BluetoothDevice device, boolean isInit) {
                //回调协议初始化结果
            }

            @Override
            public void onRcspCommand(BluetoothDevice device, CommandBase command) {
                //回调接收到的RCSP命令
            }

            @Override
            public void onRcspDataCmd(BluetoothDevice device, CommandBase dataCmd) {
                //回调接收到的RCSP数据命令
            }

            @Override
            public void onRcspError(BluetoothDevice device, BaseError error) {
                //回调RCSP处理异常
            }

            @Override
            public void onMandatoryUpgrade(BluetoothDevice device) {
                //回调设备需要强制升级
            }

            @Override
            public void onConnectStateChange(BluetoothDevice device, int status) {
                //回调设备连接状态
            }
        });
    }

}
