package com.jieli.healthaide.ui.device.upgrade;

/**
 * ReconnectDeviceParam
 *
 * @author zhongzhuocheng
 * email: zhongzhuocheng@zh-jieli.com
 * create: 2025/12/1
 * note: 回连设备参数
 */
class ReconnectDeviceParam {
    private final String address;
    private final String filePath;

    public ReconnectDeviceParam(String address, String filePath) {
        this.address = address;
        this.filePath = filePath;
    }

    public String getAddress() {
        return address;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "ReconnectDeviceParam{" +
                "address='" + address + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
