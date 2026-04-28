package com.jieli.healthaide.ui.device.market.bean;

import com.chad.library.adapter.base.entity.JSectionEntity;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  表盘商城项
 * @since 2022/6/16
 */
public class DialShopItem extends JSectionEntity {
    private final boolean isHeader;
    private final Object object;

    public DialShopItem(boolean isHeader, Object obj){
        this.isHeader = isHeader;
        this.object = obj;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public boolean isHeader() {
        return isHeader;
    }
}
