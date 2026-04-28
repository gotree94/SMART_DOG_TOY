package com.jieli.healthaide.ui.sports.model;

import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.data.entity.SportRecord;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/18
 * @desc :
 */
public class SportsRecordAndLocation {
    private SportRecord sportRecord;
    private LocationEntity locationEntity;
    private String bmpPath;


    public SportsRecordAndLocation(SportRecord sportRecord, LocationEntity locationEntity,String bmpPath) {
        this.sportRecord = sportRecord;
        this.locationEntity = locationEntity;
        this.bmpPath = bmpPath;
    }

    public SportRecord getSportRecord() {
        return sportRecord;
    }

    public void setSportRecord(SportRecord sportRecord) {
        this.sportRecord = sportRecord;
    }

    public LocationEntity getLocationEntity() {
        return locationEntity;
    }

    public void setLocationEntity(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
    }

    public String getBmpPath() {
        return bmpPath;
    }

    public void setBmpPath(String bmpPath) {
        this.bmpPath = bmpPath;
    }
}
