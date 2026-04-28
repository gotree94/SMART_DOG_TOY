package com.jieli.healthaide.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jieli.healthaide.data.entity.AICloudMessageEntity;

import java.util.List;

/**
 * @ClassName: AICloudMessageDao
 * @Description: AI云消息记录 数据库
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/7/19 16:28
 */
@Dao
public interface AICloudMessageDao {


    @Query("SELECT * FROM AICloudMessageEntity WHERE uid = :uid AND id > 0 AND time < :validTime ORDER BY time DESC LIMIT :index,16")
    List<AICloudMessageEntity> getAICloudMessageByLimit(String uid, long index, long validTime);

    @Query("DELETE from AICloudMessageEntity")
    void clean();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AICloudMessageEntity entity);

    @Update()
    void update(AICloudMessageEntity entity);

    //@Delete(entity = AICloudMessageEntity.class)
    //void deleteEntity(AICloudMessageEntity entity);

    @Delete(entity = AICloudMessageEntity.class)
    void deleteEntities(List<AICloudMessageEntity> entities);
}
