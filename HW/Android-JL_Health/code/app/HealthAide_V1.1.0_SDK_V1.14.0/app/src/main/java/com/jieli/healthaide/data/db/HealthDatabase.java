package com.jieli.healthaide.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jieli.healthaide.data.dao.AICloudMessageDao;
import com.jieli.healthaide.data.dao.HealthDao;
import com.jieli.healthaide.data.dao.LocationDao;
import com.jieli.healthaide.data.dao.SportRecordDao;
import com.jieli.healthaide.data.dao.UserDao;
import com.jieli.healthaide.data.entity.AICloudMessageEntity;
import com.jieli.healthaide.data.entity.HealthEntity;
import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 健康数据库抽象类
 *
 * @author zqjasonZhong
 * @since 2021/3/4
 */
@Database(entities = {User.class, SportRecord.class,HealthEntity.class, LocationEntity.class, AICloudMessageEntity.class}, version = 3)
public abstract class HealthDatabase extends RoomDatabase {
    private final static String DB_NAME = "jl_health.db";
    private volatile static HealthDatabase instance;
    private final ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    public abstract UserDao UserDao();
    public abstract SportRecordDao SportRecordDao();
    public abstract HealthDao HealthDao();
    public abstract LocationDao LocationDao();
    public abstract AICloudMessageDao AICloudMessageDao();

    public static HealthDatabase getInstance() {
        return instance;
    }

    public static HealthDatabase buildHealthDb(Context context) {
        if (instance == null) {
            synchronized (HealthDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context, HealthDatabase.class, DB_NAME)
                            .allowMainThreadQueries()
 //                            .createFromAsset("databases/jl_health.db")//todo 测试使用的预存健康数据
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return instance;
    }

    public ExecutorService getThreadPool() {
        return mThreadPool;
    }

    public void destroy() {
        if (!mThreadPool.isShutdown()) {
            mThreadPool.shutdownNow();
        }
        instance.close();
        instance = null;
    }
    /**
     * 数据库版本 2->3 增加了 AICloudMessageEntity 表
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `AICloudMessageEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uid` TEXT NOT NULL, `devMac` TEXT NOT NULL, `role` INTEGER NOT NULL, `time` INTEGER NOT NULL, `revId` INTEGER NOT NULL, `aiCloudState` INTEGER NOT NULL, `text` TEXT)");
        }
    };
}
