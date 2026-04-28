package com.jieli.healthaide.ui.sports.notify;

import com.jieli.healthaide.ui.sports.model.BaseRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/2
 * @desc : 运动状态的通知发送接口，当处于运动状态时会自动调用该接口的方法
 */
public interface SportsNotifySender<T extends BaseRealData> {


    /**
     * 运动的实时数据
     * @param t
     */
    void sendRealData(T t);

    /**
     * 运动的状态信息
     * @param info
     */
    void sendSportsInfo(SportsInfo info);

}
