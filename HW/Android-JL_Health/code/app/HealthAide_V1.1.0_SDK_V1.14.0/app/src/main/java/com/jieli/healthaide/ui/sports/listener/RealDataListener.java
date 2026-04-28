package com.jieli.healthaide.ui.sports.listener;

import com.jieli.healthaide.ui.sports.model.BaseRealData;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/29
 * @desc :
 */
public interface RealDataListener<T extends BaseRealData> {


    void onRealDataChange(T t);
}
