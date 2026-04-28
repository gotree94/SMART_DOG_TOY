package com.jieli.healthaide.tool.watch.synctask;

import android.text.TextUtils;

import com.jieli.bluetooth_connect.util.JL_Log;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.db.HealthDatabase;
import com.jieli.healthaide.data.entity.HealthEntity;

import java.util.Arrays;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/1
 * @desc :
 */
public class DeviceHealthDataSyncTask extends SmallFileSyncTask {


    public DeviceHealthDataSyncTask(byte type, SyncTaskFinishListener finishListener) {
        super(type, finishListener);
    }

    @Override
    protected boolean isInLocal(byte[] data) {
        //todo 判断是否需要更新数据库
        HealthEntity healthEntity = HealthEntity.from(data);
        String uid = HealthApplication.getAppViewModel().getUid();

        if (TextUtils.isEmpty(uid)) {
            JL_Log.w(tag, "isInLocal", "uid isEmpty");
            return false;
        }
        healthEntity.setUid(uid);
        HealthDao healthDao = HealthDatabase.buildHealthDb(HealthApplication.getAppViewModel().getApplication()).HealthDao();
        HealthEntity dbEntity = healthDao.findHealthById(healthEntity.getType(), uid, healthEntity.getId());
        boolean ret = dbEntity != null && Arrays.equals(dbEntity.getCrcCode(), healthEntity.getCrcCode());
        JL_Log.w(tag, "isInLocal", "data check result : " + ret);

        return ret;
    }

    @Override
    protected void saveToDb(byte[] data) {
        String uid = HealthApplication.getAppViewModel().getUid();
        if (TextUtils.isEmpty(uid)) {
            JL_Log.w(tag, "saveToDb", "uid isEmpty");
            return;
        }

        HealthEntity entity = HealthEntity.from(data);
        if (entity != null) {
            entity.setUid(uid);
            HealthDataDbHelper.getInstance().getHealthDao().insert(entity);
        }
    }

    @Override
    public int getType() {
        return TASK_TYPE_SYNC_DEVICE_SMALL_FILE + type;
    }
}
