package com.jieli.healthaide.ui.device.market.bean;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 支付方式
 * @since 2022/6/20
 */
public class PayWayEntity {
    public static final int PAY_WAY_ALI = 0;
    public static final int PAY_WAY_WEIXIN = 1;

    private final int way;

    public PayWayEntity(int way) {
        this.way = way;
    }

    public int getWay() {
        return way;
    }
}
