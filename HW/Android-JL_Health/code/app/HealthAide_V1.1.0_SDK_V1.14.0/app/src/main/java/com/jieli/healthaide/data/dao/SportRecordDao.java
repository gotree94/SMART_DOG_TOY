package com.jieli.healthaide.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jieli.healthaide.data.entity.SportRecord;

import java.util.List;

@Dao
public interface SportRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SportRecord record);

    @Update()
    void update(SportRecord record);

    @Query("SELECT * FROM SportRecord WHERE uid = :uid AND startTime = :startTime LIMIT 1")
    SportRecord findByStartTime(String uid, long startTime);

    @Query("SELECT * FROM SportRecord WHERE uid = :uid AND startTime = :startTime LIMIT 1")
    SportRecord hasRead(String uid, long startTime);

    @Query("SELECT * FROM SportRecord  ")
    List<SportRecord> getAll();

    @Query("SELECT * FROM SportRecord WHERE uid = :uid AND sync = :sync")
    List<SportRecord> findBySync(String uid, boolean sync);

    @Query("SELECT * FROM SportRecord WHERE uid =:uid AND type > 0  AND startTime > 0  ORDER BY startTime DESC LIMIT 1")
    SportRecord getLastData(String uid);

    @Query("SELECT * FROM SportRecord WHERE uid =:uid AND type > 0  AND startTime > 0  ORDER BY startTime DESC LIMIT 1")
    LiveData<SportRecord> getLastLiveData(String uid);


    @Query("SELECT * FROM SportRecord WHERE uid =:uid AND type > 0  ORDER BY startTime DESC  limit :pageSize offset:offset ")
    List<SportRecord> selectByPage(String uid, int pageSize, int offset);

    @Query("DELETE FROM SportRecord ")
    void clean();

}
