package com.jieli.watchtesttool.ui.ota;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 网络模块升级状态
 * @since 2023/12/20
 */
public class OtaStatus {
    public static final int STATE_IDLE = 0;
    public static final int STATE_START = 1;
    public static final int STATE_WORKING = 2;
    public static final int STATE_STOP = 3;

    private final int state;

    public OtaStatus(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    @Override
    public String toString() {
        return "OtaStatus{" +
                "state=" + state +
                '}';
    }
}
