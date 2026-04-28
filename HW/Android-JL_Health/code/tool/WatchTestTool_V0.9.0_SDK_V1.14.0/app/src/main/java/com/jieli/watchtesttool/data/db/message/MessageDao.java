package com.jieli.watchtesttool.data.db.message;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 信息表操作
 * @since 2023/1/13
 */
@Dao
public interface MessageDao {

    @Insert(entity = MessageEntity.class)
    void insert(MessageEntity... entities);

    @Delete(entity = MessageEntity.class)
    void delete(MessageEntity... entities);

    @Transaction
    @Query("SELECT * FROM MessageEntity WHERE mac LIKE :mac ORDER BY updateTime DESC")
    List<MessageEntity> queryMessages(String mac);

    @Transaction
    @Query("SELECT * FROM MessageEntity WHERE mac LIKE :mac AND packageName LIKE :packageName AND updateTime = :time LIMIT 1")
    MessageEntity queryMessage(String mac, String packageName, long time);

}
