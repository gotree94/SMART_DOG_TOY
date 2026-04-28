package com.jieli.watchtesttool.data.db;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.watchtesttool.WatchApplication;
import com.jieli.watchtesttool.data.db.message.MessageDao;
import com.jieli.watchtesttool.data.db.message.MessageEntity;
import com.jieli.watchtesttool.data.db.sensor.SensorDao;
import com.jieli.watchtesttool.data.db.sensor.SensorEntity;
import com.jieli.watchtesttool.data.db.weather.WeatherDao;
import com.jieli.watchtesttool.data.db.weather.WeatherEntity;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/21
 * @desc :
 */
@Database(entities = {SensorEntity.class, WeatherEntity.class, MessageEntity.class}, version = 2)
public abstract class SensorDbBase extends RoomDatabase {
    private final static String DB_NAME = "jl_sensor.db";
    private volatile static SensorDbBase instance;

    public static SensorDbBase buildDb(Context context) {
        if (instance == null) {
            synchronized (SensorDbBase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context, SensorDbBase.class, DB_NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }

    public static SensorDbBase getInstance() {
        return buildDb(WatchApplication.getWatchApplication());
    }


    public abstract SensorDao sensorDao();

    public abstract WeatherDao weatherDao();

    public abstract MessageDao messageDao();

    public void destroy() {
        instance.close();
        instance = null;
    }

    @SuppressLint("DefaultLocale")
    public static void test() {
        SensorDao sensorDao = buildDb(WatchApplication.getWatchApplication()).sensorDao();
        sensorDao.clean();
        for (int i = 0; i < 100; i++) {
            SensorEntity entity = new SensorEntity();
            entity.setMac(String.format("12:12:23:12:12:%02d", (i % 10)));
            entity.setDevName(String.format("name_%02d", (i % 10)));
            entity.setTime(System.currentTimeMillis());
            entity.setType(String.valueOf(i % 3));
            entity.setData(CHexConver.hexStr2Bytes("010203040506070809" + String.format("%02d", i % 3)));
            sensorDao.insert(entity);
        }
    }


}
