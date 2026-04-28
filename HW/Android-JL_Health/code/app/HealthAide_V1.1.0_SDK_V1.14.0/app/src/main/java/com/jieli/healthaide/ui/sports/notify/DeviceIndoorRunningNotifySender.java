package com.jieli.healthaide.ui.sports.notify;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.jieli.healthaide.ui.service.HealthService;
import com.jieli.healthaide.ui.sports.model.RunningRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/2
 * @desc :
 */
public class DeviceIndoorRunningNotifySender implements SportsNotifySender<RunningRealData> {
    Context context;

    public DeviceIndoorRunningNotifySender(Context context) {
        this.context = context;
    }

    @Override
    public void sendRealData(RunningRealData runningRealData) {
        Log.d("ZHM", "sendRealData: duration : " + runningRealData.duration + " distance : " + runningRealData.distance + " speed: " + runningRealData.speed);
        Intent intent = new Intent(context, HealthService.class);
        intent.setAction(HealthService.ACTION_SPORT_MODE);
        intent.putExtra(HealthService.EXTRA_SPORT_TIME, (long) runningRealData.duration * 1000);
        intent.putExtra(HealthService.EXTRA_SPORT_DISTANCE, (double) runningRealData.distance);
        intent.putExtra(HealthService.EXTRA_SPORT_PACE, (double) runningRealData.pace);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void sendSportsInfo(SportsInfo info) {
        Log.d("ZHM", "sendSportsInfo: " + info);
        if (info != null) {
            Intent intent = new Intent(context.getApplicationContext(), HealthService.class);
            intent.setAction(HealthService.ACTION_CURRENT_MODE);
            boolean running = info.status == SportsInfo.STATUS_BEGIN || info.status == SportsInfo.STATUS_RESUME;
            if (running) {
                intent.putExtra(HealthService.EXTRA_CURRENT_MODE, HealthService.ACTION_SPORT_MODE);
            } else {
                intent.putExtra(HealthService.EXTRA_CURRENT_MODE, HealthService.ACTION_HEALTH_RECORD);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getApplicationContext().startForegroundService(intent);
            } else {
                context.getApplicationContext().startService(intent);
            }
        }
    }


}
