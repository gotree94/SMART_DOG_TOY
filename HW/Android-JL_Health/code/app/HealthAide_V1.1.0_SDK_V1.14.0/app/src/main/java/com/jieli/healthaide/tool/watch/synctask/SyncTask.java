package com.jieli.healthaide.tool.watch.synctask;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/20
 * @desc :和固件同步以及服务器的数据同步任务都使用该接口的子类
 */
public interface SyncTask {

    int getType();

    void start();

    void setFinishListener(SyncTaskFinishListener finishListener);

}
