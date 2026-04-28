package com.jieli.healthaide.ui.device.market.bean;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  支付结果
 * @since 2022/6/23
 */
public class PayResult {

    public static final int PAY_WAY_ALI = 0;
    public static final int PAY_WAY_WEIXIN = 1;

    private final int payWay;
    private int code;
    private String message;

    public PayResult(int payWay){
        this.payWay = payWay;
    }

    public int getPayWay() {
        return payWay;
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
}
