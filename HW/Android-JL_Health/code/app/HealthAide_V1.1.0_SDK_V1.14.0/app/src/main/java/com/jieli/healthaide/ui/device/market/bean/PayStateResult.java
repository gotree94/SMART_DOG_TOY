package com.jieli.healthaide.ui.device.market.bean;

import com.jieli.healthaide.ui.device.bean.OpResult;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  付款状态结果
 * @since 2022/6/23
 */
public class PayStateResult extends OpResult<Boolean> {
    private int payWay;

    public int getPayWay() {
        return payWay;
    }

    public void setPayWay(int payWay) {
        this.payWay = payWay;
    }
}
