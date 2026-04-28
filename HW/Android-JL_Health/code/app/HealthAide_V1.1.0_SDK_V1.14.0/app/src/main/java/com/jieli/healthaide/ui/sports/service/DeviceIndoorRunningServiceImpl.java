package com.jieli.healthaide.ui.sports.service;

import android.content.Context;

import com.jieli.healthaide.R;
import com.jieli.healthaide.ui.sports.listener.SportsInfoListener;
import com.jieli.healthaide.ui.sports.model.RunningRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.ui.sports.notify.DeviceIndoorRunningNotifySender;
import com.jieli.jl_rcsp.model.RealTimeSportsData;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc : 设备的室内运动服务
 */
public class DeviceIndoorRunningServiceImpl extends SportsNotifySenderServerImpl<RunningRealData> implements SportsService {

    private final static String tag = DeviceIndoorRunningServiceImpl.class.getSimpleName();

    private final DeviceSportsServiceImpl deviceSportsService;


    public DeviceIndoorRunningServiceImpl(Context context) {
        super(new DeviceIndoorRunningNotifySender(context));
        //Context mContext = context.getApplicationContext();
        SportsInfo sportsInfo = new SportsInfo();
        sportsInfo.type = SportsInfo.TYPE_INDOOR;
        sportsInfo.titleRes = R.string.sport_indoor_running;
        deviceSportsService = new DeviceSportsServiceImpl(context, sportsInfo);
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
    public void setSportsInfoListener(SportsInfoListener listener) {
        super.setSportsInfoListener(listener);
        deviceSportsService.setSportsInfoListener(sportsInfo -> sportsInfoListener.onSportsInfoChange(sportsInfo));
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


}
