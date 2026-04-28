package com.jieli.healthaide.ui.sports.model;

import com.amap.api.location.AMapLocation;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/28
 * @desc :
 */
public class LocationRealData extends BaseRealData {
    public AMapLocation aMapLocation;

    public LocationRealData(AMapLocation aMapLocation) {
        this.aMapLocation = aMapLocation;
    }
}
