package com.jieli.healthaide.data.db;

import android.os.Handler;
import android.os.Looper;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.util.CalendarUtil;

import java.util.Calendar;

/**
 * @ClassName: VirtualDataHelper
 * @Description: 健康数据的虚拟数据添加
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/9/14 11:28
 */
public class VirtualDataHelper {
    public void adaHeartRateData() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Calendar currentCalendar = Calendar.getInstance();
            for (int i = 0; i < 1000; i++) {
                float targetWeight = 99.8f;
                String uid = HealthApplication.getAppViewModel().getUid();
                HealthEntity entity = new HealthEntity();
                entity.setSync(false);
                entity.setSpace((byte) 10);
                entity.setId(CalendarUtil.removeTime(currentCalendar.getTimeInMillis()));
                entity.setUid(uid);
                entity.setTime(currentCalendar.getTimeInMillis());
                entity.setVersion((byte) 0);
                entity.setType(HealthEntity.DATA_TYPE_HEART_RATE);

                byte[] data = new byte[24 * 6];
                for (int j = 0; j < data.length; j++) {
                    data[j] = (byte) (Math.random() * 160 + 40);
                }
                entity.setData(data);
                HealthDataDbHelper.getInstance().getHealthDao().insert(entity);
                currentCalendar.add(Calendar.DAY_OF_YEAR, -1);
            }
        });
    }
}
