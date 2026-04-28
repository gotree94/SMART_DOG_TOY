package com.jieli.healthaide.ui.sports.service;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.nio.ByteBuffer;
import java.util.Calendar;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/27
 * @desc : GPS服务
 */
public class LocationServiceImpl implements SportsService, AMapLocationListener {
    private final String tag = getClass().getSimpleName();
    private SportsInfo sportsInfo;
    private final AMapLocationClient mLocationClient;


    private AMapLocationListener aMapLocationListener;


    public void setSportsInfo(SportsInfo sportsInfo) {
        this.sportsInfo = sportsInfo;
    }

    public LocationServiceImpl(Context context, SportsInfo sportsInfo) throws Exception {
        this.sportsInfo = sportsInfo;

        mLocationClient = new AMapLocationClient(context);
        mLocationClient.setLocationListener(this);
        AMapLocationClientOption option = new AMapLocationClientOption();
        int mInterval = 2000;
        option.setGpsFirst(true)
                .setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
                .setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport)
                .setInterval(mInterval)
                .setNeedAddress(false);
        mLocationClient.setLocationOption(option);
    }

    @Override
    public void start() {
        //保存记录gps的时间
        saveGpsStartTime();
        mLocationClient.startLocation();
    }


    @Override
    public void stop() {
        mLocationClient.stopLocation();
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() != AMapLocation.LOCATION_SUCCESS) {
            JL_Log.w(tag, "onLocationChanged", "定位失败，不能保存gps数据-->");
            return;
        }
        JL_Log.v(tag, "onLocationChanged", "获取位置信息成功-->" + aMapLocation.getLatitude() + "\t" + aMapLocation.getLongitude());
        aMapLocationListener.onLocationChanged(aMapLocation);
        ThreadManager.getInstance().postRunnable(() -> {
            String uid = HealthApplication.getAppViewModel().getUid();
            if (TextUtils.isEmpty(uid)) {
                JL_Log.e(tag, "onLocationChanged", "save location uid " + uid);
            }
            LocationEntity locationEntity = HealthDataDbHelper.getInstance()
                    .getLocationDao().findByStartTime(uid, RcspUtil.intToTime(sportsInfo.id));

            if (locationEntity == null) return;
            byte[] data = ByteBuffer.allocate(21)
                    .put(LocationEntity.FLAG_LOCATION)//type
                    .putDouble(aMapLocation.getLatitude())//纬度
                    .putDouble(aMapLocation.getLongitude())//经度
                    .putFloat(aMapLocation.getSpeed())
                    .array();
            data = ByteBuffer.allocate(locationEntity.getGpsData().length + data.length)
                    .put(locationEntity.getGpsData())
                    .put(data)
                    .array();
            locationEntity.setGpsData(data);
            HealthDataDbHelper.getInstance().getLocationDao().insert(locationEntity);
        });


    }

    @Override
    public void pause() {
        mLocationClient.stopLocation();
    }

    @Override
    public void resume() {
        mLocationClient.startLocation();
    }

    public void setMapLocationListener(AMapLocationListener aMapLocationListener) {
        this.aMapLocationListener = aMapLocationListener;
    }

    //保存开始时间节点
    private void saveGpsStartTime() {
        ThreadManager.getInstance().postRunnable(() -> {
            String uid = HealthApplication.getAppViewModel().getUid();
            LocationEntity locationEntity = HealthDataDbHelper.getInstance()
                    .getLocationDao().findByStartTime(uid, RcspUtil.intToTime(sportsInfo.id));
            byte[] data = ByteBuffer.allocate(9)
                    .put(LocationEntity.FLAG_TIME)
                    .putLong(Calendar.getInstance().getTimeInMillis())
                    .array();
            if (locationEntity == null) {
                locationEntity = new LocationEntity();
                locationEntity.setUid(uid);
                locationEntity.setStartTime(RcspUtil.intToTime(sportsInfo.id));
            } else {
                data = ByteBuffer.allocate(locationEntity.getGpsData().length + data.length)
                        .put(locationEntity.getGpsData())
                        .put(data)
                        .array();
            }
            locationEntity.setGpsData(data);
            HealthDataDbHelper.getInstance().getLocationDao().insert(locationEntity);
        });
    }
}
