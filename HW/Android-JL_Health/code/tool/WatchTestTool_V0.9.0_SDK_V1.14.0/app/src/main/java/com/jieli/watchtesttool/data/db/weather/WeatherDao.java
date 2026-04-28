package com.jieli.watchtesttool.data.db.weather;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 天气表操作
 * @since 2023/1/13
 */
@Dao
public interface WeatherDao {

    @Insert(entity = WeatherEntity.class)
    void insert(WeatherEntity... entities);

    @Delete(entity = WeatherEntity.class)
    void delete(WeatherEntity... entities);

    @Update(entity = WeatherEntity.class)
    void update(WeatherEntity entity);

    @Query("SELECT * FROM WeatherEntity WHERE mac LIKE :mac ORDER BY time DESC")
    List<WeatherEntity> queryWeathers(String mac);

    @Query("SELECT * FROM weatherentity WHERE mac LIKE :mac AND province LIKE :province AND city LIKE :city AND time = :time LIMIT 1")
    WeatherEntity queryWeather(String mac, String province, String city, long time);

}
