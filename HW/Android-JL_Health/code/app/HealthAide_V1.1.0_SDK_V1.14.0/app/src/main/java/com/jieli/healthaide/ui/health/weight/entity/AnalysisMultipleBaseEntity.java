package com.jieli.healthaide.ui.health.weight.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @ClassName: AnalysisMultipleBaseEntity
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/3/9 20:14
 */
public abstract class AnalysisMultipleBaseEntity implements MultiItemEntity {
    public static final int TYPE_ONE = 1;
    public static final int TYPE_TWO = 2;
    private int type;

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
