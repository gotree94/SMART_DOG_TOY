package com.jieli.healthaide.data.db;

import android.annotation.SuppressLint;
import android.content.Context;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.dao.AICloudMessageDao;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.dao.LocationDao;
import com.jieli.healthaide.data.dao.SportRecordDao;


/**
 * @ClassName: HealthDataDbHelper
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/5/6 10:23
 */
public class HealthDataDbHelper {
    private final static String TAG = HealthDataDbHelper.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static HealthDataDbHelper instance;

    private final HealthDatabase mHealthDatabase;

    private HealthDataDbHelper() {
        Context context = HealthApplication.getAppViewModel().getApplication();
        if (null == context) {
            throw new NullPointerException("Application is null.");
        }
        mHealthDatabase = HealthDatabase.buildHealthDb(context);
    }

    public static HealthDataDbHelper getInstance() {
        if (null == instance) {
            synchronized (HealthDataDbHelper.class) {
                if (null == instance) {
                    instance = new HealthDataDbHelper();
                }
            }
        }
        return instance;
    }

    public HealthDao getHealthDao() {
        return mHealthDatabase.HealthDao();
    }

    public SportRecordDao getSportRecordDao() {
        return mHealthDatabase.SportRecordDao();
    }

    public LocationDao getLocationDao() {
        return mHealthDatabase.LocationDao();
    }

    public AICloudMessageDao getAICloudMessageDao() {
        return mHealthDatabase.AICloudMessageDao();
    }

    public HealthDatabase getHealthDatabase() {
        return mHealthDatabase;
    }
}
