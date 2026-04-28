package com.jieli.healthaide.ui.sports.service;

import com.jieli.healthaide.ui.sports.listener.RealDataListener;
import com.jieli.healthaide.ui.sports.listener.SportsInfoListener;
import com.jieli.healthaide.ui.sports.model.BaseRealData;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/29
 * @desc :
 */
public abstract class AbstractSportsServerImpl<T extends BaseRealData> implements SportsService {

    RealDataListener<T> realDataListener;
    SportsInfoListener sportsInfoListener;


    public void setRealDataListener(RealDataListener<T> realDataListener) {
        this.realDataListener = realDataListener;
    }

    public void setSportsInfoListener(SportsInfoListener sportsInfoListener) {
        this.sportsInfoListener = sportsInfoListener;
    }
}
