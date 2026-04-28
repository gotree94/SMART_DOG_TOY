package com.jieli.healthaide.ui.login.bean;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 2:06 PM
 * @desc :
 */
public class SmsCounter {
    public static final int OP_IDLE = 0;
    public static final int OP_COUNTER = 1;

    public static final int OP_SEND_CODE = 2; //
    public static final int OP_SEND_CODE_FINISH = 3;
    public static final int OP_SEND_CODE_ERROR = 4;
    private int op = OP_IDLE;
    private int time;


    public SmsCounter() {
    }

    public SmsCounter(int op, int time) {
        this.op = op;
        this.time = time;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
