package com.jieli.healthaide.ui.device.market.bean;

import com.jieli.healthaide.ui.device.bean.OpResult;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 表盘信息列表
 * @since 2022/6/17
 */
public class WatchListResult extends OpResult<DialListMsg> {

    private final int dialType;

    public WatchListResult(int dialType) {
        this.dialType = dialType;
    }

    public int getDialType() {
        return dialType;
    }
}
