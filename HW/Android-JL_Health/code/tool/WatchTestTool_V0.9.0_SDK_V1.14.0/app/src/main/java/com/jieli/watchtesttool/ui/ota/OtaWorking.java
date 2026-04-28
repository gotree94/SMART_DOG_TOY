package com.jieli.watchtesttool.ui.ota;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc OTA工作状态
 * @since 2023/12/20
 */
public class OtaWorking extends OtaStatus {
    private int progress;

    public OtaWorking() {
        super(STATE_WORKING);
    }

    public int getProgress() {
        return progress;
    }

    public OtaWorking setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    @Override
    public String toString() {
        return "OtaWorking{" +
                "progress=" + progress +
                '}';
    }
}
