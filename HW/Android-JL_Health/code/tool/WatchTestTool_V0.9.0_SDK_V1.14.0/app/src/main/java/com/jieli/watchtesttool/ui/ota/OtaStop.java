package com.jieli.watchtesttool.ui.ota;

import com.jieli.jl_rcsp.constant.WatchError;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc OTA结束状态
 * @since 2023/12/20
 */
public class OtaStop extends OtaStatus {
    private int code = -1;
    private String message;

    public OtaStop() {
        super(STATE_STOP);
    }

    public int getCode() {
        return code;
    }

    public OtaStop setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public OtaStop setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isSuccess() {
        return code == 0;
    }

    public boolean isCancel() {
        return code == WatchError.ERR_CANCEL_OP;
    }

    @Override
    public String toString() {
        return "OtaStop{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
