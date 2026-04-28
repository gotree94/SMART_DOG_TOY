package com.jieli.healthaide.ui.sports.service;

import com.jieli.healthaide.ui.sports.listener.RealDataListener;
import com.jieli.healthaide.ui.sports.listener.SportsInfoListener;
import com.jieli.healthaide.ui.sports.model.BaseRealData;
import com.jieli.healthaide.ui.sports.notify.SportsNotifySender;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/2
 * @desc : 处理通知栏消息分发
 */
public abstract class SportsNotifySenderServerImpl<T extends BaseRealData> extends AbstractSportsServerImpl<T> {
    private final SportsNotifySender<T> sender;

    protected SportsNotifySenderServerImpl(SportsNotifySender<T> sender) {
        this.sender = sender;
    }

    @Override
    public void setSportsInfoListener(SportsInfoListener sportsInfoListener) {
        super.setSportsInfoListener(sportsInfo -> {
            sportsInfoListener.onSportsInfoChange(sportsInfo);
            sender.sendSportsInfo(sportsInfo);
        });
    }

    @Override
    public void setRealDataListener(RealDataListener<T> realDataListener) {
        super.setRealDataListener(t -> {
            realDataListener.onRealDataChange(t);
            sender.sendRealData(t);
        });
    }


}
