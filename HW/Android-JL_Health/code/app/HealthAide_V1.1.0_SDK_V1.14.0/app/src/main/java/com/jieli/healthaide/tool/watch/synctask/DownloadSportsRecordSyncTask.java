package com.jieli.healthaide.tool.watch.synctask;

import android.text.TextUtils;

import com.jieli.component.thread.ThreadManager;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.dao.SportRecordDao;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.tool.watch.synctask.model.WrapperSportsRecord;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.jl_health_http.HttpClient;
import com.jieli.jl_health_http.model.param.RangeParam;
import com.jieli.jl_health_http.model.response.SportRecordDataRangeResponse;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/20
 * @desc :
 */
public class DownloadSportsRecordSyncTask extends AbstractSyncTask implements Runnable {

    private String uid;

    public DownloadSportsRecordSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
    }


    @Override
    public int getType() {
        return TASK_TYPE_SYNC_SPORT_RECORD;
    }

    @Override
    public void start() {
        //super.start();
        ThreadManager.getInstance().postRunnable(this);
    }


    @Override
    public void run() {
        JL_Log.d(tag, tag, "start");
        try {
            NetWorkStateModel netWorkStateModel = NetworkStateHelper.getInstance().getNetWorkStateModel();
            if (netWorkStateModel == null || !netWorkStateModel.isAvailable()) {
                finishListener.onFinish();//无网络，不同步
                return;
            }
            uid = HealthApplication.getAppViewModel().getUid();
            if (TextUtils.isEmpty(uid)) {
                JL_Log.i(tag, tag, "uid is empty");
                finishListener.onFinish();
                return;
            }
            syncServerRecordToLocal();//下载服务器的远动记录到本地
        } catch (Exception e) {
            JL_Log.d(tag, tag, "同步服务器 运动记录失败。 异常信息 : " + e.getMessage());
        }
        finishListener.onFinish();
    }

    private void syncServerRecordToLocal() throws IOException {
        JL_Log.i(tag, "syncServerRecordToLocal", "开始同步服务器的运动记录到本地");
        SportRecordDao sportRecordDao = HealthDataDbHelper.getInstance().getSportRecordDao();
        SportRecord sportRecord = sportRecordDao.getLastData(uid);
        Calendar calendar = Calendar.getInstance();
        long dayTime = 1000 * 3600 * 24;
        if (sportRecord == null) {
            long registerTime = HealthApplication.getAppViewModel().getRegisterTime() - dayTime;
            calendar.setTimeInMillis(registerTime);//如果没有记录，则从注册时间作为开始时间
        } else {
            calendar.setTimeInMillis(sportRecord.getStartTime() + 1000);
        }

        long startTime = calendar.getTimeInMillis();
        long endTime = Calendar.getInstance().getTimeInMillis();
        long spaceTime = 30 * dayTime;  //分页读取数据，暂定30天一个间隔
        for (long start = startTime; start < endTime; start += spaceTime) {
            String startString = CalendarUtil.serverDateFormat().format(start);
            long temp = Math.min(start + spaceTime, Calendar.getInstance().getTimeInMillis());
            String endString = CalendarUtil.serverDateFormat().format(temp);
            JL_Log.d(tag, "syncServerRecordToLocal", "startTime = " + startString + "\tendTime = " + endString);
            syncServerRecordToLocalByRange(startString, endString);
        }

    }

    /**
     * 下载服务器的远动记录到本地
     */
    private void syncServerRecordToLocalByRange(String start, String end) throws IOException {
        Response<SportRecordDataRangeResponse> response = HttpClient.createSportDataApi().getSportData(new RangeParam(start, end)).execute();
        if (!response.isSuccessful()) {
            JL_Log.w(tag, "syncServerRecordToLocalByRange", "获取服务器运动记录失败 httpCode = " + response.code());
            return;
        }
        SportRecordDataRangeResponse rangeResponse = response.body();
        if (rangeResponse == null || rangeResponse.getCode() != 0) {
            JL_Log.w(tag, "syncServerRecordToLocalByRange", "获取服务器运动记录失败 code = " + (rangeResponse == null ? -1 : rangeResponse.getCode()));
            return;
        }
        for (SportRecordDataRangeResponse.SportRecordData sportRecordData : rangeResponse.getT()) {
            JL_Log.i(tag, "syncServerRecordToLocalByRange", sportRecordData.toString());
            saveServerDataToLocal(sportRecordData);
        }
    }


    /**
     * 保存运动到本地数据库
     *
     * @param sportRecordData
     */
    private void saveServerDataToLocal(SportRecordDataRangeResponse.SportRecordData
                                               sportRecordData) throws IOException {
        JL_Log.d(tag, "saveServerDataToLocal", "下载运动记录文件");
        String path = sportRecordData.getData();
        Response<ResponseBody> response = HttpClient.createDownloadApi().download(path).execute();
        if (!response.isSuccessful()) {
            JL_Log.w(tag, "saveServerDataToLocal", "文件下载失败，status = " + response.code());
            return;
        }
        ResponseBody body = response.body();
        if (body == null) {
            JL_Log.w(tag, "saveServerDataToLocal", "文件下载失败，body 为空");
            return;
        }
        byte[] bytes = body.bytes();
        WrapperSportsRecord wrapperSportsRecord = WrapperSportsRecord.from(sportRecordData.getUid()
                , bytes);
        if (wrapperSportsRecord == null) {
            JL_Log.i(tag, "saveServerDataToLocal", "文件下载成功 ，数据解析失败 -->" + CHexConver.byte2HexStr(bytes));
            return;
        }
        SportRecord sportRecord = wrapperSportsRecord.getSportRecord();

//        String savePath = HealthApplication.getAppViewModel().getApplication().getExternalCacheDir() + File.separator + sportRecord.getStartTime() + ".sp";
//        FileOutputStream fos = new FileOutputStream(savePath);
//        fos.write(bytes);

        sportRecord.setUid(uid);
        sportRecord.setSync(true);
        if (sportRecord == null) {
            JL_Log.i(tag, "saveServerDataToLocal", "文件下载成功 ，运动记录数据解析失败 -->" + CHexConver.byte2HexStr(bytes));
            return;
        }

        final LocationEntity locationEntity = wrapperSportsRecord.getLocationEntity();
        HealthDataDbHelper.getInstance().getHealthDatabase().runInTransaction(() -> {
            //插入数据,需要添加事务
            if (locationEntity != null) {
                HealthDataDbHelper.getInstance().getLocationDao().insert(locationEntity);
            }
            HealthDataDbHelper.getInstance().getSportRecordDao().insert(sportRecord);
        });
    }


}
