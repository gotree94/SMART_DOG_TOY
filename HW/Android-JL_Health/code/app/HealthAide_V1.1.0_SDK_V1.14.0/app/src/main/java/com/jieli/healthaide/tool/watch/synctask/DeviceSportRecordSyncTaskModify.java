package com.jieli.healthaide.tool.watch.synctask;

import android.text.TextUtils;

import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.dao.SportRecordDao;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.jl_rcsp.task.smallfile.QueryFileTask;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/7/21
 * @desc : 同步固件的运动记录到手机
 */
public class DeviceSportRecordSyncTaskModify extends SmallFileSyncTask {


    public DeviceSportRecordSyncTaskModify(SyncTaskFinishListener finishListener) {
        super(QueryFileTask.TYPE_SPORTS_RECORD, finishListener);

    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_SMALL_FILE + type;
    }

    @Override
    public void start() {
        JL_Log.d(tag, "start", "");
//        HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).SportRecordDao().clean();
        super.start();
    }

    @Override
    protected boolean isInLocal(byte[] data) {
        SportRecord sportRecord = SportRecord.fromHeader(data);

        String uid = HealthApplication.getAppViewModel().getUid();

        if (TextUtils.isEmpty(uid)) {
            JL_Log.w(tag, "isInLocal", "uid isEmpty");
            return false;
        }
        JL_Log.d(tag, "isInLocal", "sportRecord : " + sportRecord);
        sportRecord.setUid(uid);
        SportRecordDao sportRecordDao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).SportRecordDao();
        SportRecord dbRecord = sportRecordDao.findByStartTime(uid, sportRecord.getStartTime());
        boolean ret = dbRecord != null && dbRecord.getStartTime() == sportRecord.getStartTime();
        JL_Log.w(tag, "isInLocal", "" + ret);
        return ret;
    }

    @Override
    protected void saveToDb(byte[] data) {
        JL_Log.w(tag, "saveToDb","保存运动记录到本地数据库");

        String uid = HealthApplication.getAppViewModel().getUid();
        if (TextUtils.isEmpty(uid)) {
            JL_Log.w(tag, "saveToDb"," uid isEmpty");
            return;
        }
        SportRecord sportRecord = SportRecord.from(data);
        sportRecord.setUid(uid);
        SportRecordDao sportRecordDao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).SportRecordDao();
        sportRecordDao.insert(sportRecord);
    }
}
