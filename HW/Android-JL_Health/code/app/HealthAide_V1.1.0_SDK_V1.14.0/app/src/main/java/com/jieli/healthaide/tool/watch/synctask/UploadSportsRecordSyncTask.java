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
import com.jieli.jl_health_http.model.response.BooleanResponse;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/8
 * @desc :
 */
public class UploadSportsRecordSyncTask extends AbstractSyncTask {
    public UploadSportsRecordSyncTask(SyncTaskFinishListener finishListener) {
        super(finishListener);
    }

    @Override
    public int getType() {
        return TASK_TYPE_UPLOAD_SPORT_RECORD;
    }

    @Override
    public void start() {
        ThreadManager.getInstance().postRunnable(() -> {
            try {
                NetWorkStateModel netWorkStateModel = NetworkStateHelper.getInstance().getNetWorkStateModel();
                if (netWorkStateModel == null || !netWorkStateModel.isAvailable()) {
                    finishListener.onFinish();//无网络，不同步
                    return;
                }
                String uid = HealthApplication.getAppViewModel().getUid();
                if (TextUtils.isEmpty(uid)) {
                    JL_Log.i(tag, "start", "开始 UploadSportsRecordSyncTask 失败 uid is empty");
                    finishListener.onFinish();
                    return;
                }
                uploadSportRecord();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finishListener.onFinish();

        });
    }


    /**
     * 上传本地运动记录到服务器
     */
    private void uploadSportRecord() throws IOException {
        JL_Log.i(tag, "uploadSportRecord", "同步本地运动记录到服务器");
        SportRecordDao sportRecordDao = HealthDataDbHelper.getInstance().getSportRecordDao();
        String uid = HealthApplication.getAppViewModel().getUid();


        List<SportRecord> list = sportRecordDao.findBySync(uid, false);
        if (list == null || list.isEmpty()) {
            JL_Log.i(tag, "uploadSportRecord", "没有本地运动数据");
            finishListener.onFinish();
            return;
        }

        for (SportRecord sportRecord : list) {
            JL_Log.i(tag, "uploadSportRecord", "上传运动记录: " + sportRecord);
            LocationEntity locationEntity = HealthDataDbHelper.getInstance().getLocationDao().findByStartTime(uid, sportRecord.getStartTime());
            WrapperSportsRecord wrapperSportsRecord = new WrapperSportsRecord((byte) 0, sportRecord, locationEntity);
            try {
                if (uploadSportRecord(wrapperSportsRecord)) {
                    sportRecord.setSync(true);
                    sportRecordDao.update(sportRecord);//上传成功，刷新数据库
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JL_Log.i(tag, "uploadSportRecord", "上传本地运动记录到服务器 结束");

    }

    /**
     * 上传运动记录到服务器
     */
    private boolean uploadSportRecord(WrapperSportsRecord wrapperSportsRecord) throws IOException {
        SportRecord sportRecord = wrapperSportsRecord.getSportRecord();
        String date = CalendarUtil.serverDateFormat().format(new Date(sportRecord.getStartTime()));
        byte[] data = wrapperSportsRecord.toData();
//        if (true) {
//             JL_Log.w(tag, "wrapper sports record data:" + CHexConver.byte2HexStr(data));
//            return true;
//        }
//
//        WrapperSportsRecord wrapperSportsRecord1 = WrapperSportsRecord.from(sportRecord.getUid(), data);
//
//        JL_Log.w(tag, "parse wrapper sports record data:" + wrapperSportsRecord1.getSportRecord());
//        JL_Log.w(tag, "parse wrapper sports location data:" + wrapperSportsRecord1.getLocationEntity());
//        if (true) return false;

        RequestBody fileBody = RequestBody.create(data, MediaType.parse("application/octet-stream"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", sportRecord.getUid() + sportRecord.getStartTime(), fileBody);
        MultipartBody.Part typePart = MultipartBody.Part.createFormData("type", String.valueOf(sportRecord.getType()));
        MultipartBody.Part datePart = MultipartBody.Part.createFormData("date", date);
        Response<BooleanResponse> response = HttpClient.createSportDataApi().uploadSportData(filePart, typePart, datePart).execute();
        if (!response.isSuccessful()) {
            return false;
        }
        BooleanResponse booleanResponse = response.body();
        return booleanResponse != null && booleanResponse.getCode() == 0;

    }

}
