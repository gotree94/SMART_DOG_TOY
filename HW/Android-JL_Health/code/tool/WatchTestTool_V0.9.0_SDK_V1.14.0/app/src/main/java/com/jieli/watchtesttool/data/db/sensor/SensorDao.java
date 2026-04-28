package com.jieli.watchtesttool.data.db.sensor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/21
 * @desc :
 */
@Dao
public interface SensorDao {
    @Insert
    void insert(SensorEntity entity);


    @Query("SELECT * FROM SensorEntity WHERE  (:mac = '' OR mac = :mac)  AND (:type='' OR type = :type) AND (:name = '' OR devName = :name ) ORDER BY time DESC")
    List<SensorEntity> find(String mac,String name, String type);


    @Query("SELECT mac FROM SensorEntity GROUP BY mac")
    String[] groupByMac();

    @Query("SELECT devName FROM SensorEntity GROUP BY mac")
    String[] groupByName();

    @Query("SELECT type FROM SensorEntity GROUP BY type")
    String[] groupByType();


    @Query("DELETE FROM SensorEntity WHERE id > 0")
    void clean();

    @Query("SELECT * FROM SensorEntity")
    List<SensorEntity> getAll();
}
