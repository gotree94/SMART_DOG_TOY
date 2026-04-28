package com.jieli.healthaide.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jieli.healthaide.data.entity.LocationEntity;

import java.util.List;

@Dao
public interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocationEntity locationEntity);


    @Query("SELECT * FROM LocationEntity WHERE uid = :uid AND startTime = :startTime LIMIT 1")
    LocationEntity findByStartTime(String uid, long startTime);


    @Query("SELECT * FROM LocationEntity  WHERE uid = :uid")
    List<LocationEntity> getAll(String uid);

    @Query("DELETE FROM LocationEntity ")
    void clean();

}
