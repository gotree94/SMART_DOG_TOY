package com.jieli.healthaide.tool.watch.synctask;

import android.text.TextUtils;
import android.util.Base64;

import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.param.HealthDataParam;
import com.jieli.jl_health_http.model.param.RangeParam;
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_health_http.model.response.HealthDataRangeResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date :  2021/7/20
 * @desc : 同步健康数据到服务器
 */
public class ServerHealthDataSyncTask extends AbstractSyncTask implements Runnable {
    private String uid;

    public ServerHealthDataSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
    }


    @Override
    public void setFinishListener(SyncTaskFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    @Override
    public void run() {
        try {
            JL_Log.i(tag, tag, "start");
            NetWorkStateModel netWorkStateModel = NetworkStateHelper.getInstance().getNetWorkStateModel();
            if (netWorkStateModel == null || !netWorkStateModel.isAvailable()) {
                finishListener.onFinish();//无网络，不同步
                return;
            }
//            HealthDataDbHelper.getInstance().getHealthDao().clean();
            uid = HealthApplication.getAppViewModel().getUid();
            if (TextUtils.isEmpty(uid)) {
                JL_Log.i(tag, tag, "uid is empty");
                finishListener.onFinish();
                return;
            }
            syncServerToLocalData();
            syncLocalDataToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishListener.onFinish();
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_HEALTH_DATA;
    }

    @Override
    public synchronized void start() {
        //super.start();
        ThreadManager.getInstance().postRunnable(this);
    }

    /**
     * 下载服务器数据到本地
     *
     * @throws IOException
     */
    private void syncServerToLocalData() throws IOException {
        JL_Log.i(tag, "syncServerToLocalData", "同步服务器健康数据到本地");
        Calendar calendar = Calendar.getInstance();
        HealthEntity healthEntity = HealthDataDbHelper.getInstance().getHealthDao().getLastData(uid);//读取数据库最新日期的数据
        long dayTime = 1000 * 3600 * 24;
        if (healthEntity == null) {
            long registerTime = HealthApplication.getAppViewModel().getRegisterTime() - dayTime;
            calendar.setTimeInMillis(registerTime);//如果没有记录，则从注册时间作为开始时间
        } else {
            calendar.setTimeInMillis(healthEntity.getTime() + 1000 * 60);
        }

        long startTime = calendar.getTimeInMillis();
        long endTime = Calendar.getInstance().getTimeInMillis();
        long spaceTime = 10 * dayTime;
        //分页读取数据，暂定10天一个间隔
        for (long start = startTime; start < endTime; start += spaceTime) {
            String startString = CalendarUtil.serverDateFormat().format(start);
            long end = Math.min(start + spaceTime, Calendar.getInstance().getTimeInMillis());
            String endString = CalendarUtil.serverDateFormat().format(end);
            JL_Log.i(tag, "syncServerToLocalData", "startTime = " + startString + "\tendTime = " + endString);
            syncServerToLocalDataByRange(startString, endString);
        }

    }

    private void syncServerToLocalDataByRange(String startTime, String endTime) throws IOException {
        Response<HealthDataRangeResponse> response = HttpClient.createHealthDataApi().getHealthData(new RangeParam(startTime, endTime)).execute();
        if (!response.isSuccessful()) {
            JL_Log.w(tag, "syncServerToLocalDataByRange", "下载服务器健康数据失败 -->" + response.message());
            return;
        }
        HealthDataRangeResponse rangeResponse = response.body();
        if (rangeResponse == null || rangeResponse.getCode() != 0) {
            JL_Log.w(tag, "syncServerToLocalDataByRange", "下载服务器健康数据失败  ------>" + rangeResponse);
            return;
        }
        for (HealthDataRangeResponse.HealthData healthData : rangeResponse.getT()) {

            if (!healthData.getUid().equals(uid)) {
                JL_Log.w(tag, "syncServerToLocalDataByRange", "异常uid  ------>" + healthData.getUid() + "\tuid=" + uid);
                continue;
            }

            byte[] src = Base64.decode(healthData.getData(), Base64.DEFAULT);
            HealthEntity entity = HealthEntity.from(src);
            if (entity == null) continue;
            entity.setSync(true);
            entity.setUid(uid);
            JL_Log.d(tag, "syncServerToLocalDataByRange", entity.toString());
            HealthDataDbHelper.getInstance().getHealthDao().insert(entity);//插入数据库
        }
    }

    /**
     * 上报健康数据到服务器
     */
    private void syncLocalDataToServer() throws IOException {
        JL_Log.i(tag, "syncLocalDataToServer", "同步本地健康数据到服务器");
        List<HealthEntity> healthEntities = HealthDataDbHelper.getInstance().getHealthDao().findBySync(uid, false);
        if (healthEntities == null || healthEntities.isEmpty()) {
            JL_Log.d(tag, "syncLocalDataToServer", "所有健康数据已上报到服务器");
            return;
        }

        List<HealthDataParam> params = new ArrayList<>();
        for (HealthEntity entity : healthEntities) {
            String base64Data = Base64.encodeToString(entity.getData(), Base64.DEFAULT);
            String dateString = CalendarUtil.serverDateFormat().format(new Date(entity.getTime()));
            HealthDataParam param = new HealthDataParam(String.valueOf(entity.getType()), base64Data, dateString);
            params.add(param);
        }
        Response<BooleanResponse> response = HttpClient.createHealthDataApi().uploadHealthData(params).execute();
        if (!response.isSuccessful()) {
            JL_Log.w(tag, "syncLocalDataToServer", "健康数据上报失败 -->" + response.message());
            return;
        }
        BooleanResponse booleanResponse = response.body();
        if (booleanResponse == null || booleanResponse.getCode() != 0) {
            JL_Log.w(tag, "syncLocalDataToServer", "健康数据上报失败  ------>" + booleanResponse);
            return;
        }

        HealthDataDbHelper.getInstance().getHealthDatabase().runInTransaction(() -> {
            //更新上传标志位,需要添加事务
            for (HealthEntity entity : healthEntities) {
                entity.setSync(true);
                HealthDataDbHelper.getInstance().getHealthDao().insert(entity);
            }
        });
//
//        for (HealthEntity entity : healthEntities) {
//            entity.setSync(true);
//            HealthDataDbHelper.getInstance().getHealthDao().insert(entity);
//        }
    }
}
