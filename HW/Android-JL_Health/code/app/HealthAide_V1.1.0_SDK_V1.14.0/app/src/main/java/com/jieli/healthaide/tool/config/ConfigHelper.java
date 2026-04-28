package com.jieli.healthaide.tool.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.jieli.component.utils.SystemUtil;
import com.jieli.healthaide.BuildConfig;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.util.FormatUtil;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 配置辅助类
 * @since 2022/9/1
 */
public class ConfigHelper {
    @SuppressLint("StaticFieldLeak")
    private static volatile ConfigHelper instance;
    private final SharedPreferences preferences;
    private final int appVersionCode;

    //Permission
    private static final String KEY_PERMISSION = "permission";
    private static final String KEY_LOGIN_TYPE = "login_type";
    //Launcher
//    private static final String KEY_POLICY_AGREE = "KEY_POLICY_AND_USER_ARGEEMENT";
    private static final String KEY_POLICY_AGREE = "policy_agreement"; //升级支持Android 12+，修改对应KEY
    //Login
    private static final String KEY_CACHE_ACCOUNT = "KEY_CACHE_MOBILE";
    private static final String KEY_LOGIN_ACCOUNT = "KEY_LOGIN_MOBILE";
    //Setting
    private static final String KEY_UNIT_TYPE = "unit_converter_type";
    //History
    private static final String KEY_REMOVE_HISTORY_ID = "remove_history_id";
    //Message Sync
    private static final String KEY_ENABLE_NOTIFICATION = "enable_notification";
    private static final String KEY_ALLOW_OTHER_APP = "allow_other_app";
    private static final String KEY_APP_LIST = "app_list";

    /// 测试配置

    /**
     * 是否使能日志调试功能
     */
    private static final String KEY_ENABLE_LOG_FUNCTION = "enable_log_function";
    /**
     * 是否使能设备认证功能
     */
    private static final String KEY_ENABLE_DEVICE_AUTH = "enable_device_auth";
    /**
     * 是否开启本地OTA测试
     */
    private static final String KEY_ENABLE_LOCAL_OTA_TEST = "enable_local_ota_test";

    private ConfigHelper(Context context) {
        this.appVersionCode = SystemUtil.getVersion(context);
        this.preferences = context.getSharedPreferences("health_config_data", Context.MODE_PRIVATE);
    }

    public static ConfigHelper getInstance() {
        if (null == instance) {
            synchronized (ConfigHelper.class) {
                if (null == instance) {
                    instance = new ConfigHelper(HealthApplication.getAppViewModel().getApplication());
                }
            }
        }
        return instance;
    }

    public boolean isAgreePolicy() {
        return preferences.getBoolean(KEY_POLICY_AGREE, false);
    }

    public void setAgreePolicy(boolean isAgree) {
        preferences.edit().putBoolean(KEY_POLICY_AGREE, isAgree).apply();
    }

    /**
     * @return 0: 手机号码登录 1：邮箱地址登录
     */
    public int getLoginType() {
        return preferences.getInt(KEY_LOGIN_TYPE, 0);
    }

    /**
     * @param loginType 0: 手机号码登录 1：邮箱地址登录
     */
    public void setLoginType(int loginType) {
        preferences.edit().putInt(KEY_LOGIN_TYPE, loginType).apply();
    }

    public String getCacheAccount() {
        return preferences.getString(KEY_CACHE_ACCOUNT, "");
    }

    public void setCacheAccount(String cacheAccount) {
        if (TextUtils.isEmpty(cacheAccount) || (!FormatUtil.checkPhoneNumber(cacheAccount) && !FormatUtil.checkEmailAddress(cacheAccount)))
            return;
        preferences.edit().putString(KEY_CACHE_ACCOUNT, cacheAccount).apply();
    }

    public String getLoginAccount() {
        return preferences.getString(KEY_LOGIN_ACCOUNT, "");
    }

    public void setLoginAccount(String loginAccount) {
        if (TextUtils.isEmpty(loginAccount) || (!FormatUtil.checkPhoneNumber(loginAccount) && !FormatUtil.checkEmailAddress(loginAccount)))
            return;
        preferences.edit().putString(KEY_LOGIN_ACCOUNT, loginAccount).apply();
    }

    public int getUnitType() {
        return preferences.getInt(KEY_UNIT_TYPE, BaseUnitConverter.TYPE_METRIC);
    }

    public void setUnitType(int unitType) {
        preferences.edit().putInt(KEY_UNIT_TYPE, unitType).apply();
    }

    public String getRemoveHistoryId() {
        return preferences.getString(KEY_REMOVE_HISTORY_ID, "");
    }

    public void setRemoveHistoryId(String id) {
        if (null == id) {
            preferences.edit().remove(KEY_REMOVE_HISTORY_ID).apply();
            return;
        }
        preferences.edit().putString(KEY_REMOVE_HISTORY_ID, id).apply();
    }

    public boolean isNotificationEnable() {
        return preferences.getBoolean(KEY_ENABLE_NOTIFICATION, false);
    }

    public void setNotificationEnable(boolean enable) {
        boolean oldValue = isNotificationEnable();
        if (oldValue != enable) {
            preferences.edit().putBoolean(KEY_ENABLE_NOTIFICATION, enable).apply();
        }
    }

    public boolean isAllowOtherApp() {
        return preferences.getBoolean(KEY_ALLOW_OTHER_APP, false);
    }

    public void setAllowOtherApp(boolean allow) {
        preferences.edit().putBoolean(KEY_ALLOW_OTHER_APP, allow).apply();
    }

    public String getAppListJson() {
        return preferences.getString(KEY_APP_LIST, "");
    }

    public void setAppListJson(String json) {
        if (null == json) {
            preferences.edit().remove(KEY_APP_LIST).apply();
            return;
        }
        preferences.edit().putString(KEY_APP_LIST, json).apply();
    }

    public boolean isBanRequestPermission(String permission) {
        String key = getPermissionKey(permission);
        int value = preferences.getInt(key, -1);
        return appVersionCode == value;
    }

    public void setBanRequestPermission(String permission) {
        String key = getPermissionKey(permission);
        preferences.edit().putInt(key, appVersionCode).apply();
    }

    public boolean isEnableLogFunc() {
        return preferences.getBoolean(KEY_ENABLE_LOG_FUNCTION, BuildConfig.DEBUG);
    }

    public void setEnableLogFunc(boolean enable) {
        preferences.edit().putBoolean(KEY_ENABLE_LOG_FUNCTION, enable).apply();
    }

    public boolean isEnableDeviceAuth() {
        return preferences.getBoolean(KEY_ENABLE_DEVICE_AUTH, true);
    }

    public void setEnableDeviceAuth(boolean enable) {
        preferences.edit().putBoolean(KEY_ENABLE_DEVICE_AUTH, enable).apply();
    }

    public boolean isEnableLocalOTATest() {
        return preferences.getBoolean(KEY_ENABLE_LOCAL_OTA_TEST, false);
    }

    public void setEnableLocalOtaTest(boolean enable) {
        preferences.edit().putBoolean(KEY_ENABLE_LOCAL_OTA_TEST, enable).apply();
    }

    private String getPermissionKey(String permission) {
        return KEY_PERMISSION + "_" + permission;
    }
}
