package com.jieli.healthaide.tool.notification;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.jieli.healthaide.tool.config.ConfigHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc App信息管理
 * @since 2021/7/20
 */
public class AppInfoManager {
    @SuppressLint("StaticFieldLeak")
    private volatile static AppInfoManager manager;
    private final List<String> appList;
    private final ConfigHelper mConfigHelper = ConfigHelper.getInstance();

    private AppInfoManager() {
        String value = mConfigHelper.getAppListJson();
        if (!TextUtils.isEmpty(value)) {
            String[] array = value.split(",");
            appList = new ArrayList<>();
            appList.addAll(Arrays.asList(array));
        } else {
            appList = new ArrayList<>();
        }
    }

    public static AppInfoManager getInstance() {
        if (null == manager) {
            synchronized (AppInfoManager.class) {
                if (null == manager) {
                    manager = new AppInfoManager();
                }
            }
        }
        return manager;
    }

    public List<String> getObservedAppList() {
        return appList;
    }

    public boolean saveObservedPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName)) return false;
        boolean ret = appList.contains(packageName);
        if (!ret) {
            ret = appList.add(packageName);
            if (ret) {
                syncLocalCache(appList);
            }
        }
        return ret;
    }

    public boolean removeObservedPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName)) return false;
        boolean ret = appList.remove(packageName);
        if (ret) {
            syncLocalCache(appList);
        }
        return ret;
    }

    public void release() {
        appList.clear();
        manager = null;
    }

    public boolean isNotificationEnable() {
        return mConfigHelper.isNotificationEnable();
    }

    public void saveNotificationEnable(boolean enable) {
        mConfigHelper.setNotificationEnable(enable);
    }

    public boolean isAllowOtherApp() {
        if (!isNotificationEnable()) return false;
        return mConfigHelper.isAllowOtherApp();
    }

    public void setAllowOtherApp(boolean allow){
        mConfigHelper.setAllowOtherApp(allow);
    }

    private void syncLocalCache(List<String> list) {
        StringBuilder text = new StringBuilder();
        for (String packageName : list) {
            text.append(packageName).append(",");
        }
        mConfigHelper.setAppListJson(text.toString());
    }
}
