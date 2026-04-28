package com.jieli.watchtesttool.ui.upgrade;

import com.jieli.jl_bt_ota.model.base.BaseError;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc Ota阶段信息
 * @since 2021/3/15
 */
public class OtaState {

    public final static int OTA_STATE_IDLE = 0;
    public final static int OTA_STATE_PREPARE = 1;
    public final static int OTA_STATE_UPGRADE = 2;
    public final static int OTA_STATE_START = 3;
    public final static int OTA_STATE_WORKING = 4;
    public final static int OTA_STATE_STOP = 5;

    public final static int OTA_RES_SUCCESS = 1;
    public final static int OTA_RES_FAILED = 2;
    public final static int OTA_RES_CANCEL = 3;

    //升级准备
    public final static int OTA_TYPE_OTA_READY = 1;
    //升级固件
    public final static int OTA_TYPE_OTA_UPGRADE_FIRMWARE = 2;
    //更新资源
    public final static int OTA_TYPE_OTA_UPDATE_RESOURCE = 3;

    private int state;
    private String otaFilePath;
    private int otaType;
    private float otaProgress;
    private int stopResult;
    private BaseError error;
    /*升级文件总数*/
    private int otaTotal;
    /*升级文件序号*/
    private int otaIndex;
    /*升级文件信息*/
    private String otaFileInfo;

    public int getState() {
        return state;
    }

    public OtaState setState(int state) {
        this.state = state;
        return this;
    }

    public String getOtaFilePath() {
        return otaFilePath;
    }

    public OtaState setOtaFilePath(String otaFilePath) {
        this.otaFilePath = otaFilePath;
        return this;
    }

    public int getOtaType() {
        return otaType;
    }

    public OtaState setOtaType(int otaType) {
        this.otaType = otaType;
        return this;
    }

    public float getOtaProgress() {
        return otaProgress;
    }

    public OtaState setOtaProgress(float otaProgress) {
        this.otaProgress = otaProgress;
        return this;
    }

    public int getStopResult() {
        return stopResult;
    }

    public OtaState setStopResult(int stopResult) {
        this.stopResult = stopResult;
        return this;
    }

    public BaseError getError() {
        return error;
    }

    public OtaState setError(BaseError error) {
        this.error = error;
        return this;
    }

    public int getOtaTotal() {
        return otaTotal;
    }

    public OtaState setOtaTotal(int otaTotal) {
        this.otaTotal = otaTotal;
        return this;
    }

    public int getOtaIndex() {
        return otaIndex;
    }

    public OtaState setOtaIndex(int otaIndex) {
        this.otaIndex = otaIndex;
        return this;
    }

    public String getOtaFileInfo() {
        return otaFileInfo;
    }

    public OtaState setOtaFileInfo(String otaFileInfo) {
        this.otaFileInfo = otaFileInfo;
        return this;
    }

    @Override
    public String toString() {
        return "OtaState{" +
                "state=" + state +
                ", otaFilePath='" + otaFilePath + '\'' +
                ", otaType=" + otaType +
                ", otaProgress=" + otaProgress +
                ", stopResult=" + stopResult +
                ", error=" + error +
                ", otaTotal=" + otaTotal +
                ", otaIndex=" + otaIndex +
                ", otaFileInfo='" + otaFileInfo + '\'' +
                '}';
    }
}
