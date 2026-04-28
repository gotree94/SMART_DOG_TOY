package com.jieli.watchtesttool.tool.upgrade.auto;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 更新事件监听器
 * @since 2022/8/2
 */
public interface OnUpdateListener {
    int OTA_TYPE_FIRMWARE = 0;
    int OTA_TYPE_RESOURCE = 1;

    void onStart(int otaType, String filePath, int total);

    void onProgress(int otaType, int flag, String filePath, float progress);

    void onNeedReconnect(int otaType, String mac, boolean isNewWay);

    void onStop(int otaType, String otaFilePath);

    void onCancel(int otaType, String filePath);

    void onError(int otaType, String filePath, int code, String message);
}
