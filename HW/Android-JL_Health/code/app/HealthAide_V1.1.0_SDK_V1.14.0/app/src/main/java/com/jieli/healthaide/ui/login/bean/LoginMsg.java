package com.jieli.healthaide.ui.login.bean;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/9/21 4:10 PM
 * @desc :
 */
public class LoginMsg {

    public static final int STATE_IDLE = 0;
    public static final int STATE_LOGINING = 1;
    public static final int STATE_LOGIN_FINISH = 2;
    public static final int STATE_LOGIN_ERROR = 3;


    private int state = STATE_IDLE;
    private String msg;

    public LoginMsg(int op, String msg) {
        this.state = op;
        this.msg = msg;
    }

    public LoginMsg() {

    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
