package com.jieli.healthaide.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jieli.healthaide.data.entity.HealthEntity;

import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 5/27/21
 * @desc :
 */
@Dao
public interface HealthDao {

    @Query("DELETE  FROM HEALTHENTITY WHERE id > 0")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HealthEntity healthEntity);


    @Query("SELECT * FROM HealthEntity WHERE  type == :type AND uid == :uid AND   id == :id LIMIT 1")
    HealthEntity findHealthById(byte type, String uid, long id);

    @Query("SELECT * FROM HEALTHENTITY WHERE type == :type AND uid == :uid AND time >= :start AND  time < :end ORDER BY time")
    LiveData<List<HealthEntity>> findHealthByDate(byte type, String uid, long start, long end);

    @Query("SELECT * FROM HEALTHENTITY WHERE type == :type AND uid == :uid ORDER BY id DESC LIMIT 1")
    LiveData<HealthEntity> findHealthLiveDataLast(byte type, String uid);

    @Query("SELECT * FROM HEALTHENTITY WHERE type == :type AND uid == :uid AND time >= :start AND  time < :end")
    LiveData<HealthEntity> findHealthLiveDataByDate(byte type, String uid, long start, long end);

    @Query("SELECT * FROM HEALTHENTITY WHERE uid = :uid AND  id > 0 ")
    LiveData<HealthEntity> findAll(String uid);

    @Query("SELECT * FROM HEALTHENTITY WHERE uid = :uid AND   sync == :sync")
    List<HealthEntity> findBySync(String uid, boolean sync);

    @Query("SELECT * FROM HealthEntity WHERE uid = :uid AND id > 0  ORDER BY time DESC LIMIT 1")
    HealthEntity getLastData(String uid);

    @Query("SELECT * FROM HEALTHENTITY WHERE type == :type AND uid == :uid ORDER BY id DESC LIMIT 1")
    HealthEntity getHealthLiveDataLast(byte type, String uid);

    @Query("SELECT * FROM HealthEntity WHERE uid = :uid AND type == :type AND id > 0  AND time >= :time ORDER BY time DESC LIMIT 1")
    HealthEntity getTodayData(String uid, byte type, long time);

    @Query("DELETE from HealthEntity")
    void clean();

}
