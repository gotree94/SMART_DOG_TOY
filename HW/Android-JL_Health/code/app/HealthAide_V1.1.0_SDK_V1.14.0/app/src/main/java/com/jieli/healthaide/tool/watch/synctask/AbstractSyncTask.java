package com.jieli.healthaide.tool.watch.synctask;

import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/1
 * @desc :
 */
public abstract class AbstractSyncTask implements SyncTask {
    /*| 0x1000 - 0x1fff: 为服务器任务
     *| 0x2000 - 0x2fff：为设备任务
     */
    public static final int TASK_TYPE_REQUEST_UID = 0x1000;
    public static final int TASK_TYPE_SYNC_SPORT_RECORD = 0x1001;
    public static final int TASK_TYPE_SYNC_HEALTH_DATA = 0x1002;
    public static final int TASK_TYPE_UPLOAD_SPORT_RECORD = 0x1003;

    public static final int TASK_TYPE_SYNC_DEVICE_SMALL_FILE = 0x2000;
    public static final int TASK_TYPE_SYNC_DEVICE_SPORT_FILE = 0x2001;
    public static final int TASK_TYPE_SYNC_DEVICE_SPORT_STATUS = 0x2002;
    public static final int TASK_TYPE_SYNC_DEVICE_HEALTH_DATA = 0x2003;
    public static final int TASK_TYPE_SYNC_DEVICE_WEATHER = 0x2004;
    public static final int TASK_TYPE_SYNC_DIAL_LIST_INFO = 0X2005;
    public static final int TASK_TYPE_SYNC_DEVICE_LOGCAT = 0x2006;

    protected SyncTaskFinishListener finishListener;
    final String tag = getClass().getSimpleName();

    public AbstractSyncTask(SyncTaskFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    @Override
    public void setFinishListener(SyncTaskFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSyncTask that = (AbstractSyncTask) o;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType());
    }
}
