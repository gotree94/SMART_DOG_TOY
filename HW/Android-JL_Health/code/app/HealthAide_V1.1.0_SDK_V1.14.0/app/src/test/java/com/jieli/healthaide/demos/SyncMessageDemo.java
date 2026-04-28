package com.jieli.healthaide.demos;

import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.util.HealthConstant;
import com.jieli.jl_rcsp.interfaces.watch.OnWatchOpCallback;
import com.jieli.jl_rcsp.model.NotificationMsg;
import com.jieli.jl_rcsp.model.base.BaseError;

import org.junit.Test;

import java.util.Calendar;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/12/1
 */
public class SyncMessageDemo {
    private final long time = Calendar.getInstance().getTimeInMillis();

    @Test
    void addSyncMessage() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //模拟微信消息
        String appName = HealthConstant.PACKAGE_NAME_WECHAT;
        NotificationMsg msg = new NotificationMsg()
                .setAppName(appName)
                .setFlag(NotificationHelper.getNotificationFlag(appName))
                .setContent("帅小伙:[1条] 你好，欢迎使用健康助手，乐享运动，助力健康!")
                .setTitle("测试消息")
                .setTime(time)
                .setOp(0);
        watchManager.pushMessageInfo(msg, new OnWatchOpCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                 //回调成功
            }

            @Override
            public void onFailed(BaseError error) {
                //回调失败
                //error： 错误信息
            }
        });
    }

    @Test
    void removeSyncMessage() {
        //WatchManager是WatchOpImpl的子类，须在1.3配置好sdk
        WatchManager watchManager = WatchManager.getInstance();
        //模拟撤销微信消息
        String appName = HealthConstant.PACKAGE_NAME_WECHAT;
        NotificationMsg msg = new NotificationMsg()
                .setAppName(appName)
                .setFlag(NotificationHelper.getNotificationFlag(appName))
                .setTime(time)
                .setOp(1);
        watchManager.removeMessageInfo(msg, new OnWatchOpCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //回调成功
            }

            @Override
            public void onFailed(BaseError error) {
                //回调失败
                //error： 错误信息
            }
        });
    }
}
