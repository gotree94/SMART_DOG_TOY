package com.jieli.healthaide.ui.device.nfc.bean;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  NFc状态
 * @since 2021/7/23
 */
public class NfcStatus {
    public final static int NFC_STATUS_IDLE = 0;//闲置
    public final static int NFC_STATUS_START = 1;
    public final static int NFC_STATUS_WORKING = 2;
    public final static int NFC_STATUS_STOP = 3;

    public final static int RESULT_OK = 0;
    public final static int RESULT_FAILURE = 1;
    public final static int RESULT_CANCEL = 2;

    private int status;
    private int result;
    private int progress;
    private int code;
    private String message;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "NfcStatus{" +
                "status=" + status +
                ", result=" + result +
                ", progress=" + progress +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
