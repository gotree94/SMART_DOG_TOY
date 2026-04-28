package com.jieli.watchtesttool.ui.file;

import android.bluetooth.BluetoothDevice;


import com.jieli.jl_rcsp.constant.AttrAndFunCode;
import com.jieli.jl_rcsp.constant.Command;
import com.jieli.jl_rcsp.interfaces.rcsp.OnRcspCallback;
import com.jieli.jl_rcsp.model.base.CommandBase;
import com.jieli.jl_rcsp.model.command.sys.UpdateSysInfoCmd;
import com.jieli.jl_rcsp.model.device.AttrBean;
import com.jieli.jl_rcsp.model.parameter.UpdateSysInfoParam;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.watchtesttool.ui.file.model.MusicNameInfo;
import com.jieli.watchtesttool.ui.file.model.MusicStatusInfo;
import com.jieli.watchtesttool.ui.file.model.PlayModeInfo;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/15/21 7:42 PM
 * @desc :
 */
public class MusicInfoHandler extends OnRcspCallback {

    @Override
    public void onRcspCommand(BluetoothDevice device, CommandBase cmd) {
        if (cmd.getId() != Command.CMD_SYS_INFO_AUTO_UPDATE) return;
        UpdateSysInfoCmd updateSysInfoCmd = (UpdateSysInfoCmd) cmd;
        UpdateSysInfoParam param = updateSysInfoCmd.getParam();
        List<AttrBean> list = param.getAttrBeanList();
        if (param.getFunction() == AttrAndFunCode.SYS_INFO_FUNCTION_MUSIC) {
            parseMusicData(device, list);
        } else if (param.getFunction() == AttrAndFunCode.SYS_INFO_FUNCTION_PUBLIC) {
            parseMusicData(device, list);
        }
    }


    void parseDeviceModeData(BluetoothDevice device, List<AttrBean> list) {
        for (AttrBean attrBean : list) {
            if (attrBean.getType() == AttrAndFunCode.SYS_INFO_ATTR_CUR_MODE_TYPE) {
                int mode = CHexConver.byteToInt(attrBean.getAttrData()[0]);
                onDeviceModeChange(mode);
            }
        }
    }

    void parseMusicData(BluetoothDevice device, List<AttrBean> list) {
        if (list == null) return;
        for (AttrBean attr : list) {
            byte[] data = attr.getAttrData();
            if (data == null || data.length == 0) continue;
            switch (attr.getType()) {
                case AttrAndFunCode.SYS_INFO_ATTR_MUSIC_STATUS_INFO:
                    boolean isPlay = (data[0] & 0x01) == 0x01;
                    int currentTime = 0;
                    int totalTime = 0;
                    int devIndex = 0;
                    int offset = 1;
                    if (data.length > 4) {
                        byte[] temp = new byte[4];
                        System.arraycopy(data, offset, temp, 0, temp.length);
                        offset += temp.length;
                        currentTime = CHexConver.bytesToInt(temp) * 1000; //秒转换成毫秒
                        if (data.length > 8) {
                            System.arraycopy(data, offset, temp, 0, temp.length);
                            offset += temp.length;
                            totalTime = CHexConver.bytesToInt(temp) * 1000; //秒转换成毫秒
                            if (data.length > 9) {
                                devIndex = CHexConver.byteToInt(data[offset]);
                            }
                        }
                    }
                    onMusicStatusChange(new MusicStatusInfo(isPlay, currentTime, totalTime, devIndex));
                    break;
                case AttrAndFunCode.SYS_INFO_ATTR_MUSIC_FILE_NAME_INFO:
                    if (data.length > 3) {
                        int index = 0;
                        byte[] buf = new byte[4];
                        System.arraycopy(data, index, buf, 0, buf.length);
                        index += buf.length;
                        int cluster = CHexConver.bytesToInt(buf);
                        String name = null;
                        if (data.length > 4) {
                            boolean isAsni = (data[index] & 0xff) == 0x01;
                            index++;
                            if (data.length > 5) {
                                try {
                                    name = new String(data, index, (data.length - index), isAsni ? "gbk" : "utf-16le");
                                    /*byte[] nameBuf = new byte[data.length - index];
                                    System.arraycopy(data, index, nameBuf, 0, nameBuf.length);
                                    JL_Log.d(TAG, "parseMusicData :: music name : " + name + ", raw data : " + CHexConver.byte2HexStr(nameBuf) +
                                            ", cluster : " + cluster + ", isAsni : " + isAsni);*/
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        onMusicNameChange(new MusicNameInfo(cluster, name));
                    }
                    break;
                case AttrAndFunCode.SYS_INFO_ATTR_MUSIC_PLAY_MODE:
                    int mode = CHexConver.byteToInt(data[0]);
                    onPlayModeChange(new PlayModeInfo(mode));
                    break;
            }
        }
    }

    protected void onMusicStatusChange(MusicStatusInfo musicStatusInfo) {
    }

    protected void onMusicNameChange(MusicNameInfo musicNameInfo) {
    }

    protected void onPlayModeChange(PlayModeInfo playModeInfo) {
    }

    protected void onDeviceModeChange(int mode) {

    }


}
