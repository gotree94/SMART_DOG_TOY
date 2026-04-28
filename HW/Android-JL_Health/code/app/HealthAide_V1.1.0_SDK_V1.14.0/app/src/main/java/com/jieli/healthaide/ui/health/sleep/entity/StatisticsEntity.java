package com.jieli.healthaide.ui.health.sleep.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/4/21 11:48 AM
 * @desc :
 */
public class StatisticsEntity implements MultiItemEntity {

    public static final int TYPE_TIME = 0;
    public static final int TYPE_SCORE = 1;


    public int itemType = TYPE_TIME;
    public int hour;

    public int min;

    public String type;

    public int score;

    public String result;

    public int typeColor;

    @Override
    public int getItemType() {
        return itemType;
    }


}
