package com.jieli.healthaide.ui.sports.service;

import android.content.Context;

import com.amap.api.location.AMapLocationListener;
import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.sports.listener.SportsInfoListener;
import com.jieli.healthaide.ui.sports.model.RunningRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.ui.sports.notify.DeviceOutdoorRunningNotifySender;
import com.jieli.jl_rcsp.model.RealTimeSportsData;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :设备的室外运动服务
 */
public class DeviceOutdoorRunningServiceImpl extends SportsNotifySenderServerImpl<RunningRealData> implements SportsService {
    //private final static String tag = DeviceOutdoorRunningServiceImpl.class.getSimpleName();

    private final DeviceSportsServiceImpl deviceSportsService;
    private LocationServiceImpl locationService;

    public DeviceOutdoorRunningServiceImpl(Context context) {
        super(new DeviceOutdoorRunningNotifySender(context));
        SportsInfo sportsInfo = new SportsInfo();
        sportsInfo.type = SportsInfo.TYPE_OUTDOOR;
        sportsInfo.useMap = true;
        sportsInfo.titleRes = R.string.sport_outdoor_running;
        deviceSportsService = new DeviceSportsServiceImpl(context, sportsInfo);
        try {
            locationService = new LocationServiceImpl(context, sportsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        deviceSportsService.setRealDataListener(deviceRealData -> {
            RealTimeSportsData input = deviceRealData.getResponse();
            RunningRealData runningRealData = new RunningRealData();
            runningRealData.step = Math.max(0, input.getStep());
            runningRealData.distance = input.getDistance();
            runningRealData.duration = input.getDuration();
            runningRealData.speed = input.getSpeed();
            runningRealData.pace = input.getPace();
            runningRealData.kcal = input.getCalorie();
            runningRealData.stepFreq = input.getStepFreq();
            runningRealData.stride = input.getStride();
            runningRealData.sportsStatus = input.getExerciseStatus();
            runningRealData.heartRate = input.getHeartRate();
            realDataListener.onRealDataChange(runningRealData);
        });

    }

    @Override
    public void start() {
        deviceSportsService.start();
    }

    @Override
    public void pause() {
        deviceSportsService.pause();
    }

    @Override
    public void resume() {
        deviceSportsService.resume();
    }

    @Override
    public void stop() {
        deviceSportsService.stop();
    }

    @Override
    public void setSportsInfoListener(SportsInfoListener sportsInfoListener) {
        super.setSportsInfoListener(sportsInfoListener);
        deviceSportsService.setSportsInfoListener(new WrapperSportsInfoListener(super.sportsInfoListener));//需要用父类的回调
    }

    public void setMapLocationListener(AMapLocationListener aMapLocationListener) {
        if (locationService != null) {
            locationService.setMapLocationListener(aMapLocationListener);
        }
    }

    private class WrapperSportsInfoListener implements SportsInfoListener {
        private final SportsInfoListener sportsInfoListener;


        public WrapperSportsInfoListener(SportsInfoListener sportsInfoListener) {
            this.sportsInfoListener = sportsInfoListener;
        }

        @Override
        public void onSportsInfoChange(SportsInfo sportsInfo) {
            if (locationService != null) {
                switch (sportsInfo.status) {
                    case SportsInfo.STATUS_BEGIN:
                    case SportsInfo.STATUS_RESUME:
                        locationService.setSportsInfo(sportsInfo);
                        locationService.start();
                        break;
                    case SportsInfo.STATUS_PAUSE:
                        locationService.pause();
                        break;
                    case SportsInfo.STATUS_STOP:
                        locationService.stop();
                        break;
                }
            }
            sportsInfoListener.onSportsInfoChange(sportsInfo);
        }
    }


}
