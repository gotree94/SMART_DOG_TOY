package com.jieli.healthaide.ui.sports.model;

import com.jieli.jl_rcsp.model.RealTimeSportsData;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/29
 * @desc :
 */
public class DeviceRealData extends BaseRealData {
    private final RealTimeSportsData response;

    public DeviceRealData(RealTimeSportsData response) {
        this.response = response;
    }

    public RealTimeSportsData getResponse() {
        return response;
    }
}
