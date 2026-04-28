package com.jieli.healthaide.ui.device.more;

import android.text.TextUtils;

import com.jieli.healthaide.tool.notification.NotificationHelper;
import com.jieli.healthaide.ui.device.watch.WatchViewModel;
import com.jieli.healthaide.util.HealthConstant;

import java.util.List;

public class MessageSyncViewModel extends WatchViewModel {
    private final NotificationHelper mNotificationHelper = NotificationHelper.getInstance();


    public boolean isOpenMessageSync() {
        return mNotificationHelper.isEnableNotification();
    }

    public boolean isSyncWeChat() {
        return mNotificationHelper.isSelectedApp(HealthConstant.PACKAGE_NAME_WECHAT);
    }

    public boolean isSyncQQ() {
        return mNotificationHelper.isSelectedApp(HealthConstant.PACKAGE_NAME_QQ);
    }

    public boolean isSyncSms() {
        return mNotificationHelper.isSelectedApp(HealthConstant.PACKAGE_NAME_SYS_MESSAGE);
    }

    public boolean isSyncOther() {
        return mNotificationHelper.isAllowOther();
    }

    public List<String> getSelectedApp() {
        return mNotificationHelper.getPackageObserverList();
    }

    public void addAppPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) return;
        mNotificationHelper.addPackageName(packageName);
    }

    public void removeAppPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) return;
        mNotificationHelper.removePackageName(packageName);
    }

    public void setEnableNotification(boolean enable) {
        mNotificationHelper.setEnableNotification(enable);
    }

    public void setNotFilter(boolean value) {
        mNotificationHelper.setAllowOther(value);
    }

}