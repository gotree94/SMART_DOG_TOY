package com.jieli.healthaide.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jieli.healthaide.data.entity.User;

import java.util.List;

/**
 * 用户数据操作接口
 *
 * @author zqjasonZhong
 * @since 2021/3/4
 */
@Dao
public interface UserDao {

    @Insert(entity = User.class)
    void addUser(User user);

    @Delete(entity = User.class)
    void deleteUser(User user);

    @Delete(entity = User.class)
    void deleteUsers(List<User> users);

    @Update(entity = User.class)
    void updateUserInfo(User user);

    @Query("SELECT * FROM User")
    List<User> getAllUsers();

    @Query("SELECT * FROM User WHERE phone_number LIKE :phone LIMIT 1")
    User getUserByPhone(String phone);
}
