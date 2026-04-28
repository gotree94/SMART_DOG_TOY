package com.jieli.healthaide.tool.bluetooth;

import com.jieli.bluetooth_connect.annotation.ConnectWay;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 需要更新的设备
 * @since 2022/6/29
 */
public class NeedUpdateDevice {
    private final String changeBleAddress;
    private final String originalBleAddress;
    private final int deviceType;
    @ConnectWay
    private final int connectWay;
    private final int vid;
    private final int uid;
    private final int pid;

    public NeedUpdateDevice(String changeBleAddress, String originalBleAddress, int deviceType,
                            int vid, int uid, int pid) {
        this(changeBleAddress, originalBleAddress, deviceType, BluetoothConstant.PROTOCOL_TYPE_BLE, vid, uid, pid);
    }

    public NeedUpdateDevice(String changeBleAddress, String originalBleAddress, int deviceType, @ConnectWay int connectWay,
                            int vid, int uid, int pid) {
        this.changeBleAddress = changeBleAddress;
        this.originalBleAddress = originalBleAddress;
        this.deviceType = deviceType;
        this.connectWay = connectWay;
        this.vid = vid;
        this.uid = uid;
        this.pid = pid;
    }

    public String getChangeBleAddress() {
        return changeBleAddress;
    }

    public String getOriginalBleAddress() {
        return originalBleAddress;
    }

    public int getDeviceType() {
        return deviceType;
    }

    @ConnectWay
    public int getConnectWay() {
        return connectWay;
    }

    public int getVid() {
        return vid;
    }

    public int getUid() {
        return uid;
    }

    public int getPid() {
        return pid;
    }

    @Override
    public String toString() {
        return "NeedUpdateDevice{" +
                "changeBleAddress='" + changeBleAddress + '\'' +
                ", originalBleAddress='" + originalBleAddress + '\'' +
                ", deviceType=" + deviceType +
                ", connectWay=" + connectWay +
                ", vid=" + vid +
                ", uid=" + uid +
                ", pid=" + pid +
                '}';
    }
}
