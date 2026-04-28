package com.jieli.healthaide.ui.sports.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;

import com.jieli.component.utils.HandlerManager;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.sports.model.DeviceRealData;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.jl_rcsp.constant.RcspErrorCode;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.model.RealTimeSportsData;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.JL_Log;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/27
 * @desc : 设备实时数据同步
 */
public class DeviceSyncRealDataServiceImpl extends AbstractSportsServerImpl<DeviceRealData> implements SportsService {
    private final Context context;
    private SportsInfo sportsInfo;
    private final Handler handler = HandlerManager.getInstance().getMainHandler();
    private final Runnable readTask = new Runnable() {
        @Override
        public void run() {
            HealthOpImpl healthOp = WatchManager.getInstance().getHealthOp();
            healthOp.readRealTimeSportsData(healthOp.getConnectedDevice(), new OnOperationCallback<RealTimeSportsData>() {
                @Override
                public void onSuccess(RealTimeSportsData result) {
                    realDataListener.onRealDataChange(new DeviceRealData(result));
                    handler.removeCallbacks(readTask);
                    handler.postDelayed(readTask, sportsInfo.readRealDataInterval);
                }

                @Override
                public void onFailed(BaseError error) {
                    JL_Log.w("DeviceSyncRealDataServiceImpl", "onFailed", "主动获取实时运动数据：" + error);
                    if (error.getSubCode() == RcspErrorCode.ERR_RESPONSE_BAD_RESULT) {
                        handler.postDelayed(readTask, sportsInfo.readRealDataInterval);
                    }
//                    handler.postDelayed(readTask, 0);
                }
            });
        }
    };
    private final ScreenBroadcastReceiver broadcastReceiver = new ScreenBroadcastReceiver();

    public void setSportInfo(SportsInfo sportsInfo) {
        this.sportsInfo = sportsInfo;

    }

    public DeviceSyncRealDataServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        handler.removeCallbacks(readTask);
        handler.post(readTask);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        context.registerReceiver(broadcastReceiver, filter);
        broadcastReceiver.isRegister = true;
    }


    @Override
    public void stop() {
        handler.removeCallbacks(readTask);
        if (broadcastReceiver.isRegister) {
            context.unregisterReceiver(broadcastReceiver);
            broadcastReceiver.isRegister = false;
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private boolean isRegister = false;


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        pause();
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        resume();
                        break;
                }
            }
        }
    }


    @Override
    public void pause() {
        handler.removeCallbacks(readTask);
    }

    @Override
    public void resume() {
        handler.post(readTask);
    }


}
